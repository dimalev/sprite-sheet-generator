package ua.sprites;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.awt.image.BufferedImage;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.imageio.ImageIO;

public class FntSaver extends DefaultHandler {
  protected static final String FONT_START = "<font>";
  protected static final String INFO =
    "  <info face=\"%s\" size=\"%.0f\" bold=\"%d\" italic=\"%d\" charset=\"%s\" " +
    "unicode=\"%d\" stretchH=\"%d\" smooth=\"%d\" aa=\"%d\" padding=\"%d,%d,%d,%d\" spacing=\"%d,%d\" />";
  protected static final String COMMON =
    "  <common lineHeight=\"%.0f\" base=\"%.0f\" scaleW=\"%.0f\" scaleH=\"%.0f\" pages=\"%d\" packed=\"%d\" />";
  protected static final String PAGES_START = "  <pages>";
  protected static final String PAGE = "    <page id=\"%d\" file=\"%s\" />";
  protected static final String PAGES_END = "  </pages>";
  protected static final String CHARS = "chars count=%d";
  protected static final String CHARS_START = "  <chars>";
  protected static final String CHAR =
    "    <char id=\"%d\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" xoffset=\"%.0f\" yoffset=\"%.0f\" " +
    "xadvance=\"%.0f\" page=\"%d\" chnl=\"%d\" letter=\"%s\" />";
  protected static final String CHARS_END = "  </chars>";
  protected static final String KERNINGS_START = "  <kernings>";
  protected static final String KERNING =
    "    <kerning first=\"%d\" second=\"%d\" amount=\"%.0f\" />";
  protected static final String KERNINGS_END = "  </kernings>";
  protected static final String FONT_END = "</font>";

  public static void save(Path filePath, Font fnt) {
    List<Glyph> glyphs = fnt.getGlyphs();
    List<Kerning> kernings = fnt.kernings;
    try(BufferedWriter writer = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"))) {
      writer.write(FONT_START, 0, FONT_START.length());
      writer.newLine();
      String s = String.format(INFO, fnt.face, fnt.size, 0, 0, "", 0, 100, 1, 1, 0,0,0,0,0,0);
      writer.write(s, 0, s.length());
      writer.newLine();
      s = String.format(COMMON, fnt.lineHeight, fnt.base, fnt.scaleW, fnt.scaleH, 1, 0);
      writer.write(s, 0, s.length());
      writer.newLine();
      writer.write(PAGES_START, 0, PAGES_START.length());
      writer.newLine();
      s = String.format(PAGE, 0, fnt.pages.get(0).imagePath.toString());
      writer.write(s, 0, s.length());
      writer.newLine();
      writer.write(PAGES_END, 0, PAGES_END.length());
      writer.newLine();
      // s = String.format(CHARS, glyphs.size());
      // writer.write(s, 0, s.length());
      // writer.newLine();
      writer.write(CHARS_START, 0, CHARS_START.length());
      writer.newLine();
      for(Glyph cc : glyphs) {
        s = String.format(
            CHAR, cc.id,
            cc.x, cc.y, cc.width, cc.height, cc.xoffset, cc.yoffset, cc.w1, 0, 0, toLetter(cc.getName())
        );
        writer.write(s, 0, s.length());
        writer.newLine();
      }
      writer.write(CHARS_END, 0, CHARS_END.length());
      writer.newLine();
      writer.write(KERNINGS_START, 0, KERNINGS_START.length());
      writer.newLine();
      for(Kerning k : kernings) {
        s = String.format(KERNING, k.left, k.right, k.kerning);
        writer.write(s, 0, s.length());
        writer.newLine();
      }
      writer.write(KERNINGS_END, 0, KERNINGS_END.length());
      writer.newLine();
      writer.write(FONT_END, 0, FONT_END.length());
      writer.newLine();
    } catch(IOException x) {
      System.err.format("IOException: %s%n", x);
    }
  }

  public static Font load(Path fntFilePath, Font fnt)
    throws ParserConfigurationException, SAXException, IOException
  {
    fnt.fntPath = fntFilePath;
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();
    XMLReader xmlReader = saxParser.getXMLReader();
    xmlReader.setContentHandler(new FntSaver(fnt));
    xmlReader.parse(fntFilePath.toString());
    return fnt;
  }

  protected Font mFont;
  protected BufferedImage mAtlasImage;
  protected int mPageEstimate;
  protected boolean mIsInsideFont = false;
  protected boolean mIsInsideInfo = false;
  protected boolean mIsInsideCommon = false;
  protected boolean mIsInsidePages = false;
  protected boolean mIsInsidePage = false;
  protected boolean mIsInsideChars = false;
  protected boolean mIsInsideChar = false;
  protected boolean mIsInsideKernings = false;
  protected boolean mIsInsideKerning = false;

  protected FntSaver(Font font) {
    mFont = font;
  }

  public void startElement(String uri, String localName, String qName, Attributes attrs) {
    if("font".equals(qName)) fontStart(attrs);
    else if("info".equals(qName)) infoStart(attrs);
    else if("common".equals(qName)) commonStart(attrs);
    else if("pages".equals(qName)) pagesStart(attrs);
    else if("page".equals(qName)) pageStart(attrs);
    else if("chars".equals(qName)) charsStart(attrs);
    else if("char".equals(qName)) charStart(attrs);
    else if("kernings".equals(qName)) kerningsStart(attrs);
    else if("kerning".equals(qName)) kerningStart(attrs);
  }

