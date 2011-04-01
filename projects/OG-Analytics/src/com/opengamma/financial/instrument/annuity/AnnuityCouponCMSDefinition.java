/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;

/**
 * A wrapper class for a AnnuityDefinition containing CouponIborDefinition.
 */
public class AnnuityCouponCMSDefinition extends AnnuityDefinition<CouponCMSDefinition> {

  /**
   * Constructor from a list of fixed coupons.
   * @param payments The fixed coupons.
   */
  public AnnuityCouponCMSDefinition(final CouponCMSDefinition[] payments) {
    super(payments);
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitAnnuityCouponCMSDefinition(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitAnnuityCouponCMSDefinition(this);
  }

}
