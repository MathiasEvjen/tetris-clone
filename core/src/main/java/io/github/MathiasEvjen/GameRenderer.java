package io.github.MathiasEvjen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameRenderer {
    final Main game;
    final GameLogic gameLogic;

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

    private Sprite[] tileTexSprites;
    private Sprite[] ghostTileTexSprites;

    private Sprite[] fallingPieceTextures;
    private Sprite[] nextPieceTextures;
    private Sprite[] heldPieceTextures;
    private Sprite[] ghostPieceTextures;
    private Array<Sprite> landedTilesTextures;

    public GameRenderer(Main game, GameLogic gameLogic) {
        this.game = game;
        this.gameLogic = gameLogic;

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
        tileTexSprites[5] = new Sprite(tPieceTex);
        tileTexSprites[6] = new Sprite(squarePieceTex);

        // Initiates the ghostTileTexSprites array and adds the ghost tile textures
        ghostTileTexSprites = new Sprite[7];
        ghostTileTexSprites[0] = new Sprite(iPieceGhostTex);
        ghostTileTexSprites[1] = new Sprite(zPieceGhostTex);
        ghostTileTexSprites[2] = new Sprite(sPieceGhostTex);
        ghostTileTexSprites[3] = new Sprite(lPieceGhostTex);
        ghostTileTexSprites[4] = new Sprite(jPieceGhostTex);
        ghostTileTexSprites[5] = new Sprite(tPieceGhostTex);
        ghostTileTexSprites[6] = new Sprite(squarePieceGhostTex);

        fallingPieceTextures = new Sprite[4];
        nextPieceTextures = new Sprite[4];
        heldPieceTextures = new Sprite[4];
        ghostPieceTextures = new Sprite[4];
        landedTilesTextures = new Array<>();
    }

    public void draw() {
        if (gameLogic.isCurrentPieceIsFalling()) setBoardTextures();

        setUiTileTextures();

        if (!gameLogic.getLandedTiles().isEmpty()) createLandedTilesTextures();

        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(background, 0, 0, worldWidth, worldHeight);

        if (gameLogic.isCurrentPieceIsFalling()) {
            drawPiece(fallingPieceTextures);
            drawPiece(ghostPieceTextures);
        }

        drawLandedTilesTextures();

        drawPiece(nextPieceTextures);
        if (gameLogic.getShouldDrawHeldPiece()) drawPiece(heldPieceTextures);

        game.batch.end();
    }

    private Sprite[] createPieceTextures(Piece pieceToDraw, int pieceId, boolean isGhost) {
        Sprite[] pieceTextures = new Sprite[4];
        for (int tile = 0; tile < pieceTextures.length; tile++) {
            pieceTextures[tile] = createTileSprite(pieceId, isGhost, pieceToDraw.getX(tile), pieceToDraw.getY(tile));
        }
        return pieceTextures;
    }

    private void createLandedTilesTextures() {
        landedTilesTextures.clear();
        for (PointF tile : gameLogic.getLandedTiles().keySet()) {
            Sprite newLandedTile = createTileSprite(gameLogic.getLandedTiles().get(tile), false, tile.x, tile.y);
            landedTilesTextures.add(newLandedTile);
        }
    }

    private Sprite createTileSprite(int pieceId, boolean isGhost, float x, float y) {
        Sprite tileSprite = new Sprite((isGhost ? ghostTileTexSprites[pieceId] : tileTexSprites[pieceId]));
        tileSprite.setSize(1, 1);
        tileSprite.setX(x);
        tileSprite.setY(y);
        return tileSprite;
    }

    private void setBoardTextures() {
        fallingPieceTextures = createPieceTextures(gameLogic.getFallingPiece(), gameLogic.getCurrentPieceID(), false);
        ghostPieceTextures = createPieceTextures(gameLogic.getGhostPiece(), gameLogic.getCurrentPieceID(), true);
    }

    private void setUiTileTextures() {
        nextPieceTextures= createPieceTextures(gameLogic.getNextPiece(), gameLogic.getNextPieceID(), false);
        if (gameLogic.getShouldDrawHeldPiece())
            heldPieceTextures = createPieceTextures(gameLogic.getHeldPiece(), gameLogic.getHeldPieceID(), false);
    }

    private void drawPiece(Sprite[] piece) {
        for (Sprite tile : piece) {
            tile.draw(game.batch);
        }
    }

    private void drawLandedTilesTextures() {
        for (Sprite tile : landedTilesTextures) {
            tile.draw(game.batch);
        }
    }
}
