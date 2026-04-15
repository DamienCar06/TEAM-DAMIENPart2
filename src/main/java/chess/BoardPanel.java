package chess;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 *  A GUI panel representing the chess board.
 */
public class BoardPanel extends JPanel {
    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 80;
    private static final Color LIGHT_COLOR = Color.WHITE;
    private static final Color DARK_COLOR = Color.BLACK;
    private static final Color SELECTED_COLOR = new Color(186, 202, 68, 180);
    private static final Color LAST_MOVE_COLOR = new Color(246, 246, 105, 180);

    private final ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    private final JLabel turnLabel = new JLabel();
    private final JTextArea historyText = new JTextArea();
    private final JLabel notificationLabel = new JLabel(" ");
    private final Stack<GameState> moveHistory = new Stack<>();
    private final List<ChessPiece> whiteCaptured = new ArrayList<>();
    private final List<ChessPiece> blackCaptured = new ArrayList<>();
    private final JTextArea capturedText = new JTextArea();

    private Point selectedSquare;
    private Point lastFrom;
    private Point lastTo;
    private ChessPiece dragPiece;
    private Point dragLocation;
    private PieceColor currentTurn = PieceColor.WHITE;

    /**
     * Constructs a new chess board panel.
     */
    public BoardPanel() {
        setPreferredSize(new Dimension(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        initBoard();
        configureMouseControls();
        updateTurnLabel();
    }

    private void initBoard() {
        clearBoard();
        currentTurn = PieceColor.WHITE;
        selectedSquare = null;
        lastFrom = null;
        lastTo = null;
        notificationLabel.setText("Click a piece to move.");

        PieceType[] backRank = {
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
        };

        for (int file = 0; file < BOARD_SIZE; file++) {
            board[0][file] = new ChessPiece(backRank[file], PieceColor.BLACK);
            board[1][file] = new ChessPiece(PieceType.PAWN, PieceColor.BLACK);
            board[6][file] = new ChessPiece(PieceType.PAWN, PieceColor.WHITE);
            board[7][file] = new ChessPiece(backRank[file], PieceColor.WHITE);
        }
    }

    private void clearBoard() {
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                board[rank][file] = null;
            }
        }
    }

