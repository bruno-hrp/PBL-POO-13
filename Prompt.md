# Prompt — RPG Arena do Destino (Java, Console)

Crie um jogo RPG de turno em Java puro, sem bibliotecas externas, rodando 100% no terminal.
O pacote de todos os arquivos é `rpg`. O ponto de entrada é `Main.java`.
Siga rigorosamente cada requisito abaixo.

---

## Visão Geral

O jogador escolhe uma classe (Guerreiro, Mago ou Arqueiro) e percorre **calabouços**.
Cada calabouço tem **5 inimigos normais em sequência** seguidos de **1 boss**.
Ao vencer inimigos o jogador acumula XP para gastar manualmente em atributos, ouro para comprar itens na loja, e mantém um streak de vitórias que multiplica o XP ganho.
O boss de cada calabouço é sempre da **classe que vence a do jogador**, criando tensão estratégica real.

---

## Arquitetura — 18 arquivos

```
rpg/
  ClassePersonagem.java   ← enum novo
  PersonagemBase.java     ← classe abstrata base
  Guerreiro.java
  Mago.java
  Arqueiro.java
  Inimigo.java
  Combate.java
  SistemaXP.java
  Inventario.java
  Item.java
  Loja.java
  Bioma.java
  StatusAlterado.java
  HabilidadeEspecial.java ← interface
  Recuperavel.java        ← interface
  Equipavel.java          ← interface
  TipoItem.java           ← enum
  Main.java
```

---

## 1. ClassePersonagem.java

Enum com três valores: `GUERREIRO`, `MAGO`, `ARQUEIRO`.

Método `boolean temVantagemSobre(ClassePersonagem outro)`:
- Guerreiro vence Arqueiro
- Arqueiro vence Mago
- Mago vence Guerreiro

Método `String getNome()` retorna o nome legível da classe.

---

## 2. Interfaces

### HabilidadeEspecial
```java
int CUSTO_HABILIDADE = 30;
void usarHabilidade(PersonagemBase alvo);
String getNomeHabilidade();
```

### Recuperavel
```java
int CUSTO_RECUPERAR = 20;
int CURA_BASE       = 30;
void recuperar();
```

### Equipavel
```java
void equipar(PersonagemBase p);
void desequipar(PersonagemBase p);
String getNomeItem();
int getPreco();
```

---

## 3. StatusAlterado.java

Enum: `NENHUM`, `ENVENENADO`, `PARALISADO`, `QUEIMANDO`, `AMALDICOADO`.
Cada valor tem `nome` (String) e `icone` (String com emoji).
Getters: `getNome()`, `getIcone()`.

---

## 4. TipoItem.java

Enum: `CONSUMIVEL`, `ARMA`, `ARMADURA`, `ACESSORIO`.

---

## 5. Bioma.java

Enum com 5 biomas: `FLORESTA`, `MASMORRA`, `VULCAO`, `CEMITERIO`, `CASTELO`.
Cada bioma tem `nome`, `icone` (emoji) e `indicesInimigos` (int[]) que aponta
para linhas de `Inimigo.TEMPLATES`.

Mapeamento sugerido:
- FLORESTA  → índices {0,1}   (Goblin, Orc)
- MASMORRA  → índices {2,4}   (Mago das Trevas, Necromante)
- VULCAO    → índices {3,5}   (Dragão Jovem, Troll)
- CEMITERIO → índices {4,2}
- CASTELO   → índices {1,3,5}

Método estático `Bioma aleatorio()`.
Getters: `getNome()`, `getIcone()`, `getIndicesInimigos()`.

---

## 6. PersonagemBase.java

Classe abstrata com os campos privados:
`nome`, `vidaMaxima`, `vida`, `ataque`, `defesa`, `energiaMaxima`, `energia`,
`nivel`, `defendendo`, `chanceEsquiva`, `chanceCritico`, `status` (StatusAlterado),
`turnosStatus`, `ouro`, `classe` (ClassePersonagem).

`MULTIPLICADOR_CRITICO = 2.0`

### Construtor
Inicializa todos os campos. `nivel = 1`, `status = NENHUM`, `classe = GUERREIRO` (padrão).
Subclasses devem sobrescrever via `protected void setClasse(ClassePersonagem c)`.

