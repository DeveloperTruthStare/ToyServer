package com.smith.toyserver;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smith.toyserver.utils.Vector2;

import java.awt.Shape;

public class GameObject {
    public Vector2 position;
    public Vector2 size;
    public Vector2 velocity;
    private ShapeRenderer shapeRenderer;
    public int controller = 0;     // Defaults to Host
    public int uniqueID = -1;
    public int lastUpdate = 0;
    public  GameObject() {
        init();

    }
    public GameObject(int controller) {
        this.controller = controller;
        init();
    }

    public GameObject(int controller, int uniqueID) {
        this.controller = controller;
        this.uniqueID = uniqueID;
        init();
    }

    public void init() {
        this.position = new Vector2(0, 0);
        this.size = new Vector2(0, 0);
        this.velocity = new Vector2(0, 0);
        shapeRenderer = new ShapeRenderer();
    }

    public void draw() {
        // Assume batch is ended
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(this.position.x, this.position.y, this.size.x, this.size.y);
        shapeRenderer.end();
    }

    public void update(float dt) {
        // Update
        this.lastUpdate += 1;
        this.position = new Vector2(this.position.x + this.velocity.x, this.position.y + this.velocity.y);
    }

    public boolean contains(GameObject go) {
        return this.position.x < go.position.x + go.size.x && this.position.x + this.size.x > go.position.x &&
                this.position.y < go.position.y + go.size.y && this.position.y + this.size.y > go.position.y;
    }
    public int getUniqueID() {
        return this.uniqueID;
    }
    public int getController() {
        return this.controller;
    }
}
