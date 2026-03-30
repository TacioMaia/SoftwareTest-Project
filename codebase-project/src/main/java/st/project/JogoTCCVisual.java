package st.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

public class JogoTCCVisual extends JFrame {
    private final int TAMANHO_CELULA = 60; // Tamanho de cada quadrado/sprite
    private final int LINHAS = 10;
    private final int COLUNAS = 10;

    private Game game;
    private JLabel labelStatus;
    private PainelJogo painelJogo;

    // Variáveis para guardar as imagens (Sprites)
    private Image spriteChao;
    private Image spriteMesa;
    private Image spriteProfessor;
    private Image spriteJogador;

    public JogoTCCVisual() {
        game = new Game();
        carregarSprites(); // Carrega as imagens antes de iniciar a tela

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

        // Captura de teclado melhorada
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                processarTecla(e.getKeyCode());
            }
        });

        pack();
        setLocationRelativeTo(null);
        setFocusable(true);
        requestFocusInWindow(); // Garante o foco para o teclado funcionar de primeira
    }

    /**
     * Tenta carregar as imagens da pasta src/main/resources/images/
     */
    private void carregarSprites() {
        spriteChao = carregarImagem("/images/chao.png");
        spriteMesa = carregarImagem("/images/mesa.png");
        spriteProfessor = carregarImagem("/images/professor.png");
        spriteJogador = carregarImagem("/images/jogador.png");
    }

    /**
     * Método auxiliar para carregar uma imagem do classpath de forma segura
     */
    private Image carregarImagem(String caminho) {
        URL url = getClass().getResource(caminho);
        if (url == null) {
            System.err.println("ERRO: Imagem não encontrada em: " + caminho);
            // Retorna uma imagem vazia para não dar erro de NullPointerException ao desenhar
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
        
        // Feedback visual de urgência
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
            // Ícone customizado na mensagem de vitória (opcional)
            JOptionPane.showMessageDialog(this, 
                "🏆 SUCESSO! TCC entregue!\nVocê foi aprovado com nota 10!", 
                "Parabéns!", 
                JOptionPane.INFORMATION_MESSAGE, 
                new ImageIcon(spriteProfessor));
            game.iniciarJogo();
            atualizarInterface();
        } else if (game.isDerrota()) {
            JOptionPane.showMessageDialog(this, 
                "💸 FIM DA LINHA...\nO prazo acabou e você pegou DP.", 
                "Game Over", 
                JOptionPane.ERROR_MESSAGE);
            game.iniciarJogo();
            atualizarInterface();
        }
    }

    /**
     * Painel de desenho atualizado para usar drawImage ao invés de fillRect/fillOval
     */
    class PainelJogo extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Ativa Anti-aliasing para deixar as bordas das imagens mais suaves
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Desenhar o Fundo (Chão) em toda a grade primeiro
            for (int linha = 0; linha < LINHAS; linha++) {
                for (int coluna = 0; coluna < COLUNAS; coluna++) {
                    int x = coluna * TAMANHO_CELULA;
                    int y = linha * TAMANHO_CELULA;
                    
                    // Desenha o sprite do chão. Se não carregar, usa cinza claro.
                    if (spriteChao != null && spriteChao.getWidth(null) > 0) {
                        g.drawImage(spriteChao, x, y, TAMANHO_CELULA, TAMANHO_CELULA, null);
                    } else {
                        g.setColor(new Color(240, 240, 240));
                        g.fillRect(x, y, TAMANHO_CELULA, TAMANHO_CELULA);
                        g.setColor(Color.LIGHT_GRAY);
                        g.drawRect(x, y, TAMANHO_CELULA, TAMANHO_CELULA); // Mantém a grade visual
                    }
                }
            }

            // 2. Desenhar Obstáculos e Professor por cima do chão
            for (int linha = 0; linha < LINHAS; linha++) {
                for (int coluna = 0; coluna < COLUNAS; coluna++) {
                    int x = coluna * TAMANHO_CELULA;
                    int y = linha * TAMANHO_CELULA;
                    
                    int tipoCelula = Game.MAPA_NIVEL[linha][coluna];

                    if (tipoCelula == 1) { // MESA/OBSTÁCULO
                        if (spriteMesa != null && spriteMesa.getWidth(null) > 0) {
                            g.drawImage(spriteMesa, x, y, TAMANHO_CELULA, TAMANHO_CELULA, null);
                        } else {
                            // Fallback se a imagem falhar (retângulo marrom antigo)
                            g.setColor(new Color(139, 69, 19));
                            g.fillRect(x + 2, y + 2, TAMANHO_CELULA - 4, TAMANHO_CELULA - 4);
                        }
                    } else if (tipoCelula == 2) { // PROFESSOR/DESTINO
                        if (spriteProfessor != null && spriteProfessor.getWidth(null) > 0) {
                            g.drawImage(spriteProfessor, x, y, TAMANHO_CELULA, TAMANHO_CELULA, null);
                        } else {
                            // Fallback (retângulo verde antigo)
                            g.setColor(Color.GREEN);
                            g.fillRect(x + 2, y + 2, TAMANHO_CELULA - 4, TAMANHO_CELULA - 4);
                        }
                    }
                }
            }

            // 3. Desenhar o Jogador por cima de tudo
            Room atual = game.getSalaAtual();
            if (atual != null) {
                int x = atual.getX() * TAMANHO_CELULA;
                int y = atual.getY() * TAMANHO_CELULA;
                
                if (spriteJogador != null && spriteJogador.getWidth(null) > 0) {
                    // Desenha o sprite do jogador levemente centralizado na célula
                    g.drawImage(spriteJogador, x + 2, y + 2, TAMANHO_CELULA - 4, TAMANHO_CELULA - 4, null);
                } else {
                    // Fallback (círculo azul antigo)
                    g.setColor(Color.BLUE);
                    g.fillOval(x + 5, y + 5, TAMANHO_CELULA - 10, TAMANHO_CELULA - 10);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JogoTCCVisual().setVisible(true);
        });
    }
}