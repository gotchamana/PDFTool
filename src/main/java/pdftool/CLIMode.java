package pdftool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "PDFTool",
    synopsisHeading = "",
    customSynopsis = "%nUsage: PDFTool [OPTION] [-i INPUT_FILE... -o OUTPUT_FILE]%n",
    description = "A PDF tool let you do some basic PDF operations%n",
    footer =
        "%nExamples: %n  PDFTool -d -i /path/to/file.pdf -o /path/to/save.pdf"
            + "%n  PDFTool -e PNG -i /path/to/file.pdf -o /path/to/save"
            + "%n  PDFTool -l MODIFY PRINT -i /path/to/file.pdf -o /path/to/save.pdf"
            + "%n  PDFTool -s 1-3,5,6 -i /path/to/file.pdf -o /path/to/save.pdf",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    version = "PDFTool 1.0")
public class CLIMode implements Runnable {

  @Option(
      names = {"-c", "--compress-images"},
      description =
          "Compress image files. "
              + "This option must be used with 'extract-images' or 'convert-to-images' option.")
  private boolean isZip;

  @Option(
      names = {"-d", "--decrypt"},
      description = "Decrypt the PDF file.")
  private boolean isDecrypt;

  @Option(
      names = {"-e", "--extract-images"},
      paramLabel = "FORMAT",
      description = "Extract the images in the PDF file. " + "FORMAT can be PNG, JPEG or GIF.")
  private String extractImagesFormat;

  @Option(
      names = {"-m", "--merge"},
      description = "Merge the PDF files sequentially with the given files.")
  private boolean isMerge;

  @Option(
      names = {"-p", "--set-password"},
      paramLabel = "PASSWORD",
      description = "Set the password of the PDF file.")
  private String password;

  @Option(
      names = {"--set-key-length"},
      paramLabel = "KEY_LENGTH",
      description =
          "The key length can be 40, 128 or 256. "
              + "The default is 256. It must be used with '-p' option.")
  private int keyLength;

  @Option(
      names = {"-l", "--limit-permission"},
      arity = "1..3",
      paramLabel = "PERMISSION",
      description = "Limit the user's permission, including PRINT, MODIFY and EXTRACT content.")
  private String[] permissions;

  @Option(
      names = {"-R", "--remove-pages"},
      arity = "1..*",
      paramLabel = "RANGE",
      description =
          "Remove single page of the PDF file. "
              + "If you have more than one page removed, use comma to split them. "
              + "E.g.,'1,3,5' will remove Page 1, Page 3 and Page 5.")
  private String removeRanges;

  @Option(
      names = {"-r", "--rotate"},
      paramLabel = "DEGREE",
      description = "Rotate the PDF clockwise with the given degree.")
  private int degree;

  @Option(
      names = {"-s", "--split"},
      arity = "1..*",
      paramLabel = "RANGE",
      description =
          "Split the PDF file into multiple files with the given ranges. "
              + "The range can be represented as, for example, '1-3' meaning from Page 1 to Page 3, '3-' meaning from Page 3 to the end, or '-3' meaning from the first page to Page 3. "
              + "You can use more than one range by using comma as separator. E.g., 1-3,5-6,4.")
  private String splitRange;

  @Option(
      names = {"-T", "--convert-images-to-pdf"},
      description = "Convert images to one PDF file sequentially.")
  private boolean isConvertImagesToPDF;

  @Option(
      names = {"-t", "--convert-to-images"},
      arity = "1",
      paramLabel = "FORMAT",
      description =
          "Convert every page of the PDF file to images. " + "FORMAT can be PNG, JPEG or GIF.")
  private String convertToImagesFormat;

  @Option(
      names = {"--set-dpi"},
      paramLabel = "DPI",
      description =
          "Set the images' dpi. The default is 300. " + "The option must be used with '-t' option.")
  private int dpi;

