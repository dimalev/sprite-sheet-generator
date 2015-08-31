package ua.sprites;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] argv) throws IOException {
		System.out.println("hello, lets start");
		
		Stream<Path> paths = null;
		
		if("-d".equals(argv[0])) {
			Path path = FileSystems.getDefault().getPath(argv[1]);
			paths = Files.list(path);
		}
		
		List<BufferedImage> list = paths.map(path -> {
			BufferedImage img = null;
			try {
				img = ImageIO.read(new File(path.toString()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return img;
		}).filter(img -> null != img)
		  //.sorted(new ByHeight())
		  .collect(Collectors.toList());

		Rectangle[] rects = new Rectangle[list.size()];
		Map<Rectangle, BufferedImage> mappings = new HashMap<>();
		int i = 0;
		for(BufferedImage img : list) {
			rects[i] = new Rectangle(0, 0, img.getWidth(), img.getHeight());
			++i;
		}
		Rectangle result = SpriteSheet.pack(rects);
		
		System.out.println("Count:" + list.size());
	}
}
