package com.smith.toyserver;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smith.toyserver.Vector2;

import java.awt.Shape;

public class GameObject {
    public Vector2 position;
    public Vector2 size;
    public Vector2 velocity;
    private ShapeRenderer shapeRenderer;
    public GameObject() {
        init();
    }

    public void init() {
        this.position = new Vector2(0, 0);
        this.size = new Vector2(0, 0);
        this.velocity = new Vector2(0, 0);
    }

    public void draw() {
        if (shapeRenderer == null) shapeRenderer = new ShapeRenderer();
        // Assume batch is ended
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(this.position.x, this.position.y, this.size.x, this.size.y);
        shapeRenderer.end();
    }

    public void update(float dt) {
        // Update
        this.position = new Vector2(this.position.x + this.velocity.x, this.position.y + this.velocity.y);
    }
    public boolean contains(GameObject go) {
        return this.position.x < go.position.x + go.size.x && this.position.x + this.size.x > go.position.x &&
                this.position.y < go.position.y + go.size.y && this.position.y + this.size.y > go.position.y;
    }
    public void syncWith(GameObject go) {
        if (go.velocity.x != this.velocity.x || go.velocity.y != this.velocity.y)
            this.velocity = new Vector2(go.velocity.x, go.velocity.y);
        if (Math.abs(go.position.x - this.position.x) > 2 || Math.abs(go.position.y - this.position.y) > 2) {
            System.out.println("Updating position");
            this.position = new Vector2(go.position.x, go.position.y);
        }
    }
}