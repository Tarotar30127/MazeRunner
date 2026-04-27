package graphics;

import world.Map;

/**
 * Software raycaster using the DDA (Digital Differential Analyzer) algorithm.
 *
 * For each screen column a ray is cast from the player's position. DDA steps
 * the ray across grid cells until it hits a wall, which is much faster than
 * testing every cell along the ray's path.
 *
 * Fisheye correction: the projected wall height is computed from the
 * *perpendicular* distance to the wall plane, not the Euclidean ray length.
 * Using Euclidean distance would make walls in the periphery appear farther
 * away and cause the familiar barrel-distortion ("fisheye") artifact.
 *
 * N/S walls (hit on a Y-axis grid boundary) are drawn darker than E/W walls
 * (hit on an X-axis grid boundary) to create a cheap depth cue without
 * any real shading calculation.
 */
public class Raycaster {

    private final Screen screen;
    private final Map    map;

    // N/S wall faces (ray crossed a Y grid line) — darker shade
    private static final int COLOR_WALL_NS = 0xCC2222;
    // E/W wall faces (ray crossed an X grid line) — brighter shade
    private static final int COLOR_WALL_EW = 0xFF5555;
    private static final int COLOR_CEILING = 0x1A1A2E;
    private static final int COLOR_FLOOR   = 0x444455;

    public Raycaster(Screen screen, Map map) {
        this.screen = screen;
        this.map    = map;
    }

    /**
     * Renders one complete frame.  All six parameters come from the Player.
     *
     * @param posX,posY    player world position
     * @param dirX,dirY    normalised direction vector (where the player faces)
     * @param planeX,planeY  camera plane vector (perpendicular to dir, length = tan(FOV/2))
     */
    public void render(double posX, double posY,
                       double dirX,  double dirY,
                       double planeX, double planeY) {

        screen.clear(COLOR_CEILING, COLOR_FLOOR);

        int screenW = screen.width;
        int screenH = screen.height;

        for (int x = 0; x < screenW; x++) {

            // ── Step 1: Ray direction ─────────────────────────────────────────
            // cameraX maps screen column to the range [-1, +1] across the camera plane.
            // At x=0, cameraX=-1 (left edge); at x=screenW-1, cameraX≈+1 (right edge).
            double cameraX  = 2.0 * x / screenW - 1.0;
            double rayDirX  = dirX + planeX * cameraX;
            double rayDirY  = dirY + planeY * cameraX;

            // ── Step 2: Starting grid cell ────────────────────────────────────
            int mapX = (int) posX;
            int mapY = (int) posY;

            // ── Step 3: Delta distances ───────────────────────────────────────
            // deltaDistX = distance the ray travels along its own direction
            // between two consecutive X-axis grid crossings.
            // Using 1e30 instead of infinity avoids NaN when multiplied by zero.
            double deltaDistX = (rayDirX == 0.0) ? 1e30 : Math.abs(1.0 / rayDirX);
            double deltaDistY = (rayDirY == 0.0) ? 1e30 : Math.abs(1.0 / rayDirY);

            // ── Step 4: Step direction + initial side distances ───────────────
            // sideDistX = ray length from current position to the *first*
            // X-axis grid crossing in the ray's direction.
            int    stepX, stepY;
            double sideDistX, sideDistY;

            if (rayDirX < 0) {
                stepX     = -1;
                sideDistX = (posX - mapX) * deltaDistX;      // left boundary
            } else {
                stepX     =  1;
                sideDistX = (mapX + 1.0 - posX) * deltaDistX; // right boundary
            }
            if (rayDirY < 0) {
                stepY     = -1;
                sideDistY = (posY - mapY) * deltaDistY;
            } else {
                stepY     =  1;
                sideDistY = (mapY + 1.0 - posY) * deltaDistY;
            }

            // ── Step 5: DDA march ─────────────────────────────────────────────
            // Advance whichever axis has the closer next crossing.
            // side=0: ray hit an X-axis crossing (E/W wall face)
            // side=1: ray hit a Y-axis crossing (N/S wall face)
            int side = 0;
            boolean hit = false;
            while (!hit) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX      += stepX;
                    side       = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY      += stepY;
                    side       = 1;
                }
                if (map.isWall(mapX, mapY)) hit = true;
            }

            // ── Step 6: Perpendicular wall distance (fisheye correction) ──────
            // Subtract one deltaDist step to get the distance to the wall plane
            // instead of the distance to the grid crossing just past it.
            double perpWallDist = (side == 0)
                    ? sideDistX - deltaDistX
                    : sideDistY - deltaDistY;

            // Guard against division-by-zero when the player stands right on a boundary
            perpWallDist = Math.max(perpWallDist, 0.001);

            // ── Step 7: Projected column height ──────────────────────────────
            int lineHeight = (int) (screenH / perpWallDist);
            int drawStart  = Math.max(0,          -lineHeight / 2 + screenH / 2);
            int drawEnd    = Math.min(screenH - 1,  lineHeight / 2 + screenH / 2);

            // ── Step 8: Colour + draw ─────────────────────────────────────────
            int color = (side == 1) ? COLOR_WALL_NS : COLOR_WALL_EW;
            for (int y = drawStart; y <= drawEnd; y++) {
                screen.setPixel(x, y, color);
            }
        }
    }
}
