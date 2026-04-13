package rpg;

public interface Equipavel {
    void equipar(PersonagemBase personagem);
    void desequipar(PersonagemBase personagem);
    String getNomeItem();
    int getPreco();
}
