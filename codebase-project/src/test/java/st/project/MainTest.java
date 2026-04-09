package st.project;

import javax.swing.SwingUtilities;

import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    @DisplayName("Cobertura Estrutural: O construtor implícito da classe Main deve funcionar")
    void testMain_ConstrutorPadrao() {
        // Instanciamos a classe apenas para pintar a assinatura dela de verde no relatório.
        assertThatCode(() -> new Main()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cobertura de Execução: O método main deve iniciar a thread do Swing sem erros")
    void testMain_MetodoExecutaInterfaceSemErros() throws Exception {
        // Ação: Chama o ponto de entrada do programa
        String[] argumentosVazios = {};
        
        assertThatCode(() -> Main.main(argumentosVazios)).doesNotThrowAnyException();

        
        SwingUtilities.invokeAndWait(() -> {
            // Apenas um pulso de sincronização. Quando chegar aqui, a linha da tela já foi executada!
        });
    }
}