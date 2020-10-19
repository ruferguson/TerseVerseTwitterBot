/* Ru Ferguson
 * 28 September 2020
 * 
 * This class is used for the unit test methods to consolidate code more nicely. */

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;

import processing.core.PApplet;

public class UnitTests  extends PApplet {
	
	MelodyPlayer player; //play a midi sequence
	MidiFileToNotes midiNotes; // read a midi file
	
	ProbabilityGenerator<Integer> pitchGen, initPitchGen;
	ProbabilityGenerator<Double> rhythmGen, initRhythmGen;
	MarkovGenerator<Integer> markovPitchGen;
	MarkovGenerator<Double> markovRhythmGen;
	
	
	UnitTests() {
		String filePath = getPath("mid/MaryHadALittleLamb.mid");
		midiNotes = new MidiFileToNotes(filePath);
		midiNotes.setWhichLine(0);
		pitchGen = new ProbabilityGenerator<Integer>();
		rhythmGen = new ProbabilityGenerator<Double>();
		markovPitchGen = new MarkovGenerator<Integer>();
		markovRhythmGen = new MarkovGenerator<Double>();
		initPitchGen = new ProbabilityGenerator<Integer>();
		initRhythmGen = new ProbabilityGenerator<Double>();
	}
	
	void P1UnitTest1() {	// Project 1: Unit Test 1
		trainP1();

		System.out.println("Pitches:\n\n-----Probability Distribution-----\n");
		for (int i = 0; i < pitchGen.getAlphabetSize(); i++) {
			System.out.println("Token: " + pitchGen.getToken(i) + " | Probability: " +
			pitchGen.getProbability(i));
		}
		System.out.println("\n------------\n\nRhythms:\n\n-----Probability Distribution-----\n");
		for (int i = 0; i < rhythmGen.getAlphabetSize(); i++) {
			System.out.println("Token: " + rhythmGen.getToken(i) + " | Probability: " + 
			rhythmGen.getProbability(i));
		}
		System.out.println("\n------------\n");
	}
	
	void P1UnitTest2() {	// Project 1: Unit Test 2
		trainP1();
		
		System.out.println("20 pitches from one melody generated from Mary Had a Little Lamb:");
		System.out.println(pitchGen.generate(20));
		System.out.println("\n20 rhythms from one melody generated from Mary Had a Little Lamb:");
		System.out.println(rhythmGen.generate(20) + "\n------------\n");
		
	}
	
	void P1UnitTest3() {	// Project 1: Unit Test 3
		ProbabilityGenerator<Integer> melodyPitchGen = new ProbabilityGenerator<Integer>();
		ProbabilityGenerator<Double> melodyRhythmGen = new ProbabilityGenerator<Double>();
		ProbabilityGenerator<Integer> probDistPitchGen = new ProbabilityGenerator<Integer>();
		ProbabilityGenerator<Double> probDistRhythmGen = new ProbabilityGenerator<Double>();
		
		ArrayList<Integer> newSongPitches = new ArrayList<Integer>();
		ArrayList<Double> newSongRhythms = new ArrayList<Double>();
		
		melodyPitchGen.train(midiNotes.getPitchArray());
		melodyRhythmGen.train(midiNotes.getRhythmArray());

		for (int i = 0; i < 9999; i++) {
			newSongPitches = melodyPitchGen.generate(20);
			newSongRhythms = melodyRhythmGen.generate(20);	
			probDistPitchGen.train(newSongPitches);
			probDistRhythmGen.train(newSongRhythms);
		}
		
		System.out.println("Probability of Generated Pitches after 10,000 iterations of 20 note melodies:\n\n-----Probability Distribution-----\n");
		for (int i = 0; i < probDistPitchGen.getAlphabetSize(); i++) {
			System.out.println("Token: " + probDistPitchGen.getToken(i) + " | Probability: " + probDistPitchGen.getProbability(i));
		}
		System.out.println("\n------------\n\nProbability of Generated Rhythms after 10,000 iterations of 20 note melodies:\n\n-----Probability Distribution-----\n");
		for (int i = 0; i < probDistRhythmGen.getAlphabetSize(); i++) {
			System.out.println("Token: " + probDistRhythmGen.getToken(i) + " | Probability: " + probDistRhythmGen.getProbability(i));
		}
		System.out.println("\n------------\n");
	}
	
