/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

import org.joda.beans.*;
import org.joda.beans.impl.direct.*;

import java.util.Map;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

@BeanDefinition
public class ComputeHost extends DirectBean {
  
  @PropertyDefinition
  private long _id;
  
  @PropertyDefinition
  private String _hostName;  

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ComputeHost}.
   * @return the meta-bean, not null
   */
  public static ComputeHost.Meta meta() {
    return ComputeHost.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ComputeHost.Meta.INSTANCE);
  }

  @Override
  public ComputeHost.Meta metaBean() {
    return ComputeHost.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        return getId();
      case -300756909:  // hostName
        return getHostName();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        setId((Long) newValue);
        return;
      case -300756909:  // hostName
        setHostName((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ComputeHost other = (ComputeHost) obj;
      return JodaBeanUtils.equal(getId(), other.getId()) &&
          JodaBeanUtils.equal(getHostName(), other.getHostName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHostName());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public long getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(long id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<Long> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the hostName.
   * @return the value of the property
   */
  public String getHostName() {
    return _hostName;
  }

  /**
   * Sets the hostName.
   * @param hostName  the new value of the property
   */
  public void setHostName(String hostName) {
    this._hostName = hostName;
  }

  /**
   * Gets the the {@code hostName} property.
   * @return the property, not null
   */
  public final Property<String> hostName() {
    return metaBean().hostName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ComputeHost}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<Long> _id = DirectMetaProperty.ofReadWrite(
        this, "id", ComputeHost.class, Long.TYPE);
    /**
     * The meta-property for the {@code hostName} property.
     */
    private final MetaProperty<String> _hostName = DirectMetaProperty.ofReadWrite(
        this, "hostName", ComputeHost.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "hostName");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case -300756909:  // hostName
          return _hostName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ComputeHost> builder() {
      return new DirectBeanBuilder<ComputeHost>(new ComputeHost());
    }

    @Override
    public Class<? extends ComputeHost> beanType() {
      return ComputeHost.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code hostName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> hostName() {
      return _hostName;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
