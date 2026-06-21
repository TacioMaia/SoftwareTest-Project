package st.project.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorUsuarios {
    private static GerenciadorUsuarios instancia;
    private List<Usuario> usuarios;
    private Usuario usuarioLogado;
    
    
    private static final Path ARQUIVO = Paths.get("usuarios.txt");

    private GerenciadorUsuarios() {
        usuarios = new ArrayList<>();
        carregarDoArquivo(); 

        
        if (usuarios.stream().noneMatch(u -> u.getLogin().equals("admin"))) {
            usuarios.add(new Usuario("admin", "admin", "Admin"));
            salvarNoArquivo();
        }
    }

    public static GerenciadorUsuarios getInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorUsuarios();
        }
        return instancia;
    }

    public boolean cadastrar(String login, String senha, String avatar) {
        
        if (login.isEmpty() || login.length() > 15 || senha.isEmpty() || senha.length() > 15) {
            return false; 
        }

        for (Usuario u : usuarios) {
            if (u.getLogin().equals(login)) return false;
        }
        usuarios.add(new Usuario(login, senha, avatar));
        salvarNoArquivo(); 
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
        salvarNoArquivo(); 
    }

    
    private void carregarDoArquivo() {
        if (Files.exists(ARQUIVO)) {
            try {
                List<String> linhas = Files.readAllLines(ARQUIVO);
                for (String linha : linhas) {
                    Usuario u = Usuario.fromCSV(linha);
                    if (u != null) usuarios.add(u);
                }
            } catch (IOException e) {
                System.err.println("Erro ao carregar usuários: " + e.getMessage());
            }
        }
    }

    
    public void salvarNoArquivo() {
        try {
            List<String> linhas = new ArrayList<>();
            for (Usuario u : usuarios) {
                linhas.add(u.toCSV());
            }
            Files.write(ARQUIVO, linhas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar usuários: " + e.getMessage());
        }
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
}