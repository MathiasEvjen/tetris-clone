/*
    TODO
    Finne ut av hvorfor det crasher når man fyller et tomrom på en firkant
    Legge inn riktig farge til de forskellige typene brikker
*/

package io.github.MathiasEvjen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.MathiasEvjen.pieces.*;

import java.util.Arrays;

public class GameBoard implements Screen {
    final Main game;

    private final String[][] board;

    private Texture testBackground;
    private Texture testSqaure;
    private Sprite testSprite;

    private Sprite[] fallingPiece;
    private Array<Sprite> landedTiles;

    private float moveTimer;
    private float moveSpeed;

    private boolean pieceIsFalling;
    private int pieceRotation;
    private boolean pieceLanded;

    private int[] pivot;
    private int currentPiece;


    public GameBoard(final Main game) {
        this.game = game;

        testBackground = new Texture("testBackground.png");
        testSqaure = new Texture("testSquare.png");

        testSprite = new Sprite(testSqaure);
        testSprite.setSize(1, 1);

        fallingPiece = new Sprite[4];

        moveSpeed = .125f;

        pivot = new int[2];

        landedTiles = new Array<>();

        board = new String[20][10];
        for (String[] tile : board) {
            Arrays.fill(tile, "EMPTY");
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        float dt = Gdx.graphics.getDeltaTime();
        moveTimer += dt;
        input();
        logic();
        draw();
    }

    public void draw() {
        ScreenUtils.clear(Color.BLUE);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(testBackground, 0, 0, worldWidth, worldHeight);

        // Creates a new piece at the top if there is no current piece falling
        if (!pieceIsFalling) {
            int randomPiece = MathUtils.random(0, 6);
            currentPiece = randomPiece;
            int[][] piece = Pieces.getPiece(randomPiece, pieceRotation);

            int tiles = 0;
            pieceRotation = 0;

            for (int y1 = 20, y2 = 0; y1 > 16; y1--, y2++) {
                for (int x1 = 3, x2 = 0; x1 < 8; x1++, x2++) {
                    if (piece[y2][x2] != 0) {
                        fallingPiece[tiles] = new Sprite(testSqaure);
                        fallingPiece[tiles].setSize(1, 1);
                        fallingPiece[tiles].setX(x1);
                        fallingPiece[tiles].setY(y1);
                        board[y1][x1] = "FALLING";
                        fallingPiece[tiles].draw(game.batch);
                        tiles++;
                    }
                    if (piece[y2][x2] == 2) {
                        pivot[0] = x1;
                        pivot[1] = y1;
                    }
                }
            }
            pieceIsFalling = true;

        } else {
            for (Sprite sprite : fallingPiece) {
                sprite.draw(game.batch);
            }
        }

        for (Sprite piece : landedTiles) {
            piece.draw(game.batch);
        }

        game.batch.end();
    }


    public void input() {
        // Move piece to the bottom
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Find the lowest point in the piece
            int lowestTileY = (int)fallingPiece[0].getY();
            for (int i = 1; i < fallingPiece.length; i++)  {
                if ((int)fallingPiece[i].getY() < lowestTileY) {
                    lowestTileY = (int)fallingPiece[i].getY();
                }
            }

            int distanceToBottom = 0;

            for (int x = lowestTileY; x >= 0; x--) {
                for (Sprite sprite : fallingPiece) {
                    if (board[(int)sprite.getY()-distanceToBottom][(int) sprite.getX()].equals("FILLED")) {
                        distanceToBottom--;
                        pieceLanded = true;
                        break;
                    }
                }
                if (x == 0) {
                    pieceLanded = true;
                    break;
                }
                if (pieceLanded) break;

                distanceToBottom++;
            }

            movePieceVertically(-distanceToBottom);
            landPiece();
        }

        // Move piece down continually
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//            if (moveTimer > moveSpeed) {
//                movePieceDown();
//                moveTimer = 0;
//            }
//        }

        // Move piece down incrementally
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            movePieceDown();
        }

