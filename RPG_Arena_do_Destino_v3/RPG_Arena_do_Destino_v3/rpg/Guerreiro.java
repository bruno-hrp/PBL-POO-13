package rpg;

public class Guerreiro extends PersonagemBase implements HabilidadeEspecial, Recuperavel {

    public Guerreiro(String nome) {
        super(nome, 120, 35, 30, 90, 10, 10);
        setClasse(ClassePersonagem.GUERREIRO);
    }

    @Override
    public String getSprite() {
        return  "    O    \n" +
                "   /|\\   \n" +
                "   / \\   \n" +
                "  [###]  \n" +
                " Guerreiro";
    }

    @Override
    public void atacar(PersonagemBase alvo) {
        boolean critico = tentarCritico();
        System.out.println("  [G] " + getNome() + " desfere um golpe de espada!");
        alvo.receberDanoDeClasse(getAtaque(), critico, getClasse());
        if ((int)(Math.random() * 100) < 15)
            alvo.aplicarStatus(StatusAlterado.ENVENENADO, 2);
    }

    @Override
    public void usarHabilidade(PersonagemBase alvo) {
        if (!consumirEnergia(CUSTO_HABILIDADE)) {
            System.out.println("  X Energia insuficiente para " + getNomeHabilidade() + "! (Custo:" + CUSTO_HABILIDADE + ")");
            return;
        }
        int dano = (int)(getAtaque() * 1.8);
        System.out.println("  [!!!] " + getNome() + " usa " + getNomeHabilidade() + "!");
        alvo.receberDanoDeClasse(dano, false, getClasse());
        // Furia berserker atordoa
        if ((int)(Math.random() * 100) < 30)
            alvo.aplicarStatus(StatusAlterado.PARALISADO, 1);
    }

    @Override
    public String getNomeHabilidade() { return "Furia Berserker"; }

    @Override
    public void recuperar() {
        if (!consumirEnergia(CUSTO_RECUPERAR)) {
            System.out.println("  X Energia insuficiente para recuperar! (Custo:" + CUSTO_RECUPERAR + ")");
            return;
        }
        int cura = CURA_BASE + 10;
        setVida(getVida() + cura);
        System.out.println("  [+] " + getNome() + " usa Segunda Folego! +" + cura
                + " vida. Vida: " + getVida() + "/" + getVidaMaxima());
    }

    @Override
    public String toString() {
        return "Guerreiro [" + getNome() + "] Nv." + getNivel()
                + " ATQ:" + getAtaque() + " DEF:" + getDefesa()
                + " ESQ:" + getChanceEsquiva() + "% CRIT:" + getChanceCritico() + "%";
    }
}
