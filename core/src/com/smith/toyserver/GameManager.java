package com.smith.toyserver;

import com.badlogic.gdx.Game;

public class GameManager {
    private final GameState gameState;
    public GameManager() {
        this.gameState = new GameState();
    }

    public void update(float dt) {
        this.gameState.player1.update(dt);
        this.gameState.player2.update(dt);
        this.gameState.ball.update(dt);

        if (this.gameState.ball.contains(this.gameState.player1)) {
            this.gameState.ball.velocity = new Vector2(5, 0);
        } else if (this.gameState.ball.contains(this.gameState.player2)) {
            this.gameState.ball.velocity = new Vector2(-5, 0);
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
}
