package rpg;

import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    // RF02 — Calabouço: 5 inimigos normais antes do boss
    private static final int INIMIGOS_POR_CALABOUCO = 5;

    public static void main(String[] args) {
        exibirTela();
        iniciarJogo();
        System.out.println("\n  Obrigado por jogar! Ate a proxima aventura.");
        scanner.close();
    }

    /** Inicializa e roda o loop principal do jogo. Permite reiniciar após game over. */
    private static void iniciarJogo() {
        PersonagemBase jogador    = criarPersonagem();
        SistemaXP      sistemaXP  = new SistemaXP();
        Inventario     inventario = new Inventario();
        int[]          ouro       = { jogador.getOuro() };

        System.out.println("\n  Bem-vindo, " + jogador.getNome() + "!");
        System.out.println("  " + jogador);
        pausa(800);

        int     calabouco = 1;
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n  ==========================================");
            System.out.printf ("  CALABOUCO #%d  |  Vitorias:%d%n",
                    calabouco, sistemaXP.getBatalhasVencidas());
            System.out.println("  ==========================================");
            System.out.println("  Prepare-se! 5 inimigos aguardam...");
            pausa(600);

            boolean sobreviveu = true;
            for (int pos = 1; pos <= INIMIGOS_POR_CALABOUCO && sobreviveu; pos++) {
                Bioma bioma = Bioma.aleatorio();

                System.out.println("\n  ------------------------------------------");
                System.out.printf ("  [%d/%d] %s %s%n", pos, INIMIGOS_POR_CALABOUCO,
                        bioma.getIcone(), bioma.getNome());
                System.out.println("  ------------------------------------------");

                jogador.restaurarParaBatalha(false);

                Inimigo inimigo = Inimigo.criarDosBioma(bioma, jogador.getNivel());
                ClassePersonagem[] classes = ClassePersonagem.values();
                inimigo.setClasseInimigo(classes[(int)(Math.random() * classes.length)]);

                System.out.printf("  Um %s [%s] apareceu! (XP:%d | Ouro:%dG)%n",
                        inimigo.getNome(), inimigo.getClasseInimigo().getNome(),
                        inimigo.getXpRecompensa(), inimigo.getOuroRecompensa());
                pausa(500);

                Combate combate = new Combate(jogador, inimigo, sistemaXP, inventario, scanner, bioma);
                boolean venceu  = combate.executar();
                ouro[0] = jogador.getOuro();

                if (venceu) {
                    sistemaXP.registrarVitoriaStreak();
                    sistemaXP.tentarSubirNivel(jogador);

                    if (pos < INIMIGOS_POR_CALABOUCO) {
                        continuar = menuEntreBatalhas(jogador, inventario, sistemaXP, ouro);
                        if (!continuar) { sobreviveu = false; break; }
                    }
                } else {
                    sistemaXP.exibirHistorico();
                    sobreviveu = false;
                    continuar  = false;
                    // Game Over — pergunta se quer tentar novamente
                    if (menuGameOver(sistemaXP)) {
                        iniciarJogo(); // reinicia recursivamente
                        return;
                    }
                }
            }

            // ── RF03: Boss counter-classe ──────────────────────
            if (sobreviveu && continuar) {
                System.out.println("\n  ==========================================");
                System.out.println("  *** BOSS DO CALABOUCO #" + calabouco + " ***");
                System.out.println("  ==========================================");
                pausa(800);

                jogador.restaurarParaBatalha(false);

                Inimigo boss = Inimigo.criarBossCounterClasse(jogador.getNivel(), jogador.getClasse());

                System.out.printf("  %s aparece! (XP:%d | Ouro:%dG)%n",
                        boss.getNome(), boss.getXpRecompensa(), boss.getOuroRecompensa());
                pausa(500);

                Bioma biomaFinal = Bioma.aleatorio();
                Combate combateBoss = new Combate(jogador, boss, sistemaXP, inventario, scanner, biomaFinal);
                boolean venceuBoss  = combateBoss.executar();
                ouro[0] = jogador.getOuro();

                if (venceuBoss) {
                    sistemaXP.registrarVitoriaStreak();
                    sistemaXP.tentarSubirNivel(jogador);
                    calabouco++;
                    continuar = menuPosBoss(jogador, inventario, sistemaXP, ouro);
                } else {
                    sistemaXP.exibirHistorico();
                    continuar = false;
                    if (menuGameOver(sistemaXP)) {
                        iniciarJogo();
                        return;
                    }
                }
            }
        }
    }

    // ── Game Over ────────────────────────────────────────────

    /** Exibe o menu de game over e retorna true se o jogador quer tentar novamente. */
    private static boolean menuGameOver(SistemaXP sistemaXP) {
        System.out.println("\n  +==========================================+");
        System.out.println("  |                                          |");
        System.out.println("  |            ** GAME OVER **               |");
        System.out.println("  |                                          |");
        System.out.printf ("  |  Batalhas: %-7d  Vitorias: %-7d   |%n",
                sistemaXP.getBatalhasTotais(), sistemaXP.getBatalhasVencidas());
        System.out.println("  |                                          |");
        System.out.println("  +==========================================+");
        System.out.println("  | 1. Tentar Novamente                      |");
        System.out.println("  | 2. Encerrar o Jogo                       |");
        System.out.println("  +==========================================+");
        System.out.print("  Escolha: ");
        try {
            int op = Integer.parseInt(scanner.nextLine().trim());
            return op == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── Criação de personagem ─────────────────────────────────

    private static PersonagemBase criarPersonagem() {
        System.out.println("\n  +==================================+");
        System.out.println("  |      CRIACAO DE PERSONAGEM       |");
        System.out.println("  +==================================+");
        System.out.println("  | 1. Guerreiro                     |");
        System.out.println("  |    Vida:120 ATQ:28 DEF:15        |");
        System.out.println("  |    Habilidade: Furia Berserker   |");
        System.out.println("  |    Vantagem vs: Arqueiro         |");
        System.out.println("  +----------------------------------+");
        System.out.println("  | 2. Mago                          |");
        System.out.println("  |    Vida:85  ATQ:35 DEF:8         |");
        System.out.println("  |    Habilidade: Tempestade Arcana |");
        System.out.println("  |    Vantagem vs: Guerreiro        |");
        System.out.println("  +----------------------------------+");
        System.out.println("  | 3. Arqueiro                      |");
        System.out.println("  |    Vida:100 ATQ:30 DEF:10        |");
        System.out.println("  |    Habilidade: Chuva de Flechas  |");
        System.out.println("  |    Vantagem vs: Mago             |");
        System.out.println("  +==================================+");
        System.out.println("  Triangulo: Guerreiro>Arqueiro>Mago>Guerreiro");

        int classe = 0;
        while (classe < 1 || classe > 3) {
            System.out.print("  Escolha sua classe (1-3): ");
            try {
                classe = Integer.parseInt(scanner.nextLine().trim());
                if (classe < 1 || classe > 3)
                    System.out.println("  X Opcao invalida! Digite 1, 2 ou 3.");
            } catch (NumberFormatException e) {
                System.out.println("  X Entrada invalida! Digite um numero.");
            }
        }

        String nome = "";
        while (nome.trim().isEmpty()) {
            System.out.print("  Nome do personagem: ");
            try {
                nome = scanner.nextLine().trim();
                if (nome.isEmpty()) System.out.println("  X Nome nao pode ser vazio!");
                if (nome.length() > 15) {
                    nome = nome.substring(0, 15);
                    System.out.println("  > Nome truncado para: " + nome);
                }
            } catch (Exception e) {
                System.out.println("  X Erro: " + e.getMessage());
                nome = "";
            }
        }

        switch (classe) {
            case 1:  return new Guerreiro(nome);
            case 2:  return new Mago(nome);
            case 3:  return new Arqueiro(nome);
            default: return new Guerreiro(nome);
        }
    }

    // ── Menu entre batalhas (dentro do calabouço) ─────────────

    private static boolean menuEntreBatalhas(PersonagemBase jogador, Inventario inventario,
                                              SistemaXP sistemaXP, int[] ouro) {
        System.out.println("\n  +----------------------------------+");
        System.out.println("  |       ENTRE INIMIGOS             |");
        System.out.println("  +----------------------------------+");
        System.out.println("  | 1. Proximo Inimigo               |");
        System.out.println("  | 2. Descansar (vida 100%)         |");
        System.out.println("  | 3. Abrir Loja                    |");
        System.out.println("  | 4. Ver Inventario                |");
        System.out.printf ("  | 5. Gastar XP [%3d disp.]         |%n", sistemaXP.getXpDisponivel());
        System.out.println("  | 6. Estatisticas                  |");
        System.out.println("  | 7. Status do Personagem          |");
        System.out.println("  | 8. Encerrar Jogo                 |");
        System.out.printf ("  | Streak: x%d | Mult: %.1fx           |%n",
                sistemaXP.getStreakAtual(), sistemaXP.getMultiplicadorXP());
        System.out.println("  +----------------------------------+");
        System.out.print("  Escolha: ");

        int opcao = 1;
        try {
            opcao = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  X Invalido. Seguindo para proximo inimigo.");
        } finally {
            System.out.println();
        }

        switch (opcao) {
            case 1: return true;
            case 2:
                System.out.println("  " + jogador.getNome() + " descansa...");
                sistemaXP.penalizarDescanso();
                jogador.restaurarParaBatalha(true);
                return true;
            case 3:
                Loja.abrir(jogador, inventario, ouro, scanner);
                return menuEntreBatalhas(jogador, inventario, sistemaXP, ouro);
            case 4:
                inventario.exibir();
                gerenciarInventario(jogador, inventario);
                return menuEntreBatalhas(jogador, inventario, sistemaXP, ouro);
            case 5:
                sistemaXP.menuGastoXP(jogador, scanner);
                return menuEntreBatalhas(jogador, inventario, sistemaXP, ouro);
            case 6:
                exibirEstatisticas(jogador, sistemaXP);
                return menuEntreBatalhas(jogador, inventario, sistemaXP, ouro);
            case 7:
                exibirStatusPersonagem(jogador);
                return menuEntreBatalhas(jogador, inventario, sistemaXP, ouro);
            case 8: return false;
            default:
                System.out.println("  X Opcao invalida.");
                return true;
        }
    }

    // ── Menu pós-boss ────────────────────────────────────────

    private static boolean menuPosBoss(PersonagemBase jogador, Inventario inventario,
                                        SistemaXP sistemaXP, int[] ouro) {
        System.out.println("\n  +----------------------------------+");
        System.out.println("  |    BOSS DERROTADO! PARABENS!     |");
        System.out.println("  +----------------------------------+");
        System.out.println("  | 1. Proximo Calabouco             |");
        System.out.println("  | 2. Descansar (vida 100%)         |");
        System.out.println("  | 3. Abrir Loja                    |");
        System.out.println("  | 4. Ver Inventario                |");
        System.out.printf ("  | 5. Gastar XP [%3d disp.]         |%n", sistemaXP.getXpDisponivel());
        System.out.println("  | 6. Estatisticas                  |");
        System.out.println("  | 7. Status do Personagem          |");
        System.out.println("  | 8. Encerrar Jogo                 |");
        System.out.println("  +----------------------------------+");
        System.out.print("  Escolha: ");

        int opcao = 1;
        try {
            opcao = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  X Invalido. Seguindo para proximo calabouco.");
        } finally {
            System.out.println();
        }

        switch (opcao) {
            case 1: return true;
            case 2:
                System.out.println("  " + jogador.getNome() + " descansa profundamente...");
                sistemaXP.penalizarDescanso();
                jogador.restaurarParaBatalha(true);
                return true;
            case 3:
                Loja.abrir(jogador, inventario, ouro, scanner);
                return menuPosBoss(jogador, inventario, sistemaXP, ouro);
            case 4:
                inventario.exibir();
                gerenciarInventario(jogador, inventario);
                return menuPosBoss(jogador, inventario, sistemaXP, ouro);
            case 5:
                sistemaXP.menuGastoXP(jogador, scanner);
                return menuPosBoss(jogador, inventario, sistemaXP, ouro);
            case 6:
                exibirEstatisticas(jogador, sistemaXP);
                return menuPosBoss(jogador, inventario, sistemaXP, ouro);
            case 7:
                exibirStatusPersonagem(jogador);
                return menuPosBoss(jogador, inventario, sistemaXP, ouro);
            case 8: return false;
            default:
                System.out.println("  X Opcao invalida.");
                return true;
        }
    }

    private static void gerenciarInventario(PersonagemBase jogador, Inventario inventario) {
        if (inventario.estaVazio()) { System.out.println("  Inventario vazio."); return; }
        System.out.print("  Equipar item? Numero do item (0 = cancelar): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx > 0) inventario.equiparItem(idx - 1, jogador);
        } catch (NumberFormatException e) {
            System.out.println("  X Numero invalido.");
        }
    }

    /** Tela de status detalhado de todos os atributos do personagem. */
    private static void exibirStatusPersonagem(PersonagemBase jogador) {
        System.out.println("\n  +====================================+");
        System.out.println("  |       STATUS DO PERSONAGEM         |");
        System.out.println("  +====================================+");
        System.out.printf ("  | Nome       : %-21s|%n", jogador.getNome());
        System.out.printf ("  | Nivel      : %-21d|%n", jogador.getNivel());
        System.out.printf ("  | Classe     : %-21s|%n", jogador.getClasse().getNome());
        System.out.println("  +------------------------------------+");
        System.out.printf ("  | Vida Atual : %-21d|%n", jogador.getVida());
        System.out.printf ("  | Vida Max   : %-21d|%n", jogador.getVidaMaxima());
        System.out.printf ("  | Energia    : %d/%-18d|%n", jogador.getEnergia(), jogador.getEnergiaMaxima());
        System.out.println("  +------------------------------------+");
        System.out.printf ("  | Ataque     : %-21d|%n", jogador.getAtaque());
        System.out.printf ("  | Defesa     : %-21d|%n", jogador.getDefesa());
        System.out.printf ("  | Agilidade  : %-20d%%|%n", jogador.getChanceEsquiva());
        System.out.printf ("  | Critico    : %-20d%%|%n", jogador.getChanceCritico());
        System.out.println("  +------------------------------------+");
        System.out.printf ("  | Ouro       : %-20dG|%n", jogador.getOuro());
        System.out.printf ("  | Status     : %-21s|%n", jogador.getStatus().getNome());
        System.out.println("  +====================================+");
    }

    private static void exibirEstatisticas(PersonagemBase jogador, SistemaXP sistemaXP) {
        System.out.println("\n  +==================================+");
        System.out.println("  |          ESTATISTICAS            |");
        System.out.println("  +==================================+");
        System.out.printf ("  | Personagem : %-19s|%n", jogador.getNome());
        System.out.printf ("  | Nivel      : %-19d|%n", jogador.getNivel());
        System.out.printf ("  | Classe     : %-19s|%n", jogador.getClasse().getNome());
        System.out.printf ("  | Ataque     : %-19d|%n", jogador.getAtaque());
        System.out.printf ("  | Defesa     : %-19d|%n", jogador.getDefesa());
        System.out.printf ("  | Esquiva    : %-18d%%|%n", jogador.getChanceEsquiva());
        System.out.printf ("  | Critico    : %-18d%%|%n", jogador.getChanceCritico());
        System.out.printf ("  | Ouro       : %-18dG|%n", jogador.getOuro());
        System.out.println("  +----------------------------------+");
        System.out.printf ("  | XP         : %-19s|%n", sistemaXP.barraXP());
        System.out.printf ("  | XP Disp.   : %-19d|%n", sistemaXP.getXpDisponivel());
        System.out.printf ("  | Streak     : %-17dx  |%n", sistemaXP.getStreakAtual());
        System.out.printf ("  | Mult. XP   : %-17.1fx  |%n", sistemaXP.getMultiplicadorXP());
        System.out.printf ("  | Vitorias   : %-19d|%n", sistemaXP.getBatalhasVencidas());
        System.out.printf ("  | Batalhas   : %-19d|%n", sistemaXP.getBatalhasTotais());
        System.out.println("  +==================================+");
        sistemaXP.exibirHistorico();
    }

    private static void exibirTela() {
        System.out.println("  +==========================================+");
        System.out.println("  |                                          |");
        System.out.println("  |        R P G  -  A R E N A              |");
        System.out.println("  |         DO    DESTINO                    |");
        System.out.println("  |                                          |");
        System.out.println("  |      O              [###]                |");
        System.out.println("  |     /|\\              | X |               |");
        System.out.println("  |     / \\             |___|               |");
        System.out.println("  |   Heroi           IA Inimiga            |");
        System.out.println("  |                                          |");
        System.out.println("  |  === HEROI   VS   COMPUTADOR ===         |");
        System.out.println("  |                                          |");
        System.out.println("  +==========================================+");
        System.out.println();
    }

    private static void pausa(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
