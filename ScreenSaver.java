import hsa2.GraphicsConsole;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;

public class ScreenSaver
{
  public static final int timeSinceInput = 1000; //ms
  public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  public static int screenWidth = screenSize.width;
  public static int screenHeight = screenSize.height;
  public static long timeAtLastInput = 0;
  public static boolean screenSaving = true;

  public static Image background;
  public static Cursor blankCursor;
  public static BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

  public static final boolean debug = true;
  public static final boolean blackAndwhite = false;

  public static final int numberOfParticles = 1000;
  public static final int scale = 120;
  public static final double xincrement = 0.05;
  public static final double yincrement = 0.05;
  public static final double zincrement = 0.008;
  public static final int maxSpeed = 5;
  public static final int minSpeed = 5;
  public static final int maxStroke = 5;
  public static final int minStroke = 5;
  public static final int angleDistributionFactor = 2;
  public static final int fieldVectorsForce = 4;


  public static Particle[] particles = new Particle[numberOfParticles];

  public static Vector[][] fieldVectors = new Vector[(int)Math.floor(screenWidth / scale) + 1]
                                                      [(int)Math.floor(screenHeight / scale) + 1];

  public static double xOffset;
  public static double yOffset;
  public static double zOffset;

  public static GraphicsConsole gc = new GraphicsConsole(screenWidth, screenHeight,
                                                         "Screen Saver");

  public static void main(String[] args)
  {
    gc.enableMouse();
    gc.enableMouseMotion();
    gc.enableMouseWheel();
    gc.setAntiAlias(true);
    gc.setBackgroundColor(new Color(60,60,80));
    gc.setColor(new Color(0,0,0,5));
    gc.clear();

    initialize();

    blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
    cursorImg, new Point(0, 0), "blank cursor");

    try
    {
      background = ImageIO.read(new File("C:/Users/danie/Desktop/code/computer science class/grade 12 compsci/HSA2 Programes/Perlin_noise_ScreenSaver/src/windowsXP.jpg"));
    }
    catch(IOException e)
    {
      System.out.println("error: background image not found");
      System.exit(1);
    }

    background = background.getScaledInstance(screenWidth, screenHeight, Image.SCALE_DEFAULT);

    while(!gc.mouseTracking)
    {
      gc.sleep(1);
    }

