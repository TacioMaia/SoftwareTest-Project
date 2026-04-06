package st.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

public class JogoTCCVisual extends JFrame {
    private final int TAMANHO_CELULA = 80; 
    private final int LINHAS = 10;
    private final int COLUNAS = 10;

    public static final int[][] MAPA_PRODUCAO = {
        {0, 0, 0, 1, 1, 0, 0, 0, 0, 2},
        {0, 1, 0, 0, 0, 0, 1, 1, 0, 0},
        {0, 1, 1, 1, 0, 1, 1, 0, 0, 1},
        {0, 0, 0, 1, 0, 0, 0, 0, 1, 1},
        {1, 1, 0, 1, 1, 1, 0, 1, 1, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 1, 1, 1, 1, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 1, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    private Game game;
    private JLabel labelStatus;
    private PainelJogo painelJogo;

    private Image spriteChao;
    private Image spriteMesa;
    private Image spriteProfessor;
    private Image spriteJogador;

    public JogoTCCVisual() {
        game = new Game(20, MAPA_PRODUCAO); 
        carregarSprites();

        setTitle("Corrida do TCC - Entrega Urgente!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        labelStatus = new JLabel("Passos Restantes: " + game.getPassosRestantes(), SwingConstants.CENTER);
        labelStatus.setFont(new Font("Arial", Font.BOLD, 22));
        labelStatus.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        labelStatus.setOpaque(true);
        labelStatus.setBackground(new Color(220, 220, 220));
        add(labelStatus, BorderLayout.NORTH);

        painelJogo = new PainelJogo();
        painelJogo.setPreferredSize(new Dimension(COLUNAS * TAMANHO_CELULA, LINHAS * TAMANHO_CELULA));
        add(painelJogo, BorderLayout.CENTER);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                processarTecla(e.getKeyCode());
            }
        });

        pack();
        setLocationRelativeTo(null);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void carregarSprites() {
        spriteChao = carregarImagem("/images/chao.png");
        spriteMesa = carregarImagem("/images/mesa.png");
        spriteProfessor = carregarImagem("/images/professor.png");
        spriteJogador = carregarImagem("/images/jogador.png");
    }

    private Image carregarImagem(String caminho) {
        URL url = getClass().getResource(caminho);
        if (url == null) {
            System.err.println("ERRO: Imagem não encontrada em: " + caminho);
            return new ImageIcon(new byte[0]).getImage(); 
        }
        return new ImageIcon(url).getImage();
    }

    private void processarTecla(int codigoTecla) {
        if (game.isDerrota() || game.isVitoria()) return;

        boolean moveu = false;

        if (codigoTecla == KeyEvent.VK_UP || codigoTecla == KeyEvent.VK_W) {
            game.mover("north"); moveu = true;
        } else if (codigoTecla == KeyEvent.VK_DOWN || codigoTecla == KeyEvent.VK_S) {
            game.mover("south"); moveu = true;
        } else if (codigoTecla == KeyEvent.VK_LEFT || codigoTecla == KeyEvent.VK_A) {
            game.mover("west"); moveu = true;
        } else if (codigoTecla == KeyEvent.VK_RIGHT || codigoTecla == KeyEvent.VK_D) {
            game.mover("east"); moveu = true;
        }

        if (moveu) {
            atualizarInterface();
            verificarFimDeJogo();
        }
    }

    private void atualizarInterface() {
        int passos = game.getPassosRestantes();
        labelStatus.setText("Passos Restantes: " + passos);
        
        if (passos <= 5) {
            labelStatus.setForeground(Color.RED);
            labelStatus.setBackground(new Color(255, 200, 200));
        } else {
            labelStatus.setForeground(Color.BLACK);
            labelStatus.setBackground(new Color(220, 220, 220));
        }
        painelJogo.repaint();
    }

    private void verificarFimDeJogo() {
        if (game.isVitoria()) {
            JOptionPane.showMessageDialog(this, 
                "🏆 SUCESSO! TCC entregue!\nFoste aprovado com nota máxima!", 
                "Parabéns!", 
                JOptionPane.INFORMATION_MESSAGE, 
                new ImageIcon(spriteProfessor));
            game.iniciarJogo();
            atualizarInterface();
        } else if (game.isDerrota()) {
            JOptionPane.showMessageDialog(this, 
                "💸 FIM DA LINHA...\nO prazo acabou e chumbaste.", 
                "Game Over", 
                JOptionPane.ERROR_MESSAGE);
            game.iniciarJogo();
            atualizarInterface();
        }
    }

    class PainelJogo extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int linha = 0; linha < LINHAS; linha++) {
                for (int coluna = 0; coluna < COLUNAS; coluna++) {
                    int x = coluna * TAMANHO_CELULA;
                    int y = linha * TAMANHO_CELULA;
                    
                    if (spriteChao != null && spriteChao.getWidth(null) > 0) {
                        g.drawImage(spriteChao, x, y, TAMANHO_CELULA, TAMANHO_CELULA, null);
                    } else {
                        g.setColor(new Color(240, 240, 240));
                        g.fillRect(x, y, TAMANHO_CELULA, TAMANHO_CELULA);
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawRect(x, y, TAMANHO_CELULA, TAMANHO_CELULA); 
                    }
                }
            }

            for (int linha = 0; linha < LINHAS; linha++) {
                for (int coluna = 0; coluna < COLUNAS; coluna++) {
                    int x = coluna * TAMANHO_CELULA;
                    int y = linha * TAMANHO_CELULA;
                    
                    int tipoCelula = JogoTCCVisual.MAPA_PRODUCAO[linha][coluna];

                    if (tipoCelula == 1) { 
                        if (spriteMesa != null && spriteMesa.getWidth(null) > 0) {
                            g.drawImage(spriteMesa, x, y, TAMANHO_CELULA, TAMANHO_CELULA, null);
                        } else {
                            g.setColor(new Color(139, 69, 19));
                            g.fillRect(x + 2, y + 2, TAMANHO_CELULA - 4, TAMANHO_CELULA - 4);
                        }
                    } else if (tipoCelula == 2) { 
                        if (spriteProfessor != null && spriteProfessor.getWidth(null) > 0) {
                            g.drawImage(spriteProfessor, x, y, TAMANHO_CELULA, TAMANHO_CELULA, null);
                        } else {
                            g.setColor(Color.GREEN);
                            g.fillRect(x + 2, y + 2, TAMANHO_CELULA - 4, TAMANHO_CELULA - 4);
                        }
                    }
                }
            }

            Room atual = game.getSalaAtual();
            if (atual != null) {
                int x = atual.getX() * TAMANHO_CELULA;
                int y = atual.getY() * TAMANHO_CELULA;
                
                if (spriteJogador != null && spriteJogador.getWidth(null) > 0) {
                    g.drawImage(spriteJogador, x + 2, y + 2, TAMANHO_CELULA - 4, TAMANHO_CELULA - 4, null);
                } else {
                    g.setColor(Color.BLUE);
                    g.fillOval(x + 5, y + 5, TAMANHO_CELULA - 10, TAMANHO_CELULA - 10);
                }
            }
        }
    }
}