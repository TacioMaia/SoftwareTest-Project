package st.project.integration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import st.project.model.GerenciadorUsuarios;
import st.project.model.Usuario;

public class IntegracaoTest {
    
    private Path arquivoTeste;

    @BeforeEach
    void setUp() throws Exception {
        arquivoTeste = Paths.get("usuarios_teste.txt");
        Files.deleteIfExists(arquivoTeste);

        Field instancia = GerenciadorUsuarios.class.getDeclaredField("instancia");
        instancia.setAccessible(true);
        instancia.set(null, null);

        Field fArquivo = GerenciadorUsuarios.class.getDeclaredField("ARQUIVO");
        fArquivo.setAccessible(true);
        fArquivo.set(null, arquivoTeste); 
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(arquivoTeste);
    }

    @Test
    @DisplayName("TI01: Cadastro deve persistir dados fisicamente no .txt")
    void testIntegracao_CadastroSalvaNoArquivo() throws IOException {
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        
        // Cadastra um usuário na memória
        ger.cadastrar("integracaoUser", "senha123", "user");

        // Verifica se o arquivofoi criado e contém o texto correto
        assertThat(Files.exists(arquivoTeste)).isTrue();
        List<String> linhasFisicas = Files.readAllLines(arquivoTeste);
        
        // Deve conter o admin e o novo usuário
        assertThat(linhasFisicas).hasSize(2);
        assertThat(linhasFisicas.get(1)).contains("integracaoUser;senha123;user;0;0");
    }

    @Test
    @DisplayName("TI02: Recuperar estado ao reiniciar o sistema")
    void testIntegracao_CarregarDoArquivoAoIniciar() throws Exception {
        // Escreve dados diretamente no disco simulando uma sessão anterior
        String dadosAntigos = "admin;admin;Admin;0;0\nvelhoUser;123;X;500;3\n";
        Files.writeString(arquivoTeste, dadosAntigos);

       
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();

        // Verificação da Integração
        List<Usuario> usuariosNaMemoria = ger.getUsuarios();
        assertThat(usuariosNaMemoria).hasSize(2);
        
        Usuario carregado = usuariosNaMemoria.get(1);
        assertThat(carregado.getLogin()).isEqualTo("velhoUser");
        assertThat(carregado.getPontuacaoMaxima()).isEqualTo(500); // Converteu o int com sucesso
        assertThat(carregado.getSessoesJogadas()).isEqualTo(3);
    }

    @Test
    @DisplayName("TI03: Sistema deve ignorar linhas corrompidas no arquivo .txt sem crashar")
    void testIntegracao_FicheiroCorrompido() throws Exception {
        // Simula um arquivo corrompido ou editado de forma errada
        String dadosCorrompidos = "admin;admin;Admin;0;0\n" + 
                                  "linha_sem_ponto_e_virgula\n" + 
                                  "userIncompleto;123;user\n" + // Faltam a pontuação e sessões
                                  "hackerUser;123;X;999;1\n";
        Files.writeString(arquivoTeste, dadosCorrompidos);

        //  tenta inicializar e ler o arquivo
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();

        // Deve ler o admin e o hackerUser perfeitamente, e ignorar as duas linhas corrompidas
        List<Usuario> usuariosLidos = ger.getUsuarios();
        
        assertThat(usuariosLidos).hasSize(2);
        assertThat(usuariosLidos).anyMatch(u -> u.getLogin().equals("admin"));
        assertThat(usuariosLidos).anyMatch(u -> u.getLogin().equals("hackerUser"));
        assertThat(usuariosLidos).noneMatch(u -> u.getLogin().equals("userIncompleto"));
    }

    @Test
    @DisplayName("TI04: Atualização de recorde deve ser refletida fisicamente no arquivo")
    void testIntegracao_AtualizaRecordeNoFicheiro() throws Exception {
        // Cadastra o usuário e força o sistema a escrever no arquivo
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        ger.cadastrar("jogadorVip", "senha", "vip");
        
        // Simula o fim de um jogo onde o recorde foi quebrado e a sessão incrementada
        Usuario u = ger.getUsuarios().stream()
                       .filter(user -> user.getLogin().equals("jogadorVip"))
                       .findFirst().get();
        
        u.setPontuacaoMaxima(850);
        u.setSessoesJogadas(5);
       
        ger.salvarNoArquivo();
     
        List<String> linhasFisicas = Files.readAllLines(arquivoTeste);
        
        // Verificamos se atualizou corretamente
        assertThat(linhasFisicas).anyMatch(linha -> linha.equals("jogadorVip;senha;vip;850;5"));
    }

    @Test
    @DisplayName("TI05: Remoção de usuário deve apagá-lo permanentemente do disco")
    void testIntegracao_RemocaoDeletaDoFicheiro() throws Exception {
        // Cria dois usuários
        GerenciadorUsuarios ger = GerenciadorUsuarios.getInstancia();
        ger.cadastrar("alvoRemocao", "123", "alvo");
        ger.cadastrar("sobrevivente", "123", "survivor");
        
        // Garante que estão no arquivo
        assertThat(Files.readAllLines(arquivoTeste)).hasSize(3); 
        
        // Remove um deles 
        ger.removerUsuario("alvoRemocao");
        
        // Verifica se foi removido do arquivo
        List<String> linhasAposRemocao = Files.readAllLines(arquivoTeste);
        
        assertThat(linhasAposRemocao).hasSize(2); 
        assertThat(linhasAposRemocao).noneMatch(linha -> linha.contains("alvoRemocao"));
        assertThat(linhasAposRemocao).anyMatch(linha -> linha.contains("sobrevivente"));
    }



}
