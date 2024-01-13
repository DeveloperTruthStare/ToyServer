package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.smith.toyserver.Button;
import com.smith.toyserver.IButtonCallback;
import com.smith.toyserver.NetworkMessageProcessor;
import com.smith.toyserver.ToyServer;

import java.sql.Array;
import java.util.ArrayList;

public class PlayScreen implements Screen, InputProcessor, IButtonCallback, NetworkMessageProcessor {
    private final ToyServer game;
    private final ArrayList<Color> colors;
    private int currentBackground = 0;
    public Button startServerButton;
    public Button disconnectButton;

    public PlayScreen(ToyServer game) {
        this.game = game;
        this.game.lobby.msgProcessor = this;
        Gdx.input.setInputProcessor(this);
        colors = new ArrayList<>();
        colors.add(new Color(0, 0, 0, 0));
        colors.add(new Color(1, 0, 0, 0));
        colors.add(new Color(0, 1, 0, 0));
        colors.add(new Color(0, 0, 1, 0));
        colors.add(new Color(1, 1, 1, 0));


        startServerButton = new Button(100, 100, 100, 100, "Start Server", 0, this);
        disconnectButton = new Button(1800, 100, 100, 100, "Disconnect", 1, this);
        disconnectButton.active = false;
    }
    public void syncBackground() {
        game.lobby.sendNetworkMessage(String.valueOf(currentBackground));
    }
    @Override
    public void processNetworkMessage(String msg) {
        this.currentBackground = Integer.parseInt(msg.substring(0, 1));
    }
    @Override
    public void onClick(int buttonId) {
        switch (buttonId) {
            case 0:
                // Start Server Button

                game.startServer();
                startServerButton.active = false;
                disconnectButton.active = true;
                break;
            case 1:
                // Disconnect Button
                disconnectButton.active = false;
                startServerButton.active = true;
                this.game.lobby.disconnect();
                break;
        }
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Color backgroundColor = colors.get(currentBackground);
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        startServerButton.render(game.batch);
        disconnectButton.render(game.batch);

        game.batch.end();
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

    // Input Processor
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.D) {
            currentBackground++;
            if (currentBackground > colors.size() - 1) {
                currentBackground = 0;
            }
            syncBackground();
        } else if (keycode == Input.Keys.A) {
            currentBackground--;
            if (currentBackground < 0) {
                currentBackground = colors.size() - 1;
            }
            syncBackground();
        }
        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.startServerButton.onMouseButtonClick(screenX, 1080 - screenY);
        this.disconnectButton.onMouseButtonClick(screenX, 1080 - screenY);
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
