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
    public  static final int XP_PENALIDADE_DESCANSO = 20;

    // RF07 — custo base de cada atributo em XP
    public static final int CUSTO_XP_ATAQUE  = 30;
    public static final int CUSTO_XP_DEFESA  = 25;
    public static final int CUSTO_XP_VIDA    = 20;
    public static final int CUSTO_XP_ENERGIA = 20;

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

    /** RF07 — Menu interativo de gasto de XP em atributos. */
    public void menuGastoXP(PersonagemBase jogador, Scanner scanner) {
        if (xpDisponivel <= 0) {
            System.out.println("  Nenhum XP disponivel para gastar.");
            return;
        }
        boolean saiu = false;
        while (!saiu && xpDisponivel > 0) {
            System.out.println("\n  +====================================+");
            System.out.println("  |      DISTRIBUIR PONTOS DE XP       |");
            System.out.println("  +====================================+");
            System.out.printf ("  | XP Disponivel: %-19d|%n", xpDisponivel);
            System.out.println("  +------------------------------------+");
            System.out.printf ("  | 1. Ataque  (+3)  custo %3d XP     |%n", CUSTO_XP_ATAQUE);
            System.out.printf ("  | 2. Defesa  (+2)  custo %3d XP     |%n", CUSTO_XP_DEFESA);
            System.out.printf ("  | 3. Vida    (+20) custo %3d XP     |%n", CUSTO_XP_VIDA);
            System.out.printf ("  | 4. Energia (+15) custo %3d XP     |%n", CUSTO_XP_ENERGIA);
            System.out.println("  | 5. Confirmar e sair                |");
            System.out.println("  +====================================+");
            System.out.print("  Escolha: ");
            try {
                int op = Integer.parseInt(scanner.nextLine().trim());
                switch (op) {
                    case 1:
                        if (gastarXPDisponivel(CUSTO_XP_ATAQUE)) jogador.melhorarAtaqueComXP(3);
                        else System.out.println("  X XP insuficiente! (precisa " + CUSTO_XP_ATAQUE + ")");
                        break;
                    case 2:
                        if (gastarXPDisponivel(CUSTO_XP_DEFESA)) jogador.melhorarDefesaComXP(2);
                        else System.out.println("  X XP insuficiente! (precisa " + CUSTO_XP_DEFESA + ")");
                        break;
                    case 3:
                        if (gastarXPDisponivel(CUSTO_XP_VIDA)) jogador.melhorarVidaComXP(20);
                        else System.out.println("  X XP insuficiente! (precisa " + CUSTO_XP_VIDA + ")");
                        break;
                    case 4:
                        if (gastarXPDisponivel(CUSTO_XP_ENERGIA)) jogador.melhorarEnergiaComXP(15);
                        else System.out.println("  X XP insuficiente! (precisa " + CUSTO_XP_ENERGIA + ")");
                        break;
                    case 5:
                        saiu = true;
                        break;
                    default:
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

    /** RN06 — Incrementa streak ao vencer sem descansar. */
    public void registrarVitoriaStreak() {
        streakAtual++;
        if (streakAtual > 1) {
            multiplicadorXP = 1.0 + (streakAtual - 1) * 0.5;
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

    public void penalizarDescanso() {
        int perda = Math.min(xpAtual, XP_PENALIDADE_DESCANSO);
        xpAtual -= perda;
        xpDisponivel = Math.max(0, xpDisponivel - perda);
        if (xpAtual < 0) xpAtual = 0;
        String log = "-" + perda + " XP (descanso) | Total: " + xpAtual + "/" + xpParaProximoNivel;
        historicoAcoes.add(log);
        System.out.println("  [!] " + log);
        resetarStreak();
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
