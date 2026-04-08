package st.project;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

//import st.project.Room;

class RoomTest {

    @Test
    void testeCriacaoDeSala() {
        Room sala = new Room (1, 2, true);
        assertThat(sala.getX()).isEqualTo(1);
        assertThat(sala.getY()).isEqualTo(2);
        assertThat(sala.isProfessorRoom()).isTrue();
    }

    @Test
    void testeAdicaoERecuperacaoDeSaidas() {
        Room sala1 = new Room(0, 0, false);
        Room sala2 = new Room(0, 1, false);
        
        sala1.setExit("north", sala2);
        assertThat(sala1.getExit("north")).isEqualTo(sala2);
        assertThat(sala1.getExit("south")).isNull();

    }

    @Test
    void testeSobrescreverSaida() {
        Room sala1 = new Room(0, 0, false);
        Room sala2 = new Room(0, 1, false);
        Room sala3 = new Room(1, 0, false);
        
        sala1.setExit("north", sala2);
        sala1.setExit("north", sala3); 
        assertThat(sala1.getExit("north")).isEqualTo(sala3);
    }

}