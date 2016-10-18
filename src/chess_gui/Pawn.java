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



public class Pawn extends Piece {

	public Pawn(Colour colour, Position position) {
		super(colour, position);
		pieceName = colour.getName() + "Pawn";
	}

	@Override
	public List<List<Position>> deriveAllMoves() {
		List<List<Position>> listHolder = new ArrayList<List<Position>>();
		List<Position> moveList1 = new ArrayList<Position>();
		Piece.addMove(moveList1, position, 0,
				(colour == Colour.WHITE) ? 1 : -1);

		if (position.getYCoord() == 2 && colour == Colour.WHITE) {
			Piece.addMove(moveList1, position, 0, 2);
		}
		else if (position.getYCoord() == 7 && colour == Colour.BLACK) {
			Piece.addMove(moveList1, position, 0, -2);
		}

		if (moveList1.size() > 0)
			listHolder.add(moveList1);

		// Add the positions the pawn can move into when capturing pieces
		List<Position> moveList2 = new ArrayList<Position>();
		Piece.addMove(moveList2, position, 1,
				(colour == Colour.WHITE) ? 1 : -1);
		if (moveList2.size() > 0)
			listHolder.add(moveList2);

		List<Position> moveList3 = new ArrayList<Position>();
		Piece.addMove(moveList3, position, -1,
				(colour == Colour.WHITE) ? 1 : -1);
		if (moveList3.size() > 0)
			listHolder.add(moveList3);

		return listHolder;
	}

	public boolean adjacentToEnPassantPosition(Position enPassantPosition) {
		// Needs to have the same y coordinate, and a difference of 1 between the x coordinates
		return (position.getYCoord() == enPassantPosition.getYCoord())
		         && (Math.abs(position.getXCoord() - enPassantPosition.getXCoord()) == 1);
	}

	public Position finalPositionAfterEnPassant(Position enPassantPosition) {
		int xCoord = enPassantPosition.getXCoord();
		int yCoord = enPassantPosition.getYCoord() + ((colour == Colour.WHITE) ? 1 : -1);
		return Position.createPosition(xCoord, yCoord);
	}

}