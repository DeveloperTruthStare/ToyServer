package com.smith.toyserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;

import Screens.PlayScreen;

public class ToyServer extends Game {
	public SpriteBatch batch;
	MyInputProcessor inputProcessor;
	Player self;
	BitmapFont font;
	SteamLobbyView lobbyViewController;

	public SteamLobby lobby;
	ShapeRenderer shapeRenderer;
	@Override
	public void create () {
		batch = new SpriteBatch();


		inputProcessor = new MyInputProcessor();
		//Gdx.input.setInputProcessor(inputProcessor);
		try {
			SteamAPI.loadLibraries();
			if (!SteamAPI.init()) {
				System.out.println("Error initializing SteamAPI");
			}
		} catch (SteamException e) {
			System.out.println("Error Starting Steam");
		}
		try {
			SteamGameServerAPI.loadLibraries();
			if (!SteamGameServerAPI.init((127 << 24) + 1, (short) 27016, (short) 27017,
					SteamGameServerAPI.ServerMode.NoAuthentication, "0.0.1")) {
				System.out.println("SteamGameServerAPI init error");
			}
		} catch (SteamException e) {
			System.out.println(e.getMessage());
		}

		font = new BitmapFont();
		lobby = new SteamLobby();
		lobbyViewController = new SteamLobbyView(lobby);
		shapeRenderer = new ShapeRenderer();
		setScreen(new PlayScreen(this));

	}
	public void startServer() {
		this.lobby.createLobby();
	}
	@Override
	public void render () {
		super.render();


		if (SteamAPI.isSteamRunning()) {
			SteamAPI.runCallbacks();
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

}
