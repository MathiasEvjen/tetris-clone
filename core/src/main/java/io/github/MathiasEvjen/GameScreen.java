/*
    TODO
    Potensielt skrive om checkIfPieceAtEdge og se om de kan legges inn i brikkenes klasser

    Legge til score og level visuelt på skjermen mens man spiller
    Legge til pausefunksjonalitet

    Flytte koordinater fra int[] til Point2d

*/

package io.github.MathiasEvjen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Arrays;

public class GameScreen implements Screen {
    final Main game;
    final GameLogic gameLogic;

    private final char[][] gameBoard;
    private record CreatePieceBoundaries(int startX, int startY, int stopX, int stopY) {}

    private int startY;
    private int stopY;
    private int startX;
    private int stopX;

    private final int LEFT_EDGE = 6;
    private final int RIGHT_EDGE = 15;
    private final int CEILING = 20;
    private final int FLOOR = 1;

    private final Texture background;

    private final Texture iPieceTex;
    private final Texture zPieceTex;
    private final Texture sPieceTex;
    private final Texture lPieceTex;
    private final Texture jPieceTex;
    private final Texture squarePieceTex;
    private final Texture tPieceTex;

    private final Texture iPieceGhostTex;
    private final Texture zPieceGhostTex;
    private final Texture sPieceGhostTex;
    private final Texture lPieceGhostTex;
    private final Texture jPieceGhostTex;
    private final Texture squarePieceGhostTex;
    private final Texture tPieceGhostTex;

    // Numbers
    private final Texture zero;
    private final Texture one;
    private final Texture two;
    private final Texture three;
    private final Texture four;
    private final Texture five;
    private final Texture six;
    private final Texture seven;
    private final Texture eight;
    private final Texture nine;

    private Sprite[] tileTexSprites;
    private Sprite[] ghostTileTexSprites;

    private Texture[] numbers;
    private Array<Sprite> scoreDigits;

    private Sprite[] fallingPieceTiles;
    private Sprite[] nextPieceIDSprites;
    private Sprite[] heldPieceTiles;
    private Sprite[] ghostPieceTiles;
    private Array<Sprite> landedTilesTiles;

    private float moveTimerSeconds;
    private float moveSpeedSeconds;
    private float moveDownTimerSeconds;
    private float moveDownSpeedSeconds;
    private float landTimeSeconds;

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

    private int score;
    private int level;
    private int completedRows;

    private int distanceToBottom;

    // Animation
    private float animationTimer;
    private float animationSpeed;

    private boolean dropToBottom;


    private Array<Integer> rowsToRemove;
    private boolean remove;
    private int removeX;
    private float removeSpeedSeconds;
    private float removeTimerSeconds;
    private boolean removedRow;
    private int removedCount;


