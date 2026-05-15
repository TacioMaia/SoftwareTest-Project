package st.project;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GerenciadorUsuariosTest {

    @BeforeEach
    void setUp() throws Exception {
        // Limpa o Singleton da memória antes de cada teste para garantir isolamento
        Field instancia = GerenciadorUsuarios.class.getDeclaredField("instancia");
        instancia.setAccessible(true);
        instancia.set(null, null);
    }

    @Test
    @DisplayName("Teste de Fronteira: Impedir cadastro de usuários duplicados")
    void testFronteira_CadastroDuplicado() {
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        
        boolean sucesso1 = ger.cadastrar("aluno1", "123", "A");
        assertThat(sucesso1).isTrue();
        
        //  O mesmo login na mesma lista
        boolean sucesso2 = ger.cadastrar("aluno1", "456", "B");
        assertThat(sucesso2).isFalse(); // Deve ser barrado
        assertThat(ger.getUsuarios()).hasSize(2); // admin + aluno1
    }

    @Test
    @DisplayName("Teste de Domínio: Autenticação com sucesso e falha")
    void testDominio_Autenticacao() {
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        ger.cadastrar("teste", "senha123", "X");

        assertThat(ger.autenticar("teste", "senha123")).isTrue();
        assertThat(ger.getUsuarioLogado().getLogin()).isEqualTo("teste");

        assertThat(ger.autenticar("teste", "senhaErrada")).isFalse();
        assertThat(ger.autenticar("naoExiste", "123")).isFalse();
    }

    @Test
    @DisplayName("Teste Estrutural: Remoção de usuário comum e blindagem do admin")
    void testEstrutural_RemocaoUsuarios() {
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        ger.cadastrar("aluno", "123", "A");

        ger.removerUsuario("aluno");
        assertThat(ger.getUsuarios()).noneMatch(u -> u.getLogin().equals("aluno")); // Foi removido

        // Tenta remover o admin 
        ger.removerUsuario("admin");
        assertThat(ger.getUsuarios()).anyMatch(u -> u.getLogin().equals("admin")); // Continua lá
    }

    @Test
    @DisplayName("Teste Estrutural: Cobertura do Padrão Singleton (Caminho Falso do IF)")
    void testEstrutural_Singleton() {
        GerenciadorUsuarios ger1 = GerenciadorUsuarios.getInstancia(); 
        GerenciadorUsuarios ger2 = GerenciadorUsuarios.getInstancia(); 
        assertThat(ger1).isSameAs(ger2);
    }

}
