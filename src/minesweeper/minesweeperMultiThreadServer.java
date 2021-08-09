package minesweeper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class minesweeperMultiThreadServer implements Runnable {

	private int clientNo = 0;
	private ObjectInputStream inputFromClient;
	private ObjectOutputStream outputToClient;
	private Connection conn;
	private PreparedStatement save1stmt, save2stmt, save3stmt, save4stmt, save5stmt, checkSaveStmt1, checkSaveStmt2,
	checkSaveStmt3, checkSaveStmt4, checkSaveStmt5, clearTable1, clearTable2, clearTable3, clearTable4, clearTable5,
	getTopFiveScores, insertScore;
	private String isSaveEmpty, topFiveScores;
	
	
	  public minesweeperMultiThreadServer() {
		  //System.out.println("Server Starting");
		  Thread t = new Thread(this);
		  t.start();
	  }
	
	public void run()
	{
		try {
	        // Create a server socket
	        ServerSocket serverSocket = new ServerSocket(8000);
	    
	        while (true) {
	          // Listen for a new connection request
	          Socket socket = serverSocket.accept();
	    
	          // Increment clientNo
	          clientNo++;

	          // Find the client's host name, and IP address
	          InetAddress inetAddress = socket.getInetAddress();
	          
	          // Create and start a new thread for the connection
	          new Thread(new HandleAClient(socket, clientNo)).start();
	        }
	      }
	      catch(IOException ex) {
	        System.err.println(ex);
	      }
	}
	
	// Define the thread class for handling new connection
	  class HandleAClient implements Runnable {
	    private Socket socket; // A connected socket
	    private int clientNum;
	    
	    /** Construct a thread */
	    public HandleAClient(Socket socket, int clientNum) {
	      this.socket = socket;
	      this.clientNum = clientNum;
	    }

	    /** Run a thread */
	    public void run() {
	      try {
	    	  
	        // Create data input and output streams
	        /*DataInputStream inputFromClient = new DataInputStream(
	          socket.getInputStream());
	        DataOutputStream outputToClient = new DataOutputStream(
	          socket.getOutputStream());*/

	        // Continuously serve the client
	        while (true) {
	        	inputFromClient = null;
	        	try {
	        		inputFromClient = new ObjectInputStream(socket.getInputStream());
	        	}
	        	catch(Exception ex)
	        	{
	        		ex.printStackTrace();
	        	}
	        	

    	       // Read from input
    	       Object object = inputFromClient.readObject();
	          
    	       
    	       //Store object into a database
    	       
    	       try {
    	    	   
    				conn = DriverManager.getConnection("jdbc:sqlite:minesweeperData.db");
    				
    				clearTable1 = conn.prepareStatement("DELETE FROM save1");				
    				clearTable2 = conn.prepareStatement("DELETE FROM save2");   				
    				clearTable3 = conn.prepareStatement("DELETE FROM save3");   				
    				clearTable4 = conn.prepareStatement("DELETE FROM save4");
    				clearTable5 = conn.prepareStatement("DELETE FROM save5");
    				
   
    				save1stmt = conn.prepareStatement("INSERT INTO save1 (row, col, cellVal, hasSeenVal, flaggedCellVal, timeLeft) "
    						+ "VALUES (?,?,?,?,?,?)");
    				
    				save2stmt = conn.prepareStatement("INSERT INTO save2 (row, col, cellVal, hasSeenVal, flaggedCellVal, timeLeft) "
    						+ "VALUES (?,?,?,?,?,?)");
    				
    				save3stmt = conn.prepareStatement("INSERT INTO save3 (row, col, cellVal, hasSeenVal, flaggedCellVal, timeLeft) "
    						+ "VALUES (?,?,?,?,?,?)");
    				
    				save4stmt = conn.prepareStatement("INSERT INTO save4 (row, col, cellVal, hasSeenVal, flaggedCellVal, timeLeft) "
    						+ "VALUES (?,?,?,?,?,?)");
    				
    				save5stmt = conn.prepareStatement("INSERT INTO save5 (row, col, cellVal, hasSeenVal, flaggedCellVal, timeLeft) "
    						+ "VALUES (?,?,?,?,?,?)");
    				
    				checkSaveStmt1 = conn.prepareStatement("SELECT * FROM save1");
    				checkSaveStmt2 = conn.prepareStatement("SELECT * FROM save2");
    				checkSaveStmt3 = conn.prepareStatement("SELECT * FROM save3");
    				checkSaveStmt4 = conn.prepareStatement("SELECT * FROM save4");
    				checkSaveStmt5 = conn.prepareStatement("SELECT * FROM save5");
    				
    				getTopFiveScores = conn.prepareStatement("SELECT * FROM highscore ORDER BY score DESC");
    				
    				insertScore = conn.prepareStatement("INSERT INTO highscore VALUES (?)");
    				
    			} catch (SQLException e) {
    				System.err.println("Connection error: " + e);
    				System.exit(1);
    			}
    	       saveObject so = (saveObject) object;
    	       
    	       if(so.getServerOption() == 1)
    	       {
    	    	   try
	    		   {
	    			   PreparedStatement stmt = null;
	    			   PreparedStatement cleartablestmt = null;
	    			   
	    			   
	    			   switch (so.getSaveSlot())
	    			   {
	    			   case 1:
	    				   cleartablestmt = clearTable1;
	    				   stmt = save1stmt;
	    				   break;
	    			   case 2:
	    				   cleartablestmt = clearTable2;
	    				   stmt = save2stmt;
	    				   break;
	    			   case 3:
	    				   cleartablestmt = clearTable3;
	    				   stmt = save3stmt;
	    				   break;
	    			   case 4:
	    				   cleartablestmt = clearTable4;
	    				   stmt = save4stmt;
	    				   break;
	    			   case 5:
	    				   cleartablestmt = clearTable5;
	    				   stmt = save5stmt;
	    				   break;
	    			   }
	    				
	    			   int boardRow = so.getBoardRow();
	    			   int boardCol = so.getBoardCol();
	    			   int [][] cellValueArray = so.getCellValueArray().clone();
	    			   int [][] hasSeenArray = so.getHasSeenArray().clone();
	    			   int [][] flaggedCellArray = so.getFlaggedCellArray().clone();
	    			   int timeLeft = so.getTimeLeft();
	    			   
	    			   cleartablestmt.executeUpdate();
	    			   
	    			   for(int i = 0; i < boardRow; ++i)
	    			   {
	    				   for(int j = 0; j < boardCol; ++j)
	    				   {
	    					   stmt.setInt(1, i);
	    					   stmt.setInt(2, j);
	    					   stmt.setInt(3, cellValueArray[i][j]);
	    					   stmt.setInt(4, hasSeenArray[i][j]);
	    					   stmt.setInt(5, flaggedCellArray[i][j]);
	    					   stmt.setInt(6, timeLeft);
	    					   stmt.executeUpdate();
	    				   }
	    			   }
	    				
	    		   }
	    		   catch (SQLException e) {
	    			   // TODO Auto-generated catch block
	    			   e.printStackTrace();
	    		   }
    	       }
    	       else if(so.getServerOption() == 2)
    	       {
    	    	   DataOutputStream outputToClient = new DataOutputStream(
    	    		          socket.getOutputStream());
    	    	   isSaveEmpty = checkSaveEmpty();
    	    	   outputToClient.writeUTF(isSaveEmpty);
    	       }
    	       else if(so.getServerOption() == 3)
    	       {
    	    	   outputToClient = new ObjectOutputStream(socket.getOutputStream());
    	    	   saveObject retrieve = retrieveArrays(so.getSaveSlot(), so);
    	    	   outputToClient.writeObject(retrieve);
    	       }
    	       else if(so.getServerOption() == 4)
    	       {
    	    	   DataOutputStream outputToClient = new DataOutputStream(
 	    		          socket.getOutputStream());
    	    	   topFiveScores = getTopFiveScores();
    	    	   outputToClient.writeUTF(topFiveScores);
    	       }
    	       else if(so.getServerOption() == 5)
    	       {
    	    	   insertScore(so.getSaveSlot());
    	       }
	    	      
	          
	        }
	      }
	      catch(IOException ex) 
	      {
	        ex.printStackTrace();
	      } 
	      catch (ClassNotFoundException e) 
	      {
			e.printStackTrace();
	      }
	      finally {
	    	     try 
	    	     {
	    	       inputFromClient.close();
	    	       conn.close();
	    	     }
	    	     catch (Exception ex) 
	    	     {
	    	       ex.printStackTrace();
	    	     }
	      }
	    }
	  }
	  
	  public String checkSaveEmpty()
	  {
		  String res = "";
		  
			  
		  try {
			  	PreparedStatement stmt = null;
			  	ResultSet rset;
			  	int numRowsInDB = 0;
			  	rset = checkSaveStmt1.executeQuery();
				
				while(rset.next())
				{
					++numRowsInDB;
				}
				
				if(numRowsInDB == 256)
					  res += "1";
				  else
					  res += "0";
				
				rset = checkSaveStmt2.executeQuery();
				numRowsInDB = 0;
				while(rset.next())
				{
					++numRowsInDB;
				}
				
				if(numRowsInDB == 256)
					  res += "1";
				  else
					  res += "0";
				
				rset = checkSaveStmt3.executeQuery();
				numRowsInDB = 0;
				while(rset.next())
				{
					++numRowsInDB;
				}
				
				if(numRowsInDB == 256)
					  res += "1";
				  else
					  res += "0";
				
				rset = checkSaveStmt4.executeQuery();
				numRowsInDB = 0;
				while(rset.next())
				{
					++numRowsInDB;
				}
				
				if(numRowsInDB == 256)
					  res += "1";
				  else
					  res += "0";
				
				rset = checkSaveStmt5.executeQuery();
				numRowsInDB = 0;
				while(rset.next())
				{
					++numRowsInDB;
				}
				
				if(numRowsInDB == 256)
					  res += "1";
				  else
					  res += "0";
				
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		  
		  return res;
	  }
	  
	  public saveObject retrieveArrays(int saveSlot, saveObject toRetrieve)
	  {
		  int numRow = toRetrieve.getBoardRow(), numCol = toRetrieve.getBoardCol();
		  int retrievedCellVal[][] = new int [numRow][numCol];
		  int retrievedHasSeen[][] = new int[numRow][numCol];
		  int retrievedFlagged[][] = new int[numRow][numCol];
		  int timeLeft = 0;
		  try
		   {
			  PreparedStatement stmt = null;
			  ResultSet rset = null;
			   
			   switch (saveSlot)
			   {
			   case 1:
				   stmt = checkSaveStmt1;
				   break;
			   case 2:
				   stmt = checkSaveStmt2;
				   break;
			   case 3:
				   stmt = checkSaveStmt3;
				   break;
			   case 4:
				   stmt = checkSaveStmt4;
				   break;
			   case 5:
				   stmt = checkSaveStmt5;
				   break;
			   }
			   
			   rset = stmt.executeQuery();
				
			   ResultSetMetaData rsmd = rset.getMetaData();
			   int numColumns = rsmd.getColumnCount();
			   
			   while(rset.next())
			   {
				   int x = 0, y = 0;
				   for(int i = 1; i <= numColumns; ++i)
				   {
					   Object o = rset.getObject(i);
					   int colInteger = Integer.parseInt(o.toString());
					   switch(i)
					   {
					   case 1:
						   x = colInteger;
						   break;
						   
					   case 2:
						   y = colInteger;
						   break;
						   
					   case 3:
						   retrievedCellVal[x][y] = colInteger;
						   break;
						   
					   case 4:
						   retrievedHasSeen[x][y] = colInteger;
						   break;
						   
					   case 5:
						   retrievedFlagged[x][y] = colInteger;
						   break;
						   
					   case 6:
						   timeLeft = colInteger;
						   break;
					   }
				   }
			   }
			   toRetrieve.setCellValueArray(retrievedCellVal);
			   toRetrieve.setHasSeenArray(retrievedHasSeen);
			   toRetrieve.setFlaggedCellArray(retrievedFlagged);
			   toRetrieve.setTimeLeft(timeLeft);
			   toRetrieve.setSaveSlot(saveSlot);
				
		   }
		   catch (SQLException e) {
			   // TODO Auto-generated catch block
			   System.out.println("Here");
			   e.printStackTrace();
		   }
		  return toRetrieve;
	  }
	  
	  public String getTopFiveScores()
	  {
		  String topFiveScores = "";
		  
		  try {
			  	PreparedStatement stmt = null;
			  	ResultSet rset;
			  	int numRows = 1;
			  	rset = getTopFiveScores.executeQuery();
				
				while(rset.next() && numRows <= 5)
				{
					++numRows;
					Object o = rset.getObject(1);
					String score = o.toString();
					topFiveScores += ("\t"+score + "\n");
				}
				
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		  
		  return topFiveScores;
		  
	  }
	  
	  public void insertScore(int score)
	  {
		  try {
			  	PreparedStatement stmt = null;
			  	stmt = insertScore;
			  	stmt.setInt(1, score);
			  	
			  	stmt.executeUpdate();
				
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
	  }
	  
	  public static void main(String[] args) 
	  {
		   new minesweeperMultiThreadServer();
		   
	  }

}
