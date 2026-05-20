# Corrida do TCC 🎓

Jogo de labirinto em Java/Swing desenvolvido para a disciplina de **Testes de Software**. O jogador navega por um mapa 10×10, coletando o Cristal de Acesso, evitando alçapões e escapando da sala do professor para avançar de nível.

---

## Funcionalidades

- **Níveis ilimitados** — a cada avanço, um novo mapa é sorteado aleatoriamente entre os disponíveis no banco de mapas
- **Alçapão com recurso especial** — cada nível contém um alçapão posicionado aleatoriamente; o Cristal de Acesso (único por sessão) é necessário para atravessá-lo, caso contrário o jogador retorna ao nível anterior com penalidade de pontuação
- **Painel de status em tempo real** — exibe nível atual, pontuação total, passos restantes e status do cristal
- **Gestão de usuários** — cadastro, autenticação, avatar e exclusão de usuários; superusuário `admin` pré-cadastrado e inviolável
- **Ranking global** — pontuação máxima e sessões jogadas de cada usuário, acessível a todos os cadastrados

---

## Arquitetura

O projeto segue o padrão **Model-View-Controller (MVC)** com adaptações ao ciclo de vida do jogo.

```
src/
└── st/
    └── project/
        ├── Main.java                      ← ponto de entrada
        ├── model/
        │   ├── Game.java                  ← lógica central do jogo
        │   ├── Room.java                  ← sala individual do mapa (grafo)
        │   ├── Usuario.java               ← entidade do usuário
        │   └── GerenciadorUsuarios.java   ← Singleton; lista e autenticação
        ├── view/
        │   ├── VistaJogo.java             ← tela principal do jogo (Swing)
        │   ├── VistaLogin.java            ← tela de login e cadastro
        │   └── VistaRanking.java          ← tela de ranking
        └── controller/
            └── JogoController.java        ← captura teclas e orquestra Model/View
```

### Decisões de design para testabilidade

| Decisão | Onde | Por quê |
|---|---|---|
| Injeção de dependência via construtor package-private | `Game(Usuario u)` | Permite mockar o usuário sem passar pelo Singleton |
| Setters package-private de estado | `setCurrentRoom`, `setNivelAtual`, `setPassosRestantes` | Permite montar qualquer cenário de teste sem código aleatório |
| Getters de componentes Swing package-private | `getLabelStatus()`, `getPainelJogo()`, `getBtnRanking()` | Permite asserções sobre a View sem quebrar encapsulamento público |
| Reset do Singleton por Reflection | `GerenciadorUsuariosTest.setUp()` | Garante isolamento completo entre casos de teste |
| Invariantes anotados no código de produção | `Game.java` | Rastreabilidade bidirecional com os testes de propriedade |

---

## Regras de pontuação

| Evento | Efeito |
|---|---|
| Coletar o Cristal de Acesso | +100 pontos |
| Alcançar a saída (avançar de nível) | +200 pontos |
| Cair no alçapão sem cristal (nível > 1) | −200 pontos (mínimo 0) |
| Cair no alçapão sem cristal (nível 1) | sem penalidade |
| Passos esgotados | game over |

---

## Tecnologias utilizadas

### Produção

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Java Swing | — | Interface gráfica (janelas, painéis, eventos) |
| Java AWT | — | Renderização do mapa (`Graphics`, `Image`) |
| Maven | 3.8+ | Gerenciamento de dependências e build |

### Testes

| Biblioteca | Versão | Uso |
|---|---|---|
| JUnit 5 (Jupiter) | 5.10+ | Framework base de testes (`@Test`, `@BeforeEach`, `@DisplayName`) |
| Mockito | 5.x | Criação de mocks, stubs, `MockedStatic`, `MockedConstruction` |
| jqwik | 1.8+ | Testes baseados em propriedade (`@Property`, `@ForAll`, `@IntRange`) |
| AssertJ | 3.x | Asserções fluentes (`assertThat`, `contains`, `isGreaterThanOrEqualTo`) |
| JaCoCo | 0.8+ | Relatório de cobertura de código (MC/DC) |

---

## Pré-requisitos

Antes de clonar e executar o projeto, certifique-se de ter instalado:

- **Java 17** ou superior
  ```bash
  java -version
  # java version "17.x.x" ...
  ```
- **Maven 3.8** ou superior
  ```bash
  mvn -version
  # Apache Maven 3.x.x ...
  ```
