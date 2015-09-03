package ua.sprites;

import java.nio.file.Path;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class SpriteSheet {
  protected List<BaseSprite> mSprites = new ArrayList<BaseSprite>();

  protected Rectangle mBoundingBox = new Rectangle();

  public Path xmlFile;
  public Path atlasFile;

  public List<BaseSprite> getSprites() { return mSprites; }

  public void addAll(Collection<BaseSprite> sprites) {
    mSprites.addAll(sprites);
  }

  public void add(BaseSprite sprite) { mSprites.add(sprite); }

  public void pack() {
    int minWidth = 0, minHeight = 0, totalArea = 0;
    int rectCount = mSprites.size();
    System.out.println("packing " + rectCount + " sprites");
    for(BaseSprite sprite : mSprites) {
      minWidth = Math.max(minWidth, sprite.width);
      minHeight = Math.max(minHeight, sprite.height);
      totalArea += sprite.width * sprite.height;
    }

    int minSide = Math.max(Math.max(minWidth, minHeight), (int)Math.ceil(Math.sqrt(totalArea)));

    int rectSide = 2;
    while(rectSide < minSide) rectSide *= 2;

    Rectangle[] rects = mSprites.stream().map((sprite) -> (Rectangle) sprite)
      .collect(Collectors.toList()).toArray(new Rectangle[mSprites.size()]);
    while(!packInRectangle(rects, new Rectangle(0,0, rectSide, rectSide))) rectSide *= 2;

    mBoundingBox = new Rectangle(0, 0, rectSide, rectSide);
  }

  public boolean packInRectangle(Rectangle[] rects, Rectangle target) {
    Arrays.sort(rects, new ByHeightFirst());

    int top = 0, nextTop = 0, left = 0, count = rects.length;
    for(int i = 0; i < count; ++i) {
      if(left + rects[i].width > target.width) {
        top = nextTop;
        left = 0;
      }
      rects[i].x = left; rects[i].y = top;
      nextTop = Math.max(nextTop, top + rects[i].height);
      if(nextTop > target.height) return false;
      left += rects[i].width;
    }
    return true;
  }

  public BufferedImage getOutputImage() {
    BufferedImage sheet = new BufferedImage(mBoundingBox.width, mBoundingBox.height, BufferedImage.TYPE_INT_ARGB);
    for(BaseSprite sprite : mSprites) {
      sprite.printToImage(sheet);
    }
    sheet.flush();
    return sheet;
  }

  static class ByHeightFirst implements Comparator<Rectangle> {
    public int compare(Rectangle left, Rectangle right) {
      if(left.height > right.height) return -1;
      if(left.height < right.height) return 1;
      if(left.width > right.width) return -1;
      if(left.width < right.width) return 1;
      return 0;
    }
  }
}
