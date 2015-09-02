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
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class Main {

  public static void main(String[] argv) throws IOException {
    System.out.println("hello, lets start");

    List<Path> paths = new LinkedList<>();
    String xmlAtlasOut = null;
    Path atlasFile = null;
    Path fontFile = null;
    Path spriteFile = null;

    boolean nextIsInput = false;
    boolean nextIsSpriteOut = false;
    boolean nextIsAtlasOut = false;
    boolean nextIsFontOut = false;

    for(String in : argv) {
      if(nextIsInput) {
        Path path = FileSystems.getDefault().getPath(in);
        if(Files.isDirectory(path)) {
          paths.addAll(Files.list(path).collect(Collectors.toList()));
        } else paths.add(path);
        nextIsInput = false;
        continue;
      }
      if(nextIsSpriteOut) {
        spriteFile = FileSystems.getDefault().getPath(in);
        continue;
      }
      if(nextIsAtlasOut) {
        atlasFile = FileSystems.getDefault().getPath(in);
        continue;
      }
      if(nextIsFontOut) {
        fontFile = FileSystems.getDefault().getPath(in);
        continue;
      }
      if("-i".equals(in)) nextIsInput = true;
      else if("-o".equals(in)) nextIsSpriteOut = true;
      else if("-x".equals(in)) nextIsAtlasOut = true;
      else if("-f".equals(in)) nextIsFontOut = true;
    }

    try {
      SpriteSheet sheet = XmlImageAtlas.load(paths.get(0));
      for(BaseSprite bsprite : sheet.getSprites()) {
        SingleFileSprite sprite = (SingleFileSprite)bsprite;
        ImageIO.write(sprite.getImage(), "png", new File(atlasFile.toString(), sprite.getName() + ".png"));
      }
    } catch(Exception ex) {
      ex.printStackTrace();
      System.err.format("Exception: %s%n", ex);
    }

    // SpriteSheet sheet = new SpriteSheet();

    // paths
    //   .map(SingleFileSprite::fromFile)
    //   .filter((out) -> null != out)
    //   .forEach(sheet::add);

    // sheet.pack();

    // ImageIO.write(sheet.getOutputImage(), "png", new File("sheet.png"));
    // if(null != atlasFile) XmlImageAtlas.save(atlasFile, "sheet.png", sheet);
  }
}
