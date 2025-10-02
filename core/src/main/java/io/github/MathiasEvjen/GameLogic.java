package io.github.MathiasEvjen;

import com.badlogic.gdx.utils.Array;


import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameLogic {
    final Main game;

    private final char[][] gameBoard;
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;

    private record CreatePieceBoundaries(int startX, int startY, int stopX, int stopY) {}

    private enum PieceType {

        I_PIECE(0),
        Z_PIECE(1),
        S_PIECE(2),
        L_PIECE(3),
        J_PIECE(4),
        T_PIECE(5),
        SQUARE_PIECE(6);

        private final int id;

        PieceType(int id) {
            this.id = id;
        }

        public static PieceType fromId(int id) {
            for (PieceType type : values()) {
                if (type.id == id) return type;
            }
            throw new IllegalArgumentException("Invalid piece id: " + id);
        }
    }
    private static final int TOTAL_PIECES = PieceType.values().length;

    private final int[] CHECK_LEFT_WALL_BOUNDS = {1, 4, 2, 3};
    private final int[] CHECK_RIGHT_WALL_BOUNDS = {3, 2, 2, 3};
    private final int[] CHECK_CEILING_BOUNDS = {2, 3, 1, 4};
    private final int[] CHECK_FLOOR_BOUNDS = {2, 3, 3, 2};
    private final int[] Z_S_ROTATION_ORDER = {3, 1, 0, 2};
    private final int[] L_J_T_ROTATION_ORDER = {1, 3, 2, 0};

    private final int MOVE_PIECE_UP = 1;
    private final int MOVE_PIECE_DOWN = -1;
    private final int MOVE_PIECE_LEFT = -1;
    private final int MOVE_PIECE_RIGHT = 1;
    private final int NO_MOVEMENT = 0;

    private enum Axis {
        X_AXIS,
        Y_AXIS
    }


    private Piece fallingPiece;
    private Piece nextPiece;
    private Piece heldPiece;
    private Piece ghostPiece;
    private Map<PointF, Integer> landedTiles;

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

    private int currentPieceID;
    private PieceType currentPieceType;
    private int nextPieceID;
    private int heldPieceID;

    // Animation
    private float animationSpeed;
    private boolean dropToBottom;


    private Array<Integer> rowsToRemove;
    private boolean shouldRemove;
    private int tileToRemove;
    private float removeSpeedSeconds;

    public GameLogic(char[][] gameBoard, Main game) {
        this.game = game;
        this.gameBoard = gameBoard;

        nextPieceID = (int) (Math.random() * TOTAL_PIECES); //Mat
        holdingPiece = false;
        firstHeldPiece = true;

        moveSpeedSeconds = .1175f;
        moveDownSpeedSeconds = 1f;   // Defines the dropspeed of the pieces
        landTimeSeconds = .8f;

        animationSpeed = -300f;
        dropToBottom = false;

        highestTile = 0;

        rowsToRemove = new Array<>();
        shouldRemove = false;
        tileToRemove = 0;
        removeSpeedSeconds = .01f;

        landedTiles = new HashMap<>();
    }



    /* ------------------------------ */
    /*                                */
    /*            Updates             */
    /*                                */
    /* ------------------------------ */

    public void update(float dt) {
        if (!currentPieceIsFalling) {
            setCurrentPiece();

            currentPieceRotation = 0;   // Resets the rotation to default
            moveDownTimerSeconds = 0;   // Sets the move down timer to 0

            // Sets the start and stop coordinates for the new piece, creates and draws it and initates that the piece is falling
            setStartAndStopCoords(currentPieceID, LEFT_EDGE);
            fallingPiece = createNewFallingPiece(currentPieceID, currentPieceRotation);
            currentPieceIsFalling = true;

            // Sets the start and stop coordinates for the next piece and creates and draws it
            setStartAndStopCoords(nextPieceID, RIGHT_EDGE);
            nextPiece = createSpecificPiece(nextPieceID);

            // Sets the start and stop coordinates for the ghost piece and creates and draws it
            ghostPiece = createGhostPiece(currentPieceID);
        }

        updateTimers(dt);
        updateGhostPiece();
        updateFallingPiece(dt);

        if (shouldRemove) {
            processRowRemoval();
            return;
        }

        findFullRows();  // Checks for filled rows and removes them
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
            movePiece(NO_MOVEMENT, MOVE_PIECE_DOWN);
            moveDownTimerSeconds = 0;
        }

        handleHighestTile();
    }

    private void updateGhostPiece() {
        // Updates the location of ghost piece
        // TODO: Come back to see if fallingPiece needs to be fallingPiece.tileCoords
        if (fallingPiece != null && !dropToBottom) {
            int lowestFallingTileY = findLowestFallingTileY();
            int distanceToBottom = findDistanceToBottom(lowestFallingTileY);

            // Updates X coordinates of the ghost piece to be the same as the falling piece
            // Updates Y coordinates to be the height of the falling piece - its distance to the bottom
            for (int tile = 0; tile < ghostPiece.length(); tile++) {
                ghostPiece.updateTile(fallingPiece.getX(tile), fallingPiece.getY(tile) - distanceToBottom, tile);
            }
        }
    }



    /* ------------------------------ */
    /*                                */
    /*  Piece creation and selection  */
    /*                                */
    /* ------------------------------ */

    private void setCurrentPiece() {
        if (holdingPiece) {
            if (firstHeldPiece) {
                setHeldPiece();
            } else {
                swapHeldPiece();
            }
        }
        // Sets the next piece as the current piece and creates a random next piece
        else {
            currentPieceID = nextPieceID;
            currentPieceType = PieceType.fromId(currentPieceID);

            nextPieceID = (int) (Math.random() * TOTAL_PIECES);
        }
    }

    private void swapHeldPiece() {
        int tmp = currentPieceID;
        currentPieceID = heldPieceID;
        currentPieceType = PieceType.fromId(currentPieceID);
        heldPieceID = tmp;

        setStartAndStopCoordsHeldPiece(PieceType.fromId(heldPieceID));
        heldPiece = createSpecificPiece(heldPieceID);
    }

    private void setHeldPiece() {
        heldPieceID = currentPieceID;
        setStartAndStopCoordsHeldPiece(PieceType.fromId(heldPieceID));

        currentPieceID = nextPieceID;
        currentPieceType = PieceType.fromId(currentPieceID);
        nextPieceID = (int) (Math.random() * TOTAL_PIECES);

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
            createdGhostPiece.addTile(fallingPiece.getX(tile), fallingPiece.getY(tile) - distanceToBottom, tile);
        }
        return createdGhostPiece;
    }

    private CreatePieceBoundaries calculatePieceBoundaries(PieceType currentPieceType, int edge) {
        int startX, startY, stopX, stopY;

        switch (currentPieceType) {
            case I_PIECE -> {
                startX = edge + (edge == LEFT_EDGE ? 2 : 1);
                startY = (edge == LEFT_EDGE ? CEILING + 1 : CEILING - 1);
                stopX = edge + (edge == LEFT_EDGE ? 7 : 6);
                stopY = (edge == LEFT_EDGE ? CEILING - 4 : CEILING - 6);
            }

            case Z_PIECE, S_PIECE -> {
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

    private void setStartAndStopCoords(int pieceId, int edge) {
        CreatePieceBoundaries pieceBoundaries = calculatePieceBoundaries(PieceType.fromId(pieceId), edge);

        startX = pieceBoundaries.startX();
        startY = pieceBoundaries.startY();
        stopX = pieceBoundaries.stopX();
        stopY = pieceBoundaries.stopY();
    }

    public void setStartAndStopCoordsHeldPiece(PieceType setPieceType) {
        switch (setPieceType) {
            case I_PIECE:
                startX = 0;
                stopX = 5;
                startY = CEILING - 1;
                stopY = CEILING - 6;
                break;
            case S_PIECE:
            case Z_PIECE:
                startX = 1;
                stopX = 6;
                startY = CEILING;
                stopY = CEILING - 5;
                break;
            default:
                startX = 0;
                stopX = 5;
                startY = CEILING - 1;
                stopY = CEILING - 5;
                break;
        }
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

            handlePieceAtEdge();

            updatePieceRotation();

            // Creates an array of the coordinates of the next rotation of the currently falling piece
            PointF[] newRotationCoords = createRotatedCoords();
            if (newRotationCoords[0] == null) return; // Aborts rotation if the first index of the coords is 99

            // Goes through the tiles of the falling piece and the newRotation coordinates and updates the tiles of the falling piece to the new coordinates
            updateFallingPieceCoords(newRotationCoords);
        }
    }

    // TODO: Figure out why held piece is not drawn after piece is placed
    public void handleHoldPieceInput() {
        if (holdingPiece) return;
        setStartAndStopCoordsHeldPiece(PieceType.fromId(currentPieceID));

        heldPiece =  createSpecificPiece(currentPieceID);

        currentPieceIsFalling = false;
        holdingPiece = true;
    }

    public void handleMovePieceDownInput() {
        if (dropToBottom) return;

        pieceLanded = false;

        checkIfPieceLanded();

        if (pieceLanded) {
            currentPieceIsFalling = false;
            landCurrentFallingPiece();
        }

        if (moveTimerSeconds > moveSpeedSeconds) {
            movePiece(NO_MOVEMENT, MOVE_PIECE_DOWN);
            moveTimerSeconds = 0;
        }
        moveDownTimerSeconds = 0;

    }

    public void handleMovePieceLeft() {
        if (dropToBottom) return;

        if (moveTimerSeconds > moveSpeedSeconds) {
            if (checkIfPieceAtEdge(true, LEFT_EDGE, 1, -1, 0)) return;
            movePiece(MOVE_PIECE_LEFT, NO_MOVEMENT);
            moveTimerSeconds = 0;
        }
    }

    public void handleMovePieceRight() {
        if (dropToBottom) return;

        if (moveTimerSeconds > moveSpeedSeconds) {
            if (checkIfPieceAtEdge(true, RIGHT_EDGE, 1, 1, 0)) return;
            movePiece(MOVE_PIECE_RIGHT, NO_MOVEMENT);
            moveTimerSeconds = 0;
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

    public void movePiece( int xDist, int yDist) {
        for (int tile = fallingPiece.length()-1; tile >= 0; tile--) {   // Must be a reverse loop!!
            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'O';
            fallingPiece.translateTile(xDist, yDist, tile);
            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'F';
        }
        fallingPiece.upDatePivotCoords(xDist, yDist);

    }

    private void landCurrentFallingPiece() {
        // Iterates through all the tiles of the landing piece
        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            landedTiles.put(ghostPiece.getTile(tile), currentPieceID);  // Adds the tile to the array holding the landed tiles
            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'X'; // Sets the coordinates of the landed tiles as filled
            currentPieceIsFalling = false; // Piece falling is set to false and a new piece will be created
            pieceLanded = false;    // Sets falling piece as not landed so the new piece can fall
            currentPieceRotation = 0;
        }
        holdingPiece = false;
    }

    private void moveLandedFloatingRowsDown() {
        for (int i = rowsToRemove.size-1; i >= 0; i--) {
            for (int y = rowsToRemove.get(i)+1; y <= highestTile; y++) {
                moveLandedTileVertically(y);
            }
            highestTile--;
        }
    }

    private void moveLandedTileVertically(int y) {
        for (PointF landedTile : landedTiles.keySet()) {
            if (landedTile.y-FLOOR == y) {
                gameBoard[(int)landedTile.y-FLOOR][(int)landedTile.x-LEFT_EDGE] = 'O';
                landedTile.translateY(-1);
                gameBoard[(int)landedTile.y-FLOOR][(int)landedTile.x-LEFT_EDGE] = 'X';
            }
        }
    }

    private int findDistanceToBottom(int lowestFallingTileY) {
        int distance = 0;

        for (int y = lowestFallingTileY; y >= 0; y--) {
            for (PointF tileCoord : fallingPiece.tileCoords) {
                if ( gameBoard[(int)tileCoord.y-FLOOR-distance][(int)tileCoord.x-LEFT_EDGE] == 'X') {
                    distance--;
                    pieceLanded = true;
                    break;
                }
            }

            // TODO: This might be necessary
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

        checkIfPieceLanded();

        if (pieceLanded && moveDownTimerSeconds > landTimeSeconds) {
            landCurrentFallingPiece();
        }
    }

    private void updateFallingPieceCoords(PointF[] newRotationCoords) {
        for (int tile = 0, newRotationCounter = 0; tile < fallingPiece.length(); tile++, newRotationCounter++) {
            for (int fallingTile = 0; fallingTile < fallingPiece.length(); fallingTile++) {
                if (fallingPiece.getX(fallingTile) == newRotationCoords[newRotationCounter].getX() && fallingPiece.getY(fallingTile) == newRotationCoords[newRotationCounter].getY()) {
                    gameBoard[(int)fallingPiece.getY(fallingTile)-FLOOR][(int)fallingPiece.getX(fallingTile)-LEFT_EDGE] = 'F';
                } else {
                    gameBoard[(int)fallingPiece.getY(fallingTile)-FLOOR][(int)fallingPiece.getX(fallingTile)-LEFT_EDGE] = 'O';    // Sets the old position on the gameBoard as O
                }
            }

            // Set the current tile to new coordinates
            fallingPiece.updateTile(newRotationCoords[newRotationCounter].getX(), newRotationCoords[newRotationCounter].getY(), tile);

            gameBoard[(int)fallingPiece.getY(tile)-FLOOR][(int)fallingPiece.getX(tile)-LEFT_EDGE] = 'F';  // Sets the new positions on the gameBoard as falling
        }
    }

    private void updatePieceRotation() {
        if (currentPieceRotation == 3) currentPieceRotation = 0;
        else currentPieceRotation++;
    }

    private PointF[] createRotatedCoords() {
        int[][] piece = PiecePicker.getPiece(currentPieceID, currentPieceRotation);

//        int[] newRotationCoords = new int[8]; // Holds the coordinates from the next rotation
        PointF[] newRotationCoords = new PointF[4];
        int newRotationCounter = 0;    // Counter for the index of newRotation

        for (int y1 = fallingPiece.pivotCoords.y + 2, y2 = 0; y1 > fallingPiece.pivotCoords.y - 3; y1--, y2++) {
            for (int x1 = fallingPiece.pivotCoords.x - 2, x2 = 0; x1 < fallingPiece.pivotCoords.x + 3; x1++, x2++) {
                if (piece[y2][x2] != 0 && piece[y2][x2] != 3) {
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') {
                        if (currentPieceRotation == 0) currentPieceRotation = 3;
                        else currentPieceRotation--;
                        newRotationCoords[0] = null;  // Sets first index to 99 to signal abort
                        return newRotationCoords;    // Checks if the new position is taken or out of bounds
                    }

                    // Saves the new coordinates to an array so that no pieces are moved preemptively
                    newRotationCoords[newRotationCounter++] = new PointF(x1, y1);
                }
            }
        }

        return newRotationCoords;
    }



    /* ------------------------------ */
    /*                                */
    /*  Collision and edge detection  */
    /*                                */
    /* ------------------------------ */

    private void handlePieceAtEdge() {
        switch (currentPieceType) {
            case I_PIECE:
                handleIPieceAtEdge();
                break;
            case Z_PIECE:
            case S_PIECE:
                handlePieceAtEdge(2, Z_S_ROTATION_ORDER);
            case L_PIECE:
            case J_PIECE:
            case T_PIECE:
                handlePieceAtEdge(3, L_J_T_ROTATION_ORDER);
                break;
            default:
                break;
        }
    }

    private void handleIPieceAtEdge() {

        // If I-Piece is at a left wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (checkIfPieceAtEdge(true, LEFT_EDGE, 2,-1, 0)) {
            shiftIPieceFromLeftWall();
        }

        // If I-Piece is at a right wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (checkIfPieceAtEdge(true, RIGHT_EDGE, 2,1, 0)) {
            shiftIPieceFromRightWall();
        }

        if (checkIfPieceAtEdge(false, FLOOR, 2,0, -1)) {
            shiftIPieceFromFloor();
        }
    }

    private void shiftIPieceFromLeftWall() {
        // If I-Piece is at rotation one and there is free space, the piecePivotCoords coords are moved 2 tiles to the right
        if (currentPieceRotation == 1) shiftIPieceRotationOneRight();

        // If I-Piece is at rotation three and there is free space, the piecePivotCoords coords are moved 1 tile to the right
        if (currentPieceRotation == 3) shiftIPieceRotationThreeRight();
    }

    private void shiftIPieceRotationOneRight() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
        for (int y1 = fallingPiece.pivotCoords.y + 2, y2 = 0; y1 > fallingPiece.pivotCoords.y - 3; y1--, y2++) {
            for (int x1 = fallingPiece.pivotCoords.x, x2 = 0; x1 < fallingPiece.pivotCoords.x + 5; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;    // Returns if there is not enough room to rotate
            }
        }
        fallingPiece.translatePivotX(2);
    }

    private void shiftIPieceRotationThreeRight() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
        defaultShiftPieceFromEdge(
            nextRotation,
            CHECK_LEFT_WALL_BOUNDS[0],
            CHECK_LEFT_WALL_BOUNDS[1],
            CHECK_LEFT_WALL_BOUNDS[2],
            CHECK_LEFT_WALL_BOUNDS[3],
            1, 0);
    }


    private void shiftIPieceFromRightWall() {
        // If I-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved 1 til to the left
        if (currentPieceRotation == 1) shiftIPieceRotationOneLeft();

        // If I-Piece is at rotation 3 and there is free space, the piecePivotCoords coords are moved 2 tiles to the left
        if (currentPieceRotation == 3) shiftIPieceRotationThreeLeft();
    }

    private void shiftIPieceRotationOneLeft() {
        int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
        defaultShiftPieceFromEdge(
            nextRotation,
            CHECK_RIGHT_WALL_BOUNDS[0],
            CHECK_RIGHT_WALL_BOUNDS[1],
            CHECK_RIGHT_WALL_BOUNDS[2],
            CHECK_RIGHT_WALL_BOUNDS[3],
            -1, 0);
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
        defaultShiftPieceFromEdge(
            nextRotation,
            CHECK_FLOOR_BOUNDS[0],
            CHECK_FLOOR_BOUNDS[1],
            CHECK_FLOOR_BOUNDS[2],
            CHECK_FLOOR_BOUNDS[3],
            0, 1);
    }


    private void handlePieceAtEdge(int tilesAtEdgeLimit, int[] rotationOrder) {
        // Left edge
        if (checkIfPieceAtEdge(true, LEFT_EDGE, tilesAtEdgeLimit, -1, 0)) {
            if (currentPieceRotation == rotationOrder[0]) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                defaultShiftPieceFromEdge(
                    nextRotation,
                    CHECK_LEFT_WALL_BOUNDS[0],
                    CHECK_LEFT_WALL_BOUNDS[1],
                    CHECK_LEFT_WALL_BOUNDS[2],
                    CHECK_LEFT_WALL_BOUNDS[3],
                    1, 0);
            }
        }

        // Right edge
        if (checkIfPieceAtEdge(true, RIGHT_EDGE, tilesAtEdgeLimit,1, 0)) {
            if (currentPieceRotation == rotationOrder[1]) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                defaultShiftPieceFromEdge(
                    nextRotation,
                    CHECK_RIGHT_WALL_BOUNDS[0],
                    CHECK_RIGHT_WALL_BOUNDS[1],
                    CHECK_RIGHT_WALL_BOUNDS[2],
                    CHECK_RIGHT_WALL_BOUNDS[3],
                    -1, 0);
            }
        }

        // Ceiling
        if (checkIfPieceAtEdge(false, CEILING, tilesAtEdgeLimit,0, 1)) {
            if (currentPieceRotation == rotationOrder[2]) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 1);
                defaultShiftPieceFromEdge(
                    nextRotation,
                    CHECK_CEILING_BOUNDS[0],
                    CHECK_CEILING_BOUNDS[1],
                    CHECK_CEILING_BOUNDS[2],
                    CHECK_CEILING_BOUNDS[3],
                    0, -1);
            }
        }

        // Floor
        if (checkIfPieceAtEdge(false, FLOOR, tilesAtEdgeLimit,0, -1)) {
            if (currentPieceRotation == rotationOrder[3]) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 3);
                defaultShiftPieceFromEdge(
                    nextRotation,
                    CHECK_FLOOR_BOUNDS[0],
                    CHECK_FLOOR_BOUNDS[1],
                    CHECK_FLOOR_BOUNDS[2],
                    CHECK_FLOOR_BOUNDS[3],
                    0, 1);
            }
        }
    }

    private boolean checkIfPieceAtEdge(boolean xAxis, int edge, int tileAtEdgeLimit, int xEdgeOffset, int yEdgeOffset) {
        int tileAtEdgeCounter = 0;

        for (int tile = 0; tile < fallingPiece.length(); tile++) {
            if ((xAxis ? fallingPiece.getX(tile) : fallingPiece.getY(tile)) == edge ||
                gameBoard[(int) fallingPiece.getY(tile) - FLOOR + yEdgeOffset][(int) fallingPiece.getX(tile) - LEFT_EDGE + xEdgeOffset] == 'X')
                tileAtEdgeCounter++;
        }

        return tileAtEdgeCounter >= tileAtEdgeLimit;
    }

    public void defaultShiftPieceFromEdge(int[][] nextRotation, int xOffset1, int xOffset2, int yOffset1, int yOffset2, int deltaX, int deltaY) {
        for (int y1 = fallingPiece.pivotCoords.y + yOffset1, y2 = 0; y1 > fallingPiece.pivotCoords.y - yOffset2; y1--, y2++) {
            for (int x1 = fallingPiece.pivotCoords.x - xOffset1, x2 = 0; x1 < fallingPiece.pivotCoords.x + xOffset2; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
            }
        }
        fallingPiece.translatePivotX(deltaX);
        fallingPiece.translatePivotY(deltaY);
    }



    /* ---------------------------------- */
    /*                                    */
    /*  Row removal and baord management  */
    /*                                    */
    /* ---------------------------------- */

    private void findFullRows() {
        rowsToRemove.clear();

        // Goes through all the rows of the gameBoard
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            if (isRowFull(y)) {
                rowsToRemove.add(y);
            }
        }

        if (rowsToRemove.size > 0) {
            shouldRemove = true;
            tileToRemove = 0;
        }
    }

    private boolean isRowFull(int y) {
        boolean filledRow = true;
        // Checks all the tiles in the row if they are full
        for (int x = 0; x < BOARD_WIDTH; x++) {
            if (gameBoard[y][x] != 'X') {
                filledRow = false;
                break;
            }
        }
        return filledRow;
    }

    private void processRowRemoval() {
        boolean rowsRemoved = removeRows();


        if (rowsRemoved) {
            moveLandedFloatingRowsDown();
        }
    }

    private boolean removeRows() {
        if (tileToRemove > BOARD_WIDTH-1) {
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
        Iterator<PointF> it = landedTiles.keySet().iterator();
        while (it.hasNext()) {
            PointF landedTile = it.next();
            if (landedTile.y - FLOOR == row && landedTile.x - LEFT_EDGE == tileToRemove) {
                it.remove(); // Safe remove
                gameBoard[row][tileToRemove] = 'O';
            }
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
        for (PointF landedTile : landedTiles.keySet()) {
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

    public int getCurrentPieceID() {
        return currentPieceID;
    }

    public int getNextPieceID() {
        return nextPieceID;
    }

    public int getHeldPieceID() {
        return heldPieceID;
    }

    public boolean getIsHoldingPiece() {
        return holdingPiece;
    }

    public boolean isCurrentPieceIsFalling() {
        return currentPieceIsFalling;
    }

    public Map<PointF, Integer> getLandedTiles() {
        return landedTiles;
    }
}
