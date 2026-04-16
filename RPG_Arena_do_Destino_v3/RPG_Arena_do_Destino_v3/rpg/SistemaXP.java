package rpg;

import java.util.ArrayList;
import java.util.Scanner;

public class SistemaXP {

    private int xpAtual;
    private int xpParaProximoNivel;
    private int batalhasVencidas;
    private int batalhasTotais;
    private ArrayList<String> historicoAcoes;

    // RF07 — XP acumulado disponível para gastar manualmente
    private int xpDisponivel;

    // RN06 — Streak multiplier
    private int streakAtual;
    private double multiplicadorXP;

    private static final int XP_BASE_NIVEL          = 100;
    public  static final int XP_PENALIDADE_DESCANSO = 0; // Removido gasto de XP ao descansar

    // RF07 — custo base de cada atributo em XP (escalam com nível/streak)
    public static final int CUSTO_XP_ATAQUE    = 30;
    public static final int CUSTO_XP_DEFESA    = 25;
    public static final int CUSTO_XP_VIDA      = 20;
    public static final int CUSTO_XP_ENERGIA   = 20;
    public static final int CUSTO_XP_AGILIDADE = 22;

    public SistemaXP() {
        this.xpAtual            = 0;
        this.xpParaProximoNivel = XP_BASE_NIVEL;
        this.batalhasVencidas   = 0;
        this.batalhasTotais     = 0;
        this.historicoAcoes     = new ArrayList<String>();
        this.xpDisponivel       = 0;
        this.streakAtual        = 0;
        this.multiplicadorXP    = 1.0;
    }

    /**
     * Calcula o custo real de um atributo considerando nível do personagem e multiplicador de streak.
     * Balanceamento: nível alto e streak alta = custo maior para cada melhoria.
     */
    public int calcularCustoReal(int custoBase, int nivelPersonagem) {
        double fatorNivel  = 1.0 + (nivelPersonagem - 1) * 0.10;
        double fatorStreak = multiplicadorXP;
        return (int) Math.ceil(custoBase * fatorNivel * fatorStreak);
    }

    /**
     * RF07 + RN06: aplica multiplicador e acumula XP disponível para gasto manual.
     */
    public boolean ganharXP(int quantidade, PersonagemBase personagem) {
        int xpReal = (int)(quantidade * multiplicadorXP);
        xpAtual += xpReal;
        xpDisponivel += xpReal;
        String mult = multiplicadorXP > 1.0 ? String.format(" (x%.1f streak!)", multiplicadorXP) : "";
        String log = "+" + xpReal + " XP" + mult + " | Disponivel: " + xpDisponivel;
        historicoAcoes.add(log);
        System.out.println("  [XP] " + log);
        if (xpAtual >= xpParaProximoNivel) {
            System.out.println("  [!] XP suficiente para subir de nivel! Distribua seus pontos no menu.");
            return true;
        }
        return false;
    }

    /** RF07 — Tenta subir de nível se tiver XP suficiente. */
    public boolean tentarSubirNivel(PersonagemBase personagem) {
        if (xpAtual >= xpParaProximoNivel) {
            xpAtual -= xpParaProximoNivel;
            xpParaProximoNivel = (int)(xpParaProximoNivel * 1.5);
            personagem.subirNivel();
            return true;
        }
        return false;
    }

