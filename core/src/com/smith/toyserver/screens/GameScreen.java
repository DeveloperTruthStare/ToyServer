package com.smith.toyserver.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.smith.toyserver.GameState;
import com.smith.toyserver.ToyServer;
import com.smith.toyserver.Vector2;

public class GameScreen implements Screen, InputProcessor {
    private GameState gameState;
    public GameScreen() {
        gameState = new GameState();
    }
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameState.player2.draw();
        gameState.player1.draw();
        gameState.ball.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.W) {
            //gameManager.setPlayerVelocity(new Vector2(0, 10));
        } else if (keycode == Input.Keys.S) {
            //gameManager.setPlayerVelocity(new Vector2(0, -10));
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.W) {
            //gameManager.setPlayerVelocity(new Vector2(0, 0));
        } else if (keycode == Input.Keys.S) {
            //gameManager.setPlayerVelocity(new Vector2(0, 0));
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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
