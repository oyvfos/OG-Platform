/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.variance.definition.VarianceSwapDefinition;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.equity.EquityVarianceSwapConverter;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/** 
 * Base class for Functions for EquityVarianceSwapSecurity.
 * These functions price using Static Replication 
 */
public abstract class EquityVarianceSwapFunction extends AbstractFunction.NonCompiledInvoker {

  
  private final String _valueRequirementName;
  private final String _curveDefinitionName;
  private final String _surfaceDefinitionName;
  @SuppressWarnings("unused")
  private final String _forwardCalculationMethod;
  private EquityVarianceSwapConverter _converter; // set in init()
  
  /** CalculationMethod constraint used in configuration to choose this model */
  public static final String CALCULATION_METHOD = "StaticReplication";
  /** Method may be Strike or Moneyness TODO Confirm */
  public static final String STRIKE_PARAMETERIZATION_METHOD = "StrikeParameterizationMethod";
  
  
  public EquityVarianceSwapFunction(final String valueRequirementName, final String curveDefinitionName, final String surfaceDefinitionName, final String forwardCalculationMethod) {
    Validate.notNull(valueRequirementName, "value requirement name");
    Validate.notNull(curveDefinitionName, "curve definition name");
    Validate.notNull(surfaceDefinitionName, "surface definition name");
    Validate.notNull(forwardCalculationMethod, "forward calculation method");

    _valueRequirementName = valueRequirementName;
    _curveDefinitionName = curveDefinitionName;
    _surfaceDefinitionName = surfaceDefinitionName;
    _forwardCalculationMethod = forwardCalculationMethod;
  }

  
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // 1. Build the analytic derivative to be priced
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ExternalId id = security.getSpotUnderlyingId();

    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);

    final VarianceSwapDefinition defn = _converter.visitEquityVarianceSwapTrade(security);
    final HistoricalTimeSeries timeSeries = dataSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, ExternalIdBundle.of(id), null);
    final VarianceSwap deriv = defn.toDerivative(now, timeSeries.getTimeSeries());

    // 2. Build up the market data bundle
    final Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(security));
    if (volSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    final VolatilitySurface volSurface = (VolatilitySurface) volSurfaceObject;
    //TODO no choice of other surfaces
    final BlackVolatilitySurface<?> blackVolSurf = new BlackVolatilitySurfaceStrike(volSurface.getSurface());

    final Object discountObject = inputs.getValue(getDiscountCurveRequirement(security));
    if (discountObject == null) {
      throw new OpenGammaRuntimeException("Could not get Discount Curve");
    }
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) discountObject;

    final Object spotObject = inputs.getValue(getSpotRequirement(security));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    final double spot = (Double) spotObject;

    final double expiry = TimeCalculator.getTimeBetween(executionContext.getValuationClock().zonedDateTime(), security.getLastObservationDate());
    final double discountFactor = discountCurve.getDiscountFactor(expiry);
    Validate.isTrue(discountFactor != 0, "The discount curve has returned a zero value for a discount bond. Check rates.");
    final ForwardCurve forwardCurve = new ForwardCurve(spot, discountCurve.getCurve()); //TODO change this
    final EquityOptionDataBundle market = new EquityOptionDataBundle(blackVolSurf, discountCurve, forwardCurve);
    
    // 3. Compute and return the value (ComputedValue)
    return computeValues(target, inputs, deriv, market);
  }

  protected abstract Set<ComputedValue> computeValues(final ComputationTarget target, final FunctionInputs inputs, final VarianceSwap derivative, final EquityOptionDataBundle market);

  protected ValueSpecification getValueSpecification(final ComputationTarget target) {
    final ValueProperties properties = getValueProperties(target);
    return new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
  }

  protected ValueProperties getValueProperties(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    return createValueProperties()
      .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
      .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD).get();
  }
  
  protected String getCurveDefinitionName() {
    return _curveDefinitionName;
  }

  protected String getSurfaceName() {
    return _surfaceDefinitionName;
  }

  private ValueRequirement getSpotRequirement(final EquityVarianceSwapSecurity security) {
    final ExternalId id = security.getSpotUnderlyingId();
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, UniqueId.of(id.getScheme().getName(), id.getValue()));
  }

  // Note that createValueProperties is _not_ used - use will mean the engine can't find the requirement
  private ValueRequirement getDiscountCurveRequirement(final EquityVarianceSwapSecurity security) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _curveDefinitionName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final EquityVarianceSwapSecurity security) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, _surfaceDefinitionName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, "EQUITY_OPTION")
        .get();
    final ExternalId id = security.getSpotUnderlyingId();
    final UniqueId newId = id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER) ? UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), id.getValue()) :
      UniqueId.of(id.getScheme().getName(), id.getValue());
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, newId, properties);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    _converter = new EquityVarianceSwapConverter(holidaySource);

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof EquityVarianceSwapSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    return Sets.newHashSet(getDiscountCurveRequirement(security), getSpotRequirement(security), getVolatilitySurfaceRequirement(security));  
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(getValueSpecification(target));
  }
}