    checkForUserInput();

  }

  public static void checkForUserInput()
  {

    while(true)
    {
      if(gc.isKeyDown(GraphicsConsole.VK_ESCAPE))
      {
        gc.close();
        System.exit(0);
      }
      if(gc.mouseWasMoved || gc.canvas.keyboardInput || gc.getMouseWheelRotation() > 0)
      {
        gc.mouseWasMoved = false;
        gc.canvas.keyboardInput = false;
        if(screenSaving)
        {
          gc.requestFocus();
          gc.getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          gc.clear();
          gc.drawImage(background, 0, 0);
        }
        screenSaving = false;
        timeAtLastInput = System.currentTimeMillis();
      }
      else if(timeAtLastInput + timeSinceInput < System.currentTimeMillis())
      {
        if(!screenSaving)
        {
          screenSave();
        }
      }
      if(screenSaving)
      {
        screenSaveAnimation();
      }
      gc.sleep(32);
    }

  }

  public static void screenSaveAnimation()
  {
    synchronized(gc)
    {
      if(debug)
      {
        gc.clear();
        updateField();
        drawField();
      }
      else
      {
        updateField();
        updateParticles();
        renderParticles();
      }
    }
  }

  public static void screenSave()
  {
    screenSaving = true;
    initialize();
    gc.clear();
    gc.requestFocus();
    gc.getContentPane().setCursor(blankCursor);

  }

  public static void initialize()
  {
    initializeField();
    initializeParticles();

  }

  public static void initializeField()
  {
    xOffset = Math.random()*1000000;
    yOffset = Math.random()*1000000;
    zOffset = Math.random()*1000000;

    for(int i = 0; i < (int)Math.floor(screenWidth/scale) + 1; i++)
    {
      for(int j = 0; j < (int)Math.floor(screenHeight/scale) + 1; j++)
      {
        fieldVectors[i][j] = new Vector(0,0);
      }
    }

  }

  public static void initializeParticles()
  {
    for(int i = 0; i < numberOfParticles; i++)
    {
      particles[i] = new Particle(Math.random()*screenWidth,
                                  Math.random()*screenHeight,
                                  Math.random()* (maxSpeed - minSpeed) + minSpeed,
                                  (int)Math.round(Math.random()*(maxStroke - minStroke) + minStroke),
                                  new Color((int)Math.round(Math.random()*255),
                                            (int)Math.round(Math.random()*255),
                                            (int)Math.round(Math.random()*255)));

    }

  }

  public static void updatePrevPosition(int i)
  {
    particles[i].lastX = particles[i].x;
    particles[i].lastY = particles[i].y;

  }

  public static void drawField()
  {
    gc.setStroke(5);
    for(int i = 0; i < (int)Math.floor(screenWidth/scale) + 1; i++)
    {
      for(int j = 0; j < (int)Math.floor(screenHeight/scale) + 1; j++)
      {
        gc.setColor(new Color(255,0,0));
        gc.drawOval(i*scale, j*scale, 1, 1);
        gc.setColor(new Color(0,0,0));
        gc.drawLine(i*scale, j*scale,
                    i*scale + (int)fieldVectors[i][j].x * 4,
                    j*scale + (int)fieldVectors[i][j].y * 4);

      }

    }

  }

  public static void updateField()
  {
    double angle;
    double tempxOffset = xOffset;
    double tempyOffset = yOffset;
    for(int i = 0; i < (int)Math.floor(screenWidth/scale) + 1; i++)
    {
      tempyOffset = yOffset;
      for(int j = 0; j < (int)Math.floor(screenHeight/scale) + 1; j++)
      {
        angle = PerlinNoise.noise(tempxOffset, tempyOffset, zOffset) * Math.PI * 2 * angleDistributionFactor;
        fieldVectors[i][j].x = Math.cos(angle) * fieldVectorsForce;
        fieldVectors[i][j].y = Math.sin(angle) * fieldVectorsForce;
        tempyOffset += xincrement;
      }
      tempxOffset += yincrement;
    }
    zOffset += zincrement;
  }

  public static void updateParticles()
  {
    double x = 0;
    double y = 0;

    for(int i = 0; i < numberOfParticles; i++)
    {
      x = fieldVectors[(int)Math.abs(Math.floor(particles[i].x/scale))]
                      [(int)Math.abs(Math.floor(particles[i].y/scale))].x;

      y = fieldVectors[(int)Math.abs(Math.floor(particles[i].x/scale))]
                      [(int)Math.abs(Math.floor(particles[i].y/scale))].y;

      particles[i].velX += x;
      particles[i].velX = x / Math.sqrt(x*x + y*y) * particles[i].speed;
      particles[i].velY += y;
      particles[i].velY = y / Math.sqrt(x*x + y*y) * particles[i].speed;

      particles[i].x += particles[i].velX;
      wrapParticles(i);
      particles[i].y += particles[i].velY;
      wrapParticles(i);

    }

  }

  public static void renderParticles()
  {
    for(int i = 0; i < numberOfParticles; i++)
    {
      if(!blackAndwhite)
      {
        gc.setColor(particles[i].color);
      }
      gc.setStroke(particles[i].stroke);
      gc.drawLine((int)Math.round(particles[i].x), (int)Math.round(particles[i].y),
                  (int)Math.round(particles[i].lastX), (int)Math.round(particles[i].lastY));
      updatePrevPosition(i);
    }
  }

  public static void wrapParticles(int i)
  {
    if(particles[i].x > screenWidth)
    {
      particles[i].x = 0;
      updatePrevPosition(i);
    }
    else if(particles[i].x < 0)
    {
      particles[i].x = screenWidth;
      updatePrevPosition(i);
    }
    if(particles[i].y > screenHeight)
    {
      particles[i].y = 0;
      updatePrevPosition(i);
    }
    else if(particles[i].y < 0)
    {
      particles[i].y = screenHeight;
      updatePrevPosition(i);
    }

  }

}
