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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
public class PawnReplacementChoice {

	ChessBoard chessBoard;
	GameController gameController;
	JOptionPane optionPane;
	JDialog dialog;
	Piece pieceCurrentlyHeld;
	Position clickedPosition;

	public PawnReplacementChoice(ChessBoard chessBoard, GameController gameController,
			Piece pieceCurrentlyHeld,Position clickedPosition) {
		this.chessBoard = chessBoard;
		this.gameController = gameController;
		this.pieceCurrentlyHeld = pieceCurrentlyHeld;
		this.clickedPosition = clickedPosition;
	}

	public void replace() {
		JButton[] optionButtons = new JButton[4];
		for (int i = 0; i < 4; i++) {
			String choice = null;
			switch(i) {
			    case 0: choice = "Bishop"; break;
			    case 1: choice = "Knight"; break;
			    case 2: choice = "Queen"; break;
			    case 3: choice = "Rook"; break;
			}
			JButton button = new JButton(choice);
			button.addActionListener(generateActionListener(choice));
			optionButtons[i] = button;
		}
		optionPane = new JOptionPane("Choose a piece to replace the pawn.", JOptionPane.QUESTION_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, optionButtons);

		dialog = new JDialog(chessBoard, true);
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		optionPane.addPropertyChangeListener(
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						if (dialog.isVisible() && (e.getSource() == optionPane)) {
							dialog.setVisible(false);
						}
					}
				});
		dialog.pack();
		dialog.setLocationRelativeTo(chessBoard);
		dialog.setVisible(true);
	}

	private ActionListener generateActionListener(final String choice) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				implementPawnReplacementChoice(choice);
			}
		};
	}

	private void implementPawnReplacementChoice(String choice) {
		if (choice.equals("Queen"))
			pieceCurrentlyHeld = new Queen(pieceCurrentlyHeld.getColour(), clickedPosition);
		else if (choice.equals("Knight"))
			pieceCurrentlyHeld = new Knight(pieceCurrentlyHeld.getColour(), clickedPosition);
		else if (choice.equals("Rook"))
			pieceCurrentlyHeld = new Rook(pieceCurrentlyHeld.getColour(), clickedPosition);
		else if (choice.equals("Bishop"))
			pieceCurrentlyHeld = new Bishop(pieceCurrentlyHeld.getColour(), clickedPosition);
		else
			assert false;
		chessBoard.setPieceAtPosition(clickedPosition, pieceCurrentlyHeld);
		chessBoard.movePiece(pieceCurrentlyHeld, clickedPosition);
		// Call this again to see if the player is in check now that a replacement piece has been selected.
		gameController.determineIfCurrentPlayerIsInCheck();
		optionPane.firePropertyChange("a", false, true);
	}

}