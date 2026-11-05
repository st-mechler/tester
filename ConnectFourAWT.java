import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConnectFourAWT extends java.awt.Frame {
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CELL_SIZE = 86;
    private static final int PADDING = 12;

    private final int[][] board = new int[ROWS][COLUMNS];
    private int currentPlayer = 1;
    private boolean gameOver = false;

    private final Label statusLabel = new Label("Spieler Rot ist am Zug.");
    private final BoardCanvas boardCanvas = new BoardCanvas();

    public ConnectFourAWT() {
        super("Vier-Gewinnt Pilot (AWT)");

        setLayout(new BorderLayout());

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        Button resetButton = new Button("Neues Spiel");
        resetButton.addActionListener(e -> resetGame());

        Panel bottomPanel = new Panel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(resetButton, BorderLayout.EAST);

        add(boardCanvas, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void resetGame() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                board[row][col] = 0;
            }
        }

        currentPlayer = 1;
        gameOver = false;
        statusLabel.setText("Spieler Rot ist am Zug.");
        boardCanvas.repaint();
    }

    private void handleMove(int column) {
        if (gameOver || column < 0 || column >= COLUMNS) {
            return;
        }

        int row = getAvailableRow(column);
        if (row < 0) {
            statusLabel.setText("Spalte voll. Bitte waehle eine andere Spalte.");
            return;
        }

        board[row][column] = currentPlayer;

        if (hasWinner(row, column, currentPlayer)) {
            gameOver = true;
            statusLabel.setText("Spieler " + playerName(currentPlayer) + " gewinnt!");
        } else if (isBoardFull()) {
            gameOver = true;
            statusLabel.setText("Unentschieden. Starte ein neues Spiel.");
        } else {
            currentPlayer = currentPlayer == 1 ? 2 : 1;
            statusLabel.setText("Spieler " + playerName(currentPlayer) + " ist am Zug.");
        }

        boardCanvas.repaint();
    }

    private int getAvailableRow(int column) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                return row;
            }
        }
        return -1;
    }

    private boolean isBoardFull() {
        for (int col = 0; col < COLUMNS; col++) {
            if (board[0][col] == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasWinner(int row, int col, int player) {
        return countConnected(row, col, 1, 0, player) >= 4
            || countConnected(row, col, 0, 1, player) >= 4
            || countConnected(row, col, 1, 1, player) >= 4
            || countConnected(row, col, 1, -1, player) >= 4;
    }

    private int countConnected(int row, int col, int dr, int dc, int player) {
        int total = 1;
        total += countDirection(row, col, dr, dc, player);
        total += countDirection(row, col, -dr, -dc, player);
        return total;
    }

    private int countDirection(int row, int col, int dr, int dc, int player) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;

        while (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && board[r][c] == player) {
            count++;
            r += dr;
            c += dc;
        }

        return count;
    }

    private String playerName(int player) {
        return player == 1 ? "Rot" : "Gelb";
    }

    private class BoardCanvas extends Canvas {
        BoardCanvas() {
            int width = COLUMNS * CELL_SIZE + (PADDING * 2);
            int height = ROWS * CELL_SIZE + (PADDING * 2);
            setPreferredSize(new Dimension(width, height));
            setBackground(new Color(25, 88, 172));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int col = (e.getX() - PADDING) / CELL_SIZE;
                    handleMove(col);
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(25, 88, 172));
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    int x = PADDING + col * CELL_SIZE;
                    int y = PADDING + row * CELL_SIZE;
                    int discSize = CELL_SIZE - 12;

                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + 6, y + 6, discSize, discSize);

                    if (board[row][col] == 1) {
                        g2.setColor(new Color(210, 35, 35));
                        g2.fillOval(x + 8, y + 8, discSize - 4, discSize - 4);
                    } else if (board[row][col] == 2) {
                        g2.setColor(new Color(235, 196, 20));
                        g2.fillOval(x + 8, y + 8, discSize - 4, discSize - 4);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        ConnectFourAWT game = new ConnectFourAWT();
        game.setVisible(true);
    }
}