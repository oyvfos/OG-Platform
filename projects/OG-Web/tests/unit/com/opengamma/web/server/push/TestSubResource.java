/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 *
 */
//@Path("/testsub/{uid}") // TODO is this necessary on a sub-resource?
public class TestSubResource {

  private final String _uid;

  public TestSubResource(String uid) {
    _uid = uid;
  }

  @GET
  @Produces("text/plain")
  public String getText() {
    return "Hello from TestSubResource " + _uid;
  }
}