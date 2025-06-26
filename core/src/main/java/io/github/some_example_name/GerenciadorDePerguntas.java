package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import io.github.some_example_name.perguntas.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class GerenciadorDePerguntas {

    private static final String CAMINHO_INTERNO_BASE = "questions/perguntas_base.json";

    private static final String CAMINHO_EXTERNO_PERGUNTAS = "minhas_perguntas.json";

    private static final String CAMINHO_EXTERNO_PROGRESSO = "progresso.json";

    private List<Pergunta> todasAsPerguntas; 
    private ProgressoUsuario progressoUsuario; 
    private final Random random; 
    private final Json json;

    public GerenciadorDePerguntas() {
        this.random = new Random();
        this.json = new Json();

        // Configura formataçãod o json
        this.json.setOutputType(JsonWriter.OutputType.json);

        this.todasAsPerguntas = new ArrayList<>();
        this.progressoUsuario = new ProgressoUsuario();
    }

    public void carregar() {

        verificarECriarArquivosUsuario();

        // Tenta carregar as perguntas do arquivo do usuário
        FileHandle arquivoPerguntasUsuario = Gdx.files.local(CAMINHO_EXTERNO_PERGUNTAS);
        try {
            ListaDePerguntas lista = json.fromJson(ListaDePerguntas.class, arquivoPerguntasUsuario);
            // Verifica se o arquivo não esta vazio
            if (lista != null && lista.perguntas != null) {
                todasAsPerguntas = lista.perguntas;
            }
        } catch (Exception e) {
            // Se houver erro da log nele e continua com uma lista vazia
            Gdx.app.error("Gerenciador", "Erro ao ler " + CAMINHO_EXTERNO_PERGUNTAS + ". Verifique se o JSON é válido.", e);
            todasAsPerguntas = new ArrayList<>();
        }

        // Tenta carregar o progresso salvo do usuário
        FileHandle arquivoProgresso = Gdx.files.local(CAMINHO_EXTERNO_PROGRESSO);
        if (arquivoProgresso.exists() && arquivoProgresso.length() > 0) {
            try {
                progressoUsuario = json.fromJson(ProgressoUsuario.class, arquivoProgresso);
                // Para caso o arquivo esteja vazio
                if (progressoUsuario == null) {
                    progressoUsuario = new ProgressoUsuario();
                }
            } catch (Exception e) {
                // Se houver erro cria um novo arquivo
                Gdx.app.error("Gerenciador", "Erro ao ler " + CAMINHO_EXTERNO_PROGRESSO + ". Criando um novo.", e);
                progressoUsuario = new ProgressoUsuario();
            }
        }
    }


    private void verificarECriarArquivosUsuario() {
        // Pega as perguntas locais
        FileHandle arquivoExterno = Gdx.files.local(CAMINHO_EXTERNO_PERGUNTAS);
        if (!arquivoExterno.exists()) {
            Gdx.app.log("Gerenciador", "Copiando perguntas base para a pasta do usuário.");
            // Pega o arquivo base de dentro do jogo
            FileHandle arquivoInterno = Gdx.files.internal(CAMINHO_INTERNO_BASE);
            
            arquivoInterno.copyTo(arquivoExterno);
        }

        // Pega o arquivo de progresso local
        FileHandle arquivoProgresso = Gdx.files.local(CAMINHO_EXTERNO_PROGRESSO);
        if (!arquivoProgresso.exists()) {
             Gdx.app.log("Gerenciador", "Criando arquivo de progresso do usuário.");
             // Cria um arquivo de progresso vazio
             arquivoProgresso.writeString(json.toJson(new ProgressoUsuario()), false);
        }
    }


    public Pergunta getProximaPergunta() {
        
        if (todasAsPerguntas.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de perguntas vazio");
        }
    
        String hoje = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        final List<String> textosPerguntasRespondidasHoje;
        if (progressoUsuario != null && progressoUsuario.dadosRevisao != null) {
            textosPerguntasRespondidasHoje = progressoUsuario.dadosRevisao.stream()
                .filter(dado -> hoje.equals(dado.dataUltimaRespostaCorreta)) // Filtra pela data de hoje
                .map(dado -> dado.textoPergunta) // Cria uma copia
                .collect(Collectors.toList()); //Junta todos os textos em uma nova lista
        } else {
            // Se não houver progresso a lista fica vazia
            textosPerguntasRespondidasHoje = Collections.emptyList();
        }

        // Agora filtra a lista principal de perguntas 
        List<Pergunta> perguntasDisponiveis = todasAsPerguntas.stream()
                // Mantem apenas as perguntas que não estão na lista de respondidas hoje
                .filter(p -> !textosPerguntasRespondidasHoje.contains(p.getTexto()))
                .collect(Collectors.toList());

        if (perguntasDisponiveis.isEmpty()) {
            Gdx.app.log("Gerenciador", "Todas as perguntas foram respondidas");
            // retorna uma pergunta aleatória
            return todasAsPerguntas.get(random.nextInt(todasAsPerguntas.size()));
        }

        // Se houver perguntas disponíveis retorna uma aleatoria
        return perguntasDisponiveis.get(random.nextInt(perguntasDisponiveis.size()));
    }

    public void processarRespostaCorreta(Pergunta pergunta) {
        String hoje = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        if (progressoUsuario == null) {
            progressoUsuario = new ProgressoUsuario();
        }
        if (progressoUsuario.dadosRevisao == null) {
            progressoUsuario.dadosRevisao = new ArrayList<>();
        }

        // Procura se ja existe um registro para esta pergunta no progresso
        DadosDeRevisao dadoExistente = progressoUsuario.dadosRevisao.stream()
            .filter(d -> pergunta.getTexto().equals(d.textoPergunta)) // Compara pelo texto da pergunta
            .findFirst()
            .orElse(null); // Se não encontrar retorna null

        if (dadoExistente != null) {
            // Se o registro já existe atualiza a data da ultima resposta correta
            dadoExistente.dataUltimaRespostaCorreta = hoje;
        } else {
            // Se não existe cria um novo registro de revisão e adiciona ele na lista
            progressoUsuario.dadosRevisao.add(new DadosDeRevisao(pergunta.getTexto(), hoje));
        }

        salvarProgresso();
    }

    private void salvarProgresso() {
        FileHandle arquivoProgresso = Gdx.files.local(CAMINHO_EXTERNO_PROGRESSO);
        arquivoProgresso.writeString(json.toJson(progressoUsuario), false);
        Gdx.app.log("Gerenciador", "Progresso salvo em formato JSON válido!");
    }
}