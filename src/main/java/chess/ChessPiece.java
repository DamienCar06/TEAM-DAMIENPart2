package chess;

/**
 * Represents a chess piece with a specific type and color.
 */
public class ChessPiece {
    final PieceType type;
    final PieceColor color;

    /**
     * Constructs a new chess piece.
     *
     * @param type the type of the piece
     * @param color the color of the piece
     */
    public ChessPiece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }

    /**
     * Returns the Unicode glyph for this piece based on its color.
     *
     * @return the Unicode character representing this piece
     */
    public String getGlyph() {
        if (color == PieceColor.WHITE) {
            return type.whiteGlyph;
        }
        return type.blackGlyph;
    }

    /**
     * Returns name for this piece.
     *
     * @return a string of the piece and color
     */
    public String getName() {
        if (color == PieceColor.WHITE) {
            return "White " + type.name();
        } 
        return "Black " + type.name();
    }
}