- As dependências de teste (JUnit 5, Mockito, jqwik, AssertJ) são baixadas automaticamente pelo Maven na primeira execução. Não é necessário instalar nada manualmente além do JDK e do Maven.

> O projeto não usa banco de dados nem arquivos externos. Os dados de usuários existem apenas em memória durante a sessão.

---

## Suíte de testes

Os testes estão organizados espelhando a estrutura de produção:

```
test/
└── st/
    └── project/
        ├── MainTest.java
        ├── model/
        │   ├── GameTest.java
        │   ├── GamePropertiesTest.java
        │   ├── RoomTest.java
        │   ├── UsuarioTest.java
        │   └── GerenciadorUsuariosTest.java
        ├── view/
        │   ├── VistaJogoTest.java
        │   ├── TelaLoginTest.java
        │   └── TelaRankingTest.java
        └── controller/
            └── JogoControllerTest.java
```

### Técnicas aplicadas

**Testes de domínio** — verificam os comportamentos centrais do jogo: mover para sala livre, coletar cristal, atravessar alçapão com e sem recurso, alcançar a saída, direções inválidas, reinício de sessão e rosa dos ventos.

**Testes de fronteira** — cobrem os valores-limite críticos: passo antes do game over vs. passo que o aciona, pontuação abaixo/igual/acima da penalidade de 200 pontos, nível mínimo 1, recorde superado vs. não superado.

**Testes estruturais (MC/DC)** — cada predicado composto do código de produção tem um par de testes que varia independentemente cada condição: `gameOver`, `tipoDestino`, `temRecursoExtra`, `passosRestantes ≤ 0`, `usuarioInjetado != null`, `getSalaAtual() == null`, `getMapa() == null`.

**Testes baseados em propriedade (jqwik)** — três invariantes do sistema verificadas contra centenas de combinações geradas automaticamente:

| ID | Propriedade |
|---|---|
| PROP01 | Pontuação nunca fica negativa após cair no alçapão |
| PROP02 | Nível nunca fica abaixo de 1 após múltiplas quedas |
| PROP03 | Passos restantes nunca ficam negativos após muitos movimentos |

**Dublês de teste (Mockito)** — mocks de `Usuario`, `Game`, `VistaJogo`, `GerenciadorUsuarios` e `Room`; `MockedStatic` de `JOptionPane` para interceptar popups sem abrir janelas reais; `MockedConstruction` de `VistaRanking` para verificar que a janela de ranking é instanciada e exibida corretamente.

### Rastreabilidade

Cada teste carrega um identificador de categoria (DM, FR, ST, PROP) rastreável diretamente ao requisito que cobre. As invariantes testadas por PROP01–PROP03 estão anotadas no código de produção em `Game.java` com referência ao teste correspondente.

---

## Como executar

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/corrida-do-tcc.git
cd corrida-do-tcc
```

### 2. Compilar o projeto

```bash
mvn compile
```

### 3. Executar o jogo

```bash
mvn exec:java -Dexec.mainClass="st.project.Main"
```

Ou, se preferir gerar o `.jar` primeiro:

```bash
mvn package
java -jar target/corrida-do-tcc.jar
```

### 4. Executar pela IDE

Abra o projeto como projeto Maven no **IntelliJ IDEA** ou **Eclipse**, aguarde o download das dependências e execute a classe `Main.java` diretamente.

---

### Executando os testes

**Todos os testes:**
```bash
mvn test
```

**Apenas uma classe de teste:**
```bash
mvn test -Dtest=GameTest
```

**Apenas um método específico:**
```bash
mvn test -Dtest=GameTest#testDM01_MovimentoParaSalaComum
```

**Relatório de cobertura (JaCoCo):**
```bash
mvn verify
# relatório gerado em: target/site/jacoco/index.html
```

Abra o arquivo `index.html` no navegador para visualizar a cobertura linha a linha por classe.

---

## Controles

| Tecla | Ação |
|---|---|
| `W` ou `↑` | Mover para norte |
| `S` ou `↓` | Mover para sul |
| `A` ou `←` | Mover para oeste |
| `D` ou `→` | Mover para leste |

---

## Usuário padrão

| Login | Senha | Perfil |
|---|---|---|
| `admin` | `admin` | Superusuário — acesso ao ranking com permissão de remover usuários |