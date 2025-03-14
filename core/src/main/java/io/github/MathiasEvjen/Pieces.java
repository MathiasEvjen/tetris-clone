package io.github.MathiasEvjen;

import io.github.MathiasEvjen.pieces.*;

public class Pieces {

    public static int[][] getPiece(int piece, int rotation) {
        switch (piece) {
            case 0:
                if (rotation == 0) return new IPiece().getPiece();
                return new IPiece().getRotation(rotation);
            case 1:
                if (rotation == 0) return new ZPiece().getPiece();
                return new ZPiece().getRotation(rotation);
            case 2:
                if (rotation == 0) return new SPiece().getPiece();
                return new SPiece().getRotation(rotation);
            case 3:
                if (rotation == 0) return new LPiece().getPiece();
                return new LPiece().getRotation(rotation);
            case 4:
                if (rotation == 0) return new JPiece().getPiece();
                return new JPiece().getRotation(rotation);
            case 5:
                if (rotation == 0) return new SquarePiece().getPiece();
                return new SquarePiece().getRotation(rotation);
            case 6:
                if (rotation == 0) return new TPiece().getPiece();
                return new TPiece().getRotation(rotation);
            default:
                return new SquarePiece().getPiece();
        }
    }
}
