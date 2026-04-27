package engine;

import entity.Player;
import graphics.Raycaster;
import graphics.Screen;
import world.Map;
import world.MazeGenerator;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Canvas;
import java.awt.Dimension;

/**
 * Entry point and wiring hub.
 *
 * Object construction order matters here:
 *   1. Generate maze (Map)
 *   2. Allocate pixel buffer (Screen)
 *   3. Create player at maze entrance
 *   4. Wire raycaster and input handler
 *   5. Build AWT Canvas and JFrame — canvas.setIgnoreRepaint(true) prevents
 *      the AWT event thread from issuing spurious repaints that cause flicker.
 *   6. Show the frame, then call createBufferStrategy(2) — MUST be done after
 *      setVisible because the native peer has to exist first.
 *   7. Request focus so the KeyListener receives events.
 *   8. Start the game loop thread.
 */
public class Game {

    private static final int SCREEN_W = 800;
    private static final int SCREEN_H = 600;
    private static final int MAZE_N   = 10;  // produces a 21×21 wall array

    public Game() {
        // 1 — Maze
        MazeGenerator gen = new MazeGenerator();
        Map map = gen.generate(MAZE_N);

        // 2 — Pixel buffer
        Screen screen = new Screen(SCREEN_W, SCREEN_H);

        // 3 — Player
        double[] start = map.getStartPosition();
        Player player = new Player(start[0], start[1], map);

        // 4 — Rendering + input
        Raycaster    raycaster = new Raycaster(screen, map);
        InputHandler input     = new InputHandler();

        // 5 — AWT Canvas
        Canvas canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        canvas.setIgnoreRepaint(true);  // game loop drives all repaints
        canvas.addKeyListener(input);

        // 5b — JFrame
        JFrame frame = new JFrame("MazeRunner  |  WASD / Arrow keys to move");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 6 — BufferStrategy (must follow setVisible)
        canvas.createBufferStrategy(2);

        // 7 — Input focus (must follow setVisible)
        canvas.requestFocusInWindow();

        // 8 — Game loop
        GameLoop loop = new GameLoop(player, raycaster, screen, input, canvas, map);
        loop.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}
