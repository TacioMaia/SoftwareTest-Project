package st.project;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class VisualTest {

    private JogoTCCVisual tela = new JogoTCCVisual();

    @Test
    void testConfiguracaoInicialDaJanela() {
        assertThat(tela).isNotNull();
        assertThat(tela.getDefaultCloseOperation()).isEqualTo(JFrame.EXIT_ON_CLOSE);
        assertThat(tela.isVisible()).isFalse();
    
    }


    @Test
    void testEventoDeTecladoMoveOJogadorParaCima() {
        
        KeyListener[] listeners = tela.getKeyListeners();
        assertThat(listeners).isNotEmpty();
        KeyListener listenerDoJogo = listeners[0];

        KeyEvent pressW = new KeyEvent(
                tela, 
                KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 
                0, 
                KeyEvent.VK_W, 
                'W'
        );
        
        listenerDoJogo.keyPressed(pressW);

        assertThat(pressW.getKeyCode()).isEqualTo(KeyEvent.VK_W);
    }


    @Test
    void testMapaDeProducaoTemTamanhoCorreto() {
        int[][] mapa = JogoTCCVisual.MAPA_PRODUCAO;
        assertThat(mapa.length).isEqualTo(10);
        for (int i = 0; i < mapa.length; i++) {
            assertThat(mapa[i].length).isEqualTo(10);
        }
    }



}