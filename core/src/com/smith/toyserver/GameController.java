package com.smith.toyserver;

public class GameController {
    public GameObject player1;
    public GameObject player2;
    public GameObject ball;
    public GameState gameState;
    public GameController() {
        player1 = new GameObject(-1, -1);
        player2 = new GameObject(-1, -1);
        ball = new GameObject(-1, -1);
        gameState = GameState.WAITING_FOR_PLAYERS;
    };

    public void update(float dt) {
        player1.update(dt);
        player2.update(dt);
        ball.update(dt);
    }
}
