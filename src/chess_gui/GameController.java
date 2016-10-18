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
import chess_gui.Piece.Colour;
import chess_gui.Checker.StalemateOption;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;

import javax.swing.SwingUtilities;
public final class GameController {

	private GameControllerStateInfo gcState = new GameControllerStateInfo();
	ChessBoard chessBoard;
	Checker stalemateChecker;
	UndoRedoMove undoRedoMove;
	EndGame endGame;
	Piece pieceCurrentlyHeld;
	List<Position> possibleMoves;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
                                
				GameController gc = new GameController();
                                
				gc.go();
			}
		});
	}

	public GameController() {
		chessBoard = new ChessBoard(this);
		stalemateChecker = new Checker(this);
		undoRedoMove = new UndoRedoMove(this, getChessBoard(), stalemateChecker.getPreviousMoments());
		endGame = new EndGame(chessBoard);
	}

	private void go() {
		chessBoard.initialiseBoard();
	}

	public void reset() {
		pieceCurrentlyHeld = null;
		possibleMoves = null;
		stalemateChecker = new Checker(this);
		endGame = new EndGame(chessBoard);
		gcState = new GameControllerStateInfo();
		undoRedoMove = new UndoRedoMove(this, getChessBoard(), stalemateChecker.getPreviousMoments());
	}

	public void squareClicked(Position clickedPosition) throws CloneNotSupportedException {

		if (endGame.isgameOver())
			return;

		Piece clickedPiece = chessBoard.getPieceAtPosition(clickedPosition);

		if (clickedPiece == null && pieceCurrentlyHeld == null)
			return;
		if (clickedPiece == null && pieceCurrentlyHeld != null) {
			attemptPiecePlacement(clickedPosition);
			return;
		}
		if (clickedPiece != null && pieceCurrentlyHeld == null) {
			assert possibleMoves == null;
			attemptToPickUpPiece(clickedPiece);
			return;
		}
		// Therefore clickedPiece != null, pieceCurrentlyHeld != null
		attemptToCaptureSquare(clickedPosition);
	}

	private void attemptPiecePlacement(Position clickedPosition) throws CloneNotSupportedException {
		assert !possibleMoves.contains(null);

		if (!possibleMoves.contains(clickedPosition))
			return;

		// Piece will now definitely move

		// Register initial position of pieces with the repeat move watcher.
		if (gcState.getMoveNumber() == 0 && undoRedoMove.getHighestMoveNumber() == 0) {
			stalemateChecker.addChessBoardMoment(captureCurrentMoment());
		}

		/*
		  Clone everything. This is necessary because of the undo feature. Undo works by
		  saving the game state after each move. If you click the undo button any number of
		  you will go back to 'pointing' to a previous game state. Without cloning, you
		  will change this saved game state if you click undo any number of times and then
		  make a move.
		 */
		gcState = gcState.clone();
		chessBoard.setChessPieces(chessBoard.getChessPiecesClone());
		pieceCurrentlyHeld = pieceCurrentlyHeld.clone();
		gcState.moveNumber++;

		moveCurrentlyHeldPiece(clickedPosition);

		// If the piece that moved was a pawn or a piece was captured, reset the 50 move counter.
		if (pieceCurrentlyHeld instanceof Pawn ||
				stalemateChecker.getPreviousNumberOfChessPieces() != chessBoard.getNumberOfChessPieces()) {
			stalemateChecker.resetToFiftyMoves();
		}
		else {
			stalemateChecker.decrementRemainingMoveNumber();
		}

		// Update the stalemateChecker with the current number of chess pieces.
		stalemateChecker.setPreviousNumberOfChessPieces(chessBoard.getNumberOfChessPieces());

		nullifyPieceAndPossibleMoves();

		gcState.checkBlockingMoves = null;
		gcState.currentPlayerToMove = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

		// This sets currentPlayerIsInCheck to true or false, and toggles the check flag.
		determineIfCurrentPlayerIsInCheck();

		if (gcState.currentPlayerIsInCheck && isCheckmate()) {
			// isCheckmate() populates gcState.getCheckBlockingMoves()
			endGame.declareWinnerByCheckmate(gcState.getCurrentPlayerToMove() == Colour.WHITE ?
					Colour.BLACK : Colour.WHITE);
		}

		undoRedoMove.setHighestMoveNumber(getMoveNumber());
		ChessBoardMoment currentMoment = captureCurrentMoment();
		stalemateChecker.addChessBoardMoment(currentMoment);

		if (!gcState.currentPlayerIsInCheck)
		    checkForStalemate();
	}

	private void moveCurrentlyHeldPiece(Position clickedPosition) {
		Position positionOfPawnAfterEnPassant = null;
		if (gcState.getEnPassantPosition() != null) {
			positionOfPawnAfterEnPassant = Position.createPosition(gcState.enPassantPosition.getXCoord(),
					(gcState.getCurrentPlayerToMove() == Colour.WHITE) ? gcState.enPassantPosition.getYCoord() + 1 : gcState.enPassantPosition.getYCoord() - 1);
		}
		Position localEnPassantPositionCopy = gcState.enPassantPosition;

		// This is set to null each time because the enPassantPosition only has a lifetime of 1 move.
		// If the opponent does not immediately initiate en passant, the opportunity is lost.
		gcState.enPassantPosition = null;

		if (pieceCurrentlyHeld instanceof King &&
				Math.abs(pieceCurrentlyHeld.getPosition().getXCoord() - clickedPosition.getXCoord()) == 2) {
			performCastling(clickedPosition);
		}
		else {
			if (pieceCurrentlyHeld instanceof Pawn && clickedPosition.equals(positionOfPawnAfterEnPassant)) {
				// Remove the piece from the board including its image
				chessBoard.removePiece(localEnPassantPositionCopy);
			}
			// If the pawn has moved a distance of 2, it is at risk of being taken by en passant
			else if (pieceCurrentlyHeld instanceof Pawn &&
					Math.abs(pieceCurrentlyHeld.getPosition().getYCoord() - clickedPosition.getYCoord()) == 2) {
				gcState.enPassantPosition = clickedPosition;
			}

			resetColoursAfterMove();
			chessBoard.movePiece(pieceCurrentlyHeld, clickedPosition);
			chessBoard.setPieceAtPosition(clickedPosition, pieceCurrentlyHeld);
		}

		pieceCurrentlyHeld.markAsHavingMoved();

        // If the piece that moved is a Pawn now on the final square, the user must choose a replacement piece
		if (pieceCurrentlyHeld instanceof Pawn &&
				clickedPosition.getYCoord() == ((gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 8 : 1)) {
			chessBoard.replacePawnWithUserChoice((Pawn)pieceCurrentlyHeld, clickedPosition);
		}
	}

	private void attemptToPickUpPiece(Piece clickedPiece) {
		if (clickedPiece.getColour() != gcState.currentPlayerToMove)
			return;
		pieceCurrentlyHeld = clickedPiece;
		chessBoard.pieceToChessArraySquare(clickedPiece.getPosition()).setBackground(Color.BLUE);
		this.possibleMoves = getAllowedMovesForPiece(clickedPiece);

		if (gcState.currentPlayerIsInCheck && !(clickedPiece instanceof King)) {
			// Possible moves that block check are those that block check which the piece is
			// physically capable of.
			List<Position> possibleMovesThatBlockCheck = new ArrayList<Position>();
			for (Position checkBlockingPosition : gcState.checkBlockingMoves) {
				if (this.possibleMoves.contains(checkBlockingPosition)) {
					possibleMovesThatBlockCheck.add(checkBlockingPosition);
				}
			}
			this.possibleMoves = possibleMovesThatBlockCheck;


			if (gcState.getCheckBlockingMoves() != null)
				assert !gcState.checkBlockingMoves.contains(null);

			// This is for the very unusual situation that an enemy pawn has moved forward twice to
			// personally threaten the King, but the enemy pawn can be taken by en passant.
			if (gcState.getCheckBlockingMoves() != null && gcState.checkBlockingMoves.contains(gcState.enPassantPosition)) {
				// If the clicked piece is a Pawn adjacent to the en passant position then add the final
				// position as a potential move to free the king from check.
				if (clickedPiece instanceof Pawn &&
						((Pawn)clickedPiece).adjacentToEnPassantPosition(gcState.enPassantPosition)) {
					possibleMoves.add(((Pawn)clickedPiece).finalPositionAfterEnPassant(gcState.enPassantPosition));
				}
			}
		}

		for (Position position : possibleMoves) {
			chessBoard.pieceToChessArraySquare(position).setBackground(Color.GREEN);
		}
	}

	private List<Position> cullIllegalMoves(List<List<Position>> initialPossibleMoves,
			Piece clickedPiece) {
		List<Position> possibleMoves;
            possibleMoves = new ArrayList<Position>();
		List<Position> checkedLine = null;
		// If the piece lies on a checked line, allow it to move anywhere on that checked line
		// that its natural movement allows
		if ((checkedLine = getCheckedLine(clickedPiece)) != null) {
			possibleMoves = addPositionsOnCheckedLine(initialPossibleMoves, checkedLine);
		}
		// else add all moves on lines up to a piece (that piece is included if it's an opposing piece)
		else {
			for (List<Position> moveList : initialPossibleMoves) {
				for (Position proposedPosition : moveList) {
					if (chessBoard.getPieceAtPosition(proposedPosition) != null) {
						if (chessBoard.getPieceAtPosition(proposedPosition).getColour()
								!= gcState.currentPlayerToMove) {
							// Are allowed to take pieces of opposite colour.
							possibleMoves.add(proposedPosition);
						}
						// End of the line
						break;
					}
					// otherwise must be attempting to move into an empty square
					possibleMoves.add(proposedPosition);
				}
			}
		}
		cullSpecialCases(possibleMoves, clickedPiece);
		return possibleMoves;
	}

	public List<Position> getAllowedMovesForPiece(Piece chessPiece) {
		List<Position> possibleMoves = new ArrayList<Position>();
		List<List<Position>> initialPossibleMoves = chessPiece.deriveAllMoves();
		possibleMoves = cullIllegalMoves(initialPossibleMoves, chessPiece);
		addSpecialCases(possibleMoves, chessPiece);
		assert !possibleMoves.contains(null);
		return possibleMoves;
	}

	/*
	  Works out the line between the King and the piece. If there is such a line, it sees if
	  an opposing piece is on the end of it that would threated the King.
	 */
	private List<Position> getCheckedLine(Piece clickedPiece) {
		King ownKing = chessBoard.getKing(gcState.currentPlayerToMove);
		boolean isDiagonalLine = false;
		List<Position> line = calculateSharedOpenLine(ownKing, clickedPiece, isDiagonalLine);
		if (line == null)
			return null;
		for (Position p : line) {
			Piece threateningPiece = chessBoard.getPieceAtPosition(p);
			if (threateningPiece != null && threateningPiece.getColour() != gcState.currentPlayerToMove) {
				if (isDiagonalLine) {
					if (threateningPiece instanceof Bishop || threateningPiece instanceof Queen)
						return line;
				}
				else {
					if (threateningPiece instanceof Rook || threateningPiece instanceof Queen)
						return line;
				}
				return null;
			}
		}
		return null;
	}

	/*
	  Calculates the line starting from the king that passes through the king. The line
	  continues until it hits a piece of either colour. Does not add the King's position
	  to the line.
	 */
	private List<Position> calculateSharedOpenLine(King king, Piece piece, boolean isDiagonalLine) {
		int xDiff = piece.getPosition().getXCoord() - king.getPosition().getXCoord();
		int yDiff = piece.getPosition().getYCoord() - king.getPosition().getYCoord();
		if (!(xDiff == 0 || yDiff == 0 || Math.abs(xDiff) == Math.abs(yDiff)))
			return null;

		int xInc, yInc;

		// If they share the same x or y coordinate, they're not on a diagonal line.
		if (xDiff == 0 || yDiff == 0)
			isDiagonalLine=(false);
		else
			isDiagonalLine=(true);

		if (xDiff > 0)
			xInc = 1;
		else if (xDiff == 0)
			xInc = 0;
		else
			xInc = -1;

		if (yDiff > 0)
			yInc = 1;
		else if (yDiff == 0)
			yInc = 0;
		else
			yInc = -1;

		boolean passedThroughPieceFlag = false;
		List<Position> retLine = new ArrayList<Position>();
		Position positionToAdd = king.getPosition();
		while (true) {
			int xCoord = positionToAdd.getXCoord();
			int yCoord = positionToAdd.getYCoord();
			xCoord += xInc;
			yCoord += yInc;
			positionToAdd = Position.createPosition(xCoord, yCoord);
			if (positionToAdd == null) {
				// Break if the line is now extending off the board
				break;
			}
			retLine.add(positionToAdd);

			// Once the line has passed through the piece, it may carry on until it hits another piece
			if (chessBoard.getPieceAtPosition(positionToAdd) != null) {
				if (passedThroughPieceFlag) {
					break;
				}
				if (chessBoard.getPieceAtPosition(positionToAdd) != piece) {
					// If the first piece on the line is not the piece in question, then the piece
					// does not lie on a checked line.
					return null;
				}
				passedThroughPieceFlag = true;
			}
		}
		return retLine;
	}

	private List<Position> addPositionsOnCheckedLine(List<List<Position>> initialPossibleMoves,
			List<Position> checkedLine) {
		List<Position> possibleMoves = new ArrayList<Position>();
		for (List<Position> positionList : initialPossibleMoves) {
			for (Position position : positionList) {
				if (checkedLine.contains(position)) {
					possibleMoves.add(position);
				}
			}
		}
		return possibleMoves;
	}

	private void cullSpecialCases(List<Position> possibleMoves,
			Piece clickedPiece) {
		Position clickedPiecePosition = clickedPiece.getPosition();
		Set<Position> positionsToDelete = new HashSet<Position>();
		if (clickedPiece instanceof Pawn) {
			// Potentially take away its forward move(s)
			int forwardMove = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 1 : -1;

			Position forwardPosition = Position.createPosition(clickedPiecePosition.getXCoord(),
					clickedPiecePosition.getYCoord() + forwardMove);
			if (chessBoard.getPieceAtPosition(forwardPosition) != null) {
				positionsToDelete.add(forwardPosition);
				// Cannot move forward two spaces if the space ahead is checked.
				positionsToDelete.add(Position.createPosition(clickedPiecePosition.getXCoord(),
						clickedPiecePosition.getYCoord() + 2 * forwardMove));
			}

			// If the first space ahead is free, may still need to cull the second space ahead.
			Position forwardPosition2 = Position.createPosition(clickedPiecePosition.getXCoord(),
					clickedPiecePosition.getYCoord() + 2 * forwardMove);
			if (chessBoard.getPieceAtPosition(forwardPosition2) != null) {
				positionsToDelete.add(forwardPosition2);
			}

			// Potentially take away forward-diagonal moves
			for (int xDisp = -1; xDisp < 2; xDisp += 2) {
				Position forwardDiagonal = Position.createPosition(clickedPiecePosition.getXCoord() + xDisp,
						clickedPiecePosition.getYCoord() + forwardMove);
				Piece potentialVictim = chessBoard.getPieceAtPosition(forwardDiagonal);
				if (potentialVictim == null || potentialVictim.getColour() == gcState.currentPlayerToMove) {
					positionsToDelete.add(forwardDiagonal);
				}
			}
		}

		// Don't allow the King to move into a checked squares (separate from the castle-ing code)
		if (clickedPiece instanceof King) {
			for (Position position : possibleMoves) {
				if (positionIsChecked(position)) {
					positionsToDelete.add(position);
				}
			}
		}

		// There may be a less clunky way of doing this. I added it, because removing the position
		// in the middle of the above loop caused a ConcurrentModificationException.
		for (Position delPosition : positionsToDelete) {
			possibleMoves.remove(delPosition);
		}
	}
        // This is for potentially adding castling and en passant.
	 private void addSpecialCases(List<Position> possibleMoves, Piece clickedPiece) {
		Position clickedPiecePos = clickedPiece.getPosition();
		if (clickedPiece instanceof King) {
			if (clickedPiece.hasMoved()	|| gcState.currentPlayerIsInCheck)
				return;

			Position reqKingPosition = Position.createPosition(King.KING_POS,
					(gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 1 : 8);
			if (!clickedPiecePos.equals(reqKingPosition))
				return;

			for (int direction = -1; direction < 2; direction += 2) {
				Position rookPosition = Position.createPosition((direction == -1) ? 1 : 8,
						clickedPiecePos.getYCoord());
				if (canCastleBetweenPositions(reqKingPosition, rookPosition, direction)) {
				    possibleMoves.add(Position.createPosition(King.KING_POS + 2 * direction,
						clickedPiecePos.getYCoord()));
				}
			}
		}

		if (gcState.getEnPassantPosition() != null && clickedPiece instanceof Pawn) {
			Pawn clickedPawn = (Pawn)clickedPiece;
			// The required y coordinate of the pawn that would perform the en passant.
			int requiredYCoord = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 5 : 4;
			if (clickedPiecePos.getYCoord() != requiredYCoord)
				return;

			if (clickedPawn.adjacentToEnPassantPosition(gcState.enPassantPosition)) {
				possibleMoves.add(clickedPawn.finalPositionAfterEnPassant(gcState.enPassantPosition));
			}
		}
	}

	private void replacePiece(Position clickedPosition) {
		resetColoursAfterMove();
		pieceCurrentlyHeld = null;
		possibleMoves = null;
	}

	private void attemptToCaptureSquare(Position clickedPosition) throws CloneNotSupportedException {
		if (clickedPosition.equals(pieceCurrentlyHeld.getPosition())) {
			replacePiece(clickedPosition);
		    return;
		}
		// If the player has tried to take one of their own pieces, do nothing.
		if (chessBoard.getPieceAtPosition(clickedPosition).getColour() == gcState.currentPlayerToMove) {
			return;
		}
		attemptPiecePlacement(clickedPosition);
	}

	/*
	  A position is also considered to be checked when the king may temporarily be blocking check in that
	  position, but if he moves into that position, he will still be in check.
	 */
	private boolean positionIsChecked(Position position) {
		// Check for threatening knights
		List<Position> knightAttackPositions = Knight.getKnightAttackPositions(position);
		for (Position knightPosition : knightAttackPositions) {
			Piece possibleKnight = chessBoard.getPieceAtPosition(knightPosition);
			if (possibleKnight == null)
				continue;
			if (possibleKnight instanceof Knight && possibleKnight.getColour() != gcState.currentPlayerToMove)
				return true;
		}

		// Check for threatening Bishops, Rooks, Queens, Kings and Pawns.
		List<List<Position>> threateningLines = getThreateningLines(position);
		if (threateningLines.size() > 0)
			return true;
		return false;
	}

	/* This method will also get called when deciding if it's a checkmate.
	  If the king can't move anywhere without being in check, this method will get called.
	  If there's more than one threatening line, it's checkmate. If there's just one
	  threatening line, each piece on the board will have to be called to see if it can
	  move onto the line (the line being squares including the attacking piece's position,
	  excluding the position that the king is on). If none can, it's checkmate.
	  A threatening line includes a pawn that's attacking the king.
	 */
	private List<List<Position>> getThreateningLines(Position position) {
		int x = 0, y = 0;
		List<List<Position>> listHolder = new ArrayList<List<Position>>();
		for (int xDisp = -1; xDisp < 2; xDisp++) {
			for (int yDisp = -1; yDisp < 2; yDisp++) {
				List<Position> line = new ArrayList<Position>();
				if (xDisp == 0 && yDisp == 0) {
					// No line for zero x and zero y displacement.
					continue;
				}
				x = position.getXCoord() + xDisp;
				y = position.getYCoord() + yDisp;
				for (;; x += xDisp, y += yDisp) {
					Position nextPosition = Position.createPosition(x, y);
					if (nextPosition == null) {
						// line has gone off the board without finding a threatening piece.
						break;
					}
					Piece threateningPiece = chessBoard.getPieceAtPosition(nextPosition);

					/* The second part of this if statement was added as an alternative to the hack of making
					 the king invisible. In short, we don't want the friendly King to be seen as blocking
					 check at a position. Because if he is, then he will be allowed to move into it, but
					  he would still be in check following that move.
					 
					  An example is if a Rook has put the king in check. Without
					  temporarily making the King invisible here, the program would allow the king to move sideways
					  yet he would still be in check following this move.
					 */
					if (threateningPiece == null ||
							(threateningPiece.getColour() == gcState.getCurrentPlayerToMove() && threateningPiece instanceof King)) {
						// continue following the line
						line.add(nextPosition);
						continue;
					}

					if (threateningPiece.getColour() == gcState.currentPlayerToMove) {
						// End of line is a friendly piece
						break;
					}

					// If a vertical/horizontal line
					if (xDisp == 0 || yDisp == 0) {
						if (threateningPiece instanceof Rook || threateningPiece instanceof Queen
								|| (threateningPiece instanceof King && (Math.abs(position.getXCoord() - x) == 1
										                                 || Math.abs(position.getYCoord() - y) == 1))) {
							line.add(nextPosition);
							listHolder.add(line);
						}
					}
					else {
						// must be a diagonal line
						if (threateningPiece instanceof Bishop || threateningPiece instanceof Queen
								|| (threateningPiece instanceof King && (Math.abs(position.getXCoord() - x) == 1
										                                 && Math.abs(position.getYCoord() - y) == 1))
								|| threateningPiece instanceof Pawn && isThreateningPawn((Pawn)threateningPiece, position)) {
							line.add(nextPosition);
							listHolder.add(line);
						}
					}
					break;
				}
			}
		}

		return listHolder;
	}

	/*
	  Short piece of logic called from getThreateningLines. Have already established that the
	  pawn is the opposite colour to the current player.
	 */
	private boolean isThreateningPawn(
			Pawn pawn, Position position) {
		Position pawnPosition = pawn.getPosition();
		int xDiff = pawnPosition.getXCoord() - position.getXCoord();
		int yDiff = pawnPosition.getYCoord() - position.getYCoord();
		// If the pawn is not directly diagonal, it can't be threatening.
		if (Math.abs(xDiff) != 1 || Math.abs(yDiff) != 1) {
			return false;
		}

		if (pawn.getColour() == Colour.BLACK && yDiff == 1
				|| pawn.getColour() == Colour.WHITE && yDiff == -1) {
			return true;
		}
		return false;
	}

	private void resetColoursAfterMove() {
		chessBoard.resetBoardSquareColour(pieceCurrentlyHeld.getPosition());
		for (Position position : possibleMoves) {
			chessBoard.resetBoardSquareColour(position);
		}
	}

	public void determineIfCurrentPlayerIsInCheck() {
		King currentPlayersKing = chessBoard.getKing(gcState.currentPlayerToMove);
		gcState.currentPlayerIsInCheck = positionIsChecked(currentPlayersKing.getPosition());
		chessBoard.toggleCheckLabel(gcState.currentPlayerIsInCheck);
	}

	private void performCastling(Position clickedPosition) {
		Position rookPosition = Position.createPosition((clickedPosition.getXCoord() == 7) ? 8 : 1,
				clickedPosition.getYCoord());
		Piece rookToCastle = chessBoard.getPieceAtPosition(rookPosition);
		Position rookDestination = Position.createPosition((rookPosition.getXCoord() == 1) ? 4 : 6,
				rookPosition.getYCoord());
		assert (rookToCastle instanceof Rook);
		// King will be set as having moved when the method returns
		rookToCastle.markAsHavingMoved();
		resetColoursAfterMove();
		chessBoard.movePiece(pieceCurrentlyHeld, clickedPosition);
		chessBoard.movePiece(rookToCastle, rookDestination);
	}

	private void checkForStalemate() {
		StalemateOption stalemateOption = stalemateChecker.isStalemate();
		if (stalemateOption == StalemateOption.MANDATORY_PLAYER_CANT_MOVE
				|| stalemateOption == StalemateOption.MANDATORY_TOO_FEW_PIECES)
			endGame.declareMandatoryStalemate(stalemateOption, gcState.currentPlayerToMove);
		else if (stalemateOption == StalemateOption.OPTIONAL_THREE_FOLD
				|| stalemateOption == StalemateOption.OPTIONAL_FIFTY_MOVE)
			endGame.informThatPlayerMayDeclareStalemate(stalemateOption,
					gcState.getCurrentPlayerToMove() == Colour.WHITE ? Colour.BLACK : Colour.WHITE);
	}

	private boolean isCheckmate() {
		King kingInCheck = chessBoard.getKing(gcState.currentPlayerToMove);
		List<List<Position>> threateningLines = getThreateningLines(kingInCheck.getPosition());
		List<Position> possibleKingMoves = getAllowedMovesForPiece(kingInCheck);

		// Definite checkmate
		if (threateningLines.size() > 1 && possibleKingMoves.size() == 0)
			return true;

		gcState.checkBlockingMoves = new ArrayList<Position>();
		if (threateningLines.size() == 1) {
			for (Position checkBlockingPosition : threateningLines.get(0)) {
				gcState.checkBlockingMoves.add(checkBlockingPosition);
			}
		}

		// Add potential knight threats which are not covered by threatening lines.
		List<Position> knightAttackPositions = Knight.getKnightAttackPositions(kingInCheck.getPosition());
		for (Position knightPosition : knightAttackPositions) {
			Piece possibleKnight = chessBoard.getPieceAtPosition(knightPosition);
			if (possibleKnight == null)
				continue;
			if (possibleKnight instanceof Knight && possibleKnight.getColour() != gcState.currentPlayerToMove)
				gcState.checkBlockingMoves.add(knightPosition);
		}

		// Can return at this point if the king himself can move out of check
		if (possibleKingMoves.size() > 0)
			return false;

		// Otherwise, may still be checkmate. Need to see if any of the pieces can move onto one of
		// the checkBlockingPosition's
		List<Piece> currentPlayersPieces = chessBoard.getPlayersPieces(gcState.currentPlayerToMove);
		for (Piece chessPiece : currentPlayersPieces) {
			if (chessPiece instanceof King)
				// King cannot block the check of himself.
				continue;
			List<Position> allowedMoves = getAllowedMovesForPiece(chessPiece);

			for (Position checkBlockingMove : gcState.checkBlockingMoves) {
				if (allowedMoves.contains(checkBlockingMove)) {
					// It's possible for the check to be blocked, so not checkmate.
					return false;
				}
			}
		}
		
		// Final check to see if attacking pawn can be taken by en passant
		if (gcState.checkBlockingMoves.size() == 1 && gcState.checkBlockingMoves.get(0).equals(gcState.enPassantPosition)) {
			for (int i = -1; i < 2; i += 2) {
				Position potentialFriendlyPawnPosition = Position.createPosition(gcState.enPassantPosition.getXCoord() + i,
						gcState.enPassantPosition.getYCoord());
				Piece friendlyPawn = chessBoard.getPieceAtPosition(potentialFriendlyPawnPosition);
				if (!(friendlyPawn instanceof Pawn))
					continue;
				List<Position> pawnAllowedMoves = getAllowedMovesForPiece(friendlyPawn);
				if (pawnAllowedMoves.contains(((Pawn)friendlyPawn).finalPositionAfterEnPassant(gcState.enPassantPosition)))
					return false;
			}
		}

		// No moves can block the threatening line, so checkmate.
		return true;
	}

	public ChessBoard getChessBoard() {
		return chessBoard;
	}

	public Colour getCurrentPlayerToMove() {
		return gcState.currentPlayerToMove;
	}

	public ChessBoardMoment captureCurrentMoment() {
		Map<Position, Piece> chessPieces = chessBoard.getChessPiecesClone();
		CastlingOpportunities castlingOpportunities = constructCastlingOpportunities();
		CastlingPiecesMovementTracker castlingPiecesMovementTracker
		= constructCastlingPiecesMovementTracker();
		GameControllerStateInfo clonedGCState = new GameControllerStateInfo(
				gcState.currentPlayerToMove,
				// Need to clone the things that aren't immutable.
				duplicateArrayList(gcState.checkBlockingMoves),
				gcState.currentPlayerIsInCheck,
				gcState.enPassantPosition,
				gcState.moveNumber);

		return new ChessBoardMoment(chessPieces, castlingOpportunities,
				castlingPiecesMovementTracker, clonedGCState);
	}

	private CastlingOpportunities constructCastlingOpportunities() {
		Boolean[] castlingOpportunitiesArray = new Boolean[4];
		for (int i = 0; i < 4; i++) {
			castlingOpportunitiesArray[i] = false;
		}
		int counter = 0;

		// Temporarily change gcState.getCurrentPlayerToMove() back to what it was
		gcState.currentPlayerToMove = (gcState.currentPlayerToMove == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

		for (int yCoord = 1; yCoord <= 8; yCoord += 7) {
			// gcState.getCurrentPlayerToMove() has already been changed to be the opposite colour
			if (gcState.getCurrentPlayerToMove() == Colour.WHITE && yCoord == 8
			|| gcState.getCurrentPlayerToMove() == Colour.BLACK && yCoord == 1) {
				castlingOpportunitiesArray[counter] = null;
				counter++;
				castlingOpportunitiesArray[counter] = null;
				counter++;
				continue;
			}
			Position reqKingPosition = Position.createPosition(King.KING_POS, yCoord);
			Piece supposedKing = chessBoard.getPieceAtPosition(reqKingPosition);
			if (!(supposedKing instanceof King)
					|| (supposedKing != null && supposedKing.hasMoved())) {
				counter += 2;
				continue;
			}
			for (int direction = -1; direction < 2; direction += 2, counter++) {
				Position rookPosition = Position.createPosition((direction == -1) ? 1 : 8,
						yCoord);
				if (canCastleBetweenPositions(reqKingPosition, rookPosition, direction)) {
					castlingOpportunitiesArray[counter] = true;
				}
			}
		}

		// Put it back to what it was before
		gcState.currentPlayerToMove = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

		return new CastlingOpportunities(castlingOpportunitiesArray[0], castlingOpportunitiesArray[1], castlingOpportunitiesArray[3], castlingOpportunitiesArray[2]);
	}

	private boolean canCastleBetweenPositions(Position kingPosition, Position rookPosition, int direction) {
		Piece potentialRook = chessBoard.getPieceAtPosition(rookPosition);
		// Can't castle with a Rook that's already moved.
		if (potentialRook == null || !(potentialRook instanceof Rook) || potentialRook.hasMoved())
			return false;
		// Check that the two squares adjacent to the King are empty and unchecked
		for (int i = 1; i < 3; i++) {
			Position nextSquare = Position.createPosition(King.KING_POS + i * direction,
					rookPosition.getYCoord());
			if (chessBoard.getPieceAtPosition(nextSquare) != null || positionIsChecked(nextSquare))
				return false;
		}
		// Positions (2, 1) and (2, 8) must be empty but may be checked.
		if (direction == -1 &&
				chessBoard.getPieceAtPosition(Position.createPosition(2, rookPosition.getYCoord())) != null) {
			return false;
		}
		return true;
	}

	private CastlingPiecesMovementTracker constructCastlingPiecesMovementTracker() {
		boolean[] inputs = new boolean[6];
		int counter = 0;
		for (int yCoord = 1; yCoord <= 8; yCoord += 7) {
			for (int xCoord = 1; xCoord != 12; xCoord += 4, counter++) {
				// Really I just want to do for xCoord in 1 5 8
				if (xCoord == 9)
					xCoord = 8;
				String requiredPiece = (xCoord == 5) ? "King" : "Rook";
				Position position = Position.createPosition(xCoord, yCoord);
				Piece pieceAtPosition = chessBoard.getPieceAtPosition(position);
				if (pieceAtPosition != null && pieceAtPosition.getName().contains(requiredPiece)
						&& !pieceAtPosition.hasMoved())
					inputs[counter] = true;
			}
		}
		return new CastlingPiecesMovementTracker(inputs);
	}
	private List<Position> duplicateArrayList(List<Position> listToDuplicate) {
		List<Position> retList = new ArrayList<Position>();
		if (listToDuplicate == null)
			return retList;
		for (Position entry : listToDuplicate) {
			retList.add(entry);
		}
		return retList;
	}

	public void nullifyPieceAndPossibleMoves() {
		pieceCurrentlyHeld = null;
		possibleMoves = null;
	}

	public Checker getChecker() {
		return stalemateChecker;
	}

	public GameControllerStateInfo getGcState() {
		return gcState;
	}

	public void setGcState(GameControllerStateInfo gcState) {
		this.gcState = gcState;
	}

	public int getMoveNumber() {
		return gcState.moveNumber;
	}

	public void undo() {
		undoRedoMove.undo();
	}

	public void redo() {
		undoRedoMove.redo();
	}

	public int getHighestRecordedMoveNumber() {
		return undoRedoMove.getHighestMoveNumber();
	}

	public Piece getPieceCurrentlyHeld() {
		return pieceCurrentlyHeld;
	}

	public void setPieceCurrentlyHeld(Piece pieceCurrentlyHeld) {
		this.pieceCurrentlyHeld = pieceCurrentlyHeld;
	}

	public List<Position> getPossibleMoves() {
		return possibleMoves;
	}

	public void setPossibleMoves(List<Position> possibleMoves) {
		this.possibleMoves = possibleMoves;
	}

}

