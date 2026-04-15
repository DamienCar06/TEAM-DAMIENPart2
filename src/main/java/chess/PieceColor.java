package chess;

/**
 * Enumeration of chess piece colors.
 */
public enum PieceColor {
    WHITE, BLACK;

    /**
     * Returns the opposite color.
     *
     * @return the opposite color
     */
    public PieceColor opposite() {
        if (this == WHITE) {
            return BLACK;
        }
        return WHITE;
    }
}        