### Métodos abstratos
- `void atacar(PersonagemBase alvo)`
- `String getSprite()`

### Método de dano — RF05 / RN05
```java
// Wrapper simples (sem classe atacante)
public void receberDano(int danoAtaque, boolean ehCritico) {
    receberDanoDeClasse(danoAtaque, ehCritico, null);
}

// Versão com vantagem de classe (RN05)
public void receberDanoDeClasse(int danoAtaque, boolean ehCritico, ClassePersonagem classeAtacante) {
    // 1. Tentativa de esquiva
    // 2. Se classeAtacante != null && classeAtacante.temVantagemSobre(this.classe) → danoAtaque *= 1.20
    // 3. defesaEfetiva = defendendo ? defesa*2 : defesa
    // 4. dano = max(1, danoAtaque - defesaEfetiva)
    // 5. Se ehCritico → dano *= MULTIPLICADOR_CRITICO, exibir "GOLPE CRITICO"
    // 6. vida -= dano; exibir resultado; defendendo = false
}
```

### Outros métodos
- `aplicarStatus(StatusAlterado, int turnos)` — só aplica se status == NENHUM
- `boolean processarStatus()` — aplica efeito do turno; retorna true se paralisado (perde turno)
  - ENVENENADO: dano = vidaMaxima/10
  - QUEIMANDO: dano = vidaMaxima/8
  - PARALISADO: perde turno, decrementa contador
  - AMALDICOADO: ataque -= 2 (mín 1)
- `defender()` — seta `defendendo = true`
- `boolean tentarEsquiva()` — random < chanceEsquiva
- `boolean tentarCritico()` — random < chanceCritico
- `boolean estaVivo()` — vida > 0
- `boolean consumirEnergia(int custo)`
- `subirNivel()` — incrementa nível; +15 vida, +3 ataque, +2 defesa, +10 energia; restaura tudo
- `restaurarParaBatalha(boolean completo)` — completo=true restaura 100%; false restaura 40%
- `barraVida()` — barra ASCII 20 blocos
- `barraEnergia()` — barra ASCII 10 blocos

**RF07 — métodos de melhoria por XP:**
```java
void melhorarAtaqueComXP(int ganho)   // setAtaque(ataque + ganho)
void melhorarDefesaComXP(int ganho)   // setDefesa(defesa + ganho)
void melhorarVidaComXP(int ganho)     // vidaMaxima += ganho; vida += ganho (cap vidaMaxima)
void melhorarEnergiaComXP(int ganho)  // energiaMaxima += ganho; energia += ganho (cap)
```

Todos os getters e setters necessários.

---

## 7. Guerreiro.java

Extends `PersonagemBase`, implements `HabilidadeEspecial`, `Recuperavel`.
`super(nome, 120, 28, 15, 80, 10, 20)` — vida, ataque, defesa, energia, esquiva%, crítico%
`setClasse(ClassePersonagem.GUERREIRO)`

- `atacar`: "desfere um golpe de espada!"; 15% chance ENVENENADO 2 turnos; passa `getClasse()` para `receberDanoDeClasse`
- `usarHabilidade` ("Furia Berserker"): custo 30E; dano = ataque * 1.8; 30% chance PARALISADO 1 turno
- `recuperar` ("Segunda Fôlego"): custo 20E; cura = CURA_BASE + 10
- `getSprite()`: sprite ASCII do guerreiro

---

## 8. Mago.java

`super(nome, 85, 35, 8, 120, 15, 30)`
`setClasse(ClassePersonagem.MAGO)`

- `atacar`: "lança uma Bola de Fogo!"; 20% chance QUEIMANDO 3 turnos
- `usarHabilidade` ("Tempestade Arcana"): custo 30E; dano = ataque * 2.2; 35% chance AMALDICOADO 3 turnos
- `recuperar` ("Meditação"): custo 20E; restaura 40 de energia (não vida)
- `getSprite()`: sprite ASCII do mago

---

## 9. Arqueiro.java

`super(nome, 100, 30, 10, 100, 25, 25)`
`setClasse(ClassePersonagem.ARQUEIRO)`

