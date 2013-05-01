package com.example.musictogether;

import java.util.List;

import com.example.musictogether.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ListActivity extends Activity {

	private ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listmusic);
		
		listView= (ListView) this.findViewById(R.id.listAllMusic);
		List<Music> listMusic=MusicList.getMusicData(getApplicationContext());
		MusicAdapter adapter=new MusicAdapter(this, listMusic);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.i("KICOOL", "onItemClick");
				
				Intent intent = new Intent(ListActivity.this,
						MusicActivity.class);
				intent.putExtra("id", arg2);
				startActivity(intent);
			}
		});
	}
}
