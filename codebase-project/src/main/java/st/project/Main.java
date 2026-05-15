package st.project;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Agora o ponto de entrada levanta primeiro o ecrã de Login
        SwingUtilities.invokeLater(() -> {
            new VistaLogin().setVisible(true);
        });
    }
}