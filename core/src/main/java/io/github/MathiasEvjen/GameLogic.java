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
            createNewPiece(currentPieceID, currentPieceRotation);
            currentPieceIsFalling = true;

            // Sets the start and stop coordinates for the next piece and creates and draws it
            setStartAndStopCoordsNextPiece(nextPieceID);
            createNextPiece(nextPieceID);

            // Sets the start and stop coordinates for the ghost piece and creates and draws it
            createGhostPiece(currentPieceID);
        }
    }

    private void updateTimers(float dt) {
        moveTimerSeconds += dt;
        moveDownTimerSeconds += dt;
        animationTimer += dt;
        removeTimerSeconds += dt;
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
        int distanceToBottom = findDistanceToBottom(lowestFallingTileY);

        ghostPiece = new Piece(currentPieceID);

        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            ghostPiece.addTile(fallingPiece.getX(tile), fallingPiece.getY(tile) - distanceToBottom, tile);
        }
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
