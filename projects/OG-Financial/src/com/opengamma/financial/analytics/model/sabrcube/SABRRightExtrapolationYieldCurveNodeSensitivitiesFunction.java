/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.volatility.VolatilityDataFittingDefaults;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SABRRightExtrapolationYieldCurveNodeSensitivitiesFunction extends SABRYieldCurveNodeSensitivitiesFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cutoffNames = constraints.getValues(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE);
    if (cutoffNames == null || cutoffNames.size() != 1) {
      return null;
    }
    final Set<String> muNames = constraints.getValues(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
    if (muNames == null || muNames.size() != 1) {
      return null;
    }
    return requirements;
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final DayCount dayCount, final ValueRequirement desiredValue) {
    final YieldCurveBundle yieldCurves = getYieldCurves(inputs, currency, desiredValue);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    final ValueRequirement surfacesRequirement = getCubeRequirement(cubeName, currency, fittingMethod);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final SABRInterestRateParameters modelParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final Currency currency) {
    return createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, currency.getCode())
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CUBE)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION)
        .withAny(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD)
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .withAny(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE)
        .withAny(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String fittingMethod = desiredValue.getConstraint(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String cutoff = desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
    return createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION)
        .with(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .with(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE, cutoff)
        .with(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER, mu).get();
  }

  @Override
  protected PresentValueNodeSensitivityCalculator getNodeSensitivityCalculator(final ValueRequirement desiredValue) {
    final Double cutoff = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE));
    final Double mu = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER));
    return PresentValueNodeSensitivityCalculator.using(new PresentValueCurveSensitivitySABRExtrapolationCalculator(cutoff, mu));
  }

}
