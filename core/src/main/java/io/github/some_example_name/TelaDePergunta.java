package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.perguntas.Pergunta;

public class TelaDePergunta implements Screen {

    private final MyGdxGame game;
    private final Stage stage;
    private final Pergunta pergunta;
    private final ListenerResposta ListenerResposta;

    // Variáveis para controlar o fluxo da tela
    private boolean respostaSelecionada = false;
    private boolean acertou = false;
    private float timerFeedback = 0f;
    private static final float DURACAO_FEEDBACK = 0.8f;

  
    public TelaDePergunta(MyGdxGame game, Pergunta pergunta, ListenerResposta listener) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.pergunta = pergunta;
        this.ListenerResposta = listener;

        montarUI();
    }

    private void montarUI() {
        // Estilos simples, usando a fonte padrão do jogo para facilitar
        Label.LabelStyle estiloTitulo = new Label.LabelStyle(game.font, Color.YELLOW);
        TextButton.TextButtonStyle estiloBotao = new TextButton.TextButtonStyle();
        estiloBotao.font = game.font;
        estiloBotao.fontColor = Color.WHITE;

        // A Tabela organiza os elementos na tela
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // Adiciona o texto da pergunta
        Label labelPergunta = new Label(pergunta.getTexto(), estiloTitulo);
        labelPergunta.setWrap(true);
        labelPergunta.setAlignment(Align.center);
        table.add(labelPergunta).width(Gdx.graphics.getWidth() * 0.9f).padBottom(40).row();

        // Criar um botão para cada opção de resposta
        for (int i = 0; i < pergunta.getOpcoes().size(); i++) {
            final int indiceOpcao = i;
            String textoOpcao = pergunta.getOpcoes().get(i);
            
            TextButton botaoOpcao = new TextButton(textoOpcao, estiloBotao);
            
            botaoOpcao.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Ignorar cliques extras
                    if (respostaSelecionada) {
                        return;
                    }

                    respostaSelecionada = true;
                    acertou = pergunta.verificarResposta(indiceOpcao);
                }
            });

            table.add(botaoOpcao).pad(10).fillX().width(Gdx.graphics.getWidth() * 0.7f).row();
        }
    }

    @Override
    public void render(float delta) {
        // Limpa a tela com uma cor de fundo azul
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (respostaSelecionada) {
            timerFeedback += delta;
  
            if (timerFeedback >= DURACAO_FEEDBACK) {
                // Se passou o tempo chamamos o método da interface para notificar a TelaDeCombate
                ListenerResposta.onPerguntaRespondida(acertou, pergunta);
            }
        }

        // Atualiza e desenha a interface do usuário
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void show() {
        // Informa ao libgdx que esta tela deve receber os inputs do jogador
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}