  public void endElement(String uri, String localName, String qName) {
    if("font".equals(qName)) fontEnd();
    else if("info".equals(qName)) infoEnd();
    else if("common".equals(qName)) commonEnd();
    else if("pages".equals(qName)) pagesEnd();
    else if("page".equals(qName)) pageEnd();
    else if("chars".equals(qName)) charsEnd();
    else if("char".equals(qName)) charEnd();
    else if("kernings".equals(qName)) kerningsEnd();
    else if("kerning".equals(qName)) kerningEnd();
  }

  protected void fontStart(Attributes attrs) {
    mIsInsideFont = true;
  }

  protected void infoStart(Attributes attrs) {
    mIsInsideInfo = true;

    mFont.face = attrs.getValue("face");
    mFont.size = Double.parseDouble(attrs.getValue("size"));
    mFont.bold = "1".equals(attrs.getValue("bold"));
    mFont.italic = "1".equals(attrs.getValue("italic"));
    mFont.smooth = "1".equals(attrs.getValue("smooth"));
    mFont.aa = "1".equals(attrs.getValue("aa"));
    mFont.stretchH = Double.parseDouble(attrs.getValue("stretchH"));

    String padding[] = attrs.getValue("padding").split(",");
    mFont.padding.x = Double.parseDouble(padding[0]);
    mFont.padding.y = Double.parseDouble(padding[1]);
    mFont.padding.width = Double.parseDouble(padding[2]);
    mFont.padding.height = Double.parseDouble(padding[3]);
  }

  protected void infoEnd() {
    mIsInsideInfo = false;
  }

  protected void commonStart(Attributes attrs) {
    mIsInsideCommon = true;

    mFont.lineHeight = Double.parseDouble(attrs.getValue("lineHeight"));
    mFont.base = Double.parseDouble(attrs.getValue("base"));
    mFont.scaleW = Double.parseDouble(attrs.getValue("scaleW"));
    mFont.scaleH = Double.parseDouble(attrs.getValue("scaleH"));

    mPageEstimate = Integer.parseInt(attrs.getValue("pages"));

    mFont.packed = "1".equals(attrs.getValue("packed"));
  }

  protected void commonEnd() {
    mIsInsideCommon = false;
  }

  protected void pagesStart(Attributes attrs) {
    mIsInsidePages = true;
  }

  protected void pageStart(Attributes attrs) {
    if(!mIsInsidePages) return;
    String path = attrs.getValue("file");
    Font.Page page = new Font.Page();
    page.imagePath = FileSystems.getDefault().getPath(mFont.fntPath.getParent().toString(), path);
    page.id = Integer.parseInt(attrs.getValue("id"));

    try {
      page.image = ImageIO.read(new File(page.imagePath.toString()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    mFont.pages.add(page);
  }

  protected void pageEnd() {
  }

  protected void pagesEnd() {
    mIsInsidePages = false;
  }

  protected void charsStart(Attributes attrs) {
    mIsInsideChars = true;
  }

  protected void charStart(Attributes attrs) {
    // <char id="33"
    // 			x="1" y="1"
    // 			width="4" height="17"
    // 			xoffset="0" yoffset="0"
    // 			xadvance="4"
    // 			page="0"
    // 			chnl="0"
    // 			letter="!"
    // 			/>
    if(!mIsInsideChars) return;

    int x = Integer.parseInt(attrs.getValue("x"));
    int y = Integer.parseInt(attrs.getValue("y"));
    int width = Integer.parseInt(attrs.getValue("width"));
    int height = Integer.parseInt(attrs.getValue("height"));
    int id = Integer.parseInt(attrs.getValue("id"));

    Font.Page page = mFont.getPage(Integer.parseInt(attrs.getValue("page")));
    String letter = attrs.getValue("letter");
    if(null == letter)
    	letter = new String(Character.toChars(id));
    else letter = fromLetter(letter);
    Glyph glyph = new Glyph(
        letter, id,
        (width > 0 && height > 0 ? page.image.getSubimage(x, y, width, height) : null),
        width, 0.0d, height, 0.0d, Double.parseDouble(attrs.getValue("xadvance")),
        Double.parseDouble(attrs.getValue("xoffset")), Double.parseDouble(attrs.getValue("yoffset"))
    );

    page.glyphs.add(glyph);
    glyph.page = page;
  }

  protected void charEnd() {
  }

  protected void charsEnd() {
    mIsInsideChars = false;
  }

  protected void kerningsStart(Attributes attrs) {
	 mIsInsideKernings = true;
  }

	  protected void kerningStart(Attributes attrs) {
	    // <kerning first="86" second="90" amount="3" />
	    if(!mIsInsideKernings) return;

	    int left = Integer.parseInt(attrs.getValue("first"));
	    int right = Integer.parseInt(attrs.getValue("second"));
	    double kerning = Double.parseDouble(attrs.getValue("amount"));

	    mFont.kernings.add(new Kerning(left, right, kerning));
	  }

	  protected void kerningEnd() {
	  }

	  protected void kerningsEnd() {
	    mIsInsideKernings = false;
	  }

  protected void fontEnd() {
    mIsInsideFont = false;
  }

  protected static String fromLetter(String c) {
	  switch(c) {
	  case "space": return " ";
	  case "double quote": return "\"";
	  case "ampersand": return "&";
	  case "quote": return "'";
	  case "slash": return "/";
	  case "backslash": return "\\";
	  case "less then": return "<";
	  case "greater then": return ">";
	  }
	  return c;
  }

  protected static String toLetter(String c) {
	  switch(c) {
	  case "\"": return "&quot;";
	  case "&": return "&amp;";
	  case "<": return "&lt;";
	  case ">": return "&gt;";
	  }
    return c;
  }
}
