package rpg;

public abstract class PersonagemBase {

    private String nome;
    private int vidaMaxima;
    private int vida;
    private int ataque;
    private int defesa;
    private int energiaMaxima;
    private int energia;
    private int nivel;
    private boolean defendendo;
    private int chanceEsquiva;
    private int chanceCritico;
    private StatusAlterado status;
    private int turnosStatus;
    private int ouro;
    private ClassePersonagem classe;

    private static final double MULTIPLICADOR_CRITICO = 2.0;

    public PersonagemBase(String nome, int vida, int ataque, int defesa, int energia,
                          int chanceEsquiva, int chanceCritico) {
        this.nome          = nome;
        this.vidaMaxima    = vida;
        this.vida          = vida;
        this.ataque        = ataque;
        this.defesa        = defesa;
        this.energiaMaxima = energia;
        this.energia       = energia;
        this.nivel         = 1;
        this.defendendo    = false;
        this.chanceEsquiva = chanceEsquiva;
        this.chanceCritico = chanceCritico;
        this.status        = StatusAlterado.NENHUM;
        this.turnosStatus  = 0;
        this.ouro          = 0;
        this.classe        = ClassePersonagem.GUERREIRO; // default, subclasses sobrescrevem
    }

    // Subclasses devem chamar este método em seu construtor
    protected void setClasse(ClassePersonagem c) { this.classe = c; }
    public ClassePersonagem getClasse() { return classe; }

    public abstract void atacar(PersonagemBase alvo);
    public abstract String getSprite();

    public void receberDano(int danoAtaque, boolean ehCritico) {
        receberDanoDeClasse(danoAtaque, ehCritico, null);
    }

    /** RN05 — Versão com vantagem de classe aplicada antes da defesa. */
    public void receberDanoDeClasse(int danoAtaque, boolean ehCritico, ClassePersonagem classeAtacante) {
        if (tentarEsquiva()) {
            System.out.println("  ✦ " + nome + " ESQUIVOU do ataque!");
            return;
        }
        // RN05: aplica bônus de vantagem antes de calcular defesa
        if (classeAtacante != null && classeAtacante.temVantagemSobre(this.classe)) {
            danoAtaque = (int)(danoAtaque * 1.20);
        }
        int defesaEfetiva = defendendo ? defesa * 2 : defesa;
        int dano = danoAtaque - defesaEfetiva;
        if (dano < 1) dano = 1;
        if (ehCritico) {
            dano = (int)(dano * MULTIPLICADOR_CRITICO);
            System.out.println("  ★ GOLPE CRITICO! Dano dobrado!");
        }
        vida -= dano;
        if (vida < 0) vida = 0;
        String bloqueio = defendendo ? " (defendendo)" : "";
        System.out.println("  " + nome + bloqueio + " recebeu " + dano
                + " de dano! Vida: " + vida + "/" + vidaMaxima);
        defendendo = false;
    }

    public void aplicarStatus(StatusAlterado novoStatus, int turnos) {
        if (this.status != StatusAlterado.NENHUM) return;
        this.status       = novoStatus;
        this.turnosStatus = turnos;
        System.out.println("  " + novoStatus.getIcone() + " " + nome
                + " foi afetado por " + novoStatus.getNome() + " por " + turnos + " turno(s)!");
    }

    public boolean processarStatus() {
        if (status == StatusAlterado.NENHUM) return false;
        switch (status) {
            case ENVENENADO: {
                int dano = Math.max(1, vidaMaxima / 10);
                vida -= dano; if (vida < 0) vida = 0;
                System.out.println("  " + nome + " sofre " + dano + " de veneno! Vida: " + vida + "/" + vidaMaxima);
                break;
            }
            case QUEIMANDO: {
                int dano = Math.max(1, vidaMaxima / 8);
                vida -= dano; if (vida < 0) vida = 0;
                System.out.println("  " + nome + " sofre " + dano + " de queimadura! Vida: " + vida + "/" + vidaMaxima);
                break;
            }
            case PARALISADO: {
                System.out.println("  " + nome + " esta PARALISADO e perde o turno!");
                turnosStatus--;
                if (turnosStatus <= 0) { System.out.println("  " + nome + " se recuperou da paralisia."); status = StatusAlterado.NENHUM; }
                return true;
            }
            case AMALDICOADO: {
                ataque = Math.max(1, ataque - 2);
                System.out.println("  " + nome + " esta amaldicoado! Ataque reduzido.");
                break;
            }
            default: break;
        }
        turnosStatus--;
        if (turnosStatus <= 0 && status != StatusAlterado.NENHUM) {
            System.out.println("  " + nome + " se recuperou de " + status.getNome() + ".");
            status = StatusAlterado.NENHUM;
        }
        return false;
    }

    public void defender() {
        this.defendendo = true;
        System.out.println("  [D] " + nome + " postura defensiva! Defesa dobrada neste turno.");
    }

