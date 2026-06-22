package st.project.view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import st.project.model.GerenciadorUsuarios;
import st.project.model.Usuario;

public class VistaRanking extends JFrame {
    public VistaRanking() {
        setTitle("Ranking Global");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        DefaultListModel<String> modelo = new DefaultListModel<>();
        List<Usuario> users = GerenciadorUsuarios.getInstancia().getUsuarios();
        
        for (Usuario u : users) {
            modelo.addElement(u.getAvatar() + " " + u.getLogin() + 
                " | Pontuação: " + u.getPontuacaoMaxima() + 
                " | Sessões: " + u.getSessoesJogadas());
        }

        JList<String> lista = new JList<>(modelo);
        add(new JScrollPane(lista), BorderLayout.CENTER);

        if (GerenciadorUsuarios.getInstancia().getUsuarioLogado().getLogin().equals("admin")) {
            JButton btnExcluir = new JButton("Remover Usuário Selecionado");
            btnExcluir.addActionListener(e -> {
                String selecao = lista.getSelectedValue();
                if (selecao != null) {
                    // Extrai o login que está na posição 1 do texto
                    String login = selecao.split(" ")[1];
                    GerenciadorUsuarios.getInstancia().removerUsuario(login);
                    JOptionPane.showMessageDialog(this, "Usuário removido.");
                    dispose(); 
                }
            });
            add(btnExcluir, BorderLayout.SOUTH);
        }
    }
}