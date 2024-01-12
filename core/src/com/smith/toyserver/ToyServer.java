package com.smith.toyserver;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;

import java.net.*;
import java.io.*;


public class ToyServer extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	boolean isServer = false;
	public void clientStart() throws IOException {
		SocketClient client = new SocketClient();
		client.startConnection("127.0.0.1", 6666);
		String response = client.sendMessage("hello server");
		System.out.println(response);
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		if (isServer) {
			SocketServer server=new SocketServer();
			try {
				server.start(6666);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				clientStart();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			SteamAPI.loadLibraries();
			//SteamAPI.restartAppIfNecessary(212680);
			if (!SteamAPI.init()) {
				System.out.println("Steam client not running");
			} else {
				System.out.println("Started Successfully");
			}
			SteamAPI.loadLibraries("./libs");
			if (SteamAPI.isSteamRunning()) {
				System.out.println("Running callbacks");
				SteamAPI.runCallbacks();
			}
		} catch (SteamException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
