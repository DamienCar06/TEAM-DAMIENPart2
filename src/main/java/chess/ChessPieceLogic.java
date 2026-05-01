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
}
