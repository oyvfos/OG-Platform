/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 * @param <T>
 */
public class CombinedInterpolatorExtrapolator<T extends Interpolator1DDataBundle> extends Interpolator1D<T> {
  private final Interpolator1D<T> _interpolator;
  private final Interpolator1D<T> _leftExtrapolator;
  private final Interpolator1D<T> _rightExtrapolator;

  public CombinedInterpolatorExtrapolator(final Interpolator1D<T> interpolator) {
    _interpolator = interpolator;
    _leftExtrapolator = null;
    _rightExtrapolator = null;
  }

  public CombinedInterpolatorExtrapolator(final Interpolator1D<T> interpolator, final Interpolator1D<T> extrapolator) {
    _interpolator = interpolator;
    _leftExtrapolator = extrapolator;
    _rightExtrapolator = extrapolator;
  }

  public CombinedInterpolatorExtrapolator(final Interpolator1D<T> interpolator, final Interpolator1D<T> leftExtrapolator, final Interpolator1D<T> rightExtrapolator) {
    _interpolator = interpolator;
    _leftExtrapolator = leftExtrapolator;
    _rightExtrapolator = rightExtrapolator;
  }

  @Override
  public T getDataBundle(final double[] x, final double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public T getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  //TODO fail earlier if there's no extrapolators?
  @Override
  public Double interpolate(final T data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.interpolate(data, value);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.interpolate(data, value);
      }
    }
    return _interpolator.interpolate(data, value);
  }

}
