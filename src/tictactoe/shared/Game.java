/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class Game 
{
    static public final int GRID_SIZE = 3;
    static public final int None = 0;
    static public final int X = 1;
    static public final int O = 2;
    static public final int Draw = 3;
    
    public int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    public int playerX;
    public int playerO;
    public int id;
    
    // Predefined combos can be drawn, transferred and checked by index.
    // combo[i] = {{row, column}, ..., {row, column}}
    static public final int[][][] combo = {
        {{0, 0}, {0, 1}, {0, 2}},
        {{1, 0}, {1, 1}, {1, 2}},
        {{2, 0}, {2, 1}, {2, 2}},
        {{0, 0}, {1, 0}, {2, 0}},
        {{0, 1}, {1, 1}, {2, 1}},
        {{0, 2}, {1, 2}, {2, 2}}, 
        {{0, 0}, {1, 1}, {2, 2}},
        {{0, 2}, {1, 1}, {2, 0}}
    };
    
    static public class Result implements java.io.Serializable
    {
        private static final long serialVersionUID = 4525639447805764212L;
        public int winner = Game.None;
        public int combo = -1;
    }
    
    public Result getResult()
    {
        Result result = new Result();
        boolean hasEmptyCells = false;
        for (int i = 0; i < combo.length; ++ i) {
            int xCount = 0;
            int oCount = 0;
            for (int c = 0; c < GRID_SIZE; ++ c) {
                final int[] cell = combo[i][c];
                final int row = cell[0];
                final int column = cell[1];
                final int value = grid[row][column];
                if (value == X) {
                    xCount += 1;
                } else if (value == O) {
                    oCount += 1;
                } else {
                    hasEmptyCells = true;
                    break;
                }
            }
            if (xCount == GRID_SIZE) {
                result.winner = X;
                result.combo = i;
                return result;
            } 
            if (oCount == GRID_SIZE) {
                result.winner = O;
                result.combo = i;
                return result;
            }
        }
        result.winner = hasEmptyCells ? None : Draw;
        return result;
    }
}
