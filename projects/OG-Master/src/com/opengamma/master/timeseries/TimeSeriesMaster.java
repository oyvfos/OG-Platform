/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose time-series master.
 * <p>
 * The time-series master provides a uniform view over the time-series database.
 * This interface provides methods that allow the master to be searched and updated.
 * 
 * @param <T> Type of time series (LocalDate/Date) to operate on
 */
@PublicSPI
public interface TimeSeriesMaster<T> {

  /**
   * Value for unknown data provider in the database.
   */
  String UNKNOWN_PROVIDER = "UNKNOWN";
  /**
   * Value for Unknown observation time in the database.
   */
  String UNKNOWN_OBSERVATION_TIME = "UNKNOWN";

  /**
   * Gets all the identifiers.
   * 
   * @return the list of identifiers, not null
   */
  List<IdentifierBundleWithDates> getAllIdentifiers();

  /**
   * Searches for time-series matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  TimeSeriesSearchResult<T> search(TimeSeriesSearchRequest<T> request);

  /**
   * Gets a time-series by unique identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  TimeSeriesDocument<T> get(UniqueIdentifier uniqueId);

  /**
   * Adds a time-series to the data store.
   * <p>
   * The unique identifier will be set in the response.
   * 
   * @param document  the document, not null
   * @return the added document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  TimeSeriesDocument<T> add(TimeSeriesDocument<T> document);

  /**
   * Updates a time-series in the data store.
   * <p>
   * The specified document must contain the unique identifier.
   * 
   * @param document  the document, not null
   * @return the current state of the document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no time-series with that unique identifier
   */
  TimeSeriesDocument<T> update(TimeSeriesDocument<T> document);

  /**
   * Removes a time-series from the data store.
   * 
   * @param uniqueId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void remove(final UniqueIdentifier uniqueId);

  /**
   * Searches for time-series matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  TimeSeriesSearchHistoricResult<T> searchHistoric(TimeSeriesSearchHistoricRequest request);

  //-------------------------------------------------------------------------
  /**
   * Gets a data point by unique identifier.
   * <p> 
   * The dataPoint UID is of the format {@code TimeSeriesUID-YYYYMMDD}.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the data point document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  DataPointDocument<T> getDataPoint(UniqueIdentifier uniqueId);

  /**
   * Adds a data point to an existing time-series in the data store.
   * 
   * @param document  the data point document, not null
   * @return the added document, may be an update of the input document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  DataPointDocument<T> addDataPoint(DataPointDocument<T> document);

  /**
   * Updates a data point in the data store.
   * <p>
   * The specified document must contain the value and the unique identifier.
   * 
   * @param document  the data point document, not null
   * @return the updated data point document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  DataPointDocument<T> updateDataPoint(DataPointDocument<T> document);

  /**
   * Removes a data point from a time-series in the data store.
   * <p> 
   * The dataPoint UID is of the format {@code TimeSeriesUID-YYYYMMDD}.
   * 
   * @param uniqueId  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void removeDataPoint(final UniqueIdentifier uniqueId);

  //-------------------------------------------------------------------------
  /**
   * Append data points to an existing time-series.
   * <p>
   * This is a bulk update method to add multiple data points to an existing time-series.
   * The document must contain the unique identifier for the time-series to append to.
   * 
   * @param document  the time series document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  void appendTimeSeries(TimeSeriesDocument<T> document);

  /**
   * Removes all data points before the given date.
   * <p>
   * This is a bulk update method to remove multiple data points from an existing time-series.
   * 
   * @param timeSeriesUid  the time-series to operate on, not null
   * @param firstDateToRetain  remove all data points before this date, not null
   */
  void removeDataPoints(UniqueIdentifier timeSeriesUid, T firstDateToRetain);

  //-------------------------------------------------------------------------
  /**
   * Searches for a timeseries unique identifier matching specific criteria.
   * 
   * @param securityBundle  the security identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @return the unique identifier, null if not found
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField);

  /**
   * Searches for a timeseries unique identifier matching specific criteria.
   * 
   * @param securityBundle  the security identifier bundle, not null
   * @param currentDate  the current trade date if applicable
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @return the unique identifier, null if not found
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField);

}
