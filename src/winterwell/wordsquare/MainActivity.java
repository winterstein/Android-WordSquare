package winterwell.wordsquare;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import winterwell.utils.io.FileUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    static final String TAG = "WORDSQUARE";
	WebView webview;
	private String baseHtml;
	MediaPlayer mediaPlayer;
	private JSBridge jsBridge;
	GameSettings settings;
//	private TextView countdown;
//	private ProgressBar timerBar;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);             
        
        webview = new WebView(this);
        webview.setBackgroundColor(Color.BLACK);
        webview.setKeepScreenOn(true);
        jsBridge = new JSBridge(this);
        webview.addJavascriptInterface(jsBridge, "jsBridge");
        webview.setSoundEffectsEnabled(true); // for the tick
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
//      TODO sound support webview.addJavascriptInterface(obj, interfaceName);
        setContentView(webview);
		try {
	//		AssetManager am = new AssetManager();
			ContentResolver cr = getContentResolver();		
			InputStream in = cr.openInputStream(
					Uri.parse("android.resource://winterwell.wordsquare/"+R.raw.page));			
			baseHtml = FileUtils.read(in);			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
//		SoundEffectConstants.CLICK;		

        // Start a new game        
        doNewGame();
    }

	private void doNewGame() {
		Log.d(TAG, "doNewGame...");
		// fresh settings per game
		SharedPreferences ps = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, ?> prefs = ps.getAll();
        settings = new GameSettings(prefs);
	    
		int wh = settings.boardSize;
		List<Character> letters = settings.dice.pickLetters(wh*wh);
		// load from a file
		String html = baseHtml;
		for(int i=0; i<wh; i++) {			
			for(int j=0; j<wh; j++) {
				html = html.replace("$"+i+j, ""+letters.get(i*wh + j));
			}
		}
		// push through settings into html
		html = html.replaceFirst("gameTime ?= ?[0-9\\*]+;", "gameTime = "+settings.minutes+"*60*1000;");
		html = html.replaceFirst("rotateLetters ?= ?(true|false);", "rotateLetters = "+settings.rotateLetters+";");
//		html = URLEncoder.encode(html);
		
		webview.loadDataWithBaseURL("fake://not/needed", html, "text/html", "utf-8", "");
		
//		(html, "text/html", "utf-8");
//		webview.reload();
		Log.d(TAG, "...a new game begins!");
		
		// sound
		try {
			if (mediaPlayer != null) {
				mediaPlayer.release();
			}
			// TODO
//			mediaPlayer = MediaPlayer.create(this, R.raw.alarm_clock_1);
//			mediaPlayer.prepare();
//			mediaPlayer.setVolume(100, 100);						
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			mediaPlayer = null;
		}
	}
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if (mediaPlayer != null) {
			mediaPlayer.release();
		}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d(TAG, "Selected: "+item.getTitle()+" "+item.getItemId());
    	int id = item.getItemId();
    	if (R.id.newGame == id) {
    		doNewGame();
    		return true;
    	}
    	if (R.id.settings == id) {
    		startActivity(new Intent(this, WordSquarePreferenceActivity.class));
    		return true;
    	}    	
    	return super.onOptionsItemSelected(item);
    }

}
