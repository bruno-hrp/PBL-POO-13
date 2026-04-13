package rpg;

import java.util.Scanner;

public class Loja {

    // Catálogo fixo de itens disponíveis
    private static final Item[] CATALOGO = {
        Item.pocaoVida(),
        Item.pocaoVidaGrande(),
        Item.pocaoEnergia(),
        Item.antiveneno(),
        Item.espadaAfiada(),
        Item.espadaLendaria(),
        Item.armaduraReforcada(),
        Item.mantoSombrio(),
        Item.amuletoGuerreiro(),
        Item.aneelCritico()
    };

    public static void abrir(PersonagemBase jogador, Inventario inventario,
                              int[] ouro, Scanner scanner) {
        boolean naLoja = true;
        while (naLoja) {
            exibirCatalogo(ouro[0]);
            System.out.print("  Escolha (0 = sair): ");
            try {
                int opcao = Integer.parseInt(scanner.nextLine().trim());
                if (opcao == 0) {
                    naLoja = false;
                } else if (opcao >= 1 && opcao <= CATALOGO.length) {
                    Item item = CATALOGO[opcao - 1];
                    comprar(item, jogador, inventario, ouro, scanner);
                } else {
                    System.out.println("  ✖ Opção inválida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("  ✖ Digite um número válido.");
            } catch (Exception e) {
                System.out.println("  ✖ Erro: " + e.getMessage());
            } finally {
                System.out.println();
            }
        }
    }

    private static void comprar(Item item, PersonagemBase jogador,
                                 Inventario inventario, int[] ouro, Scanner scanner) {
        System.out.println("\n  Item: " + item.getNomeItem() + " — " + item.getDescricao());
        System.out.println("  Preço: " + item.getPreco() + "G  |  Seu ouro: " + ouro[0] + "G");

        if (ouro[0] < item.getPreco()) {
            System.out.println("  ✖ Ouro insuficiente!");
            return;
        }

        System.out.print("  Confirmar compra? (s/n): ");
        try {
            String resp = scanner.nextLine().trim().toLowerCase();
            if (resp.equals("s")) {
                // Cria nova instância para evitar referência compartilhada
                Item novo = clonarItem(item);
                if (inventario.adicionarItem(novo)) {
                    ouro[0] -= item.getPreco();
                    System.out.println("  ✔ Compra realizada! Ouro restante: " + ouro[0] + "G");

                    // Equipáveis são equipados imediatamente se não for consumível
                    if (!novo.isConsumivel()) {
                        System.out.print("  Deseja equipar agora? (s/n): ");
                        String eq = scanner.nextLine().trim().toLowerCase();
                        if (eq.equals("s")) {
                            int idx = inventario.getItens().size() - 1;
                            inventario.equiparItem(idx, jogador);
                        }
                    }
                }
            } else {
                System.out.println("  Compra cancelada.");
            }
        } catch (Exception e) {
            System.out.println("  ✖ Erro na compra: " + e.getMessage());
        }
    }

    private static Item clonarItem(Item original) {
        // Recria o item do catálogo para garantir instância nova
        switch (original.getNomeItem()) {
            case "Poção de Vida":         return Item.pocaoVida();
            case "Poção de Vida Grande":  return Item.pocaoVidaGrande();
            case "Poção de Energia":      return Item.pocaoEnergia();
            case "Antídoto":              return Item.antiveneno();
            case "Espada Afiada":         return Item.espadaAfiada();
            case "Espada Lendária":       return Item.espadaLendaria();
            case "Armadura Reforçada":    return Item.armaduraReforcada();
            case "Manto Sombrio":         return Item.mantoSombrio();
            case "Amuleto do Guerreiro":  return Item.amuletoGuerreiro();
            case "Anel do Assassino":     return Item.aneelCritico();
            default:                      return original;
        }
    }

    private static void exibirCatalogo(int ouro) {
        System.out.println("\n  ╔══════════════════════════════════════════════╗");
        System.out.printf ("  ║  🏪  LOJA DO AVENTUREIRO    Ouro: %-6dG   ║%n", ouro);
        System.out.println("  ╠══╦═══════════════════════╦══════════════════╣");
        System.out.println("  ║Nº║ Item                  ║ Descrição        ║");
        System.out.println("  ╠══╬═══════════════════════╬══════════════════╣");
        for (int i = 0; i < CATALOGO.length; i++) {
            Item it = CATALOGO[i];
            System.out.printf("  ║%-2d║ %-21s ║ %-13s%3dG║%n",
                    i + 1, it.getNomeItem(), it.getDescricao().substring(0,
                    Math.min(13, it.getDescricao().length())), it.getPreco());
        }
        System.out.println("  ╚══╩═══════════════════════╩══════════════════╝");
    }
}
