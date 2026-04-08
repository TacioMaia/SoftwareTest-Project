package st.project;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName; 
import org.junit.jupiter.api.Test;

class GameTest{
    // Mapa fantasma para não quebrar o construtor original
    private final int[][] MAPA_DUMMY = {{0}};
    private Game game;

    @BeforeEach
    void setUp() {
        // Inicializa o jogo limpo antes de cada teste
        game = new Game(10, MAPA_DUMMY);
    }



    //---------------------------------------------------------------------------
    //                              TESTES DE DOMINIO
    //---------------------------------------------------------------------------

    // Movimentos Validos
    @Test
    @DisplayName("Teste de Dominio:Movimentos para caminhos livres")
    void testDominio_MovimentoValido_AlteraSalaEConsomePasso(){
        //Cria um hub central com saídas para todos os lados
        Room centro = new Room(1, 1, false);
        Room norte  = new Room(1, 0, false);
        Room sul    = new Room(1, 2, false);
        Room leste  = new Room(2, 1, false);
        Room oeste  = new Room(0, 1, false);

        centro.setExit("north", norte);
        centro.setExit("south", sul);
        centro.setExit("east", leste); 
        centro.setExit("west", oeste);

        //Norte
        game.setEstadoParaTestes(10, centro);
        game.mover("north");
        assertThat(game.getSalaAtual()).isEqualTo(norte);
        assertThat(game.getPassosRestantes()).isEqualTo(9);

        //Sul
        game.setEstadoParaTestes(10, centro);
        game.mover("south");
        assertThat(game.getSalaAtual()).isEqualTo(sul);
        assertThat(game.getPassosRestantes()).isEqualTo(9);

        //Leste
        game.setEstadoParaTestes(10, centro);
        game.mover("east");
        assertThat(game.getSalaAtual()).isEqualTo(leste);
        assertThat(game.getPassosRestantes()).isEqualTo(9);

        //Oeste
        game.setEstadoParaTestes(10, centro);
        game.mover("west");
        assertThat(game.getSalaAtual()).isEqualTo(oeste);
        assertThat(game.getPassosRestantes()).isEqualTo(9);
    }

    @Test
    @DisplayName("Teste de Dominio:Movimentos para sala do professor")
    void testDominio_MovimentoValido_EntraNaSalaDoProfessor() {
        //Cria uma sala comum ligada à sala do professor
        Room salaComum = new Room(0, 0, false);
        Room salaDoProfessor = new Room(1, 0, true);
        salaComum.setExit("east", salaDoProfessor);

        //Coloca o jogador na sala comum com passos sobrando
        game.setEstadoParaTestes(5, salaComum);

        //Move para a sala do professor
        game.mover("east");

        //Verifica a mudança de estado
        assertThat(game.getSalaAtual()).isEqualTo(salaDoProfessor); //Conseguiu entrar
        assertThat(game.getPassosRestantes()).isEqualTo(4); //O passo foi consumido
        assertThat(game.isVitoria()).isTrue(); // O jogo reconheceu a vitória
        assertThat(game.isDerrota()).isFalse(); // Garante que não houve conflito de estados
    }

    // Movimentos Inválidos
    @Test
    @DisplayName("Teste de Dominio:Movimentos para paredes não contam passos")
    void testDominio_MovimentoContraParedes_NaoGastaPassos(){
        Game game = new Game(5, MAPA_DUMMY);
        Room salaSemSaida = new Room(0, 0, false);
        
        game.setEstadoParaTestes(5, salaSemSaida);
        game.mover("north"); 
        
        assertThat(game.getPassosRestantes()).isEqualTo(5); 
        assertThat(game.getSalaAtual()).isEqualTo(salaSemSaida);
    }

