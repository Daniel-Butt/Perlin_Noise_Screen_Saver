import java.awt.Color;

public class Particle
{
  public double x, y, lastX, lastY, velX, velY, speed;
  public int stroke;
  public Color color;

  Particle(double x, double y, double speed, int stroke, Color color)
  {
    this.x = x;
    this.y = y;
    lastX = x;
    lastY = y;
    this.speed = speed;
    velX = 0;
    velY = 0;
    this.stroke = stroke;
    this.color = color;
  }

}
