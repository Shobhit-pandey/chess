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
public class CastlingOpportunities {

	private boolean whiteKingCanCastleLeft;
	private boolean whiteKingCanCastleRight;
	private boolean blackKingCanCastleLeft;
	private boolean blackKingCanCastleRight;

	public static Boolean nextWhiteKingCanCastleLeft = false;
	public static Boolean nextWhiteKingCanCastleRight = false;
	public static Boolean nextBlackKingCanCastleLeft = false;
	public static Boolean nextBlackKingCanCastleRight = false;

	public CastlingOpportunities(Boolean whiteKingCanCastleLeft,
			Boolean whiteKingCanCastleRight, Boolean blackKingCanCastleLeft,
			Boolean blackKingCanCastleRight) {
		super();
		if (whiteKingCanCastleLeft == null) {
			this.whiteKingCanCastleLeft = Boolean.TRUE.equals(nextWhiteKingCanCastleLeft);
			nextWhiteKingCanCastleLeft = null;
		}
		else {
			nextWhiteKingCanCastleLeft = this.whiteKingCanCastleLeft = whiteKingCanCastleLeft.booleanValue();
		}
		if (whiteKingCanCastleRight == null) {
			this.whiteKingCanCastleRight = Boolean.TRUE.equals(nextWhiteKingCanCastleRight);
			nextWhiteKingCanCastleRight = null;
		}
		else {
			nextWhiteKingCanCastleRight = this.whiteKingCanCastleRight = whiteKingCanCastleRight.booleanValue();
		}
		if (blackKingCanCastleLeft == null) {
			this.blackKingCanCastleLeft = Boolean.TRUE.equals(nextBlackKingCanCastleLeft);
			nextBlackKingCanCastleLeft = null;
		}
		else {
			nextBlackKingCanCastleLeft = this.blackKingCanCastleLeft = blackKingCanCastleLeft.booleanValue();
		}
		if (blackKingCanCastleRight == null) {
			this.blackKingCanCastleRight = Boolean.TRUE.equals(nextBlackKingCanCastleRight);
			nextBlackKingCanCastleRight = null;
		}
		else {
			nextBlackKingCanCastleRight = this.blackKingCanCastleRight = blackKingCanCastleRight.booleanValue();
		}
	}

	@Override
	public boolean equals(Object candidate) {
		if (this == candidate)
			return true;

		if (!(candidate instanceof CastlingOpportunities))
			return false;

		CastlingOpportunities confirmed = (CastlingOpportunities)candidate;
		return whiteKingCanCastleLeft == confirmed.isWhiteKingCanCastleLeft()
		&& whiteKingCanCastleRight == confirmed.isWhiteKingCanCastleRight()
		&& blackKingCanCastleLeft == confirmed.isBlackKingCanCastleLeft()
		&& blackKingCanCastleRight == confirmed.isBlackKingCanCastleRight();
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public String toString() {
		return whiteKingCanCastleLeft + " " + whiteKingCanCastleRight + " " + blackKingCanCastleLeft + " " + blackKingCanCastleRight;
	}

	public boolean isWhiteKingCanCastleLeft() {
		return whiteKingCanCastleLeft;
	}

	public boolean isWhiteKingCanCastleRight() {
		return whiteKingCanCastleRight;
	}

	public boolean isBlackKingCanCastleLeft() {
		return blackKingCanCastleLeft;
	}

	public boolean isBlackKingCanCastleRight() {
		return blackKingCanCastleRight;
	}

	public static void resetStaticVariables() {
		nextWhiteKingCanCastleLeft = false;
		nextWhiteKingCanCastleRight = false;
		nextBlackKingCanCastleLeft = false;
		nextBlackKingCanCastleRight = false;
	}

}
