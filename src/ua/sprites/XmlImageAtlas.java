package ua.sprites;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.FileSystems;
import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.awt.image.BufferedImage;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.imageio.ImageIO;

public class XmlImageAtlas extends DefaultHandler {
  public static final String XML_VERSION = "<?xml version=\"1.0\"?>";
  public static final String HEADER = "<TextureAtlas imagePath=\"%s\">";
  public static final String TEXTURE = "  <SubTexture name=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" />";
  public static final String FOOTER = "</TextureAtlas>";

  public static SpriteSheet load(Path xmlFilePath)
    throws ParserConfigurationException, SAXException, IOException
  {
    SpriteSheet sheet = new SpriteSheet();
    return load(xmlFilePath, sheet);
  }

  public static SpriteSheet load(Path xmlFilePath, SpriteSheet sheet)
    throws ParserConfigurationException, SAXException, IOException
  {
    sheet.xmlFile = xmlFilePath;
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();
    XMLReader xmlReader = saxParser.getXMLReader();
    xmlReader.setContentHandler(new XmlImageAtlas(sheet));
    xmlReader.parse(xmlFilePath.toString());
    return sheet;
  }

  public static void save(Path filePath, String atlasFileName, SpriteSheet sheet) {
    try(BufferedWriter writer = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"))) {
      String s;

      writer.write(XML_VERSION, 0, XML_VERSION.length());
      writer.newLine();

      s = String.format(HEADER, atlasFileName);
      writer.write(s, 0, s.length());
      writer.newLine();

      for(BaseSprite sprite : sheet.getSprites()) {
        if(sprite.type != BaseSprite.Type.IMAGE) continue;
        s = String.format(
          TEXTURE,
          ((ImageSprite)sprite).getName(),
          sprite.x,
          sprite.y,
          sprite.width,
          sprite.height
        );
        writer.write(s, 0, s.length());
        writer.newLine();
      }

      writer.write(FOOTER, 0, FOOTER.length());
      writer.newLine();
    } catch(IOException x) {
      System.err.format("IOException: %s%n", x);
    }
  }

  protected SpriteSheet mSheet;
  protected BufferedImage mAtlasImage;
  protected boolean mIsInsideAtlas = false;
  protected boolean mIsInsideSubTexture = false;

  protected XmlImageAtlas(SpriteSheet sheet) {
    mSheet = sheet;
  }

  public void startElement(String uri, String localName, String qName, Attributes attrs) {
    if("TextureAtlas".equals(qName)) atlasStart(attrs);
    else if("SubTexture".equals(qName)) subTextureStart(attrs);
  }

  public void endElement(String uri, String localName, String qName) {
    if("TextureAtlas".equals(qName)) atlasEnd();
    else if("SubTexture".equals(qName)) subTextureEnd();
  }

  protected void subTextureStart(Attributes attrs) {
    mIsInsideSubTexture = true;
    String name = attrs.getValue("name");
    int x = Integer.parseInt(attrs.getValue("x"));
    int y = Integer.parseInt(attrs.getValue("y"));
    int width = Integer.parseInt(attrs.getValue("width"));
    int height = Integer.parseInt(attrs.getValue("height"));
    ImageSprite sprite = new ImageSprite(name, mAtlasImage.getSubimage(x, y, width, height));
    mSheet.add(sprite);
  }

  protected void atlasStart(Attributes attrs) {
    mIsInsideAtlas = true;
    String path = attrs.getValue("imagePath");
    mSheet.atlasFile = FileSystems.getDefault().getPath(mSheet.xmlFile.getParent().toString(), path);

    try {
      mAtlasImage = ImageIO.read(new File(mSheet.atlasFile.toString()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void subTextureEnd() {
    mIsInsideSubTexture = false;
  }

  protected void atlasEnd() {
    mIsInsideAtlas = false;
  }
}
