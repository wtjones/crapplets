/*
 * @(#)SliceMap.java 1.0 started 5/12/02
    by W Travis Jones

Designed to run with 1.1 and MS runtimes.
Applet that builds a terrain using a simple process:
1. A line is randomly chosen to split the graphics area.
2. One side of the line is lowered in depth, in this case, darkened
3. The other side is raised, or brightened

The terrain graphics data is stored in mapData, a 3-dimension array with RGB data.
In paint() mapData is drawn to a back buffer, taking into account the current sea level, then the
back buffer is drawn to the screen.

Click the window to get focus and use plus and minus keys to raise and lower the sea level.

Some revisions were made during development:
1. At first, slope-intercept form was used to determine if a pixel was to be darkened or brightened.
  This worked, but because of the divides required, double precision was used, and I had to check
  for divide by zero. I dug into my old QBASIC code and wrote a function, lineFacing() that returns
  either a positive or negative number depending on which side of a line a point is.

2. At one point, backImg kept all the map data, and I used PixelGrabber to copy the back buffer to 
  an array at each frame. PixelGrabber creates a thread to do its work, and is very inefficent.
  I then realized that BufferedImage had a getRGB() method, so my back buffer became a BufferedImage.
  
3. When detecting sea level, the color blue was drawn to the back buffer. The problem is that the 
  height info is lost. I solved this by storing all map data in mapData[w][h][R G or B]. At each
  frame mapData is drawn to the back buffer with the sea level in mind, so getRGB() is no longer 
  needed.

 *
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.applet.*;


public class SliceMap extends Applet implements Runnable, KeyListener 
{
  final int LEFT = 0, RIGHT = 1, COLOR_CHANGE = 1, DEFAULT_SEA_LEVEL = 96;
  final int RED = 0, GREEN = 1, BLUE = 2, DEFAULT_GREEN = 100;
  Thread mainThread;
  private Image backImg = null;
  LineType line = new LineType();   // current splitting line
  int seaLevel = DEFAULT_SEA_LEVEL;
  int mapData[][][], mapWidth, mapHeight;

	public void init() 
	{
    mapWidth = getSize().width; mapHeight = getSize().height;
    mapData = new int[getSize().width][getSize().height][3];
    mainThread = new Thread(this);
    if (backImg == null)
      backImg = createImage(getSize().width, getSize().height);
    mainThread.start();
    
    this.addKeyListener(this);    
	}

	public void paint(Graphics g) 
	{	
    if (backImg == null)
      backImg = createImage(getSize().width, getSize().height);
      
    Graphics gback = backImg.getGraphics();
    drawMap(gback);
    g.drawImage(backImg,0,0,this);  //copy backbuffer to client area       
	}
	
  public void run()
  {
    // fill mapData[][][] with default green
    for (int y = 0; y < mapHeight; y ++)
    {
      for (int x = 0; x < mapWidth; x++)
        mapData[x][y][GREEN] = DEFAULT_GREEN;
    }
            
    while (mainThread == Thread.currentThread())
    {
       
      line = SliceBox(getSize().width, getSize().height);
      
      renderMap();
           
      repaint();
      
      try {
        mainThread.sleep(30);
      } catch (InterruptedException e) {}
      
    }
  }
  
  public void update(Graphics g)
  { // override built in update() so Java doesn't erase client area, causing flicker
    paint(g);
  }
  

  LineType SliceBox(int width, int height)
  /* returns a line that slices a rectangle area, denoted by width and height */
  {
    int side1, side2;
    side1=(int)(Math.random()*4);
    while((side2 = (int)(Math.random() * 4)) == side1) {  };

    do
    {
      switch (side1)
      {
       case 0: // up
         line.p1.x = (int)(Math.random() * getSize().width);
         line.p1.y = 0;
         break;
       case 1: // down
         line.p1.x = (int)(Math.random() * getSize().width);
         line.p1.y = getSize().height;
         break;
       case 2: // left
         line.p1.y = (int)(Math.random() * getSize().height);
         line.p1.x = 0;
         break;
       case 3: // right
         line.p1.y = (int)(Math.random() * getSize().height);
         line.p1.x = getSize().width;
         break;
      }
      
      switch (side2)
      {
       case 0: // up
         line.p2.x = (int)(Math.random() * getSize().width);
         line.p2.y = 0;
         break;
       case 1: // down
         line.p2.x = (int)(Math.random() * getSize().width);
         line.p2.y = getSize().height;
         break;
       case 2: // left
         line.p2.y = (int)(Math.random() * getSize().height);
         line.p2.x = 0;
         break;
       case 3: // right
         line.p2.y = (int)(Math.random() * getSize().height);
         line.p2.x = getSize().width;
         break;
      }
    } while ( !((line.p1.x != line.p2.x) && (line.p1.y != line.p2.y)) );
    return line;
  }

  void renderMap()
  {
    Graphics gback = backImg.getGraphics();
    Graphics g = this.getGraphics();
    int x; int y;
    int red=0, green=0, blue=0;
              
    for (y = 0; y < getSize().height; y++)
    {
           
      for ( x = 0; x < getSize().width; x++)
      { 
        if (lineFacing(x, y, line) < 0)
        {
          mapData[x][y][GREEN] -= COLOR_CHANGE;
        }
        else
        {          
          mapData[x][y][GREEN] += COLOR_CHANGE;       
        }
        
        if (mapData[x][y][GREEN] < 0) mapData[x][y][GREEN] = 0;
        
        if (mapData[x][y][GREEN] > 255) mapData[x][y][GREEN] =255;
      }      
    }
    showStatus("Sea: " + seaLevel + "  (+ and - to change)");
  }

  void drawMap(Graphics g)
  /* draw rgb data from mapData[][][] to a graphics context */
  {
    for (int y = 0; y < getSize().height; y++)
    {       
      for ( int x = 0; x < getSize().width; x++)
      { 
        if (mapData[x][y][GREEN] > seaLevel)
          g.setColor(new Color(mapData[x][y][RED], mapData[x][y][GREEN], mapData[x][y][BLUE]));
        else
          g.setColor(Color.blue);
          
        g.drawRect(x, y, 0, 0);
      }
    }
  }

  public void keyPressed(KeyEvent e) 
  {
    
    if (e.getKeyChar() == '=' || e.getKeyCode() == KeyEvent.VK_ADD)
      seaLevel++;

    if (e.getKeyChar() == '-') seaLevel--;
  }

  public void keyReleased(KeyEvent e) 
  {
  }

  public void keyTyped(KeyEvent e) 
  {  
  }

  int lineFacing(int x, int y, LineType line)
  {
    return (y - line.p1.y) * (line.p2.x - line.p1.x) - 
      (x - line.p1.x) * (line.p2.y - line.p1.y);
  }

  class LineType
  {
    Point p1 = new Point(), p2 = new Point();
  }
}
