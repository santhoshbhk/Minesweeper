package minesweeper;

public class unitCell {
	
	private boolean flaggedCell;

	private boolean hasSeen;
	
	private int cellValue;
	
	
	public unitCell()
	{
		cellValue = 0;

		flaggedCell = false;
		hasSeen = false;
	}
	
	public boolean isFlaggedCell() {
		return flaggedCell;
	}
	public void setFlaggedCell(boolean inpflaggedCell) {
		this.flaggedCell = inpflaggedCell;
	}
	
	public int getCellValue() {
		return cellValue;
	}
	public void setCellValue(int inpcellValue) {
		this.cellValue = inpcellValue;
	}

	public boolean isHasSeen() {
		return hasSeen;
	}

	public void setHasSeen(boolean inpHasSeen) {
		this.hasSeen = inpHasSeen;
	}
	
}
