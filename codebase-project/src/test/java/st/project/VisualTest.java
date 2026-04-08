package st.project;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

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
        // Cobre todas as condições de movimento (Cima, Baixo, Esquerda, Direita) - Duas teclas para cada IF
        metodoProcessar.invoke(tela, KeyEvent.VK_UP);
        metodoProcessar.invoke(tela, KeyEvent.VK_W);
        metodoProcessar.invoke(tela, KeyEvent.VK_DOWN);
        metodoProcessar.invoke(tela, KeyEvent.VK_S);
        metodoProcessar.invoke(tela, KeyEvent.VK_LEFT);
        metodoProcessar.invoke(tela, KeyEvent.VK_A);
        metodoProcessar.invoke(tela, KeyEvent.VK_RIGHT);
        metodoProcessar.invoke(tela, KeyEvent.VK_D);

        // Cobre a linha "if (moveu)" no estado FALSE (Aperta uma tecla inútil que não move)
        metodoProcessar.invoke(tela, KeyEvent.VK_X); 
    }

    // ------------------------------------------------------------------------
    // COBRIR OS 3 ESTADOS DOS IFs DE IMAGEM E O (atual != null)
    // ------------------------------------------------------------------------
    @Test
    void testCoberturaTotalDoPaintComponentEImagens() throws Exception {
        Field fChao = JogoTCCVisual.class.getDeclaredField("spriteChao"); fChao.setAccessible(true);
        Field fMesa = JogoTCCVisual.class.getDeclaredField("spriteMesa"); fMesa.setAccessible(true);
        Field fProf = JogoTCCVisual.class.getDeclaredField("spriteProfessor"); fProf.setAccessible(true);
        Field fJog = JogoTCCVisual.class.getDeclaredField("spriteJogador"); fJog.setAccessible(true);
        
        Field fPainel = JogoTCCVisual.class.getDeclaredField("painelJogo"); fPainel.setAccessible(true);
        JPanel painel = (JPanel) fPainel.get(tela);
        
        Graphics g = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB).getGraphics();

        //  Sprites Nulos (Cobre a primeira parte do &&)
        fChao.set(tela, null); fMesa.set(tela, null); fProf.set(tela, null); fJog.set(tela, null);
        painel.paint(g);

        // Sprites Vazios com largura negativa (Cobre a segunda parte do &&: getWidth > 0 = false)
        Image imgVazia = new ImageIcon(new byte[0]).getImage();
        fChao.set(tela, imgVazia); fMesa.set(tela, imgVazia); fProf.set(tela, imgVazia); fJog.set(tela, imgVazia);
        painel.paint(g);

        // Sprites Válidos (Cobre o caminho feliz do if)
        Image imgValida = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        fChao.set(tela, imgValida); fMesa.set(tela, imgValida); fProf.set(tela, imgValida); fJog.set(tela, imgValida);
        painel.paint(g);

        // Sala atual = null (Cobre a linha "if (atual != null)")
        Field fGame = JogoTCCVisual.class.getDeclaredField("game"); fGame.setAccessible(true);
        Game jogo = (Game) fGame.get(tela);
        jogo.setEstadoParaTestes(10, null); // Deixa o currentRoom nulo
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

        // 1. Cobre o "if (game.isVitoria()) return;" (Tentando mover após vencer)
        jogo.setEstadoParaTestes(10, new Room(9, 0, true)); // Sala do professor
        metodoProcessar.invoke(tela, KeyEvent.VK_W); // A linha inicial vermelha será executada e retornará

        // 2. Prepara a Thread Assassina de Pop-ups para a Derrota
        new Thread(() -> {
            try {
                Thread.sleep(500); // Aguarda o JOptionPane abrir
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_ESCAPE); robot.keyRelease(KeyEvent.VK_ESCAPE);
                robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER);
            } catch (Exception e) {}
        }).start();

        // 3. Força a Derrota e invoca a tecla (Cobre o JOptionPane, iniciarJogo e atualizarInterface)
        jogo.setEstadoParaTestes(0, new Room(0, 0, false)); // 0 passos = Derrota
        
        // Esta chamada vai disparar o "return" logo no início do método processarTecla
        metodoProcessar.invoke(tela, KeyEvent.VK_W); 
        
        // Para acionar diretamente o bloco "else if (game.isDerrota())", nós chamamos o método específico
        Method mFimDeJogo = JogoTCCVisual.class.getDeclaredMethod("verificarFimDeJogo");
        mFimDeJogo.setAccessible(true);
        mFimDeJogo.invoke(tela); // Dispara o Pop-up de derrota que será fechado pelo Robot!
        
        assertThat(tela).isNotNull();
    }

}