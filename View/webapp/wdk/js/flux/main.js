/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

// Include the 6to5 polyfill. This adds global objects expected in parts of our
// code base, such as Promise, and the runtime needed for generators.
import '6to5/polyfill';

import _ from 'lodash';
import Flux from 'flux';
import React from 'react';
import Router from 'react-router';

import createServiceAPI from './utils/createServiceAPI';
import createObjectCache from './utils/createObjectCache';
import HeadlessLocation from './utils/HeadlessLocation';
import stores from './stores';
import actionCreators from './actions';
import { routes } from './router';

/**
 * Starts a WDK application instance based on the provided configuration.
 *
 * @param {object} config Application configuration
 * @param {string} config.serviceUrl Base URL for the RESTful WDK Service
 * @param {element} config.rootElement Root element to render application
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
  const storeCache = createObjectCache(stores, dispatcher);
  const actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);

  // This is used below in `React.withContext`. Properties of this object will
  // be available in React components that declare them using the
  // `contextTypes` property. React will read this propery on each component
  // and expose the defined context properties (via `this.context`).
  //
  // EXAMPLE
  //
  //     // ...
  //
  //     contextTypes: {
  //       getStore: React.PropTypes.func.isRequired
  //     },
  //
  //     getStore: function(storeName) {
  //         return this.context.getStore(storeName);
  //     }
  //
  //     // ...
  //
  // See `./mixins/createStoreMixin` for an example of its usage.
  const reactContext = {
    getStore(name) {
      return storeCache.get(name);
    },
    getActions(name) {
      return actionCreatorsCache.get(name);
    }
  };

  const router = Router.run(routes, location, function runRoute(Handler){
    React.withContext(reactContext, function() {
      React.render( <Handler/>, rootElement);
    });
  });
}

// expose libraries, e.g. wdk._ or wdk.React
export default { createApplication, routes, _, React, Router };
