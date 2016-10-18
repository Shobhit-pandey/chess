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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
public class EndGame {

	private JFrame chessBoard;
	private boolean gameOver = false;
	private static final String WHITE_CHECKMATE = "White has checkmated Black.";
	private static final String BLACK_CHECKMATE = "Black has checkmated White.";
	private static final String PLAYER_CANT_MOVE_STALEMATE = " has no moves available, so the game ends in stalemate.";
	private static final String TOO_FEW_PIECES_STALEMATE = "Neither player has the pieces required to achieve checkmate, "
		+ "so the game ends in stalemate.";
	private static final String THREE_FOLD_MSG_START = "This exact board position, with the same moves "
		+ "available, has occurred three times in a row. ";
	private static final String FIFTY_MOVE_MSG_START = "Fifty moves have taken place without any pieces "
		+ "being taken, or a pawn being moved. ";
	private static final String OPTIONAL_MSG_END = " may declare stalemate.";

	public EndGame(JFrame chessBoard) {
		super();
		this.chessBoard = chessBoard;
	}

	public boolean isgameOver() {
		return gameOver;
	}

	public void declareWinnerByCheckmate(Colour winner) {
		gameOver = true;
		JOptionPane.showMessageDialog(chessBoard,
        winner == Colour.WHITE ? WHITE_CHECKMATE : BLACK_CHECKMATE);
	}

	public void declareMandatoryStalemate(StalemateOption stalemateOption, Colour currentPlayerToMove) {
		gameOver = true;
		String message = null;
		if (stalemateOption == StalemateOption.MANDATORY_PLAYER_CANT_MOVE)
			message = currentPlayerToMove.getName() + PLAYER_CANT_MOVE_STALEMATE;
		else
			message = TOO_FEW_PIECES_STALEMATE;
		JOptionPane.showMessageDialog(chessBoard, message);
	}

	public void informThatPlayerMayDeclareStalemate(StalemateOption stalemateOption, Colour playerToChoose) {
		String message = null;
		if (stalemateOption == StalemateOption.OPTIONAL_THREE_FOLD)
			message = THREE_FOLD_MSG_START + playerToChoose.getName() + OPTIONAL_MSG_END;
		else
			message = FIFTY_MOVE_MSG_START + playerToChoose.getName() + OPTIONAL_MSG_END;
        Object[] options = {"Declare stalemate", "Continue"};
        int n = JOptionPane.showOptionDialog(chessBoard,
        		        message,
                        "Stalemate",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
        if (n == JOptionPane.YES_OPTION) {
        	gameOver = true;
        }
	}

}