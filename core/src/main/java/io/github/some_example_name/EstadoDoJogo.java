package io.github.some_example_name;

import io.github.some_example_name.entidades.Inimigo;
import io.github.some_example_name.entidades.Jogador;

public class EstadoDoJogo {
    private final Jogador jogador;
    private Inimigo inimigoAtual;

    public EstadoDoJogo(Jogador jogador) {
        this.jogador = jogador;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public Inimigo getInimigoAtual() {
        return inimigoAtual;
    }

    public void setInimigoAtual(Inimigo inimigoAtual) {
        this.inimigoAtual = inimigoAtual;
    }
}