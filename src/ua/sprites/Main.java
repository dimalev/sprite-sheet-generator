package ua.sprites;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Main extends Task {
	
	protected String mAtlas;
	protected String mSprite;
	protected String mOutputDirectory;
	protected String mInputDirectory;
	
	public void setAtlas(String atlas) { mAtlas = atlas; }
	public void setSprite(String sprite) { mSprite = sprite; }
	public void setOutput(String output) { mOutputDirectory = output; }
	public void setInput(String input) { mInputDirectory = input; }	

  public static void main(String[] argv) throws IOException {
    System.out.println("hello, lets start");

    List<Path> paths = new LinkedList<>();
    Path atlasFile = null;
    Path fontFile = null;
    Path spriteFile = null;
    Path outputDirectory = null;

    boolean nextIsInput = false;
    boolean nextIsSpriteOut = false;
    boolean nextIsAtlasOut = false;
    boolean nextIsFontOut = false;
    boolean nextIsDirectory = false;

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
        nextIsSpriteOut = false;
        continue;
      }
      if(nextIsAtlasOut) {
        atlasFile = FileSystems.getDefault().getPath(in);
        nextIsAtlasOut = false;
        continue;
      }
      if(nextIsFontOut) {
        fontFile = FileSystems.getDefault().getPath(in);
        nextIsFontOut = false;
        continue;
      }
      if(nextIsDirectory) {
          outputDirectory = FileSystems.getDefault().getPath(in);
          nextIsDirectory = false;
          continue;
      }
      if("-i".equals(in)) nextIsInput = true;
      else if("-o".equals(in)) nextIsSpriteOut = true;
      else if("-x".equals(in)) nextIsAtlasOut = true;
      else if("-f".equals(in)) nextIsFontOut = true;
      else if("-d".equals(in)) nextIsDirectory = true;
    }
    
    List<BaseSprite> allSprites = new LinkedList<>();
    List<Path> used = new LinkedList<Path>();
    paths.stream()
    	.filter((path) -> !path.endsWith(".xml"))
    	.forEach((path) -> {
    		System.out.format("Reading as xml: %s%n", path.toString());
    		used.add(path);
    		try {
    			SpriteSheet sheet = XmlImageAtlas.load(path);
    			allSprites.addAll(sheet.getSprites());
        		System.out.format("Image file used: %s%n", sheet.atlasFile.toString());
    			used.add(sheet.atlasFile);
    		} catch(Exception e) {
    			System.out.format("Error parsing %s%n", path.toString());
    		}
    	});
    
    paths.removeAll(used);
    
    paths.stream().map(ImageSprite::fromFile).forEach(allSprites::add);
    
   	if(null != outputDirectory) {
   	    try {
    		for(BaseSprite bsprite : allSprites) {
    			ImageSprite sprite = (ImageSprite)bsprite;
    			ImageIO.write(sprite.getImage(), "png", new File(outputDirectory.toString(), sprite.getName() + ".png"));
    		}
        } catch(Exception ex) {
        	ex.printStackTrace();
        	System.err.format("Exception: %s%n", ex);
        }
   	}
    
    if(null != spriteFile || null != atlasFile) {
    	SpriteSheet sheet = new SpriteSheet();
    	sheet.addAll(allSprites);
    	sheet.xmlFile = atlasFile;
    	sheet.atlasFile = spriteFile;
    	sheet.pack();
    	
    	if(null != sheet.atlasFile) {
    		System.out.format("Writing image into %s%n", sheet.atlasFile.toString());
    		ImageIO.write(
    			sheet.getOutputImage(),
    			"png",
    			sheet.atlasFile.toFile() 
    		);
    	}

    	if(null != sheet.xmlFile) { 
    		System.out.format("Writing sheet xml into %s%n", sheet.xmlFile.toString());
    		XmlImageAtlas.save(
    			sheet.xmlFile, 
    			null == sheet.atlasFile ? "no file" : sheet.atlasFile.toString(), 
    			sheet
    		);
    	}
    }
  }
  
  public void execute() throws BuildException {
	    List<Path> paths = new LinkedList<>();
        Path inputPath = FileSystems.getDefault().getPath(mInputDirectory);
        if(Files.isDirectory(inputPath)) {
        	try {
        		paths.addAll(Files.list(inputPath).collect(Collectors.toList()));
    		} catch(IOException e) {
    			System.out.format("Error reading directory %s%n", inputPath.toString());
    			throw new BuildException();
    		}
        } else paths.add(inputPath);
  	
        List<BaseSprite> allSprites = new LinkedList<>();
        List<Path> used = new LinkedList<Path>();
        paths.stream()
        	.filter((path) -> path.endsWith(".xml"))
        	.forEach((path) -> {
        		System.out.format("Reading as xml: %s%n", path.toString());
        		used.add(path);
        		try {
        			SpriteSheet sheet = XmlImageAtlas.load(path);
        			allSprites.addAll(sheet.getSprites());
            		System.out.format("Image file used: %s%n", sheet.atlasFile.toString());
        			used.add(sheet.atlasFile);
        		} catch(Exception e) {
        			System.out.format("Error parsing %s%n", path.toString());
        		}
        	});
        
        paths.removeAll(used);
        
        paths.stream().map(ImageSprite::fromFile).forEach(allSprites::add);
        
       	if(null != mOutputDirectory) {
       	    try {
        		for(BaseSprite bsprite : allSprites) {
        			ImageSprite sprite = (ImageSprite)bsprite;
        			ImageIO.write(sprite.getImage(), "png", new File(mOutputDirectory, sprite.getName() + ".png"));
        		}
            } catch(Exception ex) {
            	ex.printStackTrace();
            	System.err.format("Exception: %s%n", ex);
            }
       	}
        
        if(null != mSprite || null != mAtlas) {
        	SpriteSheet sheet = new SpriteSheet();
        	sheet.addAll(allSprites);
        	sheet.xmlFile = FileSystems.getDefault().getPath(mAtlas);
        	sheet.atlasFile = FileSystems.getDefault().getPath(mSprite);
        	sheet.pack();
        	
        	if(null != sheet.atlasFile) {
        		System.out.format("Writing image into %s%n", sheet.atlasFile.toString());
        		try {
        			ImageIO.write(
        				sheet.getOutputImage(),
        				"png",
        				sheet.atlasFile.toFile() 
        			);
        		} catch(IOException e) {
        			System.out.format("Error wring atlas %s%n", sheet.atlasFile.toString());
        			throw new BuildException();
        		}
        	}

        	if(null != sheet.xmlFile) { 
        		System.out.format("Writing sheet xml into %s%n", sheet.xmlFile.toString());
        		XmlImageAtlas.save(
        			sheet.xmlFile, 
        			null == sheet.atlasFile ? "no file" : sheet.atlasFile.toString(), 
        			sheet
        		);
        	}
        }
  }
}
