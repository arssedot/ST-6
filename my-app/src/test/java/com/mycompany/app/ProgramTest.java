package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProgramTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    private static char[] boardFrom(String s) {
        char[] b = new char[9];
        for (int i = 0; i < 9; i++) {
            char c = s.charAt(i);
            b[i] = (c == '.') ? ' ' : c;
        }
        return b;
    }

    @Nested
    class GameTests {

        private Game game;

        @BeforeEach
        void setUp() {
            game = new Game();
        }

        @Test
        @DisplayName("Новая игра инициализируется корректно")
        void constructorInitialState() {
            assertAll(
                () -> assertEquals(State.PLAYING, game.state),
                () -> assertNotNull(game.player1),
                () -> assertNotNull(game.player2),
                () -> assertEquals('X', game.player1.symbol),
                () -> assertEquals('O', game.player2.symbol),
                () -> assertEquals(9, game.board.length)
            );
            for (char c : game.board) {
                assertEquals(' ', c);
            }
        }

        @Test
        @DisplayName("Победа X по каждой из 8 линий")
        void checkStateXWinAllLines() {
            String[] wins = {
                "XXX......",
                "...XXX...",
                "......XXX",
                "X..X..X..",
                ".X..X..X.",
                "..X..X..X",
                "X...X...X",
                "..X.X.X.."
            };
            game.symbol = 'X';
            for (String w : wins) {
                assertEquals(State.XWIN, game.checkState(boardFrom(w)), "Доска: " + w);
            }
        }

        @Test
        @DisplayName("Победа O по каждой из 8 линий")
        void checkStateOWinAllLines() {
            String[] wins = {
                "OOO......",
                "...OOO...",
                "......OOO",
                "O..O..O..",
                ".O..O..O.",
                "..O..O..O",
                "O...O...O",
                "..O.O.O.."
            };
            game.symbol = 'O';
            for (String w : wins) {
                assertEquals(State.OWIN, game.checkState(boardFrom(w)), "Доска: " + w);
            }
        }

        @Test
        @DisplayName("Ничья на полной доске без 3-в-ряд")
        void checkStateDraw() {
            game.symbol = 'X';
            char[] b = boardFrom("XOXXOOOXX");
            assertEquals(State.DRAW, game.checkState(b));
        }

        @Test
        @DisplayName("Игра продолжается на пустой доске")
        void checkStatePlayingEmpty() {
            game.symbol = 'X';
            assertEquals(State.PLAYING, game.checkState(game.board));
        }

        @Test
        @DisplayName("Игра продолжается на частично заполненной доске")
        void checkStatePlayingPartial() {
            game.symbol = 'X';
            assertEquals(State.PLAYING, game.checkState(boardFrom("X.O.X....")));
        }

        @Test
        @DisplayName("generateMoves на пустой доске возвращает все 9 позиций")
        void generateMovesEmpty() {
            ArrayList<Integer> moves = new ArrayList<>();
            game.generateMoves(game.board, moves);
            assertEquals(9, moves.size());
            for (int i = 0; i < 9; i++) {
                assertEquals(i, moves.get(i));
            }
        }

        @Test
        @DisplayName("generateMoves на полной доске возвращает пустой список")
        void generateMovesFull() {
            ArrayList<Integer> moves = new ArrayList<>();
            game.generateMoves(boardFrom("XOXOXOXOX"), moves);
            assertTrue(moves.isEmpty());
        }

        @Test
        @DisplayName("generateMoves пропускает занятые клетки")
        void generateMovesPartial() {
            ArrayList<Integer> moves = new ArrayList<>();
            game.generateMoves(boardFrom("X.O.X.O.."), moves);
            assertEquals(5, moves.size());
            assertTrue(moves.contains(1));
            assertTrue(moves.contains(3));
            assertTrue(moves.contains(5));
            assertTrue(moves.contains(7));
            assertTrue(moves.contains(8));
        }

        @Test
        @DisplayName("evaluatePosition: +INF если игрок и победитель совпадают")
        void evaluatePositionWinForPlayer() {
            game.symbol = 'X';
            char[] b = boardFrom("XXX......");
            assertEquals(Game.INF, game.evaluatePosition(b, game.player1));

            game.symbol = 'O';
            char[] b2 = boardFrom("OOO......");
            assertEquals(Game.INF, game.evaluatePosition(b2, game.player2));
        }

        @Test
        @DisplayName("evaluatePosition: -INF если победил противник")
        void evaluatePositionWinForOpponent() {
            game.symbol = 'X';
            char[] b = boardFrom("XXX......");
            assertEquals(-Game.INF, game.evaluatePosition(b, game.player2));

            game.symbol = 'O';
            char[] b2 = boardFrom("OOO......");
            assertEquals(-Game.INF, game.evaluatePosition(b2, game.player1));
        }

        @Test
        @DisplayName("evaluatePosition: 0 при ничьей")
        void evaluatePositionDraw() {
            game.symbol = 'X';
            assertEquals(0, game.evaluatePosition(boardFrom("XOXXOOOXX"), game.player1));
        }

        @Test
        @DisplayName("evaluatePosition: -1 если игра продолжается")
        void evaluatePositionPlaying() {
            game.symbol = 'X';
            assertEquals(-1, game.evaluatePosition(game.board, game.player1));
        }

        @Test
        @DisplayName("MiniMax возвращает допустимый ход на свободную клетку")
        void miniMaxTakesWinningMove() {
            char[] b = boardFrom("XX.......");
            int move = game.MiniMax(b, game.player1);
            assertTrue(move >= 1 && move <= 9);
            assertEquals(' ', b[move - 1]);
        }

        @Test
        @DisplayName("MiniMax блокирует победу противника")
        void miniMaxBlocksOpponent() {
            char[] b = boardFrom("OO..X....");
            int move = game.MiniMax(b, game.player1);
            assertEquals(3, move);
        }

        @Test
        @DisplayName("MiniMax делает ход в пределах поля")
        void miniMaxReturnsValidMove() {
            char[] b = boardFrom("X...O...X");
            int move = game.MiniMax(b, game.player2);
            assertTrue(move >= 1 && move <= 9, "Ход должен быть 1..9, получено " + move);
            assertEquals(' ', b[move - 1]);
        }

        @Test
        @DisplayName("MinMove возвращает -INF если противник уже выиграл")
        void minMoveOnTerminalBoard() {
            game.symbol = 'X';
            char[] b = boardFrom("XXX......");
            int v = game.MinMove(b, game.player2);
            assertEquals(-Game.INF, v);
        }

        @Test
        @DisplayName("MaxMove возвращает +INF если игрок уже выиграл")
        void maxMoveOnTerminalBoard() {
            game.symbol = 'X';
            char[] b = boardFrom("XXX......");
            int v = game.MaxMove(b, game.player1);
            assertEquals(Game.INF, v);
        }

        @Test
        @DisplayName("MinMove исследует доску, когда игра ещё идёт")
        void minMoveRecurses() {
            char[] b = boardFrom("XX.OO....");
            int v = game.MinMove(b, game.player1);
            assertTrue(v >= -Game.INF && v <= Game.INF);
        }

        @Test
        @DisplayName("MaxMove исследует доску, когда игра ещё идёт")
        void maxMoveRecurses() {
            char[] b = boardFrom("X...O....");
            int v = game.MaxMove(b, game.player1);
            assertTrue(v >= -Game.INF && v <= Game.INF);
        }

        @Test
        @DisplayName("Константа INF равна 100")
        void infConstant() {
            assertEquals(100, Game.INF);
        }

        @Test
        @DisplayName("Поля Player по умолчанию")
        void playerDefaults() {
            Player p = new Player();
            p.symbol = 'X';
            p.move = 5;
            p.selected = true;
            p.win = false;
            assertEquals('X', p.symbol);
            assertEquals(5, p.move);
            assertTrue(p.selected);
            assertFalse(p.win);
        }
    }

    @Nested
    class TicTacToeCellTests {

        @Test
        void constructorStoresCoordinates() {
            TicTacToeCell cell = new TicTacToeCell(4, 1, 2);
            assertEquals(4, cell.getNum());
            assertEquals(1, cell.getCol());
            assertEquals(2, cell.getRow());
            assertEquals(' ', cell.getMarker());
        }

        @Test
        void setMarkerUpdatesStateAndDisablesCell() {
            TicTacToeCell cell = new TicTacToeCell(0, 0, 0);
            cell.setMarker("X");
            assertEquals('X', cell.getMarker());
            assertEquals("X", cell.getText());
            assertFalse(cell.isEnabled());
        }

        @Test
        void setMarkerOUpdatesText() {
            TicTacToeCell cell = new TicTacToeCell(8, 2, 2);
            cell.setMarker("O");
            assertEquals('O', cell.getMarker());
            assertEquals("O", cell.getText());
        }
    }

    @Nested
    class TicTacToePanelTests {

        @Test
        void panelConstructorCreatesNineCells() {
            TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
            assertNotNull(panel);
            assertEquals(9, panel.getComponentCount());
        }
    }

    @Nested
    class UtilityTests {

        private final PrintStream originalOut = System.out;
        private ByteArrayOutputStream buffer;

        @BeforeEach
        void redirect() {
            buffer = new ByteArrayOutputStream();
            System.setOut(new PrintStream(buffer));
        }

        @AfterEach
        void restore() {
            System.setOut(originalOut);
        }

        @Test
        void printCharBoard() {
            char[] board = {'X', 'O', ' ', ' ', 'X', ' ', 'O', ' ', ' '};
            Utility.print(board);
            String out = buffer.toString();
            assertTrue(out.contains("X-"));
            assertTrue(out.contains("O-"));
        }

        @Test
        void printIntBoard() {
            int[] board = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            Utility.print(board);
            String out = buffer.toString();
            assertTrue(out.contains("1-"));
            assertTrue(out.contains("9-"));
        }

        @Test
        void printMoveList() {
            ArrayList<Integer> moves = new ArrayList<>();
            moves.add(2);
            moves.add(5);
            moves.add(7);
            Utility.print(moves);
            String out = buffer.toString();
            assertTrue(out.contains("2-"));
            assertTrue(out.contains("5-"));
            assertTrue(out.contains("7-"));
        }

        @Test
        void printEmptyMoveList() {
            Utility.print(new ArrayList<Integer>());
        }
    }
}
