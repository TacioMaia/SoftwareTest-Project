package st.project;

import javax.swing.*;
import java.awt.*;

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
            String log = txtLogin.getText();
            String sen = new String(txtSenha.getPassword());
            String ava = txtAvatar.getText().isEmpty() ? "👤" : txtAvatar.getText();
            
            if (GerenciadorUsuarios.getInstancia().cadastrar(log, sen, ava)) {
                JOptionPane.showMessageDialog(this, "Registo efetuado com sucesso!");
            } else {
                JOptionPane.showMessageDialog(this, "Este utilizador já existe.");
            }
        });
    }
}