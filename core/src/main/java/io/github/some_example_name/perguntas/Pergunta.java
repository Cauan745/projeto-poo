package io.github.some_example_name.perguntas;

import java.util.List;

public class Pergunta {
    private String texto;
    private List<String> opcoes;
    private int indiceRespostaCorreta;

    // Construtor vazio é necessário para ler o json
    public Pergunta() {}

    public String getTexto() {
        return texto;
    }

    public List<String> getOpcoes() {
        return opcoes;
    }
    
    public boolean verificarResposta(int indiceSelecionado) {
        return indiceSelecionado == this.indiceRespostaCorreta;
    }
    
    public String getTextoOpcaoCorreta() {
        if (opcoes != null && indiceRespostaCorreta >= 0 && indiceRespostaCorreta < opcoes.size()) {
            return opcoes.get(indiceRespostaCorreta);
        }
        return "Opção correta inválida";
    }
}