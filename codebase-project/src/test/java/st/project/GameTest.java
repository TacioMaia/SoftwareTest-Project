package st.project;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName; 
import org.junit.jupiter.api.Test;

//import st.project.Game;
//import st.project.Room;

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
  





}