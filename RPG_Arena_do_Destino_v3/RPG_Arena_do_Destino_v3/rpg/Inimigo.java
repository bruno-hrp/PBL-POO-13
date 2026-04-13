package rpg;

public class Inimigo extends PersonagemBase implements HabilidadeEspecial, Recuperavel {

    private String tipo;
    private int xpRecompensa;
    private int ouroRecompensa;
    private boolean ehBoss;
    private ClassePersonagem classe; // RN05 / RF03

    public static final String[][] TEMPLATES = {
        // tipo, vida, ataque, defesa, energia, esquiva, critico, xp, ouro
        {"Goblin Selvagem",     "60",  "18", "5",  "60",  "20", "10", "30",  "15"},
        {"Orc Guerreiro",       "100", "25", "12", "70",  "8",  "15", "55",  "25"},
        {"Mago das Trevas",     "75",  "32", "6",  "110", "12", "28", "70",  "35"},
        {"Dragao Jovem",        "150", "38", "18", "90",  "15", "25", "120", "60"},
        {"Necromante",          "90",  "30", "10", "130", "10", "22", "90",  "45"},
        {"Troll das Montanhas", "130", "22", "20", "50",  "5",  "10", "80",  "40"},
    };

    // Templates exclusivos de BOSS
    public static final String[][] TEMPLATES_BOSS = {
        {"BOSS: Rei Lich",        "300", "45", "20", "150", "10", "30", "200", "150"},
        {"BOSS: Dragao Anciao",   "350", "50", "25", "120", "15", "35", "250", "200"},
        {"BOSS: Senhor das Trevas","280","55", "15", "180", "20", "40", "220", "175"},
    };

    public Inimigo(String tipo, int vida, int ataque, int defesa, int energia,
                   int esquiva, int critico, int xpRecompensa, int ouroRecompensa, boolean ehBoss) {
        super(tipo, vida, ataque, defesa, energia, esquiva, critico);
        this.tipo           = tipo;
        this.xpRecompensa   = xpRecompensa;
        this.ouroRecompensa = ouroRecompensa;
        this.ehBoss         = ehBoss;
        this.classe         = ClassePersonagem.GUERREIRO; // padrão
    }

    public void setClasseInimigo(ClassePersonagem c) {
        this.classe = c;
        setClasse(c);
    }
    public ClassePersonagem getClasseInimigo() { return classe; }

    public static Inimigo criarAleatorio(int nivelJogador) {
        int idx = (int)(Math.random() * TEMPLATES.length);
        return criarDeTemplate(TEMPLATES[idx], nivelJogador, false);
    }

    public static Inimigo criarDosBioma(Bioma bioma, int nivelJogador) {
        int[] indices = bioma.getIndicesInimigos();
        int idx = indices[(int)(Math.random() * indices.length)];
        return criarDeTemplate(TEMPLATES[idx], nivelJogador, false);
    }

    public static Inimigo criarBoss(int nivelJogador) {
        int idx = (int)(Math.random() * TEMPLATES_BOSS.length);
        return criarDeTemplate(TEMPLATES_BOSS[idx], nivelJogador, true);
    }

    /**
     * RF03 — Boss counter-classe: escolhe o boss cujo tipo vence a classe do jogador.
     * Guerreiro → boss Mago | Mago → boss Arqueiro | Arqueiro → boss Guerreiro
     */
    public static Inimigo criarBossCounterClasse(int nivelJogador, ClassePersonagem classeJogador) {
        ClassePersonagem classeCounter;
        String nomeSufixo;
        switch (classeJogador) {
            case GUERREIRO: classeCounter = ClassePersonagem.MAGO;      nomeSufixo = "Arcano";  break;
            case MAGO:      classeCounter = ClassePersonagem.ARQUEIRO;  nomeSufixo = "Caçador"; break;
            case ARQUEIRO:  classeCounter = ClassePersonagem.GUERREIRO; nomeSufixo = "Titã";    break;
            default:        classeCounter = ClassePersonagem.GUERREIRO; nomeSufixo = "Supremo"; break;
        }
        int idx = (int)(Math.random() * TEMPLATES_BOSS.length);
        Inimigo boss = criarDeTemplate(TEMPLATES_BOSS[idx], nivelJogador, true);
        boss.setClasseInimigo(classeCounter);
        // Personaliza o nome do boss para indicar o counter
        String nomeBase = boss.tipo.replace("BOSS: ", "");
        boss.tipo = "BOSS: " + nomeBase + " " + nomeSufixo;
        System.out.println("  [!!] O boss é da classe " + classeCounter.getNome()
                + " — vantagem contra " + classeJogador.getNome() + "!");
        return boss;
    }

