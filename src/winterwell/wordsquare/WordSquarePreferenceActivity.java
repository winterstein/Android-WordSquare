package winterwell.wordsquare;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class WordSquarePreferenceActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		PreferenceScreen ps = new PreferenceScreen(); can't do
//		setPreferenceScreen(preferenceScreen);
		addPreferencesFromResource(R.xml.prefs);
	}
	
}
