package st.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class JogoTCCVisual extends JFrame {
    private final int TAMANHO = 50;
    private final int LINHAS = 10;
    private final int COLUNAS = 10;

    private Game game;
    private JLabel labelStatus;
    private PainelJogo painelJogo;

    public JogoTCCVisual() {
        game = new Game();

        setTitle("Corrida do TCC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        labelStatus = new JLabel("Passos Restantes: " + game.getPassosRestantes(), SwingConstants.CENTER);
        labelStatus.setFont(new Font("Arial", Font.BOLD, 18));
        labelStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelStatus, BorderLayout.NORTH);

        painelJogo = new PainelJogo();
        painelJogo.setPreferredSize(new Dimension(COLUNAS * TAMANHO, LINHAS * TAMANHO));
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
    }

    private void processarTecla(int codigoTecla) {
        if (game.isDerrota() || game.isVitoria()) return;

        Command comando = null;
        if (codigoTecla == KeyEvent.VK_UP || codigoTecla == KeyEvent.VK_W) comando = new Command(CommandWord.GO, "north");
        else if (codigoTecla == KeyEvent.VK_DOWN || codigoTecla == KeyEvent.VK_S) comando = new Command(CommandWord.GO, "south");
        else if (codigoTecla == KeyEvent.VK_LEFT || codigoTecla == KeyEvent.VK_A) comando = new Command(CommandWord.GO, "west");
        else if (codigoTecla == KeyEvent.VK_RIGHT || codigoTecla == KeyEvent.VK_D) comando = new Command(CommandWord.GO, "east");

        if (comando != null) {
            game.processarComando(comando);
            atualizarInterface();
            verificarFimDeJogo();
        }
    }

    private void atualizarInterface() {
        int passos = game.getPassosRestantes();
        labelStatus.setText("Passos Restantes: " + passos);
        if (passos <= 5) {
            labelStatus.setForeground(Color.RED);
        } else {
            labelStatus.setForeground(Color.BLACK);
        }
        painelJogo.repaint();
    }

    private void verificarFimDeJogo() {
        if (game.isVitoria()) {
            JOptionPane.showMessageDialog(this, "Sucesso! Você entregou o TCC e foi aprovado!");
            game.iniciarJogo();
            atualizarInterface();
        } else if (game.isDerrota()) {
            JOptionPane.showMessageDialog(this, "Fim da linha! O prazo acabou e você pegou DP...");
            game.iniciarJogo();
            atualizarInterface();
        }
    }

    class PainelJogo extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Room atual = game.getSalaAtual();

            for (int linha = 0; linha < LINHAS; linha++) {
                for (int coluna = 0; coluna < COLUNAS; coluna++) {
                    int x = coluna * TAMANHO;
                    int y = linha * TAMANHO;

                    g.setColor(new Color(240, 240, 240));
                    g.fillRect(x, y, TAMANHO, TAMANHO);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, TAMANHO, TAMANHO);
                }
            }

            int[][] mapa = {
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

            for (int linha = 0; linha < LINHAS; linha++) {
                for (int coluna = 0; coluna < COLUNAS; coluna++) {
                    int x = coluna * TAMANHO;
                    int y = linha * TAMANHO;
                    if (mapa[linha][coluna] == 1) {
                        g.setColor(new Color(139, 69, 19));
                        g.fillRect(x + 2, y + 2, TAMANHO - 4, TAMANHO - 4);
                    } else if (mapa[linha][coluna] == 2) {
                        g.setColor(Color.GREEN);
                        g.fillRect(x + 2, y + 2, TAMANHO - 4, TAMANHO - 4);
                    }
                }
            }

            if (atual != null) {
                g.setColor(Color.BLUE);
                g.fillOval(atual.getX() * TAMANHO + 5, atual.getY() * TAMANHO + 5, TAMANHO - 10, TAMANHO - 10);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JogoTCCVisual().setVisible(true);
        });
    }
}