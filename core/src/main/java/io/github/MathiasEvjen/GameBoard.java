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

    private Texture testBackground;
    private Texture testSqaure;
    private Sprite testSprite;

    private Sprite[] testSprites;
    private Array<Sprite> landedPieces;

    private Pieces pieces;
    private float moveTimer;

    boolean pieceFalling;
    int pieceRotation;

    public GameBoard(final Main game) {
        this.game = game;

        testBackground = new Texture("testBackground.png");
        testSqaure = new Texture("testSquare.png");

        testSprite = new Sprite(testSqaure);
        testSprite.setSize(1, 1);

        testSprites = new Sprite[4];
        pieces = new Pieces();


        landedPieces = new Array<>();

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
        if (!pieceFalling) {
            int squares = 0;
            pieceRotation = 0;
            for (int j1 = 20, j2 = 0; j1 > 16; j1--, j2++) {
                for (int i1 = 2, i2 = 0; i1 < 6; i1++, i2++) {
                    if (pieces.getSquarePiece().getPiece()[j2][i2] != 0) {
                        testSprites[squares] = new Sprite(testSqaure);
                        testSprites[squares].setSize(1, 1);
                        testSprites[squares].setX(i1);
                        testSprites[squares].setY(j1);
                        board[j1][i1] = "FILLED";
//                        System.out.println(i1);
//                        System.out.println(j1 + "\n");
                        testSprites[squares].draw(game.batch);
                        squares++;
                    }
                }
            }
//            System.out.println(Arrays.toString(testSprites));
            pieceFalling = true;
        } else {
            for (Sprite sprite : testSprites) {
                sprite.draw(game.batch);
            }
        }

        for (Sprite piece : landedPieces) {
            piece.draw(game.batch);
        }

        game.batch.end();
    }
    public void input() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {


            boolean pieceAtBottom = false;
            int lowestTile = (int)testSprites[0].getY();
            for (int i = 1; i < testSprites.length; i++)  {
                if ((int)testSprites[i].getY() < lowestTile) lowestTile = (int)testSprites[i].getY();
            }

            for (Sprite sprite : testSprites) {
//                System.out.println(lowestTile);
                if (lowestTile != 0 && board[lowestTile-1][(int)sprite.getX()].equals("FILLED")) {
                    pieceAtBottom = true;
                    break;
                } else if (sprite.getY() == 0) {
                    pieceAtBottom = true;
                    break;
                }
            }
            if (pieceAtBottom) {
                for (int i = 0; i < testSprites.length; i++) {
                    landedPieces.add(testSprites[i]);
                    testSprites[i] = null;
                    pieceFalling = false;
                }
            } else {
                for (int i = testSprites.length - 1; i >= 0; i--) {
                    board[(int)testSprites[i].getY()][(int)testSprites[i].getX()] = "EMPTY";
                    testSprites[i].translateY(-1);
                    board[(int)testSprites[i].getY()][(int)testSprites[i].getX()] = "FILLED";
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (pieceRotation == 3) pieceRotation = 0;
            else pieceRotation++;
//            for (int j1 = )
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            int furthestLeft = (int)testSprites[0].getX();
            for (int i = 1; i < testSprites.length; i++) {
                if ((int)testSprites[i].getX() < furthestLeft) furthestLeft = (int)testSprites[i].getX();
            }
            for (Sprite sprite : testSprites) {
                if (sprite.getX() == 0 || board[(int)sprite.getY()][furthestLeft - 1].equals("FILLED")) return;
            }
            for (Sprite sprite : testSprites) {
                board[(int)sprite.getY()][(int)sprite.getX()] = "EMPTY";
                sprite.translateX(-1);
                board[(int)sprite.getY()][(int)sprite.getX()] = "FILLED";
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            int furthestRight = 0;
            for (Sprite sprite : testSprites) {
                if ((int)sprite.getX() > furthestRight) furthestRight = (int)sprite.getX();
            }
            for (Sprite sprite : testSprites) {
                if (sprite.getX() == 9 || board[(int)sprite.getY()][furthestRight + 1].equals("FILLED")) return;

            }
            for (Sprite sprite : testSprites) {
                board[(int)sprite.getY()][(int)sprite.getX()] = "EMPTY";
                sprite.translateX(1);
                board[(int)sprite.getY()][(int)sprite.getX()] = "FILLED";
            }
        }
    }

    public void logic() {
        float boardWidth = game.viewport.getWorldWidth();
        float boardHeight = game.viewport.getWorldHeight();

        testSprite.setX(MathUtils.clamp(testSprite.getX(), 0, boardWidth - 1));
        testSprite.setY(MathUtils.clamp(testSprite.getY(), 0, boardHeight - 1));

        float dt = Gdx.graphics.getDeltaTime();
        moveTimer += dt;

        if (pieceFalling) {
            boolean pieceAtBottom = false;
            int lowestTile = (int)testSprites[0].getY();
            for (int i = 1; i < testSprites.length; i++)  {
                if ((int)testSprites[i].getY() < lowestTile) lowestTile = (int)testSprites[i].getY();
            }

            for (Sprite sprite : testSprites) {
//                System.out.println(lowestTile);
                if (lowestTile != 0 && board[lowestTile-1][(int)sprite.getX()].equals("FILLED")) {
                    pieceAtBottom = true;
                    break;
                } else if (sprite.getY() == 0) {
                    pieceAtBottom = true;
                    break;
                }
            }
            if (pieceAtBottom) {
                if ( moveTimer > 1f) {
                    for (int i = 0; i < testSprites.length; i++) {
                        landedPieces.add(testSprites[i]);
                        testSprites[i] = null;
                        pieceFalling = false;
                    }
                }
            }
        }

        //Printing which tiles are filled or empty (upside down)
//        for (String[] rad : board) {
//            System.out.println(Arrays.toString(rad));
//        }
//        System.out.println();


        // If there is a piece falling it will move downwards
        if (pieceFalling) {
            if (moveTimer > 1f) {
                for (int i = testSprites.length - 1; i >= 0; i--) {
                    board[(int)testSprites[i].getY()][(int)testSprites[i].getX()] = "EMPTY";
//                    System.out.println(sprite.getY());
                    testSprites[i].translateY(-1);
                    board[(int)testSprites[i].getY()][(int)testSprites[i].getX()] = "FILLED";
//                    System.out.println(sprite.getY());
                    moveTimer = 0;
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

    }
}
