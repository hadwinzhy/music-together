package com.example.musictogether;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.musictogether.LrcProcess.LrcContent;

import com.example.musictogether.R;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.AnimationUtils;

public class MusicService extends Service implements Runnable {
	private static final String TAG = "KICOOL";
	private MediaPlayer player;
	private List<Music> lists;
	public static int _id = 1; // 当前播放位置
	public static Boolean isRun = true;
	public LrcProcess mLrcProcess;
	public LrcView mLrcView;
	public static int playing_id = 0;
	public static Boolean playing = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		lists = MusicList.getMusicData(getApplicationContext());

		SeekBarBroadcastReceiver receiver = new SeekBarBroadcastReceiver();
		IntentFilter filter = new IntentFilter("net.musictogether.seekBar");
		this.registerReceiver(receiver, filter);
		new Thread(this).start();
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		String play = intent.getStringExtra("play");
		_id = intent.getIntExtra("id", 1);
		
		Log.i(TAG, play);
		
		if (play.equals("play")) {
			if (null != player) {
				player.release();
				player = null;
			}
			playSyncMusic(_id);

		} else if (play.equals("pause")) {
			if (null != player) {
				player.pause();
			}
		} else if (play.equals("playing")) {
			if (player != null) {
				player.start();
			} else {
				playSyncMusic(_id);
			}
		} else if (play.equals("replaying")) {

		} else if (play.equals("first")) {
			int id = intent.getIntExtra("id", 0);
			playSyncMusic(id);
		} else if (play.equals("rewind")) {
			int id = intent.getIntExtra("id", 0);
			playSyncMusic(id);
		} else if (play.equals("forward")) {
			int id = intent.getIntExtra("id", 0);
			playSyncMusic(id);
		} else if (play.equals("last")) {
			int id = intent.getIntExtra("id", 0);
			playSyncMusic(id);
		} else if (play.equals("syncplay")) {
			int id = intent.getIntExtra("id", 0);
			int offset = intent.getIntExtra("offset", 0);
			playMusic(id, offset);
		}
	}
	
	private void playSyncMusic(int id) {
		playMusic(id, 0);
	}
	
	private void playMusic(int id, int offset) {

		if (null != player) {
			player.release();
			player = null;
		}
		if (id >= lists.size() - 1) {
			_id = lists.size() - 1;
		} else if (id <= 0) {
			_id = 0;
		}
		Music m = lists.get(_id);
		String url = m.getUrl();
		Uri myUri = Uri.parse(url);
		player = new MediaPlayer();
		player.reset();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			player.setDataSource(getApplicationContext(), myUri);
			player.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (offset != 0) {
			Log.i("KICOOL", "Offset:" + offset);
			mHandler.postDelayed(mRunnable, offset);
		}
		else {
			player.start();
		}
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// 下一首
				if (MusicActivity.isLoop == true) {
					player.reset();
					Intent intent = new Intent("net.musictogether.completion");
					sendBroadcast(intent);
					_id = _id + 1;
					playSyncMusic(_id);
				} else { // 单曲播放
					player.reset();
					Intent intent = new Intent("net.musictogether.completion");
					sendBroadcast(intent);
					playSyncMusic(_id);
				}
			}
		});
		player.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				if (null != player) {
					player.release();
					player = null;
				}
				Music m = lists.get(_id);
				String url = m.getUrl();
				Uri myUri = Uri.parse(url);
				player = new MediaPlayer();
				player.reset();
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					player.setDataSource(getApplicationContext(), myUri);
					player.prepare();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				player.start();
				return false;
			}
		});

	}

	private class SeekBarBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int seekBarPosition = intent.getIntExtra("seekBarPosition", 0);
			// System.out.println("--------"+seekBarPosition);
			player.seekTo(seekBarPosition * player.getDuration() / 100);
			player.start();
		}

	}

	@Override
	public void run() {
		while (isRun) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (null != player) {
				int position = player.getCurrentPosition();
				int total = player.getDuration();
				Intent intent = new Intent("net.musictogether.progress");
				intent.putExtra("position", position);
				intent.putExtra("total", total);
				sendBroadcast(intent);
			}
			if (null != player) {
				if (player.isPlaying()) {
					playing = true;
				} else {
					playing = false;
				}
			}
		}
	}

	Handler mHandler = new Handler();

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			player.start();
		}
	};
	
	// 初始化歌词检索值
	private int index = 0;
	// 初始化歌曲播放时间的变量
	private int CurrentTime = 0;
	// 初始化歌曲总时间的变量
	private int CountTime = 0;

}
