package winterwell.wordsquare;

import java.util.Map;

import android.util.Log;
import android.view.SoundEffectConstants;

public class GameSettings {

	int boardSize = 4;

	boolean sounds = true;

	public GameSettings(Map<String, ?> prefs) {
        Object rl = prefs.get("rotate_letters");
        Object bs = prefs.get("board_size_pref");        
        Object gl = prefs.get("game_length_pref");
        Object s = prefs.get("sound_pref");
        Log.d(MainActivity.TAG, "rotateLetters: "+rl);
        Log.d(MainActivity.TAG, "bs: "+bs);
        Log.d(MainActivity.TAG, "gl: "+gl);
        if (rl!=null) rotateLetters = (Boolean) rl;
        if (bs != null) boardSize = Integer.valueOf(bs.toString());
        if (gl!=null) {
        	minutes = Integer.valueOf(gl.toString());
        }
        if (s!=null) {
        	sounds = (Boolean) s;
        }
	}

	EnglishDice dice = new EnglishDice();
    
	int minutes = 3;

	boolean rotateLetters = true;

}
