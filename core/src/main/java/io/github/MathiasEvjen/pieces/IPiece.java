package io.github.MathiasEvjen.pieces;

public class IPiece {
    private final int[][][] rotations = {
        {
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 1, 2, 1, 1},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        },
        {
            {0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 2, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0}
        },
        {
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {1, 1, 2, 1, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        },
        {
            {0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 2, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0}
        }
    };

    private int currentRotation = 0;

    public IPiece() {

    }

    public int[][] getPiece() {
        return rotations[currentRotation];
    }

    public int[][] getNextRotation() {
        if (currentRotation == 3) {
            currentRotation = 0;
            return rotations[currentRotation];
        }
        return rotations[++currentRotation];
    }

    public int getCurrentRotation() {
        return this.currentRotation;
    }
}
