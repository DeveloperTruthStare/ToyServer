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
	}
}
