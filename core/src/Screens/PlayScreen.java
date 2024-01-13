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
import com.badlogic.gdx.math.Vector2;
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


    public int state = 0;
    private final static int WAITING_FOR_PLAYER = 0;
    private final static int WAITING_FOR_HOST = 1;
    private final static int PLAYING = 2;
    private final static int GAME_OVER = 3;
    public BitmapFont font;

    private GameObject ball;
    private GameObject player;
    public boolean isPlayer1 = false;
    public ArrayList<GameObject> networkedGameObjects;
    public GameObject createNetworkedGO(int controller, int uniqueId) {
        GameObject go = new GameObject(controller, uniqueId);
        this.networkedGameObjects.add(go);
        return go;
    }

    public PlayScreen(ToyServer game) {
        this.game = game;
        this.game.setCurrentProcessor(this);
        this.game.lobby.msgProcessor = this;
        this.networkedGameObjects = new ArrayList<>();

        font = new BitmapFont();
        font.getData().setScale(5);
    }

    public void host() {
        this.game.lobby.createLobby();
        this.isPlayer1 = true;
        this.player = createNetworkedGO(0, 0);
        this.ball = createNetworkedGO(0, 2);
        player.position = new Vector2(100, 490);
        player.size = new Vector2(20, 100);

        ball.position = new Vector2((float) 1920 /2 - 5, (float) 1080 /2 - 5);
        ball.size = new Vector2(10, 10);

        ball.velocity = new Vector2(5, 0);
    }

    public void client() {
        this.player = createNetworkedGO(1, 1);
        this.ball = new GameObject(-1);

        player.position = new Vector2(1820, 490);
        player.size = new Vector2(20, 100);

        // Send message that we've connected
        this.game.lobby.sendNetworkMessage("Connected");
        this.state = WAITING_FOR_HOST;
    }
    public void updateObject(GameObject go) {
        for(int i = 0; i < networkedGameObjects.size(); ++i) {
            if (networkedGameObjects.get(i).getUniqueID() != go.getUniqueID()) return;
            networkedGameObjects.set(i, go);
        }
    }
    @Override
    public void processNetworkMessage(String msg) {
        if (msg.startsWith("Connected")) {
            this.state = WAITING_FOR_HOST;
        } else if (msg.startsWith("Update Object:")) {
            try {
                updateObject(new ObjectMapper().readValue(msg.substring(14), GameObject.class));
            } catch (Exception ignored) {

            }
        }
    }
    @Override
    public void onClick(int buttonId) {
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        try {
            this.update(delta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Gdx.gl.glClearColor(0.1f, 0.01f, 0.01f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        switch (this.state) {
            case WAITING_FOR_PLAYER:
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

        for (GameObject go : this.networkedGameObjects) {
           go.draw();
        }

        this.checkCollisions();
    }

    public void update(float dt) throws JsonProcessingException {
        if (this.state != PLAYING) return;
        for (GameObject go : networkedGameObjects) {
            go.update(dt);
            if ((go.getController() == 0 && isPlayer1) || (go.getController() == 1 && !isPlayer1)) {
                this.game.lobby.sendNetworkMessage("Update Object:" + new ObjectMapper().writeValueAsString(go));
            }
        }
    }
    public void checkCollisions() {
        for (GameObject go : networkedGameObjects) {
            if (go.getUniqueID() == this.ball.getUniqueID()) return;
            if (this.ball.contains(go)) {
                // move the ball away
                if (this.ball.position.x > go.position.x)
                    this.ball.velocity = new Vector2(5, 0);
                else
                    this.ball.velocity = new Vector2(-5, 0);
            }
        }
    }

    public void startGame() {
        this.state = PLAYING;
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
        switch (this.state) {
            case WAITING_FOR_PLAYER:
                return false;
            case WAITING_FOR_HOST:
                if (!isPlayer1) return false;
                if (keycode == Input.Keys.SPACE)
                    startGame();
                break;
            case PLAYING:
                if (keycode == Input.Keys.W) {
                    player.velocity = new Vector2(0, 10);
                } else if (keycode == Input.Keys.S) {
                    player.velocity = new Vector2(0, -10);
                }
                break;
        }



        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        switch (this.state) {
            case WAITING_FOR_PLAYER:
                return false;
            case WAITING_FOR_HOST:
                if (!isPlayer1) return false;
                if (keycode == Input.Keys.SPACE)
                    startGame();
                break;
            case PLAYING:
                if (keycode == Input.Keys.W) {
                    player.velocity = new Vector2(0, 0);
                } else if (keycode == Input.Keys.S) {
                    player.velocity = new Vector2(0, -0);
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
