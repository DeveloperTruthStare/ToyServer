package com.smith.toyserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamGameServerAPI;

import Screens.PlayScreen;
import Screens.TitleScreen;

public class ToyServer extends Game implements InputProcessor{
	public SpriteBatch batch;
	public SteamLobby lobby;
	public Screen currentScreen;
	public InputProcessor currentProcessor;
	@Override
	public void create () {
		batch = new SpriteBatch();
		Gdx.input.setInputProcessor(this);
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

		// Create the Lobby Manager
		lobby = new SteamLobby(this);

		// Set the initial screen
		swapScreen(new TitleScreen(this));
	}
	public void swapScreen(Screen screen) {
		this.setScreen(screen);
		this.currentScreen = screen;
	}
	public void setCurrentProcessor(InputProcessor ip) {
		System.out.println("Set Current Processor");
		this.currentProcessor = ip;
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

	@Override
	public boolean keyDown(int keycode) {
		this.currentProcessor.keyDown(keycode);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		this.currentProcessor.keyUp(keycode);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		this.currentProcessor.touchDown(screenX, screenY, pointer, button);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
