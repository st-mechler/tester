package com.project;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main extends java.awt.Frame {
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CELL_SIZE = 86;
    private static final int PADDING = 12;
    private static final String START_SCREEN = "start";
    private static final String GAME_SCREEN = "game";

    private enum Language {
        DE,
        EN
    }

    private enum GameMode {
        TWO_PLAYER,
        COMPUTER
    }

    private final int[][] board = new int[ROWS][COLUMNS];
    private final boolean[][] winningCells = new boolean[ROWS][COLUMNS];
    private int currentPlayer = 1;
    private int winnerPlayer = 0;
    private boolean gameOver = false;
    private Language currentLanguage = Language.DE;
    private GameMode gameMode = GameMode.TWO_PLAYER;

    private final Label statusLabel = new Label();
    private final Button resetButton = new Button();
    private final Button languageButton = new Button();
    private final Button startButton = new Button();
    private final Button startLanguageButton = new Button();
    private final BoardCanvas boardCanvas = new BoardCanvas();
    private final StartScreenCanvas startScreenCanvas = new StartScreenCanvas();
    private final CardLayout screenLayout = new CardLayout();
    private final Panel screenPanel = new Panel(screenLayout);

    public Main() {
        super("Vier-Gewinnt Pilot (AWT)");

        setLayout(new BorderLayout());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        resetButton.addActionListener(e -> resetGame());
        languageButton.addActionListener(e -> switchLanguage());
        startLanguageButton.addActionListener(e -> switchLanguage());
        startButton.addActionListener(e -> {
            chooseGameMode();
            resetGame();
            screenLayout.show(screenPanel, GAME_SCREEN);
        });

        Panel actionPanel = new Panel(new BorderLayout());
        actionPanel.add(languageButton, BorderLayout.WEST);
        actionPanel.add(resetButton, BorderLayout.EAST);

        Panel bottomPanel = new Panel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        Panel gamePanel = new Panel(new BorderLayout());
        gamePanel.add(boardCanvas, BorderLayout.CENTER);
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);

        Panel startPanel = new Panel(new BorderLayout());
        startPanel.add(startScreenCanvas, BorderLayout.CENTER);
        Panel startActions = new Panel();
        startActions.add(startLanguageButton);
        startActions.add(startButton);
        startPanel.add(startActions, BorderLayout.SOUTH);

        screenPanel.add(startPanel, START_SCREEN);
        screenPanel.add(gamePanel, GAME_SCREEN);
        add(screenPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
                System.exit(0);
            }
        });

        applyLanguage();

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void chooseGameMode() {
        Dialog modeDialog = new Dialog(this, currentLanguage == Language.DE ? "Spielmodus waehlen" : "Choose game mode", true);
        modeDialog.setLayout(new BorderLayout(12, 12));
        modeDialog.add(new Label(currentLanguage == Language.DE ? "Wie moechtest du spielen?" : "How would you like to play?", Label.CENTER),
                BorderLayout.CENTER);

        Panel modePanel = new Panel();
        Button twoPlayerButton = new Button(currentLanguage == Language.DE ? "2 Spieler" : "2 Players");
        Button computerButton = new Button(currentLanguage == Language.DE ? "Computer" : "Computer");
        twoPlayerButton.addActionListener(event -> {
            gameMode = GameMode.TWO_PLAYER;
            modeDialog.dispose();
        });
        computerButton.addActionListener(event -> {
            gameMode = GameMode.COMPUTER;
            modeDialog.dispose();
        });
        modePanel.add(twoPlayerButton);
        modePanel.add(computerButton);
        modeDialog.add(modePanel, BorderLayout.SOUTH);
        modeDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                modeDialog.dispose();
            }
        });

        modeDialog.pack();
        modeDialog.setResizable(false);
        modeDialog.setLocationRelativeTo(this);
        modeDialog.setVisible(true);
    }

    private void switchLanguage() {
        currentLanguage = currentLanguage == Language.DE ? Language.EN : Language.DE;
        applyLanguage();
        updateStatusLabel();
        startScreenCanvas.repaint();
    }

    private void applyLanguage() {
        setTitle(currentLanguage == Language.DE ? "Vier-Gewinnt Pilot (AWT)" : "Connect Four Pilot (AWT)");
        resetButton.setLabel(currentLanguage == Language.DE ? "Neues Spiel" : "New game");
        languageButton.setLabel(currentLanguage == Language.DE ? "EN" : "DE");
        startButton.setLabel(currentLanguage == Language.DE ? "Spiel starten" : "Start game");
        startLanguageButton.setLabel(currentLanguage == Language.DE ? "EN" : "DE");
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
            statusLabel.setText(currentLanguage == Language.DE ? "Spalte voll. Bitte waehle eine andere Spalte." : "Column is full. Please choose another column.");
            return;
        }

        playMove(column);
        if (!gameOver && gameMode == GameMode.COMPUTER && currentPlayer == 2) {
            playMove(chooseComputerMove());
        }
    }

    private void playMove(int column) {
        int row = getAvailableRow(column);
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
        if (gameOver && winnerPlayer != 0) {
            showWinnerPopup();
        }
    }

    private int chooseComputerMove() {
        int[] preferredColumns = {3, 2, 4, 1, 5, 0, 6};
        for (int column : preferredColumns) {
            if (wouldWin(column, 2)) {
                return column;
            }
        }
        for (int column : preferredColumns) {
            if (wouldWin(column, 1)) {
                return column;
            }
        }
        for (int column : preferredColumns) {
            if (getAvailableRow(column) >= 0) {
                return column;
            }
        }
        return -1;
    }

    private boolean wouldWin(int column, int player) {
        int row = getAvailableRow(column);
        if (row < 0) {
            return false;
        }

        board[row][column] = player;
        boolean wins = countConnected(row, column, 1, 0, player) >= 4
                || countConnected(row, column, 0, 1, player) >= 4
                || countConnected(row, column, 1, 1, player) >= 4
                || countConnected(row, column, 1, -1, player) >= 4;
        board[row][column] = 0;
        return wins;
    }

    private void showWinnerPopup() {
        String playerName = getPlayerName(winnerPlayer);
        Dialog winnerDialog = new Dialog(this, currentLanguage == Language.DE ? "Spielende" : "Game over", true);
        winnerDialog.setLayout(new BorderLayout(12, 12));
        winnerDialog.add(new Label(currentLanguage == Language.DE
                ? "Spieler " + playerName + " hat gewonnen!"
                : "Player " + playerName + " wins!"), BorderLayout.CENTER);

        Button closeButton = new Button("OK");
        closeButton.addActionListener(event -> winnerDialog.dispose());
        winnerDialog.add(closeButton, BorderLayout.SOUTH);
        winnerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                winnerDialog.dispose();
            }
        });

        winnerDialog.pack();
        winnerDialog.setLocationRelativeTo(this);
        winnerDialog.setVisible(true);
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
        if (player == 2 && gameMode == GameMode.COMPUTER) {
            return "Computer";
        }
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

    private class StartScreenCanvas extends Canvas {
        private final Color[] balloonColors = {
                new Color(236, 83, 91),
                new Color(255, 177, 66),
                new Color(83, 177, 220),
                new Color(157, 111, 205),
                new Color(246, 111, 151)
        };

        StartScreenCanvas() {
            setPreferredSize(new Dimension(COLUMNS * CELL_SIZE + (PADDING * 2),
                    ROWS * CELL_SIZE + (PADDING * 2)));
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setPaint(new GradientPaint(0, 0, new Color(255, 240, 207),
                    0, getHeight(), new Color(145, 216, 226)));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());

            graphics2D.setColor(new Color(255, 255, 255, 150));
            graphics2D.fillOval(getWidth() / 2 - 100, getHeight() - 115, 320, 150);
            graphics2D.fillOval(-90, getHeight() - 95, 300, 140);
            graphics2D.fillOval(getWidth() - 180, getHeight() - 105, 300, 150);

            int[] positions = {15, 32, 50, 68, 85};
            int[] heights = {200, 260, 185, 245, 205};
            for (int i = 0; i < positions.length; i++) {
                int balloonX = getWidth() * positions[i] / 100 - 30;
                int balloonY = heights[i];
                graphics2D.setColor(new Color(75, 96, 116, 150));
                graphics2D.drawLine(balloonX + 30, balloonY + 68, balloonX + 38, getHeight() - 65);
                graphics2D.setColor(balloonColors[i]);
                graphics2D.fillOval(balloonX, balloonY, 60, 76);
                graphics2D.fillPolygon(new int[] {balloonX + 25, balloonX + 35, balloonX + 30},
                        new int[] {balloonY + 70, balloonY + 70, balloonY + 82}, 3);
                graphics2D.setColor(new Color(255, 255, 255, 120));
                graphics2D.fillOval(balloonX + 12, balloonY + 12, 12, 25);
            }

            graphics2D.setColor(new Color(46, 69, 92));
            graphics2D.setFont(new Font("SansSerif", Font.BOLD, 38));
            String title = currentLanguage == Language.DE ? "Vier gewinnt!" : "Connect Four!";
            int titleWidth = graphics2D.getFontMetrics().stringWidth(title);
            graphics2D.drawString(title, (getWidth() - titleWidth) / 2, 105);

            graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 20));
            String subtitle = currentLanguage == Language.DE
                    ? "Bereit fuer eine Runde?" : "Ready for a round?";
            int subtitleWidth = graphics2D.getFontMetrics().stringWidth(subtitle);
            graphics2D.drawString(subtitle, (getWidth() - subtitleWidth) / 2, 142);
        }
    }

    public static void main(String[] args) {
        Main game = new Main();
        game.setVisible(true);
    }
}