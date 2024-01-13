package com.smith.toyserver;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;

public class ToyServer extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	SteamServer server;
	SteamClient client;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		try {
			SteamAPI.loadLibraries();
			if (!SteamAPI.init()) {
				System.out.println("Error initializing SteamAPI");
			}
		} catch (SteamException e) {
			System.out.println("Error Starting Steam");
		}

		server = new SteamServer();

	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		if (SteamAPI.isSteamRunning()) {
			SteamAPI.runCallbacks();
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

}
