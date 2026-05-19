package st.project;

import javax.swing.SwingUtilities;

import st.project.view.VistaLogin;

public class Main {
    public static void main(String[] args) {
        // Agora o ponto de entrada levanta primeiro o  de Login
        SwingUtilities.invokeLater(() -> {
            new VistaLogin().setVisible(true);
        });
    }
}