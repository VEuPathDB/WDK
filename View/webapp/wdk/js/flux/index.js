/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

import React from 'react';
import Router from 'react-router';
import Immutable from 'immutable';
import _ from 'lodash';
import Application from './application';

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.Immutable == null) window.Immutable = Immutable;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = Router;


export default {

  /**
   * Starts a WDK application instance based on the provided configuration.
   *
   * @param {object} config Application configuration
   * @param {string} config.serviceUrl Base URL for the RESTful WDK Service
   * @param {element} config.rootElement Root element to render application
   * @param {function} config.recordComponentResolver Function used to resolve
   *        a record component based on the record class name. The function
   *        will be called with the record class name and a reference to the
   *        default record component. This is useful for wrapping or for using
   *        the default without modifications.
   */
  createApplication: function(config) {
    return Application.createApplication(config);
  }

};
