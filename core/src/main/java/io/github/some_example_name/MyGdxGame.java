package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.entidades.Inimigo;
import io.github.some_example_name.entidades.Jogador;

import java.util.Random;

public class MyGdxGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assetManager;
    private GerenciadorDePerguntas gerenciadorDePerguntas;
    private EstadoDoJogo estadoDoJogo;
    private Random random;

    private int inimigosDerrotados = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        assetManager = new AssetManager();
        random = new Random();

        carregarAssets();

        gerenciadorDePerguntas = new GerenciadorDePerguntas();
        gerenciadorDePerguntas.carregar();

        // Inicializa o estado do jogo com um novo jogador
        Jogador jogador = new Jogador();
        estadoDoJogo = new EstadoDoJogo(jogador);

        iniciarProximoCombate();
    }

    private void iniciarProximoCombate() {

        String assetInimigoAleatorio = GameConstants.CAMINHOS_ASSETS_INIMIGOS[random.nextInt(GameConstants.CAMINHOS_ASSETS_INIMIGOS.length)];
        Inimigo novoInimigo = new Inimigo(assetInimigoAleatorio);
        
        estadoDoJogo.setInimigoAtual(novoInimigo);

        Gdx.app.log("MyGdxGame", "Iniciando combate contra inimigo " + (inimigosDerrotados + 1));

        // Cria a tela de combate com o novo inimigo
        setScreen(new TelaDeCombate(this, estadoDoJogo, novoInimigo, gerenciadorDePerguntas, assetManager, this::onCombateFinalizado));
    }

    private void onCombateFinalizado(boolean jogadorVenceu) {
        if (jogadorVenceu) {
            inimigosDerrotados++;
            Gdx.app.log("MyGdxGame", "Jogador venceu! Inimigos derrotados: " + inimigosDerrotados);

            // Inicia a próxima luta
            iniciarProximoCombate();
        } else {
            Gdx.app.log("MyGdxGame", "Jogador foi derrotado. Fim de jogo.");
            Gdx.app.exit();
        }
    }

    private void carregarAssets() {
        for (String assetInimigo : GameConstants.CAMINHOS_ASSETS_INIMIGOS) {
            assetManager.load(assetInimigo, Texture.class);
        }
        assetManager.load(GameConstants.TEXTURA_JOGADOR, Texture.class);
        assetManager.finishLoading(); // Espera tudo ser carregado antes de continuar
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
        if (screen != null) screen.dispose();
    }
}