package st.project;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

//import st.project.Room;

class RoomTest {

@Test
    void testeCriacaoDeSala() {
        // Testando os novos tipos (0=Piso, 2=Professor, 3=Recurso, 4=Alcapao)
        Room salaComum = new Room(1, 2, 0);
        Room salaProf = new Room(2, 2, 2);

        assertThat(salaComum.getX()).isEqualTo(1);
        assertThat(salaComum.getTipo()).isEqualTo(0);
        assertThat(salaComum.isProfessorRoom()).isFalse();
        
        assertThat(salaProf.isProfessorRoom()).isTrue();
    }

    @Test
    void testeAdicaoERecuperacaoDeSaidas() {
        Room sala1 = new Room(0, 0, 0);
        Room sala2 = new Room(0, 1, 0);
        
        sala1.setExit("north", sala2);
        assertThat(sala1.getExit("north")).isEqualTo(sala2);
        assertThat(sala1.getExit("south")).isNull();
    }

    @Test
    @DisplayName("Teste Estrutural: Cobertura de Getters e Setters pendentes")
    void testeCoordenadaYESetTipo() {
        Room sala = new Room(5, 8, 0);
        assertThat(sala.getY()).isEqualTo(8);
        sala.setTipo(4); // Muda a sala para um Alçapão
        assertThat(sala.getTipo()).isEqualTo(4);
    }

}