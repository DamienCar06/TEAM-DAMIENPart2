package chess;

import java.awt.AlphaComposite;
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

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

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

    private Point selectedSquare;
    private Point lastFrom;
    private Point lastTo;
    private ChessPiece dragPiece;
    private Point dragLocation;
    private PieceColor currentTurn = PieceColor.WHITE;

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
        if (targetPiece != null && targetPiece.color == movingPiece.color) {
            notificationLabel.setText("Cannot capture your own piece.");
            board[from.y][from.x] = movingPiece;
            return;
        }

        String moveNotation = formatMoveNotation(movingPiece, from, to, targetPiece != null);
        if (targetPiece != null) {
            notificationLabel.setText(movingPiece.getName() + " captures " + targetPiece.getName() + ".");
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