  @Option(
      names = {"-i", "--input-file"},
      required = true,
      arity = "1..*",
      paramLabel = "INPUT_FILE",
      description =
          "Input PDF or image file. If you use the merge option, you need to input more than one file. "
              + "If the file needs password, then add the password behind the file with colon seperated."
              + "E.g., File.pdf:Password. If you use the 'convert-images-to-pdf' option, you need to input image file.")
  private String[] inputFiles;

  @Option(
      names = {"-o", "--output-file"},
      required = true,
      paramLabel = "OUTPUT_FILE",
      description = "Output file.")
  private String outputFile;

  @Override
  public void run() {
    try {
      checkOptionConflict();

      if (isConvertImagesToPDF) {
        // Convert images to PDF
        BufferedImage[] images = loadImages();
        PDFOperation.imageToPDF(outputFile, images);
      } else {
        PDDocument[] docs = loadPDFs();
        checkInputfileNumberValid(docs);
        doPDFOperation(docs);
      }

      System.out.println("Finished!");
    } catch (Exception e) {
      String platform = System.getProperty("os.name").toLowerCase();

      if (platform.contains("windows")) {
        System.err.println(e.getMessage());
      } else {
        //                System.err.println((char) 27 + "[31m" + e.getMessage() + (char) 27 +
        // "[0m");
      }
      e.printStackTrace();
    }
  }

  private void checkOptionConflict() throws Exception {
    List<String> options = new ArrayList<>();

    if (isDecrypt) {
      options.add("d");
    }

    if (extractImagesFormat != null) {
      options.add("e");
    }

    if (isMerge) {
      options.add("m");
    }

    if (password != null) {
      options.add("p");
    }

    if (permissions != null) {
      options.add("l");
    }

    if (removeRanges != null) {
      options.add("R");
    }

    if (degree != 0) {
      options.add("r");
    }

    if (splitRange != null) {
      options.add("s");
    }

    if (isConvertImagesToPDF) {
      options.add("T");
    }

    if (convertToImagesFormat != null) {
      options.add("t");
    }

    if (options.isEmpty()) {
      throw new Exception("You must use one option of 'd''e''m''p''l''R''r''s''T''t'.");

    } else if (options.size() == 1) {
      if (keyLength != 0 && !options.get(0).equals("p")) {
        throw new Exception("Option 'set-key-length' needs to be used with Option 'p'.");

      } else if (dpi != 0 && !options.get(0).equals("t")) {
        throw new Exception("Option 'set-dpi' needs to be used with Option 't'.");

      } else if (isZip && !(options.get(0).equals("e") || options.get(0).equals("t"))) {
        throw new Exception("Option 'compress-images' needs to be used with Option 'e' or 't'.");
      }

    } else {
      String errorMsg = "Option ";
      for (String option : options) {
        errorMsg += "'" + option + "'";
      }
      errorMsg += " can not be used at the same time.";

      throw new Exception(errorMsg);
    }
  }

  private PDDocument[] loadPDFs() throws Exception {
    List<PDDocument> PDFs = new ArrayList<>();

    for (String inputFile : inputFiles) {
      if (inputFile.contains(":")) {
        int index = inputFile.indexOf(':');

        // Extract the file name and password
        String file = inputFile.substring(0, index);
        String password = inputFile.substring(index + 1);

        if (!isPDFFile(file)) {
          throw new Exception("'" + inputFile + "' isn't PDF file.");
        }

        PDFs.add(PDDocument.load(new File(file), password));
      } else {
        if (!isPDFFile(inputFile)) {
          throw new Exception("'" + inputFile + "' isn't PDF file.");
        }

        PDFs.add(PDDocument.load(new File(inputFile)));
      }
    }

    return PDFs.toArray(new PDDocument[] {});
  }

  private BufferedImage[] loadImages() throws Exception {
    List<BufferedImage> images = new ArrayList<>();

    for (String inputFile : inputFiles) {
      if (!(inputFile.endsWith(".png")
          || inputFile.endsWith(".jpg")
          || inputFile.endsWith(".jpeg")
          || inputFile.endsWith(".gif")
          || inputFile.endsWith(".tif")
          || inputFile.endsWith(".tiff")
          || inputFile.endsWith(".bmp"))) {
        throw new Exception("Unsupported image format!");
      }
      images.add(ImageIO.read(new File(inputFile)));
    }

    return images.toArray(new BufferedImage[] {});
  }

