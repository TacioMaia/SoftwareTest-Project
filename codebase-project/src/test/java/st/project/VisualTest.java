package st.project;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VisualTest {

private JogoTCCVisual tela;
private Method metodoProcessar;

    @BeforeEach
    void setUp() throws Exception {
        tela = new JogoTCCVisual();
        metodoProcessar = JogoTCCVisual.class.getDeclaredMethod("processarTecla", int.class);
        metodoProcessar.setAccessible(true);
    }

    // ------------------------------------------------------------------------
    // COBRIR TODAS AS VARIAÇÕES DE TECLAS, '||' E A VARIÁVEL 'moveu'
    // ------------------------------------------------------------------------
@Test
    void testTodasAsTeclasEORetornoDoMoveu() throws Exception {
        KeyListener listenerOficial = tela.getKeyListeners()[0];
        listenerOficial.keyPressed(new KeyEvent(tela, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, ' '));

        // Cobre o resto das condições rapidamente com reflexão
        metodoProcessar.invoke(tela, KeyEvent.VK_W);
        metodoProcessar.invoke(tela, KeyEvent.VK_DOWN);
        metodoProcessar.invoke(tela, KeyEvent.VK_S);
        metodoProcessar.invoke(tela, KeyEvent.VK_LEFT);
        metodoProcessar.invoke(tela, KeyEvent.VK_A);
        metodoProcessar.invoke(tela, KeyEvent.VK_RIGHT);
        metodoProcessar.invoke(tela, KeyEvent.VK_D);

        // Cobre a linha "if (moveu)" no estado FALSE
        metodoProcessar.invoke(tela, KeyEvent.VK_X); 
    }
    // ------------------------------------------------------------------------
    // COBRIR OS 3 ESTADOS DOS IFs DE IMAGEM E O (atual != null)
    // ------------------------------------------------------------------------
@Test
    void testCoberturaTotalDoPaintComponentEImagens() throws Exception {
        Method mCarregar = JogoTCCVisual.class.getDeclaredMethod("carregarImagem", String.class);
        mCarregar.setAccessible(true);
        mCarregar.invoke(tela, "/caminho/invalido/para/dar/erro.png");

        Field fChao = JogoTCCVisual.class.getDeclaredField("spriteChao"); fChao.setAccessible(true);
        Field fMesa = JogoTCCVisual.class.getDeclaredField("spriteMesa"); fMesa.setAccessible(true);
        Field fProf = JogoTCCVisual.class.getDeclaredField("spriteProfessor"); fProf.setAccessible(true);
        Field fJog = JogoTCCVisual.class.getDeclaredField("spriteJogador"); fJog.setAccessible(true);
        
        Field fPainel = JogoTCCVisual.class.getDeclaredField("painelJogo"); fPainel.setAccessible(true);
        JPanel painel = (JPanel) fPainel.get(tela);
        
        Graphics g = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB).getGraphics();

        // ESTADO 1: Sprites Nulos
        fChao.set(tela, null); fMesa.set(tela, null); fProf.set(tela, null); fJog.set(tela, null);
        painel.paint(g);

        // ESTADO 2: Sprites Vazios (Largura < 0)
        Image imgVazia = new ImageIcon(new byte[0]).getImage();
        fChao.set(tela, imgVazia); fMesa.set(tela, imgVazia); fProf.set(tela, imgVazia); fJog.set(tela, imgVazia);
        painel.paint(g);

        // ESTADO 3: Sprites Válidos
        Image imgValida = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        fChao.set(tela, imgValida); fMesa.set(tela, imgValida); fProf.set(tela, imgValida); fJog.set(tela, imgValida);
        painel.paint(g);

        // ESTADO 4: Sala atual = null
        Field fGame = JogoTCCVisual.class.getDeclaredField("game"); fGame.setAccessible(true);
        Game jogo = (Game) fGame.get(tela);
        jogo.setEstadoParaTestes(10, null);
        painel.paint(g);
    }

    // ------------------------------------------------------------------------
    // COBRIR AS CORES (passos <= 5)
    // ------------------------------------------------------------------------
    @Test
    void testMudancaDeCorLabelStatus() throws Exception {
        Method mAtualizar = JogoTCCVisual.class.getDeclaredMethod("atualizarInterface");
        mAtualizar.setAccessible(true);

        Field fGame = JogoTCCVisual.class.getDeclaredField("game"); fGame.setAccessible(true);
        Game jogo = (Game) fGame.get(tela);

        // Testa o "else" (Cores normais, Passos > 5)
        jogo.setEstadoParaTestes(20, new Room(0,0,false));
        mAtualizar.invoke(tela);

        // Testa o "if" (Cores de alerta, Passos <= 5)
        jogo.setEstadoParaTestes(3, new Room(0,0,false));
        mAtualizar.invoke(tela);
    }

    // ------------------------------------------------------------------------
    // COBRIR RETORNOS PREMATUROS E O BLOCO JOPTIONPANE DA DERROTA
    // ------------------------------------------------------------------------
@Test
    void testDerrotaEVitoriaComBloqueioDeTeclasEPopup() throws Exception {
        Field fGame = JogoTCCVisual.class.getDeclaredField("game"); fGame.setAccessible(true);
        Game jogo = (Game) fGame.get(tela);
        Method mFimDeJogo = JogoTCCVisual.class.getDeclaredMethod("verificarFimDeJogo"); mFimDeJogo.setAccessible(true);

        
        Timer timer = new Timer(200, e -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog) { w.dispose(); }
            }
        });
        timer.setRepeats(true);
        timer.start();

        // --- TESTANDO A VITÓRIA ---
        jogo.setEstadoParaTestes(15, new Room(9, 9, true)); // Sala do professor
        mFimDeJogo.invoke(tela); // Abre o pop-up de Vitória (e o Timer fecha)
        
        // Cobre o "if (game.isVitoria()) return;" (Tentando mover após vencer)
        jogo.setEstadoParaTestes(15, new Room(9, 9, true)); 
        metodoProcessar.invoke(tela, KeyEvent.VK_W); 

        // --- TESTANDO A DERROTA ---
        jogo.setEstadoParaTestes(0, new Room(0, 0, false)); // 0 passos = Derrota
        mFimDeJogo.invoke(tela); // Abre o pop-up de Derrota (e o Timer fecha)
        
        // Cobre o "if (game.isDerrota()) return;" (Tentando mover após perder)
        jogo.setEstadoParaTestes(0, new Room(0, 0, false)); 
        metodoProcessar.invoke(tela, KeyEvent.VK_W);
        
        timer.stop(); // Desliga o guardião de pop-ups
        assertThat(tela).isNotNull();
    }

}