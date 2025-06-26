package io.github.some_example_name;

import io.github.some_example_name.perguntas.Pergunta;

public interface ListenerResposta {
    void onPerguntaRespondida(boolean acertou, Pergunta pergunta);
}