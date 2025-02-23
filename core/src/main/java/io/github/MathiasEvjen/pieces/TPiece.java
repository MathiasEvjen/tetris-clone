package io.github.MathiasEvjen.pieces;

public class TPiece {

    private final int[][][] rotations = {
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
        },
        {
            {0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 1, 2, 1, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
        },
    };

    private int currentRotation = 0;

    public TPiece() {

    }

    public int[][] getPiece() {
        return rotations[currentRotation];
    }

    public int[][] getRightRotation() {
        if (currentRotation == 3) {
            currentRotation = 0;
            return rotations[currentRotation];
        }
        return rotations[++currentRotation];
    }

    public int[][] getLeftRotation() {
        if (currentRotation == 0) {
            currentRotation = 3;
            return rotations[currentRotation];
        }
        return rotations[--currentRotation];
    }

    public int getCurrentRotation() {
        return this.currentRotation;
    }
}
