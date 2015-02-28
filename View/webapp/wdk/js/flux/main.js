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
const createApplication = function createApplication(config) {
  const { serviceUrl, rootElement } = config;

  if (typeof serviceUrl !== 'string') {
    throw new Error(`Application serviceUrl ${serviceUrl} must be a string.`);
  }

  if (!(rootElement instanceof Element)) {
    throw new Error(`Application rootElement ${rootElement} must be a DOM element.`);
  }

  const location = config.location === 'none'
    ? new HeadlessLocation(config.defaultRoute || '/')
    : Router.HashLocation;

  const dispatcher = new Flux.Dispatcher();
  const serviceAPI = createServiceAPI(serviceUrl);

  runApplication({
    rootElement,
    location,
    dispatcher,
    serviceAPI,
    stores,
    actionCreators,
    recordComponentResolver: config.recordComponentResolver
  });
};

export default { createApplication, React };
