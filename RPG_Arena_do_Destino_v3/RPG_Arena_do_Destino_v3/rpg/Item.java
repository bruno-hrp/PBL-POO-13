package rpg;

public class Item implements Equipavel {

    private String nome;
    private String descricao;
    private TipoItem tipo;
    private int preco;
    private int valorCura;
    private int bonusAtaque;
    private int bonusDefesa;
    private int bonusCritico;
    private int bonusEsquiva;

    // Construtor geral
    public Item(String nome, String descricao, TipoItem tipo, int preco,
                int valorCura, int bonusAtaque, int bonusDefesa,
                int bonusCritico, int bonusEsquiva) {
        this.nome        = nome;
        this.descricao   = descricao;
        this.tipo        = tipo;
        this.preco       = preco;
        this.valorCura   = valorCura;
        this.bonusAtaque = bonusAtaque;
        this.bonusDefesa = bonusDefesa;
        this.bonusCritico = bonusCritico;
        this.bonusEsquiva = bonusEsquiva;
    }

    // ── Fábrica de itens predefinidos ──────────────────────────

    public static Item pocaoVida() {
        return new Item("Poção de Vida", "Restaura 40 de vida", TipoItem.CONSUMIVEL,
                        30, 40, 0, 0, 0, 0);
    }
    public static Item pocaoVidaGrande() {
        return new Item("Poção de Vida Grande", "Restaura 80 de vida", TipoItem.CONSUMIVEL,
                        55, 80, 0, 0, 0, 0);
    }
    public static Item pocaoEnergia() {
        return new Item("Poção de Energia", "Restaura 40 de energia", TipoItem.CONSUMIVEL,
                        25, 40, 0, 0, 0, 0);
    }
    public static Item antiveneno() {
        return new Item("Antídoto", "Cura veneno e paralisia", TipoItem.CONSUMIVEL,
                        20, 0, 0, 0, 0, 0);
    }
    public static Item espadaAfiada() {
        return new Item("Espada Afiada", "+8 Ataque", TipoItem.ARMA,
                        80, 0, 8, 0, 0, 0);
    }
    public static Item espadaLendaria() {
        return new Item("Espada Lendária", "+15 Ataque +5% Crítico", TipoItem.ARMA,
                        150, 0, 15, 0, 5, 0);
    }
    public static Item armaduraReforcada() {
        return new Item("Armadura Reforçada", "+8 Defesa", TipoItem.ARMADURA,
                        80, 0, 0, 8, 0, 0);
    }
    public static Item mantoSombrio() {
        return new Item("Manto Sombrio", "+6 Defesa +10% Esquiva", TipoItem.ARMADURA,
                        120, 0, 0, 6, 0, 10);
    }
    public static Item amuletoGuerreiro() {
        return new Item("Amuleto do Guerreiro", "+5 Ataque +5 Defesa", TipoItem.ACESSORIO,
                        100, 0, 5, 5, 0, 0);
    }
    public static Item aneelCritico() {
        return new Item("Anel do Assassino", "+15% Crítico", TipoItem.ACESSORIO,
                        90, 0, 0, 0, 15, 0);
    }

    // ── Uso do item ───────────────────────────────────────────

    public void usar(PersonagemBase alvo) {
        switch (tipo) {
            case CONSUMIVEL -> {
                if (nome.equals("Antídoto")) {
                    alvo.setStatus(StatusAlterado.NENHUM);
                    System.out.println("  💊 " + alvo.getNome() + " usou Antídoto! Status curado.");
                } else if (valorCura > 0) {
                    // decide se cura vida ou energia pelo nome
                    if (nome.contains("Energia")) {
                        alvo.setEnergia(alvo.getEnergia() + valorCura);
                        System.out.println("  💧 " + alvo.getNome() + " usou " + nome
                                + "! Energia +" + valorCura
                                + " (" + alvo.getEnergia() + "/" + alvo.getEnergiaMaxima() + ")");
                    } else {
                        alvo.setVida(alvo.getVida() + valorCura);
                        System.out.println("  🧪 " + alvo.getNome() + " usou " + nome
                                + "! Vida +" + valorCura
                                + " (" + alvo.getVida() + "/" + alvo.getVidaMaxima() + ")");
                    }
                }
            }
            default -> System.out.println("  ✖ Este item não pode ser usado diretamente em combate.");
        }
    }

    // ── Interface Equipavel ───────────────────────────────────

    @Override
    public void equipar(PersonagemBase p) {
        p.setAtaque(p.getAtaque() + bonusAtaque);
        p.setDefesa(p.getDefesa() + bonusDefesa);
        p.setBonusCritico(p.getChanceCritico() + bonusCritico);
        p.setBonusEsquiva(p.getChanceEsquiva() + bonusEsquiva);
        System.out.println("  🗡 " + p.getNome() + " equipou " + nome + "! " + descricao);
    }

    @Override
    public void desequipar(PersonagemBase p) {
        p.setAtaque(p.getAtaque() - bonusAtaque);
        p.setDefesa(p.getDefesa() - bonusDefesa);
        p.setBonusCritico(p.getChanceCritico() - bonusCritico);
        p.setBonusEsquiva(p.getChanceEsquiva() - bonusEsquiva);
        System.out.println("  " + p.getNome() + " desequipou " + nome + ".");
    }

    @Override public String getNomeItem() { return nome; }
    @Override public int    getPreco()    { return preco; }

    public TipoItem getTipo()      { return tipo; }
    public String   getDescricao() { return descricao; }
    public int      getValorCura() { return valorCura; }
    public boolean  isConsumivel() { return tipo == TipoItem.CONSUMIVEL; }

    @Override
    public String toString() {
        return String.format("%-22s %-28s %dG", nome, "(" + descricao + ")", preco);
    }
}
