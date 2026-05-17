public class Camera {
    // Current physical location coordinates of the player camera in the maze
    private double posX;
    private double posY;

    // Smooth Interpolation Engine: Tracks actual angle vs intended angle to remove jerky snapping
    private double actualAngle;
    private double targetAngle;

    // Core direction vectors (re-synthesized from the actual angle matrix every tick)
    private double dirX;
    private double dirY;
    
    // Perpendicular camera viewport plane vectors (determines Field of View)
    private double planeX;
    private double planeY;

    // --- TUNED MOVEMENT CONFIGURATION CONSTANTS ---
    private final double moveSpeed = 0.032;  // Tamed down from 0.05 for precise, elegant navigation
    private final double rotSpeed = 0.038;   // Lowered from 0.05 to prevent disorienting fast spins
    private final double lerpFactor = 0.14;  // Slightly relaxed from 0.18 for smoother camera deceleration

    /**
     * Initializes the 3D vantage camera at a specific starting position.
     */
    public Camera(double startX, double startY) {
        this.posX = startX;
        this.posY = startY;
        this.actualAngle = 0.0;
        this.targetAngle = 0.0;
        updateVectors();
    }

    /**
     * Updates the smooth camera tracking and processes physical translation movement vectors.
     * @param keyUp      Is the forward key actively pressed
     * @param keyDown    Is the backward key actively pressed
     * @param keyLeft     Is the rotate left key actively pressed
     * @param keyRight    Is the rotate right key actively pressed
     * @param maze        Reference to the maze grid to process wall physics collisions
     */
    public void update(boolean keyUp, boolean keyDown, boolean keyLeft, boolean keyRight, Maze maze) {
        // 1. Process Rotational Input Intents
        if (keyLeft)  targetAngle -= rotSpeed;
        if (keyRight) targetAngle += rotSpeed;

        // 2. Camera Angular Movement Smoothing (LERP)
        // Drifts the actual angle smoothly toward the target angle to add realistic weight
        actualAngle += (targetAngle - actualAngle) * lerpFactor;

        // 3. Re-calculate Direction and Perpendicular View Plane Matrices
        updateVectors();

        // 4. Process Forward / Backward Translation Velocity with Wall-Sliding Physics
        if (keyUp) {
            double nextX = posX + dirX * moveSpeed;
            double nextY = posY + dirY * moveSpeed;
            if (!maze.isWall((int)posY, (int)nextX)) posX = nextX;
            if (!maze.isWall((int)nextY, (int)posX)) posY = nextY;
        }
        if (keyDown) {
            double nextX = posX - dirX * moveSpeed;
            double nextY = posY - dirY * moveSpeed;
            if (!maze.isWall((int)posY, (int)nextX)) posX = nextX;
            if (!maze.isWall((int)nextY, (int)posX)) posY = nextY;
        }
    }

    /**
     * Synchronizes direction and perpendicular camera plane vectors from the running actual angle.
     */
    private void updateVectors() {
        dirX = Math.cos(actualAngle);
        dirY = Math.sin(actualAngle);
        
        // Multiplying the perpendicular vector by 0.66 achieves a comfortable ~66-degree FOV
        planeX = -dirY * 0.66;
        planeY = dirX * 0.66;
    }

    /**
     * Resets the camera positioning and angles back to baseline variables.
     */
    public void reset(double startX, double startY) {
        this.posX = startX;
        this.posY = startY;
        this.actualAngle = 0.0;
        this.targetAngle = 0.0;
        updateVectors();
    }

    // --- Core Engine Projection Getters ---
    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public double getDirX() { return dirX; }
    public double getDirY() { return dirY; }
    public double getPlaneX() { return planeX; }
    public double getPlaneY() { return planeY; }
}