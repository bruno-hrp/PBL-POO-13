package rpg;

import java.util.Scanner;

public class Combate {

    private PersonagemBase jogador;
    private Inimigo inimigo;
    private SistemaXP sistemaXP;
    private Inventario inventario;
    private Scanner scanner;
    private int turnoAtual;
    private Bioma bioma;

    private static final double BONUS_VANTAGEM_CLASSE = 1.20; // RN05: +20% dano

    public Combate(PersonagemBase jogador, Inimigo inimigo, SistemaXP sistemaXP,
                   Inventario inventario, Scanner scanner, Bioma bioma) {
        this.jogador    = jogador;
        this.inimigo    = inimigo;
        this.sistemaXP  = sistemaXP;
        this.inventario = inventario;
        this.scanner    = scanner;
        this.bioma      = bioma;
        this.turnoAtual = 1;
    }

    public boolean executar() {
        exibirInicioCombate();
        exibirVantagemClasse(); // RN05
        while (jogador.estaVivo() && inimigo.estaVivo()) {
            exibirStatus();
            System.out.println("\n  -------- TURNO " + turnoAtual + " --------");

            // Processa status do jogador antes do turno
            boolean jogadorParalisado = jogador.processarStatus();

            if (!jogadorParalisado) {
                turnoJogador();
            }

            if (!inimigo.estaVivo()) break;

            pausa(300);
            inimigo.executarTurno(jogador);

            turnoAtual++;
            pausa(200);
        }
        return resolverFim();
    }

    // ── Turno do jogador ─────────────────────────────────────

