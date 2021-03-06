/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.HashSet;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionUtils;
import com.opengamma.util.OpenGammaClock;

/**
 * Utility methods for building Bloomberg Tickers on IR Futures and IR Future Options (Refer to Bloomberg page: WIR)
 */
public class BloombergIRFutureUtils {
  
  /** Set of prefixes for Standard IR Future Options */
  public static final HashSet<String> STANDARD_PREFIX_SET = Sets.newHashSet("ER", "ED", "EF", "IR", "L ");
  /** Set of prefixes for MidCurve IR Future Options */
  public static final HashSet<String> MIDCURVE_PREFIX_SET = Sets.newHashSet("0R");
  
  /** Set of prefixes for which the logic of expiries has been tested */
  private static final HashSet<String> TESTED_PREFIX_SET = Sets.newHashSet("ER", "0R");
  
  
  /**
   *  Bloomberg's two character prefixes for Interest Rate Futures
   *  Be careful: Some of Bloomberg's code consist of 1 digit only with a trailing space! eg "L " for Sterling <p>
   *  Be careful: Some of Bloomberg's codes begin with a numeral (e.g. Euribor Mid-curves 0R). This isn't permitted in a java enum.
   *  Do NOT use name() to get string values of this enum as in this case, the trailing string will not be present
   *  Instead, use toString, which can be, and has, been overridden.    
   */
  public enum IRFuturePrefix {
    /** USD, Eurodollar, 3-month, CME */
    ED,
    /** EUR, Euro Euribor,  3-month, LIF */
    ER,
    /** JPY, Euroyen, 3-month, SGX */
    EF,
    /** AUD, 90 day Bankers' Acceptance, 3-month, SFE */
    IR,
    /** GBP, Short Sterling, 3-month, LIF */
    L {
      @Override
      public String toString() {
        return "L ";
      }
    }
  }
  
  /** Map of MonthOfYear to Bloomberg Month Codes*/
  public static final BiMap<MonthOfYear, Character> MONTH_CODE;
  static {
    MONTH_CODE = HashBiMap.create();
    MONTH_CODE.put(MonthOfYear.JANUARY, 'F');
    MONTH_CODE.put(MonthOfYear.FEBRUARY, 'G');
    MONTH_CODE.put(MonthOfYear.MARCH, 'H');
    MONTH_CODE.put(MonthOfYear.APRIL, 'J');
    MONTH_CODE.put(MonthOfYear.MAY, 'K');
    MONTH_CODE.put(MonthOfYear.JUNE, 'M');
    MONTH_CODE.put(MonthOfYear.JULY, 'N');
    MONTH_CODE.put(MonthOfYear.AUGUST, 'Q');
    MONTH_CODE.put(MonthOfYear.SEPTEMBER, 'U');
    MONTH_CODE.put(MonthOfYear.OCTOBER, 'V');
    MONTH_CODE.put(MonthOfYear.NOVEMBER, 'X');
    MONTH_CODE.put(MonthOfYear.DECEMBER, 'Z');
  }
  
  private static final Logger LOG = LoggerFactory.getLogger(BloombergIRFutureUtils.class);
  
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg ticker of IRFutureSecurity
   * @param futurePrefix 2 character String of Future (eg ED, ER, IR)
   * @param nthFuture The n'th future following valuation date
   * @param curveDate Date curve is valid; valuation date
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getQuarterlyExpiryCodeForFutures(final String futurePrefix, final int nthFuture, final LocalDate curveDate) {
    //Year convention for historical data is specific to the futurePrefix 
    final LocalDate twoDigitYearSwitch; 
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    if (futurePrefix.equals("ED")) {
      twoDigitYearSwitch = today.minus(Period.ofDays(2));
    } else {     
      twoDigitYearSwitch = today.minus(Period.ofMonths(11));
    } 
    return getQuarterlyExpiryMonthYearCode(nthFuture, curveDate, twoDigitYearSwitch);
  }
  
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg ticker of IRFutureSecurity
   * NOTE: Eurodollar FutureOptions do not share the same naming convention for past expiries as their underlying futures!
   * It appears that Bloomberg doesn't switch to a two-digit convention...
   * @param futurePrefix 2 character String of Future (eg ED, ER, IR)
   * @param nthFuture The n'th future following valuation date
   * @param curveDate Date curve is valid; valuation date
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getExpiryCodeForFutureOptions(final String futurePrefix, final int nthFuture, final LocalDate curveDate) {
    if (!TESTED_PREFIX_SET.contains(futurePrefix)) {
      LOG.debug("We recommended that you ask QR to test behaviour of IRFutureOption Volatility Surface's Expiries for prefix {}", futurePrefix);
      // The reason being that we have hard coded the behaviour of 6 consecutive months, then quarterly thereafter..
    }    
    LocalDate expiry  = IRFutureOptionUtils.getFutureOptionExpiry(nthFuture, curveDate);
    return getMonthYearCode(expiry, expiry.minusYears(10));    
  }
  
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg tickers of IRFutureSecurity and IRFutureOptionSecurity.
   * @param nthQuarter The n'th quarter following valuation date
   * @param valuationDate valuation date
   * @param twoDigitYearDate Expired futures will, before this date, be referenced by a 2-digit year (eg 12 for 2012) as opposed to trading futures (eg 2 for 2012) 
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getQuarterlyExpiryMonthYearCode(final int nthQuarter, final LocalDate valuationDate, final LocalDate twoDigitYearDate) {
    Validate.isTrue(nthQuarter > 0, "nthFuture must be greater than 0.");
    final LocalDate expiry = IRFutureOptionUtils.getQuarterlyExpiry(nthQuarter, valuationDate);
    return getMonthYearCode(expiry, twoDigitYearDate);
  }
  
  /**
   * Produces the month-year string required to build ExternalId for Bloomberg tickers of IRFutureSecurity and IRFutureOptionSecurity.
   * @param nthMonth The n'th month following valuation date
   * @param valuationDate valuation date
   * @param twoDigitYearDate Expired futures will, before this date, be referenced by a 2-digit year (eg 12 for 2012) as opposed to trading futures (eg 2 for 2012) 
   * @return e.g. M10 (for June 2010) or Z3 (for December 2013), both valid as of valuationDate 2012/04/10
   */
  public static final String getMonthlyExpiryMonthYearCode(final int nthMonth, final LocalDate valuationDate, final LocalDate twoDigitYearDate) {
    Validate.isTrue(nthMonth > 0, "nthFuture must be greater than 0.");
    final LocalDate expiry = IRFutureOptionUtils.getMonthlyExpiry(nthMonth, valuationDate);
    return getMonthYearCode(expiry, twoDigitYearDate);
  }
  /**
   * Produces Bloomberg's code for month and year 
   * @param expiry Expiry date of instrument
   * @param twoDigitSwitch Instrument specific historical date when code moves from 1- to 2- digit year code
   * @return String (e.g. Z3, H09)
   */
  public static final String getMonthYearCode(final LocalDate expiry, final LocalDate twoDigitSwitch) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(MONTH_CODE.get(expiry.getMonthOfYear()));  
    if (expiry.isBefore(twoDigitSwitch)) {
      final int yearsNum = expiry.getYear() % 100;
      if (yearsNum < 10) {
        futureCode.append("0"); // so we get '09' rather than '9'  
      }
      futureCode.append(Integer.toString(yearsNum));
    } else {
      futureCode.append(Integer.toString(expiry.getYear() % 10));
    }
    return futureCode.toString();
  }
  
  
}
