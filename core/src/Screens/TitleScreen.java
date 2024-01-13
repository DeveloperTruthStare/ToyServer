package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.smith.toyserver.Button;
import com.smith.toyserver.IButtonCallback;
import com.smith.toyserver.ToyServer;

public class TitleScreen implements Screen, IButtonCallback, InputProcessor {
    public ToyServer game;
    private final Button quitButton;
    private final Button hostButton;
    public TitleScreen(ToyServer game) {
        this.game = game;
        this.game.setCurrentProcessor(this);
        hostButton = new Button((1920/2)-100, (1080/2)+100, 200, 50, "Host Lobby", 0, this);
        quitButton = new Button((1920/2)-100, (1080/2)-100, 200, 50, "Quit", 1, this);
    }
    @Override
    public void onClick(int buttonId) {
        switch(buttonId) {
            case 0:
                // Host Lobby Button
                PlayScreen playScreen = new PlayScreen(this.game);
                playScreen.host();
                this.game.swapScreen(playScreen);
                break;
            case 1:
                // Quit Button

                break;
        }
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        hostButton.render(game.batch);
        quitButton.render(game.batch);

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

    @Override
    public boolean keyDown(int keycode) {
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
        hostButton.onMouseButtonClick(screenX, 1080 - screenY);
        quitButton.onMouseButtonClick(screenX, 1080 - screenY);
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
