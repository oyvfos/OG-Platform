/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.opengamma.integration.loadsave.portfolio.PortfolioSaver;
import com.opengamma.integration.tool.AbstractIntegrationTool;

/**
 * The portfolio saver tool
 */
public class PortfolioSaverTool extends AbstractIntegrationTool {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioSaverTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {     
    // Call the portfolio loader with the supplied arguments
    new PortfolioSaver().run(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT), 
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        getCommandLine().getOptionValues(SECURITY_TYPE_OPT), 
        getCommandLine().hasOption(WRITE_OPT), 
        getToolContext()
    );
  }
  
  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);

    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file to create and export to (CSV, XLS or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the source OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the file if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
       
    Option assetClassOption = new Option(
        SECURITY_TYPE_OPT, "securitytype", true, 
        "The security type(s) to export (ignored if ZIP output file is specified)");
    options.addOption(assetClassOption);
    
    return options;
  }

}