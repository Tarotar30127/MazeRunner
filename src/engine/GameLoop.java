package engine;

import entity.Player;
import graphics.Raycaster;
import graphics.Screen;
import world.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

public class GameLoop implements Runnable {

    // Fixed physics timestep: 1/60 second ≈ 16.67 ms
    private static final double FIXED_STEP = 1.0 / 60.0;

    private final Player       player;
    private final Raycaster    raycaster;
    private final Screen       screen;
    private final InputHandler input;
    private final Canvas       canvas;
    private final Map          map;

    private volatile boolean running;
    private Thread thread;

    public GameLoop(Player player, Raycaster raycaster, Screen screen,
                    InputHandler input, Canvas canvas, Map map) {
        this.player    = player;
        this.raycaster = raycaster;
        this.screen    = screen;
        this.input     = input;
        this.canvas    = canvas;
        this.map       = map;
    }

    public void start() {
        running = true;
        thread  = new Thread(this, "game-loop");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        double accumulator = 0.0;
        long   prev        = System.nanoTime();

        while (running) {
            long   now     = System.nanoTime();
            double elapsed = (now - prev) / 1_000_000_000.0;
            prev = now;

            // Cap elapsed to prevent a "spiral of death" after pauses (e.g. window drag)
            if (elapsed > 0.25) elapsed = 0.25;

            accumulator += elapsed;

            // Consume accumulated time in fixed physics steps
            while (accumulator >= FIXED_STEP) {
                player.move(
                    input.isMoveForward(),
                    input.isMoveBack(),
                    input.isRotLeft(),
                    input.isRotRight(),
                    FIXED_STEP
                );
                accumulator -= FIXED_STEP;
            }

            // Render at actual frame rate (uncapped between physics steps)
            raycaster.render(
                player.getPosX(), player.getPosY(),
                player.getDirX(), player.getDirY(),
                player.getPlaneX(), player.getPlaneY()
            );

            // Push the pixel buffer to the AWT double-buffered canvas
            BufferStrategy bs = canvas.getBufferStrategy();
            if (bs != null) {
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screen.getImage(), 0, 0, null);
                g.dispose();
                bs.show();
            }

            // Win condition: player reaches the exit cell
            double[] exit = map.getExitPosition();
            double dx = player.getPosX() - exit[0];
            double dy = player.getPosY() - exit[1];
            if (dx * dx + dy * dy < 0.75 * 0.75) {
                stop();
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "You escaped the maze!",
                        "You Win!", JOptionPane.INFORMATION_MESSAGE));
            }

            if (input.isEscape()) stop();

            // Yield to avoid pegging a CPU core at 100%
            Thread.yield();
        }
    }
}