    public GameScreen(final Main game) {
        this.game = game;

        // Initiates background texture
        background = new Texture("background.png");

        // Initiates the tile textures
        iPieceTex = new Texture("ITile.png");
        zPieceTex = new Texture("ZTile.png");
        sPieceTex = new Texture("STile.png");
        lPieceTex = new Texture("LTile.png");
        jPieceTex = new Texture("JTile.png");
        squarePieceTex = new Texture("SquareTile.png");
        tPieceTex = new Texture("TTile.png");

        // Initiates the ghost tile textures
        iPieceGhostTex = new Texture("ITileGhost.png");
        zPieceGhostTex = new Texture("ZTileGhost.png");
        sPieceGhostTex = new Texture("STileGhost.png");
        lPieceGhostTex = new Texture("LTileGhost.png");
        jPieceGhostTex = new Texture("JTileGhost.png");
        squarePieceGhostTex = new Texture("SquareTileGhost.png");
        tPieceGhostTex = new Texture("TTileGhost.png");

        // Initiates the tileTexSprites array and adds the tile textures
        tileTexSprites = new Sprite[7];
        tileTexSprites[0] = new Sprite(iPieceTex);
        tileTexSprites[1] = new Sprite(zPieceTex);
        tileTexSprites[2] = new Sprite(sPieceTex);
        tileTexSprites[3] = new Sprite(lPieceTex);
        tileTexSprites[4] = new Sprite(jPieceTex);
        tileTexSprites[5] = new Sprite(squarePieceTex);
        tileTexSprites[6] = new Sprite(tPieceTex);

        // Initiates the ghostTileTexSprites array and adds the ghost tile textures
        ghostTileTexSprites = new Sprite[7];
        ghostTileTexSprites[0] = new Sprite(iPieceGhostTex);
        ghostTileTexSprites[1] = new Sprite(zPieceGhostTex);
        ghostTileTexSprites[2] = new Sprite(sPieceGhostTex);
        ghostTileTexSprites[3] = new Sprite(lPieceGhostTex);
        ghostTileTexSprites[4] = new Sprite(jPieceGhostTex);
        ghostTileTexSprites[5] = new Sprite(squarePieceGhostTex);
        ghostTileTexSprites[6] = new Sprite(tPieceGhostTex);

        // Init numbers
        zero = new Texture("zero.png");
        one = new Texture("one.png");
        two = new Texture("two.png");
        three = new Texture("three.png");
        four = new Texture("four.png");
        five = new Texture("five.png");
        six = new Texture("six.png");
        seven =new Texture("seven.png");
        eight = new Texture("eight.png");
        nine =new Texture("nine.png");

        numbers = new Texture[10];
        numbers[0] =  zero;
        numbers[1] =  one;
        numbers[2] =  two;
        numbers[3] =  three;
        numbers[4] =  four;
        numbers[5] =  five;
        numbers[6] =  six;
        numbers[7] =  seven;
        numbers[8] =  eight;
        numbers[9] =  nine ;

        scoreDigits = new Array<>();

//        for (Sprite num : numbers) num.setSize(2, 2);

        nextPieceID = (int) (Math.random() * 6); //MathUtils.random(0, 6);
        holdingPiece = false;
        firstHeldPiece = true;

        fallingPieceTiles = new Sprite[4];
        nextPieceIDSprites = new Sprite[4];
        heldPieceTiles = new Sprite[4];
        ghostPieceTiles = new Sprite[4];

        moveSpeedSeconds = .1175f;
        moveDownSpeedSeconds = 1f;   // Defines the dropspeed of the pieces
        landTimeSeconds = .8f;

        animationSpeed = -300f;
        dropToBottom = false;

        piecePivotCoords = new int[2];

        landedTilesTiles = new Array<>();

        gameBoard = new char[20][10];
        for (char[] tile : gameBoard) {
            Arrays.fill(tile, 'O');
        }

        score = 0;
        level = 1;
        completedRows = 0;

        highestTile = 0;

        rowsToRemove = new Array<>();
        remove = false;
        removeX = 0;
        removeSpeedSeconds = .01f;
        removedRow = false;

        gameLogic = new GameLogic(gameBoard, game);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        float dt = Gdx.graphics.getDeltaTime();
        moveTimerSeconds += dt;
        moveDownTimerSeconds += dt;
        animationTimer += dt;
        removeTimerSeconds += dt;
        input();
        gameLogic.update(dt);
        logic();
        draw();
    }

    // PARTIALLY MOVED, NOT DONE
    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(background, 0, 0, worldWidth, worldHeight);


        /* TODO: Change this to pure visual with no logic
                 Will collect the coordinates from logic
                 and use them to draw the tiles
        */
        // Creates a new piece at the top if there is no current piece falling
        if (!currentPieceIsFalling) {
            // If a piece was just held, the held piece is set as the falling piece and the falling piece is set as the held piece
            if (holdingPiece && !firstHeldPiece) {
                int tmp = currentPieceID;
                currentPieceID = heldPieceID;
                heldPieceID = tmp;
            }

            // If it is the first held piece, the held piece is set as the current piece, the current piece is set as the next piece
            // Creates a random next piece and sets firstHeldPiece as false so it knows that there is a held piece
            else if (holdingPiece) {
                heldPieceID = currentPieceID;
                currentPieceID = nextPieceID;
                nextPieceID = (int) (Math.random() * 6);
                firstHeldPiece = false;
            }

            // Sets the current piece as next piece and creates a random next piece
            else {
                currentPieceID = nextPieceID;
                nextPieceID = (int) (Math.random() * 6);
            }

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

        // If there already is a piece falling, draws the current pieces
        else {
            // Draws the piece currently falling
            for (Sprite sprite : fallingPieceTiles) {
                sprite.draw(game.batch);
            }

            // Draws the next piece to fall
            for (Sprite nextSprite : nextPieceIDSprites) {
                nextSprite.draw(game.batch);
            }

            // Draws the held piece if there is a held piece
            if (heldPieceTiles[0] != null) {
                for (Sprite heldSprite : heldPieceTiles) {
                    heldSprite.draw(game.batch);
                }
            }

            // Draws the ghost tile
            for (Sprite ghostTile : ghostPieceTiles) {
                ghostTile.draw(game.batch);
            }
        }

        // Draws the landed tiles
        for (Sprite tile : landedTilesTiles) {
            tile.draw(game.batch);
        }

        // Draws the score
        for (Sprite scoreDigit : scoreDigits) {
            scoreDigit.draw(game.batch);
        }

        game.batch.end();
    }

