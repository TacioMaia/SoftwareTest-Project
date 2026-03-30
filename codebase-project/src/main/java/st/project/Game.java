package st.project;

public class Game {
    private Room currentRoom;
    private int passosRestantes;

    public static final int[][] MAPA_NIVEL = {
        {0, 0, 0, 1, 1, 0, 0, 0, 0, 2},
        {0, 1, 0, 0, 0, 0, 1, 1, 0, 0},
        {0, 1, 1, 1, 0, 1, 1, 0, 0, 1},
        {0, 0, 0, 1, 0, 0, 0, 0, 1, 1},
        {1, 1, 0, 1, 1, 1, 0, 1, 1, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 1, 1, 1, 1, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 1, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public Game() {
        iniciarJogo();
    }

    public void iniciarJogo() {
        passosRestantes = 20;
        criarSalas();
    }

    private void criarSalas() {
        Room[][] grid = new Room[10][10];

        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                if (MAPA_NIVEL[linha][coluna] != 1) {
                    boolean prof = (MAPA_NIVEL[linha][coluna] == 2);
                    grid[linha][coluna] = new Room(coluna, linha, prof);
                }
            }
        }

        for (int linha = 0; linha < 10; linha++) {
            for (int coluna = 0; coluna < 10; coluna++) {
                Room sala = grid[linha][coluna];
                if (sala != null) {
                    if (linha > 0 && grid[linha - 1][coluna] != null) sala.setExit("north", grid[linha - 1][coluna]);
                    if (linha < 9 && grid[linha + 1][coluna] != null) sala.setExit("south", grid[linha + 1][coluna]);
                    if (coluna > 0 && grid[linha][coluna - 1] != null) sala.setExit("west", grid[linha][coluna - 1]);
                    if (coluna < 9 && grid[linha][coluna + 1] != null) sala.setExit("east", grid[linha][coluna + 1]);
                }
            }
        }
        currentRoom = grid[9][0];
    }

    public void mover(String direction) {
        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom != null) {
            currentRoom = nextRoom;
            passosRestantes--;
        }
    }

    public Room getSalaAtual() { return currentRoom; }
    public int getPassosRestantes() { return passosRestantes; }
    public boolean isVitoria() { return currentRoom != null && currentRoom.isProfessorRoom(); }
    public boolean isDerrota() { return passosRestantes <= 0 && !isVitoria(); }
}