package st.project;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Inicia a interface gráfica na thread correta do Java Swing
        SwingUtilities.invokeLater(() -> {
            JogoTCCVisual telaDoJogo = new JogoTCCVisual();
            telaDoJogo.setVisible(true);
        });
    }
}