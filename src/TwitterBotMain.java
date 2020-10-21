/* Ru Ferguson
 * 20 October 2020
 * 
 * A Java Twitter bot that uses the an excerpt of the book of Psalms to generate a tweet using a Markov chain of 1.
 * Pressing "m" will also train the tweet generation on tweets scraped from #MondayMotivation on Twitter.
 * All unit tests from Project 1 and 2 are also accessible in this project. */

import processing.core.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.jaunt.JauntException;

import jm.music.data.Score;
import jm.util.Play;
import jm.util.Read;

import java.lang.Character;

public class TwitterBotMain extends PApplet {

	private ArrayList<String> tokens;
	private static String HEYER_TWITTER_URL = "https://twitter.com/wondruful"; // account url
	private static int TWITTER_CHAR_LIMIT = 280; // originally 140, updated to new max
	
	//useful constant strings -- for instance if you want to make sure your tweet ends on a space or ending punctuation, etc.
	private static final String fPUNCTUATION = "\",.!?;:()/\\|";
	private static final String fENDPUNCTUATION = ".!?;,";
	private static final String fREALENDPUNCTUATION = ".!?";

	private static final String fWHITESPACE = "\t\r\n ";
	private static final String fVOWELS = "aeiou";
	
	//example twitter hastag search term
	private static final String fPASSIVEAGG = "inspirationalquotes";
	private static final String fCOMMA = ","; 
	
	//handles twitter api
	TwitterInteraction tweet; 
	
	int tweetSize = 40;
	MarkovGenerator<String> markovTweetGenerator;
	int mode = 0;
	
	UnitTests unitTest = new UnitTests(); // create unit tests
	MelodyPlayer player; //play a midi sequence

	public static void main(String[] args) {
		PApplet.main("TwitterBotMain");  
	}

	public void settings() {
		size(350, 500); //dummy window
	}

	public void setup() {
		tweet = new TwitterInteraction(); 
						
		loadNovel("data/psalms_excerpt.txt"); //TODO: must train from another source
		
		player = new MelodyPlayer(this, 100.0f);
		player.setup();
	}


