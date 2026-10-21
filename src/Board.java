import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** The board model and its Swing drawing surface. Coordinates are zero-based. */
public class Board extends JPanel {
	private static final int MARGIN = 40;
	private static final int MIN_CELL_SIZE = 24;
	private final int boardSize;
	private final Location[][] locations;

	public Board(int boardSize) {
		if (boardSize < 5) throw new IllegalArgumentException("Board must be at least 5 by 5.");
		this.boardSize = boardSize;
		this.locations = new Location[boardSize][boardSize];
		setBackground(new Color(200, 150, 235));
		setPreferredSize(new Dimension(700, 700));
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int cell = getCellSize();
		int extent = cell * (boardSize - 1);
		int left = (getWidth() - extent) / 2;
		int top = (getHeight() - extent) / 2;

		g.setColor(Color.BLACK);
		for (int i = 0; i < boardSize; i++) {
			int offset = i * cell;
			g.drawLine(left, top + offset, left + extent, top + offset);
			g.drawLine(left + offset, top, left + offset, top + extent);
		}

		int pieceSize = Math.max(8, (int) (cell * 0.78));
		for (int x = 0; x < boardSize; x++) {
			for (int y = 0; y < boardSize; y++) {
				Location loc = locations[x][y];
				if (loc == null) continue;
				g.setColor(loc.getPlayer() == 1 ? Color.BLACK : Color.WHITE);
				int centerX = left + x * cell;
				int centerY = top + y * cell;
				g.fillOval(centerX - pieceSize / 2, centerY - pieceSize / 2, pieceSize, pieceSize);
				g.setColor(Color.DARK_GRAY);
				g.drawOval(centerX - pieceSize / 2, centerY - pieceSize / 2, pieceSize, pieceSize);
			}
		}
		g.dispose();
	}

	public int getCellSize() {
		int available = Math.max(1, Math.min(getWidth(), getHeight()) - 2 * MARGIN);
		return Math.max(MIN_CELL_SIZE, available / (boardSize - 1));
	}

	public int getMargin() { return MARGIN; }
	public int getBoardSize() { return boardSize; }

	/** Converts a click to the nearest intersection, or null when outside the board. */
	public Location locationAt(int pixelX, int pixelY) {
		int cell = getCellSize();
		int extent = cell * (boardSize - 1);
		int left = (getWidth() - extent) / 2;
		int top = (getHeight() - extent) / 2;
		int x = Math.round((pixelX - left) / (float) cell);
		int y = Math.round((pixelY - top) / (float) cell);
		if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return null;
		if (Math.abs(pixelX - (left + x * cell)) > cell / 2 || Math.abs(pixelY - (top + y * cell)) > cell / 2) return null;
		return new Location(x, y, 0);
	}

	public boolean isLegal(int x, int y) {
		return x >= 0 && x < boardSize && y >= 0 && y < boardSize && locations[x][y] == null;
	}

	public boolean addPiece(Location piece) {
		if (piece == null || !isLegal(piece.getX(), piece.getY())) return false;
		locations[piece.getX()][piece.getY()] = piece;
		repaint();
		return true;
	}

	public boolean isFull() {
		for (int x = 0; x < boardSize; x++)
			for (int y = 0; y < boardSize; y++)
				if (locations[x][y] == null) return false;
		return true;
	}

	/** Five or more consecutive stones wins. */
	public boolean isWin(int x, int y, int player) {
		if (x < 0 || x >= boardSize || y < 0 || y >= boardSize || locations[x][y] == null || locations[x][y].getPlayer() != player) return false;
		int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
		for (int[] direction : directions) {
			int count = 1;
			count += countDirection(x, y, direction[0], direction[1], player);
			count += countDirection(x, y, -direction[0], -direction[1], player);
			if (count >= 5) return true;
		}
		return false;
	}

	private int countDirection(int x, int y, int dx, int dy, int player) {
		int count = 0;
		for (int nx = x + dx, ny = y + dy; nx >= 0 && nx < boardSize && ny >= 0 && ny < boardSize; nx += dx, ny += dy) {
			if (locations[nx][ny] == null || locations[nx][ny].getPlayer() != player) break;
			count++;
		}
		return count;
	}
}
