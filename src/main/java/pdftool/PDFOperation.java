package pdftool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
// import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class PDFOperation {

  private static class ExtractImages extends PDFStreamEngine {

    private int imageNumber = 1, dpi;
    private String format, outputFile;

    public ExtractImages(String outputFile, String format, int dpi) {
      this.dpi = dpi;
      this.format = format;
      this.outputFile = outputFile;
    }

    private int getTotalImageNumber() {
      return imageNumber - 1;
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
      String operation = operator.getName();

      if (operation.equals("Do")) {
        COSName name = (COSName) operands.get(0);
        PDXObject object = getResources().getXObject(name);

        if (object instanceof PDImageXObject) {
          PDImageXObject image = (PDImageXObject) object;

          System.out.println("Handle Image " + imageNumber + "...");
          // ImageIOUtil.writeImage(image.getImage(), outputFile + imageNumber + "." + format, dpi);
          imageNumber++;

        } else if (object instanceof PDFormXObject) {
          PDFormXObject form = (PDFormXObject) object;
          showForm(form);
        }
      } else {
        super.processOperator(operator, operands);
      }
    }
  }

  public static Image getPDFCoverImage(PDDocument doc) throws IOException {
    PDFRenderer renderer = new PDFRenderer(doc);
    BufferedImage image = renderer.renderImage(0);
    return SwingFXUtils.toFXImage(image, new WritableImage(100, 100));
  }

  public static void rotatePDF(File doc, int degree, File outputFile) throws IOException {
    rotatePDF(PDDocument.load(doc), degree, outputFile.getAbsolutePath());
  }

  public static void rotatePDF(PDDocument doc, int degree, String outputFile) throws IOException {
    System.out.println("Start to rotate...");

    PDPageTree pages = doc.getPages();
    for (PDPage page : pages) {
      System.out.println("Handle Page " + pages.indexOf(page) + "...");
      page.setRotation(page.getRotation() + degree);
    }
    doc.save(outputFile);
    doc.close();
  }

  public static void decryptPDF(File doc, File outputFile) throws IOException {
    decryptPDF(PDDocument.load(doc), outputFile.getAbsolutePath());
  }

  public static void decryptPDF(PDDocument doc, String outputFile) throws IOException {
    System.out.println("Start to decrypt...");

    doc.setAllSecurityToBeRemoved(true);
    doc.save(outputFile);
    doc.close();
  }

  public static void encryptPDF(
      File doc, File outputFile, String password, int keyLength, String... permissions)
      throws IOException {
    encryptPDF(
        PDDocument.load(doc), outputFile.getAbsolutePath(), password, keyLength, permissions);
  }

  public static void encryptPDF(
      PDDocument doc, String outputFile, String password, int keyLength, String... permissions)
      throws IOException {
    System.out.println("Start to encrypt...");

    if (!password.equals("")) {
      System.out.println("Set password");
    }

    AccessPermission ap = new AccessPermission();

    if (permissions.length != 0) {
      for (String permission : permissions) {
        switch (permission) {
          case "modify":
            System.out.println("Limit MODIFY");
            ap.setCanModify(false);
            break;

          case "print":
            System.out.println("Limit PRINT");
            ap.setCanPrint(false);
            break;

          case "extract":
            System.out.println("Limit EXTRACT content");
            ap.setCanExtractContent(false);
            break;
        }
      }
    }

    StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, ap);
    spp.setEncryptionKeyLength(keyLength);
    spp.setPermissions(ap);

    doc.protect(spp);
    doc.save(outputFile);
    doc.close();
  }

  public static void removePages(File doc, File outputFile, int... pageNumbers) throws IOException {
    removePages(PDDocument.load(doc), outputFile.getAbsolutePath(), pageNumbers);
  }

  public static void removePages(PDDocument doc, String outputFile, int... pageNumbers)
      throws IOException {
    System.out.println("Start to remove single page...");

    PDPageTree pages = doc.getDocumentCatalog().getPages();
    for (int i = 0; i < pageNumbers.length; i++) {
      pageNumbers[i]--;
      if (i > 0) {
        for (int j = i; j < pageNumbers.length; j++) {
          pageNumbers[j]--;
        }
      }
      pages.remove(pageNumbers[i]);
    }
    if (!doc.isAllSecurityToBeRemoved()) {
      doc.setAllSecurityToBeRemoved(true);
    }
    doc.save(outputFile);
    doc.close();
  }

  public static void pdfToImage(File doc, File outputFile, String format, int dpi, boolean isZip)
      throws IOException {
    pdfToImage(PDDocument.load(doc), outputFile.getAbsolutePath(), format, dpi, isZip);
  }

  public static void pdfToImage(
      PDDocument doc, String outputFile, String format, int dpi, boolean isZip) throws IOException {
    System.out.println("Start to convert PDF to images...");

    if (outputFile.contains(".")) {
      outputFile = outputFile.substring(0, outputFile.indexOf('.'));
    }

    PDFRenderer renderer = new PDFRenderer(doc);
    for (int i = 0; i < doc.getNumberOfPages(); i++) {
      System.out.println("Handle Page " + (i + 1) + "...");
      BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
      // ImageIOUtil.writeImage(image, outputFile + (i + 1) + "." + format, dpi);
    }

    if (isZip) {
      File[] images = new File[doc.getNumberOfPages()];
      for (int i = 0; i < images.length; i++) {
        images[i] = new File(outputFile + (i + 1) + "." + format);
      }
      zipFile(images, outputFile);
    }
  }

  public static void mergePDF(File outputFile, File... docs) throws IOException {
    PDDocument[] pdocs = new PDDocument[docs.length];

    for (int i = 0; i < docs.length; i++) {
      pdocs[i] = PDDocument.load(docs[i]);
    }

    mergePDF(outputFile.getAbsolutePath(), pdocs);
  }

  public static void mergePDF(String outputFile, PDDocument... docs) throws IOException {
    System.out.println("Start to merge...");

    PDFMergerUtility merger = new PDFMergerUtility();

    for (int i = 1; i < docs.length; i++) {
      merger.appendDocument(docs[0], docs[i]);
    }

    if (!docs[0].isAllSecurityToBeRemoved()) {
      docs[0].setAllSecurityToBeRemoved(true);
    }

    docs[0].save(outputFile);
    for (PDDocument doc : docs) {
      doc.close();
    }
  }

  public static void extractImages(File doc, File outputFile, String format, int dpi, boolean isZip)
      throws IOException {
    extractImages(PDDocument.load(doc), outputFile.getAbsolutePath(), format, dpi, isZip);
  }

  public static void extractImages(
      PDDocument doc, String outputFile, String format, int dpi, boolean isZip) throws IOException {
    System.out.println("Start to extract images...");

    if (outputFile.contains(".")) {
      outputFile = outputFile.substring(0, outputFile.indexOf('.'));
    }

    ExtractImages extracter = new ExtractImages(outputFile, format, dpi);
    for (PDPage page : doc.getPages()) {
      extracter.processPage(page);
    }

    if (isZip) {
      File[] files = new File[extracter.getTotalImageNumber()];
      for (int i = 0; i < files.length; i++) {
        files[i] = new File(outputFile + (i + 1) + "." + format);
      }
      zipFile(files, outputFile);
    }
  }

  public static void splitPDF(File doc, String inputRange, File outputFile) throws IOException {
    splitPDF(PDDocument.load(doc), inputRange, outputFile.getAbsolutePath());
  }

  public static void splitPDF(PDDocument doc, String inputRange, String outputFile)
      throws IOException {
    System.out.println("Start to split...");

    String[] ranges = inputRange.split(",");
    outputFile = outputFile.endsWith(".pdf") ? outputFile.replace(".pdf", "") : outputFile;
    List<PDDocument> docs = new ArrayList<>();

    for (String range : ranges) {

      if (!range.contains("-")) {
        // Single page
        int pageNumber = Integer.parseInt(range);

        Splitter splitter = new Splitter();
        splitter.setStartPage(pageNumber);
        splitter.setEndPage(pageNumber);

        docs.addAll(splitter.split(doc));
      } else {
        // More than one page
        String[] startEnd = range.split("-");
        int start = range.startsWith("-") ? 1 : Integer.parseInt(startEnd[0]);
        int end = range.endsWith("-") ? doc.getNumberOfPages() : Integer.parseInt(startEnd[1]);

        Splitter splitter = new Splitter();
        splitter.setStartPage(start);
        splitter.setEndPage(end);
        splitter.setSplitAtPage(doc.getNumberOfPages());

        docs.addAll(splitter.split(doc));
      }
    }
    for (int i = 0; i < docs.size(); i++) {
      docs.get(i).save(outputFile + (i + 1) + ".pdf");
      docs.get(i).close();
    }
  }

  public static void imageToPDF(String outputFile, BufferedImage... inputImages)
      throws IOException {
    System.out.println("Start to convert images to PDF...");
    PDDocument doc = new PDDocument();

    for (int i = 0; i < inputImages.length; i++) {
      System.out.println("Handle Image " + (i + 1) + "...");
      PDPage page =
          new PDPage(new PDRectangle(inputImages[i].getWidth(), inputImages[i].getHeight()));
      doc.addPage(page);

      PDPageContentStream contentStream = new PDPageContentStream(doc, page);
      PDImageXObject img = LosslessFactory.createFromImage(doc, inputImages[i]);
      contentStream.drawImage(img, 0, 0);
      contentStream.close();
    }

    doc.save(outputFile);
    doc.close();
  }

  private static void zipFile(File[] files, String outputFile) throws IOException {
    System.out.println("Start to zip files...");
    outputFile = (outputFile.endsWith(".zip")) ? outputFile : outputFile + ".zip";

    FileOutputStream fos = new FileOutputStream(outputFile);
    ZipOutputStream zos = new ZipOutputStream(fos);

    for (int i = 0; i < files.length; i++) {
      System.out.println("Handle Image " + (i + 1));
      FileInputStream fis = new FileInputStream(files[i]);
      ZipEntry entry = new ZipEntry(files[i].getName());
      zos.putNextEntry(entry);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zos.write(bytes, 0, length);
      }
      // Remove origin file
      files[i].delete();
      fis.close();
    }
    zos.close();
    fos.close();
  }
}
