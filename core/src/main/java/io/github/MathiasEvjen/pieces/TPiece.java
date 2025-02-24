package io.github.MathiasEvjen.pieces;

public class TPiece {

    private final int[][][] rotations = {
        {
            {0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 1, 2, 1, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
        },
        {
            {0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 2, 1, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0},
        },
        {
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 1, 2, 1, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0},
        },
        {
            {0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 1, 2, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0},
        }
    };

    private int currentRotation = 0;

    public TPiece() {

    }

    public int[][] getPiece() {
        return rotations[currentRotation];
    }

    public int[][] getRotation(int rotation) {
        return rotations[rotation];
    }

    public int getCurrentRotation() {
        return this.currentRotation;
    }
}
