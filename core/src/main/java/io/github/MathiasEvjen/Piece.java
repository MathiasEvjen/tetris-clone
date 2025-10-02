package io.github.MathiasEvjen;

import java.awt.*;

public class Piece {
    int pieceId;
    int rotation;
    Point pivotCoords = new Point();

    PointF[] tileCoords = new PointF[4];

    public Piece(int pieceId) {
        this.pieceId = pieceId;
    }

    public Piece(int pieceId, int rotation) {
        this.pieceId = pieceId;
        this.rotation = rotation;
    }

    public void addTile(float x, float y, int tile) {
        tileCoords[tile] = new PointF(x, y);
    }

    public void setPivotCoords(int x, int y) {
        pivotCoords.x = x;
        pivotCoords.y = y;
    }

    public void upDatePivotCoords(int x, int y) {
        pivotCoords.x += x;
        pivotCoords.y += y;
    }

    public void translatePivotX(int x) {
        pivotCoords.x += x;
    }

    public void translatePivotY(int y) {
        pivotCoords.y += y;
    }

    public float getX(int tile) {
        return tileCoords[tile].x;
    }

    public float getY(int tile) {
        return tileCoords[tile].y;
    }

    public void updateTile(float x, float y, int tile) {
        tileCoords[tile].x = x;
        tileCoords[tile].y = y;
    }

    public PointF getTile(int tile) {
        return tileCoords[tile];
    }

    public void translateX(float x, int tile) {
        tileCoords[tile].translateX(x);
    }

    public void translateY(float y, int tile) {
        tileCoords[tile].translateY(y);
    }

    public void translateTile(float x, float y, int tile) {
        tileCoords[tile].x += x;
        tileCoords[tile].y += y;
    }

    public int length() {
        return tileCoords.length;
    }
}
