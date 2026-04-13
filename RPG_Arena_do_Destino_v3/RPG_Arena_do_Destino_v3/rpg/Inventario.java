package rpg;

import java.util.ArrayList;

public class Inventario {

    private ArrayList<Item> itens;
    private Item armaEquipada;
    private Item armaduraEquipada;
    private Item acessorioEquipado;
    private static final int CAPACIDADE_MAX = 10;

    public Inventario() {
        this.itens = new ArrayList<Item>();
    }

    public boolean adicionarItem(Item item) {
        if (itens.size() >= CAPACIDADE_MAX) {
            System.out.println("  ✖ Inventário cheio! (máx " + CAPACIDADE_MAX + " itens)");
            return false;
        }
        itens.add(item);
        System.out.println("  + " + item.getNomeItem() + " adicionado ao inventário.");
        return true;
    }

    public void removerItem(int indice) {
        if (indice >= 0 && indice < itens.size()) {
            itens.remove(indice);
        }
    }

    public void equiparItem(int indice, PersonagemBase personagem) {
        try {
            Item item = itens.get(indice);
            switch (item.getTipo()) {
                case ARMA -> {
                    if (armaEquipada != null) armaEquipada.desequipar(personagem);
                    armaEquipada = item;
                    item.equipar(personagem);
                    itens.remove(indice);
                }
                case ARMADURA -> {
                    if (armaduraEquipada != null) armaduraEquipada.desequipar(personagem);
                    armaduraEquipada = item;
                    item.equipar(personagem);
                    itens.remove(indice);
                }
                case ACESSORIO -> {
                    if (acessorioEquipado != null) acessorioEquipado.desequipar(personagem);
                    acessorioEquipado = item;
                    item.equipar(personagem);
                    itens.remove(indice);
                }
                default -> System.out.println("  ✖ Este item não pode ser equipado.");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("  ✖ Item inválido no inventário.");
        }
    }

    public void usarConsumivel(int indice, PersonagemBase personagem) {
        try {
            Item item = itens.get(indice);
            if (item.isConsumivel()) {
                item.usar(personagem);
                itens.remove(indice);
            } else {
                System.out.println("  ✖ Este item não é consumível. Use 'Equipar'.");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("  ✖ Índice inválido no inventário.");
        }
    }

    public void exibir() {
        System.out.println("\n  ┌─────────────────────────────────────────┐");
        System.out.println("  │              INVENTÁRIO                 │");
        System.out.println("  ├─────────────────────────────────────────┤");

        // Equipados
        System.out.printf("  │  Arma     : %-28s│%n",
                armaEquipada     != null ? armaEquipada.getNomeItem()     : "---");
        System.out.printf("  │  Armadura : %-28s│%n",
                armaduraEquipada != null ? armaduraEquipada.getNomeItem() : "---");
        System.out.printf("  │  Acessório: %-28s│%n",
                acessorioEquipado != null ? acessorioEquipado.getNomeItem(): "---");
        System.out.println("  ├────┬────────────────────────────────────┤");
        System.out.println("  │ Nº │ Item                               │");
        System.out.println("  ├────┼────────────────────────────────────┤");

        if (itens.isEmpty()) {
            System.out.println("  │         Inventário vazio               │");
        } else {
            for (int i = 0; i < itens.size(); i++) {
                Item it = itens.get(i);
                String tag = it.isConsumivel() ? "[Uso]" : "[Eqp]";
                System.out.printf("  │ %-2d │ %-5s %-28s│%n",
                        i + 1, tag, it.getNomeItem());
            }
        }
        System.out.println("  └────┴────────────────────────────────────┘");
    }

    public ArrayList<Item> getItens()  { return itens; }
    public int getTamanho()            { return itens.size(); }
    public boolean estaVazio()         { return itens.isEmpty(); }
    public Item getArmaEquipada()      { return armaEquipada; }
    public Item getArmaduraEquipada()  { return armaduraEquipada; }
    public Item getAcessorioEquipado() { return acessorioEquipado; }

    public boolean temConsumivel() {
        for (Item i : itens) if (i.isConsumivel()) return true;
        return false;
    }
}