- `atacar`: "dispara uma flecha precisa!"; 25% chance ENVENENADO 3 turnos
- `usarHabilidade` ("Chuva de Flechas"): custo 30E; 3 hits de dano = ataque * 0.8 cada; cada hit tenta crítico
- `recuperar` ("Ervas"): custo 20E; +15 vida e +20 energia
- `getSprite()`: sprite ASCII do arqueiro

---

## 10. Inimigo.java

Extends `PersonagemBase`, implements `HabilidadeEspecial`, `Recuperavel`.
Campo extra: `String tipo`, `int xpRecompensa`, `int ouroRecompensa`, `boolean ehBoss`, `ClassePersonagem classe`.

### Templates de inimigos normais (6 entradas)
```
// {nome, vida, ataque, defesa, energia, esquiva%, critico%, xp, ouro}
{"Goblin Selvagem",      60,  18,  5,  60, 20, 10,  30,  15}
{"Orc Guerreiro",       100,  25, 12,  70,  8, 15,  55,  25}
{"Mago das Trevas",      75,  32,  6, 110, 12, 28,  70,  35}
{"Dragão Jovem",        150,  38, 18,  90, 15, 25, 120,  60}
{"Necromante",           90,  30, 10, 130, 10, 22,  90,  45}
{"Troll das Montanhas", 130,  22, 20,  50,  5, 10,  80,  40}
```

### Templates de boss (3 entradas)
```
{"BOSS: Rei Lich",         300, 45, 20, 150, 10, 30, 200, 150}
{"BOSS: Dragão Ancião",    350, 50, 25, 120, 15, 35, 250, 200}
{"BOSS: Senhor das Trevas",280, 55, 15, 180, 20, 40, 220, 175}
```

Todos os stats escalam com `escala = 1.0 + (nivelJogador - 1) * 0.25`.

### Métodos de fábrica
- `criarAleatorio(int nivel)`
- `criarDosBioma(Bioma bioma, int nivel)` — sorteia entre os índices do bioma
- `criarBoss(int nivel)` — boss aleatório
- **RF03** `criarBossCounterClasse(int nivel, ClassePersonagem classeJogador)`:
  - Guerreiro → boss recebe classe MAGO (sufixo "Arcano")
  - Mago → boss recebe classe ARQUEIRO (sufixo "Caçador")
  - Arqueiro → boss recebe classe GUERREIRO (sufixo "Titã")
  - Renomeia o boss: `"BOSS: " + nomeBase + " " + sufixo`
  - Exibe aviso: `"[!!] O boss é da classe X — vantagem contra Y!"`

### IA do inimigo
**Normal** (`executarTurnoNormal`):
- vida < 30% e energia suficiente → recuperar
- 40% chance de habilidade se tiver energia
- 15% chance de defender
- senão → atacar; 15% chance de aplicar status aleatório (ENVENENADO/QUEIMANDO/PARALISADO) por 2 turnos

**Boss** (`executarTurnoBoss`):
- vida < 40% e energia suficiente → recuperar
- 60% chance de habilidade
- senão → atacar; aplica AMALDICOADO 3 turnos

Habilidade do inimigo normal: "Golpe Sombrio", dano = ataque * 1.7.
Habilidade do boss: "Força das Trevas", dano = ataque * 2.0, aplica AMALDICOADO.

`atacar` e `usarHabilidade` passam `getClasseInimigo()` para `receberDanoDeClasse`.

Métodos: `setClasseInimigo(ClassePersonagem)`, `getClasseInimigo()`.

---

## 11. SistemaXP.java

### Campos
```java
int xpAtual
int xpParaProximoNivel  // começa em 100, multiplica por 1.5 a cada nível
int batalhasVencidas
int batalhasTotais
ArrayList<String> historicoAcoes
int xpDisponivel        // RF07: XP acumulado aguardando ser gasto
int streakAtual         // RN06
double multiplicadorXP  // RN06: começa em 1.0
```

### Constantes públicas
```java
XP_BASE_NIVEL          = 100
XP_PENALIDADE_DESCANSO = 20
CUSTO_XP_ATAQUE        = 30   // RF07: dá +3 ataque
CUSTO_XP_DEFESA        = 25   // RF07: dá +2 defesa
CUSTO_XP_VIDA          = 20   // RF07: dá +20 vida máx
CUSTO_XP_ENERGIA       = 20   // RF07: dá +15 energia máx
```

