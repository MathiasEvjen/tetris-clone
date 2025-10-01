package io.github.MathiasEvjen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputHandler {
    private GameLogic gameLogic;

    public InputHandler(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            gameLogic.dropFallingPiece();
        }
        if (Gdx.input.isKeyJustPressed((Input.Keys.UP))) {
            gameLogic.rotateClockwise();
        }
    }

}