	//this loads the text file given a path p
	void loadNovel(String p) {
		String filePath = getPath(p);
		Path path = Paths.get(filePath);
		tokens = new ArrayList<String>();
		try {
			List<String> lines = Files.readAllLines(path);

			for (int i = 0; i < lines.size(); i++) {

				TextTokenizer tokenizer = new TextTokenizer(lines.get(i));
				ArrayList<String> t = tokenizer.parseSearchText();
				tokens.addAll(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
			println("Oopsie! We had a problem reading a file!");
		}
	}
	
	void printTokens() {
		for (int i = 0; i < tokens.size(); i++)
			print(tokens.get(i) + " ");
	}

	//get the relative file path 
	String getPath(String path) {
		String filePath = "";
		try {
			filePath = URLDecoder.decode(getClass().getResource(path).getPath(), "UTF-8");

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filePath;
	}

	public void draw() {
		//ellipse(width / 2, height / 2, second(), second());
	    player.play();		//play each note in the sequence -- the player will determine whether is time for a note onset
	    background(250);
	    showInstructions(); 
	}
	
	void makeTweet() {
		markovTweetGenerator = new MarkovGenerator();
		
		// if mode is 1, train also from Monday Motivation Tweets
		if (mode == 1) {
			ArrayList<String> tweetResults = tweet.searchForTweets("#MondayMotivation");
			for (int i = 0; i < tweetResults.size(); i++) {
				TextTokenizer tokenizer = new TextTokenizer(tweetResults.get(i));
				ArrayList<String> t = tokenizer.parseSearchText();
				tokens.addAll(t);
			} 
			//for (int i = 0; i < tweetResults.size(); i++) { // prints out the results of the search on twitter
			//	println(tweetResults.get(i)); 
			//}
		}
		
		markovTweetGenerator.train(tokens);
		
		ArrayList<String> myTweet = markovTweetGenerator.generate((int) random(5, tweetSize));
		
		myTweet = removeExtras(myTweet);
		myTweet = removeConsecutiveDups(myTweet);
		myTweet = checkCase(myTweet);

		String genString = arrayToString(myTweet);
		genString = checkChars(genString);			
		genString = checkLength(genString);
		
		String status = "Psalms " + (int) random(150, 300) + ":" + (int) random(1, 45) + " ";
		for (int i = 0; i < genString.length(); i++) {
			status = status + genString.charAt(i);
		}
		
		System.out.println("Status: " + status);		
		tweet.updateTwitter(status);	// Post to Twitter
		System.out.println("Posted!");
	}
	
	// remove any Twitter handles or email addresses
	ArrayList<String> removeExtras(ArrayList<String> generated) {
		  for(int i = 0; i < generated.size(); i++) {
			  String currentToken = generated.get(i);
			  if(currentToken.contains("@")) {
				  generated.remove(i);
			  } else if(currentToken.contains("#")) {
				  generated.remove(i);
			  } else if(currentToken.contains("…")) {
				  generated.remove(i);
			  } else if(currentToken.contains("https")) {
				  generated.remove(i);
			  } else if(currentToken.contains("rt")) {
				  generated.remove(i);
			  } else if (currentToken.length() == 1) {
				  if (currentToken != "a" || currentToken != "i") {
					  generated.remove(i);
				  }
			  }
		  }
		  return generated;
	}
	
	// remove any words repeated consecutively
	ArrayList<String> removeConsecutiveDups(ArrayList<String> generated) {  
		  ArrayList<String> newList = new ArrayList<String>();
		  newList.add(generated.get(0));
		  for(int i = 1; i < generated.size(); i++) {
			  if(generated.get(i - 1) != generated.get(i)) {
				  newList.add(generated.get(i));
		    }
		  }
		  return newList;
	}
	
	// convert the generated ArrayList of strings into a single string
	String arrayToString(ArrayList<String> generated) { 
		String str = "";
		for (int i = 0; i < generated.size(); i++) {
			str = str + generated.get(i) + " ";
		}
		return str;
	}
	
	// check some basic formatting (punctuation and extraneous spaces)
	String checkChars(String generated) { 
		String str = "";
		for (int i = 0; i < generated.length(); i++) { // check for random punctuation
			int isPunctuation = fPUNCTUATION.indexOf(generated.charAt(i));
			if (isPunctuation == -1 && !Character.isDigit(generated.charAt(i))) {
				str = str + generated.charAt(i);
			}
			int isWhiteSpace = fWHITESPACE.indexOf(generated.charAt(i));
			if (isWhiteSpace != -1) {
				str = str + " ";
			}
		}
		str = str.replaceAll("\\s+", " ");
		str = str.toLowerCase(); // make string all lower case
		return str;
	}
	
	// make sure the tweet is not too long for twitter length restrictions
	String checkLength(String generated) {
		String trimmed = "";
		if (generated.length() > TWITTER_CHAR_LIMIT) {
			for (int i = 0; i < TWITTER_CHAR_LIMIT; i++) {
				trimmed = trimmed + generated.charAt(i);
			}
			return trimmed;
		}
		return generated;
	}
	
	// remove any Twitter handles or email addresses
	ArrayList<String> checkCase(ArrayList<String> generated) {
		  for(int i = 0; i < generated.size(); i++) {
			  String currentToken = generated.get(i);
			  if(currentToken == "his") {
				  currentToken = currentToken.substring(0, 1).toUpperCase() + currentToken.substring(1);
			  } else if(currentToken == "him") {
				  currentToken = currentToken.substring(0, 1).toUpperCase() + currentToken.substring(1);
			  } else if(currentToken == "he") {
				  currentToken = currentToken.substring(0, 1).toUpperCase() + currentToken.substring(1);
			  } else if(currentToken == "god") {
				  currentToken = currentToken.substring(0, 1).toUpperCase() + currentToken.substring(1);
			  } else if(currentToken == "i") {
				  currentToken = currentToken.substring(0, 1).toUpperCase() + currentToken.substring(1);
			  }
			  generated.set(i, currentToken);
		  }
		  return generated;
	}
	
	// this starts & restarts the melody and runs unit tests
	public void keyPressed() {
		if (key == '1') {
			unitTest.P1UnitTest1();
		} else if (key == '2') {
			unitTest.P1UnitTest2();
		} else if (key == '3') {
			unitTest.P1UnitTest3();
		} else if (key == 'q') {
			unitTest.P2UnitTest1();
		} else if (key == 'w') {
			unitTest.P2UnitTest2();
		} else if (key == 'e') {
			unitTest.P2UnitTest3();	
		} else if (key == 'n') {
			mode = 0;
			makeTweet();
		} else if (key == 'm') {
			mode = 1;
			makeTweet();
		}
	}
	
	// display unit test instructions to the user
	public void showInstructions() {
		textAlign(CENTER);
		textSize(20);
		fill(0, 100, 255);
		text("Unit Tests for \nProbability and Markov (Order 1) \nGenerators", width/2, height*1/20);
		text("User Interaction for\n@wondruful Twitter Bot", width/2, height*13/20);
		textSize(16);
		fill(0, 160, 255);
		text("Press 1 for Project 1: Unit Test 1", width/2, height*5/20);
		fill(0, 175, 255);
		text("Press 2 for Project 1: Unit Test 2", width/2, height*6/20); 
		fill(0, 190, 255);
		text("Press 3 for Project 1: Unit Test 3", width/2, height*7/20);
		fill(0, 160, 255);
		text("Press q for Project 2: Unit Test 1", width/2, height*8/20);
	    fill(0, 175, 255);
		text("Press w for Project 2: Unit Test 2", width/2, height*9/20);
		fill(0, 190, 255);
		text("Press e for Project 2: Unit Test 3", width/2, height*10/20);
	    fill(0, 160, 255);
		text("Press n for tweet from Psalms", width/2, height*16/20);
		fill(0, 175, 255);
		text("Press m for tweet from Psalms and\n#MotivationMonday", width/2, height*17/20);
	}
}
