package io.github.some_example_name.perguntas;

// Representa o estado de revisão de uma pergunta
public class DadosDeRevisao {
    public String textoPergunta; // Identificador da pergunta
    public String dataUltimaRespostaCorreta; // Formato "YYYY-MM-DD"

    // Construtor vazio para o parser JSON
    public DadosDeRevisao() {}

    public DadosDeRevisao(String textoPergunta, String data) {
        this.textoPergunta = textoPergunta;
        this.dataUltimaRespostaCorreta = data;
    }
}