package io.github.MathiasEvjen;

import io.github.MathiasEvjen.pieces.*;

public class PiecePicker {

    public static int[][] getPiece(int piece, int rotation) {
        switch (piece) {
            case 0: return new IPiece().getRotation(rotation);
            case 1: return new ZPiece().getRotation(rotation);
            case 2: return new SPiece().getRotation(rotation);
            case 3: return new LPiece().getRotation(rotation);
            case 4: return new JPiece().getRotation(rotation);
            case 5: return new TPiece().getRotation(rotation);
            case 6: return new SquarePiece().getRotation(rotation);
            default: throw new IllegalArgumentException("Invalid piece id: " + piece);
        }
    }
}
