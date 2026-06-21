package st.project.view;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import st.project.controller.JogoController;
import st.project.model.Game;
import st.project.model.GerenciadorUsuarios;

public class VistaLogin extends JFrame {
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JTextField txtAvatar;
    private JButton btnEntrar, btnRegistar;

    public VistaLogin() {
        setTitle("Login - Corrida do TCC");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel(" Login:", SwingConstants.LEFT));
        txtLogin = new JTextField();
        add(txtLogin);

        add(new JLabel(" Senha:", SwingConstants.LEFT));
        txtSenha = new JPasswordField();
        add(txtSenha);

        add(new JLabel(" Avatar (Nome/Emoji):", SwingConstants.LEFT));
        txtAvatar = new JTextField();
        add(txtAvatar);

        btnEntrar = new JButton("Entrar no Jogo");
        btnRegistar = new JButton("Registar Novo");
        add(btnEntrar);
        add(btnRegistar);

        configurarEventos();
    }

    private void configurarEventos() {
        btnEntrar.addActionListener(e -> {
            String log = txtLogin.getText();
            String sen = new String(txtSenha.getPassword());
            if (GerenciadorUsuarios.getInstancia().autenticar(log, sen)) {
                Game model = new Game();
                VistaJogo view = new VistaJogo();
                new JogoController(model, view).iniciar();
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Credenciais inválidas!");
            }
        });

         btnRegistar.addActionListener(e -> {
                    String log = txtLogin.getText().trim(); 
                    String sen = new String(txtSenha.getPassword());
                    String ava = txtAvatar.getText().isEmpty() ? "👤" : txtAvatar.getText();
                    
                    
                    if (log.isEmpty() || sen.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Login e Senha não podem estar vazios!");
                        return; 
                    }
                    if (log.length() > 15 || sen.length() > 15) {
                        JOptionPane.showMessageDialog(this, "O Login e a Senha devem ter no máximo 15 caracteres!");
                        return; 
                    }

                    if (GerenciadorUsuarios.getInstancia().cadastrar(log, sen, ava)) {
                        JOptionPane.showMessageDialog(this, "Registo efetuado com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Este utilizador já existe.");
                    }
                });
    }
}