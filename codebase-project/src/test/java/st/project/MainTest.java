package st.project;

import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;

class MainTest {

    @Test
    @DisplayName("Cobertura Estrutural: O construtor implícito da classe Main deve funcionar")
    void testMain_ConstrutorPadrao() {
        // O JaCoCo desconta pontos se o construtor vazio gerado pelo Java não for executado.
        // Instanciamos a classe apenas para pintar a assinatura dela de verde no relatório.
        assertThatCode(() -> new Main()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cobertura de Execução: O método main deve iniciar a thread do Swing sem erros")
    void testMain_MetodoExecutaInterfaceSemErros() throws Exception {
        // Ação: Chama o ponto de entrada do programa
        String[] argumentosVazios = {};
        
        assertThatCode(() -> Main.main(argumentosVazios)).doesNotThrowAnyException();

        // O GRANDE TRUQUE DO SWING PARA O JACOCO:
        // Como o Main usa 'invokeLater', a tela é criada de forma assíncrona (em segundo plano).
        // Se o teste acabar muito rápido, o JaCoCo não vê a tela a ser criada e acusa linha vermelha.
        // O 'invokeAndWait' abaixo cria uma tarefa vazia e obriga o teste a ESPERAR
        // a thread do Swing terminar de desenhar a tela antes de dar o teste como concluído.
        SwingUtilities.invokeAndWait(() -> {
            // Apenas um pulso de sincronização. Quando chegar aqui, a linha da tela já foi executada!
        });
    }
}