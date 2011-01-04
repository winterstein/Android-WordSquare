package winterwell.wordsquare;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.webkit.WebView;

public class JSBridge {

	public void timeUp() {
		if (! mainActivity.settings.sounds) {
			Log.d(MainActivity.TAG, "No sound = no alarm clock");
			return;
		}
		if (mainActivity.mediaPlayer == null) return;
		mainActivity.mediaPlayer.start();
	}

	public JSBridge(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	MainActivity mainActivity;
	
	public void tick() {
		if (true ||  ! mainActivity.settings.sounds) {
			Log.d(MainActivity.TAG, "No sound = no tick");
			return;
		}
		mainActivity.webview.post(new Runnable() {			
			@Override
			public void run() {
				Log.d(MainActivity.TAG, "tick...");
				mainActivity.webview.playSoundEffect(SoundEffectConstants.CLICK);		
			}
		});		
	}
}
