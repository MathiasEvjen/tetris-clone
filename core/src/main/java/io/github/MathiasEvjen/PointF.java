package io.github.MathiasEvjen;

public class PointF {
    public float x;
    public float y;

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void translateX(float x) {
        this.x += x;
    }

    public void translateY(float y) {
        this.y += y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
