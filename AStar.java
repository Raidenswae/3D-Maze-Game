import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class AStar {
    
    private static class Node implements Comparable<Node> {
        int row, col;
        int gCost, hCost, fCost;
        Node parent;

        Node(int row, int col, int gCost, int hCost, Node parent) {
            this.row = row;
            this.col = col;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }

    public static List<int[]> findPath(int[][] grid, int startRow, int startCol, int goalRow, int goalCol) {
        int rows = grid.length;
        int cols = grid.length; // Safe boundary reading for columns

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[rows][cols];

        int initialH = Math.abs(startRow - goalRow) + Math.abs(startCol - goalCol);
        openSet.add(new Node(startRow, startCol, 0, initialH, null));

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.row == goalRow && current.col == goalCol) {
                List<int[]> path = new ArrayList<>();
                Node temp = current;
                while (temp != null) {
                    path.add(new int[]{temp.row, temp.col});
                    temp = temp.parent;
                }
                Collections.reverse(path);
                return path; 
            }

            closedSet[current.row][current.col] = true;

            for (int[] dir : directions) {
                // Resolved compiler type mismatch errors
                int neighborRow = current.row + dir[0]; 
                int neighborCol = current.col + dir[1]; 

                if (neighborRow < 0 || neighborRow >= rows || neighborCol < 0 || neighborCol >= cols) continue;
                if (grid[neighborRow][neighborCol] == 1 || closedSet[neighborRow][neighborCol]) continue;

                int newGCost = current.gCost + 1;
                int neighborH = Math.abs(neighborRow - goalRow) + Math.abs(neighborCol - goalCol);

                boolean skip = false;
                for (Node openNode : openSet) {
                    if (openNode.row == neighborRow && openNode.col == neighborCol && openNode.gCost <= newGCost) {
                        skip = true;
                        break;
                    }
                }

                if (!skip) {
                    openSet.add(new Node(neighborRow, neighborCol, newGCost, neighborH, current));
                }
            }
        }
        return new ArrayList<>(); 
    }
}