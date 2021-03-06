/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.functional.Function2;
import com.opengamma.util.money.Currency;

/**
 * Utility for generating a portfolio of securities.
 */
public class PortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    super.configureChain(securityGenerator);
    securityGenerator.setCurrencyCurveName("SECONDARY");
    securityGenerator.setPreferredScheme(ExternalSchemes.OG_SYNTHETIC_TICKER);
    securityGenerator.setSpotRateIdentifier(new Function2<Currency, Currency, ExternalId>() {
      @Override
      public ExternalId execute(final Currency a, final Currency b) {
        return ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, a.getCode() + b.getCode());
      }
    });
  }

  public static void main(final String[] args) { // CSIGNORE
    (new AbstractExampleTool() {

      private final PortfolioGeneratorTool _instance = new PortfolioGeneratorTool();

      @Override
      protected Options createOptions(boolean mandatoryConfigArg) {
        final Options options = super.createOptions(mandatoryConfigArg);
        _instance.createOptions(options);
        return options;
      }

      @Override
      protected void doRun() throws Exception {
        final CommandLine commandLine = getCommandLine();
        _instance.run(getToolContext(), commandLine);
      }

      @Override
      protected Class<?> getEntryPointClass() {
        return PortfolioGeneratorTool.class;
      }

    }).initAndRun(args);
    System.exit(0);
  }

}