    private static Inimigo criarDeTemplate(String[] t, int nivelJogador, boolean boss) {
        double escala = 1.0 + (nivelJogador - 1) * 0.25;
        int vida   = (int)(Integer.parseInt(t[1]) * escala);
        int ataque = (int)(Integer.parseInt(t[2]) * escala);
        int defesa = (int)(Integer.parseInt(t[3]) * escala);
        int energia = Integer.parseInt(t[4]);
        int esquiva = Integer.parseInt(t[5]);
        int critico = Integer.parseInt(t[6]);
        int xp      = (int)(Integer.parseInt(t[7]) * escala);
        int ouro    = (int)(Integer.parseInt(t[8]) * escala);
        return new Inimigo(t[0], vida, ataque, defesa, energia, esquiva, critico, xp, ouro, boss);
    }

    // ── IA ────────────────────────────────────────────────────

    public void executarTurno(PersonagemBase alvo) {
        // Verifica paralisia antes de agir
        if (processarStatus()) return;

        System.out.println("\n  [ IA - " + getNome() + " decide... ]");

        if (ehBoss) {
            executarTurnoBoss(alvo);
        } else {
            executarTurnoNormal(alvo);
        }
    }

    private void executarTurnoNormal(PersonagemBase alvo) {
        if (getVida() < getVidaMaxima() * 0.30 && getEnergia() >= CUSTO_RECUPERAR) {
            recuperar(); return;
        }
        if (getEnergia() >= CUSTO_HABILIDADE && Math.random() < 0.40) {
            usarHabilidade(alvo); return;
        }
        if (Math.random() < 0.15) {
            defender(); return;
        }
        atacar(alvo);
    }

    private void executarTurnoBoss(PersonagemBase alvo) {
        // Boss é mais agressivo e usa habilidade com mais frequência
        if (getVida() < getVidaMaxima() * 0.40 && getEnergia() >= CUSTO_RECUPERAR) {
            recuperar(); return;
        }
        if (getEnergia() >= CUSTO_HABILIDADE && Math.random() < 0.60) {
            usarHabilidade(alvo); return;
        }
        atacar(alvo);
    }

    @Override
    public String getSprite() {
        if (ehBoss) {
            return  " /======\\ \n" +
                    " | BOSS | \n" +
                    " | >  < | \n" +
                    " |  /\\  | \n" +
                    " \\======/ \n" +
                    "  " + tipo;
        }
        return  "  [###]  \n" +
                "  | X |  \n" +
                "  |___|  \n" +
                " " + tipo;
    }

    @Override
    public void atacar(PersonagemBase alvo) {
        boolean critico = tentarCritico();
        String prefixo = ehBoss ? "  [BOSS] " : "  [I] ";
        System.out.println(prefixo + getNome() + " ataca furiosamente!");
        alvo.receberDanoDeClasse(getAtaque(), critico, getClasseInimigo());
        // Boss aplica status com mais frequência
        int chanceStatus = ehBoss ? 35 : 15;
        if ((int)(Math.random() * 100) < chanceStatus) {
            StatusAlterado[] opcoes = {StatusAlterado.ENVENENADO, StatusAlterado.QUEIMANDO, StatusAlterado.PARALISADO};
            alvo.aplicarStatus(opcoes[(int)(Math.random() * opcoes.length)], 2);
        }
    }

    @Override
    public void usarHabilidade(PersonagemBase alvo) {
        if (!consumirEnergia(CUSTO_HABILIDADE)) { atacar(alvo); return; }
        int dano = ehBoss ? (int)(getAtaque() * 2.0) : (int)(getAtaque() * 1.7);
        String prefixo = ehBoss ? "  [BOSS!!!] " : "  [HAB] ";
        System.out.println(prefixo + getNome() + " usa " + getNomeHabilidade() + "!");
        alvo.receberDanoDeClasse(dano, false, getClasseInimigo());
        if (ehBoss) alvo.aplicarStatus(StatusAlterado.AMALDICOADO, 3);
    }

    @Override
    public String getNomeHabilidade() {
        return ehBoss ? "Forca das Trevas" : "Golpe Sombrio";
    }

    @Override
    public void recuperar() {
        if (!consumirEnergia(CUSTO_RECUPERAR)) return;
        int cura = ehBoss ? CURA_BASE * 2 : CURA_BASE;
        setVida(getVida() + cura);
        System.out.println("  [+] " + getNome() + " se regenera! +" + cura
                + " vida. Vida: " + getVida() + "/" + getVidaMaxima());
    }

    public int  getXpRecompensa()   { return xpRecompensa; }
    public int  getOuroRecompensa() { return ouroRecompensa; }
    public boolean isEhBoss()       { return ehBoss; }
    public String getTipo()         { return tipo; }

    @Override
    public String toString() {
        String tag = ehBoss ? "[BOSS] " : "";
        return tag + tipo + " | Vida:" + getVida() + "/" + getVidaMaxima()
                + " ATQ:" + getAtaque() + " DEF:" + getDefesa();
    }
}