    /** RF07 — Menu interativo de gasto de XP em atributos, com opção de múltiplos níveis de uma vez. */
    public void menuGastoXP(PersonagemBase jogador, Scanner scanner) {
        if (xpDisponivel <= 0) {
            System.out.println("  Nenhum XP disponivel para gastar.");
            return;
        }
        boolean saiu = false;
        while (!saiu && xpDisponivel > 0) {
            int nivel     = jogador.getNivel();
            int custoAtq  = calcularCustoReal(CUSTO_XP_ATAQUE,    nivel);
            int custoDef  = calcularCustoReal(CUSTO_XP_DEFESA,    nivel);
            int custoVid  = calcularCustoReal(CUSTO_XP_VIDA,      nivel);
            int custoNrg  = calcularCustoReal(CUSTO_XP_ENERGIA,   nivel);
            int custoAgi  = calcularCustoReal(CUSTO_XP_AGILIDADE, nivel);

            System.out.println("\n  +========================================+");
            System.out.println("  |       DISTRIBUIR PONTOS DE XP          |");
            System.out.println("  +========================================+");
            System.out.printf ("  | XP Disponivel: %-23d|%n", xpDisponivel);
            System.out.printf ("  | Nivel: %-6d        Streak: x%-6d|%n", nivel, streakAtual);
            System.out.println("  +----------------------------------------+");
            System.out.printf ("  | 1. Ataque    (+3 por vez)  custo %5d  |%n", custoAtq);
            System.out.printf ("  | 2. Defesa    (+2 por vez)  custo %5d  |%n", custoDef);
            System.out.printf ("  | 3. Vida      (+20 por vez) custo %5d  |%n", custoVid);
            System.out.printf ("  | 4. Energia   (+15 por vez) custo %5d  |%n", custoNrg);
            System.out.printf ("  | 5. Agilidade (+2%% por vez) custo %5d  |%n", custoAgi);
            System.out.println("  | 6. Confirmar e sair                     |");
            System.out.println("  +========================================+");
            System.out.print("  Escolha: ");
            try {
                int op = Integer.parseInt(scanner.nextLine().trim());
                if (op >= 1 && op <= 5) {
                    int custoUnitario;
                    String nomeAtrib;
                    switch (op) {
                        case 1: custoUnitario = custoAtq; nomeAtrib = "Ataque";    break;
                        case 2: custoUnitario = custoDef; nomeAtrib = "Defesa";    break;
                        case 3: custoUnitario = custoVid; nomeAtrib = "Vida";      break;
                        case 4: custoUnitario = custoNrg; nomeAtrib = "Energia";   break;
                        default: custoUnitario = custoAgi; nomeAtrib = "Agilidade";
                    }

                    int maxNiveis = xpDisponivel / custoUnitario;
                    if (maxNiveis <= 0) {
                        System.out.println("  X XP insuficiente! (precisa " + custoUnitario + ")");
                        continue;
                    }

                    System.out.printf("  Voce pode upar %s ate %d vez(es) agora.%n", nomeAtrib, maxNiveis);
                    System.out.print("  Quantas vezes deseja upar? (1-" + maxNiveis + ", 0=cancelar): ");
                    int vezes;
                    try {
                        vezes = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("  X Entrada invalida.");
                        continue;
                    }
                    if (vezes <= 0) continue;
                    if (vezes > maxNiveis) {
                        System.out.println("  X Ajustado para o maximo possivel: " + maxNiveis + ".");
                        vezes = maxNiveis;
                    }

                    int custoTotal = custoUnitario * vezes;
                    if (gastarXPDisponivel(custoTotal)) {
                        switch (op) {
                            case 1: jogador.melhorarAtaqueComXP(3 * vezes);     break;
                            case 2: jogador.melhorarDefesaComXP(2 * vezes);     break;
                            case 3: jogador.melhorarVidaComXP(20 * vezes);      break;
                            case 4: jogador.melhorarEnergiaComXP(15 * vezes);   break;
                            case 5: jogador.melhorarAgilidadeComXP(2 * vezes);  break;
                        }
                    }
                } else if (op == 6) {
                    saiu = true;
                } else {
                    System.out.println("  X Opcao invalida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  X Entrada invalida.");
            }
        }
        if (xpDisponivel <= 0) System.out.println("  Todo XP foi distribuido!");
    }

    private boolean gastarXPDisponivel(int custo) {
        if (xpDisponivel < custo) return false;
        xpDisponivel -= custo;
        xpAtual = Math.max(0, xpAtual - custo);
        historicoAcoes.add("Gastou " + custo + " XP em atributo. Restante: " + xpDisponivel);
        return true;
    }

    /** RN06 — Incrementa streak ao vencer sem descansar. Bônus de 20% por nível de streak (era 40%). */
    public void registrarVitoriaStreak() {
        streakAtual++;
        if (streakAtual > 1) {
            multiplicadorXP = 1.0 + (streakAtual - 1) * 0.20;
            System.out.printf("  [STREAK x%d] Multiplicador de XP: %.1fx!%n", streakAtual, multiplicadorXP);
        }
    }

    /** RN06 — Descansar reseta streak. */
    public void resetarStreak() {
        if (streakAtual > 1)
            System.out.println("  [STREAK] Streak de " + streakAtual + " resetado. Multiplicador volta para 1.0x.");
        streakAtual     = 0;
        multiplicadorXP = 1.0;
    }

    /** Descansar não gasta XP, apenas reseta streak. */
    public void penalizarDescanso() {
        resetarStreak();
        System.out.println("  [DESCANSO] Nenhum XP perdido. Streak resetada.");
    }

    public void registrarBatalha(boolean vitoria) {
        batalhasTotais++;
        if (vitoria) batalhasVencidas++;
        historicoAcoes.add("Batalha " + batalhasTotais + ": " + (vitoria ? "VITORIA" : "DERROTA"));
    }

    public void registrarAcao(String acao) { historicoAcoes.add(acao); }

    public void exibirHistorico() {
        System.out.println("\n  == HISTORICO (ultimas 10 acoes) ==");
        int inicio = Math.max(0, historicoAcoes.size() - 10);
        for (int i = inicio; i < historicoAcoes.size(); i++)
            System.out.println("  " + (i + 1) + ". " + historicoAcoes.get(i));
    }

    public String barraXP() {
        int blocos = 15;
        int cheios = xpParaProximoNivel > 0
                ? (int)((double) xpAtual / xpParaProximoNivel * blocos) : blocos;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < blocos; i++) sb.append(i < cheios ? "=" : ".");
        return sb.append("] ").append(xpAtual).append("/").append(xpParaProximoNivel).toString();
    }

    public int    getXpAtual()            { return xpAtual; }
    public int    getXpDisponivel()       { return xpDisponivel; }
    public int    getXpParaProximoNivel() { return xpParaProximoNivel; }
    public int    getBatalhasVencidas()   { return batalhasVencidas; }
    public int    getBatalhasTotais()     { return batalhasTotais; }
    public int    getStreakAtual()        { return streakAtual; }
    public double getMultiplicadorXP()   { return multiplicadorXP; }
    public ArrayList<String> getHistorico() { return historicoAcoes; }
}
