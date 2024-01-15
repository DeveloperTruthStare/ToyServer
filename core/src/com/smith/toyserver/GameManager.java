package com.smith.toyserver;

import com.badlogic.gdx.Game;

import java.util.Random;

public class GameManager implements Runnable {
    private final GameState gameState;
    private Random rand = new Random();
    private Thread mainThread;
    public GameManager(Thread mainThread) {
        this.mainThread = mainThread;
        this.gameState = new GameState();
    }

    public void update(float dt) {
        this.gameState.player1.update(dt);
        this.gameState.player2.update(dt);
        this.gameState.ball.update(dt);
        System.out.println(gameState.ball.position.y);
        if (gameState.ball.contains(gameState.player1)) {
            gameState.ball.bouncePlayer(gameState.player1);
        } else if (gameState.ball.contains(gameState.player2)) {
            gameState.ball.bouncePlayer(gameState.player2);
        }
    }

    public void setVelocity(int player, Vector2 velocity) {
        if (player == 1) {
            this.gameState.player1.velocity = new Vector2(velocity.x, velocity.y);
        } else if (player == 2) {
            this.gameState.player2.velocity = new Vector2(velocity.x, velocity.y);
        }
    }
    public void sync(GameState serverState) {
        gameState.player1.syncWith(serverState.player1);
        gameState.player2.syncWith(serverState.player2);
        gameState.ball.syncWith(serverState.ball);
    }
    public GameState getGameState() {
        return this.gameState;
    }

    @Override
    public void run() {
        while (mainThread.isAlive()) {
            update(0);

            try {
                Thread.sleep(1000 / 40);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
