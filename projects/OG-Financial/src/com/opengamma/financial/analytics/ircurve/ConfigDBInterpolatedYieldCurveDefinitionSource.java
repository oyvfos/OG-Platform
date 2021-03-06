/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBInterpolatedYieldCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  /**
   * Gets the config source.
   * @return the config source, not null
   */
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinition getDefinition(final Currency ccy, final String name) {
    return _configSource.getLatestByName(YieldCurveDefinition.class, name + "_" + ccy.getCode());
  }

  @Override
  public YieldCurveDefinition getDefinition(final Currency ccy, final String name, final InstantProvider version) {
    final Instant versionInstant = (version != null) ? version.toInstant() : null;
    return _configSource.getByName(YieldCurveDefinition.class, name + "_" + ccy.getCode(), versionInstant);
  }

}
