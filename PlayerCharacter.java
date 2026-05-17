import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

public class PlayerCharacter {
    private static double smoothSwayX = 0.0;
    private static double smoothSwayY = 0.0;

    /**
     * Renders the full-body slender humanoid chassis scaled dynamically to match 
     * the exact structural size proportions of the hostile enemy drones.
     */
    public static void draw3DFirstPersonChassis(Graphics2D g2d, int w, int h, 
                                                boolean isMoving, boolean turningRight, boolean turningLeft) {
        long time = System.currentTimeMillis();
        
        // 1. ANIMATION ENGINE
        double breathe = Math.sin(time * 0.002);                       
        double stride = isMoving ? Math.sin(time * 0.014) : 0.0;        
        double strideCos = isMoving ? Math.cos(time * 0.014) : 0.0;
        double jitter = Math.sin(time * 0.014);                         
        
        // 2. MOMENTUM SHIFT REGISTERS
        double targetSwayX = 0.0;
        double targetSwayY = isMoving ? Math.abs(Math.sin(time * 0.007)) * 2.5 : 0.0;

        if (turningLeft)  targetSwayX = 14.0;  
        if (turningRight) targetSwayX = -14.0; 

        smoothSwayX += (targetSwayX - smoothSwayX) * 0.12;
        smoothSwayY += (targetSwayY - smoothSwayY) * 0.12;

        // FIXED FOOT INVERSION MATH
        int groundY = (int) (h * 0.94); 
        int centerX = (w / 2) + (int) smoothSwayX;

        // --- SIZE PROPORTION SYNCHRONIZATION MATRIX ---
        // Instead of hardcoded pixel sizes, we extract a base scale factor tied directly 
        // to the viewport height (representing the unit scale if the enemy stood adjacent).
        int unitScale = (int) (h * 0.35); 
        int midY = groundY - (int)(unitScale * 0.75) + (int) smoothSwayY; 

        double turnRotationFactor = (smoothSwayX / 14.0) * 0.35; 

        int strokeThickness = Math.max(1, unitScale / 35);
        g2d.setStroke(new BasicStroke(strokeThickness)); 

        // --- LAYER 1: MECHANICAL LEGS ---
        int hipY = midY + (int)(unitScale * 0.21);
        int leftFootLift = isMoving ? (int) (Math.max(0, stride) * -(unitScale * 0.05)) : 0;
        int rightFootLift = isMoving ? (int) (Math.max(0, strideCos) * -(unitScale * 0.05)) : 0;

        // Left Leg Strut
        int lx = centerX - (int)(unitScale * 0.14);
        int lLegW = (int)(unitScale * 0.10);
        int lExt = (int) (lLegW * turnRotationFactor); 
        g2d.setColor(new Color(20, 24, 32)); 
        g2d.fillRect(lx, hipY, (int)(unitScale * 0.05), groundY - hipY + leftFootLift);
        g2d.setColor(new Color(0, 160, 180));
        g2d.fillRoundRect(lx - 4, hipY + 12 + leftFootLift, lLegW - Math.abs(lExt), (int)(unitScale * 0.27), 6, 6);
        if (lExt != 0) {
            g2d.setColor(new Color(0, 120, 140));
            g2d.fillRect(lx - 4 + (lExt > 0 ? lLegW - lExt : 0), hipY + 12 + leftFootLift, Math.abs(lExt), (int)(unitScale * 0.27));
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(lx - 4, hipY + 12 + leftFootLift, lLegW, (int)(unitScale * 0.27), 6, 6);
        g2d.setColor(new Color(45, 50, 60));
        g2d.fillRect(centerX - (int)(unitScale * 0.19) + (lExt / 2), groundY - 8 + leftFootLift, (int)(unitScale * 0.13), 8);

        // Right Leg Strut
        int rx = centerX + (int)(unitScale * 0.10);
        int rLegW = (int)(unitScale * 0.10);
        int rExt = (int) (rLegW * turnRotationFactor);
        g2d.setColor(new Color(20, 24, 32));
        g2d.fillRect(rx, hipY, (int)(unitScale * 0.05), groundY - hipY + rightFootLift);
        g2d.setColor(new Color(0, 160, 180));
        g2d.fillRoundRect(rx - 4, hipY + 12 + rightFootLift, rLegW - Math.abs(rExt), (int)(unitScale * 0.27), 6, 6);
        if (rExt != 0) {
            g2d.setColor(new Color(0, 120, 140));
            g2d.fillRect(rx - 4 + (rExt > 0 ? rLegW - rExt : 0), hipY + 12 + rightFootLift, Math.abs(rExt), (int)(unitScale * 0.27));
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rx - 4, hipY + 12 + rightFootLift, rLegW, (int)(unitScale * 0.27), 6, 6);
        g2d.setColor(new Color(45, 50, 60));
        g2d.fillRect(centerX + (int)(unitScale * 0.06) + (rExt / 2), groundY - 8 + rightFootLift, (int)(unitScale * 0.13), 8);

        // --- LAYER 2: STREAMLINED SHOULDERS & FOREARMS ---
        int shoulderY = midY - (int)(unitScale * 0.12) + (int)(jitter * 1.5);
        int armW = (int)(unitScale * 0.10);
        int armExt = (int) (armW * turnRotationFactor);
        
        // Left Arm Casing
        int lax = centerX - (int)(unitScale * 0.28);
        g2d.setColor(new Color(40, 45, 55));
        g2d.fillRoundRect(lax, shoulderY, armW - Math.abs(armExt), (int)(unitScale * 0.27), 8, 8);
        if (armExt != 0) {
            g2d.setColor(new Color(25, 30, 38));
            g2d.fillRect(lax + (armExt > 0 ? armW - armExt : 0), shoulderY, Math.abs(armExt), (int)(unitScale * 0.27));
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(lax, shoulderY, armW, (int)(unitScale * 0.27), 8, 8);

        // Right Arm Casing
        int rax = centerX + (int)(unitScale * 0.19);
        g2d.setColor(new Color(40, 45, 55));
        g2d.fillRoundRect(rax, shoulderY, armW - Math.abs(armExt), (int)(unitScale * 0.27), 8, 8);
        if (armExt != 0) {
            g2d.setColor(new Color(25, 30, 38));
            g2d.fillRect(rax + (armExt > 0 ? armW - armExt : 0), shoulderY, Math.abs(armExt), (int)(unitScale * 0.27));
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rax, shoulderY, armW, (int)(unitScale * 0.27), 8, 8);

        // --- LAYER 3: EXTRUDED COMPACT TORSO ---
        int torsoW = (int) ((unitScale * 0.42) + (breathe * 3));
        int torsoH = (int)(unitScale * 0.35);
        int torsoX = centerX - (torsoW / 2);
        int torsoY = midY - (int)(unitScale * 0.15);

        int splitX = centerX + (int) (torsoW * 0.5 * turnRotationFactor);
        int[] xPointsLeft = {torsoX, splitX, splitX, torsoX};
        int[] yPointsLeft = {torsoY, torsoY, torsoY + torsoH, torsoY + torsoH};
        g2d.setColor(new Color(25, 30, 45)); 
        g2d.fillPolygon(xPointsLeft, yPointsLeft, 4);

        int[] xPointsRight = {splitX, torsoX + torsoW, torsoX + torsoW, splitX};
        int[] yPointsRight = {torsoY, torsoY, torsoY + torsoH, torsoY + torsoH};
        g2d.setColor(new Color(0, 160, 180)); 
        g2d.fillPolygon(xPointsRight, yPointsRight, 4);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(torsoX, torsoY, torsoW, torsoH, 12, 12);

        // --- LAYER 4: CYLINDRICAL HELMET VISOR ---
        int headW = (int)(unitScale * 0.27);
        int headH = (int)(unitScale * 0.29);
        int headX = centerX - (headW / 2);
        int headY = torsoY - headH + 8 + (int)(jitter * 1.5);
        int headSplitX = centerX + (int) (headW * 0.5 * turnRotationFactor);

        int[] xJawL = {headX + 6, headSplitX, headSplitX, headX + 6};
        int[] yJawL = {headY + (int)(headH * 0.54), headY + (int)(headH * 0.54), headY + headH, headY + headH};
        g2d.setColor(new Color(20, 25, 35));
        g2d.fillPolygon(xJawL, yJawL, 4);

        int[] xJawR = {headSplitX, headX + headW - 6, headSplitX + headW - 6, headSplitX}; // Structural bounds mirror corrections
        int[] xJawRCorrected = {headSplitX, headX + headW - 6, headX + headW - 6, headSplitX};
        g2d.setColor(new Color(40, 45, 55));
        g2d.fillPolygon(xJawRCorrected, yJawL, 4);
        
        int helmetExt = (int) (headW * turnRotationFactor);
        g2d.setColor(new Color(0, 230, 255)); 
        g2d.fillRoundRect(headX, headY, headW - Math.abs(helmetExt), (int)(headH * 0.72), 12, 12);
        if (helmetExt != 0) {
            g2d.setColor(new Color(0, 180, 210));
            g2d.fillRect(headX + (helmetExt > 0 ? headW - helmetExt : 0), headY, Math.abs(helmetExt), (int)(headH * 0.72));
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(headX, headY, headW, (int)(headH * 0.72), 12, 12);

        g2d.setStroke(new BasicStroke(1)); 
    }
}