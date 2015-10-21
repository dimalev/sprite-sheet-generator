package ua.sprites;

import java.nio.file.Path;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class SpriteSheet {
  protected List<BaseSprite> mSprites = new ArrayList<BaseSprite>();

  protected Rectangle mBoundingBox = new Rectangle();

  public Path xmlFile;
  public Path atlasFile;
  
  public int padding = 0;

  public List<BaseSprite> getSprites() { return mSprites; }
  
  public int getWidth() { return mBoundingBox.width; }
  public int getHeight() { return mBoundingBox.height; }

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

    int top = padding, nextTop = padding, left = padding, count = rects.length, maxHeight = 0, smallest = 0;
    ArrayList<Rectangle> smallers = new ArrayList<>(16);
    for(int i = 0; i < count - smallest; ++i) {
      if(left + rects[i].width + padding > target.width) {
    	if(target.width - left > padding) smallers.add(new Rectangle(left, top, target.width - left, maxHeight));
        top = nextTop;
        left = padding;
        maxHeight = 0;
        int leftSmall = smallers.size() - 1;
    	Collections.reverse(smallers);
        while(leftSmall >= 0 && i <= count - smallest - 1) {
        	if(smallers.get(leftSmall).height < rects[count - smallest - 1].height + padding) {
        		--leftSmall;
        		continue;
        	}
        	int widthSoFar = 0;
        	Rectangle startRect = new Rectangle(smallers.get(leftSmall));
        	while(leftSmall >= 0) {
        		widthSoFar += smallers.get(leftSmall).width;
        		if(widthSoFar >= rects[count - smallest - 1].width + padding) {
        			rects[count - smallest - 1].x = startRect.x;
        			rects[count - smallest - 1].y = startRect.y;
        			int newWidth = widthSoFar - (rects[count - smallest - 1].width + padding);
        			smallers.get(leftSmall).x += smallers.get(leftSmall).width - newWidth;
        			smallers.get(leftSmall).width = newWidth;
        			if(smallers.get(leftSmall).width <= padding) --leftSmall;
        			if(startRect.height - (rects[count - smallest - 1].height + padding) > padding) {
        				++leftSmall;
        				Rectangle extraRect = new Rectangle(
								rects[count - smallest - 1].x, 
								rects[count - smallest - 1].y + rects[count - smallest - 1].height + padding,
								rects[count - smallest - 1].width + padding,
								startRect.height - (rects[count - smallest - 1].height + padding)
								);
        				if(leftSmall < smallers.size()) smallers.set(leftSmall, extraRect);
        				else smallers.add(extraRect);
        			}
        			++smallest;
        			break;
        		}
        		--leftSmall;
        	}
        }
        smallers.clear();
      }
      //if(i >= count - smallest - 1) break;
      rects[i].x = left; rects[i].y = top;
      nextTop = Math.max(nextTop, top + rects[i].height + padding);
      if(nextTop > target.height) return false;
      if(rects[i].height > maxHeight) maxHeight = rects[i].height;
      else {
    	  if(maxHeight - rects[i].height - padding > padding) 
    		  smallers.add(
    				  new Rectangle(
    						  rects[i].x, 
    						  rects[i].y + rects[i].height + padding, 
    						  rects[i].width + padding, 
    						  maxHeight - rects[i].height - padding
    						  )
    				  );
      }
      left += rects[i].width + padding;
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
