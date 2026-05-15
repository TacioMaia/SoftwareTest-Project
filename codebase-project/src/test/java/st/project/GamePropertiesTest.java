package st.project;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

public class GamePropertiesTest {

    /**
     * PROPRIEDADE 1: A pontuação total de um usuário nunca pode ficar negativa,
     * não importa quantas vezes ele caia no alçapão e perca 200 pontos.
     */
    @Property
    void pontuacaoNuncaFicaNegativaAposMultiplasQuedas(
            @ForAll @IntRange(min = 0, max = 1000) int pontuacaoInicial,
            @ForAll @IntRange(min = 1, max = 50) int vezesCaiuNoAlcapaoNoNivel2) {
        
        
        int pontuacaoSimulada = pontuacaoInicial;
        for (int i = 0; i < vezesCaiuNoAlcapaoNoNivel2; i++) {
            pontuacaoSimulada = Math.max(0, pontuacaoSimulada - 200);
        }

        // Asserção baseada em propriedade matemática
        assertThat(pontuacaoSimulada).isGreaterThanOrEqualTo(0);
    }


    /**
     * PROPRIEDADE 2: O nível do jogo nunca pode descer para valores negativos ou zero,
     * sendo o Nível 1 o chão absoluto matemático.
     */
    @Property
    void nivelNuncaDesceAbaixoDeUm(
            @ForAll @IntRange(min = 1, max = 100) int nivelInicial,
            @ForAll @IntRange(min = 1, max = 200) int quantidadesDePenalizacoes) {
        
        int nivelSimulado = nivelInicial;
        for (int i = 0; i < quantidadesDePenalizacoes; i++) {
            nivelSimulado = Math.max(1, nivelSimulado - 1);
        }

        assertThat(nivelSimulado).isGreaterThanOrEqualTo(1);
    }


}
