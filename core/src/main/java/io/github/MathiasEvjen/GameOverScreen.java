package io.github.MathiasEvjen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen implements Screen {
    final Main game;

    private Texture background;
    private Texture iTex;
    private Texture zTex;
    private Texture sTex;
    private Texture lTex;
    private Texture jTex;
    private Texture squareTex;
    private Texture tTex;

    private Sprite[] tileTex;

    private Array<Sprite> landedTiles;

    public GameOverScreen(final Main game, Array<Sprite> landedTiles, int score) {
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

        this.landedTiles = landedTiles;
        System.out.println(score);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLUE);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(background, 0, 0, worldWidth, worldHeight);

        for (Sprite piece : landedTiles) {
            piece.draw(game.batch);
        }

        game.batch.end();
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