### ganharXP (RF07 + RN06)
```java
public boolean ganharXP(int quantidade, PersonagemBase personagem) {
    int xpReal = (int)(quantidade * multiplicadorXP);
    xpAtual += xpReal;
    xpDisponivel += xpReal;
    // exibe: "+X XP (x1.5 streak!) | Disponivel: Y"
    if (xpAtual >= xpParaProximoNivel) {
        // avisa: "[!] XP suficiente para subir de nivel! Distribua seus pontos no menu."
        return true;
    }
    return false;
}
```

### tentarSubirNivel
Chamado explicitamente após cada vitória. Se `xpAtual >= xpParaProximoNivel`:
desconta, multiplica threshold por 1.5, chama `personagem.subirNivel()`.

### menuGastoXP (RF07)
Menu interativo exibido no menu pós-batalha (opção "Gastar XP"):
```
| XP Disponivel: XXX                 |
| 1. Ataque  (+3)  custo  30 XP     |
| 2. Defesa  (+2)  custo  25 XP     |
| 3. Vida    (+20) custo  20 XP     |
| 4. Energia (+15) custo  20 XP     |
| 5. Confirmar e sair               |
```
Loop até o jogador sair ou ficar sem XP.
Cada compra desconta de `xpDisponivel` E de `xpAtual`.

### RN06 — Streak
```java
public void registrarVitoriaStreak() {
    streakAtual++;
    if (streakAtual > 1) {
        multiplicadorXP = 1.0 + (streakAtual - 1) * 0.5;
        // exibe: "[STREAK x3] Multiplicador de XP: 2.0x!"
    }
}

public void resetarStreak() {
    // exibe aviso se streakAtual > 1
    streakAtual = 0;
    multiplicadorXP = 1.0;
}
```

### penalizarDescanso
Desconta `min(xpAtual, XP_PENALIDADE_DESCANSO)` de `xpAtual` e `xpDisponivel`.
Chama `resetarStreak()`.

### Outros métodos
- `registrarBatalha(boolean vitoria)`
- `registrarAcao(String acao)`
- `exibirHistorico()` — últimas 10 ações
- `barraXP()` — barra ASCII 15 blocos `[===....] X/Y`
- Getters: `getXpAtual()`, `getXpDisponivel()`, `getXpParaProximoNivel()`,
  `getBatalhasVencidas()`, `getBatalhasTotais()`, `getStreakAtual()`, `getMultiplicadorXP()`

---

## 12. Item.java

Implements `Equipavel`.
Campos: `nome`, `descricao`, `tipo` (TipoItem), `preco`, `valorCura`,
`bonusAtaque`, `bonusDefesa`, `bonusCritico`, `bonusEsquiva`.

### Itens predefinidos (fábrica estática)
```
pocaoVida()          CONSUMIVEL  30G  cura 40 vida
pocaoVidaGrande()    CONSUMIVEL  55G  cura 80 vida
pocaoEnergia()       CONSUMIVEL  25G  restaura 40 energia
antiveneno()         CONSUMIVEL  20G  cura qualquer status
espadaAfiada()       ARMA        80G  +8 ataque
espadaLendaria()     ARMA       150G  +15 ataque, +5% crítico
armaduraReforcada()  ARMADURA    80G  +8 defesa
mantoSombrio()       ARMADURA   120G  +6 defesa, +10% esquiva
amuletoGuerreiro()   ACESSORIO  100G  +5 ataque, +5 defesa
anelCritico()        ACESSORIO   90G  +15% crítico
```

Método `usar(PersonagemBase alvo)`:
- Consumíveis de vida/energia modificam o stat correspondente
- Antídoto chama `alvo.setStatus(StatusAlterado.NENHUM)`
- Equipáveis: exibe erro ("não pode ser usado diretamente em combate")

Implements `equipar` / `desequipar`: soma/subtrai os bônus no personagem.

---

## 13. Inventario.java

