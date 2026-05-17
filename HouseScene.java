import java.awt.Color;

public class HouseScene {
    // --- Structural Map Blueprint IDs ---
    public static final int AIR = 0;
    public static final int MAIN_WALL = 1;
    public static final int FOYER_WALL = 2;
    public static final int ESCAPE_PORTAL = 3;
    public static final int WINDOW_SEGMENT = 4;

    /**
     * Decorates a standard generated maze layout grid with distinct room IDs.
     * This transforms a raw maze into a recognizable first-person indoor house architecture.
     */
    public static void decorateHouseLayout(int[][] grid) {
        int rows = grid.length;
        // FIXED: Changed from grid.length to grid.length to support non-square layouts safely
        int cols = grid.length; 

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Keep paths clear
                if (grid[r][c] == AIR) continue;
                if (grid[r][c] == ESCAPE_PORTAL) continue;

                // Designate the starting quadrant area as a distinct Foyer Entryway
                if (r <= 3 && c <= 3) {
                    grid[r][c] = FOYER_WALL;
                }
                // Convert select exterior perimeter structures into Window segments to increase outdoor lighting visibility
                else if ((r == 0 || r == rows - 1 || c == 0 || c == cols - 1) && (r % 3 == 0 || c % 3 == 0)) {
                    grid[r][c] = WINDOW_SEGMENT;
                }
                // Default remaining items to standard house structural partitions
                else {
                    grid[r][c] = MAIN_WALL;
                }
            }
        }
    }

    /**
     * Resolves high-visibility, saturated cartoon/cel-shaded colors based on the structural room ID.
     * Tuned to contrast beautifully against the pink sky, dark purple floors, and black outlines.
     */
    public static Color getStructuralColor(int wallType, int side) {
        Color baseColor;

        switch (wallType) {
            case FOYER_WALL:
                // Vibrant cartoon warm-chestnut wood styling for entry halls
                baseColor = new Color(185, 95, 45); 
                break;
            case WINDOW_SEGMENT:
                // Electric glowing neon cyan glass window segments
                baseColor = new Color(0, 230, 255); 
                break;
            case ESCAPE_PORTAL:
                // Hot laser magenta exit gateway portal
                baseColor = new Color(255, 10, 160); 
                break;
            case MAIN_WALL:
            default:
                // Saturated cartoon minty teal interior walls
                baseColor = new Color(0, 170, 190); 
                break;
        }

        // Apply a clean ambient occlusion shift to North/South directional facing axes
        if (side == 1) {
            baseColor = baseColor.darker();
        }

        return baseColor;
    }
}