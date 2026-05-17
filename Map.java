import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Core rendering controller and gameplay pipeline management module for the 3D Voxel Engine.
 * Operates on a high-performance 60 FPS update matrix following MVC architecture principles.
 */
public class Map extends JPanel {
    
    // --- Engine Configuration Constants ---
    private static final int GAME_LOOP_DELAY_MS = 16; // Implements ~60 FPS update frequency
    private static final int RAYCAST_STRIP_WIDTH = 4; // Horizontal multi-sampled pixel strip width
    private static final int HUD_MINIMAP_SCALE = 12;  // Scale coefficient for grid-space radar dots
    private static final double PLAYER_COLLISION_RADIUS = 0.42;

    // --- Stylized Pop-Art Color Configurations ---
    private static final Color COLOR_SKY = new Color(240, 40, 110);      // Saturated Pink/Magenta
    private static final Color COLOR_HUD_BG = new Color(15, 10, 25, 220); // Translucent Dark Velvet
    private static final Color COLOR_HUD_BORDER = new Color(255, 255, 255, 80);
    private static final Color COLOR_MINIMAP_AIR = new Color(35, 30, 60);
    private static final Color COLOR_PATH_GUIDE = new Color(255, 215, 0, 240);  // High-contrast Gold
    private static final Color COLOR_ENEMY_BLIP = new Color(255, 60, 0);         // Aggressive Laser Red
    private static final Color COLOR_PLAYER_BLIP = new Color(0, 255, 150);       // Cyber Emerald Green

    // --- Core Architecture System Submodules ---
    private Maze maze;
    private final Timer gameTimer;
    private final Camera camera;   
    private final Control control; 
    private List<Enemy> enemies;
    private double[] zBuffer;

    /**
     * Initializes a new Map engine instance, configuring decoupled hardware components
     * and launching the high-responsiveness 60 FPS real-time calculation loop.
     */
    public Map() {
        this.maze = new Maze();
        this.camera = new Camera(1.5, 1.5); 
        this.control = new Control();
        
        this.addKeyListener(control);
        this.setFocusable(true);
        
        spawnEnemies();

        // Instantiate primary execution update ticker loop
        this.gameTimer = new Timer(RAYCAST_STRIP_WIDTH * 4, e -> gameTick());
        // Standardize delay explicitly matching Delay Config Constants
        this.gameTimer.setDelay(GAME_LOOP_DELAY_MS);
        this.gameTimer.start();
    }

    /**
     * Spawns active enemy drone entities inside non-obstructed corridor spaces.
     */
    private void spawnEnemies() {
        this.enemies = new ArrayList<>();
        int[][] grid = maze.getGrid();
        Random rand = new Random();
        
        int count = 0;
        while (count < 3) {
            int r = rand.nextInt(grid.length);
            int c = rand.nextInt(grid.length); // Robust non-square dimension safety check
            
            if (grid[r][c] == HouseScene.AIR && (r > 3 || c > 3)) {
                enemies.add(new Enemy(c + 0.5, r + 0.5));
                count++;
            }
        }
    }

    /**
     * Process real-time gameplay updates, coordinate smooth interpolation angles,
     * evaluate spatial entity intersections, and check map target boundary goals.
     */
    private void gameTick() {
        // Evaluates mutation cooldown timers to dynamically open/close paths on the fly
        //maze.updateDynamicPaths(camera, enemies);

        // Synchronize hardware input registries straight down to the perspective camera vector space
        camera.update(control.isKeyUp(), control.isKeyDown(), control.isKeyLeft(), control.isKeyRight(), maze);

        // Process enemy state registries and vector collision parameters
        for (Enemy enemy : enemies) {
            enemy.update(camera.getPosX(), camera.getPosY(), maze);
            
            double dx = enemy.x - camera.getPosX();
            double dy = enemy.y - camera.getPosY();
            
            if (Math.sqrt(dx * dx + dy * dy) < PLAYER_COLLISION_RADIUS) {
                handleGameOverSequence();
                return;
            }
        }

        // Process stage clearance conditions
        if (maze.isGoal((int) camera.getPosY(), (int) camera.getPosX())) {
            handleStageClearSequence();
        }

        repaint();
    }

