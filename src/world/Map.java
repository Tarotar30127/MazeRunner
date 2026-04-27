package world;

public class Map {

    // walls[x][y] — true means solid wall
    private final boolean[][] walls;
    public final int width;
    public final int height;

    public Map(boolean[][] walls) {
        this.walls  = walls;
        this.width  = walls.length;
        this.height = walls[0].length;
    }

    // Returns true for out-of-bounds coordinates (treat edges as solid)
    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return walls[x][y];
    }

    public boolean isWall(double x, double y) {
        return isWall((int) x, (int) y);
    }

    // Centre of the entrance cell (grid cell 0,0 → wall array position 1,1)
    public double[] getStartPosition() {
        return new double[]{1.5, 1.5};
    }

    // Centre of the exit cell (last cell in the grid)
    public double[] getExitPosition() {
        return new double[]{width - 1.5, height - 1.5};
    }
}
