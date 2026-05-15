package st.project;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorUsuarios {
    private static GerenciadorUsuarios instancia;
    private List<Usuario> usuarios;
    private Usuario usuarioLogado;

    private GerenciadorUsuarios() {
        usuarios = new ArrayList<>();
        // Superusuário (admin) exigido no requisito
        usuarios.add(new Usuario("admin", "admin", "👑 Admin"));
    }

    public static GerenciadorUsuarios getInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorUsuarios();
        }
        return instancia;
    }

    public boolean cadastrar(String login, String senha, String avatar) {
        for (Usuario u : usuarios) {
            if (u.getLogin().equals(login)) return false; // Impede login repetido
        }
        usuarios.add(new Usuario(login, senha, avatar));
        return true;
    }

    public boolean autenticar(String login, String senha) {
        for (Usuario u : usuarios) {
            if (u.getLogin().equals(login) && u.getSenha().equals(senha)) {
                usuarioLogado = u;
                return true;
            }
        }
        return false;
    }

    public void removerUsuario(String login) {
        usuarios.removeIf(u -> u.getLogin().equals(login) && !login.equals("admin"));
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
}