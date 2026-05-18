package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VistaJogoTest {

    private VistaJogo vista;
    private Game gameMock;
    private Graphics graphicsSpy;

    @BeforeEach
    void setUp() {
        vista = new VistaJogo();
        gameMock = mock(Game.class);
        
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        graphicsSpy = spy(img.getGraphics());
    }

    @Test
    @DisplayName("Teste Estrutural: Atualizar Tela deve formatar o texto do label corretamente")
    void deveAtualizarLabelDeStatusCorretamente() {
        when(gameMock.getNivelAtual()).thenReturn(2);
        when(gameMock.getPontuacaoTotal()).thenReturn(500);
        when(gameMock.getPassosRestantes()).thenReturn(20);
        when(gameMock.hasRecurso()).thenReturn(true);

        vista.atualizarTela(gameMock);

        String textoEsperado = "Nível: 2 | Pontos: 500 | Passos: 20 | Recurso: ATIVADO";
        assertThat(vista.getLabelStatus().getText()).isEqualTo(textoEsperado);
    }

    @Test
    @DisplayName("Teste Estrutural/Fronteira: Atualizar Tela com status de Recurso FALTA")
    void deveAtualizarLabelDeStatusSemRecurso() {
        when(gameMock.getNivelAtual()).thenReturn(1);
        when(gameMock.getPontuacaoTotal()).thenReturn(0);
        when(gameMock.getPassosRestantes()).thenReturn(55);
        when(gameMock.hasRecurso()).thenReturn(false);

        vista.atualizarTela(gameMock);

        assertThat(vista.getLabelStatus().getText()).contains("Recurso: FALTA");
    }

    @Test
    @DisplayName("MC/DC: Componente de Pintura com gameModel nulo não deve fazer nada (Contrato)")
    void deveRetornarCedoNoPaintComponentSeGameModelForNulo() {
        vista.getPainelJogo().paintComponent(graphicsSpy);
        verify(graphicsSpy, never()).drawImage(any(), anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("MC/DC: Componente de Pintura sem sala atual (Cobre ramificação atual == null)")
    void devePintarSemSalaAtual() {
        int[][] mapaTeste = new int[10][10]; // Mapa zerado
        
        when(gameMock.getMapa()).thenReturn(mapaTeste);
        when(gameMock.getSalaAtual()).thenReturn(null); // Aqui cobre o if(atual != null) dando FALSO

        vista.atualizarTela(gameMock);
        vista.getPainelJogo().paintComponent(graphicsSpy);

        // Nenhuma imagem ou formato do jogador deve ter sido renderizado
        verify(graphicsSpy, never()).fillOval(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("MC/DC: Componente de Pintura carregando todos os tipos de blocos COM imagens")
    void devePintarTodosOsTiposDeBlocosComImagens() {
        int[][] mapaTeste = new int[10][10];
        mapaTeste[0][0] = 0; 
        mapaTeste[0][1] = 1; 
        mapaTeste[0][2] = 2; 
        mapaTeste[0][3] = 3; 
        mapaTeste[0][4] = 4; 
        mapaTeste[0][5] = 99; // Cobre um tipo não mapeado para garantir que não lance exceção (100% de MC/DC)

        Room roomMock = mock(Room.class);
        when(roomMock.getX()).thenReturn(0);
        when(roomMock.getY()).thenReturn(0);

        when(gameMock.getMapa()).thenReturn(mapaTeste);
        when(gameMock.getSalaAtual()).thenReturn(roomMock);

        vista.atualizarTela(gameMock);
        vista.getPainelJogo().paintComponent(graphicsSpy);

        verify(graphicsSpy, atLeastOnce()).drawImage(any(), anyInt(), anyInt(), anyInt(), anyInt(), isNull());
    }

    @Test
    @DisplayName("MC/DC: Componente de Pintura ativando os fallbacks visuais SEM imagens")
    void devePintarCoresDeFallbackQuandoImagensFaltarem() {
        vista.setImgPiso(null);
        vista.setImgParede(null);
        vista.setImgProf(null);
        vista.setImgRecurso(null);
        vista.setImgAlcapao(null);
        vista.setImgPlayer(null);

        int[][] mapaTeste = new int[10][10];
        mapaTeste[0][0] = 1; // Dispara if do fallback corrompido
        mapaTeste[0][1] = 2; // Dispara else-if com imagem null (avalia Falso)
        mapaTeste[0][2] = 3; // Dispara else-if com imagem null (avalia Falso)
        mapaTeste[0][3] = 4; // Dispara else-if com imagem null (avalia Falso)

        Room roomMock = mock(Room.class);
        when(roomMock.getX()).thenReturn(5); 
        when(roomMock.getY()).thenReturn(5);

        when(gameMock.getMapa()).thenReturn(mapaTeste);
        when(gameMock.getSalaAtual()).thenReturn(roomMock);

        vista.atualizarTela(gameMock);
        vista.getPainelJogo().paintComponent(graphicsSpy);

        verify(graphicsSpy, atLeastOnce()).fillRect(anyInt(), anyInt(), anyInt(), anyInt());
        verify(graphicsSpy, atLeastOnce()).fillOval(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Estrutural: Cobrir falha no carregamento de imagem inexistente (url == null)")
    void deveRetornarNullAoCarregarImagemInexistente() throws Exception {
        // Uso da técnica de Reflection para cobrir o método privado carregar() no cenário de caminho inexistente
        Method metodoCarregar = VistaJogo.class.getDeclaredMethod("carregar", String.class);
        metodoCarregar.setAccessible(true);
        Image img = (Image) metodoCarregar.invoke(vista, "/images/imagem_que_nao_existe.png");
        
        assertThat(img).isNull();
    }

    @Test
    @DisplayName("Dublê Avançado: Deve exibir o popup de Game Over interceptado via Static Mock")
    void deveMostrarMensagemFimDeJogoCorreta() {
        when(gameMock.getPontuacaoTotal()).thenReturn(999);
        vista.atualizarTela(gameMock);

        try (MockedStatic<JOptionPane> mockPane = mockStatic(JOptionPane.class)) {
            
            vista.mostrarMensagemFim();
            
            mockPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(vista),
                    contains("999"),
                    eq("Game Over"),
                    eq(JOptionPane.INFORMATION_MESSAGE)
            ));
        }
    }
    
    @Test
    @DisplayName("Contrato UI: O botão de Ranking deve existir e invocar a janela corretamente")
    void devePossuirBotaoDeRankingComAcao() {
        try (MockedConstruction<VistaRanking> mockRanking = mockConstruction(VistaRanking.class)) {
            
            vista.getBtnRanking().doClick(); 
            
            assertThat(mockRanking.constructed()).hasSize(1);
            verify(mockRanking.constructed().get(0)).setVisible(true);
        }
    }
}