package io.github.MathiasEvjen;

import io.github.MathiasEvjen.pieces.*;

public class Pieces {

    public static int[][] getPiece(int piece, int rotation) {
        switch (piece) {
            case 0:
                if (rotation == 0) return new IPiece().getPiece();
                return new IPiece().getRotation(rotation);
            case 1:
                if (rotation == 0) return new NPiece().getPiece();
                return new NPiece().getRotation(rotation);
            case 2:
                if (rotation == 0) return new NMirroredPiece().getPiece();
                return new NMirroredPiece().getRotation(rotation);
            case 3:
                if (rotation == 0) return new LPiece().getPiece();
                return new LPiece().getRotation(rotation);
            case 4:
                if (rotation == 0) return new LMirroredPiece().getPiece();
                return new LMirroredPiece().getRotation(rotation);
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

    public SquarePiece getSquarePiece() {
        return new SquarePiece();
    }

    public IPiece getIPiece() {
        return new IPiece();
    }

    public LPiece getLPiece() {
        return new LPiece();
    }

    public LMirroredPiece getLMirroredPiece() {
        return new LMirroredPiece();
    }

    public NPiece getNPiece() {
        return new NPiece();
    }

    public NMirroredPiece getNMirroredPiece() {
        return new NMirroredPiece();
    }

    public TPiece getTPiece() {
        return new TPiece();
    }
}
