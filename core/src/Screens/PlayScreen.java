package Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.smith.toyserver.GameController;
import com.smith.toyserver.GameState;
import com.smith.toyserver.utils.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smith.toyserver.Button;
import com.smith.toyserver.GameObject;
import com.smith.toyserver.IButtonCallback;
import com.smith.toyserver.NetworkMessageProcessor;
import com.smith.toyserver.ToyServer;

import java.sql.Array;
import java.util.ArrayList;



public class PlayScreen implements Screen, InputProcessor, IButtonCallback, NetworkMessageProcessor {
    private final ToyServer game;
    public BitmapFont font;
    public boolean isPlayer1 = false;
    public GameController gameController;
    public int framesSinceLastNetworkSync = 0;
    public PlayScreen(ToyServer game) {
        this.game = game;
        this.game.setCurrentProcessor(this);
        this.game.lobby.msgProcessor = this;
        this.gameController = new GameController();

        font = new BitmapFont();
        font.getData().setScale(5);
    }

    public void host() {
        this.game.lobby.createLobby();
        this.gameController = new GameController();
        this.isPlayer1 = true;
        this.gameController.player1 = new GameObject(0, 0);
        this.gameController.ball = new GameObject(0, 2);
        this.gameController.player2 = new GameObject(1, 2);

        this.gameController.player1.position = new Vector2(100, 490);
        this.gameController.player1.size = new Vector2(20, 100);

        this.gameController.ball.position = new Vector2((float) 1920 /2 - 5, (float) 1080 /2 - 5);
        this.gameController.ball.size = new Vector2(10, 10);
        this.gameController.ball.velocity = new Vector2(0, 0);

        this.gameController.player2.position = new Vector2(1820, 490);
        this.gameController.player2.size = new Vector2(20, 100);
    }

    public void client() {
        // Send message that we've connected
        this.gameController = new GameController();
        this.gameController.player1 = new GameObject(0, 0);
        this.gameController.ball = new GameObject(0, 2);
        this.gameController.player2 = new GameObject(1, 2);

        this.gameController.player1.position = new Vector2(100, 490);
        this.gameController.player1.size = new Vector2(20, 100);

        this.gameController.ball.position = new Vector2((float) 1920 /2 - 5, (float) 1080 /2 - 5);
        this.gameController.ball.size = new Vector2(10, 10);
        this.gameController.ball.velocity = new Vector2(0, 0);

        this.gameController.player2.position = new Vector2(1820, 490);
        this.gameController.player2.size = new Vector2(20, 100);

        this.game.lobby.sendNetworkMessage("Connected");
        this.gameController.gameState = GameState.WAITING_FOR_HOST;
    }

    @Override
    public void processNetworkMessage(String msg) {
        if (msg.startsWith("SetPlayer1:")) {

        } else if (msg.startsWith("SetPlayer2:")) {

        } else if (msg.startsWith("SetBall:")) {

        } else if (msg.startsWith("SetState:")) {
            msg = msg.substring(9);
            if (msg.startsWith("PLAYING")) {
                this.gameController.gameState = GameState.PLAYING;
            }
        } else if (msg.startsWith("Connected")) {
            this.gameController.gameState = GameState.WAITING_FOR_HOST;
        }
    }
    @Override
    public void processGameState(GameController  state) {
        if (isPlayer1) {
            // Only update values controlled by not us
            this.gameController.player2.set(state.player2);
        } else {
            // Only update host controlled values
            this.gameController.gameState = state.gameState;
            this.gameController.player1.set(state.player1);
            this.gameController.ball.set(state.ball);
        }
    }
    @Override
    public void onClick(int buttonId) {
    }
    @Override
    public void show() {

    }

    public float time = 0;
    public int frames = 0;
    @Override
    public void render(float delta) {
        time += delta;
        frames++;
        if (time >= 1) {
            time = 0;
            System.out.println(frames);
            frames = 0;
        }
        try {
            this.update(delta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        switch (this.gameController.gameState) {
            case WAITING_FOR_PLAYERS:
                font.draw(game.batch, "Waiting for Another Player", (float) 1920 /2 - 300, 720);
                break;
            case WAITING_FOR_HOST:
                if (isPlayer1)
                    font.draw(game.batch, "Press SPACE to begin", (float) 1920 /2 - 200, 720);
                else
                    font.draw(game.batch, "Waiting for Host", (float) 1920 /2 - 200, 720);
                break;
        }

        game.batch.end();

        this.gameController.player1.draw();
        this.gameController.player2.draw();
        this.gameController.ball.draw();

        this.checkCollisions();
    }

    public void update(float dt) throws JsonProcessingException {
        if (this.gameController.gameState == GameState.WAITING_FOR_PLAYERS) return;
        this.gameController.update(dt);

        framesSinceLastNetworkSync++;
        if (framesSinceLastNetworkSync > 1) {
            framesSinceLastNetworkSync = 0;
            this.game.lobby.updateGameState(this.gameController);
        }
    }
    public void checkCollisions() {
        if (this.gameController.ball.contains(this.gameController.player1)) {
            this.gameController.ball.velocity = new Vector2(5, 0);
        } else if (this.gameController.ball.contains(this.gameController.player2)) {
            this.gameController.ball.velocity = new Vector2(-5, 0);
        }
    }

    public void startGame() {
        this.gameController.gameState = GameState.PLAYING;
        this.gameController.ball.velocity = new Vector2(5, 0);

        try {
            this.game.lobby.updateGameState(this.gameController);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
        switch (this.gameController.gameState) {
            case WAITING_FOR_PLAYERS:
                return false;
            case WAITING_FOR_HOST:
                if (!isPlayer1) return false;
                if (keycode == Input.Keys.SPACE)
                    startGame();
                break;
            case PLAYING:
                if (keycode == Input.Keys.W) {
                    if (isPlayer1)
                        gameController.player1.velocity = new Vector2(0, 10);
                    else
                        gameController.player2.velocity = new Vector2(0, 10);
                } else if (keycode == Input.Keys.S) {
                    if (isPlayer1)
                        gameController.player1.velocity = new Vector2(0, -10);
                    else
                        gameController.player2.velocity = new Vector2(0, -10);
                }
                break;
        }



        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        switch (this.gameController.gameState) {
            case WAITING_FOR_PLAYERS:
                return false;
            case WAITING_FOR_HOST:
                if (!isPlayer1) return false;
                if (keycode == Input.Keys.SPACE)
                    startGame();
                break;
            case PLAYING:
                if (keycode == Input.Keys.W) {
                    if (isPlayer1)
                        gameController.player1.velocity = new Vector2(0, 0);
                    else
                        gameController.player2.velocity = new Vector2(0, 0);
                } else if (keycode == Input.Keys.S) {
                    if (isPlayer1)
                        gameController.player1.velocity = new Vector2(0, 0);
                    else
                        gameController.player2.velocity = new Vector2(0, 0);
                }
                break;
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
