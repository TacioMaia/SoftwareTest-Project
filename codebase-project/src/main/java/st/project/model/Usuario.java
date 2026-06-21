package st.project.model;

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
    

    public void setSessoesJogadas(int sessoes) { this.sessoesJogadas = sessoes; }

   
    public String toCSV() {
        return login + ";" + senha + ";" + avatar + ";" + pontuacaoMaxima + ";" + sessoesJogadas;
    }

   
    public static Usuario fromCSV(String linha) {
        String[] dados = linha.split(";");
        if (dados.length >= 5) {
            Usuario u = new Usuario(dados[0], dados[1], dados[2]);
            u.setPontuacaoMaxima(Integer.parseInt(dados[3]));
            u.setSessoesJogadas(Integer.parseInt(dados[4]));
            return u;
        }
        return null;
    }
}