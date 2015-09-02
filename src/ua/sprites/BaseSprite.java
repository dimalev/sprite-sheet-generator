package ua.sprites;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class BaseSprite extends Rectangle {
  public static enum Type { IMAGE, GLYPH };
  public Type type;
  public abstract void printToImage(BufferedImage output);

  public BaseSprite() { type = null; }
}