    // PARTIALLY MOVED, NOT DONE
    private void input() {

        // Moves the piece directly to the bottom from the current position when the space key is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Find the lowest point in the piece

            dropToBottom = true;
            score += distanceToBottom;
        }

        // When the up key is pressed, the currently falling piece is rotated once clockwise
        if (currentPieceIsFalling && Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
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

        // Hold the currently falling piece if the falling piece hasn't already been held
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            if (holdingPiece) return;
            holdPiece(currentPieceID);
        }

        // Moves piece down when the down key is pressed
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && !dropToBottom) {
            if (moveTimerSeconds > moveSpeedSeconds) {
                movePieceDown();
                moveTimerSeconds = 0;
                score++;
            }
            moveDownTimerSeconds = 0;
        }

        // Moves piece left when the left key is pressed
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !dropToBottom) {
            if (moveTimerSeconds > moveSpeedSeconds) {
                movePieceLeft();
                moveTimerSeconds = 0;
            }
        }

        // Moves piece right when the right key is pressed
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !dropToBottom) {
            if (moveTimerSeconds > moveSpeedSeconds) {
                movePieceRight();
                moveTimerSeconds = 0;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pause();
        }
    }



    public void logic() {

        // TODO: Score is a disaster and needs work
        for (int i = 0; i < scoreDigits.size; i++) {
            scoreDigits.removeIndex(i);
        }

        if (score == 0) {
            scoreDigits.add(new Sprite(numbers[0]));
            scoreDigits.get(0).setSize(2, 2);
            scoreDigits.get(0).setX(14);
            scoreDigits.get(0).setY(22);
        }

        int currentScore = score;
        Array<Integer> tmp = new Array<>();
        int scoreDigitCounter = 0;
        if (scoreDigits.size != 0) scoreDigits.removeIndex(0);
        while (currentScore != 0) {
//            System.out.println();
            scoreDigits.add(new Sprite(numbers[currentScore % 10]));
            scoreDigits.get(scoreDigitCounter).setSize(2, 2);

            tmp.add(currentScore % 10);
//            scoreDigits.get(scoreDigitCounter).setX(14 - (2 * scoreDigitCounter));
//            scoreDigits.get(scoreDigitCounter).setY(22);
//            System.out.print(currentScore % 10 + " a");

            currentScore = currentScore / 10;

            scoreDigitCounter++;
        }


        for (int i = scoreDigits.size-1; i >= 0; i--) {
//            System.out.println(i);
            scoreDigits.get(i).setX(14 - (2 * i));
            scoreDigits.get(i).setY(22);
        }

//        System.out.println("Digits:" + scoreDigits.size);
//        System.out.println("Score:" + score);

//        for (int tall : tmp) System.out.print(tall + " ");
//        System.out.println(tmp.size);

//        printBoard();
    }

    // DONE
    private void createNewPiece(int currentPieceID, int rotation) {
        int[][] piece = PiecePicker.getPiece(currentPieceID, rotation);   // Creates a piece from currentPieceID
        int tiles = 0;

        System.out.println("Current piece: " + currentPieceID);

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (piece[y2][x2] != 0 && piece[y2][x2] != 3) {
                    fallingPieceTiles[tiles] = new Sprite(tileTexSprites[currentPieceID]);
                    fallingPieceTiles[tiles].setSize(1, 1);
                    fallingPieceTiles[tiles].setX(x1);
                    fallingPieceTiles[tiles].setY(y1);
                    gameBoard[y1-FLOOR][x1-LEFT_EDGE] = 'F';
                    fallingPieceTiles[tiles].draw(game.batch);
                    tiles++;
                }
                if (piece[y2][x2] == 2 || piece[y2][x2] == 3) {
                    piecePivotCoords[0] = x1;
                    piecePivotCoords[1] = y1;
                }
            }
        }
    }

    // DONE
    private void createNextPiece(int nextPieceID) {
        int[][] next = PiecePicker.getPiece(nextPieceID, 0);
        int tiles = 0;

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (next[y2][x2] != 0 && next[y2][x2] != 3) {
                    nextPieceIDSprites[tiles] = new Sprite(tileTexSprites[nextPieceID]);
                    nextPieceIDSprites[tiles].setSize(1, 1);
                    nextPieceIDSprites[tiles].setX(x1);
                    nextPieceIDSprites[tiles].setY(y1);
                    nextPieceIDSprites[tiles].draw(game.batch);
                    tiles++;
                }
            }
        }
    }

    // DONE
    private void createHeldPiece(int pieceID) {
        int[][] hold = PiecePicker.getPiece(pieceID, 0);
        int tiles = 0;

        for (int y1 = startY, y2 = 0; y1 > stopY; y1--, y2++) {
            for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                if (hold[y2][x2] != 0 && hold[y2][x2] != 3) {
                    heldPieceTiles[tiles] = new Sprite(tileTexSprites[currentPieceID]);
                    heldPieceTiles[tiles].setSize(1, 1);
                    heldPieceTiles[tiles].setX(x1);
                    heldPieceTiles[tiles].setY(y1);
                    tiles++;
                }
            }
        }
    }

    // DONE
    private void createGhostPiece(int currentPieceID) {
        int lowestFallingTileY = findLowestFallingTileY();
        findDistanceToBottom(lowestFallingTileY);

        for (int i = 0; i < fallingPieceTiles.length; i++) {
            ghostPieceTiles[i] = new Sprite(ghostTileTexSprites[currentPieceID]);
            ghostPieceTiles[i].setSize(1, 1);
            ghostPieceTiles[i].setX(fallingPieceTiles[i].getX());
            ghostPieceTiles[i].setY(fallingPieceTiles[i].getY() - distanceToBottom);
            ghostPieceTiles[i].draw(game.batch);
        }
    }

    // NOT MOVED
    public void holdPiece(int setPieceID) {
        switch (setPieceID) {
            case 0:
                startX = 0;
                stopX = 5;
                startY = CEILING - 1;
                stopY = CEILING - 6;
                break;
            case 1:
            case 2:
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

        createHeldPiece(setPieceID);

        currentPieceIsFalling = false;
        holdingPiece = true;
    }

    // MOVED, NOT DONE
    private void updateFallingPieceCoords(int[] newRotationCoords) {
        for (int tile = 0, newRotationCounter = 0; tile < fallingPieceTiles.length; tile++, newRotationCounter += 2) {
            for (Sprite fallingTile : fallingPieceTiles) {
                if (fallingTile.getX() == newRotationCoords[newRotationCounter] && fallingTile.getY() == newRotationCoords[newRotationCounter + 1]) {
                    gameBoard[(int)fallingPieceTiles[tile].getY()-FLOOR][(int)fallingPieceTiles[tile].getX()-LEFT_EDGE] = 'F';
                } else {
                    gameBoard[(int)fallingPieceTiles[tile].getY()-FLOOR][(int)fallingPieceTiles[tile].getX()-LEFT_EDGE] = 'O';    // Sets the old position on the gameBoard as O
                }
            }


            // Sets the current tile to new coordinates
            fallingPieceTiles[tile].setX(newRotationCoords[newRotationCounter]);
            fallingPieceTiles[tile].setY(newRotationCoords[newRotationCounter + 1]);

            gameBoard[(int)fallingPieceTiles[tile].getY()-FLOOR][(int)fallingPieceTiles[tile].getX()-LEFT_EDGE] = 'F';  // Sets the new positions on the gameBoard as falling
        }
    }

    // MOVED, NOT DONE
    private int[] createRotatedCoords(int pieceID, int rotation) {
        int[][] piece = PiecePicker.getPiece(currentPieceID, currentPieceRotation);

        int tile = 0;    // Counts the number of
        int[] newRotationCoords = new int[8]; // Holds the coordinates from the next rotation
        int newRotationCounter = 0;    // Counter for the index of newRotation

        for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 2, x2 = 0; x1 < piecePivotCoords[0] + 3; x1++, x2++) {
                if (piece[y2][x2] != 0 && piece[y2][x2] != 3) {
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') {
                        if (currentPieceRotation == 0) currentPieceRotation = 3;
                        else currentPieceRotation--;
                        newRotationCoords[0] = 99;  // Sets first index to 99 to signal abort
                        return newRotationCoords;    // Checks if the new position is taken or out of bounds
                    }

                    // Saves the new coordinates to an array so that no pieces are moved preemptively
                    newRotationCoords[newRotationCounter++] = x1;
                    newRotationCoords[newRotationCounter++] = y1;
                    tile++; // Increments the number of tiles counted
                }
            }
        }

        return newRotationCoords;
    }

    // DONE
    private void checkIfPiecesAtEdge() {
        switch (currentPieceID) {
            case 0:
                iPieceAtEdge();
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

    // DONE
    private boolean checkIfIPieceAtWallLeft() {
        int iPieceAtWallLeftCounter = 0;
        for (Sprite tile : fallingPieceTiles) {
            if (tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE-1] == 'X') iPieceAtWallLeftCounter++;
        }
        return iPieceAtWallLeftCounter >= 2;
    }

    // DONE
    private boolean checkIfIPieceAtWallRight() {
        int iPieceAtWallRightCounter = 0;
        for (Sprite tile : fallingPieceTiles) {
            if (tile.getX() == RIGHT_EDGE || gameBoard[(int) tile.getY() - FLOOR][(int) tile.getX() - LEFT_EDGE + 1] == 'X')
                iPieceAtWallRightCounter++;
        }
        return iPieceAtWallRightCounter >= 2;
    }

    // DONE
    private boolean checkIfIPieceAtFloor() {
        int iPieceAtFloorCounter = 0;
        for (Sprite tile : fallingPieceTiles) {
            if (tile.getY() == FLOOR || gameBoard[(int) tile.getY() - FLOOR - 1][(int) tile.getX() - LEFT_EDGE] == 'X')
                iPieceAtFloorCounter++;
        }
        return iPieceAtFloorCounter >= 2;
    }

    // DONE
    private void iPieceAtEdge() {
        // Checks if an I-Piece is at a left or right wall
        boolean iPieceAtWallLeft = checkIfIPieceAtWallLeft();
        boolean iPieceAtWallRight = checkIfIPieceAtWallRight();
        boolean iPieceAtFloor = checkIfIPieceAtFloor();

        // If I-Piece is at a left wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (iPieceAtWallLeft) {
            // If I-Piece is at rotation one and there is free space, the piecePivotCoords coords are moved 2 tiles to the right
            if (currentPieceRotation == 1) {
//                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
//                for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
//                    for (int x1 = piecePivotCoords[0], x2 = 0; x1 < piecePivotCoords[0] + 5; x1++, x2++) {
//                        if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
//                            if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;    // Returns if there is not enough room to rotate
//                    }
//                }
//                piecePivotCoords[0] += 2;
            }

            // If I-Piece is at rotation three and there is free space, the piecePivotCoords coords are moved 1 tile to the right
            if (currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                movePieceOutFromLeftWall(nextRotation);
            }
        }

        // If I-Piece is at a right wall and there is free space, the piecePivotCoords coords are moved out from the wall so it can rotate
        if (iPieceAtWallRight) {
            // If I-Piece is at rotation 1 and there is free space, the piecePivotCoords coords are moved 1 til to the left
            if (currentPieceRotation == 1) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 2);
                movePieceOutFromRightWall(nextRotation);
            }

            // If I-Piece is at rotation 3 and there is free space, the piecePivotCoords coords are moved 2 tiles to the left
            if (currentPieceRotation == 3) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, 0);
                for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
                    for (int x1 = piecePivotCoords[0] - 4, x2 = 0; x1 < piecePivotCoords[0] + 1; x1++, x2++) {
                        if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                            if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
                    }
                }
                piecePivotCoords[0] -= 2;
            }
        }

        if (iPieceAtFloor) {
            if (currentPieceRotation == 0) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, currentPieceRotation + 1);
                for (int y1 = piecePivotCoords[1] + 4, y2 = 0; y1 > piecePivotCoords[1] - 1; y1--, y2++) {
                    for (int x1 = piecePivotCoords[0] - 2, x2 = 0; x1 < piecePivotCoords[0] + 3; x1++, x2++) {
                        if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3) {
                            if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
                        }
                    }
                }
                piecePivotCoords[1] += 2;
            }

            if (currentPieceRotation == 2) {
                int[][] nextRotation = PiecePicker.getPiece(currentPieceID, currentPieceRotation + 1);
                movePieceOutFromFloor(nextRotation);
            }
        }
    }

    // DONE
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

    // DONE
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

    // DONE
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

    // DONE
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

    // DONE
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

    // DONE
    public void movePieceOutFromLeftWall(int[][] nextRotation) {
        for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 1, x2 = 0; x1 < piecePivotCoords[0] + 4; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
            }
        }
        piecePivotCoords[0]++;
    }

    // DONE
    public void movePieceOutFromRightWall(int[][] nextRotation) {
        for (int y1 = piecePivotCoords[1] + 2, y2 = 0; y1 > piecePivotCoords[1] - 3; y1--, y2++) {
            for (int x1 = piecePivotCoords[0] - 3, x2 = 0; x1 < piecePivotCoords[0] + 2; x1++, x2++) {
                if (nextRotation[y2][x2] != 0 && nextRotation[y2][x2] != 3)
                    if (x1 < LEFT_EDGE || x1 > RIGHT_EDGE || y1 < FLOOR || y1 > CEILING || gameBoard[y1-FLOOR][x1-LEFT_EDGE] == 'X') return;
            }
        }
        piecePivotCoords[0]--;
    }

    // DONE
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

    // DONE
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

    // DONE
    public void movePieceVertically(int distance) {
        for (int i = fallingPieceTiles.length-1; i >= 0; i--) {   // Must be a reverse loop!!
            gameBoard[(int)fallingPieceTiles[i].getY()-FLOOR][(int)fallingPieceTiles[i].getX()-LEFT_EDGE] = 'O';
            fallingPieceTiles[i].translateY(distance);
            gameBoard[(int)fallingPieceTiles[i].getY()-FLOOR][(int)fallingPieceTiles[i].getX()-LEFT_EDGE] = 'F';
        }
    }

    // DONE
    private int findLowestFallingTileY() {
        int lowestTileY = (int)fallingPieceTiles[0].getY();
        for (int i = 1; i < fallingPieceTiles.length; i++)  {
            if ((int)fallingPieceTiles[i].getY() < lowestTileY) {
                lowestTileY = (int)fallingPieceTiles[i].getY();
            }
        }
        return lowestTileY;
    }

    // MOVED, NOT DONE
    // Should return distance to bottom
    private void findDistanceToBottom(int lowestFallingTileY) {
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
        distanceToBottom = distance;
    }

    // NOT DONE
    public void movePieceDown() {
        pieceLanded = false;
        int lowestTile = (int) fallingPieceTiles[0].getY();
        for (int i = 1; i < fallingPieceTiles.length; i++) {
            if ((int) fallingPieceTiles[i].getY() < lowestTile) lowestTile = (int) fallingPieceTiles[i].getY();
        }

        checkIfPieceLanded();

        if (pieceLanded) {
            currentPieceIsFalling = false;
            landPiece();
        } else {
            movePieceVertically(-1);
            piecePivotCoords[1]--;
        }
    }

    // NOT DONE
    public void movePieceLeft() {
        for (Sprite tile : fallingPieceTiles) {
            if (tile == null || tile.getX() == LEFT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE - 1] == 'X') return;
        }

        movePieceLaterally(-1);
        piecePivotCoords[0]--;
    }

    // NOT DONE
    public void movePieceRight() {
        for (Sprite tile : fallingPieceTiles) {
            // Checks if the tiles furthest right are at the right edge
            if (tile == null || tile.getX() == RIGHT_EDGE || gameBoard[(int)tile.getY()-FLOOR][(int)tile.getX()-LEFT_EDGE + 1] == 'X') return;
        }

        movePieceLaterally(1);
        piecePivotCoords[0]++;
    }

    // NOT DONE
    // Moves the falling piece laterally
    // Takes in the distance to be moved
    public void movePieceLaterally(int distance) {
        for (Sprite sprite : fallingPieceTiles) {
            gameBoard[(int)sprite.getY()-FLOOR][(int)sprite.getX()-LEFT_EDGE] = 'O';
            sprite.translateX(distance);
            gameBoard[(int)sprite.getY()-FLOOR][(int)sprite.getX()-LEFT_EDGE] = 'F';
        }
    }

    // DONE
    private void checkIfPieceLanded() {
        for (Sprite tile : fallingPieceTiles) {
            if ((tile.getY() != FLOOR && gameBoard[(int)tile.getY()-FLOOR-1][(int)tile.getX()-LEFT_EDGE] == 'X') || tile.getY() == FLOOR) {
                pieceLanded = true;
                break;
            }
        }
    }

    // NOT MOVED, NOT DONE
    // Lands the current piece
    private void landPiece() {
        // Iterates through all the tiles of the landing piece
        for (int i = 0; i < fallingPieceTiles.length; i++) {
            landedTilesTiles.add(fallingPieceTiles[i]);   // Adds the tile to the array holding the landed tiles
            gameBoard[(int)fallingPieceTiles[i].getY()-FLOOR][(int)fallingPieceTiles[i].getX()-LEFT_EDGE] = 'X'; // Sets the coordinates of the landed tiles as filled
            fallingPieceTiles[i] = null; // The current falling piece array is reset to null
            currentPieceIsFalling = false; // Piece falling is set to false and a new piece will be created
            pieceLanded = false;    // Sets falling piece as not landed so the new piece can fall
            currentPieceRotation = 0;
        }
        holdingPiece = false;
    }

    // DONE
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

    // DONE
    private void setStartAndStopCoordsCurrentPiece(int currentPieceID) {
        CreatePieceBoundaries pieceBoundaries = calculatePieceBoundaries(currentPieceID, LEFT_EDGE);

        startX = pieceBoundaries.startX();
        startY = pieceBoundaries.startY();
        stopX = pieceBoundaries.stopX();
        stopY = pieceBoundaries.stopY();
    }

    // DONE
    private void setStartAndStopCoordsNextPiece(int nextPieceID) {
        CreatePieceBoundaries pieceBoundaries = calculatePieceBoundaries(nextPieceID, RIGHT_EDGE);

        startX = pieceBoundaries.startX();
        startY = pieceBoundaries.startY();
        stopX = pieceBoundaries.stopX();
        stopY = pieceBoundaries.stopY();
    }


    // TODO: More handling of points
    // Calculates and returns points when removing one row based on the player's current level
    private int calculatePoints1Row(int level) {
        return 100 * (level);
    }

    // Calculates and returns points when removing two rows based on the player's current level
    private int calculatePoints2Rows(int level) {
        return 100 * (level);
    }

    // Calculates and returns points when removing three rows based on the player's current level
    private int calculatePoints3Rows(int level) {
        return 300 * (level);
    }

    // Calculates and returns points when removing four rows based on the player's current level
    private int calculatePoints4Rows(int level) {
        return 1200 * (level);
    }

    // Debug board in the console
    private void printBoard() {
        for (int y = gameBoard.length-1; y >= 0; y--) {
            System.out.println(Arrays.toString(gameBoard[y]));
        }
        System.out.println("");
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        iPieceTex.dispose();
        zPieceTex.dispose();
        sPieceTex.dispose();
        lPieceTex.dispose();
        jPieceTex.dispose();
        squarePieceTex.dispose();
        tPieceTex.dispose();

        background.dispose();

        iPieceGhostTex.dispose();
        zPieceGhostTex.dispose();
        sPieceGhostTex.dispose();
        lPieceGhostTex.dispose();
        jPieceGhostTex.dispose();
        squarePieceGhostTex.dispose();
        tPieceGhostTex.dispose();

        for (Texture num : numbers) {
            num.dispose();
        }
    }
}
