package entity;

import world.Map;

/**
 * Holds the player's position and camera orientation in world space.
 *
 * The camera model uses two 2D vectors:
 *   dir   — unit vector pointing in the direction the player faces
 *   plane — camera plane vector, perpendicular to dir
 *             |plane| / |dir| = tan(FOV/2); with |dir|=1 and |plane|=0.66, FOV≈66°
 *
 * Rotation is applied by multiplying both vectors by the same 2D rotation matrix:
 *   [ cos θ  -sin θ ] [ x ]
 *   [ sin θ   cos θ ] [ y ]
 * This keeps |dir| and |plane| constant and the angle between them exactly 90°.
 */
public class Player {

    private double posX, posY;
    private double dirX, dirY;
    private double planeX, planeY;

    private static final double MOVE_SPEED  = 3.0;   // world units per second
    private static final double ROT_SPEED   = 2.5;   // radians per second
    private static final double WALL_BUFFER = 0.25;  // collision radius (must be < 0.5)

    private final Map map;

    public Player(double startX, double startY, Map map) {
        this.posX   = startX;
        this.posY   = startY;
        this.map    = map;
        // Facing east (+X), camera plane along +Y for a right-handed 2D coordinate system
        this.dirX   =  1.0;
        this.dirY   =  0.0;
        this.planeX =  0.0;
        this.planeY =  0.66;
    }

    /**
     * Update position and orientation for one fixed timestep.
     * X and Y axes are checked independently for wall collisions, producing
     * "wall sliding" so the player glides along surfaces rather than stopping dead.
     */
    public void move(boolean fwd, boolean back, boolean rotLeft, boolean rotRight, double dt) {
        if (rotLeft)  rotate( ROT_SPEED * dt);
        if (rotRight) rotate(-ROT_SPEED * dt);

        double speed = MOVE_SPEED * dt;

        if (fwd) {
            double newX = posX + dirX * speed;
            double newY = posY + dirY * speed;
            // Check each axis independently: positive buffer in the direction of motion
            if (!map.isWall(newX + Math.signum(dirX) * WALL_BUFFER, posY)) posX = newX;
            if (!map.isWall(posX, newY + Math.signum(dirY) * WALL_BUFFER)) posY = newY;
        }
        if (back) {
            double newX = posX - dirX * speed;
            double newY = posY - dirY * speed;
            if (!map.isWall(newX - Math.signum(dirX) * WALL_BUFFER, posY)) posX = newX;
            if (!map.isWall(posX, newY - Math.signum(dirY) * WALL_BUFFER)) posY = newY;
        }
    }

    // Applies the 2D rotation matrix to both dir and plane vectors simultaneously.
    private void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double newDirX   = dirX   * cos - dirY   * sin;
        double newDirY   = dirX   * sin + dirY   * cos;
        double newPlaneX = planeX * cos - planeY * sin;
        double newPlaneY = planeX * sin + planeY * cos;

        dirX   = newDirX;
        dirY   = newDirY;
        planeX = newPlaneX;
        planeY = newPlaneY;
    }

    public double getPosX()   { return posX; }
    public double getPosY()   { return posY; }
    public double getDirX()   { return dirX; }
    public double getDirY()   { return dirY; }
    public double getPlaneX() { return planeX; }
    public double getPlaneY() { return planeY; }
}
