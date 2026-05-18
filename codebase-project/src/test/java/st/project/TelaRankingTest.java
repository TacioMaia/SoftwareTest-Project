package st.project;

import java.awt.Component;

import javax.swing.JButton;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;

public class TelaRankingTest {

    @Test
    @DisplayName("Contrato de Segurança: Botão 'Remover' oculto para utilizador comum")
    void testBotaoRemover_OcultoParaUsuarioComum() {
        // Simula um utilizador comum logado
        Usuario uComum = new Usuario("joao", "123", "X");
        
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.getUsuarioLogado()).thenReturn(uComum);

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class)) {
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaRanking vista = new VistaRanking();
            JButton btnRemover = encontrarBotao(vista, "Remover Utilizador Selecionado");

            // O botão pode não ter sido adicionado à tela (null) ou estar invisível (isVisible = false)
            boolean estaVisivel = (btnRemover != null && btnRemover.isVisible());
            assertThat(estaVisivel).isFalse();
        }
    }

    @Test
    @DisplayName("Contrato de Segurança: Botão 'Remover' visível para Admin")
    void testBotaoRemover_VisivelParaAdmin() {
        // Simula o superusuário logado
        Usuario uAdmin = new Usuario("admin", "admin", "A");
        
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.getUsuarioLogado()).thenReturn(uAdmin);

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class)) {
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaRanking vista = new VistaRanking();
            JButton btnRemover = encontrarBotao(vista, "Remover Utilizador Selecionado");

            // Para o admin, o botão TEM de existir e estar visível
            assertThat(btnRemover).isNotNull();
            assertThat(btnRemover.isVisible()).isTrue();
        }
    }

    @Test
    @DisplayName("Teste de Integração: A tabela deve ser preenchida com os utilizadores (Cobre o FOR)")
    void testPreenchimentoDaLista() {
        // Criamos um utilizador falso para aparecer no ranking
        Usuario u1 = new Usuario("jogador1", "senha", "👤");
        u1.setPontuacaoMaxima(500);
        
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        // O Mock agora devolve uma lista com o nosso utilizador (Isto faz o FOR rodar!)
        Mockito.when(gerMock.getUsuarios()).thenReturn(java.util.List.of(u1));
        Mockito.when(gerMock.getUsuarioLogado()).thenReturn(u1); // Utilizador comum logado

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class)) {
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaRanking vista = new VistaRanking();
            
            // Verifica se a lista existe na tela
            javax.swing.JList<String> lista = encontrarLista(vista);
            org.assertj.core.api.Assertions.assertThat(lista).isNotNull();
            
            // Verifica se o texto formatado entrou na lista (Cobre as linhas do modelo.addElement)
            org.assertj.core.api.Assertions.assertThat(lista.getModel().getSize()).isEqualTo(1);
            org.assertj.core.api.Assertions.assertThat(lista.getModel().getElementAt(0))
                .contains("👤 jogador1")
                .contains("Pontuação: 500");
        }
    }

    @Test
    @DisplayName("Teste de Integração: Admin remove um utilizador com sucesso")
    void testAcaoBotaoRemover() throws Exception {
        Usuario uAdmin = new Usuario("admin", "admin", "A");
        Usuario uAlvo = new Usuario("alvo", "123", "👤");
        
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.getUsuarioLogado()).thenReturn(uAdmin);
        Mockito.when(gerMock.getUsuarios()).thenReturn(java.util.List.of(uAlvo));

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class);
             MockedStatic<javax.swing.JOptionPane> optionPaneMock = mockStatic(javax.swing.JOptionPane.class)) {
            
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaRanking vista = new VistaRanking();
            
            // 1. Encontra a lista e simula o clique no utilizador "alvo"
            javax.swing.JList<String> lista = encontrarLista(vista);
            org.assertj.core.api.Assertions.assertThat(lista).isNotNull();
            lista.setSelectedIndex(0); // Seleciona o primeiro da lista

            // 2. Encontra o botão de remover e clica nele
            JButton btnRemover = encontrarBotao(vista, "Remover Utilizador Selecionado");
            org.assertj.core.api.Assertions.assertThat(btnRemover).isNotNull();
            
            for (java.awt.event.ActionListener al : btnRemover.getActionListeners()) {
                al.actionPerformed(new java.awt.event.ActionEvent(btnRemover, java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
            }

            // 3. Verifica se o Gerenciador foi chamado para remover exatamente o usuário
            Mockito.verify(gerMock).removerUsuario("alvo");

            
            optionPaneMock.verify(() -> javax.swing.JOptionPane.showMessageDialog(
                Mockito.any(java.awt.Component.class), 
                Mockito.eq("Utilizador removido.")
            ));
        
            org.assertj.core.api.Assertions.assertThat(vista.isDisplayable()).isFalse();
        }
    }

    @Test
    @DisplayName("Teste de Integração: Admin clica em Remover sem selecionar ninguém na lista")
    void testAcaoBotaoRemover_SemSelecao() throws Exception {
        Usuario uAdmin = new Usuario("admin", "admin", "A");
        GerenciadorUsuarios gerMock = Mockito.mock(GerenciadorUsuarios.class);
        Mockito.when(gerMock.getUsuarioLogado()).thenReturn(uAdmin);
        Mockito.when(gerMock.getUsuarios()).thenReturn(java.util.List.of()); // Lista vazia

        try (MockedStatic<GerenciadorUsuarios> singleton = mockStatic(GerenciadorUsuarios.class)) {
            singleton.when(GerenciadorUsuarios::getInstancia).thenReturn(gerMock);

            VistaRanking vista = new VistaRanking();
            
            // Garantimos que a lista não tem rigorosamente nada selecionado
            javax.swing.JList<String> lista = encontrarLista(vista);
            lista.clearSelection();

            JButton btnRemover = encontrarBotao(vista, "Remover Utilizador Selecionado");
            
            // Clica no botão sem ter selecionado ninguém
            for (java.awt.event.ActionListener al : btnRemover.getActionListeners()) {
                al.actionPerformed(new java.awt.event.ActionEvent(btnRemover, java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
            }

            Mockito.verify(gerMock, Mockito.never()).removerUsuario(Mockito.anyString());
        }
    }



    private JButton encontrarBotao(Component comp, String contemTexto) {
        if (comp instanceof JButton && ((JButton) comp).getText().contains(contemTexto)) return (JButton) comp;
        if (comp instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) comp).getComponents()) {
                JButton b = encontrarBotao(c, contemTexto);
                if (b != null) return b;
            }
        }
        return null;
    }

@SuppressWarnings("unchecked")
    private javax.swing.JList<String> encontrarLista(Component comp) {
        if (comp instanceof javax.swing.JList) return (javax.swing.JList<String>) comp;
        if (comp instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) comp).getComponents()) {
                javax.swing.JList<String> l = encontrarLista(c);
                if (l != null) return l;
            }
        }
        return null;
    }

}
