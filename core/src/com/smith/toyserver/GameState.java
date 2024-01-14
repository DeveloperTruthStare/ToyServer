package com.smith.toyserver;

public class GameState {
    public GameObject player1;
    public GameObject player2;
    public GameObject ball;
    public boolean gameStarted;
    public GameState() {
        player1 = new GameObject();
        player2 = new GameObject();
        ball = new GameObject();

        player1.position = new Vector2(50, 490);
        player1.size = new Vector2(20, 100);

        player2.position = new Vector2(1870, 490);
        player2.size = new Vector2(20, 100);

        ball.position = new Vector2(1920/2, 540);
        ball.size = new Vector2(25, 25);
        ball.velocity = new Vector2(5, 0);

        gameStarted = false;
    }
}
