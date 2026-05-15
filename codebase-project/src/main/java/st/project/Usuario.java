package st.project;

public class Usuario {
    private String login;
    private String senha;
    private String avatar;
    private int pontuacaoMaxima;
    private int sessoesJogadas;

    public Usuario(String login, String senha, String avatar) {
        this.login = login;
        this.senha = senha;
        this.avatar = avatar;
        this.pontuacaoMaxima = 0;
        this.sessoesJogadas = 0;
    }

    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getAvatar() { return avatar; }
    
    public int getPontuacaoMaxima() { return pontuacaoMaxima; }
    public void setPontuacaoMaxima(int pontuacao) { this.pontuacaoMaxima = pontuacao; }
    
    public int getSessoesJogadas() { return sessoesJogadas; }
    public void incrementarSessao() { this.sessoesJogadas++; }
}