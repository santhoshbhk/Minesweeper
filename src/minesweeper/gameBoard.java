package minesweeper;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class gameBoard extends JPanel{
	
	private final int BOARDROW = 16; //number of rows in the board
	private final int BOARDCOL = 16; //number of cols in the board
	
	private final int CELLROW = 15; //unit cell row in pixels
	private final int CELLCOL = 15; //unit cell col in pixels
	
	private unitCell [][] boardArray;
	private ImagePanel [][] img;
	private int numFlags, score, timeRemaining;;
	private boolean hasLost;
	
	private JLabel numMinesDisplay, scoreLabel;
	private JPanel scorePanel;
	private Timer timer;
	
	private JMenuBar mb;
	private JMenu File, HighScore;
	private JMenuItem newMenu, openMenu, saveMenu, exitMenu, save1, save2, save3, checkHighScore, 
	save4, save5, open1, open2, open3, open4, open5;
	private JFrame currFrame;
	
	private DataInputStream fromServerCheckSave = null, fromServerTopFiveScores = null;
	private Socket socket = null;
	String host = null;
	
	private int [][] cellValueArray;
	private int [][] hasSeenArray;
	private int [][] flaggedCellArray;
	
	public gameBoard(JFrame mainFrame, unitCell[][] loadedBoardArray, int timeLeft)
	{
		currFrame = mainFrame;
		establishConnectionToServer();
		this.setLayout(null);
		if(loadedBoardArray == null)
		{
			initializeBoard();
			setMines();
			setCellVal();
			initializeImage();
			createGridAndScore();
		}
		else
		{
			hasLost = false;
			cellValueArray = new int [BOARDROW][BOARDCOL];
	        hasSeenArray = new int [BOARDROW][BOARDCOL];
	        flaggedCellArray = new int [BOARDROW][BOARDCOL];
	        numFlags = 40;
	        
			boardArray = loadedBoardArray.clone();
			int loadedTimeRemaining = timeLeft + 1;
			
			for(int i = 0; i < BOARDROW; ++i)
			{
				for(int j = 0; j < BOARDCOL; ++j)
				{
					if(boardArray[i][j].isFlaggedCell())
						--numFlags;
				}
			}
			loadImage();
			loadGridAndScore(loadedTimeRemaining, numFlags);
		}
		createMenuBar(currFrame);
		
		int counter = 0;
		
		System.out.println("Solution to the board: (9 is a mine & 0 is empty)");
		
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				if(boardArray[i][j].getCellValue() == 9)
					++counter;
					
				System.out.print(boardArray[i][j].getCellValue() + " ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		//System.out.println(counter);
	}
	
	public boolean checkOOB(int i, int j) //Check if it is out of bounds
	{
		if(i < 0 || j < 0 || i >= BOARDROW || j >= BOARDCOL)
			return false;
		else
			return true;
	}
	
	public boolean checkValid(int i, int j)
	{
		if(checkOOB(i,j) == false)
			return false;
		
		if(boardArray[i][j].isHasSeen() == true || boardArray[i][j].getCellValue() == 9 || boardArray[i][j].isFlaggedCell() == true)
			return false;
		else
			return true;
	}
	
	public int checkValue(int i ,int j)
	{
		return boardArray[i][j].getCellValue();
	}
	
	public void initializeBoard()
	{
		numFlags = 40;
		hasLost = false;
		boardArray = new unitCell[BOARDROW][BOARDCOL];
		cellValueArray = new int [BOARDROW][BOARDCOL];
        hasSeenArray = new int [BOARDROW][BOARDCOL];
        flaggedCellArray = new int [BOARDROW][BOARDCOL];
		
		for(int i = 0 ; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
				boardArray[i][j] = new unitCell();
		}
	}
	
	public void setMines()
	{
		int mineCount = 0;
		int randomRow, randomCol;
		Random rand = new Random();
		
		while(mineCount != numFlags)
		{
			randomRow = rand.nextInt(BOARDROW - 1 - 0 + 1) - 0; //Generate random number between 0 and 15
			randomCol = rand.nextInt(BOARDCOL - 1 - 0 + 1) - 0; //Generate random number between 0 and 15
			
			if(boardArray[randomRow][randomCol].getCellValue() == 0)
			{
				boardArray[randomRow][randomCol].setCellValue(9); //9 is the cell value with mines
				++mineCount;
			}
		}
	}
	
	public void setCellVal()
	{
		for(int i = 0 ; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				int counter = 0;
				if(checkOOB(i - 1, j) && checkValue(i - 1, j) == 9) // Check north cell
					++counter;
				if(checkOOB(i + 1, j) && checkValue(i + 1, j) == 9) //check south cell
					++counter;
				if(checkOOB(i, j - 1) && checkValue(i, j - 1) == 9) //check west cell
					++counter;
				if(checkOOB(i, j + 1) && checkValue(i, j + 1) == 9) //check east cell
					++counter;
				if(checkOOB(i - 1, j + 1) && checkValue(i - 1, j + 1) == 9) // check north east cell
					++counter;
				if(checkOOB(i + 1, j + 1) && checkValue(i + 1, j + 1) == 9) // check south east cell
					++counter;
				if(checkOOB(i + 1, j - 1) && checkValue(i + 1, j - 1) == 9) // check south west cell
					++counter;
				if(checkOOB(i - 1, j - 1) && checkValue(i - 1, j - 1) == 9) // check north west cell
					++counter;
				
				if(boardArray[i][j].getCellValue() != 9)
					boardArray[i][j].setCellValue(counter);
			}
		}
	}
	
	public void initializeImage()
	{
		img = new ImagePanel[BOARDROW][BOARDCOL];
		
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{	
				img[i][j] = new ImagePanel();
				
				img[i][j].setImg(10); //Setting to 10.png which is the covered cell;
				img[i][j].setPos(i, j);
				
				img[i][j].addMouseListener(new cellImageListener());
			}
			
		}
	}
	
	public void loadImage()
	{
		img = new ImagePanel[BOARDROW][BOARDCOL];
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{	
				img[i][j] = new ImagePanel();
				if(boardArray[i][j].isHasSeen())
					img[i][j].setImg(boardArray[i][j].getCellValue());
				else if(boardArray[i][j].isFlaggedCell())
					img[i][j].setImg(11);
				else
					img[i][j].setImg(10);
				
				img[i][j].setPos(i, j);
				
				img[i][j].addMouseListener(new cellImageListener());
			}
			
		}
	}
	
	public void createGridAndScore()
	{
		JPanel Grid = new JPanel();
		JPanel displayPanel = new JPanel();
		numMinesDisplay = new JLabel();
		numMinesDisplay.setText("Flags Rem: " + numFlags);
		
		numMinesDisplay.setMinimumSize(new Dimension(20,10));
		displayPanel.add(numMinesDisplay);
		displayPanel.setBounds(50 ,(BOARDROW * BOARDCOL) + 30, 100,20);
		
		Grid.setSize(BOARDROW * BOARDCOL, BOARDROW * BOARDCOL);
		
		Grid.setVisible(true);
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				Grid.add(img[i][j]);
			}
		}
		Grid.setLayout(new GridLayout(BOARDROW,BOARDCOL));
		Grid.setBounds(50, 25, BOARDROW * BOARDCOL, BOARDROW * BOARDCOL);
		
		scorePanel = new JPanel();
		scoreLabel = new JLabel();
		
		timeRemaining = 1001;
		score = timeRemaining;
		
		timer = new Timer(1000, new timerListener());
		scorePanel.add(scoreLabel);
		scorePanel.setBounds(95, 5, 150,20);
		
		this.add(Grid);
		this.add(displayPanel);
		this.add(scorePanel);
		timer.start();
		
	}
	
	public void loadGridAndScore(int inpTimeRemaining, int inpNumFlagsRemaining)
	{
		JPanel Grid = new JPanel();
		JPanel displayPanel = new JPanel();
		numMinesDisplay = new JLabel();
		numMinesDisplay.setText("Flags Rem: " + inpNumFlagsRemaining);
		
		numMinesDisplay.setMinimumSize(new Dimension(20,10));
		displayPanel.add(numMinesDisplay);
		displayPanel.setBounds(50 ,(BOARDROW * BOARDCOL) + 30, 100,20);
		
		Grid.setSize(BOARDROW * BOARDCOL, BOARDROW * BOARDCOL);
		
		Grid.setVisible(true);
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				Grid.add(img[i][j]);
			}
		}
		Grid.setLayout(new GridLayout(BOARDROW,BOARDCOL));
		Grid.setBounds(50, 25, BOARDROW * BOARDCOL, BOARDROW * BOARDCOL);
		
		scorePanel = new JPanel();
		scoreLabel = new JLabel();
		
		timeRemaining = inpTimeRemaining;
		score = timeRemaining;
		
		timer = new Timer(1000, new timerListener());
		scorePanel.add(scoreLabel);
		scorePanel.setBounds(95, 5, 150,20);
		
		this.add(Grid);
		this.add(displayPanel);
		this.add(scorePanel);
		timer.start();
	}
	
	public boolean checkAllSeen()
	{
		
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				if(boardArray[i][j].isHasSeen() == false)
					return false;
			}
		}
		return true;
	}
	
	class timerListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) {
			--timeRemaining;
			--score;
			scoreLabel.setText("Time Remaining: " + timeRemaining);
			if(timeRemaining <= 0)
			{
				timer.stop();
				executeExitSequence();
				
			}
		}
		
	}
	
	public void printHasSeen()
	{
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				if(boardArray[i][j].isHasSeen())
					System.out.print(1 + " ");
				else if(!boardArray[i][j].isHasSeen() && !boardArray[i][j].isFlaggedCell())
					System.out.print(0 + " ");
				else if (boardArray[i][j].isFlaggedCell())
					System.out.print(9 + " ");
			}
			System.out.print("\n");
		}
		System.out.println("\n");
	}
	
	class cellImageListener implements MouseListener
	{

		public void mouseClicked(MouseEvent me) {
			ImagePanel currImg = (ImagePanel) me.getSource();
			int i = currImg.getRow(); //i value for the image that called this listener
			int j = currImg.getCol(); //j value for the image that called this listener
			customPair coordinate = new customPair(i,j);
			
			if(me.getButton() == MouseEvent.BUTTON1 && !hasLost && !boardArray[i][j].isFlaggedCell()) //if left mouse button is clicked
			{
				if(boardArray[i][j].getCellValue() == 0) //if the cell is an empty cell
				{
					bfs(coordinate);
				}
				else if(boardArray[i][j].getCellValue() == 9 && !hasLost) // if the cell is a mine
				{
					executeExitSequence();
					boardArray[i][j].setHasSeen(true);
				}
				else if(!hasLost)//if the cell is a number
				{
					changeCellImg(checkValue(i,j), i, j); //change the image and repaint
					boardArray[i][j].setHasSeen(true);
				}
				
			}
			else if (me.getButton() == MouseEvent.BUTTON3 && !hasLost) //if right mouse button is clicked
			{
				if(boardArray[i][j].isFlaggedCell() && !boardArray[i][j].isHasSeen())
				{
					changeCellImg(10,i,j);
					++numFlags;
					boardArray[i][j].setFlaggedCell(false);
					numMinesDisplay.setText("Flags Rem: " + numFlags);
				}
				else if(!boardArray[i][j].isFlaggedCell() && !boardArray[i][j].isHasSeen())
				{
					changeCellImg(11, i, j);
					--numFlags;
					boardArray[i][j].setFlaggedCell(true);
					numMinesDisplay.setText("Flags Rem: " + numFlags);
				}

			}
			
			if(!hasLost && (checkAllSeen() || numFlags == 0))
			{
				for(int x = 0; x < BOARDROW; ++x)
				{
					for(int y = 0; y < BOARDCOL; ++y)
					{
						if(boardArray[x][y].getCellValue() == 9 && !boardArray[x][y].isFlaggedCell())
						{
							executeExitSequence();
						}
					}
				}
				
				//if code reaches here it means the player won!
				if(!hasLost)
					executeWinSequence();
					
			}
			
		}

		public void mouseEntered(MouseEvent me) {
			
		}

		public void mouseExited(MouseEvent me) {
			
		}

		public void mousePressed(MouseEvent me) {
					
		}

		public void mouseReleased(MouseEvent me) {
			
		}
		
	}
	
	public void bfs(customPair coordinate)
	{
		
		Queue<customPair> tempQueue = new LinkedList<customPair>();
		int i, j;
		tempQueue.add(coordinate);
		
		while(!tempQueue.isEmpty())
		{
			i = tempQueue.peek().geti();
			j = tempQueue.peek().getj();
			changeCellImg(0,i,j);
			boardArray[i][j].setHasSeen(true);
			
			tempQueue.poll();
			
			if(checkValid(i - 1, j)) // North cell
			{
				if(checkValue(i - 1, j) == 0)
					tempQueue.offer(new customPair(i - 1, j));
				else
				{
					changeCellImg(checkValue(i - 1, j), i - 1, j);
					boardArray[i - 1][j].setHasSeen(true);
				}
					
			}
			
			if(checkValid(i + 1, j)) //South cell
			{
				if(checkValue(i + 1, j) == 0)
					tempQueue.offer(new customPair(i + 1, j));
				else
				{
					changeCellImg(checkValue(i + 1, j), i + 1, j);
					boardArray[i + 1][j].setHasSeen(true);
				}
			}
			
			if(checkValid(i, j + 1)) // East Cell
			{
				if(checkValue(i, j + 1) == 0)
					tempQueue.offer(new customPair(i, j + 1));
				else
				{
					changeCellImg(checkValue(i, j + 1), i, j + 1);
					boardArray[i][j + 1].setHasSeen(true);
				}
			}
			
			if(checkValid(i, j - 1)) // West Cell
			{
				if(checkValue(i, j - 1) == 0)
					tempQueue.offer(new customPair(i, j - 1));
				else
				{
					changeCellImg(checkValue(i, j - 1), i, j - 1);
					boardArray[i][j - 1].setHasSeen(true);
				}
			}
			
			if(checkValid(i - 1, j + 1)) // North East cell
			{
				if(checkValue(i - 1, j + 1) == 0)
					tempQueue.offer(new customPair(i - 1, j + 1));
				else
				{
					changeCellImg(checkValue(i - 1, j + 1), i - 1, j + 1);
					boardArray[i - 1][j + 1].setHasSeen(true);
				}
			}
			
			if(checkValid(i - 1, j - 1)) // North West Cell
			{
				if(checkValue(i - 1, j - 1) == 0)
					tempQueue.offer(new customPair(i - 1, j - 1));
				else
				{
					changeCellImg(checkValue(i - 1, j - 1), i - 1, j - 1);
					boardArray[i - 1][j - 1].setHasSeen(true);
				}
			}
			
			if(checkValid(i + 1, j + 1)) // South East cell
			{
				if(checkValue(i + 1, j + 1) == 0)
					tempQueue.offer(new customPair(i + 1, j + 1));
				else
				{
					changeCellImg(checkValue(i + 1, j + 1), i + 1, j + 1);
					boardArray[i + 1][j + 1].setHasSeen(true);
				}
			}
			
			if(checkValid(i + 1, j - 1)) //South West cell
			{
				if(checkValue(i + 1, j - 1) == 0)
					tempQueue.offer(new customPair(i + 1, j - 1));
				else
				{
					changeCellImg(checkValue(i + 1, j - 1), i + 1, j - 1);
					boardArray[i + 1][j - 1].setHasSeen(true);
				}
			}
		}
		
	}
	
	public void changeCellImg(int val, int i, int j)
	{
		img[i][j].setImg(val);
		img[i][j].repaint();
	}
	
	public void executeExitSequence()
	{
		hasLost = true;
		timer.stop();
		numMinesDisplay.setText("Game Lost!!!!");
		scoreLabel.setText("Time Remaining: " + 0);
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				if(boardArray[i][j].getCellValue() == 9 && !boardArray[i][j].isFlaggedCell())
				{
					changeCellImg(9, i, j);
					boardArray[i][j].setHasSeen(true);
				}
				
				else if(boardArray[i][j].getCellValue() != 9 && boardArray[i][j].isFlaggedCell())
				{
					changeCellImg(12, i, j);
					boardArray[i][j].setHasSeen(true);
				}
			}
		}
		
		JFrame f;
		f = new JFrame();
		JOptionPane.showMessageDialog(f,"You Lost :( Your score is: " + 0 + "\nPlease choose what you want to do next from the menu bar");
	}
	
	public void executeWinSequence()
	{
		timer.stop();
		numMinesDisplay.setText("Game Won!!!!!");
		//insertHighScore(score);
		JFrame f;
		f = new JFrame();
		JOptionPane.showMessageDialog(f,"You won! Your score is: " + score + "\nPlease choose what you want to do next from the menu bar");
	}
	
	public void checkIfDBEmpty()
	{
		String saveRes;
		updateBoardArrays();
		saveObject checkSave1 =
		          new saveObject(cellValueArray, hasSeenArray, flaggedCellArray, timeRemaining, 1, BOARDROW, 
		        		  BOARDCOL, 2);
		
		try {
			if(socket == null)
				socket = new Socket("localhost", 8000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			
			ObjectOutputStream toServer =
			          new ObjectOutputStream(socket.getOutputStream());
			fromServerCheckSave = new DataInputStream(socket.getInputStream());
			//check if Save1 is used
			
			toServer.writeObject(checkSave1);
			saveRes = fromServerCheckSave.readUTF();
			
			if(saveRes.charAt(0) == '1')
			{
				save1.setText("Game 1");
				open1.setText("Game 1");
			}
			
			if(saveRes.charAt(1) == '1')
			{
				save2.setText("Game 2");
				open2.setText("Game 2");
			}
			
			if(saveRes.charAt(2) == '1')
			{
				save3.setText("Game 3");
				open3.setText("Game 3");
			}
			
			if(saveRes.charAt(3) == '1')
			{
				save4.setText("Game 4");
				open4.setText("Game 4");
			}
			
			if(saveRes.charAt(4) == '1')
			{
				save5.setText("Game 5");
				open5.setText("Game 5");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createMenuBar(JFrame mainFrame)
	{
		mb = new JMenuBar();
		File = new JMenu("File");
		HighScore = new JMenu("High Score");
		
		checkHighScore = new JMenuItem("Check High Score");
		
		newMenu = new JMenuItem("New");
		newMenu.setName("New");
		
		openMenu = new JMenu("Open");
		openMenu.setName("Open");
		
		saveMenu = new JMenu("Save");
		saveMenu.setName("Save");
		
		exitMenu = new JMenuItem("Exit");
		exitMenu.setName("Exit");
		
		save1 = new JMenuItem("Empty");
		save1.setName("Save1");
		save2 = new JMenuItem("Empty");
		save2.setName("Save2");
		save3 = new JMenuItem("Empty");
		save3.setName("Save3");
		save4 = new JMenuItem("Empty");
		save4.setName("Save4");
		save5 = new JMenuItem("Empty");
		save5.setName("Save5");
		
		open1 = new JMenuItem("Empty");
		open1.setName("Open1");
		open2 = new JMenuItem("Empty");
		open2.setName("Open2");
		open3 = new JMenuItem("Empty");
		open3.setName("Open3");
		open4 = new JMenuItem("Empty");
		open4.setName("Open4");
		open5 = new JMenuItem("Empty");
		open5.setName("Open5");
		
		checkIfDBEmpty();
		
		newMenu.addActionListener(new menuItemListener());
		exitMenu.addActionListener(new menuItemListener());
		
		save1.addActionListener(new saveGameListener());
		save2.addActionListener(new saveGameListener());
		save3.addActionListener(new saveGameListener());
		save4.addActionListener(new saveGameListener());
		save5.addActionListener(new saveGameListener());
		
		open1.addActionListener(new openGameListener());
		open2.addActionListener(new openGameListener());
		open3.addActionListener(new openGameListener());
		open4.addActionListener(new openGameListener());
		open5.addActionListener(new openGameListener());
		
		checkHighScore.addActionListener(new getHighScore());
		
		File.add(newMenu);
		File.addSeparator();
		File.add(openMenu);
		File.addSeparator();
		File.add(saveMenu);
		File.addSeparator();
		File.add(exitMenu);
		
		HighScore.add(checkHighScore);
		
		saveMenu.add(save1);
		saveMenu.add(save2);
		saveMenu.add(save3);
		saveMenu.add(save4);
		saveMenu.add(save5);
		
		
		openMenu.add(open1);
		openMenu.add(open2);
		openMenu.add(open3);
		openMenu.add(open4);
		openMenu.add(open5);
		
		
		
		mb.add(File);
		//mb.add(HighScore);
		mainFrame.setJMenuBar(mb);

	}
	
	class saveGameListener implements ActionListener
	{
		
		public void actionPerformed(ActionEvent ae) {
			JMenuItem currMenuItem = (JMenuItem)ae.getSource();
			
			if(currMenuItem.getName().equals("Save1") && !hasLost)
			{
				saveGame(1);
				save1.setText("Game 1");
				open1.setText("Game 1");
			}
			
			if(currMenuItem.getName().equals("Save2") && !hasLost)
			{
				saveGame(2);
				save2.setText("Game 2");
				open2.setText("Game 2");
			}
			
			if(currMenuItem.getName().equals("Save3") && !hasLost)
			{
				saveGame(3);
				save3.setText("Game 3");
				open3.setText("Game 3");
			}
			
			if(currMenuItem.getName().equals("Save4") && !hasLost)
			{
				saveGame(4);
				save4.setText("Game 4");
				open4.setText("Game 4");
			}
			
			if(currMenuItem.getName().equals("Save5") && !hasLost)
			{
				saveGame(5);
				save5.setText("Game 5");
				open5.setText("Game 5");
			}
		}
		
	}
	
	public void updateBoardArrays()
	{
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				cellValueArray[i][j] = boardArray[i][j].getCellValue();
				
				if(boardArray[i][j].isFlaggedCell())
					flaggedCellArray[i][j] = 1;
				else
					flaggedCellArray[i][j] = 0;
				
				if(boardArray[i][j].isHasSeen())
					hasSeenArray[i][j] = 1;
				else
					hasSeenArray[i][j] = 0;
			}
			
		}
	}
	
	public void establishConnectionToServer()
	{
		host = "localhost";
		try
		{
			if(socket == null)
		 		socket = new Socket(host, 8000);
		}
		catch (IOException ex) 
		{
	        ex.printStackTrace();
	    }
	}
	
	public void saveGame(int saveSlot)
	{
		//String host = "localhost";
		 try {
			 /*
			 	//Connecting to the server
			 	if(socket == null)
			 		socket = new Socket(host, 8000);*/

		        // Create an output stream to the server
		        ObjectOutputStream toServer =
		          new ObjectOutputStream(socket.getOutputStream());
		        
		        //Get the save arrays
		        
		        updateBoardArrays();
		        
		        // Create a Student object and send to the server
		        saveObject save =
		          new saveObject(cellValueArray, hasSeenArray, flaggedCellArray, timeRemaining, saveSlot, BOARDROW, 
		        		  BOARDCOL, 1);
		        toServer.writeObject(save);
		      }
		      catch (IOException ex) {
		        ex.printStackTrace();
		      }
	}
	
	class openGameListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ae) {
			JMenuItem currMenuItem = (JMenuItem)ae.getSource();
			
			if(currMenuItem.getName().equals("Open1") && !currMenuItem.getText().equals("Empty"))
			{
				openGame(1);
			}
			
			if(currMenuItem.getName().equals("Open2") && !currMenuItem.getText().equals("Empty"))
			{
				openGame(2);
			}
			
			if(currMenuItem.getName().equals("Open3") && !currMenuItem.getText().equals("Empty"))
			{
				openGame(3);
			}
			
			if(currMenuItem.getName().equals("Open4") && !currMenuItem.getText().equals("Empty"))
			{
				openGame(4);
			}
			
			if(currMenuItem.getName().equals("Open5") && !currMenuItem.getText().equals("Empty"))
			{
				openGame(5);
			}
			
		}
	}
	
	public void openGame(int gameNum)
	{
		saveObject retrievedObject = null;
		ObjectInputStream inputFromServer = null;
		ObjectOutputStream outputToServer = null;
		Object object = null;
		try 
		{
			outputToServer = new ObjectOutputStream(socket.getOutputStream());
			updateBoardArrays();
			saveObject save =
			          new saveObject(cellValueArray, hasSeenArray, flaggedCellArray, timeRemaining, gameNum, BOARDROW, 
			        		  BOARDCOL, 3);
			outputToServer.writeObject(save);
			inputFromServer = new ObjectInputStream(socket.getInputStream());
			object = inputFromServer.readObject();
			retrievedObject = (saveObject) object;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		loadBoard(retrievedObject);
		
	}
	
	public void loadBoard(saveObject newState)
	{
		
		int [][] retrievedCellVal = newState.getCellValueArray().clone();
		int [][] retrievedHasSeen = newState.getHasSeenArray().clone();
		int[][] retrievedFlagged = newState.getFlaggedCellArray().clone();
		int timeLeft = newState.getTimeLeft();
		
		for(int i = 0; i < BOARDROW; ++i)
		{
			for(int j = 0; j < BOARDCOL; ++j)
			{
				boardArray[i][j].setCellValue(retrievedCellVal[i][j]);
				
				if(retrievedFlagged[i][j] == 1)
					boardArray[i][j].setFlaggedCell(true);
				else
					boardArray[i][j].setFlaggedCell(false);
				
				if(retrievedHasSeen[i][j] == 1)
					boardArray[i][j].setHasSeen(true);
				else
					boardArray[i][j].setHasSeen(false);
			}
		}
		
		currFrame.dispose();
		mainFrame newBoard = new mainFrame(boardArray, timeLeft);
		newBoard.setTitle("Minesweeper");
		newBoard.setVisible(true);
		
	}
	
	class menuItemListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ae) {
			JMenuItem currMenuItem = (JMenuItem)ae.getSource();
			
			if(currMenuItem.getName().equals("New"))
			{
				currFrame.dispose();
				mainFrame newBoard = new mainFrame();
				newBoard.setTitle("Minesweeper");
				newBoard.setVisible(true);
			}
			else if(currMenuItem.getName().equals("Open"))
			{
				
			}
			else if(currMenuItem.getName().equals("Save"))
			{
				
			}
			else if(currMenuItem.getName().equals("Exit"))
			{
				System.exit(0);
			}
		}
		
	}
	
	public void insertHighScore(int score)
	{
		updateBoardArrays();
		saveObject checkTopFive =
		          new saveObject(cellValueArray, hasSeenArray, flaggedCellArray, timeRemaining, score, BOARDROW, 
		        		  BOARDCOL, 5);
		
		try {
			if(socket == null)
				socket = new Socket("localhost", 8000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			
			ObjectOutputStream toServer =
			          new ObjectOutputStream(socket.getOutputStream());
			
			toServer.writeObject(checkTopFive);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	class getHighScore implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			String topFiveScores = "";
			updateBoardArrays();
			saveObject checkTopFive =
			          new saveObject(cellValueArray, hasSeenArray, flaggedCellArray, timeRemaining, 1, BOARDROW, 
			        		  BOARDCOL, 4);
			
			try {
				if(socket == null)
					socket = new Socket("localhost", 8000);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				
				ObjectOutputStream toServer =
				          new ObjectOutputStream(socket.getOutputStream());
				fromServerTopFiveScores = new DataInputStream(socket.getInputStream());
				
				toServer.writeObject(checkTopFive);
				topFiveScores = fromServerCheckSave.readUTF();
				if(topFiveScores.isEmpty())
					topFiveScores = "No High Score set. Be the First! Open saved game 4 to continue with the win!";
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			JFrame f;
			f = new JFrame();
			f.setTitle("High Score");
			JOptionPane.showMessageDialog(f,"Top 5 Scores: \n\n" + topFiveScores);
		}
		
	}
}
