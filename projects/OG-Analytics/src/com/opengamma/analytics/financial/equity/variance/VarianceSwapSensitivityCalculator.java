/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.interestrate.NodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * This Calculator provides price sensitivities for the VarianceSwap derivative to changes in
 * Interest rates,<p>
 * Equity European Option Volatility,<p>
 * Spot VarianceSwap contracts,<p>
 * Equity Spot contracts,<p>
 * Equity Forward contracts,<p>
 */
public final class VarianceSwapSensitivityCalculator extends EquityDerivativeSensitivityCalculator {

  private static final VarianceSwapSensitivityCalculator INSTANCE = new VarianceSwapSensitivityCalculator();

  public static VarianceSwapSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  private VarianceSwapSensitivityCalculator() {
    super(VarianceSwapPresentValueCalculator.getInstance());
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
   * Also known as PVBP and DV01, though note scaling - this returns per UNIT change in rate (100%). calcPV01 returns per basis point change in rates.  <p>
   * <p>
   * Rates enter the pricing of a VarianceSwap in two places: in the discounting and forward projection.<p>
   * The presentValue has been structured such that the form of the PV = Z(t,T) * FwdPrice(t,T) with Z a zero coupon bond, and t and T the valuation and settlement times respectively.
   * The form of our discounting rates is such that Z(t,T) = exp[- R(t,T) * (T-t)], hence  dZ/dR = -(T-t)*Z(t,T) and d(PV)/dR = PV * dZ/dR
   * The forward's dependence on the discounting rate is similar to the zero coupon bonds, but of opposite sign, dF/dR = (T-t)*F(t,T)
   * @param swap the VarianceSwap
   * @param market the EquityOptionDataBundle
   * @param shift Relative size of shift made in centered-finite difference approximation.
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate
   */
  public Double calcDiscountRateSensitivity(final VarianceSwap swap, final EquityOptionDataBundle market, final double shift) {
    Validate.notNull(market);
    Validate.notNull(swap);
    // Sensitivity from the discounting
    final VarianceSwapStaticReplication pricer = new VarianceSwapStaticReplication();
    final double pv = pricer.presentValue(swap, market);
    final double timeToSettlement = swap.getTimeToSettlement();

    // Sensitivity from forward projection
    final double fwdSens = calcForwardSensitivity(swap, market, shift);
    final double fwd = market.getForwardCurve().getForward(timeToSettlement);

    return timeToSettlement * (fwd * fwdSens - pv);

  }

  /**
   * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
   * Also know as PVBP and DV01, though note this return per UNIT change in rate. calcPV01 returns per basis point change in rates.  <p>
   * <p>
   * Rates enter the pricing of a VarianceSwap in two places: in the discounting and forward projection.<p>
   * The presentValue has been structured such that the form of the PV = Z(t,T) * FwdPrice(t,T) with Z a zero coupon bond, and t and T the valuation and settlement times respectively.
   * The form of our discounting rates is such that Z(t,T) = exp[- R(t,T) * (T-t)], hence  dZ/dR = (t-T)*Z(t,T) and d(PV)/dR = PV * dZ/dR
   * The forward's dependence on the discounting rate is similar to the zero coupon bonds, but of opposite sign, dF/dR = (T-t)*F(t,T)
   * @param swap the VarianceSwap
   * @param market the EquityOptionDataBundle
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate
   */
  public Double calcDiscountRateSensitivity(final VarianceSwap swap, final EquityOptionDataBundle market) {
    final double relativeShift = 0.01;
    return calcDiscountRateSensitivity(swap, market, relativeShift);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a basis point (bp) move in the funding rates across all maturities. <p>
   * Also know as PVBP and DV01.
   * @param swap the VarianceSwap
   * @param market the EquityOptionDataBundle
   * @return A Double in the currency, swap.getCurrency()
   */
  public Double calcPV01(final VarianceSwap swap, final EquityOptionDataBundle market) {
    return calcDiscountRateSensitivity(swap, market) / 10000;
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the continuously-compounded discount rates at the knot points of the funding curve. <p>
   * The return format is a DoubleMatrix1D (i.e. a vector) with length equal to the total number of knots in the curve <p>
   * The change of a curve due to the movement of a single knot is interpolator-dependent, so an instrument can have sensitivity to knots at times beyond its maturity
   * @param swap the VarianceSwap
   * @param market the EquityOptionDataBundle
   * @return A DoubleMatrix1D containing bucketed delta in order and length of market.getDiscountCurve(). Currency amount per unit amount change in discount rate
   */
  public DoubleMatrix1D calcDeltaBucketed(final VarianceSwap swap, final EquityOptionDataBundle market) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null EquityOptionDataBundle");

    // We know that the VarianceSwap only has true sensitivity to one maturity on one curve.
    // A function written for interestRate sensitivities spreads this sensitivity across yield nodes
    // NodeSensitivityCalculator.curveToNodeSensitivities(curveSensitivities, interpolatedCurves)

    // 2nd arg = LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves
    final YieldAndDiscountCurve discCrv = market.getDiscountCurve();
    final String discCrvName = discCrv.getCurve().getName();
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle();
    interpolatedCurves.setCurve(discCrvName, discCrv);

    // 1st arg = Map<String, List<DoublesPair>> curveSensitivities = <curveName, List<(maturity,sensitivity)>>
    final double settlement = swap.getTimeToSettlement();
    final Double sens = calcDiscountRateSensitivity(swap, market);
    final Map<String, List<DoublesPair>> curveSensitivities = new HashMap<String, List<DoublesPair>>();
    curveSensitivities.put(discCrvName, Lists.newArrayList(new DoublesPair(settlement, sens)));

    final NodeSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    return distributor.curveToNodeSensitivities(curveSensitivities, interpolatedCurves);
  }

  /**
   * This calculates the derivative of the present value (PV) with respect to the level of the fair value of variance
   * of a spot starting swap with an expiry equal to the that remaining in the existing VarianceSwap,
   * as described by David E Kuenzi in Risk 2005, 'Variance swaps and non-constant vega' <p>
   * This is simply the proportion of time left in the existing swap.
   * <p>
   *
   * @param swap the VarianceSwap
   * @param market the EquityOptionDataBundle
   * @return A Double representing the number of spot-starting VarianceSwaps required to hedge the variance exposure
   */
  public Double calcSensitivityToFairVariance(final VarianceSwap swap, final EquityOptionDataBundle market) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null EquityOptionDataBundle");

    final int nObsExpected = swap.getObsExpected();
    final int nObsSoFar = swap.getObservations().length;
    final int nObsDidntHappen = swap.getObsDisrupted();

    return (nObsExpected - nObsSoFar - nObsDidntHappen) / nObsExpected * swap.getVarNotional();
  }

}