    private void turnoJogador() {
        boolean acaoValida = false;
        while (!acaoValida) {
            exibirMenuAcoes();
            try {
                int opcao = Integer.parseInt(scanner.nextLine().trim());
                switch (opcao) {
                    case 1:
                        jogador.atacar(inimigo);
                        sistemaXP.registrarAcao("T" + turnoAtual + ": Atacou " + inimigo.getNome());
                        acaoValida = true;
                        break;
                    case 2:
                        jogador.defender();
                        sistemaXP.registrarAcao("T" + turnoAtual + ": Defendeu");
                        acaoValida = true;
                        break;
                    case 3:
                        if (jogador instanceof HabilidadeEspecial) {
                            ((HabilidadeEspecial) jogador).usarHabilidade(inimigo);
                            sistemaXP.registrarAcao("T" + turnoAtual + ": Usou habilidade especial");
                        } else {
                            System.out.println("  X Sem habilidade especial!");
                        }
                        acaoValida = true;
                        break;
                    case 4:
                        if (jogador instanceof Recuperavel) {
                            ((Recuperavel) jogador).recuperar();
                            sistemaXP.registrarAcao("T" + turnoAtual + ": Recuperou");
                        } else {
                            System.out.println("  X Nao pode recuperar!");
                        }
                        acaoValida = true;
                        break;
                    case 5:
                        usarItemCombate();
                        acaoValida = true;
                        break;
                    case 6:
                        sistemaXP.exibirHistorico();
                        break;
                    default:
                        System.out.println("  X Opcao invalida! Escolha de 1 a 6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  X Entrada invalida! Digite um numero.");
            } catch (Exception e) {
                System.out.println("  X Erro: " + e.getMessage());
            } finally {
                if (!acaoValida) System.out.println("  > Tente novamente.");
            }
        }
    }

    private void usarItemCombate() {
        if (inventario.estaVazio() || !inventario.temConsumivel()) {
            System.out.println("  X Nenhum item consumivel no inventario!");
            return;
        }
        inventario.exibir();
        System.out.print("  Numero do item a usar (0 = cancelar): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx == 0) return;
            inventario.usarConsumivel(idx - 1, jogador);
        } catch (NumberFormatException e) {
            System.out.println("  X Numero invalido.");
        }
    }

    // ── Menus e exibição ─────────────────────────────────────

    private void exibirMenuAcoes() {
        String nomeHab = "Habilidade Especial";
        if (jogador instanceof HabilidadeEspecial)
            nomeHab = ((HabilidadeEspecial) jogador).getNomeHabilidade();

        System.out.println("\n  +------------------------------+");
        System.out.println("  |      ESCOLHA SUA ACAO        |");
        System.out.println("  +------------------------------+");
        System.out.println("  | 1. Atacar                    |");
        System.out.println("  | 2. Defender                  |");
        System.out.printf ("  | 3. %-26s |%n", nomeHab + " (-" + HabilidadeEspecial.CUSTO_HABILIDADE + "E)");
        System.out.printf ("  | 4. %-26s |%n", "Recuperar (-" + Recuperavel.CUSTO_RECUPERAR + "E)");
        System.out.printf ("  | 5. %-26s |%n", "Usar Item [" + inventario.getTamanho() + " itens]");
        System.out.println("  | 6. Ver Historico             |");
        System.out.println("  +------------------------------+");
        System.out.print("  Escolha: ");
    }

    private void exibirVantagemClasse() {
        ClassePersonagem cJog = jogador.getClasse();
        ClassePersonagem cIni = inimigo.getClasseInimigo();
        if (cJog.temVantagemSobre(cIni)) {
            System.out.printf("  [VANTAGEM] %s > %s — seu dano e +20%% neste combate!%n",
                    cJog.getNome(), cIni.getNome());
        } else if (cIni.temVantagemSobre(cJog)) {
            System.out.printf("  [DESVANTAGEM] %s > %s — o inimigo causa +20%% de dano!%n",
                    cIni.getNome(), cJog.getNome());
        }
    }

    /**
     * RN05 — Calcula dano com bônus de vantagem de classe.
     * Se o atacante tem vantagem, aplica +0% no dano bruto antes da defesa.
     */
    public static int aplicarVantagemClasse(int danoBase, ClassePersonagem atacante, ClassePersonagem defensor) {
        if (atacante.temVantagemSobre(defensor)) {
            return (int)(danoBase * 1.10);
        }
        return danoBase;
    }

    private void exibirInicioCombate() {
        System.out.println("\n  " + bioma.getIcone() + "  " + bioma.getNome().toUpperCase());
        System.out.println();
        // Sprites lado a lado
        String[] spJog = jogador.getSprite().split("\n");
        String[] spIni = inimigo.getSprite().split("\n");
        int linhas = Math.max(spJog.length, spIni.length);
        System.out.println("  ==========================================");
        for (int i = 0; i < linhas; i++) {
            String l = i < spJog.length ? spJog[i] : "         ";
            String r = i < spIni.length ? spIni[i] : "         ";
            System.out.printf("  %-15s    VS    %-15s%n", l, r);
        }
        System.out.println("  ==========================================");
        if (inimigo.isEhBoss())
            System.out.println("  *** BATALHA DE BOSS! Prepare-se! ***");
        System.out.println();
        pausa(600);
    }

    private void exibirStatus() {
        String statusJog = jogador.getStatus() != StatusAlterado.NENHUM
                ? " [" + jogador.getStatus().getNome() + "]" : "";
        String statusIni = inimigo.getStatus() != StatusAlterado.NENHUM
                ? " [" + inimigo.getStatus().getNome() + "]" : "";

        System.out.println("\n  ----------- STATUS -----------");
        System.out.printf ("  Heroi  : %s Nv.%d%s%n", jogador.getNome(), jogador.getNivel(), statusJog);
        System.out.println("  Vida   : " + jogador.barraVida());
        System.out.println("  Energia: " + jogador.barraEnergia());
        System.out.println("  XP     : " + sistemaXP.barraXP());
        System.out.println("  Ouro   : " + jogador.getOuro() + "G");
        System.out.println();
        System.out.printf ("  Inimigo: %s%s%n", inimigo.getNome(), statusIni);
        System.out.println("  Vida   : " + inimigo.barraVidaInimigo());
        System.out.println("  Energia: " + inimigo.barraEnergia());
        System.out.println("  ------------------------------");
    }

    private boolean resolverFim() {
        System.out.println();
        if (jogador.estaVivo()) {
            System.out.println("  +============================+");
            System.out.println("  |      *** VITORIA! ***      |");
            System.out.printf ("  | %-26s |%n", jogador.getNome() + " venceu!");
            System.out.println("  +============================+");
            sistemaXP.registrarBatalha(true);
            int ouro = inimigo.getOuroRecompensa();
            jogador.adicionarOuro(ouro);
            System.out.println("  [+] +" + ouro + "G de ouro! Total: " + jogador.getOuro() + "G");
            sistemaXP.ganharXP(inimigo.getXpRecompensa(), jogador);
            return true;
        } else {
            System.out.println("  +============================+");
            System.out.println("  |      --- DERROTA...        |");
            System.out.printf ("  | %-26s |%n", jogador.getNome() + " foi derrotado!");
            System.out.println("  +============================+");
            sistemaXP.registrarBatalha(false);
            return false;
        }
    }

    private void pausa(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
