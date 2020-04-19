package pdftool;

import java.util.concurrent.ExecutionException;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

public class Main {

  public static void main(String[] args) {
    System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

    gotoCLIMode(args);
  }

  private static void gotoCLIMode(String[] args) {
    CLIMode cli = new CLIMode();
    CommandLine cmd = new CommandLine(cli);
    cmd.parseWithHandlers(
        new CommandLine.IParseResultHandler2<Object>() {
          @Override
          public Object handleParseResult(ParseResult parseResult) {
            return null;
          }
        },
        new CommandLine.IExceptionHandler2<Object>() {
          @Override
          public Object handleParseException(ParameterException e, String[] args) {
            String platform = System.getProperty("os.name").toLowerCase();

            if (platform.contains("windows")) {
              System.err.println("Invalid option\nTry 'PDFTool -h'");
            } else {
              System.err.println(
                  (char) 27 + "[31m" + "Invalid option\nTry 'PDFTool -h'" + (char) 27 + "[0m");
            }
            System.exit(0);

            return null;
          }

          public Object handleExecutionException(ExecutionException ex, ParseResult parseResult) {
            return null;
          }

          @Override
          public Object handleExecutionException(
              CommandLine.ExecutionException ee, ParseResult pr) {
            throw new UnsupportedOperationException(
                "Not supported yet."); // To change body of generated methods, choose Tools |
                                       // Templates.
          }
        },
        args);

    if (cmd.isUsageHelpRequested()) {
      cmd.usage(System.out);
    } else if (cmd.isVersionHelpRequested()) {
      cmd.printVersionHelp(System.out);
    } else {
      cli.run();
    }
  }
}
