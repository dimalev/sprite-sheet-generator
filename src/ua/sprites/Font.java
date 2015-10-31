package ua.sprites;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import java.nio.file.Path;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Font {
  public String face;
  public double size;
  public boolean bold = false;
  public boolean italic = false;
  public double stretchH = 100;
  public boolean smooth = false;
  public boolean aa = false;
  public Rectangle2D.Double padding = new Rectangle2D.Double();

  public double lineHeight;
  public double base;
  public double scaleW;
  public double scaleH;
  public boolean packed = false;

  public Path fntPath;

  public List<Page> pages = new LinkedList<>();
  public List<Kerning> kernings = new ArrayList<>();

  public static class Page {
    public int id;
    public Path imagePath;
    public BufferedImage image;

    public List<Glyph> glyphs = new LinkedList<Glyph>();

    public Page() {
    }
  }

  public Font() {
  }

  public List<Glyph> getGlyphs() {
    List<Glyph> allGlyphs = new LinkedList<Glyph>();
    for(Page page : pages)
        allGlyphs.addAll(page.glyphs);
    return allGlyphs;
  }

  public Page getPage(int id) {
    for(Page p : pages) if(p.id == id) return p;
    return null;
  }

  public Glyph getGlyph(String letter) {
    return null;
  }
}
