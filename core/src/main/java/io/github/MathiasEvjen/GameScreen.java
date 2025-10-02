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
    final InputHandler inputHandler;
    final GameRenderer gameRenderer;

    private final char[][] gameBoard;

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
    private Texture[] numbers;
    private Array<Sprite> scoreDigits;


    public GameScreen(final Main game) {
        this.game = game;
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

        gameBoard = new char[20][10];
        for (char[] tile : gameBoard) {
            Arrays.fill(tile, 'O');
        }

//        score = 0;
//        level = 1;
//        completedRows = 0;
//
//        highestTile = 0;
//
//        rowsToRemove = new Array<>();
//        remove = false;
//        removeX = 0;
//        removeSpeedSeconds = .01f;
//        removedRow = false;

        gameLogic = new GameLogic(gameBoard, this.game);
        inputHandler = new InputHandler(this.gameLogic);
        gameRenderer = new GameRenderer(this.game, gameLogic);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        float dt = Gdx.graphics.getDeltaTime();
        inputHandler.handleInput();
        gameLogic.update(dt);
        gameRenderer.draw();
    }

    public void logic() {

        // TODO: Score is a disaster and needs work
//        for (int i = 0; i < scoreDigits.size; i++) {
//            scoreDigits.removeIndex(i);
//        }
//
//        if (score == 0) {
//            scoreDigits.add(new Sprite(numbers[0]));
//            scoreDigits.get(0).setSize(2, 2);
//            scoreDigits.get(0).setX(14);
//            scoreDigits.get(0).setY(22);
//        }
//
//        int currentScore = score;
//        Array<Integer> tmp = new Array<>();
//        int scoreDigitCounter = 0;
//        if (scoreDigits.size != 0) scoreDigits.removeIndex(0);
//        while (currentScore != 0) {
////            System.out.println();
//            scoreDigits.add(new Sprite(numbers[currentScore % 10]));
//            scoreDigits.get(scoreDigitCounter).setSize(2, 2);
//
//            tmp.add(currentScore % 10);
////            scoreDigits.get(scoreDigitCounter).setX(14 - (2 * scoreDigitCounter));
////            scoreDigits.get(scoreDigitCounter).setY(22);
////            System.out.print(currentScore % 10 + " a");
//
//            currentScore = currentScore / 10;
//
//            scoreDigitCounter++;
//        }


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
        gameRenderer.dispose();

        for (Texture num : numbers) {
            num.dispose();
        }
    }
}
