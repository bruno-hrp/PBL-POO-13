package rpg;

public class Arqueiro extends PersonagemBase implements HabilidadeEspecial, Recuperavel {

    public Arqueiro(String nome) {
        super(nome, 100, 30, 10, 100, 25, 25);
        setClasse(ClassePersonagem.ARQUEIRO);
    }

    @Override
    public String getSprite() {
        return  "    O    \n" +
                "   /|>   \n" +
                "   /|    \n" +
                "   [ ]   \n" +
                " Arqueiro";
    }

    @Override
    public void atacar(PersonagemBase alvo) {
        boolean critico = tentarCritico();
        System.out.println("  [A] " + getNome() + " dispara uma flecha precisa!");
        alvo.receberDanoDeClasse(getAtaque(), critico, getClasse());
        if ((int)(Math.random() * 100) < 25)
            alvo.aplicarStatus(StatusAlterado.ENVENENADO, 3);
    }

    @Override
    public void usarHabilidade(PersonagemBase alvo) {
        if (!consumirEnergia(CUSTO_HABILIDADE)) {
            System.out.println("  X Energia insuficiente para " + getNomeHabilidade() + "! (Custo:" + CUSTO_HABILIDADE + ")");
            return;
        }
        int dano = (int)(getAtaque() * 0.8);
        System.out.println("  [!!!] " + getNome() + " usa " + getNomeHabilidade() + "! Multiplas flechas!");
        alvo.receberDanoDeClasse(dano, tentarCritico(), getClasse());
        alvo.receberDanoDeClasse(dano, tentarCritico(), getClasse());
        alvo.receberDanoDeClasse(dano, tentarCritico(), getClasse());
    }

    @Override
    public String getNomeHabilidade() { return "Chuva de Flechas"; }

    @Override
    public void recuperar() {
        if (!consumirEnergia(CUSTO_RECUPERAR)) {
            System.out.println("  X Energia insuficiente para recuperar! (Custo:" + CUSTO_RECUPERAR + ")");
            return;
        }
        int curaVida = 15, curaEnergia = 20;
        setVida(getVida() + curaVida);
        setEnergia(getEnergia() + curaEnergia);
        System.out.println("  [+] " + getNome() + " usa Ervas! +" + curaVida
                + " vida, +" + curaEnergia + " energia.");
    }

    @Override
    public String toString() {
        return "Arqueiro [" + getNome() + "] Nv." + getNivel()
                + " ATQ:" + getAtaque() + " DEF:" + getDefesa()
                + " ESQ:" + getChanceEsquiva() + "% CRIT:" + getChanceCritico() + "%";
    }
}
