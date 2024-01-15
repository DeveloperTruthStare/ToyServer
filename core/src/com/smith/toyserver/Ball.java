package com.smith.toyserver;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Random;

public class Ball extends GameObject {
    private Random rand = new Random();
    private int maxX = 1920;
    private int maxY = 1080;

    public Ball() {
        super(Color.WHITE);
        resetBall();
    }

    public void bounceVert() {
        velocity.y*=-1;
    }

    public void bouncePlayer(GameObject player) {
        color = player.color;
        velocity.x = velocity.x*-1.05f;
        velocity.y+=player.velocity.y*rand.nextFloat();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if(position.y<0 || position.y>maxY) {
            bounceVert();
        }
        if(position.x <0){
            resetBall();
        }
        if(position.x>maxX){
            resetBall();
            velocity.x*=-1;
        }
    }

    private void resetBall() {
        color = Color.WHITE;
        position = new Vector2(maxX/2, maxY/2);
        size = new Vector2(25, 25);
        velocity = new Vector2(5, 0);
    }
}