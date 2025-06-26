package io.github.some_example_name.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class RenderizacaoUtils {

    private RenderizacaoUtils() {}

    // Desenha uma textura com uma borda preta ao redor
    public static void desenharComBorda(SpriteBatch batch, Texture texture, float x, float y, float width, float height, boolean flipX, boolean flipY, float espessuraBorda) {
        Color corOriginal = batch.getColor().cpy();
        batch.setColor(Color.BLACK);

        float[] offsets = {
            -espessuraBorda, -espessuraBorda, espessuraBorda, -espessuraBorda,
            -espessuraBorda,  espessuraBorda, espessuraBorda,  espessuraBorda,
            -espessuraBorda, 0,                espessuraBorda, 0,
            0, -espessuraBorda,                0,  espessuraBorda
        };

        for (int i = 0; i < offsets.length; i += 2) {
            batch.draw(texture, x + offsets[i], y + offsets[i+1], width, height, 0, 0, texture.getWidth(), texture.getHeight(), flipX, flipY);
        }

        batch.setColor(corOriginal);
        batch.draw(texture, x, y, width, height, 0, 0, texture.getWidth(), texture.getHeight(), flipX, flipY);
    }
}