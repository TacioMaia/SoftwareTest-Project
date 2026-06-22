package st.project.view;

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

import st.project.model.GerenciadorUsuarios;
import st.project.model.Usuario;



public class TelaLoginTest {

    

    @Test
    @DisplayName("Teste de Domínio: Fluxo de Login com Falha")
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
    @DisplayName("Teste de Domínio: Fluxo de Login com Sucesso")
    void testCliqueBotaoEntrarComSucesso() throws Exception {
        // Agora forçamos o mock a devolver TRUE
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.autenticar(any(), any())).thenReturn(true);

        Usuario usuarioFalso = new Usuario("jogador", "senha123", "avatar");
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
    @DisplayName("Teste de Fronteira: UI bloqueia registro com campos vazios (MC/DC 100%)")
    void testRegistarCamposVazios() throws Exception {
        try (MockedStatic<javax.swing.JOptionPane> optionPaneMock = mockStatic(javax.swing.JOptionPane.class)) {
            VistaLogin vistaLogin = new VistaLogin();
            
            // Prepara a reflexão para injetar valores nos dois campos
            java.lang.reflect.Field fLogin = VistaLogin.class.getDeclaredField("txtLogin");
            fLogin.setAccessible(true);
            javax.swing.JTextField txtLogin = (javax.swing.JTextField) fLogin.get(vistaLogin);

            java.lang.reflect.Field fSenha = VistaLogin.class.getDeclaredField("txtSenha");
            fSenha.setAccessible(true);
            javax.swing.JPasswordField txtSenha = (javax.swing.JPasswordField) fSenha.get(vistaLogin);
            
            JButton btnRegistar = encontrarBotao(vistaLogin, "Registrar Novo");

            // --- CASO 1: Login vazio, Senha preenchida ---
            // O Java lê "log.isEmpty()" (Verdadeiro) e entra no IF.
            txtLogin.setText(""); 
            txtSenha.setText("senhaValida");
            btnRegistar.doClick();

            // --- CASO 2: Login preenchido, Senha vazia ---
            // O Java lê "log.isEmpty()" (Falso) e é OBRIGADO a ler o "sen.isEmpty()" (Verdadeiro).
            txtLogin.setText("loginValido"); 
            txtSenha.setText("");
            btnRegistar.doClick();

            // Verificamos se o bloqueio disparou exatamente 2 vezes
            optionPaneMock.verify(() -> javax.swing.JOptionPane.showMessageDialog(
                Mockito.any(java.awt.Component.class), 
                Mockito.eq("Login e Senha não podem estar vazios!")
            ), Mockito.times(2));
        }
    }

    @Test
    @DisplayName("Teste de Fronteira: UI bloqueia registro com credenciais muito grandes (MC/DC 100%)")
    void testRegistarCamposGrandes() throws Exception {
        try (MockedStatic<javax.swing.JOptionPane> optionPaneMock = mockStatic(javax.swing.JOptionPane.class)) {
            VistaLogin vistaLogin = new VistaLogin();
            
            java.lang.reflect.Field fLogin = VistaLogin.class.getDeclaredField("txtLogin");
            fLogin.setAccessible(true);
            javax.swing.JTextField txtLogin = (javax.swing.JTextField) fLogin.get(vistaLogin);

            java.lang.reflect.Field fSenha = VistaLogin.class.getDeclaredField("txtSenha");
            fSenha.setAccessible(true);
            javax.swing.JPasswordField txtSenha = (javax.swing.JPasswordField) fSenha.get(vistaLogin);
            
            JButton btnRegistar = encontrarBotao(vistaLogin, "Registrar Novo");

            // --- CASO 1: Login Gigante, Senha normal ---
            // Avalia o lado ESQUERDO do ||
            txtLogin.setText("1234567890123456"); // 16 caracteres
            txtSenha.setText("senhaOk");
            btnRegistar.doClick();

            // --- CASO 2: Login normal, Senha Gigante ---
            // Avalia o lado DIREITO do ||
            txtLogin.setText("loginOk"); 
            txtSenha.setText("1234567890123456"); // 16 caracteres
            btnRegistar.doClick();

            // Verificamos se o bloqueio disparou exatamente 2 vezes
            optionPaneMock.verify(() -> javax.swing.JOptionPane.showMessageDialog(
                Mockito.any(java.awt.Component.class), 
                Mockito.eq("O Login e a Senha devem ter no máximo 15 caracteres!")
            ), Mockito.times(2));
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
