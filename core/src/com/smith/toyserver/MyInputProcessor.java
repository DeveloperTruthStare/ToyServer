package com.smith.toyserver;

import com.badlogic.gdx.InputProcessor;

import java.util.ArrayList;

public class MyInputProcessor implements InputProcessor {
    ArrayList<InputProcessor> listeners;

    public MyInputProcessor() {
        listeners = new ArrayList<>();
    }
    public void addListener(InputProcessor ip) {
        listeners.add(ip);
    }
    @Override
    public boolean keyDown(int keycode) {
        for (InputProcessor ip: listeners) {
            ip.keyDown(keycode);
        }
        System.out.println(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (InputProcessor ip: listeners) {
            ip.keyUp(keycode);
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
