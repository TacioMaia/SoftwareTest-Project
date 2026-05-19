package st.project;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GameTest {

    private Game game;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        // Injeção de Dependência 
        mockUsuario = mock(Usuario.class);
        game = new Game(mockUsuario);
        game.iniciarSessao();
    }

    // =========================================================================
    // TESTES DE DOMÍNIO 
    // =========================================================================

    //Verifica que mover para uma direção válida com sala disponível consome exatamente 1 passo e troca a sala atual
    @Test
    @DisplayName("DM01 – Movimento para sala comum: consome 1 passo e muda a sala")
    void testDM01_MovimentoParaSalaComum() {
        Room origem = new Room(0, 0, 0);
        Room destino = new Room(1, 0, 0);
        origem.setExit("east", destino);

        game.setPassosRestantes(10);
        game.setCurrentRoom(origem);
        
        game.mover("east");

        assertThat(game.getSalaAtual()).isEqualTo(destino);
        assertThat(game.getPassosRestantes()).isEqualTo(9);
    }

    //Verifica que tentar mover para uma direção sem saída não faz nada. não consome passo e nem muda de sala
    @Test
    @DisplayName("DM02 – Movimento para parede (sem saída): não consome passos e não muda de lugar")
    void testDM02_MovimentoParaParede() {
        Room salaIsolada = new Room(0, 0, 0);
        game.setPassosRestantes(10);
        game.setCurrentRoom(salaIsolada); 
        
        game.mover("north");
        
        assertThat(game.getSalaAtual()).isEqualTo(salaIsolada);
        assertThat(game.getPassosRestantes()).isEqualTo(10);
    }
    // Verifica se ta tudo certo com a coleta do recurso, se soma +100 pontos, ativa a flag HasRecurso = verdadeira e muda o tipo de sala para 0 (sala comum)
    @Test
    @DisplayName("DM03 – Coletar recurso (Tipo 3): ganha 100 pontos, ativa flag e limpa a sala")
    void testDM03_ColetarRecurso() {
        Room origem = new Room(0, 0, 0);
        Room salaRecurso = new Room(1, 0, 3);
        origem.setExit("east", salaRecurso);
        
        game.setCurrentRoom(origem);
        game.setPontuacaoTotal(0);
        
        game.mover("east");
        
        assertThat(game.hasRecurso()).isTrue();
        assertThat(game.getPontuacaoTotal()).isEqualTo(100);
        assertThat(salaRecurso.getTipo()).isEqualTo(0);
    }

    // Verifica o caminho do alçapão quando o jogador tem o recurso, consegue atravessar, o HasRecurso fica falso e muda o tipo de sala para 0 (sala comum)
    @Test
    @DisplayName("DM04 – Alçapão com recurso: transpõe, consome recurso e não perde pontos")
    void testDM04_AlcapaoComRecurso() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);
        
        game.setCurrentRoom(origem);
        game.setTemRecursoExtra(true); 
        game.setPontuacaoTotal(300);
        
        game.mover("east");
        
        assertThat(game.hasRecurso()).isFalse();
        assertThat(game.getSalaAtual()).isEqualTo(salaAlcapao);
        assertThat(salaAlcapao.getTipo()).isEqualTo(0);
        assertThat(game.getPontuacaoTotal()).isEqualTo(300); // Pontuação intacta
    }

    // Verifica o que acontece se cair no alçapão sem recurso nos niveis maiores que 1. perde 200 pontos e desce para o nivel anterior
    @Test
    @DisplayName("DM05 – Alçapão sem recurso (Nível > 1): desce de nível, perde 200 pts e reseta mapa")
    void testDM05_AlcapaoSemRecursoNivelMaior() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);
        
        game.setCurrentRoom(origem);
        game.setTemRecursoExtra(false);
        game.setNivelAtual(3);
        game.setPontuacaoTotal(500);
        
        game.mover("east");
        
        assertThat(game.getNivelAtual()).isEqualTo(2); 
        assertThat(game.getPontuacaoTotal()).isEqualTo(300); // 500 - 200
        assertThat(game.getPassosRestantes()).isEqualTo(55); // Mapa recarregado
    }

    // Verifica o que acontece se cair no alçapão sem recurso no nivel 1. não aplica os -200 pontos e permanece no nivel 1
    @Test
    @DisplayName("DM06 – Alçapão sem recurso (Nível 1): permanece no nível 1 e não perde pontos")
    void testDM06_AlcapaoSemRecursoNivel1() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);
        
        game.setCurrentRoom(origem);
        game.setTemRecursoExtra(false);
        game.setNivelAtual(1);
        game.setPontuacaoTotal(150);
        
        game.mover("east");
        
        assertThat(game.getNivelAtual()).isEqualTo(1); // Blindado de cair para 0
        assertThat(game.getPontuacaoTotal()).isEqualTo(150); // Blindado de perder pontos
        assertThat(game.getPassosRestantes()).isEqualTo(55); // Mapa recarregado
    }

    // Verifica que chegar na sala da saída avança o nível 1 vai para 2, da 200 pontos, e recarrega o mapa (passos voltam a 55 no novo nivel)
    @Test
    @DisplayName("DM07 – Alcançar a saída (Tipo 2): avança nível, concede 200 pontos e reseta mapa")
    void testDM07_AlcancarSaida() {
        Room origem = new Room(0, 0, 0);
        Room salaSaida = new Room(1, 0, 2);
        origem.setExit("east", salaSaida);
        
        game.setCurrentRoom(origem);
        game.setNivelAtual(1);
        game.setPontuacaoTotal(0);
        
        game.mover("east");
        
        assertThat(game.getNivelAtual()).isEqualTo(2);
        assertThat(game.getPontuacaoTotal()).isEqualTo(200);
        assertThat(game.getPassosRestantes()).isEqualTo(55); // Mapa recarregado
    }

    // Verifica que o jogo não quebra nem consome passo quando recebe entradas anormais
    @Test
    @DisplayName("DM08 – Direções inválidas: Mover para direção nula, anômala ou sem sala é ignorado")
    void testDM08_DirecoesInvalidasNaoAlteramEstado() {
        Room origem = new Room(0, 0, 0);
        game.setCurrentRoom(origem);
        game.setPassosRestantes(10);
        
        game.mover(null);
        game.mover("diagonal_estranha");
        game.mover("");
        
        assertThat(game.getPassosRestantes()).isEqualTo(10);
        assertThat(game.getSalaAtual()).isEqualTo(origem);

        // Contrato extra: Sala atual ser nula (Garante segurança do sistema - MC/DC)
        game.setCurrentRoom(null);
        game.mover("north"); // Não deve lançar NullPointerException
    }

    // Verifica que iniciarSessao faz o reset certo. nível volta a 1, pontuação fica 0, gameOver vira false. Também verifica via verify(mockUsuario) que o contador de sessões do usuário foi incrementado
    @Test
    @DisplayName("DM09 – Inicialização de sessão: Reseta estado e contabiliza a jogada")
    void testDM09_InicializacaoDeSessaoLimpaEstado() {
        game.setNivelAtual(10);
        game.setPontuacaoTotal(999);
        game.setGameOver(true);
        
        game.iniciarSessao();
        
        assertThat(game.getNivelAtual()).isEqualTo(1);
        assertThat(game.getPontuacaoTotal()).isEqualTo(0);
        assertThat(game.isGameOver()).isFalse();
        
        // Verifica se contabilizou a sessão no usuário original
        verify(mockUsuario, atLeastOnce()).incrementarSessao(); // um usuario real ia incrementar sem dar pra conferir, por isso usei mock
    }

    // Verifica se as 4 direções estão levando pras salas vizinhas certas.
    @Test
    @DisplayName("DM10 – Rosa dos ventos: As 4 direções cardinais movem para os vizinhos corretos")
    void testDM10_QuatroDirecoesCardinais() {
        Room centro = new Room(5, 5, 0);
        Room norte  = new Room(5, 4, 0);
        Room sul    = new Room(5, 6, 0);
        Room leste  = new Room(6, 5, 0);
        Room oeste  = new Room(4, 5, 0);
        
        centro.setExit("north", norte);
        centro.setExit("south", sul);
        centro.setExit("east",  leste);
        centro.setExit("west",  oeste);

        game.setCurrentRoom(centro);
        game.mover("north");
        assertThat(game.getSalaAtual()).isEqualTo(norte);

        game.setCurrentRoom(centro);
        game.mover("south");
        assertThat(game.getSalaAtual()).isEqualTo(sul);

        game.setCurrentRoom(centro);
        game.mover("east");
        assertThat(game.getSalaAtual()).isEqualTo(leste);

        game.setCurrentRoom(centro);
        game.mover("west");
        assertThat(game.getSalaAtual()).isEqualTo(oeste);
    }

    // =========================================================================
    //  TESTES DE FRONTEIRA 
    // =========================================================================

    // Verifica que com 2 passos, depois de mover, o jogo fica com 1 passo e não termina
    @Test
    @DisplayName("FR01 – Quase Game Over (Fronteira superior de passos): 2 passos -> 1 passo (Jogo continua)")
    void testFR01_FronteiraPassosNaoZera() {
        Room origem = new Room(0, 0, 0);
        Room destino = new Room(1, 0, 0);
        origem.setExit("east", destino);

        game.setCurrentRoom(origem);
        game.setPassosRestantes(2); 
        
        game.mover("east");

        assertThat(game.getPassosRestantes()).isEqualTo(1);
        assertThat(game.isGameOver()).isFalse();
    }

    // Verifica que com exatamente 1 passo, após mover, os passos chegam a 0 e o isGameOver() vira true
    @Test
    @DisplayName("FR02 – Game Over Exato (Fronteira inferior de passos): 1 passo -> 0 passos (Gera Game Over)")
    void testFR02_FronteiraPassosZeradosGeraGameOver() {
        Room salaAtual = new Room(0, 0, 0);
        Room proximaSala = new Room(0, 1, 0);
        salaAtual.setExit("east", proximaSala);
        
        game.setCurrentRoom(salaAtual);
        game.setPassosRestantes(1); 
        
        game.mover("east");
        
        assertThat(game.getPassosRestantes()).isEqualTo(0);
        assertThat(game.isGameOver()).isTrue();
    }

    // Verifica que cair no alçapão com apenas 50 pontos não resulta em pontuação negativa
    @Test
    @DisplayName("FR03 – Pontuação inferior à penalidade: 50 pts ao cair no alçapão fica com 0 (Não negativo)")
    void testFR03_FronteiraPunicaoMenorQue200() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);

        game.setCurrentRoom(origem);
        game.setNivelAtual(2); // Precisa ser > 1 para aplicar a punição
        game.setPontuacaoTotal(50);
        game.setTemRecursoExtra(false);
        
        game.mover("east");

        assertThat(game.getPontuacaoTotal()).isEqualTo(0);
    }

    // Verifica o caso exato onde a punição é igual à pontuação: 200 - 200 = 0
    @Test
    @DisplayName("FR04 – Pontuação exata da penalidade: 200 pts ao cair no alçapão vai a exatamente 0")
    void testFR04_FronteiraPunicaoExata200() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);

        game.setCurrentRoom(origem);
        game.setNivelAtual(2);
        game.setPontuacaoTotal(200);
        game.setTemRecursoExtra(false);
        
        game.mover("east");

        assertThat(game.getPontuacaoTotal()).isEqualTo(0);
    }

    //Verifica que com 201 pontos o resultado é exatamente 1, após a punição do alçapão
    @Test
    @DisplayName("FR05 – Pontuação logo acima da penalidade: 201 pts ao cair no alçapão fica com exatamente 1")
    void testFR05_FronteiraPunicaoAcimaDe200() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);

        game.setCurrentRoom(origem);
        game.setNivelAtual(2);
        game.setPontuacaoTotal(201);
        game.setTemRecursoExtra(false);
        
        game.mover("east");

        assertThat(game.getPontuacaoTotal()).isEqualTo(1);
    }

    // Verifica se o nivel 1 permanece 1 se cair no alçapão
    @Test
    @DisplayName("FR06 – Limite inferior de Nível: Nível 1 permanece 1 ao cair no alçapão sem recurso")
    void testFR06_FronteiraNivelMinimo() {
        Room origem = new Room(0, 0, 0);
        Room salaAlcapao = new Room(1, 0, 4);
        origem.setExit("east", salaAlcapao);

        game.setCurrentRoom(origem);
        game.setNivelAtual(1);
        game.setPontuacaoTotal(500); // Pontuação não deve cair no nível 1
        game.setTemRecursoExtra(false);
        
        game.mover("east");

        // Confirma a blindagem da fronteira inferior do nível
        assertThat(game.getNivelAtual()).isEqualTo(1); 
        assertThat(game.getPontuacaoTotal()).isEqualTo(500);
    }

    // Simula o jogo acabando com 600 pontos quando o recorde do usuário era 50, Verifica que o recorde foi atualizado.
    @Test
    @DisplayName("FR07 – Superando o Recorde: Nova máxima atualiza a pontuação do usuário")
    void testFR07_FronteiraRecordeSuperado() {
        // Simula que o usuário tinha 50 de recorde
        when(mockUsuario.getPontuacaoMaxima()).thenReturn(50); // Stub, progama o mock para responder o valor
        
        // Simula que o jogo vai acabar no próximo passo com 600 pontos
        game.setPontuacaoTotal(600);
        game.setPassosRestantes(1);
        Room salaAtual = new Room(0, 0, 0);
        Room proximaSala = new Room(0, 1, 0);
        salaAtual.setExit("east", proximaSala);
        game.setCurrentRoom(salaAtual);
        
        game.mover("east"); // Dá o último passo e aciona finalizarJogo()
        
        // Verifica se o recorde foi sobrescrito com 600
        verify(mockUsuario).setPontuacaoMaxima(600); // usando mock pra confirmar que o codigo gravou o novo recorde
        assertThat(game.isGameOver()).isTrue();
    }

    // Simula o jogo acabando com 100 pontos quando o recorde era 900, Verifica que o metodo de atualizar o recorde não foi chamado.
    @Test
    @DisplayName("FR08 – Abaixo do Recorde: Pontuação final menor que o recorde não atualiza o usuário")
    void testFR08_FronteiraRecordeNaoSuperado() {
        // Simula que o usuário tem um recorde altíssimo (900)
        when(mockUsuario.getPontuacaoMaxima()).thenReturn(900);
        
        // Jogo acaba com apenas 100 pontos
        game.setPontuacaoTotal(100);
        game.setPassosRestantes(1);
        Room salaAtual = new Room(0, 0, 0);
        Room proximaSala = new Room(0, 1, 0);
        salaAtual.setExit("east", proximaSala);
        game.setCurrentRoom(salaAtual);
        
        game.mover("east"); // Dá o último passo e aciona finalizarJogo()
        
        // Verifica que NENHUMA tentativa de atualizar a pontuação máxima foi feita
        verify(mockUsuario, never()).setPontuacaoMaxima(anyInt());
        assertThat(game.isGameOver()).isTrue();
    }

    // =========================================================================
    // TESTES ESTRUTURAIS (MC/DC)
    // =========================================================================

    // Verifica se bloqueia o movimento quando o gameOver é verdadeiro
    @Test
    @DisplayName("ST01 – MC/DC: Movimento bloqueado quando gameOver = true")
    void testST01_GameOverImpedeMovimento() {
        game.setGameOver(true);
        Room salaAnterior = game.getSalaAtual();
        
        game.mover("north");
        
        // Garante a avaliação verdadeira do 'if (gameOver) return;'
        assertThat(game.getSalaAtual()).isEqualTo(salaAnterior);
    }

    // verifica a estrutura da matriz gerada. Garante que a geração aleatória sempre produz um mapa de tamanho correto
    @Test
    @DisplayName("ST02 – Estrutural: O Mapa gerado e retornado é sempre de tamanho 10x10")
    void testST02_MatrizDoMapa() {
        // Garante a cobertura do getter getMapa()
        int[][] mapa = game.getMapa();
        assertThat(mapa).isNotNull();
        assertThat(mapa.length).isEqualTo(10);
        assertThat(mapa[0].length).isEqualTo(10);
    }

    // Anda por toda a matriz contando tipos 3 e 4, Verifica se só tem 1 alçapão e 1 recurso
    @Test
    @DisplayName("ST03 – Mutação/Integridade: O mapa aleatório gera exatamente 1 Recurso (3) e 1 Alçapão (4)")
    void testST03_MapaGeraElementosAleatoriosCorretos() {
        int countRecurso = 0;
        int countAlcapao = 0;
        
        for (int[] linha : game.getMapa()) {
            for (int tipo : linha) {
                if (tipo == 3) countRecurso++;
                if (tipo == 4) countAlcapao++;
            }
        }

        assertThat(countRecurso).as("O mapa deve ter exatamente 1 recurso").isEqualTo(1);
        assertThat(countAlcapao).as("O mapa deve ter exatamente 1 alçapão").isEqualTo(1);
    }

    // Confirma que o mapa base sempre tem uma saída e que é só uma
    @Test
    @DisplayName("ST04 – Mutação/Integridade: O mapa aleatório gera exatamente 1 Saída (2)")
    void testST04_MapaGeraUmaSaida() {
        int countSaida = 0;
        
        for (int[] linha : game.getMapa()) {
            for (int tipo : linha) {
                if (tipo == 2) countSaida++;
            }
        }

        assertThat(countSaida).as("O mapa deve ter exatamente 1 saída fixada").isEqualTo(1);
    }

    // Verifica quando o UsuarioInjetado é null na criação do Game, Game sem injeção de dependencia. Verifica se tá na sessão do usuario correto
    @Test
    @DisplayName("ST05 – MC/DC: O construtor padrão aciona corretamente o fallback para o Singleton")
    void testST05_ConstrutorPadraoUsaSingleton() {
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        ger.cadastrar("testUser", "123", "avatar");
        ger.autenticar("testUser", "123");

        // Instancia sem o mockUsuario, forçando o if(usuarioInjetado != null) a ser FALSO
        Game jogoReal = new Game(); 
        jogoReal.iniciarSessao();
        
        assertThat(ger.getUsuarioLogado().getSessoesJogadas()).isGreaterThanOrEqualTo(1);
    }
    
    // =========================================================================
    // TESTES DE INTEGRAÇÃO 
    // =========================================================================

    // Cria um Game e inicia a sessão, testa o nivel 1, game over falso, 55 passos, sala não nula, faz movimento e ve se diminui os passos e muda a sala
    @Test
    @DisplayName("IN01 – Fluxo real de jogo navega corretamente sem mocks de salas")
    void testIN01_FluxoRealNoMapa() {
        Game jogoReal = new Game(mockUsuario); 
        
        jogoReal.setNivelAtual(10);
        jogoReal.setGameOver(true);
        
        jogoReal.iniciarSessao(); // Limpa lixo e gera novo mapa internamente
        
        assertThat(jogoReal.getNivelAtual()).isEqualTo(1);
        assertThat(jogoReal.isGameOver()).isFalse();
        assertThat(jogoReal.getPassosRestantes()).isEqualTo(55);
        assertThat(jogoReal.getSalaAtual()).isNotNull();

        Room salaInicial = jogoReal.getSalaAtual();
        
        // Tenta achar uma saída válida do spawn ([9][0])
        String direcaoValida = null;
        if (salaInicial.getExit("north") != null) direcaoValida = "north";
        else if (salaInicial.getExit("east") != null) direcaoValida = "east";

        if (direcaoValida != null) {
            jogoReal.mover(direcaoValida);
            assertThat(jogoReal.getPassosRestantes()).isEqualTo(54);
            assertThat(jogoReal.getSalaAtual()).isNotEqualTo(salaInicial);
        }
    }

    // =========================================================================
    // TESTES BASEADOS EM PROPRIEDADE (JQWIK)
    // =========================================================================

    // Gera milhares de combinações de pontuação inicial e niveis diferentes. aplicando em todas a queda no alçapão sem recurso e verifica se o resultado é sempre >=0
    @Property
    @DisplayName("PROP01 – A matemática do alçapão jamais permite pontuação negativa")
    void pontuacaoNuncaFicaNegativa(
            @ForAll @IntRange(min = 0, max = 10000) int pontuacaoInicial,
            @ForAll @IntRange(min = 2, max = 5) int nivelSimulado
    ) {
        // Instancia local para evitar problemas com ciclo de vida (BeforeEach) do Jqwik
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

    // Gera combinações de varios niveis iniciais e quedas seguidas. Testando que o nivel final é sempre >= 1
    @Property
    @DisplayName("PROP02 – O nível do jogador nunca pode ser inferior a 1 (Invariante)")
    void nivelNuncaFicaMenorQueUm(
            @ForAll @IntRange(min = 1, max = 50) int nivelInicial,
            @ForAll @IntRange(min = 1, max = 100) int qtdQuedas
    ) {
        Usuario localMock = mock(Usuario.class);
        Game propGame = new Game(localMock);
        
        propGame.setNivelAtual(nivelInicial);
        Room origem = new Room(0, 0, 0);
        Room alcapao = new Room(1, 0, 4);
        origem.setExit("east", alcapao);
        
        // Simula o jogador caindo no alçapão múltiplas vezes seguidas
        for(int i = 0; i < qtdQuedas; i++) {
            propGame.setCurrentRoom(origem);
            propGame.setTemRecursoExtra(false);
            propGame.mover("east");
        }
        
        // O nível deve ser sempre >= 1
        assertThat(propGame.getNivelAtual()).isGreaterThanOrEqualTo(1);
    }

    // Gera os passos iniciais de 1 a 100 e faz vários movimentos. testando que os passos nunca ficam negativos
    @Property
    @DisplayName("PROP03 – Os passos restantes nunca ficam negativos (Invariante)")
    void passosNuncaFicamNegativos(
            @ForAll @IntRange(min = 1, max = 100) int passosIniciais,
            @ForAll @IntRange(min = 1, max = 200) int qtdMovimentos
    ) {
        Usuario localMock = mock(Usuario.class);
        Game propGame = new Game(localMock);
        
        propGame.setPassosRestantes(passosIniciais);
        
        // Cria um corredor para o jogador ir e voltar
        Room origem = new Room(0, 0, 0);
        Room destino = new Room(1, 0, 0);
        origem.setExit("east", destino);
        destino.setExit("west", origem); 
        
        propGame.setCurrentRoom(origem);
        
        // Simula o jogador andando de um lado para o outro milhares de vezes
        for(int i = 0; i < qtdMovimentos; i++) {
            String direcao = (i % 2 == 0) ? "east" : "west";
            propGame.mover(direcao);
        }
        
        // Os passos devem travar em 0 e gerar Game Over, nunca ficando negativos
        assertThat(propGame.getPassosRestantes()).isGreaterThanOrEqualTo(0);
    }
}