        // Rotate piece
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (pieceIsFalling) {
                // Updates the currently falling piece's rotation
                if (pieceRotation == 3) pieceRotation = 0;
                else pieceRotation++;

                int[][] piece = Pieces.getPiece(currentPiece, pieceRotation);

                int tile = 0;    // Counts the number of
                int[] newRotation = new int[8]; // Holds the coordinates from the next rotation
                int newRotationCounter = 0;    // Counter for the index of newRotation



                for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                    for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                        if (piece[y2][x2] != 0) {
                            if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;    // Checks if the new position is taken or out of bounds

                            // Saves the new coordinates to an array so that no pieces are moved preemptively
                            newRotation[newRotationCounter++] = x1;
                            newRotation[newRotationCounter++] = y1;
                            tile++; // Increments the number of tiles counted
                        }

                        // Updates the pivot point
                        if (piece[y2][x2] == 2) {
                            pivot[0] = x1;
                            pivot[1] = y1;
                        }
                    }
                }

                // Goes through the tiles of the falling piece and the newRotation coordinates and updates the tiles of the falling piece to the new coordinates
                for (tile = 0, newRotationCounter = 0; tile < fallingPiece.length; tile++, newRotationCounter += 2) {
                    board[(int)fallingPiece[tile].getY()][(int)fallingPiece[tile].getX()] = "EMPTY";    // Sets the old position on the board as empty

                    // Sets the current tile to new coordinates
                    fallingPiece[tile].setX(newRotation[newRotationCounter]);
                    fallingPiece[tile].setY(newRotation[newRotationCounter + 1]);

                    board[(int)fallingPiece[tile].getY()][(int)fallingPiece[tile].getX()] = "FALLING";  // Sets the new positions on the board as falling
                }
            }
        }

        // Move piece left continaully
//        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            if (moveTimer > moveSpeed) {
//                movePieceLeft();
//                moveTimer = 0;
//            }
//        }

        // Move left incrementally
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            movePieceLeft();
        }

        // Move piece right continually
