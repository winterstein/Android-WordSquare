package winterwell.wordsquare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnglishDice {
	// Create dice face arrays
	String[] dice = new String[]{
		"TOESSI",
		"ASPFFK",
		"NUIHMQu",
		"OBJOAB",
		"LNHNRZ",
		"AHSPCO",
		"RYVDEL",
		"IOTMUC",
		"LREIXD",
		"TERWHV",
		"TSTIYD",
		"WNGEEH",
		"ERTTYL",
		"OWTOAT",
		"AEANEG",
		"EIUNES"
	};

	
	List<Character> pickLetters(int NUM_DICE) {
		// pick a letter per dice
		List<Character> letters = new ArrayList<Character>(NUM_DICE);
		for(int i=0; i<NUM_DICE; i++) {
			int r = (int) Math.floor(Math.random() * 6);
			// we only have 16 dice - random choice for 5th row of 5x5 boards
			int di = i<dice.length? i : (int) Math.floor(Math.random() * dice.length);
			letters.add(dice[di].charAt(r));
		}
		Collections.shuffle(letters);
		return letters;
	}

}
