package com.smith.toyserver;

import com.badlogic.gdx.graphics.Color;

public class GameState {
    public GameObject player1;
    public GameObject player2;
    public Ball ball;
    public boolean gameStarted;
    public GameState() {
        player1 = new GameObject(Color.BLUE);
        player2 = new GameObject(Color.RED);
        ball = new Ball();

        player1.position = new Vector2(50, 490);
        player1.size = new Vector2(20, 100);

        player2.position = new Vector2(1870, 490);
        player2.size = new Vector2(20, 100);

        gameStarted = false;
    }
}
