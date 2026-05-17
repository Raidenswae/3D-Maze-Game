import javax.swing.JFrame;

public class Window extends JFrame {
    public Window() {
        setTitle("Maze Game with 3D Raycasting & A* Pathfinding");
        setSize(720, 520); // Widened to properly hold both 3D viewpoint and MiniMap HUD
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        Map map = new Map();
        add(map);   
        
        map.requestFocusInWindow(); // Focus inputs instantly on launch
    }
}