    private void configureMouseControls() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point square = pointToSquare(e.getPoint());
                if (!squareInBounds(square)) {
                    return;
                }
                ChessPiece piece = board[square.y][square.x];
                if (piece == null || piece.color != currentTurn) {
                    return;
                }
                selectedSquare = square;
                dragPiece = piece;
                board[square.y][square.x] = null;
                dragLocation = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragPiece == null) {
                    return;
                }
                Point target = pointToSquare(e.getPoint());
                if (!squareInBounds(target)) {
                    board[selectedSquare.y][selectedSquare.x] = dragPiece;
                    dragPiece = null;
                    repaint();
                    return;
                }
                if (target.equals(selectedSquare)) {
                    board[selectedSquare.y][selectedSquare.x] = dragPiece;
                } else {
                    executeMove(selectedSquare, target, dragPiece);
                }
                dragPiece = null;
                dragLocation = null;
                selectedSquare = null;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (dragPiece != null) {
                    return;
                }
                Point square = pointToSquare(e.getPoint());
                if (!squareInBounds(square)) {
                    return;
                }
                if (selectedSquare == null) {
                    ChessPiece piece = board[square.y][square.x];
                    if (piece != null && piece.color == currentTurn) {
                        selectedSquare = square;
                        notificationLabel.setText("Piece selected. Click destination square.");
                        repaint();
                    }
                } else {
                    if (selectedSquare.equals(square)) {
                        selectedSquare = null;
                        notificationLabel.setText("Click a piece to move.");
                        repaint();
                        return;
                    }
                    ChessPiece piece = board[selectedSquare.y][selectedSquare.x];
                    if (piece != null) {
                        executeMove(selectedSquare, square, piece);
                    }
                    selectedSquare = null;
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragPiece != null) {
                    dragLocation = e.getPoint();
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private void executeMove(Point from, Point to, ChessPiece movingPiece) {
        ChessPiece targetPiece = board[to.y][to.x];
        Move move = new Move(from, to, movingPiece, targetPiece);
        GameState state = new GameState(board, move, currentTurn);
        moveHistory.push(state);

        String moveNotation = formatMoveNotation(movingPiece, from, to, targetPiece != null);
        if (targetPiece != null) {
            notificationLabel.setText(movingPiece.getName() + " captures " + targetPiece.getName() + ".");
            if (targetPiece.color == PieceColor.WHITE) {
                whiteCaptured.add(targetPiece);
            } else {
                blackCaptured.add(targetPiece);
            }
            updateCapturedDisplay();
        } else {
            notificationLabel.setText(movingPiece.getName() + " moved.");
        }

        board[from.y][from.x] = null;
        board[to.y][to.x] = movingPiece;
        lastFrom = new Point(from);
        lastTo = new Point(to);
        appendHistory(moveNotation);

        if (targetPiece != null && targetPiece.type == PieceType.KING) {
            String winner = movingPiece.color == PieceColor.WHITE ? "White" : "Black";
            JOptionPane.showMessageDialog(this, winner + " wins by capturing the king!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        currentTurn = currentTurn.opposite();
        updateTurnLabel();
    }

    private String formatMoveNotation(ChessPiece piece, Point from, Point to, boolean capture) {
        String colFrom = String.valueOf((char) ('a' + from.x));
        String rowFrom = String.valueOf(8 - from.y);
        String colTo = String.valueOf((char) ('a' + to.x));
        String rowTo = String.valueOf(8 - to.y);
        return (piece.color == PieceColor.WHITE ? "W" : "B") + " " + piece.type.shorthand + ": " + colFrom + rowFrom + (capture ? "x" : "-") + colTo + rowTo;
    }

    private void updateTurnLabel() {
        turnLabel.setText((currentTurn == PieceColor.WHITE ? "White" : "Black") + " to move");
    }

    private void appendHistory(String text) {
        historyText.append(text + "\n");
        historyText.setCaretPosition(historyText.getDocument().getLength());
    }

    private boolean squareInBounds(Point square) {
        return square.x >= 0 && square.x < BOARD_SIZE && square.y >= 0 && square.y < BOARD_SIZE;
    }
    
    private Point pointToSquare(Point point) {
        int file = point.x / TILE_SIZE;
        int rank = point.y / TILE_SIZE;
        return new Point(file, rank);
    }

    /**
     * Creates the side panel containing game information.
     * Includes turn indicator, move history, captured pieces display, and control buttons.
     * @return the side panel
     */
    public JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        sidePanel.setPreferredSize(new Dimension(300, BOARD_SIZE * TILE_SIZE));

        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 16f));
        notificationLabel.setFont(notificationLabel.getFont().deriveFont(Font.PLAIN, 14f));
        notificationLabel.setForeground(Color.DARK_GRAY);

        historyText.setEditable(false);
        historyText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        historyText.setLineWrap(true);
        historyText.setWrapStyleWord(true);

        capturedText.setEditable(false);
        capturedText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        capturedText.setLineWrap(true);
        capturedText.setWrapStyleWord(true);

        JScrollPane historyScroll = new JScrollPane(historyText);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Move History"));

        JScrollPane capturedScroll = new JScrollPane(capturedText);
        capturedScroll.setBorder(BorderFactory.createTitledBorder("Captured Pieces"));
        capturedScroll.setPreferredSize(new Dimension(280, 120));

        JButton resetButton = new JButton("Reset Game");
        resetButton.addActionListener(e -> resetGame());

        JButton undoButton = new JButton("Undo Move");
        undoButton.addActionListener(e -> undoMove());

        JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        buttonPanel.add(undoButton, BorderLayout.WEST);
        buttonPanel.add(resetButton, BorderLayout.EAST);

        sidePanel.add(turnLabel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(historyScroll, BorderLayout.CENTER);
        centerPanel.add(capturedScroll, BorderLayout.SOUTH);
        sidePanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(notificationLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        sidePanel.add(bottomPanel, BorderLayout.SOUTH);

        return sidePanel;
    }

    /**
     * Saves the current game state to a file.
     *
     * @param filePath saved game file
     * @return true if the save was successful, false otherwise
     */
    public boolean saveGame(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("CHESS_GAME_STATE\n");
            writer.write("BOARD_STATE\n");

            for (int rank = 0; rank < BOARD_SIZE; rank++) {
                StringBuilder line = new StringBuilder();
                for (int file = 0; file < BOARD_SIZE; file++) {
                    ChessPiece piece = board[rank][file];
                    if (piece == null) {
                        line.append(".");
                    } else {
                        char colorChar = piece.color == PieceColor.WHITE ? 'W' : 'B';
                        line.append(colorChar).append(piece.type.shorthand);
                    }
                    if (file < BOARD_SIZE - 1) line.append(",");
                }
                writer.write(line.toString());
                writer.newLine();
            }

            writer.write("CURRENT_TURN\n");
            writer.write(currentTurn == PieceColor.WHITE ? "W" : "B");
            writer.newLine();

            writer.write("MOVE_HISTORY\n");
            writer.write(historyText.getText());
            writer.newLine();

            writer.write("END_GAME_STATE\n");

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a saved game state from a file.
     *
     * @param filePath saved game file
     * @return true if the load was successful, false otherwise
     */
    public boolean loadGame(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            line = reader.readLine();
            if (line == null || !line.equals("CHESS_GAME_STATE")) {
                return false;
            }

            line = reader.readLine();
            if (line == null || !line.equals("BOARD_STATE")) {
                return false;
            }

            clearBoard();

            for (int rank = 0; rank < BOARD_SIZE; rank++) {
                line = reader.readLine();
                if (line == null) {
                    return false;
                }
                String[] squares = line.split(",");
                if (squares.length != BOARD_SIZE) {
                    return false;
                }

                for (int file = 0; file < BOARD_SIZE; file++) {
                    String square = squares[file].trim();
                    if (!square.equals(".")) {
                        char colorChar = square.charAt(0);
                        String typeStr = square.substring(1);
                        PieceColor color = colorChar == 'W' ? PieceColor.WHITE : PieceColor.BLACK;
                        PieceType type = shorthandToPieceType(typeStr);
                        if (type != null) {
                            board[rank][file] = new ChessPiece(type, color);
                        }
                    }
                }
            }

            line = reader.readLine();
            if (line == null || !line.equals("CURRENT_TURN")) {
                return false;
            }

            line = reader.readLine();
            if (line == null) {
                return false;
            }
            currentTurn = line.trim().equals("W") ? PieceColor.WHITE : PieceColor.BLACK;

            line = reader.readLine();
            if (line == null || !line.equals("MOVE_HISTORY")) {
                return false;
            }

            StringBuilder historyBuilder = new StringBuilder();
            line = reader.readLine();
            while (line != null && !line.equals("END_GAME_STATE")) {
                historyBuilder.append(line).append("\n");
                line = reader.readLine();
            }

            selectedSquare = null;
            lastFrom = null;
            lastTo = null;
            dragPiece = null;
            dragLocation = null;

            historyText.setText(historyBuilder.toString());
            notificationLabel.setText("Game loaded.");
            updateTurnLabel();
            repaint();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Resets the game to its initial state.
     */
    public void resetGame() {
        initBoard();
        historyText.setText("");
        notificationLabel.setText("New game started.");
        repaint();
    }

    private static PieceType shorthandToPieceType(String shorthand) {
        switch (shorthand) {
            case "K":
                return PieceType.KING;
            case "Q":
                return PieceType.QUEEN;
            case "R":
                return PieceType.ROOK;
            case "B":
                return PieceType.BISHOP;
            case "N":
                return PieceType.KNIGHT;
            case "P":
                return PieceType.PAWN;
            default:
                return null;
        }
    }

    /**
     * Represents a chess move with its start and end point with the piece moved.
     */
    private static class Move {
        final Point from;
        final Point to;
        final ChessPiece piece;
        final ChessPiece captured;

        /**
         * Constructs a new Move record.
         *
         * @param from the starting square
         * @param to the ending square
         * @param piece the piece being moved
         * @param captured the piece being captured, or null
         */
        Move(Point from, Point to, ChessPiece piece, ChessPiece captured) {
            this.from = new Point(from);
            this.to = new Point(to);
            this.piece = piece;
            this.captured = captured;
        }
    }

    /**
     * Represents the complete state of the game at a specific point, allowing to undo moves.
     */
    private static class GameState {
        final ChessPiece[][] boardState;
        final Move move;
        final PieceColor turn;

        /**
         * Constructs a new GameState.
         *
         * @param board current board configuration
         * @param move move made
         * @param turn current player's turn
         */
        GameState(ChessPiece[][] board, Move move, PieceColor turn) {
            this.boardState = deepCopyBoard(board);
            this.move = move;
            this.turn = turn;
        }

        private static ChessPiece[][] deepCopyBoard(ChessPiece[][] original) {
            ChessPiece[][] copy = new ChessPiece[original.length][original[0].length];
            for (int i = 0; i < original.length; i++) {
                for (int j = 0; j < original[i].length; j++) {
                    copy[i][j] = original[i][j];
                }
            }
            return copy;
        }
    }

    private void updateCapturedDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("White Captured:\n");
        for (ChessPiece piece : blackCaptured) {
            sb.append(piece.type.shorthand).append(" ");
        }
        sb.append("\n\nBlack Captured:\n");
        for (ChessPiece piece : whiteCaptured) {
            sb.append(piece.type.shorthand).append(" ");
        }
        capturedText.setText(sb.toString());
    }

    private void undoMove() {
        if (moveHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No moves to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        GameState previousState = moveHistory.pop();
        Move lastMove = previousState.move;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = previousState.boardState[i][j];
            }
        }

        if (lastMove.captured != null) {
            if (lastMove.captured.color == PieceColor.WHITE) {
                whiteCaptured.remove(lastMove.captured);
            } else {
                blackCaptured.remove(lastMove.captured);
            }
            updateCapturedDisplay();
        }

        currentTurn = previousState.turn;
        lastFrom = null;
        lastTo = null;
        selectedSquare = null;
        dragPiece = null;
        dragLocation = null;

        historyText.setText("");
        for (GameState state : moveHistory) {
            appendHistory(formatMoveNotation(state.move.piece, state.move.from, state.move.to, state.move.captured != null));
        }

        notificationLabel.setText("Move undone.");
        updateTurnLabel();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBoard(g2);
        drawPieces(g2);
        drawDragPiece(g2);
        g2.dispose();
    }

    private void drawBoard(Graphics2D g2) {
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                boolean lightSquare = (rank + file) % 2 == 0;
                g2.setColor(lightSquare ? LIGHT_COLOR : DARK_COLOR);
                g2.fillRect(file * TILE_SIZE, rank * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        if (lastFrom != null && lastTo != null) {
            g2.setColor(LAST_MOVE_COLOR);
            g2.fillRect(lastFrom.x * TILE_SIZE, lastFrom.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g2.fillRect(lastTo.x * TILE_SIZE, lastTo.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        if (selectedSquare != null) {
            g2.setColor(SELECTED_COLOR);
            g2.fillRect(selectedSquare.x * TILE_SIZE, selectedSquare.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        g2.setColor(Color.BLACK);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int file = 0; file < BOARD_SIZE; file++) {
            String letter = String.valueOf((char) ('a' + file));
            g2.drawString(letter, file * TILE_SIZE + 4, BOARD_SIZE * TILE_SIZE - 4);
        }
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            String number = String.valueOf(8 - rank);
            g2.drawString(number, 4, rank * TILE_SIZE + 14);
        }
    }

    private void drawPieces(Graphics2D g2) {
        g2.setFont(new Font(Font.SERIF, Font.PLAIN, 42));
        FontMetrics metrics = g2.getFontMetrics();

        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                ChessPiece piece = board[rank][file];
                if (piece != null) {
                    int x = file * TILE_SIZE + (TILE_SIZE - metrics.stringWidth(piece.getGlyph())) / 2;
                    int y = rank * TILE_SIZE + ((TILE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
                    boolean lightSquare = (rank + file) % 2 == 0;
                    g2.setColor(lightSquare ? Color.BLACK : Color.WHITE);
                    g2.drawString(piece.getGlyph(), x, y);
                }
            }
        }
    }

    private void drawDragPiece(Graphics2D g2) {
        if (dragPiece == null || dragLocation == null) {
            return;
        }
        int x = dragLocation.x - TILE_SIZE / 2;
        int y = dragLocation.y - TILE_SIZE / 2;
        g2.setFont(new Font(Font.SERIF, Font.PLAIN, 42));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        Point square = pointToSquare(dragLocation);
        boolean lightSquare = (square.y + square.x) % 2 == 0;
        g2.setColor(lightSquare ? Color.BLACK : Color.WHITE);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(dragPiece.getGlyph(), x + (TILE_SIZE - metrics.stringWidth(dragPiece.getGlyph())) / 2, y + ((TILE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent());
    }
}
