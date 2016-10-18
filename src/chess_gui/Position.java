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
public class Position implements Comparable {
	
	private int xCoord;
	private int yCoord;

	private Position(int xCoord, int yCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	public static Position createPosition(int xCoord, int yCoord) {
		if (xCoord >= 1 && xCoord <= 8
				&& yCoord >= 1 && yCoord <= 8)
			return new Position(xCoord, yCoord);
		return null;
	}

	public int getXCoord() {
		return xCoord;
	}

	public int getYCoord() {
		return yCoord;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Position))
			return false;
		Position p = (Position)o;
		return p.getXCoord() == getXCoord() && p.getYCoord() == getYCoord();
	}

	@Override
	public int hashCode() {
		return xCoord * yCoord;
	}

	@Override
	public String toString() {
		return "Position (" + xCoord + ", " + yCoord + ")";
	}

	@Override
	public int compareTo(Object object) {
		Position otherPosition = (Position)object;
		if (getXCoord() == otherPosition.getXCoord()) {
			return getYCoord() - otherPosition.getYCoord();
		}
		return getXCoord() - otherPosition.getXCoord();
	}

}