	void P2UnitTest1() {	// Project 2: Unit Test 1
		trainP2();

		System.out.println("\nPitches:\n\n-----Transition Table-----\n\n   " + markovPitchGen.getAlphabet());
		for (int i = 0; i < markovPitchGen.getAlphabetSize(); i++) {
	        System.out.println(markovPitchGen.getToken(i) + " " + markovPitchGen.getProbabilities(i));
		}
		System.out.println("\n------------\n\nRhythms:\n\n-----Transition Table-----\n\n    " + markovRhythmGen.getAlphabet());
		for (int i = 0; i < markovRhythmGen.getAlphabetSize(); i++) {
	        System.out.println(markovRhythmGen.getToken(i) + " " + markovRhythmGen.getProbabilities(i));
		}
		System.out.println("\n------------\n");		
	}
	
	void P2UnitTest2() {	// Project 2: Unit Test 2
		trainP2();
		
		System.out.println("20 pitches from one melody generated using a Markov Chain from Mary Had a Little Lamb:");
		System.out.println(markovPitchGen.generate(20, initPitchGen.generate(initPitchGen.getProbabilities())));
		System.out.println("\n20 rhythms from one melody generated using a Markov Chain from Mary Had a Little Lamb:");
		System.out.println(markovRhythmGen.generate(20, initRhythmGen.generate(initRhythmGen.getProbabilities())) + "\n------------\n");
	}
	
	void P2UnitTest3() {	// Project 2: Unit Test 3
		// UNIT TEST 3
		trainP2();

		MarkovGenerator<Integer> ttDistPitchGen = new MarkovGenerator<Integer>();
		MarkovGenerator<Double> ttDistRhythmGen = new MarkovGenerator<Double>();
		
		ArrayList<Integer> newSongPitches = new ArrayList<Integer>();
		ArrayList<Double> newSongRhythms = new ArrayList<Double>();	
		
		for (int i = 0; i < 9999; i++) {
			newSongPitches = markovPitchGen.generate(20, initPitchGen.generate(initPitchGen.getProbabilities()));
			newSongRhythms = markovRhythmGen.generate(20, initRhythmGen.generate(initRhythmGen.getProbabilities()));
			ttDistPitchGen.train(newSongPitches);
			ttDistRhythmGen.train(newSongRhythms);
		}
		
		System.out.println("\nProbability of Generated Pitches after 10,000 iterations of 20 note melodies:\n\n-----Transition Table-----\n\n   " + ttDistPitchGen.getAlphabet());
		for (int i = 0; i < ttDistPitchGen.getAlphabetSize(); i++) {
	        System.out.println(ttDistPitchGen.getToken(i) + " " + ttDistPitchGen.getProbabilities(i));
		}
		System.out.println("\n------------\n\nProbability of Generated Rhythms after 10,000 iterations of 20 note melodies:\n\n-----Transition Table-----\n\n    " + ttDistRhythmGen.getAlphabet());
		for (int i = 0; i < ttDistRhythmGen.getAlphabetSize(); i++) {
	        System.out.println(ttDistRhythmGen.getToken(i) + " " + ttDistRhythmGen.getProbabilities(i));
		}
		System.out.println("\n------------\n");	
}
	
	void trainP1() {
		pitchGen.train(midiNotes.getPitchArray());
		rhythmGen.train(midiNotes.getRhythmArray());
	}
	
	void trainP2() {
		markovPitchGen.train(midiNotes.getPitchArray());
		markovRhythmGen.train(midiNotes.getRhythmArray());
		
		initPitchGen.train(midiNotes.getPitchArray()); // must train to get initial pitch
		initRhythmGen.train(midiNotes.getRhythmArray()); // must train to get initial rhythm
	}
	
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

}

