package com.smith.toyserver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.awt.Shape;

public class Button {
    Rectangle rect;
    IButtonCallback callback;
    String text;
    BitmapFont font;
    ShapeRenderer shape;
    public boolean active = true;
    private int id;
    public Button(int x, int y, int width, int height, String text, int id, IButtonCallback callback) {
        this.rect = new Rectangle(x, y, width, height);
        this.callback = callback;
        this.text = text;
        font = new BitmapFont();
        shape = new ShapeRenderer();
        this.id = id;
    }

    public void onMouseButtonClick(int x, int y) {
        if (!active) return;
        if (x > this.rect.x && x < this.rect.x + this.rect.width && y > this.rect.y && y < this.rect.y + this.rect.height) {
            callback.onClick(this.id);
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.1f, 0.1f, 0.1f, 1);
        shape.rect(this.rect.x, this.rect.y, this.rect.width, this.rect.height);
        shape.end();
        batch.begin();
        font.draw(batch, text, this.rect.x + 15, this.rect.y+ this.rect.height/2);
    }
}