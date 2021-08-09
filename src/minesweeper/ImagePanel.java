package minesweeper;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private Image img;
	private int rowNum, colNum;
	
	public void setPos(int i, int j)
	{
		rowNum = i;
		colNum = j;
	}
	
	public int getRow()
	{
		return rowNum;
	}
	
	public int getCol()
	{
		return colNum;
	}
	
	public void setImg(int imgNum)
	{
		img = new ImageIcon(imgNum + ".png").getImage();
	}
	
	public Image getImg()
	{
		return img;
	}
	
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
}
