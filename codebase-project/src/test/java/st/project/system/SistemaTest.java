package st.project.system;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

import st.project.model.Game;
import st.project.model.GerenciadorUsuarios;
import st.project.model.Room;
import st.project.view.VistaJogo;
import st.project.view.VistaLogin;
import st.project.view.VistaRanking;

public class SistemaTest {

    @BeforeEach
    void setup() {
        GerenciadorUsuarios.getInstancia().getUsuarios().removeIf(u -> !u.getLogin().equals("admin"));
        fecharTodasAsJanelas();
    }

    @AfterEach
    void tearDown() {
        fecharTodasAsJanelas();
    }

    @Test
    @DisplayName("UJ01 - registrar um novo usuário pela tela de login com sucesso")
    void testUJ01_RegistrarNovoUsuarioComSucesso() throws Exception {
        VistaLogin login = new VistaLogin();
        login.setVisible(true);

        preencherCampo(login, "txtLogin", "jogador1");
        preencherCampo(login, "txtSenha", "senha123");
        preencherCampo(login, "txtAvatar", "av1");

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            clicarBotao(login, "btnRegistar");
            dialogo.verify(() -> JOptionPane.showMessageDialog(eq(login), eq("Registro efetuado com sucesso!")));
        }

        boolean registrado = GerenciadorUsuarios.getInstancia().getUsuarios().stream()
                .anyMatch(u -> u.getLogin().equals("jogador1") && u.getAvatar().equals("av1"));
        assertThat(registrado).isTrue();
    }

    @Test
    @DisplayName("UJ02 - tentar registrar com campos em branco bloqueia o cadastro")
    void testUJ02_RegistrarComCamposEmBrancoNaoPersisteNada() throws Exception {
        VistaLogin login = new VistaLogin();
        login.setVisible(true);

        preencherCampo(login, "txtLogin", "");
        preencherCampo(login, "txtSenha", "");

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            clicarBotao(login, "btnRegistar");
            dialogo.verify(() -> JOptionPane.showMessageDialog(eq(login), eq("Login e Senha não podem estar vazios!")));
        }

        assertThat(GerenciadorUsuarios.getInstancia().getUsuarios()).hasSize(1);
    }

    @Test
    @DisplayName("UJ03 - registrar um login que já existe não duplica o usuário")
    void testUJ03_RegistrarLoginDuplicadoNaoDuplicaUsuario() throws Exception {
        GerenciadorUsuarios.getInstancia().cadastrar("existente", "123456", "av");
        VistaLogin login = new VistaLogin();
        login.setVisible(true);

        preencherCampo(login, "txtLogin", "existente");
        preencherCampo(login, "txtSenha", "outraSenha");

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            clicarBotao(login, "btnRegistar");
            dialogo.verify(() -> JOptionPane.showMessageDialog(eq(login), eq("Este usuário já existe.")));
        }

        long ocorrencias = GerenciadorUsuarios.getInstancia().getUsuarios().stream()
                .filter(u -> u.getLogin().equals("existente")).count();
        assertThat(ocorrencias).isEqualTo(1L);
    }

    @Test
    @DisplayName("UJ04 - login com sucesso transiciona e abre a tela principal do jogo")
    void testUJ04_LoginComSucessoAbreTelaDoJogo() throws Exception {
        GerenciadorUsuarios.getInstancia().cadastrar("jogador2", "123456", "av");
        VistaLogin login = new VistaLogin();
        login.setVisible(true);

        preencherCampo(login, "txtLogin", "jogador2");
        preencherCampo(login, "txtSenha", "123456");
        clicarBotao(login, "btnEntrar");

        VistaJogo telaJogo = obterJanela(VistaJogo.class);
        assertThat(telaJogo).isNotNull();
        assertThat(login.isDisplayable()).isFalse();
    }

    @Test
    @DisplayName("UJ05 - login com credenciais inválidas mantém o jogador preso na tela de login")
    void testUJ05_LoginComCredenciaisInvalidasPermaneceNaTela() throws Exception {
        VistaLogin login = new VistaLogin();
        login.setVisible(true);

        preencherCampo(login, "txtLogin", "naoexiste");
        preencherCampo(login, "txtSenha", "errada");

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            clicarBotao(login, "btnEntrar");
            dialogo.verify(() -> JOptionPane.showMessageDialog(eq(login), eq("Credenciais inválidas!")));
        }

        assertThat(login.isDisplayable()).isTrue();
        assertThat(obterJanela(VistaJogo.class)).isNull();
    }

    @Test
    @DisplayName("UJ06 - jogador anda pelo labirinto e coleta o recurso")
    void testUJ06_JogadorColetaORecursoAndandoPeloLabirinto() throws Exception {
        VistaJogo tela = logarEAbrirJogo("jogador3");
        Game game = getGame(tela);

        forcarMapaComCaminhoPara(game, 3); // 3 = Recurso
        for (int tecla : encontrarCaminho(game, 3)) pressionarTecla(tela, tecla);

        assertThat(game.hasRecurso()).isTrue();
        assertThat(getLabelStatus(tela).getText()).contains("Recurso: ATIVADO");
    }

    @Test
    @DisplayName("UJ07 - jogador atravessa o alçapão tendo o recurso e segue em frente sem penalidade")
    void testUJ07_JogadorAtravessaAlcapaoComRecursoSemPenalidade() throws Exception {
        VistaJogo tela = logarEAbrirJogo("jogador4");
        Game game = getGame(tela);

        // Coleta o recurso primeiro
        forcarMapaComCaminhoPara(game, 3);
        for (int tecla : encontrarCaminho(game, 3)) pressionarTecla(tela, tecla);
        assertThat(game.hasRecurso()).isTrue();

        int nivelAntes = game.getNivelAtual();

        // Encontra o alçapão e atravessa
        forcarMapaComCaminhoPara(game, 4);
        for (int tecla : encontrarCaminho(game, 4)) pressionarTecla(tela, tecla);

        assertThat(game.getNivelAtual()).isEqualTo(nivelAntes);
        assertThat(game.hasRecurso()).isFalse();
        assertThat(game.isGameOver()).isFalse();
    }

    @Test
    @DisplayName("UJ08 - jogador cai no alçapão sem o recurso e o nível é reiniciado com um novo mapa")
    void testUJ08_JogadorCaiNoAlcapaoSemRecursoReiniciaNivel() throws Exception {
        VistaJogo tela = logarEAbrirJogo("jogador5");
        Game game = getGame(tela);

        forcarMapaComCaminhoPara(game, 4);
        for (int tecla : encontrarCaminho(game, 4)) pressionarTecla(tela, tecla);

        assertThat(game.getNivelAtual()).isEqualTo(1); // Nível reseta/mantém-se no mínimo
        assertThat(game.hasRecurso()).isFalse();
        assertThat(getLabelStatus(tela).getText()).contains("Passos: 30"); // Passos resetados, confirmando novo mapa
    }

    @Test
    @DisplayName("UJ09 - jogador alcança a saída, avança de nível e soma 200 pontos")
    void testUJ09_JogadorAlcancaSaidaEAvancaDeNivel() throws Exception {
        VistaJogo tela = logarEAbrirJogo("jogador6");
        Game game = getGame(tela);

        forcarMapaComCaminhoPara(game, 2); // 2 = Professor (Saída)
        for (int tecla : encontrarCaminho(game, 2)) pressionarTecla(tela, tecla);

        assertThat(game.getNivelAtual()).isEqualTo(2);
        assertThat(game.getPontuacaoTotal()).isGreaterThanOrEqualTo(200);
    }

    @Test
    @DisplayName("UJ10 - jogador logado abre o ranking pela tela do jogo e apenas visualiza")
    void testUJ10_JogadorAbreRanking() throws Exception {
        GerenciadorUsuarios.getInstancia().cadastrar("concorrente", "123456", "av");
        VistaJogo telaJogo = logarEAbrirJogo("jogador7");

        clicarBotao(telaJogo, "btnRanking");

        VistaRanking telaRanking = obterJanela(VistaRanking.class);
        assertThat(telaRanking).isNotNull();

        JList<String> lista = obterListaRanking(telaRanking);
        boolean temJogador7 = false, temConcorrente = false;
        
        for (int i = 0; i < lista.getModel().getSize(); i++) {
            if (lista.getModel().getElementAt(i).contains("jogador7")) temJogador7 = true;
            if (lista.getModel().getElementAt(i).contains("concorrente")) temConcorrente = true;
        }

        assertThat(temJogador7).isTrue();
        assertThat(temConcorrente).isTrue();
        assertThat(obterBotaoRemoverRanking(telaRanking)).isNull(); // Botão de Admin não deve existir
    }

    @Test
    @DisplayName("UJ11 - admin abre o ranking e remove um usuário selecionado")
    void testUJ11_AdminRemoveUsuarioPeloRanking() throws Exception {
        GerenciadorUsuarios.getInstancia().cadastrar("pararemover", "123456", "av");
        VistaJogo telaJogo = logarEAbrirJogo("admin"); // Loga como administrador

        clicarBotao(telaJogo, "btnRanking");
        VistaRanking telaRanking = obterJanela(VistaRanking.class);

        JList<String> lista = obterListaRanking(telaRanking);
        JButton btnExcluir = obterBotaoRemoverRanking(telaRanking);
        assertThat(btnExcluir).isNotNull(); // Admin visualiza o botão

        for (int i = 0; i < lista.getModel().getSize(); i++) {
            if (lista.getModel().getElementAt(i).contains("pararemover")) {
                lista.setSelectedIndex(i);
                break;
            }
        }

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            btnExcluir.doClick();
        }

        assertThat(GerenciadorUsuarios.getInstancia().getUsuarios()).noneMatch(u -> u.getLogin().equals("pararemover"));
    }

    @Test
    @DisplayName("UJ12 - ao esgotar os passos, escolher jogar novamente inicia uma nova sessão")
    void testUJ12_EsgotarPassosEEscolherJogarNovamenteIniciaNovaSessao() throws Exception {
        VistaJogo tela = logarEAbrirJogo("jogador8");
        Game game = getGame(tela);

        esgotarPassosAndandoEmCorredor(tela, game, 1);
        int ultimaTecla = obterTeclaDirecaoLivre(game);

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            dialogo.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), eq(JOptionPane.YES_NO_OPTION)))
                   .thenReturn(JOptionPane.YES_OPTION);
            
            pressionarTecla(tela, ultimaTecla); // Dá exatamente 1 passo, gastando o zero sem usar o laço
        }

        assertThat(game.isGameOver()).isFalse();
        assertThat(game.getNivelAtual()).isEqualTo(1); // Nível recomeçou
    }

    @Test
    @DisplayName("UJ13 - ao esgotar os passos, escolher não jogar novamente volta para a tela de login")
    void testUJ13_EsgotarPassosEEscolherNaoVoltaParaLogin() throws Exception {
        VistaJogo tela = logarEAbrirJogo("jogador9");
        Game game = getGame(tela);

        esgotarPassosAndandoEmCorredor(tela, game, 1);
        int ultimaTecla = obterTeclaDirecaoLivre(game);

        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            dialogo.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), eq(JOptionPane.YES_NO_OPTION)))
                   .thenReturn(JOptionPane.NO_OPTION);

            pressionarTecla(tela, ultimaTecla);
        }

        assertThat(tela.isDisplayable()).isFalse(); // Jogo fechou
        assertThat(obterJanela(VistaLogin.class)).isNotNull(); // Login voltou à tela
    }

    @Test
    @DisplayName("UJ14 - admin tenta remover um utilizador sem selecionar nenhum na lista")
    void testUJ14_AdminClicaRemoverSemSelecionar() throws Exception {
        // Cadastra um utilizador para garantir que a lista tem elementos
        GerenciadorUsuarios.getInstancia().cadastrar("intocavel", "123456", "av");
        VistaJogo telaJogo = logarEAbrirJogo("admin"); // Loga como administrador

        clicarBotao(telaJogo, "btnRanking");
        VistaRanking telaRanking = obterJanela(VistaRanking.class);

        JButton btnExcluir = obterBotaoRemoverRanking(telaRanking);
        assertThat(btnExcluir).isNotNull(); 

        
        try (MockedStatic<JOptionPane> dialogo = mockStatic(JOptionPane.class)) {
            btnExcluir.doClick();
            
           
            dialogo.verifyNoInteractions();
        }

       
        assertThat(telaRanking.isDisplayable()).isTrue();
        
       
        assertThat(GerenciadorUsuarios.getInstancia().getUsuarios())
            .anyMatch(u -> u.getLogin().equals("intocavel"));
    }

    // =========================================================================
    // MÉTODOS AUXILIARES 
    // =========================================================================

    private int obterTeclaDirecaoLivre(Game game) {
        Room atual = game.getSalaAtual();
        String[] direcoes = {"north", "south", "east", "west"};
        int[] keys = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT};
        
        for (int i = 0; i < 4; i++) {
            Room vizinho = atual.getExit(direcoes[i]);
            // Procura o primeiro vizinho que seja uma sala livre (tipo 0)
            if (vizinho != null && vizinho.getTipo() == 0) return keys[i]; 
        }
        return KeyEvent.VK_UP; // Fallback
    }

    private VistaJogo logarEAbrirJogo(String login) throws Exception {
        if (!login.equals("admin")) {
            GerenciadorUsuarios.getInstancia().cadastrar(login, "123456", "av");
        }
        VistaLogin telaLogin = new VistaLogin();
        telaLogin.setVisible(true);
        preencherCampo(telaLogin, "txtLogin", login);
        preencherCampo(telaLogin, "txtSenha", login.equals("admin") ? "admin" : "123456");
        clicarBotao(telaLogin, "btnEntrar");
        return obterJanela(VistaJogo.class);
    }

    private void preencherCampo(Object tela, String nomeCampo, String valor) throws Exception {
        Field field = tela.getClass().getDeclaredField(nomeCampo);
        field.setAccessible(true);
        Object componente = field.get(tela);
        if (componente instanceof JTextField) {
            ((JTextField) componente).setText(valor);
        }
    }

    private void clicarBotao(Object tela, String nomeBotao) throws Exception {
        Field field = tela.getClass().getDeclaredField(nomeBotao);
        field.setAccessible(true);
        JButton btn = (JButton) field.get(tela);
        btn.doClick();
    }

    private void pressionarTecla(VistaJogo tela, int keyCode) {
        KeyEvent evento = new KeyEvent(tela, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        for (KeyListener kl : tela.getKeyListeners()) {
            kl.keyPressed(evento);
        }
    }

    private <T extends Window> T obterJanela(Class<T> tipoJanela) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            for (Window w : Window.getWindows()) {
                if (tipoJanela.isInstance(w) && w.isDisplayable()) return tipoJanela.cast(w);
            }
            Thread.sleep(100);
        }
        return null;
    }

    private void fecharTodasAsJanelas() {
        for (Window w : Window.getWindows()) w.dispose();
    }

    private Game getGame(VistaJogo tela) throws Exception {
        Field field = VistaJogo.class.getDeclaredField("gameModel");
        field.setAccessible(true);
        return (Game) field.get(tela);
    }

    private JLabel getLabelStatus(VistaJogo tela) throws Exception {
        Field field = VistaJogo.class.getDeclaredField("labelStatus");
        field.setAccessible(true);
        return (JLabel) field.get(tela);
    }

    @SuppressWarnings("unchecked")
    private JList<String> obterListaRanking(VistaRanking tela) {
        for (java.awt.Component comp : tela.getContentPane().getComponents()) {
            if (comp instanceof JScrollPane) {
                return (JList<String>) ((JScrollPane) comp).getViewport().getView();
            }
        }
        return null;
    }

    private JButton obterBotaoRemoverRanking(VistaRanking tela) {
        for (java.awt.Component comp : tela.getContentPane().getComponents()) {
            if (comp instanceof JButton && ((JButton) comp).getText().contains("Remover")) {
                return (JButton) comp;
            }
        }
        return null;
    }

    // =========================================================================
    // NAVEGAÇÃO DO LABIRINTO | BFS ALGORITHM
    // =========================================================================

    private void esgotarPassosAndandoEmCorredor(VistaJogo tela, Game game, int passosDesejados) {
        Room atual = game.getSalaAtual();
        int keyIda = KeyEvent.VK_UP, keyVolta = KeyEvent.VK_DOWN;

        String[] direcoes = {"north", "south", "east", "west"};
        int[] keys = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT};
        int[] contraKeys = {KeyEvent.VK_DOWN, KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT};

        for (int i = 0; i < 4; i++) {
            Room vizinho = atual.getExit(direcoes[i]);
            if (vizinho != null && vizinho.getTipo() == 0) { // Encontra uma sala normal vazia
                keyIda = keys[i];
                keyVolta = contraKeys[i];
                break;
            }
        }

        while (game.getPassosRestantes() > passosDesejados) {
            pressionarTecla(tela, keyIda);
            if (game.getPassosRestantes() <= passosDesejados) break;
            pressionarTecla(tela, keyVolta);
        }
    }

    private void forcarMapaComCaminhoPara(Game game, int tipoAlvo) throws Exception {
        for (int i = 0; i < 20; i++) {
            if (!encontrarCaminho(game, tipoAlvo).isEmpty()) return;
            // Se o alvo spawnou cercado de paredes ou sem rota, recarrega via reflection
            Method m = Game.class.getDeclaredMethod("carregarNivel");
            m.setAccessible(true);
            m.invoke(game);
        }
        throw new IllegalStateException("Não foi possível gerar um mapa com acesso ao bloco tipo: " + tipoAlvo);
    }

    private List<Integer> encontrarCaminho(Game game, int tipoAlvo) {
        Room inicio = game.getSalaAtual();
        Queue<Room> fila = new LinkedList<>();
        Map<Room, Room> pai = new HashMap<>();
        Map<Room, Integer> direcaoPai = new HashMap<>();
        Set<Room> visitados = new HashSet<>();

        fila.add(inicio);
        visitados.add(inicio);

        while (!fila.isEmpty()) {
            Room atual = fila.poll();
            if (atual.getTipo() == tipoAlvo) {
                List<Integer> caminho = new ArrayList<>();
                Room node = atual;
                while (pai.containsKey(node)) {
                    caminho.add(direcaoPai.get(node));
                    node = pai.get(node);
                }
                Collections.reverse(caminho);
                return caminho;
            }
            adicionarVizinho(atual, "north", KeyEvent.VK_UP, fila, visitados, pai, direcaoPai);
            adicionarVizinho(atual, "south", KeyEvent.VK_DOWN, fila, visitados, pai, direcaoPai);
            adicionarVizinho(atual, "east", KeyEvent.VK_RIGHT, fila, visitados, pai, direcaoPai);
            adicionarVizinho(atual, "west", KeyEvent.VK_LEFT, fila, visitados, pai, direcaoPai);
        }
        return Collections.emptyList();
    }

    private void adicionarVizinho(Room atual, String dir, int keyCode, Queue<Room> fila, Set<Room> visitados, Map<Room, Room> pai, Map<Room, Integer> direcaoPai) {
        Room vizinho = atual.getExit(dir);
        if (vizinho != null && !visitados.contains(vizinho) && vizinho.getTipo() != 1) { // 1 = Parede
            visitados.add(vizinho);
            pai.put(vizinho, atual);
            direcaoPai.put(vizinho, keyCode);
            fila.add(vizinho);
        }
    }
}