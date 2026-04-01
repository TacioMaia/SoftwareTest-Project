import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import st.project.Game;
import st.project.Room;

class GameTest {

    private Game game;

    // 1. TESTES ESTRUTURAIS: 100% Cobertura MC/DC
    
    @Test
    @DisplayName("isVitoria MC/DC (T-T): Sala não-nula e É a sala do professor -> TRUE")
    void testVitoria_NotNullAndIsProf_ReturnsTrue() {
        Game game = new Game();
        game.setEstadoParaTestes(5, new Room(9, 0, true)); // Injeta o estado!
        assertThat(game.isVitoria()).isTrue();
    }

    @Test
    @DisplayName("isVitoria MC/DC (F-F): Sala nula -> FALSE")
    void testVitoria_NullRoom_ReturnsFalse() {
        Game game = new Game();
        game.setEstadoParaTestes(5, null);
        assertThat(game.isVitoria()).isFalse();
    }

    @Test
    @DisplayName("isVitoria MC/DC (T-F): Sala não-nula mas NÃO É a sala do professor -> FALSE")
    void testVitoria_NotNullAndNotProf_ReturnsFalse() {
        Game game = new Game();
        game.setEstadoParaTestes(5, new Room(0, 0, false));
        assertThat(game.isVitoria()).isFalse();
    }

    // ALVO: isDerrota() -> passosRestantes <= 0 && !isVitoria()

    @Test
    @DisplayName("isDerrota MC/DC (T-T): Sem passos e Não é vitória -> TRUE")
    void testDerrota_NoStepsAndNotWin_ReturnsTrue() {
        Game game = new Game();
        game.setEstadoParaTestes(0, new Room(0, 0, false));
        assertThat(game.isDerrota()).isTrue();
    }

    @Test
    @DisplayName("isDerrota MC/DC (F-T): Com passos e Não é vitória -> FALSE")
    void testDerrota_HasStepsAndNotWin_ReturnsFalse() {
        Game game = new Game();
        game.setEstadoParaTestes(1, new Room(0, 0, false));
        assertThat(game.isDerrota()).isFalse();
    }

    @Test
    @DisplayName("isDerrota MC/DC (T-F): Sem passos mas É vitória -> FALSE")
    void testDerrota_NoStepsButIsWin_ReturnsFalse() {
        Game game = new Game();
        game.setEstadoParaTestes(0, new Room(9, 0, true));
        assertThat(game.isDerrota()).isFalse();
    }


    // 2. TESTES DE FRONTEIRA 
    @Test
    @DisplayName("Fronteira Interna (+1): 1 passo restante em sala comum -> Continua jogando")
    void testBoundary_OneStepLeft_Playing() {
        Game game = new Game();
        game.setEstadoParaTestes(1, new Room(0, 0, false));
        assertThat(game.isDerrota()).isFalse();
        assertThat(game.isVitoria()).isFalse();
    }

    @Test
    @DisplayName("Limite Exato (0): 0 passos restantes em sala comum -> DERROTA")
    void testBoundary_ZeroStepsLeft_Lose() {
        Game game = new Game();
        game.setEstadoParaTestes(0, new Room(0, 0, false));
        assertThat(game.isDerrota()).isTrue();
    }

    @Test
    @DisplayName("Fronteira Externa (-1): -1 passo restante em sala comum -> DERROTA")
    void testBoundary_NegativeStepsLeft_Lose() {
        Game game = new Game();
        game.setEstadoParaTestes(-1, new Room(0, 0, false));
        assertThat(game.isDerrota()).isTrue();
    }
   
    //  TESTES DE DOMÍNIO

    @Test
    @DisplayName("Domínio Válido: Mover para uma sala adjacente válida subtrai 1 passo")
    void testDomain_ValidMove_DecrementsSteps() {
        // Inicia um jogo normal usando o construtor padrão
        Game game = new Game(); 
        int passosIniciais = game.getPassosRestantes(); 
        
        game.mover("north"); // Tenta mover
        
        assertThat(game.getPassosRestantes()).isEqualTo(passosIniciais - 1);
        assertThat(game.getSalaAtual().getY()).isEqualTo(8); 
    }

    @Test
    @DisplayName("Domínio Inválido: Tentar mover para parede ou fora do mapa não subtrai passos")
    void testDomain_InvalidMove_DoesNotDecrementSteps() {
        // Inicia um jogo normal
        Game game = new Game(); 
        int passosIniciais = game.getPassosRestantes(); 
        
        // No mapa inicial, a posição "west" (oeste) não tem saída
        game.mover("west"); 
        
        assertThat(game.getPassosRestantes()).isEqualTo(passosIniciais);
        assertThat(game.getSalaAtual().getY()).isEqualTo(9); 
    }
}


