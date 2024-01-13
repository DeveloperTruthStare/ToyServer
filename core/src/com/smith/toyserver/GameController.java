package com.smith.toyserver;

public class GameController {
    public GameObject player1;
    public GameObject player2;
    public GameObject ball;
    public GameState gameState;
    public GameController() {};

    public void update(float dt) {
        player1.update(dt);
        player2.update(dt);
        ball.update(dt);
    }
}