    public boolean tentarEsquiva()  { return (int)(Math.random() * 100) < chanceEsquiva; }
    public boolean tentarCritico()  { return (int)(Math.random() * 100) < chanceCritico; }
    public boolean estaVivo()       { return vida > 0; }

    public boolean consumirEnergia(int custo) {
        if (energia < custo) return false;
        energia -= custo;
        return true;
    }

    public void subirNivel() {
        nivel++;
        int bV = 15, bA = 3, bD = 2, bE = 10;
        vidaMaxima += bV; vida = vidaMaxima;
        ataque     += bA; defesa += bD;
        energiaMaxima += bE; energia = energiaMaxima;
        System.out.println("\n  *** " + nome + " subiu para o Nivel " + nivel + "! ***");
        System.out.printf ("  +Vida:+%d | +ATQ:+%d | +DEF:+%d | +NRG:+%d%n", bV, bA, bD, bE);
        System.out.println("  Vida e Energia totalmente restauradas!\n");
    }

    public void restaurarParaBatalha(boolean completo) {
        if (completo) {
            vida    = vidaMaxima;
            energia = energiaMaxima;
            System.out.println("  Descansou. Vida e energia totalmente restauradas!");
        } else {
            int cV = (int)(vidaMaxima * 0.40);
            int cE = (int)(energiaMaxima * 0.40);
            vida    = Math.min(vidaMaxima,    vida    + cV);
            energia = Math.min(energiaMaxima, energia + cE);
            System.out.println("  Recuperacao parcial: +" + cV + " Vida, +" + cE + " Energia.");
        }
        defendendo = false;
        status     = StatusAlterado.NENHUM;
        turnosStatus = 0;
    }

    public String barraVida() {
        int b = 20, c = (int)((double)vida / vidaMaxima * b);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < b; i++) sb.append(i < c ? "X" : ".");
        return sb.append("] ").append(vida).append("/").append(vidaMaxima).toString();
    }

    public String barraEnergia() {
        int b = 10, c = (int)((double)energia / energiaMaxima * b);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < b; i++) sb.append(i < c ? "#" : ".");
        return sb.append("] ").append(energia).append("/").append(energiaMaxima).toString();
    }

    // Getters
    public String         getNome()          { return nome; }
    public int            getVida()          { return vida; }
    public int            getVidaMaxima()    { return vidaMaxima; }
    public int            getAtaque()        { return ataque; }
    public int            getDefesa()        { return defesa; }
    public int            getEnergia()       { return energia; }
    public int            getEnergiaMaxima() { return energiaMaxima; }
    public int            getNivel()         { return nivel; }
    public boolean        isDefendendo()     { return defendendo; }
    public int            getChanceEsquiva() { return chanceEsquiva; }
    public int            getChanceCritico() { return chanceCritico; }
    public StatusAlterado getStatus()        { return status; }
    public int            getOuro()          { return ouro; }

    // Setters
    public void setVida(int v)              { vida    = Math.max(0, Math.min(vidaMaxima, v)); }
    public void setEnergia(int e)           { energia = Math.max(0, Math.min(energiaMaxima, e)); }
    public void setAtaque(int a)            { if (a > 0) ataque = a; }
    public void setDefesa(int d)            { if (d >= 0) defesa = d; }
    public void setBonusCritico(int v)      { chanceCritico = Math.max(0, Math.min(95, v)); }
    public void setBonusEsquiva(int v)      { chanceEsquiva = Math.max(0, Math.min(90, v)); }
    public void setStatus(StatusAlterado s) { status = s; turnosStatus = 0; }
    public void adicionarOuro(int q)        { if (q > 0) ouro += q; }
    public boolean gastarOuro(int q)        { if (ouro < q) return false; ouro -= q; return true; }

    // ── RF07: Gasto de XP em atributos ───────────────────────
    public void melhorarAtaqueComXP(int ganho) {
        setAtaque(getAtaque() + ganho);
        System.out.printf("  [UP] Ataque: +%d → %d%n", ganho, getAtaque());
    }
    public void melhorarDefesaComXP(int ganho) {
        setDefesa(getDefesa() + ganho);
        System.out.printf("  [UP] Defesa: +%d → %d%n", ganho, getDefesa());
    }
    public void melhorarVidaComXP(int ganho) {
        vidaMaxima += ganho;
        vida = Math.min(vida + ganho, vidaMaxima);
        System.out.printf("  [UP] Vida Max: +%d → %d%n", ganho, vidaMaxima);
    }
    public void melhorarEnergiaComXP(int ganho) {
        energiaMaxima += ganho;
        energia = Math.min(energia + ganho, energiaMaxima);
        System.out.printf("  [UP] Energia Max: +%d → %d%n", ganho, energiaMaxima);
    }
}
