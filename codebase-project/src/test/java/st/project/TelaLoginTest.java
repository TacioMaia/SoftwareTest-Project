package st.project;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;

public class TelaLoginTest {

    @Test
    @DisplayName("Teste de Integração: Fluxo de Login com Falha (Exibe JOptionPane)")
    void testCliqueBotaoEntrarComFalha() throws Exception {
        // Mock do Gerenciador para garantir que a autenticação falha
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.autenticar(any(), any())).thenReturn(false);

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class);
             MockedStatic<JOptionPane> optionPaneMock = mockStatic(JOptionPane.class)) {
            
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaLogin vistaLogin = new VistaLogin();

            // Interagimos diretamente com os campos 
            JTextField campoLogin = encontrarCampoTexto(vistaLogin, "login");
            if (campoLogin != null) campoLogin.setText("usuarioInexistente");

            JButton btnEntrar = encontrarBotao(vistaLogin, "Entrar no Jogo");
            assertThat(btnEntrar).as("O botão não foi encontrado na tela!").isNotNull();
            
            // Simula o clique do utilizador
            if (btnEntrar != null) btnEntrar.doClick();

            // Verifica se a tela tentou mostrar o aviso de erro 
          optionPaneMock.verify(() -> JOptionPane.showMessageDialog(
            Mockito.any(java.awt.Component.class), 
            Mockito.eq("Credenciais inválidas!")), Mockito.atLeastOnce());
        }
    }

    @Test
    @DisplayName("Teste de Integração: Fluxo de Login com Sucesso (Inicia o Jogo)")
    void testCliqueBotaoEntrarComSucesso() throws Exception {
        // Agora forçamos o mock a devolver TRUE
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.autenticar(any(), any())).thenReturn(true);

        Usuario usuarioFalso = new Usuario("jogador", "senha123", "👤");
        Mockito.when(gerMock.getUsuarioLogado()).thenReturn(usuarioFalso);

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class)) {
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaLogin vistaLogin = new VistaLogin();
            
            JButton btnEntrar = encontrarBotao(vistaLogin, "Entrar no Jogo");
            assertThat(btnEntrar).isNotNull();

            // Clica no botão de entrar
            for (java.awt.event.ActionListener al : btnEntrar.getActionListeners()) {
                al.actionPerformed(new java.awt.event.ActionEvent(btnEntrar, java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
            }
          
            // verifica se a janela foi removida da memória.
            assertThat(vistaLogin.isDisplayable()).isFalse();
        }
    }

    @Test
    @DisplayName("Teste de Integração: Fluxo de Registo com Sucesso")
    void testCliqueBotaoRegistarComSucesso() throws Exception {
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        // Força o cadastro a devolver TRUE
        Mockito.when(gerMock.cadastrar(any(), any(), any())).thenReturn(true);

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class);
             MockedStatic<JOptionPane> optionPaneMock = mockStatic(JOptionPane.class)) {
            
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaLogin vistaLogin = new VistaLogin();

            java.lang.reflect.Field fAvatar = VistaLogin.class.getDeclaredField("txtAvatar");
            fAvatar.setAccessible(true);
            javax.swing.JTextField txtAvatar = (javax.swing.JTextField) fAvatar.get(vistaLogin);
            txtAvatar.setText("🤖");
            
            JButton btnRegistar = encontrarBotao(vistaLogin, "Registar Novo"); 
            assertThat(btnRegistar).as("Botão de registo não encontrado!").isNotNull();

            // Clica no botão
            for (java.awt.event.ActionListener al : btnRegistar.getActionListeners()) {
                al.actionPerformed(new java.awt.event.ActionEvent(btnRegistar, java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
            }

            // Verifica a linha da mensagem de sucesso
            optionPaneMock.verify(() -> JOptionPane.showMessageDialog(
                Mockito.any(java.awt.Component.class), 
                Mockito.eq("Registo efetuado com sucesso!")
            ), Mockito.atLeastOnce());
        }
    }

    @Test
    @DisplayName("Teste de Integração: Fluxo de Registo com Falha (Duplicado)")
    void testCliqueBotaoRegistarComFalha() throws Exception {
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        // Força o cadastro a falhar (FALSE)
        Mockito.when(gerMock.cadastrar(any(), any(), any())).thenReturn(false);

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class);
             MockedStatic<JOptionPane> optionPaneMock = mockStatic(JOptionPane.class)) {
            
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaLogin vistaLogin = new VistaLogin();
            
            JButton btnRegistar = encontrarBotao(vistaLogin, "Registar Novo");
            assertThat(btnRegistar).isNotNull();

            // Clica no botão
            for (java.awt.event.ActionListener al : btnRegistar.getActionListeners()) {
                al.actionPerformed(new java.awt.event.ActionEvent(btnRegistar, java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
            }

            // Verifica a linha da mensagem de falha
            optionPaneMock.verify(() -> JOptionPane.showMessageDialog(
                Mockito.any(java.awt.Component.class), 
                Mockito.eq("Este utilizador já existe.")
            ), Mockito.atLeastOnce());
        }
    }



    // --- MÉTODOS DE BUSCA DE COMPONENTES ---
    private JButton encontrarBotao(Component comp, String texto) {
        if (comp instanceof JButton && ((JButton) comp).getText().equalsIgnoreCase(texto)) return (JButton) comp;
        if (comp instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) comp).getComponents()) {
                JButton b = encontrarBotao(c, texto);
                if (b != null) return b;
            }
        }
        return null;
    }

    private JTextField encontrarCampoTexto(Component comp, String dicaOuNome) {
        if (comp instanceof JTextField && !(comp instanceof JPasswordField)) return (JTextField) comp;
        if (comp instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) comp).getComponents()) {
                JTextField f = encontrarCampoTexto(c, dicaOuNome);
                if (f != null) return f;
            }
        }
        return null;
    }

}
