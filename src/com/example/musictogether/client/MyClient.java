package com.example.musictogether.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.musictogether.MainActivity;
import com.example.musictogether.MusicActivity;
import com.example.musictogether.MusicService;
import com.example.musictogether.server.MyServer;

public class MyClient implements Runnable {

	public static final String TEST_NETWORK = "test network";
	public static final String PLAY = "play";

	private Context context;

	public MyClient(Context context) {
		this.context = context;
	}

	public void startSyncTime() {

	}

	@Override
	public void run() {
		try {
			Socket socket = new Socket("192.168.1.185", 54321);
			if (!socket.isConnected()) {
				socket.close();
				Log.v("MusicClient", "is not connected, close");
				return;
			}

			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (true) {
				String str = in.readLine();
				if (str.equals(TEST_NETWORK)) {
					out.println(MyServer.RESPONSE_TEST_NETWORK);
				} else if (str.equals(PLAY)) {
					Log.v("MusicClient", "play");

					Intent intent = new Intent(context, MusicService.class);
					intent.putExtra("play", "syncplay");
					intent.putExtra("offset", 1);
					context.startService(intent);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
