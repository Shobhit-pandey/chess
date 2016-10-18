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
public class King extends Piece {

	public static final int KING_POS = 5;

	public King(Colour colour, Position position) {
		super(colour, position);
		pieceName = colour.getName() + "King";
	}

	@Override
	public List<List<Position>> deriveAllMoves() {
		List<List<Position>> listHolder = new ArrayList<List<Position>>();
		int[] xCoords = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
		int[] yCoords = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
		for (int i = 0; i < xCoords.length; i++) {
			List<Position> moveList = new ArrayList<Position>();
			Piece.addMove(moveList, position, xCoords[i], yCoords[i]);
			if (moveList.size() > 0)
				listHolder.add(moveList);
		}
		return listHolder;
	}

}

