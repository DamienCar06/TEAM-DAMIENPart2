package chess;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JLabel;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBoard(g2);
        drawPieces(g2);
        //drawDragPiece(g2);
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
}
