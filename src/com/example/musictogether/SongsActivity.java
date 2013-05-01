package com.example.musictogether;

import com.example.musictogether.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SongsActivity extends Activity {

	private static final String TAG = "SongsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.songs);
		Log.i(TAG, "onCreate");
	}
}
