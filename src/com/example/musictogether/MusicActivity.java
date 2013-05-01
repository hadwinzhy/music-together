package com.example.musictogether;

import java.util.List;

import com.example.musictogether.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicActivity extends Activity {

	private TextView textName;
	private TextView textSinger;
	private TextView textStartTime;
	private TextView textEndTime;
	private ImageButton imageBtnLast;
	private ImageButton imageBtnRewind;
	public static ImageButton imageBtnPlay;
	private ImageButton imageBtnForward;
	private ImageButton imageBtnNext;
	private ImageButton imageBtnLoop;
	private ImageButton imageBtnRandom;
	public static LrcView lrc_view;
	private ImageView icon;
	private SeekBar seekBar1;
	private AudioManager audioManager;// 音量管理者
	private int maxVolume;// 最大音量
	private int currentVolume;// 当前音量
	private SeekBar seekBarVolume;
	private List<Music> lists;
	private Boolean isPlaying = false;
	private static int id = 1;
	private static int currentId = 2;
	private static Boolean replaying = false;
	private MyProgressBroadCastReceiver receiver;
	private MyCompletionListner completionListner;
	public static Boolean isLoop = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.music);

		textName = (TextView) this.findViewById(R.id.music_name);
		textSinger = (TextView) this.findViewById(R.id.music_singer);
		textStartTime = (TextView) this.findViewById(R.id.music_start_time);
		textEndTime = (TextView) this.findViewById(R.id.music_end_time);
		seekBar1 = (SeekBar) this.findViewById(R.id.music_seekBar);
		// icon = (ImageView) this.findViewById(R.id.image_icon);
		imageBtnLast = (ImageButton) this.findViewById(R.id.music_lasted);
		imageBtnRewind = (ImageButton) this.findViewById(R.id.music_rewind);
		imageBtnPlay = (ImageButton) this.findViewById(R.id.music_play);
		imageBtnForward = (ImageButton) this.findViewById(R.id.music_foward);
		imageBtnNext = (ImageButton) this.findViewById(R.id.music_next);
		imageBtnLoop = (ImageButton) this.findViewById(R.id.music_loop);
		seekBarVolume = (SeekBar) this.findViewById(R.id.music_volume);
		imageBtnRandom = (ImageButton) this.findViewById(R.id.music_random);
		lrc_view = (LrcView) findViewById(R.id.LyricShow);

		imageBtnLast.setOnClickListener(new MyListener());
		imageBtnRewind.setOnClickListener(new MyListener());
		imageBtnPlay.setOnClickListener(new MyListener());
		imageBtnForward.setOnClickListener(new MyListener());
		imageBtnNext.setOnClickListener(new MyListener());
		imageBtnLoop.setOnClickListener(new MyListener());
		imageBtnRandom.setOnClickListener(new MyListener());

		lists = MusicList.getMusicData(this);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获得最大音量
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 获得当前音量
		seekBarVolume.setMax(maxVolume);
		seekBarVolume.setProgress(currentVolume);
		seekBarVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						progress, AudioManager.FLAG_ALLOW_RINGER_MODES);
			}
		});
		// 电话状态监听
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new MobliePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
		seekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBar1.setProgress(seekBar.getProgress());
				Intent intent = new Intent("net.musictogether.seekBar");
				intent.putExtra("seekBarPosition", seekBar.getProgress());
				// System.out.println("==========="+seekBar.getProgress());
				sendBroadcast(intent);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		completionListner = new MyCompletionListner();
		IntentFilter filter = new IntentFilter("net.musictogether.completion");
		this.registerReceiver(completionListner, filter);
	}

	private class MobliePhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: /* 无任何状态时 */
				Intent intent = new Intent(MusicActivity.this,
						MusicService.class);
				intent.putExtra("play", "playing");
				intent.putExtra("id", id);
				startService(intent);
				isPlaying = true;
				imageBtnPlay.setImageResource(R.drawable.pause1);
				replaying = true;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: /* 接起电话时 */

			case TelephonyManager.CALL_STATE_RINGING: /* 电话进来时 */
				Intent intent2 = new Intent(MusicActivity.this,
						MusicService.class);
				intent2.putExtra("play", "pause");
				startService(intent2);
				isPlaying = false;
				imageBtnPlay.setImageResource(R.drawable.play1);
				replaying = false;
				break;
			default:
				break;

			}

		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		receiver = new MyProgressBroadCastReceiver();
		IntentFilter filter = new IntentFilter(".progress");
		this.registerReceiver(receiver, filter);

		id = getIntent().getIntExtra("id", 1);
		if (id == currentId) {
			Music m = lists.get(id);
			textName.setText(m.getTitle());
			textSinger.setText(m.getSinger());
			textEndTime.setText(toTime((int) m.getTime()));
			Intent intent = new Intent(MusicActivity.this, MusicService.class);
			intent.putExtra("play", "replaying");
			intent.putExtra("id", id);
			startService(intent);
			if (replaying == true) {
				imageBtnPlay.setImageResource(R.drawable.pause1);
				// /replaying=false;
				isPlaying = true;
			} else {
				imageBtnPlay.setImageResource(R.drawable.play1);
				// replaying=true;
				isPlaying = false;
			}

		} else {
			Music m = lists.get(id);
			textName.setText(m.getTitle());
			textSinger.setText(m.getSinger());
			textEndTime.setText(toTime((int) m.getTime()));
			imageBtnPlay.setImageResource(R.drawable.pause1);
			Intent intent = new Intent(MusicActivity.this, MusicService.class);
			intent.putExtra("play", "play");
			intent.putExtra("id", id);
			startService(intent);
			isPlaying = true;
			replaying = true;
			currentId = id;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(receiver);
		this.unregisterReceiver(completionListner);
		super.onDestroy();
	}

	public class MyProgressBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int position = intent.getIntExtra("position", 0);
			int total = intent.getIntExtra("total", 0);
			int progress = position * 100 / total;
			textStartTime.setText(toTime(position));
			seekBar1.setProgress(progress);
			seekBar1.invalidate();
		}

	}

	private class MyListener implements OnClickListener {

		private static final String TAG = "KICOOL";

		@Override
		public void onClick(View v) {
			if (v == imageBtnLast) {
				Log.i(TAG, "imageBtnLast");
				
				// 第一首
				id = 0;
				Music m = lists.get(0);
				textName.setText(m.getTitle());
				textSinger.setText(m.getSinger());
				textEndTime.setText(toTime((int) m.getTime()));
				imageBtnPlay.setImageResource(R.drawable.pause1);
				Intent intent = new Intent(MusicActivity.this,
						MusicService.class);
				intent.putExtra("play", "first");
				intent.putExtra("id", id);
				startService(intent);
				isPlaying = true;
			} else if (v == imageBtnRewind) {
				Log.i(TAG, "imageBtnRewind");
				// 前一首
				int id = MusicService._id - 1;
				if (id >= lists.size() - 1) {
					id = lists.size() - 1;
				} else if (id <= 0) {
					id = 0;
				}
				Music m = lists.get(id);
				textName.setText(m.getTitle());
				textSinger.setText(m.getSinger());
				textEndTime.setText(toTime((int) m.getTime()));
				imageBtnPlay.setImageResource(R.drawable.pause1);
				Intent intent = new Intent(MusicActivity.this,
						MusicService.class);
				intent.putExtra("play", "rewind");
				intent.putExtra("id", id);
				startService(intent);
				isPlaying = true;
			} else if (v == imageBtnPlay) {
				Log.i(TAG, "imageBtnPlay");
				// 正在播放
				if (isPlaying == true) {
					Intent intent = new Intent(MusicActivity.this,
							MusicService.class);
					intent.putExtra("play", "pause");
					startService(intent);
					isPlaying = false;
					imageBtnPlay.setImageResource(R.drawable.play1);
					replaying = false;
				} else {
					Intent intent = new Intent(MusicActivity.this,
							MusicService.class);
					intent.putExtra("play", "playing");
					intent.putExtra("id", id);
					startService(intent);
					isPlaying = true;
					imageBtnPlay.setImageResource(R.drawable.pause1);
					replaying = true;
				}
			} else if (v == imageBtnForward) {
				Log.i(TAG, "imageBtnForward");
				// 下一首
				int id = MusicService._id + 1;
				if (id >= lists.size() - 1) {
					id = lists.size() - 1;
				} else if (id <= 0) {
					id = 0;
				}
				Music m = lists.get(id);
				textName.setText(m.getTitle());
				textSinger.setText(m.getSinger());
				textEndTime.setText(toTime((int) m.getTime()));
				imageBtnPlay.setImageResource(R.drawable.pause1);
				Intent intent = new Intent(MusicActivity.this,
						MusicService.class);
				intent.putExtra("play", "forward");
				intent.putExtra("id", id);
				startService(intent);
				isPlaying = true;
			} else if (v == imageBtnNext) {
				Log.i(TAG, "imageBtnNext");
				// 最后一首
				int id = lists.size() - 1;
				Music m = lists.get(id);
				textName.setText(m.getTitle());
				textSinger.setText(m.getSinger());
				textEndTime.setText(toTime((int) m.getTime()));
				imageBtnPlay.setImageResource(R.drawable.pause1);
				Intent intent = new Intent(MusicActivity.this,
						MusicService.class);
				intent.putExtra("play", "last");
				intent.putExtra("id", id);
				startService(intent);
				isPlaying = true;
			} else if (v == imageBtnLoop) {
				Log.i(TAG, "imageBtnLoop");
				if (isLoop == true) {
					// 顺序播放
					imageBtnLoop
							.setBackgroundResource(R.drawable.play_loop_spec);
					isLoop = false;
				} else {
					// 单曲播放
					imageBtnLoop
							.setBackgroundResource(R.drawable.play_loop_sel);
					isLoop = true;
				}
			} else if (v == imageBtnRandom) {
				Log.i(TAG, "imageBtnRandom");
				imageBtnRandom.setImageResource(R.drawable.play_random_sel);
			}

		}
	}

	private class MyCompletionListner extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Music m = lists.get(MusicService._id);
			textName.setText(m.getTitle());
			textSinger.setText(m.getSinger());
			textEndTime.setText(toTime((int) m.getTime()));
			imageBtnPlay.setImageResource(R.drawable.pause1);
		}

	}

	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
}
