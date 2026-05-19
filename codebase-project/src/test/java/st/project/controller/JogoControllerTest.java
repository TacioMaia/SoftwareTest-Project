package st.project.controller;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import st.project.model.Game;
import st.project.view.VistaJogo;
import st.project.view.VistaLogin;

public class JogoControllerTest {

    private Game modelMock;
    private VistaJogo viewMock;
    private JogoController controller;
    private Method metodoProcessar;


    @BeforeEach
    void setUp() throws Exception {
        // Criação d "Dublês de Teste"
        modelMock = Mockito.mock(Game.class);
        viewMock = Mockito.mock(VistaJogo.class);

        // Instanciamos o Controlador injetando os dublês
        controller = new JogoController(modelMock, viewMock);

        // Preparamos a Reflexão para chamar o método privado processarInput
        metodoProcessar = JogoController.class.getDeclaredMethod("processarInput", int.class);
        metodoProcessar.setAccessible(true);
    }

    @Test
    @DisplayName("Teste Estrutural: Iniciar o controlador deve configurar a vista e o modelo")
    void testIniciarControlador() {

        controller.iniciar();

        // Verificações originais
        verify(modelMock, times(1)).iniciarSessao();
        verify(viewMock, times(1)).atualizarTela(modelMock);
        verify(viewMock, times(1)).setVisible(true);

        org.mockito.ArgumentCaptor<java.awt.event.KeyListener> captor = org.mockito.ArgumentCaptor.forClass(java.awt.event.KeyListener.class);
        verify(viewMock).addKeyListener(captor.capture());
        // Cria um evento falso da tecla 'W'
        java.awt.Component componenteFake = Mockito.mock(java.awt.Component.class);
        java.awt.event.KeyEvent eventoTeclaW = new java.awt.event.KeyEvent(
                componenteFake, java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_W, 'W');

        captor.getValue().keyPressed(eventoTeclaW);
    }


    @Test
    @DisplayName("Teste de Domínio: Teclas de movimento devem acionar o modelo")
    void testMapeamentoDeTeclas() throws Exception {
when(modelMock.isGameOver()).thenReturn(false);

        // Cima
        metodoProcessar.invoke(controller, KeyEvent.VK_W);
        metodoProcessar.invoke(controller, KeyEvent.VK_UP);
        verify(modelMock, times(2)).mover("north");

        // Baixo
        metodoProcessar.invoke(controller, KeyEvent.VK_S);
        metodoProcessar.invoke(controller, KeyEvent.VK_DOWN);
        verify(modelMock, times(2)).mover("south");

        // Esquerda
        metodoProcessar.invoke(controller, KeyEvent.VK_A);
        metodoProcessar.invoke(controller, KeyEvent.VK_LEFT);
        verify(modelMock, times(2)).mover("west");

        // Direita
        metodoProcessar.invoke(controller, KeyEvent.VK_D);
        metodoProcessar.invoke(controller, KeyEvent.VK_RIGHT);
        verify(modelMock, times(2)).mover("east");
       
        // Tecla sem ação
        metodoProcessar.invoke(controller, KeyEvent.VK_X); 

        // Atualiza a tela
        verify(viewMock, times(9)).atualizarTela(modelMock);
    }

    @Test
    @DisplayName("Teste Estrutural: Game Over e o utilizador escolhe SIM (Jogar de novo)")
    void testGameOver_EscolheJogarNovamente() throws Exception {
        // Prepara o modelo para dizer que o jogo acabou após o próximo movimento
        when(modelMock.isGameOver()).thenReturn(true);

        // Intercepta o JOptionPane do Java Swing
        try (MockedStatic<JOptionPane> optionPaneMock = mockStatic(JOptionPane.class)) {
            
            // Fingimos que o usuário clicou em "SIM" quando a caixa de diálogo apareceu
            optionPaneMock.when(() -> JOptionPane.showConfirmDialog(
                    any(), any(), any(), eq(JOptionPane.YES_NO_OPTION)
            )).thenReturn(JOptionPane.YES_OPTION);

            metodoProcessar.invoke(controller, KeyEvent.VK_S);

            // Verificações do caminho "SIM"
            verify(modelMock, times(1)).mover("south"); // Registrou o movimento
            verify(viewMock, times(1)).mostrarMensagemFim(); // Mostrou o alerta de fim
            verify(modelMock, times(1)).iniciarSessao(); 
            //Atualiza a tela novamente para mostrar o novo estado do jogo
            verify(viewMock, times(2)).atualizarTela(modelMock); 
        }
    }

    @Test
    @DisplayName("Teste Estrutural: Game Over e o utilizador escolhe NÃO (Sair)")
    void testGameOver_EscolheSair() throws Exception {
        when(modelMock.isGameOver()).thenReturn(true);

        try (MockedStatic<JOptionPane> optionPaneMock = mockStatic(JOptionPane.class)) {
            
            // Fingimos que o utilizador clicou em "NÃO"
            optionPaneMock.when(() -> JOptionPane.showConfirmDialog(
                    any(), any(), any(), eq(JOptionPane.YES_NO_OPTION)
            )).thenReturn(JOptionPane.NO_OPTION);

            // Fecha a tela de login instantaneamente para evitar que o teste trave
            //  Timer de segurança para a nova janela que é instanciada
            javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
                for (java.awt.Window w : java.awt.Window.getWindows()) {
                    if (w instanceof VistaLogin) {
                        w.dispose();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();

            // Pressiona 'D' (Direita)
            metodoProcessar.invoke(controller, KeyEvent.VK_D);

            // Verificações do caminho "NÃO"
            verify(modelMock, times(1)).mover("east");
            verify(viewMock, times(1)).mostrarMensagemFim();
            
            // Como escolheu NÃO, a vista do jogo deve ter sido descartada
            verify(viewMock, times(1)).dispose();
            
            // Garantimos que não tentou reiniciar a sessão
            verify(modelMock, times(0)).iniciarSessao();
        }
    }

}
