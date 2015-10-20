package ua.sprites;

import java.awt.image.BufferedImage;

public class Glyph extends ImageSprite {
  public Font.Page page;
  public final double lw;
  public final double w1;
  public final double h1;
  public final double xoffset;
  public final double yoffset;
  public Glyph(String symbol, BufferedImage bi, int width, double logicalWidth, int height,
               double overbaseline, double moveright, double xoffset, double yoffset) {
    super(symbol, bi);
    type = Type.GLYPH;
    this.width = width;
    this.height = height;
    h1 = overbaseline;
    w1 = moveright;
    lw = logicalWidth;
    this.xoffset = xoffset;
    this.yoffset = yoffset;
  }
}
