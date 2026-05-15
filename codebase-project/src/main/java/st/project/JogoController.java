package st.project;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;

public class JogoController {
    private Game model;
    private VistaJogo view;

    public JogoController(Game model, VistaJogo view) {
        this.model = model;
        this.view = view;

        this.view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                processarInput(e.getKeyCode());
            }
        });
    }

    public void iniciar() {
        model.iniciarSessao();
        view.atualizarEcra(model);
        view.setVisible(true);
    }

    private void processarInput(int codigoTecla) {
        if (codigoTecla == KeyEvent.VK_UP || codigoTecla == KeyEvent.VK_W) model.mover("north");
        else if (codigoTecla == KeyEvent.VK_DOWN || codigoTecla == KeyEvent.VK_S) model.mover("south");
        else if (codigoTecla == KeyEvent.VK_LEFT || codigoTecla == KeyEvent.VK_A) model.mover("west");
        else if (codigoTecla == KeyEvent.VK_RIGHT || codigoTecla == KeyEvent.VK_D) model.mover("east");
        
        view.atualizarEcra(model);

        if (model.isGameOver()) {
            view.mostrarMensagemFim();
            int resp = JOptionPane.showConfirmDialog(view, "Queres jogar outra sessão?", "Sessão Terminada", JOptionPane.YES_NO_OPTION);
            
            if (resp == JOptionPane.YES_OPTION) {
                model.iniciarSessao();
                view.atualizarEcra(model);
            } else {
                view.dispose();
                new VistaLogin().setVisible(true); // Volta ao ecrã inicial
            }
        }
    }
}