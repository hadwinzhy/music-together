package com.example.musictogether.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.example.musictogether.MainActivity;
import com.example.musictogether.Utils;
import com.example.musictogether.client.MyClient;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

public class MyServer implements Runnable {

	class MySocket {
		private Socket socket;
		private long networkDelay;

		public MySocket(Socket socket) {
			this.socket = socket;
		}

		public void setNetworkDelay(long networkDelay) {
			this.networkDelay = networkDelay;
		}

		public long getNetworkDelay() {
			return this.networkDelay;
		}

		public Socket getSocket() {
			return this.socket;
		}

		public String getAddress() {
			return this.socket.getRemoteSocketAddress().toString();
		}
	}

	public static final String RESPONSE_TEST_NETWORK = "response test network";

	private LinkedList<MySocket> clientList = new LinkedList<MySocket>();

	private boolean isClientExist(String ipAddr) {
		for (MySocket client : clientList) {
			if (client.getAddress().equals(ipAddr)) {
				return true;
			}
		}
		return false;
	}

	public void calculateNetworkDelay(MySocket mySocket) {
		// send a message to client and wait for response

		// send current time to client
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mySocket.getSocket().getOutputStream())),
					true);
			long sendTime = System.currentTimeMillis();
			Log.v("MusicServer", "sendTime is" + sendTime);
			out.println(MyClient.TEST_NETWORK);

			BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getSocket().getInputStream()));
			in.readLine();
			long arriveTime = System.currentTimeMillis();
			Log.v("MusicServer", "arriveTime is " + arriveTime);
			Log.v("MusicServer", "delay is " + (arriveTime - sendTime) / 2);
			mySocket.setNetworkDelay((arriveTime - sendTime) / 2);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Log.v("MusicServer", "server start  " + Utils.getIPAddress(true));
			ServerSocket serverSocket = new ServerSocket(54321);

			while (true) {
				Socket client = serverSocket.accept();
				String address = client.getRemoteSocketAddress().toString();
				Log.v("MusicServer", "new client" + address);

				if (!isClientExist(address)) {
					MySocket mySocket = new MySocket(client);
					clientList.add(mySocket);
					calculateNetworkDelay(mySocket);
				} else {
					Log.v("MusicServer", "old client");
				}

				// BufferedReader in = new BufferedReader(new
				// InputStreamReader(client.getInputStream()));
				// while(true){
				// String a = in.readLine();
				// Log.v("MusicServer", a + "from client " +
				// client.getLocalAddress());
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendPlayItem() {
		try {
			for (MySocket mySocket : clientList) {
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mySocket.getSocket()
						.getOutputStream())), true);
				out.println(MyClient.PLAY);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
