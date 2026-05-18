package st.project;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GameTest {

    private Game game;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        // Configurando o Dublê de Teste (Mock) limpo para os testes unitários
        mockUsuario = mock(Usuario.class);
        
        // Injeção de Dependência através do Construtor para Testabilidade (Slide 7)
        game = new Game(mockUsuario);
        game.iniciarSessao();
    }

    // =======================================================================
    // 1. TESTES DE DOMÍNIO E CONTRATOS 
    // =======================================================================

    @Test
    @DisplayName("Domínio: Movimento para parede (sala inexistente) não consome passos")
    void testDominio_MovimentoParaParede() {
        Room salaIsolada = new Room(0, 0, 0);
        game.setCurrentRoom(salaIsolada); 
        
        int passosIniciais = game.getPassosRestantes();
        game.mover("north");
        
        assertThat(game.getPassosRestantes()).isEqualTo(passosIniciais);
    }

    @Test
    @DisplayName("Contrato: Mover para direção nula, inválida ou sem sala atual é ignorado (Slide 4)")
    void testContrato_DirecaoInvalida() {
        int passosIniciais = game.getPassosRestantes();
        
        // Quebra de contrato 1: Direções nulas ou inexistentes
        game.mover(null);
        game.mover("diagonal_estranha");
        assertThat(game.getPassosRestantes()).isEqualTo(passosIniciais);

        // Quebra de contrato 2: Sala atual ser nula (Garante 100% de MC/DC no ||)
        game.setCurrentRoom(null);
        game.mover("north"); // Não deve lançar NullPointerException
    }

    // =======================================================================
    // 2. TESTES DE FRONTEIRAS E ESTRUTURAIS (MC/DC) 
    // =======================================================================

    @Test
    @DisplayName("Fronteira: Passos chegam exatamente a zero finaliza o jogo")
    void testFronteira_PassosZerados() {
        Room salaAtual = new Room(0, 0, 0);
        Room proximaSala = new Room(0, 1, 0);
        salaAtual.setExit("east", proximaSala);
        
        game.setCurrentRoom(salaAtual);
        game.setPassosRestantes(1); 
        
        game.mover("east");
        
        assertThat(game.getPassosRestantes()).isEqualTo(0);
        assertThat(game.isGameOver()).isTrue();
        verify(mockUsuario, atLeastOnce()).getPontuacaoMaxima(); 
    }

    @Test
    @DisplayName("MC/DC: Movimento bloqueado quando gameOver = true")
    void testEstrutural_GameOverImpedeMovimento() {
        game.setGameOver(true);
        Room salaAnterior = game.getSalaAtual();
        
        game.mover("north");
        
        assertThat(game.getSalaAtual()).isEqualTo(salaAnterior);
    }

    @Test
    @DisplayName("MC/DC: Entrar em sala com Recurso (Tipo 3) aumenta 100 pontos e concede recurso")
    void testEstrutural_PegarRecurso() {
        Room salaAtual = new Room(0, 0, 0);
        Room salaRecurso = new Room(1, 0, 3);
        salaAtual.setExit("east", salaRecurso);
        
        game.setCurrentRoom(salaAtual);
        game.setPontuacaoTotal(0);
        
        game.mover("east");
        
        assertThat(game.hasRecurso()).isTrue();
        assertThat(game.getPontuacaoTotal()).isEqualTo(100);
        assertThat(salaRecurso.getTipo()).isEqualTo(0);
    }

    @Test
    @DisplayName("MC/DC: Entrar em Alçapão (Tipo 4) COM recurso consome o recurso e avança")
    void testEstrutural_AlcapaoComRecurso() {
        Room salaAtual = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        salaAtual.setExit("east", salaAlcapao);
        
        game.setCurrentRoom(salaAtual);
        game.setTemRecursoExtra(true); 
        
        game.mover("east");
        
        assertThat(game.hasRecurso()).isFalse();
        assertThat(game.getSalaAtual()).isEqualTo(salaAlcapao);
        assertThat(salaAlcapao.getTipo()).isEqualTo(0);
    }

    @Test
    @DisplayName("MC/DC: Entrar em Alçapão SEM recurso no Nível 1 apenas reinicia o mapa atual")
    void testEstrutural_AlcapaoSemRecurso_Nivel1() {
        Room salaAtual = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        salaAtual.setExit("east", salaAlcapao);
        
        game.setCurrentRoom(salaAtual);
        game.setTemRecursoExtra(false);
        game.setNivelAtual(1);
        
        game.mover("east");
        
        assertThat(game.getNivelAtual()).isEqualTo(1);
        assertThat(game.getPassosRestantes()).isEqualTo(55); 
    }

    @Test
    @DisplayName("MC/DC: Entrar em Alçapão SEM recurso no Nível > 1 deduz pontos e desce de nível")
    void testEstrutural_AlcapaoSemRecurso_NivelMaior() {
        Room salaAtual = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        salaAtual.setExit("east", salaAlcapao);
        
        game.setCurrentRoom(salaAtual);
        game.setTemRecursoExtra(false);
        game.setNivelAtual(3);
        game.setPontuacaoTotal(500);
        
        game.mover("east");
        
        assertThat(game.getNivelAtual()).isEqualTo(2); 
        assertThat(game.getPontuacaoTotal()).isEqualTo(300); // 500 - 200
        assertThat(game.getPassosRestantes()).isEqualTo(55); 
    }

    @Test
    @DisplayName("MC/DC: Chegar à Saída (Tipo 2) aumenta nível e concede 200 pontos")
    void testEstrutural_PassarDeFase() {
        Room salaAtual = new Room(0, 0, 0);
        Room salaSaida = new Room(1, 0, 2);
        salaAtual.setExit("east", salaSaida);
        
        game.setCurrentRoom(salaAtual);
        game.setNivelAtual(1);
        game.setPontuacaoTotal(0);
        
        game.mover("east");
        
        assertThat(game.getNivelAtual()).isEqualTo(2);
        assertThat(game.getPontuacaoTotal()).isEqualTo(200);
    }

    @Test
    @DisplayName("MC/DC: finalizarJogo com pontuação MAIOR que a máxima do usuário (Verdadeiro)")
    void testEstrutural_FinalizarJogo_PontuacaoMaiorQueMaxima() {
        when(mockUsuario.getPontuacaoMaxima()).thenReturn(50);
        
        game.setPontuacaoTotal(100);
        game.setPassosRestantes(1);
        
        Room salaAtual = new Room(0, 0, 0);
        Room proximaSala = new Room(0, 1, 0);
        salaAtual.setExit("south", proximaSala);
        game.setCurrentRoom(salaAtual);
        
        game.mover("south");
        
        verify(mockUsuario).setPontuacaoMaxima(100);
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    @DisplayName("MC/DC: finalizarJogo com pontuação MENOR OU IGUAL à máxima do usuário (Falso)")
    void testEstrutural_FinalizarJogo_PontuacaoMenorOuIgualMaxima() {
        when(mockUsuario.getPontuacaoMaxima()).thenReturn(200);
        
        game.setPontuacaoTotal(100);
        game.setPassosRestantes(1);
        
        Room salaAtual = new Room(0, 0, 0);
        Room proximaSala = new Room(0, 1, 0);
        salaAtual.setExit("south", proximaSala);
        game.setCurrentRoom(salaAtual);
        
        game.mover("south");
        
        verify(mockUsuario, never()).setPontuacaoMaxima(anyInt());
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    @DisplayName("Estrutural: Cobertura do getter getMapa()")
    void testEstrutural_GetMapa() {
        int[][] mapa = game.getMapa();
        assertThat(mapa).isNotNull();
        assertThat(mapa.length).isEqualTo(10);
    }

    @Test
    @DisplayName("Estrutural: Garante que o construtor padrão utiliza o Singleton corretamente")
    void testEstrutural_ConstrutorPadrao() {
        // Cadastra e loga um usuário no Singleton de forma real
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        ger.cadastrar("testUser", "123", "avatar");
        ger.autenticar("testUser", "123");

        // Instancia o jogo usando o construtor sem injeção (cobertura total das linhas iniciais)
        Game jogoReal = new Game();
        jogoReal.iniciarSessao();
        
        assertThat(ger.getUsuarioLogado().getSessoesJogadas()).isGreaterThanOrEqualTo(1);
    }

    // =======================================================================
    // 3. TESTES DE UNIDADES EM CONJUNTO / INTEGRAÇÃO 
    // =======================================================================

    @Test
    @DisplayName("Integração: Fluxo de movimento em um mapa real validando inicialização")
    void testIntegracao_FluxoRealNoMapa() {
        Game jogoReal = new Game(mockUsuario); 
        
        // Modifica atributos para checar se o iniciar zera tudo corretamente (Mutantes)
        jogoReal.setNivelAtual(10);
        jogoReal.setGameOver(true);
        
        jogoReal.iniciarSessao();
        
        assertThat(jogoReal.getNivelAtual()).isEqualTo(1);
        assertThat(jogoReal.isGameOver()).isFalse();
        assertThat(jogoReal.getPassosRestantes()).isEqualTo(55);
        assertThat(jogoReal.getSalaAtual()).isNotNull();

        Room salaInicial = jogoReal.getSalaAtual();
        
        // Tenta achar uma saída válida do spawn ([9][0] que geralmente vai para North ou East)
        String direcaoValida = null;
        if (salaInicial.getExit("north") != null) direcaoValida = "north";
        else if (salaInicial.getExit("east") != null) direcaoValida = "east";

        if (direcaoValida != null) {
            jogoReal.mover(direcaoValida);
            assertThat(jogoReal.getPassosRestantes()).isEqualTo(54);
            assertThat(jogoReal.getSalaAtual()).isNotEqualTo(salaInicial);
        }
    }

    // =======================================================================
    // 4. TESTES BASEADOS EM PROPRIEDADE (JQWIK) 
    // =======================================================================

    @Property
    @DisplayName("Propriedade: A pontuação nunca fica negativa após cair num alçapão")
    void pontuacaoNuncaFicaNegativa(
            @ForAll @IntRange(min = 0, max = 150) int pontuacaoInicial,
            @ForAll @IntRange(min = 2, max = 5) int nivelSimulado
    ) {
        // Criação de mock local para evitar problemas com o ciclo de vida do Jqwik (ignora o BeforeEach)
        Usuario localMock = mock(Usuario.class);
        Game propGame = new Game(localMock);
        
        propGame.setNivelAtual(nivelSimulado);
        propGame.setPontuacaoTotal(pontuacaoInicial);
        propGame.setTemRecursoExtra(false);

        Room salaAtual = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        salaAtual.setExit("north", salaAlcapao);
        propGame.setCurrentRoom(salaAtual);

        propGame.mover("north");

        assertThat(propGame.getPontuacaoTotal()).isGreaterThanOrEqualTo(0);
    }
}