/*
 
 *
 */

import java.awt.*;
import java.applet.*;
import java.awt.geom.*;

public class MazeApplet extends Applet implements Runnable
{
	
  private final int MAZE_COL_MAX = 49;
  private final int MAZE_ROW_MAX = 39;
  private final int MAZE_COL_MIN = 19;
  private final int MAZE_ROW_MIN = 9;
  private final int MAZE_CORNER = 1;
  private final int MAZE_WALL = 2;
  private final int STATE_CLEAR = 0;
  private final int STATE_BUILD = 1;
  private final int STATE_BUILD_DONE = 3;
  private int screenCol;
  private int screenRow;
  
  private int topLeftCol;
  private int topLeftRow;
  private int startCol, startRow;
  private int endCol, endRow;
  private boolean sucess;
  private Color wallColor;
  private Color startColor;
  private Color endColor;
  private Color cornerColor;
  private int state;
  private int maze[][];
  private int mazeCols, mazeRows;
  private Image backImg = null; //handle to the offscreen buffer

  int y1 = 8,y2 = 7, y3, c1, c2, c3;
  String str1, str2, str3;
//  Monkey m = new Monkey();
  Thread t1, t2, mainThread;


	public void init() {
    state = STATE_CLEAR;
    //backImg = createImage(500, 400);
    
    

    /*
    t1.start();
    t2 = new Thread();
    t2.start();
    */
    mainThread = new Thread(this); 
    mainThread.start();
 
	}

  public void update(Graphics g)
  {
    paint(g);
  }

	public void paint(Graphics g) {
    int h = 3;
    int blockSize = 6;
    int mx =0, my=0;
    int row, col;
    Graphics2D gback; //context for back buffer
    //Image backImg;

    if (backImg == null) 
      backImg = createImage(500, 400);

    gback = (Graphics2D) backImg.getGraphics();
    // clear back buffer
    gback.setColor(Color.black);
    gback.fillRect(0,0,500,400);
    //Graphics2D g2 = (Graphics2D) g;
    //g.drawString("" + (int)(Math.random() * 5) + " " + str1, 0, 30 );
    //g.drawString("" + c2+ " " + str2, 0, y2 );
    //g.drawString("" + System.currentTimeMillis()+ " " + str3, 40, 200 );
    //g2.draw(new Rectangle2D.Float(10,20,200,200));

    if (state != STATE_CLEAR)
    {
      for (col = 0; col < mazeCols; col++)
      {
        for (row = 0; row < mazeRows; row++)
        {
          gback.setColor(Color.red);
          gback.draw(new Rectangle2D.Float(mx,my,blockSize,blockSize));
          if (maze[row][col] == MAZE_WALL)
          {
            gback.setColor(Color.blue);
            gback.fillRect(mx,my,blockSize,blockSize); 
          }
        // g2.draw(new Rectangle2D.Float(0,0,16,16));
          //System.out.println("drawing?");
         mx += blockSize;
       }
       mx = 0; my +=blockSize;
      }
    }
    g.drawImage(backImg,0,0,this);
    backImg = null;
	}

  public void run()
  {
    while (mainThread == Thread.currentThread())
    {
      
      if (state == STATE_CLEAR)
      {
        RandomSize();
        maze = new int [mazeRows][mazeCols];
        InsertCorners();
     
        state = STATE_BUILD;
        t1 = new Thread() 
        {
          public void run()
          {
            //while(t1 == Thread.currentThread()) 
            //{
            
             // c1++;
              //y1=(int)(Math.random() * 200);
              BuildMaze(10);
    
              
      
            
            //}
  
          } 
        };
        t1.start();
      }

      repaint();
          try {
           mainThread.sleep(10);
         } catch (InterruptedException e) {}
       
      if (state == STATE_BUILD_DONE) state = STATE_CLEAR;  
    } // end while

  } //end run()


  private void RandomSize ()
  {
    // sets size of maze, dimensions must be odd
    int offset = 0;
    offset = (int)(Math.random() * ((MAZE_COL_MAX - MAZE_COL_MIN)+1));
    if (offset % 2 != 0) offset--;  //make it even
    mazeCols = MAZE_COL_MIN + offset;
    

    offset = (int)(Math.random() * ((MAZE_ROW_MAX - MAZE_ROW_MIN)+1));
    if (offset % 2 != 0) offset--;  //make it even
    mazeRows = MAZE_ROW_MIN + offset;
    //System.out.println("rows: " + mazeRows + " cols: " + mazeCols); 
  }

  private void InsertCorners ()
  {
    int row, col;
  
  //DrawOnMaze(hdc,2,2,RGB(255,255,255));
    for (col = 1; col < mazeCols; col += 2)
    {
      for (row = 1; row < mazeRows ; row += 2)
      {
        maze[row][col] = MAZE_CORNER;
      }
    }  
  }


  private void BuildMaze(long waitTime)
  {
    //int totalCorners = ((mazeCols - 1) / 2) * ((mazeRows - 1) / 2);
    //long startTime;
     int randCol = 0, randRow = 0, randDir, row, col;
    int numCorners;
    boolean cornerLeft = true, gotCorner, hitWall;
   
    while (cornerLeft)
    {  //repaint();
      gotCorner = false;
      while (!gotCorner)
      {
        // first get the column for a random corner
        numCorners = (mazeCols - 1) / 2;  
        randCol = (int)(Math.random() * numCorners) * 2 + 1; 
        
        // then the row
        numCorners = (mazeRows - 1) / 2;  
        randRow = (int)(Math.random() * numCorners) * 2 + 1; 
        
        // is it not yet taken?  
        if (maze[randRow][randCol] == MAZE_CORNER)
        {
          gotCorner = true;
          //System.out.println("got a corner");
        }
      }
      randDir = (int)(Math.random() * 4);
      
      hitWall = false; col = randCol; row = randRow;
      while (!hitWall)
      {
        maze[row][col] = MAZE_WALL;
        switch(randDir)
        {
          case 0:
            row = row - 1;
            break;
          case 1:
            row = row + 1;
            break;
          case 2:
            col = col - 1;
            break;
          case 3:
            col = col + 1;
            break;
        }
        if (row < 0 || row == mazeRows || col < 0 || col == mazeCols)
          hitWall = true;
        else if (maze[row][col] == MAZE_WALL)
          hitWall = true;
      } // we hit a wall
  
      // See if any corners are left...
      cornerLeft = false;
      for (col = 1; col < mazeCols; col += 2)
      {
        for (row = 1; row < mazeRows; row += 2)
        {
          if (maze[row][col] == MAZE_CORNER)
          {
            cornerLeft = true;
            //goto exitfor;     // VERY BAD PRACTICE, but fast
          }
        }
      }
        //exitfor: ;
 //repaint();
            try { t1.sleep(20); } catch (InterruptedException e) {}
//waitTicks(20);
    } // End of corners.
    state = STATE_BUILD_DONE;
  }


  public void waitTicks(long waitTime)
  {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < startTime + waitTime) { }
  }
} // end class



class t3 extends Thread
{

  public void run()
  {

  }
  

}