# 🎓 Corrida do TCC — Entrega Urgente!

> Projeto desenvolvido para a disciplina de **Testes de Software**. Um jogo em Java onde o jogador deve navegar por um labirinto e entregar o TCC ao professor antes que os passos acabem.

---

## 📋 Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Como Jogar](#como-jogar)
- [Arquitetura do Código](#arquitetura-do-código)
- [Testes](#testes)
- [Estratégias de Teste](#estratégias-de-teste)

---

## Sobre o Projeto

**Corrida do TCC** é um jogo 2D de labirinto desenvolvido em Java com interface gráfica Swing. O jogador controla um estudante que precisa navegar por um mapa de **10×10 células** e encontrar o professor para entregar o TCC, tudo isso dentro de um limite de **25 passos**. Células bloqueadas representam obstáculos (mesas), e o objetivo é traçar o caminho até a célula do professor antes que os passos se esgotem.

O projeto foi desenvolvido como base de código para aplicação de técnicas de teste de software, incluindo testes de domínio, fronteira, estruturais e MC/DC.

---

## Estrutura do Projeto

```
SoftwareTest-Project-main/
└── codebase-project/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/st/project/
        │   │   ├── Main.java              # Ponto de entrada da aplicação
        │   │   ├── Game.java              # Lógica central do jogo
        │   │   ├── Room.java              # Representa cada célula do mapa
        │   │   └── JogoTCCVisual.java     # Interface gráfica (Swing)
        │   └── resources/images/
        │       ├── chao.png               # Sprite do chão
        │       ├── mesa.png               # Sprite das mesas (obstáculos)
        │       ├── professor.png          # Sprite do professor (objetivo)
        │       └── jogador.png            # Sprite do jogador
        └── test/
            └── java/st/project/
                ├── GameTest.java          # Testes da lógica principal
                ├── RoomTest.java          # Testes da entidade Room
                ├── MainTest.java          # Testes do ponto de entrada
                └── VisualTest.java        # Testes da interface gráfica
```

---

## Tecnologias Utilizadas

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 11+ | Linguagem principal |
| Java Swing | — | Interface gráfica |
| Maven | 3.x | Gerenciamento de dependências e build |
| JUnit Jupiter | 5.12.2 | Framework de testes |
| AssertJ | 3.27.7 | Asserções fluentes nos testes |

---

## Pré-requisitos

- **Java JDK 11** ou superior instalado
- **Apache Maven 3.x** instalado
- Variável de ambiente `JAVA_HOME` configurada corretamente

Para verificar as versões instaladas:

```bash
java -version
mvn -version
```

---

## Como Executar

**1. Clone o repositório:**

```bash
git clone https://github.com/seu-usuario/SoftwareTest-Project.git
cd SoftwareTest-Project/codebase-project
```

**2. Compile o projeto:**

```bash
mvn compile
```

**3. Execute o jogo:**

```bash
mvn exec:java -Dexec.mainClass="st.project.Main"
```

**4. Para rodar os testes:**

```bash
mvn test
```

---

## Como Jogar

Ao iniciar, uma janela gráfica será aberta com o mapa do labirinto.

| Tecla | Ação |
|---|---|
| `↑` ou `W` | Mover para o Norte |
| `↓` ou `S` | Mover para o Sul |
| `←` ou `A` | Mover para o Oeste |
| `→` ou `D` | Mover para o Leste |

**Legenda do mapa:**

- 🟫 **Mesa** — Obstáculo intransponível (parede)
- 👨‍🏫 **Professor** — Destino final (objetivo)
- 🧑‍🎓 **Jogador** — Posição atual do estudante

**Regras:**

- O jogador começa no canto inferior esquerdo do mapa.
- Cada movimento válido consome **1 passo**.
- Tentar mover para uma célula bloqueada **não consome passos**.
- O jogador tem **25 passos** para alcançar o professor.
- **Vitória:** chegar à sala do professor com passos restantes (inclusive com 0).
- **Derrota:** os passos chegam a zero sem ter alcançado o professor.
- Após o fim do jogo (vitória ou derrota), a partida é reiniciada automaticamente.

**Alerta visual:** quando restam 5 ou menos passos, o contador no topo fica vermelho.

---

## Arquitetura do Código

### `Room.java`

Representa cada célula navegável do mapa. Armazena as coordenadas `(x, y)`, se a célula contém o professor, e um mapa de saídas (`north`, `south`, `east`, `west`) para as células vizinhas.

```
Room
 ├── posX, posY         : coordenadas no grid
 ├── isProfessor        : true se for a célula do professor
 └── exits (HashMap)    : referências para as salas vizinhas
```

### `Game.java`

Núcleo da lógica do jogo. Responsável por:

- Interpretar a matriz do mapa (`0` = livre, `1` = parede, `2` = professor)
- Construir o grafo de `Room`s com suas conexões
- Controlar o número de passos restantes
- Processar movimentos do jogador
- Determinar os estados de vitória e derrota

**Regra de prioridade:** a vitória tem precedência absoluta sobre a derrota — estar na sala do professor com 0 passos ainda é vitória.

**Spawn do jogador:** o jogo tenta iniciar o jogador na célula `[linha_final][0]` (canto inferior esquerdo). Caso seja uma parede, um mecanismo de *fallback* busca a primeira célula navegável disponível de baixo para cima.

### `JogoTCCVisual.java`

Interface gráfica construída com Java Swing. Responsável por:

- Renderizar o mapa com sprites PNG (com fallback para formas geométricas caso as imagens não carreguem)
- Capturar eventos de teclado e repassar ao `Game`
- Exibir o contador de passos (com alerta visual ao chegar em 5 ou menos)
- Exibir diálogos de vitória e derrota via `JOptionPane`
- Reiniciar a partida automaticamente após o fim de jogo

### `Main.java`

Ponto de entrada. Inicia a interface gráfica na *Event Dispatch Thread* (EDT) do Swing usando `SwingUtilities.invokeLater`, garantindo thread-safety.

---

## Testes

O projeto conta com **4 classes de teste** cobrindo diferentes camadas e estratégias:

### `RoomTest.java`

Valida o comportamento básico da entidade `Room`:

- Criação correta com coordenadas e flag de professor
- Adição e recuperação de saídas (exits)
- Sobrescrita de saída existente

### `MainTest.java`

Cobre a classe de entrada da aplicação:

- Instanciação do construtor padrão sem erros
- Execução do método `main()` sem lançar exceções, com sincronização da EDT do Swing

### `GameTest.java`

Classe principal de testes, organizada em três categorias:

#### Testes de Domínio

| Teste | O que valida |
|---|---|
| Movimentos para caminhos livres | Todas as 4 direções alteram a sala e consomem 1 passo |
| Movimento para sala do professor | Reconhece vitória ao entrar na sala correta |
| Movimento contra paredes | Tentativa inválida não gasta passos |
| Entradas anômalas | Strings inválidas, vazias ou `null` são tratadas como parede |
| Fim de jogo ignora movimento | Nenhuma ação é processada após vitória ou derrota |
| Inicialização do mapa | Tradução correta de `0`, `1`, `2` e mecanismo de spawn fallback |
| Reinício do jogo | `iniciarJogo()` restaura passos e posição de origem |

#### Testes de Fronteira

| Teste | O que valida |
|---|---|
| 1 passo restante em sala comum | Jogo ainda ativo (nem vitória nem derrota) |
| 0 passos em sala comum | Derrota imediata |
| Passos negativos em sala comum | Estado de derrota mantido |
| Jogo criado com 0 passos | Inicia já em estado de derrota |
| 0 passos na sala do professor | **Vitória** tem prioridade (não é derrota) |
| 1 passo na sala do professor | Vitória reconhecida |
| Passos negativos na sala do professor | Vitória ainda mantida (regra da sala prevalece) |

#### Testes Estruturais e MC/DC

| Teste | O que valida |
|---|---|
| Mapa 3×3 | Garante que todas as ramificações Norte/Sul/Leste/Oeste do `criarSalas()` são executadas |
| Fallback com múltiplas linhas | Loop de fallback percorre mais de uma linha ao buscar spawn |
| `isVitoria()` com sala nula | Curto-circuito do `&&` retorna falso |
| `isVitoria()` com sala não-professor | Segunda condição do `&&` retorna falso |
| `isDerrota()` com passos | Curto-circuito do `<=` retorna falso |
| `isDerrota()` sem passos e sem vitória | Retorna verdadeiro |
| Vizinhos que são paredes | Cobre ramificações `True && False` do mapeamento de saídas |
| Mapa 100% fechado | Loop de fallback percorre todas as linhas sem encontrar sala (currentRoom = null) |

### `VisualTest.java`

Testa a camada de apresentação via reflexão Java para acessar métodos e campos privados:

| Teste | O que valida |
|---|---|
| Todas as teclas e flag `moveu` | Cobre todos os `if/else if` de teclas, incluindo tecla inválida (ramo `false` de `moveu`) |
| Cobertura total do `paintComponent` | Renderização com sprites nulos, vazios e válidos; sala atual nula |
| Mudança de cor do label de status | Cor normal (> 5 passos) e cor de alerta (≤ 5 passos) |
| Derrota e vitória com popup e bloqueio de teclas | Exibe `JOptionPane`, fecha via `Timer`, testa retorno prematuro em `processarTecla` |

---

## Estratégias de Teste

O projeto aplica de forma deliberada as seguintes técnicas da disciplina de Testes de Software:

- **Teste de Domínio:** validação dos comportamentos esperados dentro das regras do jogo
- **Teste de Fronteira (Boundary Value Analysis):** verificação dos limites críticos de passos (0, 1, -1) e suas combinações com os estados de vitória/derrota
- **Teste Estrutural (White-box):** exercício direto das ramificações internas do código (`if/else`, loops, condições compostas)
- **MC/DC (Modified Condition/Decision Coverage):** cada condição atômica das expressões booleanas de `isVitoria()` e `isDerrota()` é testada de forma independente
