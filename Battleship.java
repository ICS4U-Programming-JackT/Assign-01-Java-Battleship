import java.util.Scanner;
import java.util.Random;

/**
 * Simple Battleship game implementation.
 * Player vs computer on a 4x4 board.
 *
 * Not taught:
 * ANSI escape color codes:
 * https://en.wikipedia.org/wiki/ANSI_escape_code
 * 2D arrays (String[][]):
 * https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html
 *
 * Symbols:
 * 0 = empty water
 * S = ship
 * X = hit
 * M = miss
 *
 * Colors are used to highlight ship, hit, and miss states.
 *
 * @author  Jack
 * @version 1.0
 * @since   2025-09-28
 */
public final class Battleship {

    /** Shared Scanner for input (keyboard). */
    private static final Scanner SCANNER = new Scanner(System.in);

    /** Random number generator for computer moves. */
    private static final Random RAND = new Random();

    /** Symbol representing empty water. */
    private static final String EMPTY = "0";

    /** Symbol representing a ship. */
    private static final String SHIP = "S";

    /** Symbol representing a hit. */
    private static final String HIT = "X";

    /** Symbol representing a miss. */
    private static final String MISS = "M";

    /** Reset color code (goes back to normal text). */
    private static final String RESET = "\u001B[0m";

    /** Red color code for hits. */
    private static final String RED = "\u001B[31m";

    /** Green color code for ships. */
    private static final String GREEN = "\u001B[32m";

    /** Yellow color code for misses. */
    private static final String YELLOW = "\u001B[33m";

    /** Cyan color code for empty cells. */
    private static final String CYAN = "\u001B[36m";

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Battleship() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Entry point of the program.
     * This is the first method Java runs.
     *
     * @param args command line arguments (unused)
     */
    public static void main(final String[] args) {
        mainGame();
    }

    /**
     * Runs the main game loop.
     * Handles tutorial prompt, board setup, player/computer turns,
     * and win/lose conditions.
     */
    public static void mainGame() {
        System.out.println("Welcome to Battleship!");

        // Ask player if they want to see instructions
        System.out.print("Would you like to see the tutorial? (y/n): ");
        final String choice = SCANNER.nextLine();
        if (choice.equalsIgnoreCase("y")) {
            System.out.println("\nTutorial:");
            System.out.println("You and the computer each get a 4x4 grid.");
            System.out.println("S = Ship, "
                    + RED + "X = Hit" + RESET + ", "
                    + YELLOW + "M = Miss" + RESET + ", "
                    + CYAN + "0 = Empty" + RESET + ".");
            System.out.println("Take turns guessing enemy positions "
                    + "until all ships are sunk.");
            System.out.println("Good luck!\n");
        }

        // Create player and enemy boards with ships placed
        String[][] playerGrid = setupGrid(4, 4);
        String[][] enemyGrid = setupGrid(4, 4);

        // This grid shows what the player knows about the enemy
        // At the start, everything is hidden (just 0)
        String[][] playerViewOfEnemy = new String[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                playerViewOfEnemy[i][j] = EMPTY;
            }
        }

        boolean gameOver = false;

