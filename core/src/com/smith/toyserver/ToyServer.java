package com.smith.toyserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;

public class ToyServer extends Game{
	public SpriteBatch batch;
	public Screen currentScreen;
	public InputProcessor currentProcessor;
	@Override
	public void create () {
		batch = new SpriteBatch();
		// Initialize steamworks api
		try {
			SteamAPI.loadLibraries();
			if (!SteamAPI.init()) {
				System.out.println("Error initializing SteamAPI");
			}
		} catch (SteamException e) {
			System.out.println("Error Starting Steam");
		}

		// Initialize steamworks-server api
		try {
			SteamGameServerAPI.loadLibraries();
			if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27016, (short) 27017,
					SteamGameServerAPI.ServerMode.NoAuthentication, "0.0.1")) {
				System.out.println("SteamGameServerAPI init error");
			}
		} catch (SteamException e) {
			System.out.println(e.getMessage());
		}
	}

}
