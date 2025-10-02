package io.github.MathiasEvjen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputHandler {
    private GameLogic gameLogic;

    public InputHandler(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) gameLogic.dropFallingPiece();
        if (Gdx.input.isKeyJustPressed((Input.Keys.UP))) gameLogic.rotateClockwise();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) gameLogic.handleHoldPieceInput();
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) gameLogic.handleMovePieceDownInput();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) gameLogic.handleMovePieceLeft();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) gameLogic.handleMovePieceRight();
    }
}
