package com.smith.toyserver;

import com.badlogic.gdx.InputProcessor;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

public class Player implements InputProcessor {
    public final int W = 51;
    public final int A = 29;
    public static final int S = 47;
    public final int D = 32;

    private int x, y;
    private int velocity_x = 0;
    private int velocity_y = 0;

    final static int MAX_X = 1080;
    final static int MAX_Y = 1920;


    boolean isSelf = false;
    public Player(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public void update(float dt) {
        this.x += velocity_x;
        this.y += velocity_y;
        if (this.x >= MAX_X)
            this.x = MAX_X;
        if (this.y >= MAX_Y)
            this.y = MAX_Y;
        if (this.x <= 0)
            this.x = 0;
        if (this.y <= 0)
            this.y = 0;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == A)
            velocity_x = 10;
        if (keycode == D)
            velocity_x = -10;
        if (keycode == W)
            velocity_y = 10;
        if (keycode == S)
            velocity_y = -10;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == A)
            velocity_x = 0;
        if (keycode == D)
            velocity_x = 0;
        if (keycode == W)
            velocity_y = 0;
        if (keycode == S)
            velocity_y = 0;
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
