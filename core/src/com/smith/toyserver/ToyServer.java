package com.smith.toyserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smith.toyserver.networking.NetworkGameController;
import com.smith.toyserver.screens.GameScreen;
import com.smith.toyserver.screens.TitleScreen;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ToyServer extends Game {

	public GameManager manager;

	private InputProcessor currentScreen;
	public InputProcessor inputProcessor = new InputProcessor()  {

		@Override
		public boolean keyDown(int keycode) {
			if (keycode == Input.Keys.S)
				setPlayerVelocity(new Vector2(0, -10));
			else if (keycode == Input.Keys.W)
				setPlayerVelocity(new Vector2(0, 10));
			if (currentScreen != null)
				currentScreen.keyDown(keycode);
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Input.Keys.S || keycode == Input.Keys.W)
				setPlayerVelocity(new Vector2(0, 0));
			if (currentScreen != null)
				currentScreen.keyUp(keycode);
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			if (currentScreen != null)
				currentScreen.keyTyped(character);
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			if (currentScreen != null)
				currentScreen.touchDown(screenX, screenY, pointer, button);
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			if (currentScreen != null)
				currentScreen.touchUp(screenX, screenY, pointer, button);
			return false;
		}

		@Override
		public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
			if (currentScreen != null)
				currentScreen.touchCancelled(screenX, screenY, pointer, button);
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			if (currentScreen != null)
				currentScreen.touchDragged(screenX, screenY, pointer);
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			if (currentScreen != null)
				currentScreen.mouseMoved(screenX, screenY);
			return false;
		}

		@Override
		public boolean scrolled(float amountX, float amountY) {
			if (currentScreen != null)
				currentScreen.scrolled(amountX, amountY);
			return false;
		}
	};

	public SpriteBatch batch;
	private TitleScreen titleScreen;
	private GameScreen gameScreen;

	public NetworkGameController networkManager;
	@Override
	public void create () {
		this.manager = new GameManager(Thread.currentThread());
		networkManager = new NetworkGameController(this, manager);
		Thread managerThread = new Thread(this.manager);
		managerThread.start();

		batch = new SpriteBatch();
		titleScreen = new TitleScreen(this);
		setScreen(this.titleScreen);
		this.currentScreen = titleScreen;
		Gdx.input.setInputProcessor(inputProcessor);
	}
	@Override
	public void render() {
		super.render();
	}

	// Gets called from UI
	public void startServer() {
		networkManager.startServer();
		System.out.println("Starting Server");
		openGameScreen();
	}

	// Gets called from Steam callbacks
	public void startClient() {
		System.out.println("Starting Client");
		openGameScreen();
	}
	private void openGameScreen() {
		gameScreen = new GameScreen();
		gameScreen.setGameState(manager.getGameState());
		setScreen(gameScreen);
		this.currentScreen = gameScreen;
	}

	public void setPlayerVelocity(Vector2 velocity) {
		networkManager.setPlayerVelocity(velocity);
		gameScreen.setGameState(networkManager.getGameState());
	}
}