package com.smith.toyserver;

public class GameManager {
    public int position;
    public int direction;
    public GameManager() {
        position = 0;
        direction = 5;
    }
    public void update(float dt) {
        position += direction;
        if (position < 0)
            direction = 5;
        if (position > 1920)
            direction = -5;
    }
    public void processInput() {

    }

}
