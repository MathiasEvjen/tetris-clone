package io.github.MathiasEvjen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class GameLogic {
    final Main game;

    private class PointF{
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

        public void addTile(float x, float y, int tile) {
            tileCoords[tile] = new PointF(x, y);
        }

        public void setPivotCoords(int x, int y) {
            pivotCoords.x = x;
            pivotCoords.y = y;
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

        public int length() {
            return tileCoords.length;
        }
    }

    private final char[][] gameBoard;


    private record CreatePieceBoundaries(int startX, int startY, int stopX, int stopY) {}

    private static final int TOTAL_PIECES = 7;

    private Piece fallingPiece;
    private Piece nextPiece;
    private Piece heldPiece;
    private Piece ghostPiece;
    private Array<PointF> landedTiles;

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

//    private int distanceToBottom;

    // Animation

    private float animationSpeed;

    private float dropSpeed;

    private boolean dropToBottom;


    private Array<Integer> rowsToRemove;
    private boolean shouldRemove;
    private int tileToRemove;
    private float removeSpeedSeconds;
    private boolean removedRow;
    private int removedCount;

    public GameLogic(char[][] gameBoard, Main game) {
        this.game = game;
        this.gameBoard = gameBoard;

        nextPieceID = (int) (Math.random() * TOTAL_PIECES); //MathUtils.random(0, 6);
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
        shouldRemove = false;
        tileToRemove = 0;
        removeSpeedSeconds = .01f;
        removedRow = false;

        landedTiles = new Array<>();
    }



    /* ------------------------------ */
    /*                                */
    /*            Updates             */
    /*                                */
    /* ------------------------------ */

    public void update(float dt) {
        updateTimers(dt);
        updateGhostPiece();
        updateFallingPiece(dt);

        if (shouldRemove) {
            processRowRemoval();
            return;
        }
        findFullRows();  // Checks for filled rows and removes them

        if (!currentPieceIsFalling) {
            setCurrentPiece();

            currentPieceRotation = 0;   // Resets the rotation to default
            moveDownTimerSeconds = 0;   // Sets the move down timer to 0

            // Sets the start and stop coordinates for the new piece, creates and draws it and initates that the piece is falling
            setStartAndStopCoordsCurrentPiece(currentPieceID);
            fallingPiece = createNewFallingPiece(currentPieceID, currentPieceRotation);
            currentPieceIsFalling = true;

            // Sets the start and stop coordinates for the next piece and creates and draws it
            setStartAndStopCoordsNextPiece(nextPieceID);
            nextPiece = createSpecificPiece(nextPieceID);

            // Sets the start and stop coordinates for the ghost piece and creates and draws it
            ghostPiece = createGhostPiece(currentPieceID);
        }
    }

    private void updateTimers(float dt) {
        moveTimerSeconds += dt;
        moveDownTimerSeconds += dt;
        animationTimer += dt;
        removeTimerSeconds += dt;
    }

    private void updateFallingPiece(float dt) {
        // Moves the falling piece smoothly downards
        // Stops when the piece hits the bottom
        if (dropToBottom) {
            clearFallingPieceOnBoard();
            movePieceToBottom(dt);
            handlePieceLanding();
        }

        // Check if piece is at bottom
        if (!dropToBottom && currentPieceIsFalling) {
            checkIfPieceAtBottom();
        }

        // If there is a piece falling it will move downwards every second
        if (!dropToBottom && !pieceLanded && currentPieceIsFalling && moveDownTimerSeconds > moveDownSpeedSeconds) {
            movePieceVertically(-1);
        }

        handleHighestTile();
    }



    /* ------------------------------ */
    /*                                */
    /*  Piece creation and selection  */
    /*                                */
    /* ------------------------------ */

    private void setCurrentPiece() {
        if (holdingPiece) {
            if (firstHeldPiece) {
                holdPiece();
            } else {
                swapHeldPiece();
            }
        }
        // Sets the next piece as the current piece and creates a random next piece
        else {
            currentPieceID = nextPieceID;
            nextPieceID = (int) (Math.random() * TOTAL_PIECES);
        }
    }

    private void swapHeldPiece() {
        int tmp = currentPieceID;
        currentPieceID = heldPieceID;
        heldPieceID = tmp;
    }

    private void holdPiece() {
        heldPieceID = currentPieceID;
        currentPieceID = nextPieceID;
        nextPieceID = (int) (Math.random() * 6);
        firstHeldPiece = false;
    }

    private Piece createNewFallingPiece(int currentPieceID, int rotation) {
        int[][] randomPiece = PiecePicker.getPiece(currentPieceID, rotation);   // Creates a piece from currentPieceID
        int tile = 0;

        Piece newPiece = new Piece(currentPieceID, rotation);

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (randomPiece[y2][x2] != 0 && randomPiece[y2][x2] != 3) {
                    newPiece.addTile(x1, y1, tile);
                    gameBoard[y1-FLOOR][x1-LEFT_EDGE] = 'F';
                    tile++;
                }
                if (randomPiece[y2][x2] == 2 || randomPiece[y2][x2] == 3) {
                    newPiece.setPivotCoords(x1, y1);
                }
            }
        }
        return newPiece;
    }

    private Piece createSpecificPiece(int pieceID) {
        int[][] pickedPiece = PiecePicker.getPiece(pieceID, 0);
        int tile = 0;

        Piece createdPiece = new Piece(pieceID, 0);

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (pickedPiece[y2][x2] != 0 && pickedPiece[y2][x2] != 3) {
                    createdPiece.addTile(x1, y1, tile);
                    tile++;
                }
            }
        }
        return createdPiece;
    }

    private Piece createGhostPiece(int currentPieceID) {
        int lowestFallingTileY = findLowestFallingTileY();
        int distanceToBottom = findDistanceToBottom(lowestFallingTileY);

        Piece createdGhostPiece = new Piece(currentPieceID);

        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            ghostPiece.addTile(fallingPiece.getX(tile), fallingPiece.getY(tile) - distanceToBottom, tile);
        }
        return createdGhostPiece;
    }

    private CreatePieceBoundaries calculatePieceBoundaries(int currentPieceID, int edge) {
        int startX, startY, stopX, stopY;

        switch (currentPieceID) {
            // Sets the start and stop coordinates for creating IPieces
            case 0 -> {
                startX = edge + (edge == LEFT_EDGE ? 2 : 1);
                startY = (edge == LEFT_EDGE ? CEILING : CEILING - 1);
                stopX = edge + (edge == LEFT_EDGE ? 7 : 6);
                stopY = (edge == LEFT_EDGE ? CEILING - 5 : CEILING - 6);
            }

            // Sets the start and stop coordinates for creating S and Z pieces
            case 1, 2 -> {
                startX = edge + (edge == LEFT_EDGE ? 3 : 2);
                startY = (edge == LEFT_EDGE ? CEILING + 2 : CEILING);
                stopX = edge + (edge == LEFT_EDGE ? 8 : 7);
                stopY = (edge == LEFT_EDGE ? CEILING -3 : CEILING - 5);
            }

            // Sets the start and stop coordinates for creating L, J, T and Square pieces
            default -> {
                startX = edge + (edge == LEFT_EDGE ? 2 : 1);
                startY = (edge == LEFT_EDGE ? CEILING + 1 : CEILING - 1);
                stopX = edge + (edge == LEFT_EDGE ? 7 : 6);
                stopY = (edge == LEFT_EDGE ? CEILING - 4 : CEILING - 5);
            }
        }

        return new CreatePieceBoundaries(startX, startY, stopX, stopY);
    }

    private void setStartAndStopCoordsCurrentPiece(int currentPieceID) {
        CreatePieceBoundaries pieceBoundaries = calculatePieceBoundaries(currentPieceID, LEFT_EDGE);

        startX = pieceBoundaries.startX();
        startY = pieceBoundaries.startY();
        stopX = pieceBoundaries.stopX();
        stopY = pieceBoundaries.stopY();
    }

    private void setStartAndStopCoordsNextPiece(int nextPieceID) {
        CreatePieceBoundaries pieceBoundaries = calculatePieceBoundaries(nextPieceID, RIGHT_EDGE);

        startX = pieceBoundaries.startX();
        startY = pieceBoundaries.startY();
        stopX = pieceBoundaries.stopX();
        stopY = pieceBoundaries.stopY();
    }



    /* ------------------------------ */
    /*                                */
    /*         Input handling         */
    /*                                */
    /* ------------------------------ */

    public void dropFallingPiece() {
        dropToBottom = true;
    }

    public void rotateClockwise() {
        if (currentPieceIsFalling) {
            if (pieceLanded) moveDownTimerSeconds = 0;

            // Checks if the current piece is at an edge and needs to be moved out to rotate
            checkIfPiecesAtEdge();

            // Updates the currently falling piece's rotation
            if (currentPieceRotation == 3) currentPieceRotation = 0;
            else currentPieceRotation++;

            // Creates an array of the coordinates of the next rotation of the currently falling piece
            int[] newRotationCoords = createRotatedCoords(currentPieceID, currentPieceRotation);
            if (newRotationCoords[0] == 99) return; // Aborts rotation if the first index of the coords is 99

            // Goes through the tiles of the falling piece and the newRotation coordinates and updates the tiles of the falling piece to the new coordinates
            updateFallingPieceCoords(newRotationCoords);
        }
    }



    /* ------------------------------ */
    /*                                */
    /*     Movement and landing       */
    /*                                */
    /* ------------------------------ */

    private void movePieceToBottom(float dt) {
        // Fluidly moves the tiles of the falling piece downward
        for (int fallingTile = 0; fallingTile < fallingPiece.length(); fallingTile++) {
            // Updates the Y coordinates of the tiles every fram
            fallingPiece.translateY(animationSpeed * dt, fallingTile);
        }
    }

    private void handlePieceLanding() {
        // Fluidly moves the tiles of the falling piece downward
        for (int fallingTile = 0; fallingTile < fallingPiece.length(); fallingTile++) {
            // Checks if the lowest tile of the falling piece is at or below the floor or at or below
            // the current position of the lowest ghost piece tile
            if (findLowestFallingTileY() <= FLOOR || fallingPiece.getY(fallingTile) <= ghostPiece.getY(fallingTile)) {
                // Sets the falling piece tile positions equal to the position of their ghost piece counterparts
                for (int tile = 0; tile < fallingPiece.length(); tile++) {
                    fallingPiece.updateTile(ghostPiece.getX(tile), ghostPiece.getY(tile), tile);
                }

                // Stops tile from dropping and lands the piece
                dropToBottom = false;
                landCurrentFallingPiece();
                break;
            };

        }
    }

    private void movePieceVertically(int distance) {
        for (int tile = fallingPiece.length()-1; tile >= 0; tile--) {   // Must be a reverse loop!!
            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'O';
            fallingPiece.translateY(distance, tile);
            fallingPiece.translatePivotY(distance);
            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'F';
        }
        moveDownTimerSeconds = 0;
    }

    private void landCurrentFallingPiece() {
        // Iterates through all the tiles of the landing piece
        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            landedTiles.add(fallingPiece.getTile(tile));   // Adds the tile to the array holding the landed tiles
            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'X'; // Sets the coordinates of the landed tiles as filled
            currentPieceIsFalling = false; // Piece falling is set to false and a new piece will be created
            pieceLanded = false;    // Sets falling piece as not landed so the new piece can fall
            currentPieceRotation = 0;
        }
        holdingPiece = false;
    }

    private void updateGhostPiece() {
        // Updates the location of ghost piece
        // TODO: Come back to see if fallingPiece needs to be fallingPiece.tileCoords
        if (fallingPiece != null && !dropToBottom) {
            int lowestFallingTileY = findLowestFallingTileY();  // Finds the Y coordinate of the lowest tile of the falling piece
            int distanceToBottom = findDistanceToBottom(lowestFallingTileY);   // Calculates the distance to the bottom of the lowest tile

            // Updates X coordinates of the ghost piece to be the same as the falling piece
            // Updates Y coordinates to be the height of the falling piece - its distance to the bottom
            for (int tile = 0; tile < ghostPiece.length(); tile++) {
                ghostPiece.updateTile(fallingPiece.getX(tile), fallingPiece.getY(tile) - distanceToBottom, tile);
            }
        }
    }

    private void moveLandedFloatingRowsDown() {
        for (int i = rowsToRemove.size-1; i >= 0; i--) {
            for (int y = rowsToRemove.get(i)+1; y <= highestTile; y++) {
                moveLandedTileVertically(y);
            }
            highestTile--;
        }
    }

    public void moveLandedTileVertically(int y) {
        for (PointF landedTile : landedTiles) {
            if (landedTile.y-FLOOR == y) {
                gameBoard[(int)landedTile.y][(int)landedTile.x-LEFT_EDGE] = 'O';
                landedTile.translateY(-1);
                gameBoard[(int)landedTile.y-FLOOR][(int)landedTile.x-LEFT_EDGE] = 'X';
            }
        }
    }

    public int findDistanceToBottom(int lowestFallingTileY) {
        int distance = 0;

        for (int y = lowestFallingTileY; y >= 0; y--) {
            for (PointF tileCoord : fallingPiece.tileCoords) {
                if (gameBoard[(int)tileCoord.y-FLOOR-distance][(int)tileCoord.x-LEFT_EDGE] == 'X') {
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
        return distance;
    }

    private void checkIfPieceLanded() {
        for (PointF tileCoord : fallingPiece.tileCoords) {
            if ((tileCoord.y != FLOOR && gameBoard[(int)tileCoord.y-FLOOR-1][(int)tileCoord.x-LEFT_EDGE] == 'X') || tileCoord.y == FLOOR) {
                pieceLanded = true;
                break;
            }
        }
    }

    private void checkIfPieceAtBottom() {
        pieceLanded = false;

        // Checks the tiles to see if the tile directly under it is either filled or the bottom of the gameBoard
        // Sets the piece as landed if either criteria is met
        checkIfPieceLanded();

        // If the piece cannot move down it will be stored in the landedTilesSprites array
        if (pieceLanded && moveDownTimerSeconds > landTimeSeconds) {
            landCurrentFallingPiece();    // Lands the current tile
        }
    }



    /* ------------------------------ */
    /*                                */
    /*  Collision and edge detection  */
    /*                                */
    /* ------------------------------ */

    private void checkIfPiecesAtEdge() {
        switch (currentPieceID) {
            case 0:
                handleIPieceAtWall();
                break;
            case 1:
                zPieceAtEdge();
                break;
            case 2:
                sPieceAtEdge();
                break;
            case 3:
                lPieceAtEdge();
                break;
            case 4:
                jPieceAtEdge();
                break;
            case 6:
                tPieceAtEdge();
                break;
            default:
                break;
        }
    }

    private void handleIPieceAtWall() {
        // Checks if an I-Piece is at a left or right wall
        boolean iPieceAtWallLeft = checkIfIPieceAtWallLeft();
        boolean iPieceAtWallRight = checkIfIPieceAtWallRight();
        boolean iPieceAtFloor = checkIfIPieceAtFloor();

        // If I-Piece is at a left wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (iPieceAtWallLeft) {
            shiftIPieceFromLeftWall();
        }

        // If I-Piece is at a right wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (iPieceAtWallRight) {
            shiftIPieceFromRightWall();
        }

        if (iPieceAtFloor) {
            shiftIPieceFromFloor();
        }
    }

    public void zPieceAtEdge() {
        boolean zPieceAtWallLeft = false;
        boolean zPieceAtWallRight = false;
        boolean zPieceAtCeiling = false;
        boolean zPieceAtFloor = false;

        // Checks if a Z-Piece is at a wall, ceiling or floor
        if (currentPieceID == 1) {
            int zPieceAtWallLeftCounter = 0;
            int zPieceAtWallRightCounter = 0;
            int zPieceAtCeilingCounter = 0;
            int zPieceAtFloorCounter = 0;
            for (Sprite tile : fallingPieceTiles) {
                if (tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE-1] == 'X') zPieceAtWallLeftCounter++;
                if (tile.getX() == RIGHT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE+1] == 'X') zPieceAtWallRightCounter++;
                if (tile.getY() == CEILING || gameBoard[(int)tile.getY()-FLOOR+1][(int)tile.getX()-LEFT_EDGE] == 'X') zPieceAtCeilingCounter++;
                if (tile.getY() == FLOOR || gameBoard[(int)tile.getY()-FLOOR-1][(int)tile.getX()-LEFT_EDGE] == 'X') zPieceAtFloorCounter++;
            }
            if (zPieceAtWallLeftCounter >= 1) zPieceAtWallLeft = true;
            if (zPieceAtWallRightCounter >= 1) zPieceAtWallRight = true;
            if (zPieceAtCeilingCounter == 2) zPieceAtCeiling = true;
            if (zPieceAtFloorCounter == 2) zPieceAtFloor = true;
        }

        // If Z-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (zPieceAtWallLeft) {
            // If Z-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the right
            if (currentPieceID == 1 && currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                movePieceOutFromLeftWall(nextRotation);
            }
        }

        // If Z-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (zPieceAtWallRight) {
            // If Z-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the left
            if (currentPieceID == 1 && currentPieceRotation == 1) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                movePieceOutFromRightWall(nextRotation);
            }
        }

        // If Z-Piece is at the ceiling and there is free space, the piecePivotCoords coords are moved down from the ceiling so it can rotate
        if (zPieceAtCeiling) {
            if (currentPieceID == 1 && currentPieceRotation == 0) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 1);
                movePieceOutFromCeiling(nextRotation);
            }
        }

        // If Z-Piece is at the floor and there is free sace, the piecePivotCoords coords are moved up from the floor so it can rotate
        if (zPieceAtFloor) {
            if (currentPieceID == 1 && currentPieceRotation == 2) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 3);
                movePieceOutFromFloor(nextRotation);
            }
        }
    }

    public void sPieceAtEdge() {
        boolean sPieceAtWallLeft = false;
        boolean sPieceAtWallRight = false;
        boolean sPieceAtCeiling = false;
        boolean sPieceAtFloor = false;

        // Checks if an S-Piece is at a wall, ceiling or floor
        if (currentPieceID == 2) {
            int sPieceAtWallLeftCounter = 0;
            int sPieceAtWallRightCounter = 0;
            int sPieceAtCeilingCounter = 0;
            int sPieceAtFloorCounter = 0;
            for (Sprite tile : fallingPieceTiles) {
                if (tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE-1] == 'X') sPieceAtWallLeftCounter++;
                if (tile.getX() == RIGHT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE+1] == 'X') sPieceAtWallRightCounter++;
                if (tile.getY() == CEILING || gameBoard[(int)tile.getY()-FLOOR+1][(int)tile.getX()-LEFT_EDGE] == 'X') sPieceAtCeilingCounter++;
                if (tile.getY() == FLOOR || gameBoard[(int)tile.getY()-FLOOR-1][(int)tile.getX()-LEFT_EDGE] == 'X') sPieceAtFloorCounter++;
            }
            if (sPieceAtWallLeftCounter >= 1) sPieceAtWallLeft = true;
            if (sPieceAtWallRightCounter >= 1) sPieceAtWallRight = true;
            if (sPieceAtCeilingCounter == 2) sPieceAtCeiling = true;
            if (sPieceAtFloorCounter == 2) sPieceAtFloor = true;
        }

        // If S-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (sPieceAtWallLeft) {
            // If S-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the right
            if (currentPieceID == 2 && currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                movePieceOutFromLeftWall(nextRotation);
            }
        }

        // If S-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (sPieceAtWallRight) {
            // If S-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the left
            if (currentPieceID == 2 && currentPieceRotation == 1) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                movePieceOutFromRightWall(nextRotation);
            }
        }

        // If S-Piece is at the ceiling and there is free space, the piecePivotCoords coords are moved down from the ceiling so it can rotate
        if (sPieceAtCeiling) {
            if (currentPieceID == 2 && currentPieceRotation == 0) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 1);
                movePieceOutFromCeiling(nextRotation);
            }
        }

        // If S-Piece is at the floor and there is free space, the piecePivotCoords coords are moved up from the floor so it can rotate
        if (sPieceAtFloor) {
            if (currentPieceID == 2 && currentPieceRotation == 2) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 3);
                movePieceOutFromFloor(nextRotation);
            }
        }
    }

    public void lPieceAtEdge() {
        boolean lPieceAtWallLeft = false;
        boolean lPieceAtWallRight = false;
        boolean lPieceAtFloor = false;

        // Checks if an L-Piece is at a wall
        if (currentPieceID == 3) {
            int lPieceAtWallLeftCounter = 0;
            int lPieceAtWallRightCounter = 0;
            int lPieceAtFloorCounter = 0;
            for (Sprite tile : fallingPieceTiles) {
                if (tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE-1] == 'X') lPieceAtWallLeftCounter++;
                if (tile.getX() == RIGHT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE+1] == 'X') lPieceAtWallRightCounter++;
                if (tile.getY() == FLOOR || gameBoard[(int)tile.getY()-FLOOR-1][(int)tile.getX()-LEFT_EDGE] == 'X') lPieceAtFloorCounter++;
            }
            if (lPieceAtWallLeftCounter >= 2) lPieceAtWallLeft = true;
            if (lPieceAtWallRightCounter >= 2) lPieceAtWallRight = true;
            if (lPieceAtFloorCounter >= 2) lPieceAtFloor = true;
        }

        // If L-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (lPieceAtWallLeft) {
            // If L-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the right
            if (currentPieceID == 3 && currentPieceRotation == 1) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                movePieceOutFromLeftWall(nextRotation);
            }
        }

        // If L-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (lPieceAtWallRight) {
            // If L-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the left
            if (currentPieceID == 3 && currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                movePieceOutFromRightWall(nextRotation);
            }
        }

        // If L-Piece is at the floor and there is free space, the piecePivotCoords coords are moved up from the floor so it can rotate
        if (lPieceAtFloor) {
            if (currentPieceID == 3 && currentPieceRotation == 0) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 1);
                movePieceOutFromFloor(nextRotation);
            }
        }
    }

    public void jPieceAtEdge() {
        boolean jPieceAtWallLeft = false;
        boolean jPieceAtWallRight = false;
        boolean jPieceAtFloor = false;

        // Checks if a J-Piece is at a wall
        if (currentPieceID == 4) {
            int jPieceAtWallLeftCounter = 0;
            int jPieceAtWallRightCounter = 0;
            int jPieceAtFloorCounter = 0;
            for (Sprite tile : fallingPieceTiles) {
                if (tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE-1] == 'X') jPieceAtWallLeftCounter++;
                if (tile.getX() == RIGHT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE+1] == 'X') jPieceAtWallRightCounter++;
                if (tile.getY() == FLOOR || gameBoard[(int)tile.getY()-FLOOR-1][(int)tile.getX()-LEFT_EDGE] == 'X') jPieceAtFloorCounter++;
            }
            if (jPieceAtWallLeftCounter >= 2) jPieceAtWallLeft = true;
            if (jPieceAtWallRightCounter >= 2) jPieceAtWallRight = true;
            if (jPieceAtFloorCounter >= 2) jPieceAtFloor = true;
        }

        // If J-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (jPieceAtWallLeft) {
            // If J-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the right
            if (currentPieceID == 4 && currentPieceRotation == 1) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                movePieceOutFromLeftWall(nextRotation);
            }
        }

        // If J-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (jPieceAtWallRight) {
            // If J-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the left
            if (currentPieceID == 4 && currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                movePieceOutFromRightWall(nextRotation);
            }
        }

        // If J-Piece is at the floor and there is free space, the piecePivotCoords coords are moved up from the floor so it can rotate
        if (jPieceAtFloor) {
            if (currentPieceID == 4 && currentPieceRotation == 0) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 1);
                movePieceOutFromFloor(nextRotation);
            }
        }
    }

    public void tPieceAtEdge() {
        boolean tPieceAtWallLeft = false;
        boolean tPieceAtWallRight = false;
        boolean tPieceAtFloor = false;

        // Checks if a T-Piece is at a wall
        if (currentPieceID == 6) {
            int tPieceAtWallLeftCounter = 0;
            int tPieceAtWallRightCounter = 0;
            int tPieceAtFloorCounter = 0;
            for (Sprite tile : fallingPieceTiles) {
                if (tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE-1] == 'X') tPieceAtWallLeftCounter++;
                if (tile.getX() == RIGHT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE+1] == 'X') tPieceAtWallRightCounter++;
                if (tile.getY() == FLOOR || gameBoard[(int)tile.getY()-FLOOR-1][(int)tile.getX()-LEFT_EDGE] == 'X') tPieceAtFloorCounter++;
            }
            if (tPieceAtWallLeftCounter >= 2) tPieceAtWallLeft = true;
            if (tPieceAtWallRightCounter >= 2) tPieceAtWallRight = true;
            if (tPieceAtFloorCounter >= 2) tPieceAtFloor = true;
        }

        // If T-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (tPieceAtWallLeft) {
            // If T-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the right
            if (currentPieceID == 6 && currentPieceRotation == 1) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                movePieceOutFromLeftWall(nextRotation);
            }
        }

        // If T-Piece is at the wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (tPieceAtWallRight) {
            // If T-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved one tile to the left
            if (currentPieceID == 6 && currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                movePieceOutFromRightWall(nextRotation);
            }
        }

        // If T-Piece is at the floor and there is free space, the piecePivotCoords coords are moved up from the floor so it can rotate
        if (tPieceAtFloor) {
            if (currentPieceID == 6 && currentPieceRotation == 0) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 1);
                movePieceOutFromFloor(nextRotation);
            }
        }
    }


    private boolean checkIfIPieceAtWallLeft() {
        int iPieceAtWallLeftCounter = 0;
        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            if (fallingPiece.getX(tile) == LEFT_EDGE ||
                gameBoard[(int)fallingPiece.getY(tile)][(int)fallingPiece.getX(tile)-LEFT_EDGE-1] == 'X') {
                    iPieceAtWallLeftCounter++;
                    if (iPieceAtWallLeftCounter == 2) return true;
            }
        }
        return false;
    }

    private boolean checkIfIPieceAtWallRight() {
        int iPieceAtWallRightCounter = 0;
        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            if (fallingPiece.getX(tile) == RIGHT_EDGE ||
                gameBoard[(int)fallingPiece.getY(tile) - FLOOR][(int)fallingPiece.getX(tile) - LEFT_EDGE + 1] == 'X') {
                    iPieceAtWallRightCounter++;
                    if (iPieceAtWallRightCounter == 2) return true;
            }
        }
        return false;
    }

    private boolean checkIfIPieceAtFloor() {
        int iPieceAtFloorCounter = 0;
        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            if (fallingPiece.getY(tile) == FLOOR || gameBoard[(int)fallingPiece.getY(tile) - FLOOR - 1][(int)fallingPiece.getX(tile) - LEFT_EDGE] == 'X')
                iPieceAtFloorCounter++;
        }
        return iPieceAtFloorCounter >= 2;
    }


    private void shiftIPieceFromLeftWall() {
        // If I-Piece is at rotation one and there is free space, the piecePivotCoords coords are moved 2 tiles to the right
        if (currentPieceRotation == 1) shiftIPieceRotationOneRight();

        // If I-Piece is at rotation three and there is free space, the piecePivotCoords coords are moved 1 tile to the right
        if (currentPieceRotation == 3) shiftIPieceRotationThreeRight();
    }

    private void shiftIPieceRotationOneRight() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
        for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
            for (int x1 = piecePivotCoords[0], x2 = 0; x1 < piecePivotCoords[0] + 5; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;    // Returns if there is not enough room to rotate
            }
        }
        piecePivotCoords[0] += 2;
        fallingPiece.translatePivotX(2);
    }

    private void shiftIPieceRotationThreeRight() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
        movePieceOutFromLeftWall(nextRotation);
    }


    private void shiftIPieceFromRightWall() {
        // If I-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved 1 til to the left
        if (currentPieceRotation == 1) shiftIPieceRotationOneLeft();

        // If I-Piece is at rotation 3 and there is free space, the piecePivotCoords coords are moved 2 tiles to the left
        if (currentPieceRotation == 3) shiftIPieceRotationThreeLeft();
    }

    private void shiftIPieceRotationOneLeft() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
        movePieceOutFromRightWall(nextRotation);
    }

    private void shiftIPieceRotationThreeLeft() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
        for (int y1 = fallingPiece.pivotCoords.y + 2, y2 = 0; y1 > fallingPiece.pivotCoords.y - 3; y1--, y2++) {
            for (int x1 = fallingPiece.pivotCoords.x - 4, x2 = 0; x1 < fallingPiece.pivotCoords.x + 1; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
            }
        }
        fallingPiece.translatePivotX(-2);
    }


    private void shiftIPieceFromFloor() {
        if (currentPieceRotation == 0) {
            shiftIPieceRotationZeroFloor();
        }

        if (currentPieceRotation == 2) {
            shiftIPieceRotationTwoFloor();
        }
    }

    private void shiftIPieceRotationZeroFloor() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, currentPieceRotation + 1);
        for (int y1 = fallingPiece.pivotCoords.y + 4, y2 = 0; y1 > fallingPiece.pivotCoords.y - 1; y1--, y2++) {
            for (int x1 = fallingPiece.pivotCoords.x - 2, x2 = 0; x1 < fallingPiece.pivotCoords.x + 3; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3) {
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
                }
            }
        }
        fallingPiece.translatePivotY(2);
    }

    private void shiftIPieceRotationTwoFloor() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, currentPieceRotation + 1);
        movePieceOutFromFloor(nextRotation);
    }



    

    // TODO: Design abstract method for rotations
    private void wallKickIPiece(int[][] nextRotation, int deltaX, int deltaY) {
        for (int y = 0; y < nextRotation.length; y++) {
            for (int x = 0; x < nextRotation[0].length; x++) {
                if (nextRotation[y][x] != 0 && nextRotation[y][x] != 3) {
                    int newX = fallingPiece.pivotCoords.x + (x - ) + deltaX;
                    int newY = fallingPiece.pivotCoords.y + y + deltaY;
                    if (newX < LEFT_EDGE || newX > RIGHT_EDGE || newY < FLOOR || newY > CEILING ||
                        gameBoard[newY-FLOOR][newX-LEFT_EDGE] == 'X') return;
                }
            }
        }
        fallingPiece.translatePivotX(deltaX);
        fallingPiece.translatePivotY(deltaY);
    }







    public void movePieceOutFromLeftWall(int[][] nextRotation) {
        for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 1, x2 = 0; x1 < piecePivotCoords[0] + 4; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
            }
        }
        piecePivotCoords[0]++;
    }

    public void movePieceOutFromRightWall(int[][] nextRotation) {
        for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 3, x2 = 0; x1 < piecePivotCoords[0] + 2; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
            }
        }
        piecePivotCoords[0]--;
    }

    public void movePieceOutFromCeiling(int[][] nextRotation) {
        for (int y1 = piecePivotCoords[1] + 1, y2 = 0; y1 > piecePivotCoords[1] - 4; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 2, x2 = 0; x1 < piecePivotCoords[0] + 3; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3) {
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
                }
            }
        }
        piecePivotCoords[1]--;
    }

    public void movePieceOutFromFloor(int[][] nextRotation) {
        for (int y1 = piecePivotCoords[1] + 3, y2 = 0; y1 > piecePivotCoords[1] - 2; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 2, x2 = 0; x1 < piecePivotCoords[0] + 3; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3) {
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
                }
            }
        }
        piecePivotCoords[1]++;
    }



    /* ---------------------------------- */
    /*                                    */
    /*  Row removal and baord management  */
    /*                                    */
    /* ---------------------------------- */

    // Row removal and board management
    private boolean removeRows() {
        if (tileToRemove > 9) { // 9 Is the board edge
            tileToRemove = 0;
            shouldRemove = false;
            return true;
        }

        if (removeTimerSeconds > removeSpeedSeconds) {
            // Loops through all the landed tiles on the gameBoard
            for (int row : rowsToRemove) {
                removeTiles(row);
            }

            removeTimerSeconds = 0;
            if (tileToRemove <= 9) tileToRemove++;
        }

        return false;
    }

    private void removeTiles(int row) {
        for (PointF landedTile : landedTiles) {
            // When a landed tile on that position is found it is deleted
            if (landedTile.y-FLOOR == row && landedTile.x-LEFT_EDGE == tileToRemove) {
                landedTiles.removeIndex(landedTiles.indexOf(landedTile, true)); // VIKTIG: Kan være grunnen til feilmedlding. Om nødvendig prøv false
                // Sets the tile slot on the gameBoard to O
                gameBoard[row][tileToRemove] = 'O';
            }
        }
    }

    private void findFullRows() {
        rowsToRemove.clear();

        // Goes through all the rows of the gameBoard
        for (int y = 0; y < gameBoard.length; y++) {
            // If all the tiles are the row is flagged to be removed
            if (isRowFull(y)) {
                rowsToRemove.add(y);
            }
        }

        if (rowsToRemove.size > 0) {
            shouldRemove = true;
            tileToRemove = 0;
            removedCount = 0;
        }
    }

    private boolean isRowFull(int y) {
        boolean filledRow = true;
        // Checks all the tiles in the row if they are full
        for (int x = 0; x < gameBoard[y].length; x++) {
            if (gameBoard[y][x] != 'X') {
                filledRow = false;
                break;
            }
        }
        return filledRow;
    }

    private void processRowRemoval() {
        boolean rowsRemoved = removeRows();

        // If a row was removed, the rest of the gameBoard is moved down to fill the empty space
        if (rowsRemoved) {
            moveLandedFloatingRowsDown();
        }
    }

    private void clearFallingPieceOnBoard() {
        // Sets the current positions of the falling piece to empty on the gameboard
        for (PointF tileCoord : fallingPiece.tileCoords) {
            gameBoard[(int)tileCoord.y-FLOOR][(int)tileCoord.x-LEFT_EDGE] = '0';
        }
    }

    private void handleHighestTile() {
        // Loops over all landed tiles and checks if one of them is at ceiling height
        for (PointF landedTile : landedTiles) {
            if (landedTile.y == CEILING) {

                // TODO: Come back to this after the refactoring
                // Switches to game over screen once a tile hits the ceiling
                //game.setScreen(new GameOverScreen(game, landedTilesTiles, score));
            }

            // Updates the current highest tile
            if (landedTile.y > highestTile) highestTile = (int)landedTile.y-FLOOR;
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


    /* ------------------------------ */
    /*                                */
    /*            Getters             */
    /*                                */
    /* ------------------------------ */

    public Piece getFallingPiece() {
        return fallingPiece;
    }

    public Piece getNextPiece() {
        return nextPiece;
    }

    public Piece getHeldPiece() {
        return heldPiece;
    }

    public Piece getGhostPiece() {
        return ghostPiece;
    }
}
