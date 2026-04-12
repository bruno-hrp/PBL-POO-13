# PBL-POO-13
PBL 13 - Jogo de RPG

Instruções
PROJETO INTEGRADOR — DESENVOLVIMENTO DE UM JOGO RPG EM JAVA COM ORIENTAÇÃO A OBJETOS



Você foi contratado para desenvolver um jogo RPG executado via console em Java, no qual o jogador enfrenta o próprio sistema (computador) como adversário. O jogo deve simular um combate estratégico entre um personagem controlado pelo usuário e um inimigo automatizado, com regras claras de ataque, defesa, uso de habilidades e evolução. Todo o sistema deve obrigatoriamente utilizar os conceitos de orientação a objetos estudados: objetos, atributos, construtor, encapsulamento com getters e setters, métodos, herança, polimorfismo, classes abstratas, interfaces, vetores de objetos e tratamento de exceções com try, catch e finally.



O jogo começa com a criação do personagem pelo usuário, que deve escolher uma classe entre Guerreiro, Mago ou Arqueiro. Cada uma dessas classes deve herdar de uma classe abstrata base (por exemplo, PersonagemBase), que contém atributos comuns como nome, vida, ataque, defesa e energia, além de métodos como receberDano() e estaVivo(). O método atacar() deve ser abstrato e obrigatoriamente sobrescrito em cada subclasse, garantindo polimorfismo em tempo de execução. Interfaces também devem ser utilizadas para definir comportamentos adicionais, como HabilidadeEspecial (com método usarHabilidade) e Recuperavel (com método recuperar), sendo implementadas de forma diferente por cada classe.



Durante a execução, o jogador enfrentará um inimigo controlado pelo computador. Esse inimigo também deve ser modelado como objeto e pode ser instanciado a partir de subclasses da mesma hierarquia, permitindo reaproveitamento e polimorfismo. O comportamento do inimigo deve ser automático, com decisões simples como atacar, usar habilidade ou recuperar, simulando uma inteligência básica.



O combate ocorre em turnos alternados. Em cada turno, o jogador escolhe uma ação via menu (obrigatoriamente implementado com switch-case), enquanto o computador executa sua ação automaticamente. O sistema deve validar todas as entradas do usuário e tratar erros com try-catch-finally, garantindo que o jogo não seja interrompido por falhas.



A lógica de combate deve seguir a regra: dano = ataque - defesa do alvo, com dano mínimo igual a 1. A energia deve ser consumida ao executar ações especiais, e o sistema deve impedir ações caso não haja energia suficiente. O jogo termina quando a vida do jogador ou do inimigo chega a zero.



A seguir, um exemplo de ambientação em ASCII para contextualizar o jogo:



Jogador:

  O
  /|\
  / \
Herói em combate


Inimigo (Computador):

 [###] 
  | X | 
 |___|
IA inimiga


Campo de batalha:


===========================
   HERÓI  VS  COMPUTADOR 
===========================


Durante o jogo, o status deve ser exibido constantemente:


--- STATUS ---
 Herói: Vida = 80 | Energia = 30
 Inimigo: Vida = 65 | Energia = 20
 ----------------

As ações disponíveis ao jogador devem incluir: atacar, defender (reduz dano no próximo turno), usar habilidade especial (com maior custo de energia) e recuperar (vida ou energia). O inimigo deve tomar decisões automaticamente com base em regras simples (por exemplo, atacar sempre que possível, usar habilidade quando tiver energia suficiente).



O sistema deve utilizar vetores (arrays) para armazenar possíveis inimigos, histórico de ações ou personagens disponíveis, podendo o uso de ArrayList ou outras coleções. Todos os atributos devem ser privados, com acesso controlado por getters e setters. O encapsulamento deve garantir integridade dos dados, impedindo valores inválidos como vida negativa.



O tratamento de exceções deve ser amplamente utilizado: leitura de dados do usuário, escolha de ações inválidas, acesso a posições inexistentes em vetores e cálculos indevidos devem ser protegidos com try-catch-finally. O sistema deve continuar funcionando mesmo após erros.



O jogo deve ser totalmente funcional via console, com estrutura modularizada, uso consistente de orientação a objetos e comportamento dinâmico baseado em polimorfismo. A avaliação considerará a correta aplicação dos conceitos, robustez do sistema, coerência das regras e organização do código.