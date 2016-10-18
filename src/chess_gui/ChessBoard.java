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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
@SuppressWarnings("serial")
public class ChessBoard extends JFrame{
	
	private static final int CHESSBOARD_WIDTH = 8;
	private static final int CHESSBOARD_LENGTH = 8;

	private GameController gc;
	private ChessBoard chessBoard;
	private JPanel contentPanel = new JPanel();
	private JPanel gridJPanel = new JPanel();
	private JToolBar soleJToolBar = new JToolBar();
	private JButton newGameButton = new JButton("New game");
        private JLabel shobhit =new JLabel("Shobhit Pandey");
       //label.setText("shobhit");
	private JButton undoButton = new JButton("Undo");
	private JButton redoButton = new JButton("Redo");
	private JLabel checkNotifier = new JLabel("CHECK  !!!");
	private JLabel[][] chessSquareArray = new JLabel[CHESSBOARD_LENGTH][CHESSBOARD_WIDTH];

	private Map<Position, Piece> chessPieces;

	public ChessBoard(final GameController gc) {
		this.gc = gc;
		chessBoard = this;
		setSize(600, 600);
		setContentPane(contentPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("chess by SHOBHIT PANDEY");

		contentPanel.setLayout(new BorderLayout());
		initializeGridJPanel();
		contentPanel.add(gridJPanel, BorderLayout.CENTER);
		initializeSoleJToolBar();
		contentPanel.add(soleJToolBar, BorderLayout.NORTH);

		newGameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				NewGameChoice sole = new NewGameChoice(chessBoard);
				sole.show();
			}
		});

		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				gc.undo();
				redoButton.setEnabled(true);
				if (gc.getMoveNumber() == 0) {
					undoButton.setEnabled(false);
				}
			}
		});

		redoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				gc.redo();
				undoButton.setEnabled(true);
				if (gc.getMoveNumber() == gc.getHighestRecordedMoveNumber()) {
					redoButton.setEnabled(false);
				}
			}
		});
		
		initializeChessSquareArray();

		setVisible(true);
	}

	public void reset() {
		gc.reset();
		clearJLabels();
		resetAllBoardSquareColours();
		initialiseBoard();
		toggleCheckLabel(false);
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
	}

	public void resetAllBoardSquareColours() {
		for (int i = 0; i < CHESSBOARD_LENGTH; i++) {
			for (int j = 0; j < CHESSBOARD_WIDTH; j++) {
				JLabel square = chessSquareArray[i][j];
				if (square.getName().charAt(0) == 'g')
					square.setBackground(Color.RED);
			    else
			    	square.setBackground(Color.WHITE);
			}
		}
	}

	private void initializeGridJPanel() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(CHESSBOARD_LENGTH);
        gridLayout.setColumns(CHESSBOARD_WIDTH);
        gridJPanel.setLayout(gridLayout);
	}

	private void initializeSoleJToolBar() {
        soleJToolBar.setOrientation(JToolBar.HORIZONTAL);
        soleJToolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        soleJToolBar.setFloatable(false);
        soleJToolBar.add(newGameButton);
        soleJToolBar.add(new JToolBar.Separator());
        soleJToolBar.add(undoButton);
        undoButton.setEnabled(false);
        soleJToolBar.add(new JToolBar.Separator());
        soleJToolBar.add(redoButton);
        redoButton.setEnabled(false);
        soleJToolBar.add(new JToolBar.Separator());
        checkNotifier.setForeground(Color.RED);
        checkNotifier.setVisible(false);
        soleJToolBar.add(checkNotifier);
	}

	private void initializeChessSquareArray() {
		boolean bool1 = false, bool2 = false;
		for (int i = 0; i < CHESSBOARD_LENGTH; i++) {
			for (int j = 0; j < CHESSBOARD_WIDTH; j++) {
				chessSquareArray[i][j] = new JLabel((Icon)null, JLabel.CENTER);
				chessSquareArray[i][j].setOpaque(true);
				//chessSquareArray[i][j] = new JLabel("[" + i + "][" + j + "]");
				if (bool1 ^ bool2) {
					chessSquareArray[i][j].setBackground(Color.RED);
					chessSquareArray[i][j].setName("gray" + i + j);
				}
				else {
					chessSquareArray[i][j].setBackground(Color.WHITE);
					chessSquareArray[i][j].setName("white" + i + j);
				}
				gridJPanel.add(chessSquareArray[i][j]);
				bool2 = (bool2 == true) ? false : true;

				chessSquareArray[i][j].addMouseListener(new MouseAdapter() {
//					@Override
//					public void mouseEntered(MouseEvent mouseEvent) {
//						mouseEvent.getComponent().setBackground(Color.GREEN);
//					}

//					@Override
//					public void mouseExited(MouseEvent mouseEvent) {
//						Component currentJLabel = (JLabel) mouseEvent.getComponent();
//						if (currentJLabel.getName().charAt(0) == 'g')
//							currentJLabel.setBackground(Color.GRAY);
//						else
//							currentJLabel.setBackground(Color.WHITE);
//					}

					@Override
					public void mouseReleased(MouseEvent mouseEvent) {
						Component currentJLabel = (JLabel) mouseEvent.getComponent();
						String labelName = currentJLabel.getName();
						Position clickedPosition = arrayToBoard(labelName.charAt(labelName.length() - 2) - '0',
								labelName.charAt(labelName.length() - 1) - '0');
                                            try {
                                                gc.squareClicked(clickedPosition);
                                            } catch (CloneNotSupportedException ex) {
                                                Logger.getLogger(ChessBoard.class.getName()).log(Level.SEVERE, null, ex);
                                            }
					}
                });
			}
			bool1 = (bool1 == true) ? false : true;
		}
	}

	public void initialiseBoard() {
		chessPieces = new HashMap<Position, Piece>();
                 shobhit.setForeground(Color.BLUE);
        shobhit.setVisible(true);
        soleJToolBar.add(shobhit);
		addInitialSixteenPieces();
		Set<Position> positionSet = chessPieces.keySet();
		for (Position position : positionSet) {
			Piece cp = chessPieces.get(position);
			paintBoardSquare(cp.getName(), position);
		}
	}

	/*
	 * Called from GameController when the new game button is clicked.
	 */
	public void clearJLabels() {
		Set<Position> piecePositionSet = chessPieces.keySet();
		for (Position piecePosition : piecePositionSet)
		    pieceToChessArraySquare(piecePosition).setIcon(null);
	}

	private void addInitialSixteenPieces() {
		Colour colour = null;
		int xCoord = 0, yCoord = 0;

		for (int i = 1; i <= 2; i++) {
			colour = (i == 1) ? Colour.WHITE : Colour.BLACK;
			yCoord = (i == 1) ? 2 : 7;
			for (xCoord = 1; xCoord <= CHESSBOARD_WIDTH; xCoord++) {
				Position position = Position.createPosition(xCoord, yCoord);
				setPieceAtPosition(position, Piece.createChessPiece("Pawn", colour, position));
			}
		}

		for (int i = 1; i <= 2; i++) {
			colour = (i == 1) ? Colour.WHITE : Colour.BLACK;
			yCoord = (i == 1) ? 1 : 8;
			for (xCoord = 1; xCoord <= CHESSBOARD_WIDTH; xCoord++) {
				Position position = Position.createPosition(xCoord, yCoord);
				switch (xCoord) {
				    case 1: case 8:
				    	setPieceAtPosition(position, Piece.createChessPiece("Rook", colour, position));
				    	break;
				    case 2: case 7:
				    	setPieceAtPosition(position, Piece.createChessPiece("Knight", colour, position));
				    	break;
				    case 3: case 6:
				    	setPieceAtPosition(position, Piece.createChessPiece("Bishop", colour, position));
				    	break;
				    case 4:
				    	setPieceAtPosition(position, Piece.createChessPiece("Queen", colour, position));
				    	break;
				    case 5:
				    	setPieceAtPosition(position, Piece.createChessPiece("King", colour, position));
				    	break;
				}
			}
		}
	}

	private void paintBoardSquare(String pieceName, Position position) {
        InputStream inIcon = ClassLoader.getSystemResourceAsStream(pieceName+".gif");
        assert inIcon != null : "inIcon should not be null.";
        BufferedImage imgIcon = null;

		try {
			imgIcon = ImageIO.read(inIcon);
		} catch (Exception e) {
			System.out.println("Error: Could not locate \"" + pieceName + ".gif\" in the current folder.");
			assert false;
		}


    		pieceToChessArraySquare(position).setIcon(new ImageIcon(imgIcon));
	}

	public Piece getPieceAtPosition(Position position) {
		return chessPieces.get(position);
	}

	public void setPieceAtPosition(Position position, Piece newPiece) {
		assert position != null;
		assert newPiece != null;
		assert position.equals(newPiece.getPosition()): "position = " + position
		+ ", and newPiece.getPosition() = " + newPiece.getPosition();
		chessPieces.put(position, newPiece);
	}

	public static Colour getColourOfSquareAtPosition(Position position) {
		int xCoord = position.getXCoord();
		int yCoord = position.getYCoord();
		if (Math.abs(xCoord - yCoord) % 2 == 0)
			return Colour.BLACK;
		else
			return Colour.WHITE;
	}

	private Position arrayToBoard(int xCoord, int yCoord) {
		xCoord = 7 - xCoord;
		xCoord++;
		yCoord++;
		int z = xCoord;
		xCoord = yCoord;
		yCoord = z;
		return Position.createPosition(xCoord, yCoord);
	}

	public JLabel pieceToChessArraySquare(Position position) {
		int z = position.getXCoord();
		int xCoord = position.getYCoord();
		int yCoord = z;
		xCoord--;
		yCoord--;
		xCoord = 7 - xCoord;
		return chessSquareArray[xCoord][yCoord];
	}

	public King getKing(Colour currentPlayerToMove) {
		Set<Position> chessPieceSet = chessPieces.keySet();
		for (Position position : chessPieceSet) {
			Piece chessPiece = chessPieces.get(position);
			if (chessPiece instanceof King && chessPiece.getColour() == currentPlayerToMove)
				return (King)chessPiece;
		}
		assert false : "There should always be a king of either colour";
		return null;
	}

	public void resetBoardSquareColour(Position position) {
		JLabel square = pieceToChessArraySquare(position);
		if (square.getName().charAt(0) == 'g')
			square.setBackground(Color.RED);
	    else
	    	square.setBackground(Color.WHITE);
	}

	public void movePiece(Piece pieceCurrentlyHeld,
			Position clickedPosition) {
		removePiece(pieceCurrentlyHeld.getPosition());
		pieceCurrentlyHeld.setPosition(clickedPosition);
		addPiece(pieceCurrentlyHeld);
		undoButton.setEnabled(true);
		redoButton.setEnabled(false);
	}

	public void removePiece(Position piecePosition) {
		pieceToChessArraySquare(piecePosition).setIcon(null);
		chessPieces.remove(piecePosition);
	}

	public void addPiece(Piece pieceToAdd) {
		setPieceAtPosition(pieceToAdd.getPosition(), pieceToAdd);
		paintBoardSquare(pieceToAdd.getName(), pieceToAdd.getPosition());
	}

	public void toggleCheckLabel(boolean flag) {
		checkNotifier.setVisible(flag);
	}

	public List<Piece> getPlayersPieces(Colour currentPlayerToMove) {
        List<Piece> currentPlayersPieces = new ArrayList<Piece>();

		Set<Position> keySet = chessPieces.keySet();
		for (Position position : keySet) {
			assert position != null;
			Piece fetchedPiece = chessPieces.get(position);
			assert fetchedPiece != null;
			if (fetchedPiece.getColour() == currentPlayerToMove)
				currentPlayersPieces.add(fetchedPiece);
		}
		return currentPlayersPieces;
	}

	public void replacePawnWithUserChoice(Pawn pieceCurrentlyHeld,
			Position clickedPosition) {
		PawnReplacementChoice sole = new PawnReplacementChoice(chessBoard, gc, pieceCurrentlyHeld, clickedPosition);
		sole.replace();
	}

	public int getNumberOfChessPieces() {
		return chessPieces.size();
	}

	public Map<Position, Piece> getChessPiecesClone() {
		Map<Position, Piece> chessPiecesClone =  new HashMap<Position, Piece>();
		Set<Position> keySet = chessPieces.keySet();
		for (Position position : keySet) {
            chessPiecesClone.put(position, chessPieces.get(position).clone());
//      Position positionClone = Position.createPosition(position.getXCoord(), position.getYCoord());
//			Piece chessPieceToClone = chessPieces.get(position);
//			Colour colour = chessPieceToClone.getColour();
//			if (chessPieceToClone instanceof Rook)
//				chessPiecesClone.put(positionClone, new Rook(colour, positionClone));
//			else if (chessPieceToClone instanceof Knight)
//				chessPiecesClone.put(positionClone, new Knight(colour, positionClone));
//			else if (chessPieceToClone instanceof Bishop)
//				chessPiecesClone.put(positionClone, new Bishop(colour, positionClone));
//			else if (chessPieceToClone instanceof Queen)
//				chessPiecesClone.put(positionClone, new Queen(colour, positionClone));
//			else if (chessPieceToClone instanceof King)
//				chessPiecesClone.put(positionClone, new King(colour, positionClone));
//			else if (chessPieceToClone instanceof Pawn)
//				chessPiecesClone.put(positionClone, new Pawn(colour, positionClone));
//			else
//				assert false;
		}

		return chessPiecesClone;
	}

	public Map<Position, Piece> getChessPieces() {
		return chessPieces;
	}

	public void setChessPieces(Map<Position, Piece> chessPieces) {
		this.chessPieces = chessPieces;
	}

}
