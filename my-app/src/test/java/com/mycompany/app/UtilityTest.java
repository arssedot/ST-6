package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UtilityTest {

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
