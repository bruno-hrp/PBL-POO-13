package rpg;

public class Mago extends PersonagemBase implements HabilidadeEspecial, Recuperavel {

    public Mago(String nome) {
        super(nome, 85, 35, 8, 120, 15, 30);
        setClasse(ClassePersonagem.MAGO);
    }

    @Override
    public String getSprite() {
        return  "    @    \n" +
                "   /|\\   \n" +
                "   / \\   \n" +
                "  [~~~]  \n" +
                "   Mago  ";
    }

    @Override
    public void atacar(PersonagemBase alvo) {
        boolean critico = tentarCritico();
        System.out.println("  [M] " + getNome() + " lanca uma Bola de Fogo!");
        alvo.receberDanoDeClasse(getAtaque(), critico, getClasse());
        if ((int)(Math.random() * 100) < 20)
            alvo.aplicarStatus(StatusAlterado.QUEIMANDO, 3);
    }

    @Override
    public void usarHabilidade(PersonagemBase alvo) {
        if (!consumirEnergia(CUSTO_HABILIDADE)) {
            System.out.println("  X Mana insuficiente para " + getNomeHabilidade() + "! (Custo:" + CUSTO_HABILIDADE + ")");
            return;
        }
        int dano = (int)(getAtaque() * 2.2);
        System.out.println("  [!!!] " + getNome() + " usa " + getNomeHabilidade() + "! Raios arrasam tudo!");
        alvo.receberDanoDeClasse(dano, false, getClasse());
        if ((int)(Math.random() * 100) < 35)
            alvo.aplicarStatus(StatusAlterado.AMALDICOADO, 3);
    }

    @Override
    public String getNomeHabilidade() { return "Tempestade Arcana"; }

    @Override
    public void recuperar() {
        if (!consumirEnergia(CUSTO_RECUPERAR)) {
            System.out.println("  X Mana insuficiente para recuperar! (Custo:" + CUSTO_RECUPERAR + ")");
            return;
        }
        int recarga = 40;
        setEnergia(getEnergia() + recarga);
        System.out.println("  [+] " + getNome() + " medita! +" + recarga
                + " mana. Mana: " + getEnergia() + "/" + getEnergiaMaxima());
    }

    @Override
    public String toString() {
        return "Mago [" + getNome() + "] Nv." + getNivel()
                + " ATQ:" + getAtaque() + " DEF:" + getDefesa()
                + " ESQ:" + getChanceEsquiva() + "% CRIT:" + getChanceCritico() + "%";
    }
}