Campos: `ArrayList<Item> itens` (máx 10), `Item armaEquipada`, `Item armaduraEquipada`, `Item acessorioEquipado`.

Métodos:
- `adicionarItem(Item)` — retorna false se cheio
- `equiparItem(int indice, PersonagemBase)` — desequipa o slot anterior se houver; remove do inventário
- `usarConsumivel(int indice, PersonagemBase)` — remove após usar
- `exibir()` — tabela ASCII mostrando slots equipados + lista de itens com tag [Uso]/[Eqp]
- `temConsumivel()`, `estaVazio()`, `getTamanho()`, `getItens()`

---

## 14. Loja.java

Catálogo estático com todos os 10 itens de `Item.java`.
Método `abrir(PersonagemBase, Inventario, int[] ouro, Scanner)`:
- Exibe tabela formatada com número, nome, descrição, preço
- Mostra ouro atual do jogador
- Ao comprar: clona o item (instância nova), desconta ouro, oferece equipar se for equipável
- Loop até o jogador sair (opção 0)

---

## 15. Combate.java

### Campos
`jogador`, `inimigo`, `sistemaXP`, `inventario`, `scanner`, `turnoAtual`, `bioma`.

### executar()
```java
exibirInicioCombate();
exibirVantagemClasse();  // RN05: anuncia vantagem/desvantagem antes do combate
while (jogador.estaVivo() && inimigo.estaVivo()) {
    exibirStatus();
    // processa status do jogador
    if (!jogadorParalisado) turnoJogador();
    if (!inimigo.estaVivo()) break;
    inimigo.executarTurno(jogador);
    turnoAtual++;
}
return resolverFim();
```

### exibirVantagemClasse() — RN05
Compara `jogador.getClasse()` com `inimigo.getClasseInimigo()`.
- Jogador tem vantagem → "[VANTAGEM] X > Y — seu dano é +20% neste combate!"
- Inimigo tem vantagem → "[DESVANTAGEM] Y > X — o inimigo causa +20% de dano!"

### turnoJogador()
Menu com 6 opções:
1. Atacar
2. Defender
3. Habilidade Especial (exibe nome e custo de energia)
4. Recuperar (exibe custo)
5. Usar Item [N itens]
6. Ver Histórico

### resolverFim()
- Vitória: concede ouro (`jogador.adicionarOuro`), chama `sistemaXP.ganharXP`, `sistemaXP.registrarBatalha(true)`. Retorna true.
- Derrota: `sistemaXP.registrarBatalha(false)`, exibe histórico. Retorna false.

### exibirStatus()
Exibe barras de vida, energia, XP atual, ouro, status atual do jogador e do inimigo.

### exibirInicioCombate()
Sprites lado a lado com `printf` formatado, separados por "VS".
Exibe bioma. Se boss, exibe aviso especial.

---

## 16. Main.java

### Constante
```java
private static final int INIMIGOS_POR_CALABOUCO = 5;  // RF02
```

### Fluxo principal (RF02)
```
while (continuar) {
    exibe cabeçalho do calabouço N

    // 5 inimigos normais
    for (pos = 1..5) {
        Bioma bioma = Bioma.aleatorio()
        exibe "[pos/5] bioma"
        jogador.restaurarParaBatalha(false)  // recuperação parcial
        inimigo = Inimigo.criarDosBioma(bioma, nivel)
        inimigo.setClasseInimigo(classe aleatória)  // RN05
        exibe nome + classe do inimigo
        combate = new Combate(...)
        venceu = combate.executar()

        if (venceu) {
            sistemaXP.registrarVitoriaStreak()   // RN06
            sistemaXP.tentarSubirNivel(jogador)  // RF07: level-up se XP suficiente
            if (pos < 5) menuEntreBatalhas(...)  // menu entre inimigos
        } else {
            exibe game over; break
        }
    }

    // Boss (RF03)
    exibe cabeçalho de boss
    jogador.restaurarParaBatalha(false)
    boss = Inimigo.criarBossCounterClasse(nivel, jogador.getClasse())
    combate boss...
    if (venceuBoss) {
        sistemaXP.registrarVitoriaStreak()
        sistemaXP.tentarSubirNivel(jogador)
        calabouco++
        menuPosBoss(...)
    } else {
        game over
    }
}
```

