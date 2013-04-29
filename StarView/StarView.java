/*
  StarView.java 1.0 02/03/26
  by W Travis Jones
 
  A simple starfield, but with the effect of a person looking out a space ship
  at warp speed, while spinning his body around.
 */

import java.awt.*;
import java.applet.*;


public class StarView extends Applet implements Runnable
{	
  private final int MAX_STARS = 1000;
  private final int MAX_STAR_X = 150;
  private final int MAX_STAR_Y = 100;
  private final int MAX_STAR_Z = 120;
  private final int MIN_STAR_X = -150;
  private final int MIN_STAR_Y = -100;
  private final int MIN_STAR_Z = -120;
  private final int STAR_SPEED = 2;
  private final int STOP_DELAY = 100;
  private final int H_SCALE = 256, V_SCALE = 256;

  double tcos[] = new double[360];  // math tables for speed
  double tsin[] = new double[360];
  private Image backImg = null;   //handle to the offscreen buffer
  Point3D stars[] = new Point3D[MAX_STARS];
  Point3D star = new Point3D();
  private int spin = 0, delay = 0;
  
  Thread mainThread;

	public void init() 
  {
    // setup tables
    for(int i = 0; i <= 359; i++)
    {
      tcos[i] = Math.cos(i * Math.PI / 180);
      tsin[i] = Math.sin(i * Math.PI / 180);
    }

    // allocate stars
    for (int i = 0; i < MAX_STARS; i++)
      stars[i] = new Point3D();

  
    for (int i = 0; i < MAX_STARS; i++)
    {
      resetStar(stars[i]);
      stars[i].z = MAX_STAR_Z - (int)(Math.random() * (MAX_STAR_Z - MIN_STAR_Z));
    }
    showStatus("Facing toward " + spin + " degrees!");
    mainThread = new Thread(this);
    mainThread.start();
	}

  public void update(Graphics g)
  { // override built in update() so Java doesn't erase client area, causing flicker
    paint(g);
  }

	public void paint(Graphics g) 
  {
    
    Graphics gback; 
    if (backImg == null) 
      backImg = createImage(getSize().width, getSize().height);
    gback =  backImg.getGraphics(); // get context

    gback.setColor(Color.black);
		gback.fillRect(0, 0, getSize().width, getSize().height);
    
    for (int i = 0; i < MAX_STARS; i++)
      drawProjectedStar(
        gback,
        rotatePoint(stars[i],0, spin, 0));
    
   
    
    g.drawImage(backImg,0,0,this);  //copy backbuffer to client area
	}
  
  public void run()
  {
    while (mainThread == Thread.currentThread())
    {
      for (int i = 0; i < MAX_STARS; i++)
      {
        stars[i].z -= STAR_SPEED;
        if (stars[i].z < MIN_STAR_Z)
        {
          resetStar(stars[i]);
          stars[i].z = MAX_STAR_Z;
        }
      }
      
      if (spin == 0 || spin == 90 || spin == 180 || spin == 270)
      {
        if (delay == 0)
        {
          delay = STOP_DELAY;
          showStatus("Facing toward " + spin + " degrees!");
        }
        else {delay--; if (delay == 0) showStatus("Spinning!!");}
      }
      
      if (delay == 0)
        spin++; spin = spin % 359;
      repaint();
      try {
        mainThread.sleep(30);
      } catch (InterruptedException e) {}
    }
  }

  void resetStar(Point3D star)
  {
    star.x = MAX_STAR_X - (int)(Math.random() * (MAX_STAR_X - MIN_STAR_X)) ;
    star.y = MAX_STAR_Y - (int)(Math.random() * (MAX_STAR_Y - MIN_STAR_Y)); 
  }

  void drawProjectedStar(Graphics g, Point3D star)
  {
    int vidX, vidY;
    int shade = 0;
  
    if (star.z < 1) return;   // divide by Z is bad

    // get a shade of grey depending on distance
    shade = (int)((256/MAX_STAR_Z) * star.z);
    shade = 256 - shade;
    shade += 55;    // brighten it up a bit
    if (shade < 0) shade = 0;
    if (shade > 255) shade = 255;
    
    g.setColor(new Color(shade, shade, shade));    
    vidX = (int)((H_SCALE * star.x) / star.z);
    vidX += (int)(getSize().width / 2);
    vidY = (int)((H_SCALE * star.y) / star.z);
    vidY += (int)(getSize().height / 2);
    
    //draw it bigger if closer
    if (star.z < 30)
      g.drawRect(vidX, vidY, 1, 1); 
    else g.drawRect(vidX, vidY, 0, 0);
  }

  Point3D rotatePoint (Point3D point, int xDeg, int yDeg, int zDeg)
  {
    //rotate a point in 3d, I ripped this directly from an old basic program
    //of mine

    Point3D temp = new Point3D();
    Point3D rotated = new Point3D();

    rotated.x = point.x; rotated.y = point.y; rotated.z = point.z; 
    //--DO X AXIS
    //-rotate on x axis-
    //x is unaltered
    temp.y = tcos[xDeg] * rotated.y - tsin[xDeg] * rotated.z;
    temp.z = tsin[xDeg] * rotated.y + tcos[xDeg] * rotated.z;
 
    //-put new values into translated array-
    //x is unaltered
    rotated.y = temp.y;
    rotated.z = temp.z;


    //--DO Y AXIS
    //-rotate on y axis-
    temp.x = tcos[yDeg] * rotated.x + tsin[yDeg] * rotated.z;
    //y is unaltered
    temp.z = (-tsin[yDeg]) * rotated.x + tcos[yDeg] * rotated.z;
 
    //-put new values into translated array-
    rotated.x = temp.x;
    //y is unaltered
    rotated.z = temp.z;


    //--DO Z AXIS
    //-rotate on z axis-
    temp.x = tcos[zDeg] * rotated.x - tsin[zDeg] * rotated.y;
    temp.y = tsin[zDeg] * rotated.x + tcos[zDeg] * rotated.y;
    //z is unaltered
 
    //-put new values into translated array-
    rotated.x = temp.x;
    rotated.y = temp.y;
    //z is unaltered
    
    return rotated;
  }
}

class Point3D
{
  public double x, y, z;
}



