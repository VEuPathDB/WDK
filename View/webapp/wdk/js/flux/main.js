/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

// Include the babel polyfill. This adds global objects expected in parts of our
// code base, such as Promise, and the runtime needed for generators.
// import 'babel/polyfill';
// import 'babel/browser'; // remove this before going to prod

import React from 'react';
import Flux from 'flux';
import Router from 'react-router';

import createServiceAPI from './utils/createServiceAPI';
import HeadlessLocation from './utils/HeadlessLocation';
import stores from './stores';
import actionCreators from './actions';
import { runApplication } from './application';

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
var createApplication = function createApplication(config) {
  var {
    baseUrl,
    serviceUrl,
    rootElement,
    recordComponentResolver,
    cellRendererResolver,
    location
  } = config;

  if (typeof serviceUrl !== 'string') {
    throw new Error(`Application serviceUrl ${serviceUrl} must be a string.`);
  }

  if (!(rootElement instanceof Element)) {
    throw new Error(`Application rootElement ${rootElement} must be a DOM element.`);
  }

  var dispatcher = new Flux.Dispatcher();
  var serviceAPI = createServiceAPI(serviceUrl);

  // Determine Router Location implementation based on config options.
  var routerLocation;

  if (location == 'none') {
    routerLocation = new HeadlessLocation(config.defaultRoute || '/');
  }
  else if (baseUrl) {
    routerLocation = Router.HistoryLocation;
  }
  else {
    routerLocation = Router.HashLocation;
  }

  runApplication({
    baseUrl,
    rootElement,
    routerLocation,
    dispatcher,
    serviceAPI,
    stores,
    actionCreators,
    recordComponentResolver,
    cellRendererResolver
  });
};

export default { createApplication, React };
