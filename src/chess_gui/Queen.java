/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess_gui;

/**
 *
 * @author Shobhit
 */
import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

	public Queen(Colour colour, Position position) {
		super(colour, position);
		pieceName = colour.getName() + "Queen";
	}

	@Override
	public List<List<Position>> deriveAllMoves() {
		List<List<Position>> listHolder = new ArrayList<List<Position>>();
		Piece.addStraightTranslations(listHolder, position);
		Piece.addDiagonalTranslations(listHolder, position);
		return listHolder;
	}

}
