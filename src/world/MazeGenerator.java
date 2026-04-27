package world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Generates a perfect maze (exactly one path between every pair of cells) using
 * the iterative Randomized Depth-First Search / Recursive Backtracker algorithm.
 *
 * Grid layout in the wall array:
 *   - Maze cells live at (2*cx+1, 2*cy+1), where cx,cy ∈ [0, n-1].
 *   - The cell between (cx,cy) and its eastern neighbour (cx+1,cy) is the wall
 *     at (2*cx+2, 2*cy+1).  Similarly for the other three directions.
 *   - All positions not listed above stay as walls, forming the outer border and
 *     the uncarved passages of the maze.
 */
public class MazeGenerator {

    // N, E, S, W offsets in cell-space (not wall-array space)
    private static final int[][] DIRS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private final Random random;

    public MazeGenerator() {
        this.random = new Random();
    }

    public MazeGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * @param n  Number of maze cells per side.  Produces a (2n+1)×(2n+1) wall array.
     */
    public Map generate(int n) {
        int gridW = 2 * n + 1;
        int gridH = 2 * n + 1;

        // Start everything as walls (true = solid)
        boolean[][] walls   = new boolean[gridW][gridH];
        for (int x = 0; x < gridW; x++)
            for (int y = 0; y < gridH; y++)
                walls[x][y] = true;

        boolean[][] visited = new boolean[n][n];

        Deque<int[]> stack = new ArrayDeque<>();
        visited[0][0] = true;
        stack.push(new int[]{0, 0});

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int cx = cur[0];
            int cy = cur[1];

            // Carve the cell open in the wall array
            walls[2 * cx + 1][2 * cy + 1] = false;

            // Collect unvisited neighbours in cell-space
            List<int[]> neighbours = new ArrayList<>(4);
            for (int[] d : DIRS) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (nx >= 0 && nx < n && ny >= 0 && ny < n && !visited[nx][ny]) {
                    neighbours.add(new int[]{nx, ny, d[0], d[1]});
                }
            }

            if (neighbours.isEmpty()) {
                stack.pop(); // dead end — backtrack
            } else {
                Collections.shuffle(neighbours, random);
                int[] chosen = neighbours.get(0);
                int nx = chosen[0];
                int ny = chosen[1];
                int dx = chosen[2];
                int dy = chosen[3];

                // Knock through the wall between current cell and chosen neighbour.
                // The wall sits at (2*cx+1+dx, 2*cy+1+dy) in the wall array.
                walls[2 * cx + 1 + dx][2 * cy + 1 + dy] = false;

                visited[nx][ny] = true;
                stack.push(new int[]{nx, ny});
            }
        }

        // Carve entrance gap at the top of cell (0,0)
        walls[1][0] = false;

        // Carve exit gap at the bottom of cell (n-1, n-1)
        walls[2 * (n - 1) + 1][2 * (n - 1) + 2] = false;

        return new Map(walls);
    }
}