### criarPersonagem()
Exibe as 3 classes com stats completos e **indica a vantagem de cada uma**
("Vantagem vs: Arqueiro", etc.).
Exibe: "Triangulo: Guerreiro > Arqueiro > Mago > Guerreiro".
Pede nome (max 15 chars).

### menuEntreBatalhas()
Opções:
1. Próximo Inimigo
2. Descansar (−XP, vida 100%) → `penalizarDescanso()` + `restaurarParaBatalha(true)`
3. Abrir Loja
4. Ver Inventário
5. Gastar XP [N disp.] → `sistemaXP.menuGastoXP(jogador, scanner)` (RF07)
6. Estatísticas
7. Encerrar Jogo

Exibe na borda do menu: `Streak: xN | Mult: 1.5x` (RN06).

### menuPosBoss()
Mesmas opções, mas opção 1 = "Próximo Calabouço".

### exibirEstatisticas()
Exibe: nome, nível, classe, ataque, defesa, esquiva%, crítico%, ouro,
barra de XP, XP disponível, streak atual, multiplicador XP, vitórias, batalhas totais.
Chama `sistemaXP.exibirHistorico()`.

---

## Requisitos Funcionais

| ID | Requisito |
|----|-----------|
| RF01 | Escolha de classe no início com stats e vantagem exibidos |
| RF02 | Fila de 5 inimigos normais antes do boss em cada calabouço |
| RF03 | Boss é sempre da classe counter à do jogador (Guerreiro→Mago boss, etc.) |
| RF04 | Combate por turnos: Atacar / Defender / Habilidade / Recuperar / Item / Histórico |
| RF05 | Sistema de status: Envenenado, Queimando, Paralisado, Amaldiçoado |
| RF06 | Loja com 10 itens (consumíveis + equipáveis) comprados com ouro |
| RF07 | XP acumulado manualmente; jogador escolhe gastar em Ataque / Defesa / Vida / Energia |
| RF08 | Level-up ao atingir threshold de XP (chamado explicitamente após vitória) |
| RF09 | Inventário com slots separados para arma, armadura, acessório + itens avulsos |
| RF10 | Biomas com inimigos temáticos; cada batalha sorteia bioma aleatório |

## Regras de Negócio

| ID | Regra |
|----|-------|
| RN01 | Dano mínimo = 1 (nunca negativo) |
| RN02 | Defesa dobrada ao usar ação Defender (dura 1 turno) |
| RN03 | Recuperação parcial de 40% de vida e energia antes de cada batalha (sem descansar) |
| RN04 | Descanso = −20 XP + restauração completa de vida/energia |
| RN05 | Triângulo de classe: quem tem vantagem aplica +20% de dano ANTES de calcular defesa |
| RN06 | Multiplicador de streak: 1ª vitória=1.0x, 2ª=1.5x, 3ª=2.0x… Descansar reseta para 1.0x |
| RN07 | Inimigos escalam com nível: todos os stats * (1.0 + (nivel-1) * 0.25) |
| RN08 | Crítico dobra o dano final (após defesa) |
| RN09 | Status só aplica se o alvo já não tiver outro status ativo |
| RN10 | Paralisado perde o turno inteiro; outros status causam efeito e decrementam contador |

---

## Estilo de Output

- Toda saída começa com 2 espaços de indentação.
- Menus usam bordas ASCII: `+===+`, `|`, `+---+`.
- Barras de vida/energia/XP: blocos de caracteres (`X`, `#`, `=`).
- Sprites do personagem e inimigo exibidos lado a lado com `printf` de largura fixa.
- `Thread.sleep()` curtos (200–800ms) para dar ritmo ao combate.
- Caracteres especiais (★ ✦ ☠ 🔥 ⚡ 💀) para destacar eventos críticos e status.
- Sem cores ANSI (compatibilidade máxima de terminal).

---

## Compilação e Execução

```bash
javac rpg/*.java
java rpg.Main
```

Sem dependências externas. Java 14+ recomendado (switch expressions com `->` em `Item.java`).
Para Java 8–13, substituir switch expressions por blocos `switch` tradicionais.