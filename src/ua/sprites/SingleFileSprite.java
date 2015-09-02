package ua.sprites;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SingleFileSprite extends BaseSprite {
  public static SingleFileSprite fromFile(Path path) {
    BufferedImage img = null;
    try {
      img = ImageIO.read(new File(path.toString()));
      String fileName = path.getFileName().toString();
      int lastDot = fileName.lastIndexOf(".");
      return new SingleFileSprite(fileName.substring(0, lastDot), img);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  private String mName;
  private BufferedImage mImage;

  public String getName() { return mName; }

  public BufferedImage getImage() { return mImage; }

  public SingleFileSprite(String name, BufferedImage bi) {
    type = Type.IMAGE;
    mName = name;
    mImage = bi;
    width = bi.getWidth();
    height = bi.getHeight();
  }

  @Override
  public void printToImage(BufferedImage output) {
    output.getGraphics().drawImage(mImage, x, y, null);
  }
}
