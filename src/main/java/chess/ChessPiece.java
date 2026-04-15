package chess;

public class ChessPiece {
    final PieceType type;
    final PieceColor color;

    public ChessPiece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }

    public String getGlyph() {
        if (color == PieceColor.WHITE) {
            return type.whiteGlyph;
        }
        return type.blackGlyph;
    }

    public String getName() {
        if (color == PieceColor.WHITE) {
            return "White " + type.name();
        } 
        return "Black " + type.name();
    }
}
