import java.awt.Color;
import java.awt.Graphics2D;

public class Ground {
    // --- Advanced Minecrafty Earth Tone Palette ---
    private static final Color soilBase = new Color(115, 70, 40);       // Rich brown clay base
    private static final Color pathStone = new Color(140, 130, 125);     // Voxel cobblestone brick paths
    private static final Color grassAccent = new Color(65, 130, 55);     // Pop-art moss/grass tuft block accents
    private static final Color outlineColor = new Color(25, 18, 32);     // Stylized dark block border gap

    /**
     * ADVANCED ENGINE FLOOR-MAPPING MODULE:
     * Projects mathematically flawless, perspective-correct 16x16 voxel ground textures.
     * Uses true ray vectors to cleanly lock checking patterns directly to the maze layout coordinates.
     * * @param camera        Reference to the modular decoupled vantage camera pipeline
     * @param rayDirX       Re-projected current ray direction matrix X component
     * @param rayDirY       Re-projected current ray direction matrix Y component
     * @param x             Current horizontal screen column being drawn
     * @param stripWidth    The width of the raycasted column block (4 pixels)
     * @param startY        Where the ground slice begins (exactly at the bottom boundary of the wall)
     * @param screenHeight  Total viewport height configuration
     */
    public static void drawCartoonStripe(Graphics2D g2d, Camera camera, double rayDirX, double rayDirY,
                                         int x, int stripWidth, int startY, int screenHeight) {
        if (startY >= screenHeight) return;

        // Fetch primary screen dimensions safely to resolve midpoint perspective lookups
        int screenWidth = g2d.getDeviceConfiguration().getBounds().width;
        int horizonCenter = (int) (screenHeight * 0.64); // Matches Map.java's elevated camera split

        int stepY = 2; 
        int currentY = startY;

        while (currentY < screenHeight) {
            // Calculate a clean vertical distance factor relative to our elevated vantage camera setup
            double currentDist = (double) screenHeight / (2.0 * currentY - 2.0 * horizonCenter);
            if (currentDist <= 0) currentDist = 0.01;

            // --- 1. RAY-TO-FLOOR CARTESIAN INTERCEPT PROJECTION ---
            // Calculate exact, un-warped mathematical layout space coordinates (U, V) across the ground plane
            double weight = currentDist;
            double floorX = weight * rayDirX + camera.getPosX();
            double floorY = weight * rayDirY + camera.getPosY();

            // Extract the clean fractional block offset to convert absolute coordinates into tile textures
            double tileFracX = floorX - Math.floor(floorX);
            double tileFracY = floorY - Math.floor(floorY);

            // Map fractional tile spaces cleanly into 16x16 voxel pixel coordinate indices
            int texX = (int) (tileFracX * 16.0);
            int texY = (int) (tileFracY * 16.0);
            texX = Math.max(0, Math.min(15, texX));
            texY = Math.max(0, Math.min(15, texY));

            // Generate a fast deterministic pixel seed to control random pixel texture grain
            int pixelSeed = ((int)Math.floor(floorX) * 73 + (int)Math.floor(floorY) * 97 + texX * 13 + texY * 19) & 0xFFFF;

            // Identify grid structural layout assignments to match block paths with walls precisely
            int mapX = (int) Math.floor(floorX);
            int mapY = (int) Math.floor(floorY);

            // --- 2. ADVANCED MINECRAFTY BLUEPRINT SHADER PIPELINE ---
            Color pixelColor = soilBase;

            // Checkerboard walkway path condition (simulates stone blocks running down corridors)
            boolean isWalkwayPattern = (mapX % 2 == 0 ^ mapY % 2 == 0);
            
            if (isWalkwayPattern) {
                pixelColor = pathStone; // Walkway tile pathway color
            } else if ((pixelSeed % 11 == 0) && (texX > 2 && texX < 6 && texY > 4 && texY < 8)) {
                pixelColor = grassAccent; // Occasional clean mossy voxel patches
            }

            // --- 3. RETRO CELL SHADING GRIT GENERATOR ---
            int noiseHash = (texX * 31 + texY * 17 + pixelSeed);
            int noiseGrit = (noiseHash ^ (noiseHash >> 5)) & 7; // Extract a clean pixel gradient scale

            int r = Math.min(255, Math.max(0, pixelColor.getRed() - 4 + noiseGrit));
            int g = Math.min(255, Math.max(0, pixelColor.getGreen() - 4 + noiseGrit));
            int b = Math.min(255, Math.max(0, pixelColor.getBlue() - 3 + noiseGrit));
            pixelColor = new Color(r, g, b);

            // Apply global environmental comic-book atmospheric step fog thresholds over distance
            double cellShadeFactor;
            if (currentDist > 8.0)       cellShadeFactor = 0.15; // Distant shadow boundaries
            else if (currentDist > 5.0)  cellShadeFactor = 0.45; // Mid-ground atmospheric decay
            else if (currentDist > 2.5)  cellShadeFactor = 0.75; // Natural lighting fields
            else                         cellShadeFactor = 1.10; // Vibrant pop-art close foreground

            int sr = Math.min(255, (int)(pixelColor.getRed() * cellShadeFactor));
            int sg = Math.min(255, (int)(pixelColor.getGreen() * cellShadeFactor));
            int sb = Math.min(255, (int)(pixelColor.getBlue() * cellShadeFactor));
            pixelColor = new Color(sr, sg, sb);

            // --- 4. STRUCTURAL GRID JOINT OUTLINES ---
            // Injects clean block panel border cracks where ground grid files link together
            if (texX == 0 || texY == 0 || texX == 15 || texY == 15) {
                if (pixelSeed % 2 == 0) {
                    pixelColor = outlineColor;
                }
            }

            // Render out the highly detailed textured strip segment block
            g2d.setColor(pixelColor);
            g2d.fillRect(x, currentY, stripWidth, stepY);

            currentY += stepY;
        }
    }
}