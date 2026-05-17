import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

public class Enemy {
    public double x;
    public double y;
    public double distance; 
    public boolean isAlive = true;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(double playerX, double playerY, Maze maze) {
        if (!isAlive) return;

        double diffX = playerX - this.x;
        double diffY = playerY - this.y;
        double dist = Math.sqrt(diffX * diffX + diffY * diffY);

        if (dist < 6.0 && dist > 0.4) {
            double enemySpeed = 0.022; 
            double moveX = (diffX / dist) * enemySpeed;
            double moveY = (diffY / dist) * enemySpeed;

            if (!maze.isWall((int)this.y, (int)(this.x + moveX))) this.x += moveX;
            if (!maze.isWall((int)(this.y + moveY), (int)this.x)) this.y += moveY;
        }
    }

    public void draw3DStripe(Graphics2D g2d, int stripe, int drawStartX, int spriteWidth, int drawStartY, int spriteHeight) {
        double relX = (double) (stripe - drawStartX) / spriteWidth;
        if (relX < 0 || relX > 1.0) return;

        long time = System.currentTimeMillis() + (int)(x * 500); 
        double hover = Math.sin(time * 0.01) * 0.05;             
        double sway = Math.cos(time * 0.007) * 0.10; 

        // Precision ground pinning calculations
        int adjustedStartY = drawStartY + (int) (hover * spriteHeight);
        int adjustedEndY = adjustedStartY + spriteHeight;

        int midX = drawStartX + (spriteWidth / 2);
        double normX = (double) (stripe - midX) / (spriteWidth / 2); 

        int strokeThickness = Math.max(1, spriteHeight / 160);
        g2d.setStroke(new BasicStroke(strokeThickness));

        for (int y = adjustedStartY; y < adjustedEndY; y++) {
            double normY = (double) (y - adjustedStartY) / spriteHeight;

            // 1. Cybernetic Visor / Dome Head Unit (Proportional to Player Character Upper Helmet)
            if (normY > 0.05 && normY < 0.30) {
                if (Math.abs(normX) < 0.28) {
                    Color armorColor = new Color(80, 16, 24);
                    if (normY > 0.12 && normY < 0.18 && Math.abs(normX - sway) < 0.12) {
                        g2d.setColor(new Color(255, 0, 60)); // Glowing tracking lens
                    } else {
                        double shade = 1.0 - Math.abs(normX) * 0.3;
                        g2d.setColor(new Color((int)(armorColor.getRed() * shade), (int)(armorColor.getGreen() * shade), (int)(armorColor.getBlue() * shade)));
                    }
                    g2d.fillRect(stripe, y, 1, 1);
                    continue;
                }
            }

            // 2. Torso Central Enclosure (Proportional to Player Character Main Torso Layout)
            if (normY >= 0.30 && normY <= 0.65) {
                double bodyWidth = 0.42; 
                if (Math.abs(normX) < bodyWidth) {
                    Color chestColor = new Color(50, 55, 65);
                    if (Math.abs(normX) < 0.06) {
                        g2d.setColor(new Color(255, 130, 0)); // Core reactor trim
                    } else {
                        double shade = 1.0 - Math.abs(normX) * 0.4;
                        g2d.setColor(new Color((int)(chestColor.getRed() * shade), (int)(chestColor.getGreen() * shade), (int)(chestColor.getBlue() * shade)));
                    }
                    g2d.fillRect(stripe, y, 1, 1);
                    continue;
                }
            }

            // 3. Lower Support Strut / Thruster Assembly (Proportional to Player Character Legs Layout)
            if (normY > 0.65 && normY < 0.95) {
                if (Math.abs(normX) < 0.25) {
                    if (normY > 0.75 && normY < 0.82) {
                        g2d.setColor(new Color(255, 95, 0)); // Stabilizing exhaust emitter flare
                    } else {
                        g2d.setColor(new Color(25, 30, 40));
                    }
                    g2d.fillRect(stripe, y, 1, 1);
                    continue;
                }
            }
        }
        g2d.setStroke(new BasicStroke(1));
    }
}