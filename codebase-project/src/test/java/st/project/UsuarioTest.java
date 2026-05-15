package st.project;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UsuarioTest {

    @Test
    @DisplayName("Teste de Domínio: Criação de Usuário e inicialização correta")
    void testCriacaoUsuario() {
        Usuario u = new Usuario("joao123", "senha123", "joao");
        
        assertThat(u.getLogin()).isEqualTo("joao123");
        assertThat(u.getSenha()).isEqualTo("senha123");
        assertThat(u.getAvatar()).isEqualTo("joao");
        assertThat(u.getPontuacaoMaxima()).isEqualTo(0);
        assertThat(u.getSessoesJogadas()).isEqualTo(0);
    }

    @Test
    @DisplayName("Teste de Domínio: Incrementar sessão e atualizar pontuação")
    void testAtualizacaoDados() {
        Usuario u = new Usuario("maria", "12345", "maria");
        
        u.incrementarSessao();
        u.incrementarSessao();
        assertThat(u.getSessoesJogadas()).isEqualTo(2);

        u.setPontuacaoMaxima(500);
        assertThat(u.getPontuacaoMaxima()).isEqualTo(500);
    }

}