    // Entradas Anormais
    @Test
    @DisplayName("P3: Entradas anômalas (strings não reconhecidas ou nulas) devem ser tratadas como parede")
    void testDominio_EntradasAnomalas() {
        Room salaInicial = new Room(0, 0, false);
        game.setEstadoParaTestes(5, salaInicial);
        
        // Tenta direção inventada
        game.mover("diagonal");
        assertThat(game.getPassosRestantes()).isEqualTo(5);
        
        // Tenta string vazia
        game.mover("");
        assertThat(game.getPassosRestantes()).isEqualTo(5);
        
        // Tenta null
        game.mover(null);
        assertThat(game.getPassosRestantes()).isEqualTo(5);
    }

    // Estados Finais (GAME OVER)
    @Test
    @DisplayName("Teste de Dominio:Movimentos devem ser ignorados após vitória/derrota")
    void testDominio_FimDeJogo_IgnoraMovimento(){
        Game game = new Game(5, MAPA_DUMMY);
        Room salaComum = new Room(0, 0, false);
        Room salaProf = new Room(1, 0, true);
        salaComum.setExit("east", salaProf);

        //Simula derrota e tenta andar
        game.setEstadoParaTestes(0, salaComum);
        game.mover("east");
        assertThat(game.getPassosRestantes()).isEqualTo(0); //Bloqueia ação / trava os passos

        //Simula vitoria e tenta andar
        game.setEstadoParaTestes(5, salaProf);
        game.mover("north");
        assertThat(game.getPassosRestantes()).isEqualTo(5); //Bloqueia ação / trava os passos
    }
    
    // Dominio de inicio (MAPA e SPAW)
    @Test
    @DisplayName("Teste de Dominio: Inicialização deve traduzir matriz (1=Parede, 2=Prof) e usar fallback de spawn se o canto for parede")
    void testDominio_Inicializacao_TraducaoMapaESpawnAlternativo() {
        //Cria um mini mapa de 1 linha e 3 colunas: [ Parede(1), Normal(0), Professor(2) ]
        int[][] mapaCustomizado = {
            {1, 0, 2}
        };
        
        //Instancia o jogo. Ele vai rodar o criarSalas().
        Game jogo = new Game(10, mapaCustomizado);
        
        //O Fallback de Spawn funcionou?
        // Como a posição padrão [0][0] tem o valor 1 (parede), o jogo DEVE ter nascido no [0][1].
        assertThat(jogo.getSalaAtual().getX()).isEqualTo(1); 
        assertThat(jogo.getSalaAtual().getY()).isEqualTo(0);
        assertThat(jogo.isVitoria()).isFalse(); // A sala [0][1] é normal (0)
        
        //A parede (1) foi mapeada à esquerda (oeste)?
        jogo.mover("west"); 
        assertThat(jogo.getPassosRestantes()).isEqualTo(10); // Bateu na parede, passos não desceram
        
        //A sala do professor (2) foi mapeada à direita (leste)?
        jogo.mover("east");
        assertThat(jogo.isVitoria()).isTrue(); // Encontrou o professor!
    }

    // Dominio de reinicio (RESET)
    @Test
    @DisplayName("Teste de Dominio: O método iniciarJogo() deve resetar os passos e recolocar o jogador na origem")
    void testDominio_ReiniciarJogo_ResetaEstadoCompleto() {
        // Cria um mapa pequeno para teste (1 linha, 2 colunas)
        int[][] mapaSimples = {{0, 0}};
        Game jogoTestandoReset = new Game(15, mapaSimples);
        
        // Guarda as coordenadas da sala de origem para comparar no final
        int xInicial = jogoTestandoReset.getSalaAtual().getX();
        int yInicial = jogoTestandoReset.getSalaAtual().getY();

        // Simula o jogador a meio de uma partida (quase a perder)
        Room salaLonge = new Room(9, 9, false);
        jogoTestandoReset.setEstadoParaTestes(2, salaLonge);

        // O jogador (ou o sistema) reinicia a partida
        jogoTestandoReset.iniciarJogo();

        // Verifica se as variáveis vitais voltaram ao estado de fábrica
        assertThat(jogoTestandoReset.getPassosRestantes()).isEqualTo(15); 
        assertThat(jogoTestandoReset.getSalaAtual().getX()).isEqualTo(xInicial);
        assertThat(jogoTestandoReset.getSalaAtual().getY()).isEqualTo(yInicial);
        assertThat(jogoTestandoReset.isDerrota()).isFalse();
        assertThat(jogoTestandoReset.isVitoria()).isFalse();
    }

    

