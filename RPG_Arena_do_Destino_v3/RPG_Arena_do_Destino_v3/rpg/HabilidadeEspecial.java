package rpg;

public interface HabilidadeEspecial {
    int CUSTO_HABILIDADE = 25;
    void usarHabilidade(PersonagemBase alvo);
    String getNomeHabilidade();
}
