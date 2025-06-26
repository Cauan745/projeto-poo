package io.github.some_example_name.perguntas;

import java.util.ArrayList;
import java.util.List;

// Representa o arquivo de progresso do usuário
public class ProgressoUsuario {
    public List<DadosDeRevisao> dadosRevisao;

    public ProgressoUsuario() {
        this.dadosRevisao = new ArrayList<>();
    }
}