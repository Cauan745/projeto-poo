package io.github.some_example_name.entidades;

public abstract class Entidade {
    private final int pontosDeVidaMaximos;
    private int pontosDeVida;
    private int dano;
    private boolean derrotado;
    private final String nomeArquivoTextura;

    public Entidade(int pontosDeVidaIniciais, int dano, String nomeArquivoTextura) {
        this.pontosDeVidaMaximos = pontosDeVidaIniciais;
        this.pontosDeVida = pontosDeVidaIniciais;
        this.dano = dano;
        this.derrotado = false;
        this.nomeArquivoTextura = nomeArquivoTextura;
    }

    public int getPontosDeVida() {
        return pontosDeVida;
    }

    public int getPontosDeVidaMaximos() {
        return pontosDeVidaMaximos;
    }
    
    public int getDano() {
        return dano;
    }

    public void receberDano(int quantidade) {
        this.pontosDeVida -= quantidade;
        if (this.pontosDeVida <= 0) {
            this.pontosDeVida = 0;
            this.derrotado = true;
        }
    }

    public void resetar() {
        this.pontosDeVida = this.pontosDeVidaMaximos;
        this.derrotado = false;
    }

    public boolean estaDerrotado() {
        return derrotado;
    }

    public void setDerrotado(boolean derrotado) {
        this.derrotado = derrotado;
    }

    public String getNomeArquivoTextura() {
        return nomeArquivoTextura;
    }
}