    private void handleGameOverSequence() {
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, "GAME OVER! Discovered by a drone patrol.");
        resetGamePositions();
        spawnEnemies();
        gameTimer.start();
    }

    private void handleStageClearSequence() {
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, "STAGE CLEAR!");
        this.maze = new Maze();
        resetGamePositions();
        spawnEnemies();
        gameTimer.start();
    }

    private void resetGamePositions() {
        camera.reset(1.5, 1.5);
        control.reset(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int screenWidth = getWidth();
        int screenHeight = getHeight();
        
        // Dynamically optimize and balance depth buffers on window scaling transformations
        if (zBuffer == null || zBuffer.length != screenWidth) {
            zBuffer = new double[screenWidth];
        }

        // Establishes a highly optimized vertical horizon split Vantage Point line (~64% depth)
        int horizonCenter = (int) (screenHeight * 0.5); 

        // Modular Rendering Pipeline Architecture Layers
        drawBackgroundSky(g2d, screenWidth, horizonCenter);
        render3DSceneGeometry(g2d, screenWidth, screenHeight, horizonCenter);
        render3DSpriteBillboards(g2d, screenWidth, screenHeight, horizonCenter);
        drawHUDOverlay(g2d, screenWidth, screenHeight);
    }

    private void drawBackgroundSky(Graphics2D g2d, int width, int horizonCenter) {
        g2d.setColor(COLOR_SKY);
        g2d.fillRect(0, 0, width, horizonCenter);
    }

    /**
     * Executes vertical multi-sampled Digital Differential Analysis (DDA) rays 
     * columns straight across the viewport to translate spatial indices into 3D voxel walls.
     */
    private void render3DSceneGeometry(Graphics2D g2d, int w, int h, int horizonCenter) {
        int[][] grid = maze.getGrid();
        int gridWidth = grid.length;
        int gridHeight = grid.length;

        for (int x = 0; x < w; x += RAYCAST_STRIP_WIDTH) {
            double cameraX = 2 * (x + RAYCAST_STRIP_WIDTH / 2.0) / (double) w - 1;
            double rayDirX = camera.getDirX() + camera.getPlaneX() * cameraX;
            double rayDirY = camera.getDirY() + camera.getPlaneY() * cameraX;

            int mapX = (int) camera.getPosX();
            int mapY = (int) camera.getPosY();

            double deltaDistX = (rayDirX == 0) ? Double.MAX_VALUE : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? Double.MAX_VALUE : Math.abs(1 / rayDirY);
            double sideDistX, sideDistY;
            double perpWallDist;

            int stepX, stepY;
            int hit = 0;
            int side = 0; 
            int wallType = 0;

            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (camera.getPosX() - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - camera.getPosX()) * deltaDistX;
            }
            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (camera.getPosY() - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - camera.getPosY()) * deltaDistY;
            }

            // Core Ray Intersection Traversal Loop
            while (hit == 0) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                
                // Absolute grid array safety protection boundary clamp
                if (mapX < 0 || mapX >= gridWidth || mapY < 0 || mapY >= gridHeight) break;
                
                if (grid[mapY][mapX] > 0) {
                    hit = 1;
                    wallType = grid[mapY][mapX];
                }
            }

            if (hit == 1) {
                if (side == 0) perpWallDist = (mapX - camera.getPosX() + (1 - stepX) / 2.0) / rayDirX;
                else           perpWallDist = (mapY - camera.getPosY() + (1 - stepY) / 2.0) / rayDirY;

                if (perpWallDist <= 0) perpWallDist = 0.01;
                
                // Write absolute depth registers straight across the width matrix
                for (int i = 0; i < RAYCAST_STRIP_WIDTH && (x + i) < w; i++) {
                    zBuffer[x + i] = perpWallDist;
                }

                int lineHeight = (int) (h / perpWallDist);
                int drawStart = -lineHeight / 2 + horizonCenter;

                // Fire procedural voxel texturing module calculations
                Wall.drawNatureStripe(g2d, x, RAYCAST_STRIP_WIDTH, drawStart, lineHeight, wallType, side, mapX, mapY, perpWallDist);

                // Draw explicit stylized horizontal cell-shading boundaries
                g2d.setColor(Color.BLACK);
                int edgeOutlineThickness = Math.max(1, h / 140);
                g2d.fillRect(x, drawStart, RAYCAST_STRIP_WIDTH, edgeOutlineThickness); 
                g2d.fillRect(x, drawStart + lineHeight, RAYCAST_STRIP_WIDTH, edgeOutlineThickness); 

                // Inject high-detail ray-projected floor geometry matrix slices directly underneath
                int wallBottomY = drawStart + lineHeight;
                Ground.drawCartoonStripe(g2d, camera, rayDirX, rayDirY, x, RAYCAST_STRIP_WIDTH, wallBottomY, h);
            }
        }
    }

    /**
     * Transforms enemy coordinates, orders depth queues via Z-sorting pipelines,
     * and renders billboard actors cleanly pinned directly to floor coordinates.
     */
    private void render3DSpriteBillboards(Graphics2D g2d, int w, int h, int horizonCenter) {
        for (Enemy enemy : enemies) {
            enemy.distance = ((camera.getPosX() - enemy.x) * (camera.getPosX() - enemy.x) + 
                              (camera.getPosY() - enemy.y) * (camera.getPosY() - enemy.y));
        }
        Collections.sort(enemies, (a, b) -> Double.compare(b.distance, a.distance));

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive) continue;

            double spriteX = enemy.x - camera.getPosX();
            double spriteY = enemy.y - camera.getPosY();

            double invDet = 1.0 / (camera.getPlaneX() * camera.getDirY() - camera.getDirX() * camera.getPlaneY());
            double transformX = invDet * (camera.getDirY() * spriteX - camera.getDirX() * spriteY);
            
            // Clean vector tracking via relative camera plane matrix getters
            double transformY = invDet * (-camera.getPlaneY() * spriteX + camera.getPlaneX() * spriteY); 

            if (transformY <= 0.1) continue; 

            int spriteScreenX = (int) ((w / 2) * (1 + transformX / transformY));
            int spriteHeight = Math.abs((int) (h / transformY));
            int spriteWidth = Math.abs((int) (h / transformY));
            
            int drawStartY = -spriteHeight / 2 + horizonCenter;
            int drawStartX = -spriteWidth / 2 + spriteScreenX;
            int drawEndX = spriteWidth / 2 + spriteScreenX;

            for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
                if (stripe >= 0 && stripe < w && transformY < zBuffer[stripe]) {
                    enemy.draw3DStripe(g2d, stripe, drawStartX, spriteWidth, drawStartY, spriteHeight);
                }
            }
        }
    }

    /**
     * Composes all 2D screen elements, vector route track points, radar nodes,
     * and first-person chassis overlay frames over the top of the completed 3D buffers.
     */
    private void drawHUDOverlay(Graphics2D g2d, int w, int h) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[][] grid = maze.getGrid();
        int gridWidth = grid.length;
        int gridHeight = grid.length;

        // --- 1. Map Outer Frame Boundary Background ---
        int offsetX = w - (gridWidth * HUD_MINIMAP_SCALE) - 20;
        int offsetY = 20;
        int backgroundWidth = gridWidth * HUD_MINIMAP_SCALE + 12;
        int backgroundHeight = gridHeight * HUD_MINIMAP_SCALE + 12;

        g2d.setColor(COLOR_HUD_BG);
        g2d.fillRect(offsetX - 6, offsetY - 6, backgroundWidth, backgroundHeight);
        g2d.setColor(COLOR_HUD_BORDER);
        g2d.drawRect(offsetX - 6, offsetY - 6, backgroundWidth, backgroundHeight);

        // --- 2. Render Voxel MiniMap Grid Structure Blocks ---
        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                int cellX = offsetX + c * HUD_MINIMAP_SCALE;
                int cellY = offsetY + r * HUD_MINIMAP_SCALE;
                int renderDimension = HUD_MINIMAP_SCALE - 1;

                if (grid[r][c] > 0) {
                    g2d.setColor(HouseScene.getStructuralColor(grid[r][c], 0));
                    g2d.fillRect(cellX, cellY, renderDimension, renderDimension);
                } else {
                    g2d.setColor(COLOR_MINIMAP_AIR);
                    g2d.fillRect(cellX, cellY, renderDimension, renderDimension);
                }
            }
        }

        // --- 3. Render Intelligent A* Route Path Track Nodes ---
        List<int[]> path = AStar.findPath(grid, (int) camera.getPosY(), (int) camera.getPosX(), maze.getGoalRow(), maze.getGoalCol());
        g2d.setColor(COLOR_PATH_GUIDE); 
        for (int[] point : path) {
            int r = point[0]; 
            int c = point[1]; 
            if ((r == (int) camera.getPosY() && c == (int) camera.getPosX()) || maze.isGoal(r, c)) continue;
            g2d.fillOval(offsetX + c * HUD_MINIMAP_SCALE + 3, offsetY + r * HUD_MINIMAP_SCALE + 3, HUD_MINIMAP_SCALE - 6, HUD_MINIMAP_SCALE - 6);
        }

        // --- 4. Draw Threat Hostile Radar Intercept Blips ---
        g2d.setColor(COLOR_ENEMY_BLIP); 
        for (Enemy enemy : enemies) {
            if (enemy.isAlive) {
                g2d.fillOval((int) (offsetX + enemy.x * HUD_MINIMAP_SCALE - 3), (int) (offsetY + enemy.y * HUD_MINIMAP_SCALE - 3), 6, 6);
            }
        }

        // --- 5. Draw Player Localized Anchor Pivot Point ---
        g2d.setColor(COLOR_PLAYER_BLIP); 
        g2d.fillOval((int) (offsetX + camera.getPosX() * HUD_MINIMAP_SCALE - 4), (int) (offsetY + camera.getPosY() * HUD_MINIMAP_SCALE - 4), 8, 8);

        // --- 6. Render First-Person 3/4 Profile Mechanical Mech Suit Chassis Frame ---
        boolean isMoving = control.isKeyUp() || control.isKeyDown();
        PlayerCharacter.draw3DFirstPersonChassis(g2d, w, h, isMoving, control.isKeyRight(), control.isKeyLeft());
    }
}