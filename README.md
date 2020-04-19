# PDFTool
`java -jar PDFTool-1.0.jar`
```
Usage: PDFTool [OPTION] [-i INPUT_FILE... -o OUTPUT_FILE]

A PDF tool let you do some basic PDF operations

  -c, --compress-images   Compress image files. This option must be used with
                            'extract-images' or 'convert-to-images' option.
  -d, --decrypt           Decrypt the PDF file.
  -e, --extract-images=FORMAT
                          Extract the images in the PDF file. FORMAT can be PNG,
                            JPEG or GIF.
  -m, --merge             Merge the PDF files sequentially with the given files.
  -p, --set-password=PASSWORD
                          Set the password of the PDF file.
      --set-key-length=KEY_LENGTH
                          The key length can be 40, 128 or 256. The default is 256.
                            It must be used with '-p' option.
  -l, --limit-permission=PERMISSION [PERMISSION [PERMISSION]]
                          Limit the user's permission, including PRINT, MODIFY and
                            EXTRACT content.
  -R, --remove-pages=RANGE...
                          Remove single page of the PDF file. If you have more than
                            one page removed, use comma to split them. E.g.,'1,3,5'
                            will remove Page 1, Page 3 and Page 5.
  -r, --rotate=DEGREE     Rotate the PDF clockwise with the given degree.
  -s, --split=RANGE...    Split the PDF file into multiple files with the given
                            ranges. The range can be represented as, for example,
                            '1-3' meaning from Page 1 to Page 3, '3-' meaning from
                            Page 3 to the end, or '-3' meaning from the first page
                            to Page 3. You can use more than one range by using
                            comma as separator. E.g., 1-3,5-6,4.
  -T, --convert-images-to-pdf
                          Convert images to one PDF file sequentially.
  -t, --convert-to-images=FORMAT
                          Convert every page of the PDF file to images. FORMAT can
                            be PNG, JPEG or GIF.
      --set-dpi=DPI       Set the images' dpi. The default is 300. The option must
                            be used with '-t' option.
  -i, --input-file=INPUT_FILE...
                          Input PDF or image file. If you use the merge option, you
                            need to input more than one file. If the file needs
                            password, then add the password behind the file with
                            colon seperated.E.g., File.pdf:Password. If you use the
                            'convert-images-to-pdf' option, you need to input image
                            file.
  -o, --output-file=OUTPUT_FILE
                          Output file.
  -h, --help              Show this help message and exit.
  -V, --version           Print version information and exit.

Examples:
  PDFTool -d -i /path/to/file.pdf -o /path/to/save.pdf
  PDFTool -e PNG -i /path/to/file.pdf -o /path/to/save
  PDFTool -l MODIFY PRINT -i /path/to/file.pdf -o /path/to/save.pdf
  PDFTool -s 1-3,5,6 -i /path/to/file.pdf -o /path/to/save.pdf
```
