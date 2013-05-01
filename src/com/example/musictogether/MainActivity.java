package com.example.musictogether;

import com.example.musictogether.client.MyClient;
import com.example.musictogether.server.MyServer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	public static final String TAG = "MusicTogether";
	
	private Button serverBt, clientBt, playBt;

	private MyServer s; 
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		serverBt = (Button) this.findViewById(R.id.server);
		clientBt = (Button) this.findViewById(R.id.client);
		playBt = (Button) this.findViewById(R.id.play);

		playBt.setOnClickListener(this);
		serverBt.setOnClickListener(this);
		clientBt.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.server:
			s = new MyServer();
			new Thread(s).start();
			break;
		case R.id.client:
			new Thread(new  MyClient()).start();
			break;
		case R.id.play:
			if(s != null)
				s.sendPlayItem();
		}
	}

}
