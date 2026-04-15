package chess;

/**
 * Enumeration of chess piece types.
 */
public enum PieceType {
    KING("K", "♔", "♚"),
    QUEEN("Q", "♕", "♛"),
    ROOK("R", "♖", "♜"),
    BISHOP("B", "♗", "♝"),
    KNIGHT("N", "♘", "♞"),
    PAWN("P", "♙", "♟");

    final String shorthand;
    final String whiteGlyph;
    final String blackGlyph;

    /**
     * Constructs a PieceType with the given shorthand and glyphs.
     *
     * @param shorthand the shorthand notation
     * @param whiteGlyph the Unicode glyph for white pieces
     * @param blackGlyph the Unicode glyph for black pieces
     */
    PieceType(String shorthand, String whiteGlyph, String blackGlyph) {
        this.shorthand = shorthand;
        this.whiteGlyph = whiteGlyph;
        this.blackGlyph = blackGlyph;
    }
}
