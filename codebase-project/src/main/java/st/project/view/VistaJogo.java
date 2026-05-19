package st.project.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import st.project.model.Room;
import st.project.model.Game;

public class VistaJogo extends JFrame {
    private JLabel labelStatus;
    private PainelJogo painelJogo;
    private Game gameModel;

    // Variáveis para armazenar as imagens
    private Image imgPiso, imgParede, imgProf, imgRecurso, imgAlcapao, imgPlayer;
    private JButton btnRanking; // Transformado em atributo para testabilidade

    public VistaJogo() {
        carregarImagens();
        setTitle("Corrida do TCC - Desafio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        labelStatus = new JLabel("", SwingConstants.CENTER);
        labelStatus.setFont(new Font("Arial", Font.BOLD, 16));
        labelStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelStatus, BorderLayout.NORTH);

        painelJogo = new PainelJogo();
        painelJogo.setPreferredSize(new Dimension(600, 600)); 
        add(painelJogo, BorderLayout.CENTER);
        
        JPanel pnlSul = new JPanel();
        btnRanking = new JButton("Ver Ranking");
        btnRanking.addActionListener(e -> abrirRanking());
        btnRanking.setFocusable(false); 
        pnlSul.add(btnRanking);
        add(pnlSul, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setFocusable(true);
    }

    // Encapsulado para facilitar os testes da ação do botão
    void abrirRanking() {
        new VistaRanking().setVisible(true);
    }

    private void carregarImagens() {
        imgPiso = carregar("/images/chao.png");
        imgParede = carregar("/images/mesa.png");
        imgProf = carregar("/images/professor.png");
        imgRecurso = carregar("/images/recurso.png");
        imgAlcapao = carregar("/images/alcapao.png");
        imgPlayer = carregar("/images/jogador.png");
    }

    private Image carregar(String caminho) {
        URL url = getClass().getResource(caminho);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        return null; // Retorna null caso a imagem não seja encontrada
    }

    public void atualizarTela(Game game) {
        this.gameModel = game;
        String status = String.format("Nível: %d | Pontos: %d | Passos: %d | Recurso: %s", 
            game.getNivelAtual(), game.getPontuacaoTotal(), game.getPassosRestantes(), 
            game.hasRecurso() ? "ATIVADO" : "FALTA");
            
        labelStatus.setText(status);
        painelJogo.repaint();
    }

    public void mostrarMensagemFim() {
        JOptionPane.showMessageDialog(this, 
            "Fim de Jogo! Pontuação Final: " + gameModel.getPontuacaoTotal(), 
            "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    class PainelJogo extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gameModel == null) return;
            
            int[][] mapa = gameModel.getMapa();
            Room atual = gameModel.getSalaAtual();

            for (int l = 0; l < 10; l++) {
                for (int c = 0; c < 10; c++) {
                    int x = c * 60;
                    int y = l * 60;
                    int tipo = mapa[l][c];

                    // Desenha o Piso primeiro em todas as células
                    if (imgPiso != null) g.drawImage(imgPiso, x, y, 60, 60, null);
                    else {
                        g.setColor(new Color(240, 240, 240));
                        g.fillRect(x, y, 60, 60);
                    }

                    // Desenha o objeto da célula
                    if (tipo == 1 && imgParede != null) g.drawImage(imgParede, x, y, 60, 60, null);
                    else if (tipo == 2 && imgProf != null) g.drawImage(imgProf, x, y, 60, 60, null);
                    else if (tipo == 3 && imgRecurso != null) g.drawImage(imgRecurso, x, y, 60, 60, null);
                    else if (tipo == 4 && imgAlcapao != null) g.drawImage(imgAlcapao, x, y, 60, 60, null);
                    
                    // Fallback para cores caso a imagem falhe
                    if (tipo == 1 && imgParede == null) { g.setColor(new Color(139, 69, 19)); g.fillRect(x, y, 60, 60); }
                    
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, 60, 60);
                }
            }

            if (atual != null) {
                if (imgPlayer != null) g.drawImage(imgPlayer, atual.getX() * 60 + 5, atual.getY() * 60 + 5, 50, 50, null);
                else {
                    g.setColor(Color.BLUE);
                    g.fillOval(atual.getX() * 60 + 10, atual.getY() * 60 + 10, 40, 40);
                }
            }
        }
    }

    // ===============================================================================
    // MÉTODOS "PACKAGE-PRIVATE" (DEFAULT) PARA TESTABILIDADE (Slide 7)
    // Permite injetar cenários de falha de imagem e acessar componentes internos
    // ===============================================================================
    void setImgPiso(Image img) { this.imgPiso = img; }
    void setImgParede(Image img) { this.imgParede = img; }
    void setImgProf(Image img) { this.imgProf = img; }
    void setImgRecurso(Image img) { this.imgRecurso = img; }
    void setImgAlcapao(Image img) { this.imgAlcapao = img; }
    void setImgPlayer(Image img) { this.imgPlayer = img; }
    
    JLabel getLabelStatus() { return labelStatus; }
    PainelJogo getPainelJogo() { return painelJogo; }
    JButton getBtnRanking() { return btnRanking; }
}