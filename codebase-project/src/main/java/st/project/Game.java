package st.project;

public class Game {
    private Room currentRoom;
    private int passosRestantes;
    private int passosIniciais;
    private int[][] mapa;

    public Game(int passosIniciais, int[][] mapa) {
        this.passosIniciais = passosIniciais;
        this.mapa = mapa;
        iniciarJogo();
    }

    public void iniciarJogo() {
        this.passosRestantes = passosIniciais;
        criarSalas();
    }

    private void criarSalas() {
        int linhas = mapa.length;
        int colunas = mapa[0].length;
        Room[][] grid = new Room[linhas][colunas];

        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                if (mapa[linha][coluna] != 1) { 
                    boolean prof = (mapa[linha][coluna] == 2);
                    grid[linha][coluna] = new Room(coluna, linha, prof);
                }
            }
        }

        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                Room sala = grid[linha][coluna];
                if (sala != null) {
                    if (linha > 0 && grid[linha - 1][coluna] != null) sala.setExit("north", grid[linha - 1][coluna]);
                    if (linha < linhas - 1 && grid[linha + 1][coluna] != null) sala.setExit("south", grid[linha + 1][coluna]);
                    if (coluna > 0 && grid[linha][coluna - 1] != null) sala.setExit("west", grid[linha][coluna - 1]);
                    if (coluna < colunas - 1 && grid[linha][coluna + 1] != null) sala.setExit("east", grid[linha][coluna + 1]);
                }
            }
        }
        
        if (grid[linhas - 1][0] != null) {
            currentRoom = grid[linhas - 1][0];
        } else {
            for(int l = linhas - 1; l >= 0; l--) {
                for(int c = 0; c < colunas; c++){
                    if(grid[l][c] != null){
                        currentRoom = grid[l][c];
                        break;
                    }
                }
                if(currentRoom != null) break;
            }
        }
    }

    public void mover(String direction) {
        if (isDerrota() || isVitoria()) return; 

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