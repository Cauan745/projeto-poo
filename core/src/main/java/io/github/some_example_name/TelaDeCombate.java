package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.entidades.Inimigo;
import io.github.some_example_name.entidades.Jogador;
import io.github.some_example_name.perguntas.Pergunta;
import io.github.some_example_name.utils.RenderizacaoUtils;

import java.util.function.Consumer;

public class TelaDeCombate implements Screen, ListenerResposta {

    private final MyGdxGame game;
    private final Jogador jogador;
    private final Inimigo inimigo;
    private final GerenciadorDePerguntas gerenciadorDePerguntas;
    private final Consumer<Boolean> callbackFimCombate; // Calback do mygdxgame

    private final Stage stage; //  Gerencia os sprites na tela
    private final Texture texturaJogador;
    private final Texture texturaInimigo;
    private final Label labelPvJogador;
    private final Label labelPvInimigo;
    private final TextButton botaoAtacar;

    // Constantes para os estados da animaç[ão
    private static final int ESTADO_ANIMACAO_PARADO = 0;
    private static final int ESTADO_ANIMACAO_AVANCANDO = 1;
    private static final int ESTADO_ANIMACAO_RETORNANDO = 2;


    private static final int ATACANTE_NENHUM = 0;
    private static final int ATACANTE_JOGADOR = 1;
    private static final int ATACANTE_INIMIGO = 2;

    // Variaveis para guardar o estado atual
    private int estadoAnimacao = ESTADO_ANIMACAO_PARADO;
    private int atacanteAtual = ATACANTE_NENHUM; 

    private float timerAnimacao = 0f; 
    private float jogadorPosInicialX, jogadorPosInicialY; 
    private float inimigoPosInicialX, inimigoPosInicialY; 

    private static final float FATOR_ESCALA_SPRITE = 2.5f; 
    private static final float DURACAO_AVANCO = 0.4f; // Tempo em segundos da animaçao
    private static final float DURACAO_RETORNO = 0.3f; // Tempo em segundos da animação

