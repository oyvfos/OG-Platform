/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the PayoffStyle subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface PayoffStyleVisitor<T> {

  T visitAssetOrNothingPayoffStyle(AssetOrNothingPayoffStyle payoffStyle);

  T visitAsymmetricPoweredPayoffStyle(AsymmetricPoweredPayoffStyle payoffStyle);

  T visitBarrierPayoffStyle(BarrierPayoffStyle payoffStyle);

  T visitCappedPoweredPayoffStyle(CappedPoweredPayoffStyle payoffStyle);

  T visitCashOrNothingPayoffStyle(CashOrNothingPayoffStyle payoffStyle);
  
  T visitFixedStrikePayoffStyle(FixedStrikePayoffStyle payoffStyle);

  T visitPoweredPayoffStyle(PoweredPayoffStyle payoffStyle);

  T visitVanillaPayoffStyle(VanillaPayoffStyle payoffStyle);
  
}
