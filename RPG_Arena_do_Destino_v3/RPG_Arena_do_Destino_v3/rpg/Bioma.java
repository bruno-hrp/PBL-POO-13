package rpg;

public enum Bioma {
    FLORESTA  ("Floresta Sombria",   "🌲", new int[]{0, 1}),   // Goblin, Orc
    MASMORRA  ("Masmorra Profunda",  "⛏", new int[]{2, 4}),   // Mago Trevas, Necromante
    VULCAO    ("Cratera do Vulcão",  "🌋", new int[]{3, 5}),   // Dragão, Troll
    CEMITERIO ("Cemitério Maldito",  "💀", new int[]{4, 2}),   // Necromante, Mago Trevas
    CASTELO   ("Castelo em Ruínas", "🏰", new int[]{1, 3, 5}); // Orc, Dragão, Troll

    private final String nome;
    private final String icone;
    private final int[] indicesInimigos; // índices em Inimigo.TEMPLATES

    Bioma(String nome, String icone, int[] indicesInimigos) {
        this.nome            = nome;
        this.icone           = icone;
        this.indicesInimigos = indicesInimigos;
    }

    public String getNome()              { return nome; }
    public String getIcone()             { return icone; }
    public int[]  getIndicesInimigos()   { return indicesInimigos; }

    public static Bioma aleatorio() {
        Bioma[] valores = values();
        return valores[(int)(Math.random() * valores.length)];
    }

    /** Inimigo temático do bioma, escalado ao nível do jogador */
    public Inimigo gerarInimigo(int nivelJogador) {
        int idx = indicesInimigos[(int)(Math.random() * indicesInimigos.length)];
        String[] t = Inimigo.TEMPLATES[idx];
        double escala = 1.0 + (nivelJogador - 1) * 0.25;
        int vida    = (int)(Integer.parseInt(t[1]) * escala);
        int ataque  = (int)(Integer.parseInt(t[2]) * escala);
        int defesa  = (int)(Integer.parseInt(t[3]) * escala);
        int energia = Integer.parseInt(t[4]);
        int esquiva = Integer.parseInt(t[5]);
        int critico = Integer.parseInt(t[6]);
        int xp      = (int)(Integer.parseInt(t[7]) * escala);
        return new Inimigo(t[0], vida, ataque, defesa, energia, esquiva, critico, xp, xp, false);
    }
}
