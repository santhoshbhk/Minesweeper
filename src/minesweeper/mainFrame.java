package minesweeper;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

public class mainFrame extends JFrame{
	
	public mainFrame()
	{
		this.setSize(500,400);
		//new minesweeperMultiThreadServer();
		this.add(new gameBoard(this, null, 1001));
	}
	
	public mainFrame(unitCell[][] loadedBoardArray, int timeLeft)
	{
		this.setSize(500,400);
		//new minesweeperMultiThreadServer();
		this.add(new gameBoard(this, loadedBoardArray, timeLeft));
	}
	
	public static void main(String [] args)
	{
		mainFrame board1 = new mainFrame();
		board1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		board1.setTitle("Minesweeper");
		board1.setVisible(true);
	}
}
