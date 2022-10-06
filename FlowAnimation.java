import hsa2.GraphicsConsole;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Color;

public class FlowAnimation extends PerlinNoise
{
  public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  public static int screenWidth = screenSize.width;
  public static int screenHeight = screenSize.height;

  public static GraphicsConsole gc = new GraphicsConsole(screenWidth, screenHeight, "Flow");

  public static final int numberOfPoints = 1000;
  public static double[][] points = new double[numberOfPoints][4];

  public static double[][][] fieldVectors = new double[screenWidth + 1][screenHeight + 1][2];

  public static final double increment = 0.001;
  public static double xOffset;
  public static double yOffset;
  public static double zOffset;

  public static void main(String[] args)
  {
    gc.setAntiAlias(true);
    gc.setBackgroundColor(new Color(80,80,80));
    gc.setColor(new Color(0,0,0,20));
    gc.setStroke(2);
    gc.clear();

    xOffset = Math.random()*10000;
    xOffset = Math.random()*10000;
    zOffset = Math.random()*10000;

    for(int i = 0; i < numberOfPoints; i++)
    {
      points[i][0] = (int)Math.round(Math.random()*screenWidth);
      points[i][1] = (int)Math.round(Math.random()*screenHeight);
      points[i][2] = points[i][0];
      points[i][3] = points[i][1];
    }

    drawLoop();

  }

  public static void drawLoop()
  {
    while(true)
    {
      gc.sleep(1);
      if(gc.isKeyDown(GraphicsConsole.VK_ESCAPE))
      {
        gc.close();
        System.exit(0);
      }

      synchronized(gc)
      {
        updateField();
        updatePoints();
        renderPoints();
      }

    }
  }

  public static void updatePrevPosition(int index)
  {
    points[index][2] = points[index][0];
    points[index][3] = points[index][1];

  }

  public static void updateField()
  {
    double angle;
    for(int i = 0; i < screenWidth + 1; i++)
    {
      for(int j = 0; j < screenHeight + 1; j++)
      {
        angle = noise(xOffset, yOffset, zOffset) * Math.PI * 8;
        fieldVectors[i][j][0] = Math.cos(angle) * 8;
        fieldVectors[i][j][1] = Math.sin(angle) * 8;
        yOffset += increment;
      }
      xOffset += increment;
    }
    zOffset += increment;
  }

  public static void updatePoints()
  {
    for(int i = 0; i < numberOfPoints; i++)
    {
      points[i][0] += fieldVectors[(int)Math.abs(Math.floor(points[i][0]))][(int)Math.abs(Math.floor(points[i][1]))][0];
      wrapPoints();
      points[i][1] += fieldVectors[(int)Math.abs(Math.floor(points[i][0]))][(int)Math.abs(Math.floor(points[i][1]))][1];
      wrapPoints();
    }

  }

  public static void renderPoints()
  {
    for(int i = 0; i < numberOfPoints; i++)
    {
      gc.drawLine((int)Math.round(points[i][0]), (int)Math.round(points[i][1]), (int)Math.round(points[i][2]), (int)Math.round(points[i][3]));
      updatePrevPosition(i);
    }
  }

  public static void wrapPoints()
  {
    for(int i = 0; i < numberOfPoints; i++)
    {
      if(points[i][0] >= screenWidth)
      {
        points[i][0] = 0;
        updatePrevPosition(i);
      }
      else if(points[i][0] <= 0)
      {
        points[i][0] = screenWidth;
        updatePrevPosition(i);
      }
      if(points[i][1] >= screenHeight)
      {
        points[i][1] = 0;
        updatePrevPosition(i);
      }
      else if(points[i][1] <= 0)
      {
        points[i][1] = screenHeight;
        updatePrevPosition(i);
      }

    }

  }

}
