package winterwell.wordsquare;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "WORDSQUARE";
	private WebView webview;
//	private TextView countdown;
//	private ProgressBar timerBar;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        webview = (WebView) findViewById(R.id.webView);
        //countdown = (TextView) findViewById(R.id.countdown);
//        timerBar = (ProgressBar) findViewById(R.id.timerBar);
//        setContentView(R.layout.main);
        // Start a new game
        doNewGame();
    }

    EnglishDice dice = new EnglishDice();
//	Thread gameTimerThread;
    
	private void doNewGame() {
		int wh = 4;
		List<Character> letters = dice.pickLetters(4*4);
		// TODO load from a file
		String html = "<html><style>table.board {border: solid black 1px;}"
				+"table.board td {width:50px;height:50px;font-size: 24pt;border: solid black 1px;text-align:center;}"
			    +"</style><table class='board'>";
		for(int i=0; i<wh; i++) {
			html += "<tr>";
			for(int j=0; j<wh; j++) {
				html += "<td>"+letters.get(i*wh + j)+"</td>";
			}
			html += "</tr>";
		}
		html += "</table></html>";
		webview.loadData(html, "text/html", "utf-8");
		
		if (true) return;
//		if (gameTimerThread != null) {
//			gameTimerThread.stop();
//		}
//		Runnable timer = new Runnable() {
//            public void run() {
//            	int progress = 0;
//            	int target = 2*60*1000;
//            	long start = System.currentTimeMillis();
//                while (progress < 100) {
//                	progress = (int) ((System.currentTimeMillis() - start) / target);
//                	final int fp = progress;
//                    // Update the progress bar
//                    mHandler.post(new Runnable() {
//                        public void run() {
//                        	countdown.setText(fp);
////							timerBar.setProgress(fp);
//                        }
//                    });
//                    try {
//						Thread.currentThread().sleep(100);
//					} catch (InterruptedException e) {
//					}
//                }
//            }
//        };
//		gameTimerThread = new Thread(timer);
//		gameTimerThread.start();
	}
	

//    private Handler mHandler = new Handler();

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d(TAG, "Selected: "+item.getTitle()+" "+item.getItemId());
    	int id = item.getItemId();
    	if (R.id.newGame == id) {
    		doNewGame();
    	}
    	return super.onOptionsItemSelected(item);
    }

}