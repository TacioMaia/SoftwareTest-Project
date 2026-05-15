package st.project;

import java.util.Random;

public class Game {
    private Room currentRoom;
    private int passosRestantes;
    private int passosIniciais = 55; 
    private int nivelAtual;
    private int pontuacaoTotal;
    private boolean temRecursoExtra;
    private int[][] mapa;
    private boolean gameOver;

    public void iniciarSessao() {
        nivelAtual = 1;
        pontuacaoTotal = 0;
        gameOver = false;
        GerenciadorUsuarios.getInstancia().getUsuarioLogado().incrementarSessao();
        carregarNivel();
    }

    private void carregarNivel() {
        temRecursoExtra = false;
        passosRestantes = passosIniciais;
        gerarMapaAleatorio();
        criarSalas();
    }

    private void gerarMapaAleatorio() {
        int[][][] bancoDeMapas = {
            {
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
            },
            {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
            },
            {
                {0, 1, 0, 0, 0, 1, 0, 0, 0, 2},
                {0, 1, 0, 1, 0, 1, 0, 1, 1, 1},
                {0, 1, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 1, 1, 1, 1, 1, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 1, 0},
                {0, 1, 1, 1, 1, 1, 0, 1, 1, 0},
                {0, 1, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 1, 1, 1, 1, 0, 0}
            }
        };

        Random rand = new Random();
        int indiceSorteado = rand.nextInt(bancoDeMapas.length);

        mapa = new int[10][10];
        for (int l = 0; l < 10; l++) {
            for (int c = 0; c < 10; c++) {
                mapa[l][c] = bancoDeMapas[indiceSorteado][l][c];
            }
        }

        posicionarElementoAleatorio(4); 
        posicionarElementoAleatorio(3); 
    }

    private void posicionarElementoAleatorio(int tipo) {
        Random rand = new Random();
        int l, c;
        do {
            l = rand.nextInt(10);
            c = rand.nextInt(10);
        } while (mapa[l][c] != 0 || (l == 9 && c == 0));
        
        mapa[l][c] = tipo;
    }

    private void criarSalas() {
        Room[][] grid = new Room[10][10];
        for (int l = 0; l < 10; l++) {
            for (int c = 0; c < 10; c++) {
                if (mapa[l][c] != 1) { 
                    grid[l][c] = new Room(c, l, mapa[l][c]);
                }
            }
        }
        for (int l = 0; l < 10; l++) {
            for (int c = 0; c < 10; c++) {
                Room sala = grid[l][c];
                if (sala != null) {
                    if (l > 0 && grid[l - 1][c] != null) sala.setExit("north", grid[l - 1][c]);
                    if (l < 9 && grid[l + 1][c] != null) sala.setExit("south", grid[l + 1][c]);
                    if (c > 0 && grid[l][c - 1] != null) sala.setExit("west", grid[l][c - 1]);
                    if (c < 9 && grid[l][c + 1] != null) sala.setExit("east", grid[l][c + 1]);
                }
            }
        }
        currentRoom = grid[9][0]; 
    }

    public void mover(String direction) {
        if (gameOver) return;

        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom != null) {
            passosRestantes--;
            int tipoDestino = nextRoom.getTipo();
            
            if (tipoDestino == 3) {
                // GANHA 100 PONTOS AO PEGAR O RECURSO
                pontuacaoTotal += 100;
                temRecursoExtra = true;
                nextRoom.setTipo(0);
                mapa[nextRoom.getY()][nextRoom.getX()] = 0;
                currentRoom = nextRoom;
                
            } else if (tipoDestino == 4) {
                if (temRecursoExtra) {
                    temRecursoExtra = false;
                    nextRoom.setTipo(0);
                    mapa[nextRoom.getY()][nextRoom.getX()] = 0;
                    currentRoom = nextRoom;
                } else {
                    // PERDE 200 PONTOS AO VOLTAR DE FASE (Exceto no Nível 1)
                    if (nivelAtual > 1) {
                        pontuacaoTotal = Math.max(0, pontuacaoTotal - 200);
                    }
                    nivelAtual = Math.max(1, nivelAtual - 1);
                    carregarNivel();
                    return; 
                }
            } else {
                currentRoom = nextRoom;
            }

            if (currentRoom.getTipo() == 2) {
                // GANHA 200 PONTOS AO PASSAR DE FASE
                pontuacaoTotal += 200;
                nivelAtual++;
                carregarNivel();
            } else if (passosRestantes <= 0) {
                finalizarJogo();
            }
        }
    }

    private void finalizarJogo() {
        gameOver = true;
        Usuario u = GerenciadorUsuarios.getInstancia().getUsuarioLogado();
        if (pontuacaoTotal > u.getPontuacaoMaxima()) {
            u.setPontuacaoMaxima(pontuacaoTotal);
        }
    }

    public Room getSalaAtual() { return currentRoom; }
    public int getPassosRestantes() { return passosRestantes; }
    public int getNivelAtual() { return nivelAtual; }
    public int getPontuacaoTotal() { return pontuacaoTotal; }
    public boolean hasRecurso() { return temRecursoExtra; }
    public int[][] getMapa() { return mapa; }
    public boolean isGameOver() { return gameOver; }
}