  private boolean isPDFFile(String file) {
    // Check the input file whether is a pdf file.
    return file.endsWith(".pdf");
  }

  private void checkInputfileNumberValid(PDDocument[] docs) throws Exception {
    // If user chose the merge option, the input file needs more than one.
    // Otherwise, the input file must be single.
    if (docs.length > 1 && !isMerge) {
      throw new Exception("You can only input one PDF file.");
    }
  }

  private void doPDFOperation(PDDocument[] docs) throws Exception {
    if (isDecrypt) {
      // Decrypt
      PDFOperation.decryptPDF(docs[0], outputFile);

    } else if (extractImagesFormat != null) {
      // Extract images
      // DPI use 300 for default
      dpi = (dpi == 0) ? 300 : dpi;

      // Check the image format
      extractImagesFormat = extractImagesFormat.toLowerCase();
      if (!(extractImagesFormat.equals("png")
          || extractImagesFormat.equals("jpg")
          || extractImagesFormat.equals("jpeg")
          || extractImagesFormat.equals("gif"))) {
        throw new Exception("Unsupported image format. You only can choose PNG, JPG and GIF!");
      }

      PDFOperation.extractImages(docs[0], outputFile, extractImagesFormat, dpi, isZip);

    } else if (isMerge) {
      // Merge
      // Check input file number
      if (docs.length < 2) {
        throw new Exception("Invalid input file number. You need to input more than one file!");
      }

      PDFOperation.mergePDF(outputFile, docs);

    } else if (password != null) {
      // Set the password
      // Key length use 256 for default
      keyLength = (keyLength == 0) ? 256 : keyLength;

      // Check the key length
      if (!(keyLength == 40 || keyLength == 128 || keyLength == 256)) {
        throw new Exception("Invalid key length. You can only use 40, 128 and 256!");
      }

      PDFOperation.encryptPDF(docs[0], outputFile, password, keyLength);

    } else if (permissions != null) {
      // Limit the permission
      // Check the permission user choose
      for (String permission : permissions) {
        permission = permission.toLowerCase();
        if (!(permission.equals("modify")
            || permission.equals("print")
            || permission.equals("extract"))) {
          throw new Exception("Invalid permission. You can only use MODIFY, PRINT and EXTRACT!");
        }
      }

      PDFOperation.encryptPDF(docs[0], outputFile, "", 256, permissions);

    } else if (removeRanges != null) {
      // Remove the single page
      String[] pageNumberStrs = removeRanges.split(",");
      int[] pageNumbers = new int[pageNumberStrs.length];

      for (int i = 0; i < pageNumbers.length; i++) {
        pageNumbers[i] = Integer.parseInt(pageNumberStrs[i]);
      }

      PDFOperation.removePages(docs[0], outputFile, pageNumbers);

    } else if (degree != 0) {
      // Rotate
      PDFOperation.rotatePDF(docs[0], degree, outputFile);

    } else if (splitRange != null) {
      // Split
      PDFOperation.splitPDF(docs[0], splitRange, outputFile);

    } else if (convertToImagesFormat != null) {
      // Convert to images
      // DPI use 300 for default
      dpi = dpi == 0 ? 300 : dpi;

      // Check the image format
      convertToImagesFormat = convertToImagesFormat.toLowerCase();
      if (!(convertToImagesFormat.equals("png")
          || convertToImagesFormat.equals("jpg")
          || convertToImagesFormat.equals("jpeg")
          || convertToImagesFormat.equals("gif"))) {
        throw new Exception("Unsupported image format. You only can choose PNG, JPG and GIF!");
      }

      PDFOperation.pdfToImage(docs[0], outputFile, convertToImagesFormat, dpi, isZip);

    } else {
      throw new Exception("Unknown error");
    }
  }
}
