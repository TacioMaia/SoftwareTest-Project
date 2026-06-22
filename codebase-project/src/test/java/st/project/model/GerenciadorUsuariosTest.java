package st.project.model;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



public class GerenciadorUsuariosTest {

    private Path arquivoTesteUnitario;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Criamos um ficheiro temporário para os testes unitários não sujarem o disco
        arquivoTesteUnitario = Paths.get("usuarios_teste_unitario.txt");
        Files.deleteIfExists(arquivoTesteUnitario);

        // 2. Apagamos qualquer instância anterior do Singleton da memória
        Field instancia = GerenciadorUsuarios.class.getDeclaredField("instancia");
        instancia.setAccessible(true);
        instancia.set(null, null);

        // 3. Injetamos o caminho do ficheiro falso na variável do Gerenciador
        Field fArquivo = GerenciadorUsuarios.class.getDeclaredField("ARQUIVO");
        fArquivo.setAccessible(true);
        fArquivo.set(null, arquivoTesteUnitario);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Limpamos o ficheiro falso após cada teste
        Files.deleteIfExists(arquivoTesteUnitario);
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

    @Test
    @DisplayName("Teste de Fronteira: Impedir cadastro com campos vazios ou muito grandes")
    void testFronteira_TamanhoCredenciais() {
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        
        // Testa caminhos vazios 
        org.assertj.core.api.Assertions.assertThat(ger.cadastrar("", "123", "X")).isFalse();
        org.assertj.core.api.Assertions.assertThat(ger.cadastrar("123", "", "X")).isFalse();
        
        // Testa caminhos maiores que 15 caracteres 
        org.assertj.core.api.Assertions.assertThat(ger.cadastrar("usuarioMuitoGrande123", "123", "X")).isFalse();
        org.assertj.core.api.Assertions.assertThat(ger.cadastrar("user", "senhaMuitoGrande123", "X")).isFalse();
    }

    @Test
    @DisplayName("Teste Estrutural:  (Arquivo corrompido ou inacessível)")
    void testCatchIOExceptions() throws Exception {
        //  Apaga qualquer instância anterior do Singleton
        java.lang.reflect.Field instancia = GerenciadorUsuarios.class.getDeclaredField("instancia");
        instancia.setAccessible(true);
        instancia.set(null, null);

        // Cria uma pasta temporária no sistema
        java.nio.file.Path dirInvalido = java.nio.file.Files.createTempDirectory("dir_invalido");

        java.lang.reflect.Field fArquivo = GerenciadorUsuarios.class.getDeclaredField("ARQUIVO");
        fArquivo.setAccessible(true);
        fArquivo.set(null, dirInvalido);

        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();

        ger.cadastrar("testeExcecao", "123", "X");

        java.nio.file.Files.deleteIfExists(dirInvalido);
    }

}
