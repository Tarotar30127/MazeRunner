package graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class Screen {

    public final int width;
    public final int height;
    private final BufferedImage image;
    private final int[] pixels;

    public Screen(int width, int height) {
        this.width  = width;
        this.height = height;
        this.image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Grab a direct reference to the underlying raster data — no copy-on-write
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public void setPixel(int x, int y, int rgb) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        pixels[y * width + x] = rgb;
    }

    // Fills the top half with ceilingRgb and the bottom half with floorRgb.
    // Uses System.arraycopy row-by-row to avoid per-pixel method call overhead.
    public void clear(int ceilingRgb, int floorRgb) {
        int[] ceilRow  = new int[width];
        int[] floorRow = new int[width];
        Arrays.fill(ceilRow,  ceilingRgb);
        Arrays.fill(floorRow, floorRgb);

        int half = height / 2;
        for (int y = 0; y < half; y++)
            System.arraycopy(ceilRow,  0, pixels, y * width, width);
        for (int y = half; y < height; y++)
            System.arraycopy(floorRow, 0, pixels, y * width, width);
    }

    public BufferedImage getImage() {
        return image;
    }
}