    // Construtor da tela
    public TelaDeCombate(final MyGdxGame game, EstadoDoJogo estadoDoJogo, Inimigo inimigo, GerenciadorDePerguntas gerenciador, AssetManager assetManager, Consumer<Boolean> callback) {
        
        this.game = game;
        this.jogador = estadoDoJogo.getJogador();
        this.inimigo = inimigo;
        this.gerenciadorDePerguntas = gerenciador;
        this.callbackFimCombate = callback;
        this.stage = new Stage(new ScreenViewport()); // Serve para carregar as texturas na tela

        this.texturaJogador = assetManager.get(jogador.getNomeArquivoTextura(), Texture.class);
        this.texturaInimigo = assetManager.get(inimigo.getNomeArquivoTextura(), Texture.class);

        Label.LabelStyle labelStyle = new Label.LabelStyle(game.font, Color.WHITE);
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;

        // Cria as labels de vida e o botão de ataque
        labelPvJogador = new Label("", labelStyle);
        labelPvInimigo = new Label("", labelStyle);
        atualizarLabelsDeVida(); // Vai preencher o texto dos labels

        botaoAtacar = new TextButton("Atacar", textButtonStyle);
        
        botaoAtacar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                
                if (estadoAnimacao == ESTADO_ANIMACAO_PARADO) {
                    Pergunta pergunta = gerenciadorDePerguntas.getProximaPergunta();
                    // Muda para a tela de pergunta passando a si mesma como o listener
                    game.setScreen(new TelaDePergunta(game, pergunta, TelaDeCombate.this));
                }
            }
        });

        Table table = new Table();
        table.setFillParent(true); // Faz a tabela ocupar toda a tela
        table.bottom().padBottom(30); // Alinha a tabela na parte de baixo com um preenchimento
        table.add(labelPvJogador).pad(10);
        table.add(labelPvInimigo).pad(10).row(); // .row() pula para a próxima linha da tabela
        table.add(botaoAtacar).colspan(2).pad(20); // .colspan(2) faz o botão ocupar 2 colunas
        stage.addActor(table);
    }

    // Chamado pela tela de pergunta quando o jogador responde.
    @Override
    public void onPerguntaRespondida(boolean acertou, Pergunta pergunta) {
        game.setScreen(this); // Garante que o jogo volte a exibir a tela de combate.
        if (acertou) {
            gerenciadorDePerguntas.processarRespostaCorreta(pergunta);
            iniciarAnimacao(ATACANTE_JOGADOR); // Se acertou o jogador ataca
        } else {
            iniciarAnimacao(ATACANTE_INIMIGO); // Se errou o inimigo ataca
        }
    }

    private void iniciarAnimacao(int atacante) {
        if (estadoAnimacao != ESTADO_ANIMACAO_PARADO) return; // garantir que apenas uma animação ocorra
        this.atacanteAtual = atacante;
        this.estadoAnimacao = ESTADO_ANIMACAO_AVANCANDO;
        this.timerAnimacao = 0f; // Reseta o cronômetro da animação
        botaoAtacar.setVisible(false); // Esconde o botão de atacar durante a animação
    }

    @Override
    public void render(float delta) {
        // Limpar tela
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Atualiza a lógica do jogo
        atualizarAnimacao(delta);

        // Desenha os bonecos
        game.batch.setProjectionMatrix(stage.getCamera().combined); // Configura a câmera
        game.batch.begin(); // Inicia o processo de desenho
        desenharEntidades(game.batch);
        game.batch.end(); // Finaliza o processo de desenho

        // Desenha a tela
        stage.act(delta);
        stage.draw();
    }

    private void atualizarAnimacao(float delta) {
        if (estadoAnimacao == ESTADO_ANIMACAO_PARADO) return;

        timerAnimacao += delta; // Incrementa o timer com o tempo que passou

        if (estadoAnimacao == ESTADO_ANIMACAO_AVANCANDO) {
            // Quando o tempo de avanço terminar
            if (timerAnimacao >= DURACAO_AVANCO) {
                if (atacanteAtual == ATACANTE_JOGADOR) inimigo.receberDano(jogador.getDano());
                else jogador.receberDano(inimigo.getDano());
                
                atualizarLabelsDeVida();
                estadoAnimacao = ESTADO_ANIMACAO_RETORNANDO;
                timerAnimacao = 0f; // Reseta o timer
            }
        } else if (estadoAnimacao == ESTADO_ANIMACAO_RETORNANDO) {
            // Quando o tempo de retorno terminar
            if (timerAnimacao >= DURACAO_RETORNO) {
                estadoAnimacao = ESTADO_ANIMACAO_PARADO;
                atacanteAtual = ATACANTE_NENHUM;

                // Verifica se o combate terminou
                if (inimigo.estaDerrotado()) callbackFimCombate.accept(true); // vitória
                else if (jogador.estaDerrotado()) callbackFimCombate.accept(false); // derrota
                else botaoAtacar.setVisible(true); // se ninguém morreu mostra o botão novamente
            }
        }
    }

    private void desenharEntidades(SpriteBatch batch) {
        // Calcula o tamanho das imagens
        float larguraJogador = texturaJogador.getWidth() * FATOR_ESCALA_SPRITE;
        float alturaJogador = texturaJogador.getHeight() * FATOR_ESCALA_SPRITE;
        float larguraInimigo = texturaInimigo.getWidth() * FATOR_ESCALA_SPRITE;
        float alturaInimigo = texturaInimigo.getHeight() * FATOR_ESCALA_SPRITE;

        float jogadorX = jogadorPosInicialX;
        float inimigoX = inimigoPosInicialX;

        // Calcula a nova posição se a animação estiver rolando
        if (estadoAnimacao != ESTADO_ANIMACAO_PARADO) {
            float progresso;
            if (estadoAnimacao == ESTADO_ANIMACAO_AVANCANDO) {
                // Interpolation é usado para criar o movimento suave dos boneco
                progresso = Interpolation.pow2Out.apply(timerAnimacao / DURACAO_AVANCO);
                if (atacanteAtual == ATACANTE_JOGADOR) jogadorX = Interpolation.linear.apply(jogadorPosInicialX, inimigoPosInicialX - larguraJogador, progresso);
                else inimigoX = Interpolation.linear.apply(inimigoPosInicialX, jogadorPosInicialX + larguraJogador, progresso);
            } else {
                progresso = Interpolation.pow2In.apply(timerAnimacao / DURACAO_RETORNO);
                if (atacanteAtual == ATACANTE_JOGADOR) jogadorX = Interpolation.linear.apply(inimigoPosInicialX - larguraJogador, jogadorPosInicialX, progresso);
                else inimigoX = Interpolation.linear.apply(jogadorPosInicialX + larguraJogador, inimigoPosInicialX, progresso);
            }
        }

        RenderizacaoUtils.desenharComBorda(batch, texturaJogador, jogadorX, jogadorPosInicialY, larguraJogador, alturaJogador, false, false, 2.0f);
        RenderizacaoUtils.desenharComBorda(batch, texturaInimigo, inimigoX, inimigoPosInicialY, larguraInimigo, alturaInimigo, false, false, 2.0f);
    }
    
    private void atualizarLabelsDeVida() {
        labelPvJogador.setText("PV Jogador: " + jogador.getPontosDeVida() + "/" + jogador.getPontosDeVidaMaximos());
        labelPvInimigo.setText("PV Inimigo: " + inimigo.getPontosDeVida() + "/" + inimigo.getPontosDeVidaMaximos());
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Atualiza o viewport da UI

        // Recalcula o tamanho dos sprites
        float larguraRenderJogador = texturaJogador.getWidth() * FATOR_ESCALA_SPRITE;
        float larguraRenderInimigo = texturaInimigo.getWidth() * FATOR_ESCALA_SPRITE;
        
        jogadorPosInicialX = width * 0.20f - (larguraRenderJogador / 2);
        
        inimigoPosInicialX = width * 0.80f - (larguraRenderInimigo / 2);

        // Define a altura Y para um pouco abaixo do meio da tela
        float yPos = height * 0.45f;
        jogadorPosInicialY = yPos;
        inimigoPosInicialY = yPos;
    }

    @Override
    public void show() {
        // Fazer com que que botões e cliques funcionem
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        // Reseta o estado da animação quando trocar de tela
        estadoAnimacao = ESTADO_ANIMACAO_PARADO;
        botaoAtacar.setVisible(true);
    }
    
    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
}