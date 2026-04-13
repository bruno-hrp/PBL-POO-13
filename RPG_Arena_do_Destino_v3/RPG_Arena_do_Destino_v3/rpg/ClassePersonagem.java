package rpg;

public enum ClassePersonagem {
    GUERREIRO, MAGO, ARQUEIRO;

    /**
     * RN05 — Triângulo de vantagem: Guerreiro > Arqueiro > Mago > Guerreiro.
     * Retorna true se ESTA classe tem vantagem sobre o alvo.
     */
    public boolean temVantagemSobre(ClassePersonagem outro) {
        if (this == GUERREIRO && outro == ARQUEIRO) return true;
        if (this == ARQUEIRO  && outro == MAGO)     return true;
        if (this == MAGO      && outro == GUERREIRO) return true;
        return false;
    }

    public String getNome() {
        switch (this) {
            case GUERREIRO: return "Guerreiro";
            case MAGO:      return "Mago";
            case ARQUEIRO:  return "Arqueiro";
            default:        return "?";
        }
    }
}
