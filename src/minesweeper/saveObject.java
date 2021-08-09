package minesweeper;

public class saveObject implements java.io.Serializable{
	private int [][] cellValueArray;
	private int [][] hasSeenArray;
	private int [][] flaggedCellArray;
	private int timeLeft = 0, saveSlot = 0;
	private int boardRow = 0, boardCol = 0;
	private int serverOption = 0;
	/*
	 * 1 => Requesting to save into the database
	 * 2 => Check if the saveSlot is empty or not
	 * 3 => Retrieve a saved object and return to the client
	 * 4 => get top 5 highscore string
	 * 5 => insert score into highscore database table
	 */
	
	public saveObject(int [][] inpCellValueArray, int [][] inpHasSeenArray,
			int [][] inpFlaggedCellArray, int inpTimeLeft, int inpSaveSlot, int inpboardRow, int inpboardCol,
			int inpServerOption)
	{
		this.cellValueArray = inpCellValueArray.clone();
		this.hasSeenArray = inpHasSeenArray.clone();
		this.flaggedCellArray = inpFlaggedCellArray.clone();
		this.timeLeft = inpTimeLeft;
		this.saveSlot = inpSaveSlot;
		this.boardRow = inpboardRow;
		this.boardCol = inpboardCol;
		this.serverOption = inpServerOption;
	}
	
	public saveObject()
	{
		this.cellValueArray = new int [16][16];
		this.hasSeenArray = new int[16][16];
		this.flaggedCellArray = new int[16][16];
	}

	public int[][] getCellValueArray() {
		return cellValueArray;
	}

	public void setCellValueArray(int[][] cellValueArray) {
		this.cellValueArray = cellValueArray.clone();
	}

	public int[][] getHasSeenArray() {
		return hasSeenArray;
	}

	public void setHasSeenArray(int[][] hasSeenArray) {
		this.hasSeenArray = hasSeenArray.clone();
	}

	public int[][] getFlaggedCellArray() {
		return flaggedCellArray;
	}

	public void setFlaggedCellArray(int[][] flaggedCellArray) {
		this.flaggedCellArray = flaggedCellArray.clone();
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public int getSaveSlot() {
		return saveSlot;
	}

	public void setSaveSlot(int saveSlot) {
		this.saveSlot = saveSlot;
	}

	public int getBoardRow() {
		return boardRow;
	}

	public void setBoardRow(int boardRow) {
		this.boardRow = boardRow;
	}

	public int getBoardCol() {
		return boardCol;
	}

	public void setBoardCol(int boardCol) {
		this.boardCol = boardCol;
	}

	public int getServerOption() {
		return serverOption;
	}

	public void setServerOption(int serverOption) {
		this.serverOption = serverOption;
	}

	

}
