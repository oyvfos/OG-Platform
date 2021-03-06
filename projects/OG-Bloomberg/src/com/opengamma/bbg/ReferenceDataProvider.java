/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Set;


/**
 * Provides reference data for security from a data provider like Bloomberg.
 * <p>
 * The reference data provider is a service for searching security fields from Bloomberg.
 */
public interface ReferenceDataProvider {

  /**
   * @param securities Not null, not empty
   * @param fields Not null, not empty
   * @return Not null
   * @throws RuntimeException If not successful
   */
  ReferenceDataResult getFields(Set<String> securities, Set<String> fields);
  
}
