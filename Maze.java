import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Maze {
    private final int cellSize = 40;
    
    // Grid dimensions (Must be odd numbers for the wall/path generation logic to align perfectly)
    private final int rows = 11; 
    private final int cols = 11; 
    
    private final int[][] grid;

    // Target destination coordinates
    private final int goalRow = rows - 2;
    private final int goalCol = cols - 2;

    public Maze() {
        grid = new int[rows][cols];
        generateRandomMaze();
    }

    private void generateRandomMaze() {
        // Step 1: Fill the entire grid with solid walls (1)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = 1;
            }
        }

        // Step 2: Run Depth-First Search cutting paths starting from (1, 1)
        carvePath(1, 1);

        // Step 3: Embed the goal indicator tag explicitly into the map structure
        grid[goalRow][goalCol] = 3;
    }

    /**
     * Recursive Backtracker (DFS) to carve paths through the walls.
     */
    private void carvePath(int r, int c) {
        grid[r][c] = 0; // Mark current cell as a walkable path

        // Define neighbors 2 steps away (Up, Down, Left, Right)
        int[][] directions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        List<int[]> dirList = new ArrayList<>();
        for (int[] d : directions) {
            dirList.add(d);
        }
        
        // Randomize directions to ensure a unique maze shape every run
        Collections.shuffle(dirList, new Random());

        for (int[] dir : dirList) {
            int nextR = r + dir[0];
            int nextC = c + dir[1];

            // Verify boundaries and check if the neighbor cell is still an unvisited wall
            if (nextR > 0 && nextR < rows - 1 && nextC > 0 && nextC < cols - 1) {
                if (grid[nextR][nextC] == 1) {
                    // Carve through the wall between the current cell and the neighbor cell
                    grid[r + dir[0] / 2][c + dir[1]  / 2] = 0;
                    
                    // Recursively move to the next cell
                    carvePath(nextR, nextC);
                }
            }
        }
    }

    // --- Core Layout Getters ---
    public int getCellSize() { return cellSize; }
    public int getGoalRow() { return goalRow; }
    public int getGoalCol() { return goalCol; }
    public int[][] getGrid() { return grid; }

    public boolean isWall(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col] == 1;
        }
        return true; 
    }

    public boolean isGoal(int row, int col) {
        return row == goalRow && col == goalCol;
    }

    /**
     * Renders the generated environment background.
     */
    public void draw(Graphics g) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * cellSize;
                int y = r * cellSize;

                if (grid[r][c] == 1) {
                    g.setColor(Color.DARK_GRAY); // Walls
                    g.fillRect(x, y, cellSize, cellSize);
                } else if (grid[r][c] == 3) {
                    g.setColor(Color.GREEN); // Goal
                    g.fillRect(x + 5, y + 5, cellSize - 10, cellSize - 10);
                }

                // Draw background layout tracking grids
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }
}