    //---------------------------------------------------------------------------
    //                            TESTES DE FRONTEIRA
    //---------------------------------------------------------------------------

    @Test
    @DisplayName("Teste Fronteira: 1 passo restante em sala comum -> Jogo continua")
    void testFronteira_UmPassoRestante_ContinuaJogando() {
        Room salaComum = new Room(0, 0, false);
        game.setEstadoParaTestes(1, salaComum);
        
        // Com 1 passo, o jogo ainda não acabou. Não é derrota nem vitória.
        assertThat(game.isDerrota()).isFalse();
        assertThat(game.isVitoria()).isFalse();
    }

    @Test
    @DisplayName("Teste Fronteira: 0 passos restantes em sala comum -> Derrota imediata")
    void testFronteira_ZeroPassosRestantes_Derrota() {
        Room salaComum = new Room(0, 0, false);
        game.setEstadoParaTestes(0, salaComum);
        
        // Chegou a zero exatamente. O jogo deve decretar derrota.
        assertThat(game.isDerrota()).isTrue();
        assertThat(game.isVitoria()).isFalse();
    }

    @Test
    @DisplayName("Teste Fronteira: Passos negativos em sala comum -> Mantém estado de Derrota")
    void testFronteira_PassosNegativos_Derrota() {
        Room salaComum = new Room(0, 0, false);
        
        // Embora o jogo normal não deva deixar chegar a -1 (bloqueia no 0),
        // testa o limite inferior matemático para garantir robustez da lógica.
        game.setEstadoParaTestes(-1, salaComum);
        
        assertThat(game.isDerrota()).isTrue();
    }

    @Test
    @DisplayName("Teste de Fronteira: Jogo criado com 0 passos já inicia em Derrota")
    void testFronteira_PassosIniciaisZero_IniciaDerrotado() {
        // Instancia o jogo na fronteira absoluta de passos
        int[][] mapaComum = {{0, 0}};
        Game jogoSemPassos = new Game(0, mapaComum);
        
        // O jogo já deve nascer finalizado
        assertThat(jogoSemPassos.isDerrota()).isTrue();
        assertThat(jogoSemPassos.getPassosRestantes()).isEqualTo(0);
    }

    @Test
    @DisplayName("Teste de Fronteira: Chegar ao professor no limite exato de passos (0 passos) garante a Vitória")
    void testFronteira_ZeroPassos_NaSalaDoProfessor_Vitoria() {
        Room salaDoProfessor = new Room(9, 0, true); // true = professor
        game.setEstadoParaTestes(0, salaDoProfessor);
        
        // A vitória deve ter prioridade absoluta sobre a falta de passos
        assertThat(game.isVitoria()).isTrue();
        assertThat(game.isDerrota()).isFalse();
    }

    @Test
    @DisplayName("Teste de Fronteira: Chegar ao professor com passos sobrando garante a Vitória")
    void testFronteira_UmPasso_NaSalaDoProfessor_Vitoria() {
        Room salaDoProfessor = new Room(9, 0, true);
        game.setEstadoParaTestes(1, salaDoProfessor);
        
        assertThat(game.isVitoria()).isTrue();
        assertThat(game.isDerrota()).isFalse();
    }

    @Test
    @DisplayName("Teste de Fronteira: Passos negativos na sala do professor ainda mantêm a Vitória")
    void testFronteira_PassosNegativos_NaSalaDoProfessor_Vitoria() {
        Room salaDoProfessor = new Room(9, 0, true);
        
        // Simula uma anomalia numérica, mas garantindo que a regra da sala prevalece
        game.setEstadoParaTestes(-1, salaDoProfessor);
        
        assertThat(game.isVitoria()).isTrue();
        assertThat(game.isDerrota()).isFalse();
    }



