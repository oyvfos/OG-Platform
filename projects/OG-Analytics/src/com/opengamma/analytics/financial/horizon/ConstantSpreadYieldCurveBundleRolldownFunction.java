/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Produces a YieldCurveBundle that has been shifted forward in time without slide. 
 * That is, it moves in such a way that the rate or discount factor requested for the same maturity DATE 
 * will be equal for the original market data bundle and the shifted one. 
 */
public final class ConstantSpreadYieldCurveBundleRolldownFunction implements RolldownFunction<YieldCurveBundle> {
  private static final ConstantSpreadYieldCurveRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveRolldownFunction.getInstance();
  private static final ConstantSpreadYieldCurveBundleRolldownFunction INSTANCE = new ConstantSpreadYieldCurveBundleRolldownFunction();

  public static ConstantSpreadYieldCurveBundleRolldownFunction getInstance() {
    return INSTANCE;
  }

  private ConstantSpreadYieldCurveBundleRolldownFunction() {
  }

  @Override
  public YieldCurveBundle rollDown(final YieldCurveBundle data, final double time) {
    final YieldCurveBundle shiftedCurves = new YieldCurveBundle();
    for (final String name : data.getAllNames()) {
      shiftedCurves.setCurve(name, CURVE_ROLLDOWN.rollDown(data.getCurve(name), time));
    }
    return shiftedCurves;
  }

}
