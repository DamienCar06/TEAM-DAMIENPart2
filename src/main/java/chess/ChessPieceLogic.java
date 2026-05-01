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
}
