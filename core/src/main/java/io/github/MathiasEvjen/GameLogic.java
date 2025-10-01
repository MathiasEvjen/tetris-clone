package io.github.MathiasEvjen;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class GameLogic {

    private class PointF{
        public float x;
        public float y;

        public PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private class Piece{
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

        public void addTile(float x, float y, int pos) {
            tileCoords[pos] = new PointF(x, y);
        }

        public void setPivotCoords(int x, int y) {
            pivotCoords.x = x;
            pivotCoords.y = y;
        }

        public float getX(int pos) {
            return tileCoords[pos].x;
        }

        public float getY(int pos) {
            return tileCoords[pos].y;
        }

        public void updateTile(float x, float y, int pos) {
            tileCoords[pos].x = x;
            tileCoords[pos].y = y;
        }

        public int length() {
            return tileCoords.length;
        }
    }

    private final char[][] gameBoard;


    private record CreatePieceBoundaries(int startX, int startY, int stopX, int stopY) {}

    private Piece fallingPiece;
    private Piece nextPiece;
    private Piece heldPiece;
    private Piece ghostPiece;

    // Boundary coords for creating new game pieces
    private int startY;
    private int stopY;
    private int startX;
    private int stopX;

    // Game board boundaries
    private final int LEFT_EDGE = 6;
    private final int RIGHT_EDGE = 15;
    private final int CEILING = 20;
    private final int FLOOR = 1;

    // Timers
    private float moveTimerSeconds;
    private float moveSpeedSeconds;
    private float moveDownTimerSeconds;
    private float moveDownSpeedSeconds;
    private float landTimeSeconds;
    private float animationTimer;
    private float removeTimerSeconds;

    private boolean currentPieceIsFalling;
    private int currentPieceRotation;
    private boolean pieceLanded;
    private boolean holdingPiece;
    private boolean firstHeldPiece;

    private int highestTile;

    private int[] piecePivotCoords;
    private int currentPieceID;
    private int nextPieceID;
    private int heldPieceID;

    private int distanceToBottom;

    // Animation

    private float animationSpeed;

    private float dropSpeed;

    private boolean dropToBottom;


    private Array<Integer> rowsToRemove;
    private boolean remove;
    private int removeX;
    private float removeSpeedSeconds;
    private boolean removedRow;
    private int removedCount;

    public GameLogic(char[][] gameBoard) {
        this.gameBoard = gameBoard;

        nextPieceID = (int) (Math.random() * 6); //MathUtils.random(0, 6);
        holdingPiece = false;
        firstHeldPiece = true;

        moveSpeedSeconds = .1175f;
        moveDownSpeedSeconds = 1f;   // Defines the dropspeed of the pieces
        landTimeSeconds = .8f;

        animationSpeed = -300f;
        dropToBottom = false;

        piecePivotCoords = new int[2];

        highestTile = 0;

        rowsToRemove = new Array<>();
        remove = false;
        removeX = 0;
        removeSpeedSeconds = .01f;
        removedRow = false;


    }

    public void update(float dt) {
        updateTimers(dt);
    }

    // Updates game timers
    private void updateTimers(float dt) {
        moveTimerSeconds += dt;
        moveDownTimerSeconds += dt;
        animationTimer += dt;
        removeTimerSeconds += dt;
    }

    private void updateGhostPiece() {
        // Updates the location of ghost piece
        if (fallingPieceTiles[0] != null && !dropToBottom) {
            int lowestFallingTileY = findLowestFallingTileY();  // Finds the Y coordinate of the lowest tile of the falling piece
            findDistanceToBottom(lowestFallingTileY);   // Calculates the distance to the bottom of the lowest tile

            // Updates X coordinates of the ghost piece to be the same as the falling piece
            // Updates Y coordinates to be the height of the falling piece - its distance to the bottom
            for (int i = 0; i < ghostPieceTiles.length; i++) {
                ghostPieceTiles[i].setX(fallingPieceTiles[i].getX());
                ghostPieceTiles[i].setY(fallingPieceTiles[i].getY() - distanceToBottom);
            }
        }
    }

    private void createNewPiece(int currentPieceID, int rotation) {
        int[][] newPiece = PiecePicker.getPiece(currentPieceID, rotation);   // Creates a piece from currentPieceID
        int tile = 0;

        fallingPiece = new Piece(currentPieceID, rotation);

        System.out.println("Current piece: " + currentPieceID);

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (newPiece[y2][x2] != 0 && newPiece[y2][x2] != 3) {
                    fallingPiece.addTile(x1, y1, tile);
                    gameBoard[y1-FLOOR][x1-LEFT_EDGE] = 'F';
                    tile++;
                }
                if (newPiece[y2][x2] == 2 || newPiece[y2][x2] == 3) {
                    fallingPiece.setPivotCoords(x1, y1);
                }
            }
        }
    }

    private void createNextPiece(int nextPieceID) {
        int[][] next = PiecePicker.getPiece(nextPieceID, 0);
        int tile = 0;

        nextPiece = new Piece(nextPieceID, 0);

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (next[y2][x2] != 0 && next[y2][x2] != 3) {
                    nextPiece.addTile(x1, y1, tile);
                    tile++;
                }
            }
        }
    }

    private void createHeldPiece(int pieceID) {
        int[][] hold = PiecePicker.getPiece(pieceID, 0);
        int tile = 0;

        heldPiece = new Piece(pieceID, 0);

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (hold[y2][x2] != 0 && hold[y2][x2] != 3) {
                    heldPiece.addTile(x1, y1, tile);
                    tile++;
                }
            }
        }
    }

    private void createGhostPiece(int currentPieceID) {
        int lowestFallingTileY = findLowestFallingTileY();
        findDistanceToBottom(lowestFallingTileY);

        ghostPiece = new Piece(currentPieceID);

        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            ghostPiece.addTile(fallingPiece.getX(tile), fallingPiece.getY(tile) - distanceToBottom, tile);
        }
    }

    private int findLowestFallingTileY() {
        int lowestTileY = (int)fallingPiece.getY(0);
        for (int tile = 1; tile < fallingPiece.length(); tile++)  {
            if ((int)fallingPiece.getY(tile) < lowestTileY) {
                lowestTileY = (int)fallingPiece.getY(tile);
            }
        }
        return lowestTileY;
    }


    public int findDistanceToBottom(int lowestFallingTileY) {
        int distance = 0;

        for (int y = lowestFallingTileY; y >= 0; y--) {
            for (Sprite sprite : fallingPieceTiles) {
                if (gameBoard[(int)sprite.getY()-FLOOR-distance][(int) sprite.getX()-LEFT_EDGE] == 'X') {
                    distance--;
                    pieceLanded = true;
                    break;
                }
            }
            if (y == FLOOR) {
                pieceLanded = true;
                break;
            }
            if (pieceLanded) {
                break;
            }

            distance++;
        }
        return = distance;
    }
}
