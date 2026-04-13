package rpg;

public enum StatusAlterado {
    NENHUM      ("Nenhum",      "  "),
    ENVENENADO  ("Envenenado",  "☠ "),
    PARALISADO  ("Paralisado",  "⚡ "),
    QUEIMANDO   ("Queimando",   "🔥"),
    AMALDICOADO ("Amaldiçoado", "💀");

    private final String nome;
    private final String icone;

    StatusAlterado(String nome, String icone) {
        this.nome  = nome;
        this.icone = icone;
    }

    public String getNome()  { return nome; }
    public String getIcone() { return icone; }
}
