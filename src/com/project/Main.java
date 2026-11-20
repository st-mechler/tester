package com.project;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.LinearGradientPaint;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public class Main extends java.awt.Frame {
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CELL_SIZE = 86;
    private static final int PADDING = 12;

    private enum Language {
        DE,
        EN
    }

    private enum GameMode {
        TWO_PLAYER,
        COMPUTER
    }

    @FunctionalInterface
    private interface SoundEffect extends AutoCloseable {
        void play();

        @Override
        default void close() {
        }
    }

    private static final class DropSound implements SoundEffect {
        private final Clip clip;

        DropSound() {
            Clip preparedClip = null;
            AudioInputStream audioStream = null;
            try {
                int sampleRate = 22_050;
                int frameCount = sampleRate / 8;
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                byte[] audioData = new byte[frameCount * 2];
                for (int frame = 0; frame < frameCount; frame++) {
                    double progress = (double) frame / frameCount;
                    double frequency = 620 - (360 * progress);
                    double envelope = (1 - progress) * (1 - progress);
                    short sample = (short) (Math.sin(2 * Math.PI * frequency * frame / sampleRate)
                            * envelope * 15_000);
                    audioData[frame * 2] = (byte) sample;
                    audioData[frame * 2 + 1] = (byte) (sample >> 8);
                }

                audioStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, frameCount);
                preparedClip = AudioSystem.getClip();
                preparedClip.open(audioStream);
            } catch (LineUnavailableException | IOException | RuntimeException exception) {
                if (preparedClip != null) {
                    preparedClip.close();
                    preparedClip = null;
                }
            } finally {
                if (audioStream != null) {
                    try {
                        audioStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            clip = preparedClip;
        }

        @Override
        public synchronized void play() {
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        }

        @Override
        public synchronized void close() {
            if (clip != null) {
                clip.close();
            }
        }
    }

    private final int[][] board = new int[ROWS][COLUMNS];
    private final boolean[][] winningCells = new boolean[ROWS][COLUMNS];
    private final SoundEffect dropSound = new DropSound();
    private int currentPlayer = 1;
    private int winnerPlayer = 0;
    private boolean gameOver = false;
    private Language currentLanguage = Language.DE;
    private GameMode gameMode = GameMode.TWO_PLAYER;

    private final Label statusLabel = new Label();
    private final Button resetButton = new Button();
    private final Button languageButton = new Button();
    private final BoardCanvas boardCanvas = new BoardCanvas();

    public Main() {
        super("Vier-Gewinnt Pilot (AWT)");

        setLayout(new BorderLayout());
        setBackground(new Color(7, 12, 35));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        statusLabel.setForeground(new Color(169, 230, 255));
        statusLabel.setBackground(new Color(7, 12, 35));
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        resetButton.setBackground(new Color(24, 93, 154));
        resetButton.setForeground(Color.WHITE);
        languageButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        languageButton.setBackground(new Color(24, 93, 154));
        languageButton.setForeground(Color.WHITE);

        resetButton.addActionListener(e -> resetGame());
        languageButton.addActionListener(e -> switchLanguage());

        Panel actionPanel = new Panel(new BorderLayout());
        actionPanel.setBackground(new Color(7, 12, 35));
        actionPanel.add(languageButton, BorderLayout.WEST);
        actionPanel.add(resetButton, BorderLayout.EAST);

        Panel bottomPanel = new Panel(new BorderLayout());
        bottomPanel.setBackground(new Color(7, 12, 35));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        add(boardCanvas, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dropSound.close();
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
        dropSound.play();

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
            setBackground(new Color(5, 9, 29));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (event.getX() < PADDING || event.getX() >= PADDING + COLUMNS * CELL_SIZE
                            || event.getY() < PADDING || event.getY() >= PADDING + ROWS * CELL_SIZE) {
                        return;
                    }
                    int column = (event.getX() - PADDING) / CELL_SIZE;
                    handleMove(column);
                }
            });
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setPaint(new GradientPaint(0, 0, new Color(5, 9, 29),
                    getWidth(), getHeight(), new Color(18, 30, 75)));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
            drawStarfield(graphics2D);

            int boardWidth = COLUMNS * CELL_SIZE;
            int boardHeight = ROWS * CELL_SIZE;
            graphics2D.setColor(new Color(0, 0, 0, 110));
            graphics2D.fillRoundRect(PADDING + 5, PADDING + 8, boardWidth, boardHeight, 30, 30);
            graphics2D.setPaint(new GradientPaint(PADDING, PADDING, new Color(52, 164, 255),
                    PADDING + boardWidth, PADDING + boardHeight, new Color(24, 55, 142)));
            graphics2D.fillRoundRect(PADDING, PADDING, boardWidth, boardHeight, 28, 28);
            graphics2D.setColor(new Color(131, 223, 255, 190));
            graphics2D.drawRoundRect(PADDING + 1, PADDING + 1, boardWidth - 3, boardHeight - 3, 26, 26);
            graphics2D.setColor(new Color(8, 24, 79, 190));
            graphics2D.drawRoundRect(PADDING + 5, PADDING + 5, boardWidth - 11, boardHeight - 11, 22, 22);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    int x = PADDING + col * CELL_SIZE;
                    int y = PADDING + row * CELL_SIZE;
                    drawSlot(graphics2D, x, y);
                    if (board[row][col] != 0) {
                        drawDisc(graphics2D, x, y, board[row][col], winningCells[row][col]);
                    }
                }
            }
            graphics2D.dispose();
        }

        private void drawStarfield(Graphics2D graphics2D) {
            for (int star = 0; star < 64; star++) {
                int x = (star * 97 + 31) % Math.max(1, getWidth());
                int y = (star * 193 + 17) % Math.max(1, getHeight());
                int brightness = 90 + (star * 37 % 150);
                graphics2D.setColor(new Color(150, 210, 255, brightness));
                graphics2D.fillOval(x, y, star % 9 == 0 ? 3 : 2, star % 9 == 0 ? 3 : 2);
            }
        }

        private void drawSlot(Graphics2D graphics2D, int x, int y) {
            int size = CELL_SIZE - 18;
            graphics2D.setColor(new Color(7, 21, 65, 180));
            graphics2D.fillOval(x + 8, y + 10, size + 2, size + 2);
            graphics2D.setPaint(new GradientPaint(x + 12, y + 12, new Color(9, 22, 66),
                    x + size + 14, y + size + 14, new Color(27, 62, 132)));
            graphics2D.fillOval(x + 9, y + 9, size, size);
            graphics2D.setColor(new Color(109, 196, 255, 95));
            graphics2D.drawOval(x + 9, y + 9, size, size);
        }

        private void drawDisc(Graphics2D graphics2D, int x, int y, int player, boolean winning) {
            int size = CELL_SIZE - 28;
            int discX = x + (CELL_SIZE - size) / 2;
            int discY = y + (CELL_SIZE - size) / 2;
            graphics2D.setColor(new Color(0, 0, 0, 100));
            graphics2D.fillOval(discX + 3, discY + 5, size, size);

            Color highlight = winning ? new Color(194, 255, 208)
                    : player == 1 ? new Color(255, 150, 143) : new Color(255, 247, 151);
            Color base = winning ? new Color(25, 153, 93)
                    : player == 1 ? new Color(198, 29, 61) : new Color(238, 166, 25);
            Color shadow = winning ? new Color(7, 91, 63)
                    : player == 1 ? new Color(105, 17, 49) : new Color(164, 77, 12);
            graphics2D.setPaint(new LinearGradientPaint(discX, discY, discX + size, discY + size,
                    new float[] {0.0f, 0.48f, 1.0f}, new Color[] {highlight, base, shadow}));
            graphics2D.fillOval(discX, discY, size, size);
            graphics2D.setColor(new Color(255, 255, 255, 135));
            graphics2D.fillOval(discX + size / 5, discY + size / 7, size / 3, size / 6);
            graphics2D.setColor(new Color(255, 255, 255, 115));
            graphics2D.drawOval(discX, discY, size, size);
        }
    }

    public static void main(String[] args) {
        Main game = new Main();
        game.chooseGameMode();
        game.resetGame();
        game.setVisible(true);
    }
}