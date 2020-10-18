/* Ru Ferguson
 * 20 October 2020
 * 
 */


import processing.core.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.jaunt.JauntException;

import java.lang.Character;


public class TwitterBotMain extends PApplet {

	private ArrayList<String> tokens;
	private static String HEYER_TWITTER_URL = "https://twitter.com/wondruful"; // account url
	private static int TWITTER_CHAR_LIMIT = 280; // originally 140, updated to new max
	
	//useful constant strings -- for instance if you want to make sure your tweet ends on a space or ending punctuation, etc.
	private static final String fPUNCTUATION = "\",.!?;:()/\\";
	private static final String fENDPUNCTUATION = ".!?;,";
	private static final String fREALENDPUNCTUATION = ".!?";

	private static final String fWHITESPACE = "\t\r\n ";
	
	//example twitter hastag search term
	private static final String fPASSIVEAGG = "inspirationalquotes";
	private static final String fCOMMA = ","; 
	
	//handles twitter api
	TwitterInteraction tweet; 
	
	int tweetSize = 40;
	MarkovGenerator<String> markovTweetGenerator;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main("TwitterBotMain");  //Not really using processing functionality but ya know, you _could_. UI not required.
		
	}

	public void settings() {
		size(300, 300); //dummy window

	};

	public void setup() {
		tweet = new TwitterInteraction(); 
		
		
		
// NOTE: everything starts uncommented. Comment out the calls that you would like to try and use.
				
		loadNovel("data/psalms_excerpt.txt"); //TODO: must train from another source
//		println("Token size: " + tokens.size());

		//TODO: train an AI algorithm (eg, Markov Chain) and generate text for markov chain status
		
		markovTweetGenerator = new MarkovGenerator();
		
		makeTweet();
		//markovTweetGenerator.train(tokens);
		//ArrayList<String> generatedTweet = markovTweetGenerator.generate(tweetSize);
		
		//System.out.println("trained: " + generatedTweet);
		
		//Make sure within Twitter limits (used to be 140 but now is more?)
		//String status = "";
		//for (int i = 0; i < generatedTweet.size(); i++) {
		//	status = status + generatedTweet.get(i) + " ";
		//}
		
		//System.out.println(status);
		//String status = "first tweet?";
		//tweet.updateTwitter(status);				
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

	}
	
	void makeTweet() {
		//ArrayList<String> tweetResults = tweet.searchForTweets("Inspirational Quotes");
		
	//	for (int i = 0; i < tweetResults.size(); i++) {
	//		TextTokenizer tokenizer = new TextTokenizer(tweetResults.get(i));
	//		ArrayList<String> t = tokenizer.parseSearchText();
	//		tokens.addAll(t);
	//	}
		
		markovTweetGenerator.train(tokens);
		
		ArrayList<String> myTweet = markovTweetGenerator.generate((int) random(5, tweetSize));
		
		String genString = arrayToString(myTweet);
		genString = checkChars(genString);			
		genString = checkLength(genString);
		
		String status = "";
		for (int i = 0; i < genString.length(); i++) {
			status = status + genString.charAt(i);
		}
		
		System.out.println("status is: " + status);
	}
	
	String arrayToString(ArrayList<String> generated) {
		String str = "";
		for (int i = 0; i < generated.size(); i++) {
			str = str + generated.get(i) + " ";
		}
		return str;
	}
	
	String checkChars(String generated) {
		String str = "";
		for (int i = 0; i < generated.length(); i++) {
			int isPunctuation = fPUNCTUATION.indexOf(generated.charAt(i));
			if (isPunctuation == -1 && !Character.isDigit(generated.charAt(i))) {
				str = str + generated.charAt(i);
			}
		}
		for (int i = 0; i < 3; i ++) {
			str = str.replace("  ", " ");
			str = str.replace("   ", " ");
			str = str.replace("/t", "");
			str = str.replace("/r", "");
			str = str.replace("/n", "");
		}
		return str;
	}
	
	String checkLength(String generated) {
		String trimmed = "";
		if (generated.length() > TWITTER_CHAR_LIMIT) {
			for (int i = 0; i < TWITTER_CHAR_LIMIT; i++) {
				System.out.println("inhere");
				trimmed = trimmed + generated.charAt(i);
			}
			return trimmed;
		}
		return generated;
	}
	
	/* NOTE: everything starts uncommented. Comment out the calls that you would like to try and use.
		
	loadNovel("data/The Grand Sophy excerpt.txt"); //TODO: must train from another source
	println("Token size: " + tokens.size());

	TODO: train an AI algorithm (eg, Markov Chain) and generate text for markov chain status
		
	// can train on twitter statuses -- note: in your code I would put this part in a separate function
	// but anyhow, here is an example of searrching twitter hashtag. You have to pay $$ to the man to get more results. :(
	// see TwitterInteraction class
		
	ArrayList<String> tweetResults = tweet.searchForTweets("John Cage");
	for (int i = 0; i < tweetResults.size(); i++) {
		println(tweetResults.get(i)); //just prints out the results for now
	}
		
	//Make sure within Twitter limits (used to be 140 but now is more?)
	
	String status = "first tweet?";
	tweet.updateTwitter(status);
				
	//prints the text content of the sites that come up with the google search of dogs
	//you may use this content to train your AI too
	
	Scraper scraper = new Scraper(); 
	ArrayList<String> results;
	try {
		results = scraper.scrapeGoogleResults("dogs");
		// print your results
		System.out.println(results); 
		scraper.scrape("http://google.com",  "dogs"); //see class documentation
	} catch (JauntException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 */

}