    //---------------------------------------------------------------------------
    //                            TESTES ESTRUTURAIS
    //---------------------------------------------------------------------------

    @Test
    @DisplayName("Teste Estrutural: Mapeamento de salas deve cobrir ramificações Norte, Sul, Leste e Oeste")
    void testEstrutural_CriarSalas_GeraPortasTodasDirecoes() {
        // Matriz 3x3 força o código a validar todos os 'ifs' de vizinhança no criarSalas()
        int[][] mapa3x3 = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        Game jogo3x3 = new Game(10, mapa3x3);
        assertThat(jogo3x3.getSalaAtual()).isNotNull();
    }

    @Test
    @DisplayName("Teste Estrutural: Fallback de spawn deve buscar em múltiplas linhas")
    void testEstrutural_FallbackSpawn_MultiplasLinhas() {
        // Linha inferior inteira de paredes força o loop a quebrar e subir para a linha anterior
        int[][] mapaFallback = {
            {0, 0},
            {1, 1}
        };
        Game jogoFallback = new Game(10, mapaFallback);
        assertThat(jogoFallback.getSalaAtual().getY()).isEqualTo(0);
    }

    @Test
    @DisplayName("MC/DC isVitoria: Sala nula (Curto-circuito retorna Falso)")
    void testMCDC_Vitoria_SalaNula() {
        game.setEstadoParaTestes(5, null);
        assertThat(game.isVitoria()).isFalse();
    }

    @Test
    @DisplayName("MC/DC isVitoria: Sala válida mas não é professor (Retorna Falso)")
    void testMCDC_Vitoria_SalaNaoProfessor() {
        game.setEstadoParaTestes(5, new Room(0, 0, false));
        assertThat(game.isVitoria()).isFalse();
    }

    @Test
    @DisplayName("MC/DC isDerrota: Com passos restantes (Curto-circuito retorna Falso)")
    void testMCDC_Derrota_ComPassos() {
        game.setEstadoParaTestes(1, new Room(0, 0, false));
        assertThat(game.isDerrota()).isFalse();
    }

    @Test
    @DisplayName("MC/DC isDerrota: Sem passos e não é vitória (Retorna Verdadeiro)")
    void testMCDC_Derrota_SemPassos_NaoVitoria() {
        game.setEstadoParaTestes(0, null);
        assertThat(game.isDerrota()).isTrue();
    }

    @Test
    @DisplayName("Teste Estrutural: Mapeamento de portas (True && False) - Vizinhos que são paredes")
    void testEstrutural_CriarSalas_VizinhosSaoParedes() {
        // Cria um mapa em formato de cruz invertida.
        // A sala do centro (linha 1, coluna 1) não está em nenhuma borda do mapa.
        // No entanto, todos os seus 4 vizinhos (Norte, Sul, Leste, Oeste) são PAREDES (1).
        int[][] mapaComParedes = {
            {0, 1, 0},
            {1, 0, 1},
            {0, 1, 0}
        };
        
        // Ao instanciar, o 'if (linha > 0)' será Verdadeiro, mas o 'grid != null' será Falso.
        // Isso cobre todas as ramificações MC/DC das direções (linhas norte e leste inclusas).
        Game jogoParedes = new Game(10, mapaComParedes);
        
        assertThat(jogoParedes.getSalaAtual()).isNotNull();
    }

    @Test
    @DisplayName("Teste Estrutural: Loop de Fallback - Mapa inteiro de paredes (forçando l < 0)")
    void testEstrutural_FallbackSpawn_MapaTotalmenteFechado() {
        // Um mapa onde 100% das posições são paredes (1).
        // Isso obriga o loop 'for' das linhas a rodar até o fim e a variável 'l' ficar menor que 0,
        // cobrindo a condição de saída natural do loop de fallback.
        int[][] mapaFechado = {
            {1, 1},
            {1, 1}
        };
        
        Game jogoFechado = new Game(10, mapaFechado);
        
        // Como não há salas, o fallback não encontra nada, e currentRoom permanece nula.
        assertThat(jogoFechado.getSalaAtual()).isNull();
    }

}