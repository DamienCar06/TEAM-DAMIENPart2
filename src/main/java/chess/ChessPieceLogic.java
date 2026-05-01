package chess;

import java.awt.Point;

public class ChessPieceLogic {
    private static final int BOARD_SIZE = 8;

    private static boolean isValidPieceMove(ChessPiece[][] board, Point from, Point to, ChessPiece piece) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        switch (piece.type) {
            case PAWN:
                return isValidPawnMove(board, from, to, piece.color);
            case ROOK:
                return isValidRookMove(board, from, to);
            case KNIGHT:
                return isValidKnightMove(dx, dy);
            case BISHOP:
                return isValidBishopMove(board, from, to);
            case QUEEN:
                return isValidQueenMove(board, from, to);
            case KING:
                return isValidKingMove(dx, dy);
            default:
                return false;
        }
    }

    private static boolean isValidPawnMove(ChessPiece[][] board, Point from, Point to, PieceColor color) {
        int direction = color == PieceColor.WHITE ? -1 : 1;
        int startRank = color == PieceColor.WHITE ? 6 : 1;
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        
        if(dx == 0 && board[to.y][to.x] == null) {
            if (dy == direction) {
                return true;
            }
            if (from.y == startRank && dy == 2 * direction && board[from.y + direction][from.x] == null) {
                return true;
            }
        }

        if(Math.abs(dx) == 1 && dy == direction && board[to.y][to.x] != null && board[to.y][to.x].color != color) {
            return true;
        }

        return false;
    }

    private static boolean isValidRookMove(ChessPiece[][] board, Point from, Point to) {
        int dx = Math.abs(to.x - from.x);
        int dy = Math.abs(to.y - from.y);

        if (dx != 0 && dy != 0) {
            return false;
        }

        return isPathClear(board, from, to);
    }

    private static boolean isPathClear(ChessPiece[][] board, Point from, Point to) {
        int dx = Integer.compare(to.x - from.x, 0);
        int dy = Integer.compare(to.y - from.y, 0);

        int x = from.x + dx;
        int y = from.y + dy;

        while (x != to.x || y != to.y) {
            if (board[y][x] != null) {
                return false;
            }
            x += dx;
            y += dy;
        }

        return true;
    }

    private static boolean isValidKnightMove(int dx, int dy) {
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        return (absDx == 2 && absDy == 1) || (absDx == 1 && absDy == 2);
    }

    private static boolean isValidBishopMove(ChessPiece[][] board, Point from, Point to) {
        int dx = Math.abs(to.x - from.x);
        int dy = Math.abs(to.y - from.y);

        if (dx != dy) {
            return false;
        }

        return isPathClear(board, from, to);
    }

    private static boolean isValidQueenMove(ChessPiece[][] board, Point from, Point to) {
        int dx = Math.abs(to.x - from.x);
        int dy = Math.abs(to.y - from.y);

        if ((dx == 0 || dy == 0) || (dx == dy)) {
            return isPathClear(board, from, to);
        }

        return false;
    }

    private static boolean isValidKingMove(int dx, int dy) {
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        if (absDx <= 1 && absDy <= 1) {
            return true;
        }

        return false;
    }

    private static boolean isSquareAttacked(ChessPiece[][] board, Point square, PieceColor byColor) {
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                ChessPiece piece = board[rank][file];
                if (piece != null && piece.color == byColor) {
                    Point from = new Point(file, rank);
                    if (isValidPieceMove(board, from, square, piece)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Point findKing(ChessPiece[][] board, PieceColor color) {
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                ChessPiece piece = board[rank][file];
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return new Point(file, rank);
                }
            }
        }
        return null;
    }

    public static boolean isKingInCheck(ChessPiece[][] board, PieceColor kingColor) {
        // Find the king
        Point kingPos = findKing(board, kingColor);
        if (kingPos == null) {
            return false; // Should not happen
        }

        // Check if any enemy piece can attack the king
        PieceColor enemyColor = kingColor.opposite();
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                ChessPiece piece = board[rank][file];
                if (piece != null && piece.color == enemyColor) {
                    Point from = new Point(file, rank);
                    if (isValidPieceMove(board, from, kingPos, piece)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    private static boolean wouldPutKingInCheck(ChessPiece[][] board, Point from, Point to, ChessPiece piece) {
        // Simulate the move
        ChessPiece capturedPiece = board[to.y][to.x];
        board[to.y][to.x] = piece;
        board[from.y][from.x] = null;

        boolean inCheck = isKingInCheck(board, piece.color);

        // Undo the move
        board[from.y][from.x] = piece;
        board[to.y][to.x] = capturedPiece;

        return inCheck;
    }
}
