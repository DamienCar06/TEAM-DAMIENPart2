package chess;

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

    PieceType(String shorthand, String whiteGlyph, String blackGlyph) {
        this.shorthand = shorthand;
        this.whiteGlyph = whiteGlyph;
        this.blackGlyph = blackGlyph;
    }
}
