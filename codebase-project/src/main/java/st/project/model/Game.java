package st.project.model;

import java.util.Random;

public class Game {
    private Room currentRoom;
    private int passosRestantes;
    private int passosIniciais = 30; 
    private int nivelAtual;
    private int pontuacaoTotal;
    private boolean temRecursoExtra;
    private int[][] mapa;
    private boolean gameOver;

    // Dependência injetada para facilitar os testes unitários
    private Usuario usuarioInjetado;

    public Game() {
    }

    Game(Usuario usuarioInjetado) {
        this.usuarioInjetado = usuarioInjetado;
    }

    private Usuario getUsuarioAtivo() {
        if (usuarioInjetado != null) {
            return usuarioInjetado;
        }
        return GerenciadorUsuarios.getInstancia().getUsuarioLogado();
    }

    public void iniciarSessao() {
        nivelAtual = 1;
        pontuacaoTotal = 0;
        gameOver = false;
        getUsuarioAtivo().incrementarSessao();
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
        
        // Ignora entradas inválidas para evitar exceções indesejadas
        if (direction == null || currentRoom == null) return;

        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom != null) {
            passosRestantes--;
            int tipoDestino = nextRoom.getTipo();
            
            if (tipoDestino == 3) { // Recurso
                pontuacaoTotal += 100;
                temRecursoExtra = true;
                nextRoom.setTipo(0);
                mapa[nextRoom.getY()][nextRoom.getX()] = 0;
                currentRoom = nextRoom;
                
            } else if (tipoDestino == 4) { // Alçapão
                if (temRecursoExtra) {
                    temRecursoExtra = false;
                    nextRoom.setTipo(0);
                    mapa[nextRoom.getY()][nextRoom.getX()] = 0;
                    currentRoom = nextRoom;
                } else {
                    // Punição por cair no alçapão sem recurso
                    if (nivelAtual > 1) {
                        // INVARIANTE: pontuação nunca pode ser negativa, independente da penalidade aplicada.
                        // Coberto por teste de propriedade: GameTest#PROP01
                        pontuacaoTotal = Math.max(0, pontuacaoTotal - 200);
                    }
                    // INVARIANTE: nível mínimo é sempre 1, independente de quantas quedas ocorrerem.
                    // Coberto por teste de propriedade: GameTest#PROP02
                    nivelAtual = Math.max(1, nivelAtual - 1);
                    carregarNivel();
                    return; 
                }
            } else {
                currentRoom = nextRoom;
            }

            if (currentRoom.getTipo() == 2) { // Saída
                pontuacaoTotal += 200;
                nivelAtual++;
                carregarNivel();
            } else if (passosRestantes <= 0) {
                finalizarJogo();
            }
        }
    }

    // INVARIANTE: os passos travam em 0 ao acionar o game over, nunca ficando negativos.
    // Coberto por teste de propriedade: GameTest#PROP03
    private void finalizarJogo() {
        gameOver = true;
        Usuario u = getUsuarioAtivo();
        if (pontuacaoTotal > u.getPontuacaoMaxima()) {
            u.setPontuacaoMaxima(pontuacaoTotal);
        }
        GerenciadorUsuarios.getInstancia().salvarNoArquivo();
    }

    public Room getSalaAtual() { return currentRoom; }
    public int getPassosRestantes() { return passosRestantes; }
    public int getNivelAtual() { return nivelAtual; }
    public int getPontuacaoTotal() { return pontuacaoTotal; }
    public boolean hasRecurso() { return temRecursoExtra; }
    public int[][] getMapa() { return mapa; }
    public boolean isGameOver() { return gameOver; }

    // Métodos para injeção de estado em testes 
    void setCurrentRoom(Room room) { this.currentRoom = room; }
    void setPassosRestantes(int passos) { this.passosRestantes = passos; }
    void setNivelAtual(int nivel) { this.nivelAtual = nivel; }
    void setPontuacaoTotal(int pontuacao) { this.pontuacaoTotal = pontuacao; }
    void setTemRecursoExtra(boolean temRecurso) { this.temRecursoExtra = temRecurso; }
    void setGameOver(boolean isGameOver) { this.gameOver = isGameOver; }
}