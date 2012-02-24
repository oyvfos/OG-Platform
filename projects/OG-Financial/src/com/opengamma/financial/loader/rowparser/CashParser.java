/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparser;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;

/**
 * This class parses standard OG import fields to generate a Cash security
 */
public class CashParser extends RowParser {

  private static final String ID_SCHEME = "CASH_LOADER";

  //CSOFF
  protected String CURRENCY = "currency";
  protected String REGION = "region";
  protected String START = "start";
  protected String MATURITY = "maturity";
  protected String DAY_COUNT = "daycount";
  protected String RATE = "rate";
  protected String AMOUNT = "amount";
  //CSON

  public CashParser(ToolContext toolContext) {
    super(toolContext);
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> cashDetails) {
    Currency ccy = Currency.of(getWithException(cashDetails, CURRENCY));
    ExternalId region = ExternalId.of(RegionUtils.ISO_COUNTRY_ALPHA2, REGION);
    LocalDateTime maturity = LocalDateTime.of(
        LocalDate.parse(getWithException(cashDetails, MATURITY), CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    LocalDateTime start = LocalDateTime.of(
        LocalDate.parse(getWithException(cashDetails, START), CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(cashDetails, DAY_COUNT));
    double rate = Double.parseDouble(getWithException(cashDetails, RATE));
    double amount = Double.parseDouble(getWithException(cashDetails, AMOUNT));
    
    CashSecurity cash = new CashSecurity(ccy, region, start.atZone(TimeZone.UTC), maturity.atZone(TimeZone.UTC), dayCount, rate, amount);
    cash.setName("Cash " + ccy.getCode() + " " + NOTIONAL_FORMATTER.format(amount) + " @ "
        + RATE_FORMATTER.format(rate) + ", maturity "
        + maturity.toString(OUTPUT_DATE_FORMATTER));
    cash.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {cash};
    return result;
  }

  @Override
  public Map<String, String> constructRow(ManageableSecurity security) {
    Map<String, String> result = new HashMap<String, String>();
    CashSecurity cash = (CashSecurity) security;
    
    result.put(CURRENCY, cash.getCurrency().getCode());
    result.put(REGION, RegionUtils.getRegions(getToolContext().getRegionSource(), cash.getRegionId()).iterator().next().getFullName());
    result.put(START, cash.getStart().toString(CSV_DATE_FORMATTER));
    result.put(MATURITY, cash.getMaturity().toString(CSV_DATE_FORMATTER));
    result.put(DAY_COUNT, cash.getDayCount().getConventionName());
    result.put(RATE, Double.toString(cash.getRate()));
    result.put(AMOUNT, Double.toString(cash.getAmount()));
    
    return result;
  }

}