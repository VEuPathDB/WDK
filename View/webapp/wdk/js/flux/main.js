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
import stores from './stores';
import actionCreators from './actions';
import { appRoutes } from './router';

/**
 * Starts a WDK application instance based on the provided configuration.
 *
 * @param {object} config Application configuration
 * @param {string} config.serviceUrl Base URL for the RESTful WDK Service
 * @param {element} config.rootElement Root element to render application
 */
function createApplication(config) {
  var { serviceUrl, rootElement } = config;

  if (typeof serviceUrl !== 'string') {
    throw new Error(`Application serviceUrl ${serviceUrl} must be a string.`);
  }

  if (!(rootElement instanceof Element)) {
    throw new Error(`Application rootElement ${rootElement} must be a DOM element.`);
  }

  var dispatcher = new Flux.Dispatcher();
  var serviceAPI = createServiceAPI(serviceUrl);
  var storeCache = createObjectCache(stores, dispatcher);
  var actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);

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
  var reactContext = {
    getStore(name) {
      return storeCache.get(name);
    },
    getActions(name) {
      return actionCreatorsCache.get(name);
    }
  };

  Router.run(appRoutes, function runRoute(Handler){
    React.withContext(reactContext, function() {
      React.render( <Handler/>, rootElement);
    });
  });
}

// expose libraries, e.g. wdk._ or wdk.React
export default { createApplication, appRoutes, _, React, Router };
