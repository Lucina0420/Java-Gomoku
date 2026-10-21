import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Ui {
    private final JFrame frame = new JFrame("Gomoku");
    private Board gomokuBoard;
    private int currentPlayer = 1;
    private String playerName1;
    private String playerName2;
    private BufferedWriter moveWriter;
    private boolean gameOver;

    public void init() {
        if (!askForPlayers()) return;
        startGame();
    }

    private boolean askForPlayers() {
        playerName1 = askName("Player 1 (Black), enter your name:");
        if (playerName1 == null) return false;
        playerName2 = askName("Player 2 (White), enter your name:");
        if (playerName2 == null) return false;
        JOptionPane.showMessageDialog(null, "Welcome, " + playerName1 + " and " + playerName2 + "!\nBlack moves first. Get five or more stones in a row to win.");
        return true;
    }

    private String askName(String prompt) {
        while (true) {
            String name = JOptionPane.showInputDialog(null, prompt);
            if (name == null) return null;
            name = name.trim();
            if (!name.isEmpty()) return name;
            JOptionPane.showMessageDialog(null, "Please enter a name.");
        }
    }

    private void startGame() {
        String[] options = {"14x14", "15x15", "17x17", "20x20"};
        int choice = JOptionPane.showOptionDialog(null, "Choose the board size:", "New Gomoku Game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        if (choice < 0) return;
        int boardSize = Integer.parseInt(options[choice].substring(0, options[choice].indexOf('x')));
        currentPlayer = 1;
        gameOver = false;
        gomokuBoard = new Board(boardSize);
        frame.getContentPane().removeAll();
        frame.add(gomokuBoard, BorderLayout.CENTER);
        frame.setSize(760, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        openMoveLog();
        gomokuBoard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { play(e); }
        });
        frame.setTitle("Gomoku — " + playerName1 + " (Black) to move");
        frame.revalidate();
        frame.repaint();
    }

    private void openMoveLog() {
        closeFileWriter();
        try {
            Path logPath = Paths.get("moves.txt");
            moveWriter = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            moveWriter.write("\n--- New game: " + playerName1 + " vs " + playerName2 + " (" + gomokuBoard.getBoardSize() + "x" + gomokuBoard.getBoardSize() + " board) ---\n");
            moveWriter.flush();
        } catch (IOException ex) {
            moveWriter = null;
            JOptionPane.showMessageDialog(frame, "Moves will not be saved: " + ex.getMessage(), "Log unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void writeMove(int player, int x, int y) {
        if (moveWriter == null) return;
        try {
            String name = player == 1 ? playerName1 : playerName2;
            moveWriter.write(name + " (" + (player == 1 ? "Black" : "White") + "): (" + (x + 1) + ", " + (y + 1) + ")\n");
            moveWriter.flush();
        } catch (IOException ex) {
            closeFileWriter();
            JOptionPane.showMessageDialog(frame, "Could not save the move log: " + ex.getMessage(), "Log error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void play(MouseEvent e) {
        if (gameOver) return;
        Location point = gomokuBoard.locationAt(e.getX(), e.getY());
        if (point == null) return;
        int x = point.getX();
        int y = point.getY();
        if (!gomokuBoard.isLegal(x, y)) {
            frame.setTitle("That intersection is occupied — " + (currentPlayer == 1 ? playerName1 + " (Black)" : playerName2 + " (White)") + " to move");
            return;
        }

        int movingPlayer = currentPlayer;
        gomokuBoard.addPiece(new Location(x, y, movingPlayer));
        writeMove(movingPlayer, x, y);
        if (gomokuBoard.isWin(x, y, movingPlayer)) {
            gameOver = true;
            String winner = movingPlayer == 1 ? playerName1 : playerName2;
            endGame(winner + " wins!");
            return;
        }
        if (gomokuBoard.isFull()) {
            gameOver = true;
            endGame("It's a draw!");
            return;
        }
        currentPlayer = -movingPlayer;
        String next = currentPlayer == 1 ? playerName1 + " (Black)" : playerName2 + " (White)";
        frame.setTitle("Gomoku — " + next + " to move");
    }

    private void endGame(String result) {
        closeFileWriter();
        int choice = JOptionPane.showOptionDialog(frame, result, "Game Over", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, new String[] {"Play again", "Exit"}, "Play again");
        if (choice == 0) startGame();
        else System.exit(0);
    }

    private void closeFileWriter() {
        if (moveWriter == null) return;
        try { moveWriter.close(); }
        catch (IOException ex) { ex.printStackTrace(); }
        finally { moveWriter = null; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ui().init());
    }
}

