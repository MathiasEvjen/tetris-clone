/*
    TODO
    Fikse rotasjon ved vegg og tak for S og Z
    Legge til mulighet for å holde brikker
    Legge til mulighet til å se neste brikke som kommer
    Legge til poengsystem
        Legge til flere poeng for flere linjer fjernet av gangen
    Legge til speedup ved et visst antall poeng
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

import java.util.Arrays;

public class GameBoard implements Screen {
    final Main game;

    private final String[][] board;

    private int startY;
    private int startX;
    private int stopX;

    private final Texture background;
    private final Texture iTex;
    private final Texture zTex;
    private final Texture sTex;
    private final Texture lTex;
    private final Texture jTex;
    private final Texture squareTex;
    private final Texture tTex;

    private Sprite[] tileTex;

    private Sprite[] fallingPiece;
    private Array<Sprite> landedTiles;

    private float moveTimer;
    private float moveDownTimer;
    private float moveDownSpeed;
    private float moveSpeed;

    private boolean pieceIsFalling;
    private int pieceRotation;
    private boolean pieceLanded;

    private int[] pivot;
    private int currentPiece;


    public GameBoard(final Main game) {
        this.game = game;

        background = new Texture("background.png");
        iTex = new Texture("ITile.png");
        zTex = new Texture("ZTile.png");
        sTex = new Texture("STile.png");
        lTex = new Texture("LTile.png");
        jTex = new Texture("JTile.png");
        squareTex = new Texture("SquareTile.png");
        tTex = new Texture("TTile.png");

        tileTex = new Sprite[7];
        tileTex[0] = new Sprite(iTex);
        tileTex[1] = new Sprite(zTex);
        tileTex[2] = new Sprite(sTex);
        tileTex[3] = new Sprite(lTex);
        tileTex[4] = new Sprite(jTex);
        tileTex[5] = new Sprite(squareTex);
        tileTex[6] = new Sprite(tTex);


        fallingPiece = new Sprite[4];

        moveSpeed = .125f;
        moveDownSpeed = .5f;   // Defines the dropspeed of the pieces

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
        moveDownTimer += dt;
        input();
        logic();
        draw();
    }

    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(background, 0, 0, worldWidth, worldHeight);

        // Creates a new piece at the top if there is no current piece falling
        if (!pieceIsFalling) {
            int randomPiece =  MathUtils.random(0, 6);
//            System.out.println("Piece no.: " + randomPiece);
            currentPiece = randomPiece;
            int[][] piece = Pieces.getPiece(randomPiece, pieceRotation);

            switch (randomPiece) {
                case 0:
                    startX = 2;
                    stopX = 7;
                    startY = 19;
                    break;
                case 1:
                case 2:
                    startX = 3;
                    stopX = 8;
                    startY = 21;
                    break;
                default:
                    startX = 2;
                    stopX = 7;
                    startY = 20;
                    break;
            }

            int tiles = 0;
            pieceRotation = 0;

            moveDownTimer = 0;

            for (int y1 = startY, y2 = 0; y1 > 16; y1--, y2++) {
                for (int x1 = startX, x2 = 0; x1 < stopX; x1++, x2++) {
                    if (piece[y2][x2] != 0 && piece[y2][x2] != 3) {
                        fallingPiece[tiles] = new Sprite(tileTex[randomPiece]);
                        fallingPiece[tiles].setSize(1, 1);
                        fallingPiece[tiles].setX(x1);
                        fallingPiece[tiles].setY(y1);
//                        System.out.println("[" + x1 + ", " + y1 + "]");
                        board[y1][x1] = "FALLING";
                        fallingPiece[tiles].draw(game.batch);
                        tiles++;
                    }
                    if (piece[y2][x2] == 2 || piece[y2][x2] == 3) {
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
//            System.out.println();
//            String coords = "";
//            for (Sprite tile : fallingPiece) {
//                coords += "[";
//                coords += tile.getX();
//                coords += ", ";
//                coords += tile.getY();
//                coords += "], ";
//            }
//            System.out.println(coords + "\n");
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

            for (int y = lowestTileY; y >= 0; y--) {
                for (Sprite sprite : fallingPiece) {
                    if (board[(int)sprite.getY()-distanceToBottom][(int) sprite.getX()].equals("FILLED")) {
                        distanceToBottom--;
                        pieceLanded = true;
                        break;
                    }
                }
                if (y == 0) {
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
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (moveTimer > moveSpeed) {
                movePieceDown();
                moveTimer = 0;
            }
        }

        // Rotate piece
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (pieceIsFalling) {
                if (pieceLanded) moveDownTimer = 0;


                boolean iPieceAtWallLeft = false;
                boolean iPieceAtWallRight = false;

                boolean zPieceAtWallLeft = false;
                boolean zPieceAtWallRight = false;
                boolean zPieceAtCeiling = false;
                boolean zPieceAtFloor = false;

                boolean sPieceAtWallLeft = false;
                boolean sPieceAtWallRight = false;
                boolean sPieceAtCeiling = false;
                boolean sPieceAtFloor = false;

                boolean lPieceAtWallLeft = false;
                boolean lPieceAtWallRight = false;
                boolean lPieceAtFloor = false;

                boolean jPieceAtWallLeft = false;
                boolean jPieceAtWallRight = false;
                boolean jPieceAtFloor = false;

                boolean tPieceAtWallLeft = false;
                boolean tPieceAtWallRight = false;
                boolean tPieceAtFloor = false;


                // Checks if an I-Piece is at a left or right wall
                if (currentPiece == 0) {
                    int iPieceAtWallLeftCounter = 0;
                    int iPieceAtWallRightCounter = 0;
                    for (Sprite tile : fallingPiece) {
                        if (tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX()-1].equals("FILLED")) iPieceAtWallLeftCounter++;
                        if (tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX()+1].equals("FILLED")) iPieceAtWallRightCounter++;
                    }
                    if (iPieceAtWallLeftCounter >= 2) iPieceAtWallLeft = true;
                    if (iPieceAtWallRightCounter >= 2) iPieceAtWallRight = true;
                }

                // Checks if a Z-Piece is at a wall, ceiling or floor
                if (currentPiece == 1) {
                    int zPieceAtWallLeftCounter = 0;
                    int zPieceAtWallRightCounter = 0;
                    int zPieceAtCeilingCounter = 0;
                    int zPieceAtFloorCounter = 0;
                    for (Sprite tile : fallingPiece) {
                        if (tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX()-1].equals("FILLED")) zPieceAtWallLeftCounter++;
                        if (tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX()+1].equals("FILLED")) zPieceAtWallRightCounter++;
                        if (tile.getY() == 19 || board[(int)tile.getY()+1][(int)tile.getX()].equals("FILLED")) zPieceAtCeilingCounter++;
                        if (tile.getY() == 0 || board[(int)tile.getY()-1][(int)tile.getX()].equals("FILLED")) zPieceAtFloorCounter++;
                    }
                    if (zPieceAtWallLeftCounter >= 1) zPieceAtWallLeft = true;
                    if (zPieceAtWallRightCounter >= 1) zPieceAtWallRight = true;
                    if (zPieceAtCeilingCounter == 2) zPieceAtCeiling = true;
                    if (zPieceAtFloorCounter == 2) zPieceAtFloor = true;
                }

                // Checks if an S-Piece is at a wall, ceiling or floor
                if (currentPiece == 2) {
                    int sPieceAtWallLeftCounter = 0;
                    int sPieceAtWallRightCounter = 0;
                    int sPieceAtCeilingCounter = 0;
                    int sPieceAtFloorCounter = 0;
                    for (Sprite tile : fallingPiece) {
                        if (tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX()-1].equals("FILLED")) sPieceAtWallLeftCounter++;
                        if (tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX()+1].equals("FILLED")) sPieceAtWallRightCounter++;
                        if (tile.getY() == 19 || board[(int)tile.getY()+1][(int)tile.getX()].equals("FILLED")) sPieceAtCeilingCounter++;
                        if (tile.getY() == 0 || board[(int)tile.getY()-1][(int)tile.getX()].equals("FILLED")) sPieceAtFloorCounter++;
                    }
                    if (sPieceAtWallLeftCounter >= 1) sPieceAtWallLeft = true;
                    if (sPieceAtWallRightCounter >= 1) sPieceAtWallRight = true;
                    if (sPieceAtCeilingCounter == 2) sPieceAtCeiling = true;
                    if (sPieceAtFloorCounter == 2) sPieceAtFloor = true;
                }

                // Checks if an L-Piece is at a wall
                if (currentPiece == 3) {
                    int lPieceAtWallLeftCounter = 0;
                    int lPieceAtWallRightCounter = 0;
                    int lPieceAtFloorCounter = 0;
                    for (Sprite tile : fallingPiece) {
                        if (tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX()-1].equals("FILLED")) lPieceAtWallLeftCounter++;
                        if (tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX()+1].equals("FILLED")) lPieceAtWallRightCounter++;
                        if (tile.getY() == 0 || board[(int)tile.getY()-1][(int)tile.getX()].equals("FILLED")) lPieceAtFloorCounter++;
                    }
                    if (lPieceAtWallLeftCounter >= 2) lPieceAtWallLeft = true;
                    if (lPieceAtWallRightCounter >= 2) lPieceAtWallRight = true;
                    if (lPieceAtFloorCounter >= 2) lPieceAtFloor = true;
                }

                // Checks if a J-Piece is at a wall
                if (currentPiece == 4) {
                    int jPieceAtWallLeftCounter = 0;
                    int jPieceAtWallRightCounter = 0;
                    int jPieceAtFloorCounter = 0;
                    for (Sprite tile : fallingPiece) {
                        if (tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX()-1].equals("FILLED")) jPieceAtWallLeftCounter++;
                        if (tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX()+1].equals("FILLED")) jPieceAtWallRightCounter++;
                        if (tile.getY() == 0 || board[(int)tile.getY()-1][(int)tile.getX()].equals("FILLED")) jPieceAtFloorCounter++;
                    }
                    if (jPieceAtWallLeftCounter >= 2) jPieceAtWallLeft = true;
                    if (jPieceAtWallRightCounter >= 2) jPieceAtWallRight = true;
                    if (jPieceAtFloorCounter >= 2) jPieceAtFloor = true;
                }

                // Checks if a T-Piece is at a wall
                if (currentPiece == 6) {
                    int tPieceAtWallLeftCounter = 0;
                    int tPieceAtWallRightCounter = 0;
                    int tPieceAtFloorCounter = 0;
                    for (Sprite tile : fallingPiece) {
                        if (tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX()-1].equals("FILLED")) tPieceAtWallLeftCounter++;
                        if (tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX()+1].equals("FILLED")) tPieceAtWallRightCounter++;
                        if (tile.getY() == 0 || board[(int)tile.getY()-1][(int)tile.getX()].equals("FILLED")) tPieceAtFloorCounter++;
                    }
                    if (tPieceAtWallLeftCounter >= 2) tPieceAtWallLeft = true;
                    if (tPieceAtWallRightCounter >= 2) tPieceAtWallRight = true;
                    if (tPieceAtFloorCounter >= 2) tPieceAtFloor = true;
                }



                // If I-Piece is at a left wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (iPieceAtWallLeft) {
                    // If I-Piece is at rotation one and there is free space, the pivot coords are moved 2 tiles to the right
                    if (currentPiece == 0 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0], x2 = 0; x1 < pivot[0] + 5; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;    // Returns if there is not enough room to rotate
                            }
                        }
                        pivot[0] += 2;
                    }

                    // If I-Piece is at rotation three and there is free space, the pivot coords are moved 1 tile to the right
                    if (currentPiece == 0 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 1, x2 = 0; x1 < pivot[0] + 4; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        pivot[0]++;
                    }
                }

                // If I-Piece is at a right wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (iPieceAtWallRight) {
                    // If I-Piece is at rotation 1 and there is free space, the pivot coords are moved 1 til to the left
                    if (currentPiece == 0 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 3, x2 = 0; x1 < pivot[0] + 2; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]--;
                    }

                    // If I-Piece is at rotation 3 and there is free space, the pivot coords are moved 2 tiles to the left
                    if (currentPiece == 0 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 4, x2 = 0; x1 < pivot[0] + 1; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0] -= 2;
                    }
                }

                // If Z-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (zPieceAtWallLeft) {
                    // If Z-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the right
                    if (currentPiece == 1 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 1, x2 = 0; x1 < pivot[0] + 4; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]++;
                    }
                }

                // If Z-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (zPieceAtWallRight) {
                    // If Z-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the left
                    if (currentPiece == 1 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 3, x2 = 0; x1 < pivot[0] + 2; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]--;
                    }
                }

                // If Z-Piece is at the ceiling and there is free space, the pivot coords are moved down from the ceiling so it can rotate
                if (zPieceAtCeiling) {
                    if (currentPiece == 1 && pieceRotation == 0) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 1);
                        for (int y1 = pivot[1] + 1, y2 = 0; y1 > pivot[1] - 4; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]--;
                    }
                }

                // If Z-Piece is at the floor and there is free sace, the pivot coords are moved up from the floor so it can rotate
                if (zPieceAtFloor) {
                    if (currentPiece == 1 && pieceRotation == 2) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 3);
                        for (int y1 = pivot[1] + 3, y2 = 0; y1 > pivot[1] - 2; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]++;
                    }
                }

                // If S-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (sPieceAtWallLeft) {
                    // If S-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the right
                    if (currentPiece == 2 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 1, x2 = 0; x1 < pivot[0] + 4; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]++;
                    }
                }

                // If S-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (sPieceAtWallRight) {
                    // If S-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the left
                    if (currentPiece == 2 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 3, x2 = 0; x1 < pivot[0] + 2; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]--;
                    }
                }

                // If S-Piece is at the ceiling and there is free space, the pivot coords are moved down from the ceiling so it can rotate
                if (sPieceAtCeiling) {
                    if (currentPiece == 2 && pieceRotation == 0) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 1);
                        for (int y1 = pivot[1] + 1, y2 = 0; y1 > pivot[1] - 4; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]--;
                    }
                }

                // If S-Piece is at the floor and there is free space, the pivot coords are moved up from the floor so it can rotate
                if (sPieceAtFloor) {
                    if (currentPiece == 2 && pieceRotation == 2) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 3);
                        for (int y1 = pivot[1] + 3, y2 = 0; y1 > pivot[1] - 2; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]++;
                    }
                }

                // If L-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (lPieceAtWallLeft) {
                    // If L-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the right
                    if (currentPiece == 3 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 1, x2 = 0; x1 < pivot[0] + 4; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]++;
                    }
                }

                // If L-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (lPieceAtWallRight) {
                    // If L-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the left
                    if (currentPiece == 3 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 3, x2 = 0; x1 < pivot[0] + 2; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]--;
                    }
                }

                // If L-Piece is at the floor and there is free space, the pivot coords are moved up from the floor so it can rotate
                if (lPieceAtFloor) {
                    if (currentPiece == 3 && pieceRotation == 0) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 1);
                        for (int y1 = pivot[1] + 3, y2 = 0; y1 > pivot[1] - 2; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]++;
                    }
                }

                // If J-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (jPieceAtWallLeft) {
                    // If J-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the right
                    if (currentPiece == 4 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 1, x2 = 0; x1 < pivot[0] + 4; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]++;
                    }
                }

                // If J-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (jPieceAtWallRight) {
                    // If J-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the left
                    if (currentPiece == 4 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 3, x2 = 0; x1 < pivot[0] + 2; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]--;
                    }
                }

                // If J-Piece is at the floor and there is free space, the pivot coords are moved up from the floor so it can rotate
                if (jPieceAtFloor) {
                    if (currentPiece == 4 && pieceRotation == 0) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 1);
                        for (int y1 = pivot[1] + 3, y2 = 0; y1 > pivot[1] - 2; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]++;
                    }
                }

                // If T-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (tPieceAtWallLeft) {
                    // If T-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the right
                    if (currentPiece == 6 && pieceRotation == 1) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 2);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 1, x2 = 0; x1 < pivot[0] + 4; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]++;
                    }
                }

                // If T-Piece is at the wall and there is free space, the pivot coords are moved out from the wall so it can rotate
                if (tPieceAtWallRight) {
                    // If T-Piece is at rotation 1 and there is free space, the pivot coords are moved one tile to the left
                    if (currentPiece == 6 && pieceRotation == 3) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 0);
                        for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                            for (int x1 = pivot[0] - 3, x2 = 0; x1 < pivot[0] + 2; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3)
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                            }
                        }
                        pivot[0]--;
                    }
                }

                // If T-Piece is at the floor and there is free space, the pivot coords are moved up from the floor so it can rotate
                if (tPieceAtFloor) {
                    if (currentPiece == 6 && pieceRotation == 0) {
                        int[][] nextPiece = Pieces.getPiece(currentPiece, 1);
                        for (int y1 = pivot[1] + 3, y2 = 0; y1 > pivot[1] - 2; y1--, y2++) {
                            for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                                if (nextPiece[y2][x2] != 0 && nextPiece[y2][x2] != 3) {
                                    if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) return;
                                }
                            }
                        }
                        pivot[1]++;
                    }
                }



                // Updates the currently falling piece's rotation
                if (pieceRotation == 3) pieceRotation = 0;
                else pieceRotation++;

                int[][] piece = Pieces.getPiece(currentPiece, pieceRotation);

                int tile = 0;    // Counts the number of
                int[] newRotation = new int[8]; // Holds the coordinates from the next rotation
                int newRotationCounter = 0;    // Counter for the index of newRotation



                for (int y1 = pivot[1] + 2, y2 = 0; y1 > pivot[1] - 3; y1--, y2++) {
                    for (int x1 = pivot[0] - 2, x2 = 0; x1 < pivot[0] + 3; x1++, x2++) {
                        if (piece[y2][x2] != 0 && piece[y2][x2] != 3) {
                            if (x1 < 0 || x1 > 9 || y1 < 0 || y1 > 19 || board[y1][x1].equals("FILLED")) {
                                if (pieceRotation == 0) pieceRotation = 3;
                                else pieceRotation--;
                                return;    // Checks if the new position is taken or out of bounds
                            }

                            // Saves the new coordinates to an array so that no pieces are moved preemptively
                            newRotation[newRotationCounter++] = x1;
                            newRotation[newRotationCounter++] = y1;
                            tile++; // Increments the number of tiles counted
                        }
                    }
                }

                // Goes through the tiles of the falling piece and the newRotation coordinates and updates the tiles of the falling piece to the new coordinates
                for (tile = 0, newRotationCounter = 0; tile < fallingPiece.length; tile++, newRotationCounter += 2) {
                    for (Sprite fallingTile : fallingPiece) {
                        if (fallingTile.getX() == newRotation[newRotationCounter] && fallingTile.getY() == newRotation[newRotationCounter + 1]) {
                            board[(int)fallingPiece[tile].getY()][(int)fallingPiece[tile].getX()] = "FALLING";
                        } else {
                            board[(int)fallingPiece[tile].getY()][(int)fallingPiece[tile].getX()] = "EMPTY";    // Sets the old position on the board as empty
                        }
                    }


                    // Sets the current tile to new coordinates
                    fallingPiece[tile].setX(newRotation[newRotationCounter]);
                    fallingPiece[tile].setY(newRotation[newRotationCounter + 1]);

                    board[(int)fallingPiece[tile].getY()][(int)fallingPiece[tile].getX()] = "FALLING";  // Sets the new positions on the board as falling
                }
            }
        }

        // Move piece left continaully
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (moveTimer > moveSpeed) {
                movePieceLeft();
                moveTimer = 0;
            }
        }

        // Move piece right continually
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (moveTimer > moveSpeed) {
                movePieceRight();
                moveTimer = 0;
            }
        }
    }

    public void logic() {
        // If there is a piece falling, a check on whether it cannot move further down is done
        if (pieceIsFalling) {
            pieceLanded = false;

            // Checks the tiles to see if the tile directly under it is either filled or the bottom of the board
            // Sets the piece as landed if either criteria is met
            for (Sprite sprite : fallingPiece) {
                if ((sprite.getY() != 0 && board[(int)sprite.getY()-1][(int)sprite.getX()].equals("FILLED")) || sprite.getY() == 0) {
                    pieceLanded = true;
                    break;
                }
            }

            // If the piece cannot move down it will be stored in the landedTiles array
            if (pieceLanded) {
                if (moveDownTimer > moveDownSpeed) {
                    landPiece();    // Lands the current tile
                }
            }

        }

        // If there is a piece falling it will move downwards every second
        if (pieceIsFalling) {
            if (moveDownTimer > moveDownSpeed) {
                movePieceVertically(-1);
                pivot[1]--;
                moveDownTimer = 0;
            }
        }

        removeCompletedRows();  // Checks for filled rows and removes them

        for (Sprite tile : landedTiles) {
            if (tile.getY() == 19) {
                game.setScreen(new GameOverScreen(game, landedTiles));
            }
        }

        // Printing which tiles are filled or empty (upside down)
//        for (int i = board.length - 1; i >= 0; i--) {
//            System.out.println(Arrays.toString(board[i]));
//        }
//        System.out.println();

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
        for (Sprite tile : fallingPiece) {
            if (tile == null || tile.getX() == 0 || board[(int)tile.getY()][(int)tile.getX() - 1].equals("FILLED")) return;
        }

        movePieceLaterally(-1);
        pivot[0]--;
    }

    public void movePieceRight() {
        for (Sprite tile : fallingPiece) {
            // Checks if the tiles furthest right are at the right edge
            if (tile == null || tile.getX() == 9 || board[(int)tile.getY()][(int)tile.getX() + 1].equals("FILLED")) return;
        }

        movePieceLaterally(1);
        pivot[0]++;
    }

    // Moves the falling piece laterally
    // Takes in the distance to be moved
    public void movePieceLaterally(int distance) {
        for (Sprite sprite : fallingPiece) {
            board[(int)sprite.getY()][(int)sprite.getX()] = "EMPTY";
            sprite.translateX(distance);
            board[(int)sprite.getY()][(int)sprite.getX()] = "FALLING";
        }
    }

    // Lands the current piece
    public void landPiece() {
        // Iterates through all the tiles of the landing piece
        for (int i = 0; i < fallingPiece.length; i++) {
            landedTiles.add(fallingPiece[i]);   // Adds the tile to the array holding the landed tiles
            board[(int)fallingPiece[i].getY()][(int)fallingPiece[i].getX()] = "FILLED"; // Sets the coordinates of the landed tiles as filled
            fallingPiece[i] = null; // The current falling piece array is reset to null
            pieceIsFalling = false; // Piece falling is set to false and a new piece will be created
            pieceLanded = false;    // Sets falling piece as not landed so the new piece can fall
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

            removedRow = false;
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
        iTex.dispose();
        zTex.dispose();
        sTex.dispose();
        lTex.dispose();
        jTex.dispose();
        squareTex.dispose();
        tTex.dispose();
        background.dispose();
    }
}