        // Main turn loop: keep going until someone wins
        while (!gameOver) {
            // Show both boards
            System.out.println("\nYour grid:");
            displayGrid(playerGrid, true);

            System.out.println("\nEnemy grid:");
            displayGrid(playerViewOfEnemy, false);

            // Player takes a turn
            int[] coords = handleInput(); // row and column chosen by user
            gameOver = handleAttacks(
                    enemyGrid, playerViewOfEnemy, coords[0], coords[1]);

            // Check if player won
            if (gameOver) {
                System.out.println(GREEN + "You win!" + RESET);
                break;
            }

            // Enemy takes a random shot
            int x = RAND.nextInt(4); // random row (0–3)
            int y = RAND.nextInt(4); // random column (0–3)
            System.out.println("Enemy fires at ("
                    + (x + 1) + ", " + (y + 1) + ")");

            // Apply computer attack to player’s grid
            gameOver = handleAttacks(playerGrid, playerGrid, x, y);

            // Check if computer won
            if (gameOver) {
                System.out.println(RED
                        + "The enemy has sunk all your ships. Game over!"
                        + RESET);
            }
        }
    }

    /**
     * Sets up a board of the given size and places ships randomly.
     *
     * @param size  the grid size (e.g., 4 for 4x4)
     * @param ships number of ships to place
     * @return grid with ships placed
     */
    public static String[][] setupGrid(final int size, final int ships) {
        String[][] grid = new String[size][size];

        // Fill the board with empty water first
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = EMPTY;
            }
        }

        // Randomly place ships until we hit the target number
        int placed = 0;
        while (placed < ships) {
            int x = RAND.nextInt(size);
            int y = RAND.nextInt(size);
            if (grid[x][y].equals(EMPTY)) {
                grid[x][y] = SHIP;
                placed++;
            }
        }
        return grid;
    }

    /**
     * Displays a grid with colors for each type of cell.
     *
     * @param grid      the board to show
     * @param showShips true if ships should be visible (player’s own board)
     */
    public static void displayGrid(final String[][] grid,
                                   final boolean showShips) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                String cell = grid[i][j];
                String out;

                // Only show ships on player’s grid, not enemy’s grid
                if (!showShips && cell.equals(SHIP)) {
                    out = CYAN + EMPTY + RESET;
                } else if (cell.equals(SHIP)) {
                    out = GREEN + SHIP + RESET;
                } else if (cell.equals(HIT)) {
                    out = RED + HIT + RESET;
                } else if (cell.equals(MISS)) {
                    out = YELLOW + MISS + RESET;
                } else {
                    out = CYAN + EMPTY + RESET;
                }
                System.out.print(out + " ");
            }
            System.out.println(); // move to next row
        }
    }

    /**
     * Gets player input for row and column.
     * Loops until valid input is given (1–4).
     *
     * @return int[] containing row and column
     */
    public static int[] handleInput() {
        int row = -1;
        int col = -1;
        boolean valid = false;

        while (!valid) {
            try {
                System.out.print("Enter row (1-4): ");
                row = Integer.parseInt(SCANNER.nextLine()) - 1;

                System.out.print("Enter column (1-4): ");
                col = Integer.parseInt(SCANNER.nextLine()) - 1;

                // Check if coordinates are in range
                if (row >= 0 && row < 4 && col >= 0 && col < 4) {
                    valid = true;
                } else {
                    System.out.println("Invalid coordinates. Try again.");
                }
            } catch (NumberFormatException e) {
                // If user types something not a number, this runs
                System.out.println("Invalid input. Enter a number 1-4.");
            }
        }
        return new int[] {row, col};
    }

    /**
     * Handles attacks on the board.
     * Updates grid state and checks win condition.
     *
     * @param targetGrid the board being attacked
     * @param viewGrid   the attacker’s view of the board
     * @param row        row of attack
     * @param col        column of attack
     * @return true if all ships are sunk
     */
    public static boolean handleAttacks(final String[][] targetGrid,
                                        final String[][] viewGrid,
                                        final int row,
                                        final int col) {
        // Check if shot hits a ship
        if (targetGrid[row][col].equals(SHIP)) {
            System.out.println(RED + "Hit!" + RESET);
            targetGrid[row][col] = HIT;
            viewGrid[row][col] = HIT;
        } else if (targetGrid[row][col].equals(EMPTY)) {
            System.out.println(YELLOW + "Miss!" + RESET);
            targetGrid[row][col] = MISS;
            viewGrid[row][col] = MISS;
        }

        // Check if any ships remain on this board
        for (int i = 0; i < targetGrid.length; i++) {
            for (int j = 0; j < targetGrid[i].length; j++) {
                if (targetGrid[i][j].equals(SHIP)) {
                    return false; // still ships left
                }
            }
        }
        return true; // no ships remain = game over
    }
}
