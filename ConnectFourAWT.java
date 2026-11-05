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

    private enum Language {
        DE,
        EN
    }

    private final int[][] board = new int[ROWS][COLUMNS];
    private final boolean[][] winningCells = new boolean[ROWS][COLUMNS];
    private int currentPlayer = 1;
    private int winnerPlayer = 0;
    private boolean gameOver = false;
    private Language currentLanguage = Language.DE;

    private final Label statusLabel = new Label();
    private final Button resetButton = new Button();
    private final Button languageButton = new Button();
    private final BoardCanvas boardCanvas = new BoardCanvas();

    public ConnectFourAWT() {
        super("Vier-Gewinnt Pilot (AWT)");

        setLayout(new BorderLayout());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        resetButton.addActionListener(e -> resetGame());
        languageButton.addActionListener(e -> switchLanguage());

        Panel actionPanel = new Panel(new BorderLayout());
        actionPanel.add(languageButton, BorderLayout.WEST);
        actionPanel.add(resetButton, BorderLayout.EAST);

        Panel bottomPanel = new Panel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        add(boardCanvas, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
                System.exit(0);
            }
        });

        applyLanguage();
        resetGame();

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void switchLanguage() {
        currentLanguage = currentLanguage == Language.DE ? Language.EN : Language.DE;
        applyLanguage();
        updateStatusLabel();
    }

    private void applyLanguage() {
        setTitle(currentLanguage == Language.DE ? "Vier-Gewinnt Pilot (AWT)" : "Connect Four Pilot (AWT)");
        resetButton.setLabel(currentLanguage == Language.DE ? "Neues Spiel" : "New game");
        languageButton.setLabel(currentLanguage == Language.DE ? "EN" : "DE");
    }

    private void resetGame() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                board[row][col] = 0;
                winningCells[row][col] = false;
            }
        }

        currentPlayer = 1;
        winnerPlayer = 0;
        gameOver = false;
        updateStatusLabel();
        boardCanvas.repaint();
    }

    private void handleMove(int column) {
        if (gameOver || column < 0 || column >= COLUMNS) {
            return;
        }

        int row = getAvailableRow(column);
        if (row < 0) {
            statusLabel.setText(currentLanguage == Language.DE ? "Spalte voll. Bitte wähle eine andere Spalte." : "Column is full. Please choose another column.");
            return;
        }

        board[row][column] = currentPlayer;

        if (hasWinner(row, column, currentPlayer)) {
            winnerPlayer = currentPlayer;
            gameOver = true;
            updateStatusLabel();
        } else if (isBoardFull()) {
            gameOver = true;
            winnerPlayer = 0;
            updateStatusLabel();
        } else {
            currentPlayer = currentPlayer == 1 ? 2 : 1;
            updateStatusLabel();
        }

        boardCanvas.repaint();
    }

    private void updateStatusLabel() {
        if (gameOver) {
            if (winnerPlayer != 0) {
                String playerName = getPlayerName(winnerPlayer);
                statusLabel.setText(currentLanguage == Language.DE
                        ? "Spieler " + playerName + " gewinnt!"
                        : "Player " + playerName + " wins!");
                return;
            }

            statusLabel.setText(currentLanguage == Language.DE ? "Unentschieden. Starte ein neues Spiel." : "Draw. Start a new game.");
            return;
        }

        String playerName = getPlayerName(currentPlayer);
        statusLabel.setText(currentLanguage == Language.DE
                ? "Spieler " + playerName + " ist am Zug."
                : "Player " + playerName + " is up next.");
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
        boolean foundWinningLine = markWinningLine(row, col, 1, 0, player);
        foundWinningLine |= markWinningLine(row, col, 0, 1, player);
        foundWinningLine |= markWinningLine(row, col, 1, 1, player);
        foundWinningLine |= markWinningLine(row, col, 1, -1, player);
        return foundWinningLine;
    }

    private boolean markWinningLine(int row, int col, int dr, int dc, int player) {
        if (countConnected(row, col, dr, dc, player) < 4) {
            return false;
        }

        int firstRow = row;
        int firstCol = col;
        while (firstRow - dr >= 0 && firstRow - dr < ROWS
                && firstCol - dc >= 0 && firstCol - dc < COLUMNS
                && board[firstRow - dr][firstCol - dc] == player) {
            firstRow -= dr;
            firstCol -= dc;
        }

        while (firstRow >= 0 && firstRow < ROWS && firstCol >= 0 && firstCol < COLUMNS
                && board[firstRow][firstCol] == player) {
            winningCells[firstRow][firstCol] = true;
            firstRow += dr;
            firstCol += dc;
        }

        return true;
    }

    private int countConnected(int row, int col, int dr, int dc, int player) {
        int total = 1;
        total += countDirection(row, col, dr, dc, player);
        total += countDirection(row, col, -dr, -dc, player);
        return total;
    }

    private int countDirection(int row, int col, int dr, int dc, int player) {
        int count = 0;
        int currentRow = row + dr;
        int currentCol = col + dc;

        while (currentRow >= 0 && currentRow < ROWS && currentCol >= 0 && currentCol < COLUMNS
                && board[currentRow][currentCol] == player) {
            count++;
            currentRow += dr;
            currentCol += dc;
        }

        return count;
    }

    private String getPlayerName(int player) {
        if (currentLanguage == Language.DE) {
            return player == 1 ? "Rot" : "Gelb";
        }
        return player == 1 ? "Red" : "Yellow";
    }

    private class BoardCanvas extends Canvas {
        BoardCanvas() {
            int width = COLUMNS * CELL_SIZE + (PADDING * 2);
            int height = ROWS * CELL_SIZE + (PADDING * 2);
            setPreferredSize(new Dimension(width, height));
            setBackground(new Color(25, 88, 172));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    int column = (event.getX() - PADDING) / CELL_SIZE;
                    handleMove(column);
                }
            });
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics2D.setColor(new Color(25, 88, 172));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    int x = PADDING + col * CELL_SIZE;
                    int y = PADDING + row * CELL_SIZE;
                    int discSize = CELL_SIZE - 12;

                    graphics2D.setColor(Color.WHITE);
                    graphics2D.fillOval(x + 6, y + 6, discSize, discSize);

                    if (winningCells[row][col]) {
                        graphics2D.setColor(new Color(35, 155, 70));
                        graphics2D.fillOval(x + 8, y + 8, discSize - 4, discSize - 4);
                    } else if (board[row][col] == 1) {
                        graphics2D.setColor(new Color(210, 35, 35));
                        graphics2D.fillOval(x + 8, y + 8, discSize - 4, discSize - 4);
                    } else if (board[row][col] == 2) {
                        graphics2D.setColor(new Color(235, 196, 20));
                        graphics2D.fillOval(x + 8, y + 8, discSize - 4, discSize - 4);
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