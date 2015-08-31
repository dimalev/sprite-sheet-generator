package ua.sprites;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class SpriteSheet {
	public static Rectangle pack(Rectangle[] rects) {
		int minWidth = 0, minHeight = 0, totalArea = 0;
		int rectCount = rects.length;
		for(int i = 0; i < rectCount; ++i ) {
			minWidth = Math.max(minWidth, rects[i].width);
			minHeight = Math.max(minHeight, rects[i].height);
			totalArea += rects[i].width * rects[i].height;
		}
		
		int minSide = Math.max(Math.max(minWidth, minHeight), (int)Math.ceil(Math.sqrt(totalArea)));
		
		int rectSide = 2;
		while(rectSide < minSide) rectSide *= 2;
		
		while(!packInRectangle(rects, new Rectangle(0,0, rectSide, rectSide))) rectSide *= 2;
		
		return new Rectangle(0, 0, rectSide, rectSide);
	}
	
	public static boolean packInRectangle(Rectangle[] rects, Rectangle target) {
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
	
	static class ByHeightFirst implements Comparator<Rectangle> {
		public int compare(Rectangle left, Rectangle right) {
			if(left.height > right.height) return 1;
			if(left.height < right.height) return -1;
			if(left.width > right.width) return 1;
			if(left.width < right.width) return -1;
			return 0;
		}
		
	}
}