//        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            if (moveTimer > moveSpeed) {
//                movePieceRight();
//                moveTimer = 0;
//            }
//        }

        // Move piece right incrementally
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            movePieceRight();
        }
    }

    public void logic() {
        float boardWidth = game.viewport.getWorldWidth();
        float boardHeight = game.viewport.getWorldHeight();

        testSprite.setX(MathUtils.clamp(testSprite.getX(), 0, boardWidth - 1));
        testSprite.setY(MathUtils.clamp(testSprite.getY(), 0, boardHeight - 1));


        // If there is a piece falling, a check on whether it cannot move further down is done
        if (pieceIsFalling) {
            pieceLanded = false;
            int lowestTile = (int)fallingPiece[0].getY();
            for (int i = 2; i < fallingPiece.length; i++)  {
                if ((int)fallingPiece[i].getY() < lowestTile) {
                    lowestTile = (int)fallingPiece[i].getY();
                }
            }

            for (Sprite sprite : fallingPiece) {
//                System.out.println(lowestTile);
                if ((sprite.getY() != 0 && board[(int)sprite.getY()-1][(int)sprite.getX()].equals("FILLED")) || sprite.getY() == 0) {
                    pieceLanded = true;
                    break;
                }
            }

            // If the piece cannot move down it will be stored in the landedTiles array
            // The current falling piece array is reset to null
            // Piece falling is set to false and a new piece will be created
            if (pieceLanded) {
                if (moveTimer > 1f) {
                    landPiece();
                }
            }
        }

        // If there is a piece falling it will move downwards
        if (pieceIsFalling) {
            if (moveTimer > 1f) {
                movePieceVertically(-1);
                pivot[1]--;
                moveTimer = 0;
            }
        }

        //Printing which tiles are filled or empty (upside down)
//        for (int i = board.length - 1; i >= 0; i--) {
//            System.out.println(Arrays.toString(board[i]));
//        }
//        System.out.println();
//
        removeCompletedRows();
    }

    public void movePieceVertically(int distance) {
        for (int i = fallingPiece.length-1; i >= 0; i--) {   // Must be a reverse loop!!
            board[(int)fallingPiece[i].getY()][(int)fallingPiece[i].getX()] = "EMPTY";
            fallingPiece[i].translateY(distance);
            board[(int)fallingPiece[i].getY()][(int)fallingPiece[i].getX()] = "FALLING";
        }
    }

    public void moveLandedTileVertically(int y) {
        for (Sprite tile : landedTiles) {
            if (tile.getY() == y) {
                board[(int)tile.getY()][(int)tile.getX()] = "EMPTY";
                tile.translateY(-1);
                board[(int)tile.getY()][(int)tile.getX()] = "FILLED";
            }
        }
    }

    public void movePieceDown() {
        pieceLanded = false;
        int lowestTile = (int) fallingPiece[0].getY();
        for (int i = 1; i < fallingPiece.length; i++) {
            if ((int) fallingPiece[i].getY() < lowestTile) lowestTile = (int) fallingPiece[i].getY();
        }

        for (Sprite sprite : fallingPiece) {
//                System.out.println(lowestTile);
            if ((sprite.getY() != 0 && board[(int)sprite.getY()-1][(int)sprite.getX()].equals("FILLED")) || sprite.getY() == 0) {
                pieceLanded = true;
                break;
            }
        }

        if (pieceLanded) {
            pieceIsFalling = false;
            landPiece();
        } else {
            movePieceVertically(-1);
        }
        pivot[1]--;
    }

    public void movePieceLeft() {
        int furthestLeftX = (int) fallingPiece[0].getX();
        int furthestLeftY = (int) fallingPiece[0].getY();
        for (int i = 1; i < fallingPiece.length; i++) {
            if ((int) fallingPiece[i].getX() < furthestLeftX) {
                furthestLeftX = (int) fallingPiece[i].getX();
                furthestLeftY = (int) fallingPiece[i].getY();
            }
        }

        if (furthestLeftX == 0 || board[furthestLeftY][furthestLeftX - 1].equals("FILLED")) return;

        movePieceLaterally(-1);
        pivot[0]--;
    }

    public void movePieceRight() {
        int furthestRightX = 0;
        int furthestRighty = 0;
        for (Sprite sprite : fallingPiece) {
            if ((int) sprite.getX() > furthestRightX) {
                furthestRightX = (int) sprite.getX();
                furthestRighty = (int) sprite.getY();
            }
        }

        if (furthestRightX == 9 || board[furthestRighty][furthestRightX + 1].equals("FILLED")) return;

        movePieceLaterally(1);
        pivot[0]++;
    }

    public void movePieceLaterally(int distance) {
        for (Sprite sprite : fallingPiece) {
            board[(int)sprite.getY()][(int)sprite.getX()] = "EMPTY";
            sprite.translateX(distance);
            board[(int)sprite.getY()][(int)sprite.getX()] = "FALLING";
        }
    }

    public void landPiece() {
        for (int i = 0; i < fallingPiece.length; i++) {
            landedTiles.add(fallingPiece[i]);
            board[(int)fallingPiece[i].getY()][(int)fallingPiece[i].getX()] = "FILLED";
            fallingPiece[i] = null;
            pieceIsFalling = false;
            pieceLanded = false;
            pieceRotation = 0;
        }
    }

    // Deletes the line at the given y-position
    public void removeRow(int y) {
        // Loops through all x-positions in the line
        for (int x = 0; x < 10; x++) {
            // Loops through all the landed tiles on the board
            for (Sprite tile : landedTiles) {
                // When a landed tile on that position is found it is deleted
                if (tile.getY() == y && tile.getX() == x) {
                    landedTiles.removeIndex(landedTiles.indexOf(tile, true)); // VIKTIG: Kan være grunnen til feilmedlding. Om nødvendig prøv false
                }
            }
            // Sets the removed slot on the board to empty
            board[y][x] = "EMPTY";
        }
    }

    // Goes through the whole board and removes the lines that are full
    public void removeCompletedRows() {
        boolean removedRow = false;
        int filledTiles = 0;    // Holds the number of tiles filled in the row
        Array<Integer> filledRows = new Array<>();

        // Goes through all the rows of the board and checks if they are full
        for (int y1 = 0; y1 < board.length; y1++) {
            // Checks all the tiles in the row if they are full
            for (int x = 0; x < board[y1].length; x++) {
                if (board[y1][x].equals("FILLED")) filledTiles++;    // Updates filledTiles counter if tile is filled
            }

            // If all the tiles are filled, the row is removed
            if (filledTiles == board[y1].length) {
                removeRow(y1);
                y1--;    // Decrements y1 so the current line is checked again when all the tiles are moved one down
                removedRow = true;
            }

            filledTiles = 0;    // Resets the number of filled tiles
            if (removedRow) {
                for (int y2 = y1 + 1; y2 < board.length; y2++) {
                    moveLandedTileVertically(y2);
                }
            }
        }
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
        testSqaure.dispose();
        testBackground.dispose();
    }
}
