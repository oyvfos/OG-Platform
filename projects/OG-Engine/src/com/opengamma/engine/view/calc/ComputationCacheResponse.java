/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;

/**
 * Encapsulates the results from a computation cache query.
 */
@PublicSPI
@BeanDefinition
public class ComputationCacheResponse extends DirectBean {

  /**
   * The results obtained from the computation caches.
   */
  @PropertyDefinition
  private List<Pair<ValueSpecification, Object>> _results;
  
  public void setResults(Collection<Pair<ValueSpecification, Object>> result) {
    setResults(Lists.newArrayList(result));
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ComputationCacheResponse}.
   * @return the meta-bean, not null
   */
  public static ComputationCacheResponse.Meta meta() {
    return ComputationCacheResponse.Meta.INSTANCE;
  }

  @Override
  public ComputationCacheResponse.Meta metaBean() {
    return ComputationCacheResponse.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 1097546742:  // results
        return getResults();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 1097546742:  // results
        setResults((List<Pair<ValueSpecification, Object>>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the results obtained from the computation caches.
   * @return the value of the property
   */
  public List<Pair<ValueSpecification, Object>> getResults() {
    return _results;
  }

  /**
   * Sets the results obtained from the computation caches.
   * @param results  the new value of the property
   */
  public void setResults(List<Pair<ValueSpecification, Object>> results) {
    this._results = results;
  }

  /**
   * Gets the the {@code results} property.
   * @return the property, not null
   */
  public final Property<List<Pair<ValueSpecification, Object>>> results() {
    return metaBean().results().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ComputationCacheResponse}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code results} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Pair<ValueSpecification, Object>>> _results = DirectMetaProperty.ofReadWrite(this, "results", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("results", _results);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ComputationCacheResponse createBean() {
      return new ComputationCacheResponse();
    }

    @Override
    public Class<? extends ComputationCacheResponse> beanType() {
      return ComputationCacheResponse.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code results} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Pair<ValueSpecification, Object>>> results() {
      return _results;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
