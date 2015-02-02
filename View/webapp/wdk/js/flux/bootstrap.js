/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

// Include the 6to5 polyfill. This adds global objects expected in parts of our
// code base, such as Promise, and the runtime needed for generators.
import '6to5/polyfill';

import di from 'di';
import _ from 'lodash';
import React from 'react';
import Router from 'react-router';
import ServiceAPI from './ServiceAPI';
import { appRoutes } from './router';

/**
 * TODO Provide a more comprehensive configuration module. Possibly look into
 * dependency injection libraries (wire.js is one that looks good). This will
 * make it much easier to manage the needs of WDK client consumers.
 */
var wdk = {
  config(spec) {
    this._spec = spec;
  },

  run(spec) {
    this._spec = this._spec || {};

    if (spec) {
      _.assign(this._spec, spec);
    }

    var serviceUrl = this._spec.serviceUrl;

    if (!serviceUrl) {
      throw new Error('A serviceUrl must be provided');
    }

    // Create a provider for a configured ServiceAPI.
    // This will cause injector.get(ServiceAPI) to
    // return the result of getServiceAPI().
    var getServiceAPI = function getServiceAPI() {
      return new ServiceAPI(serviceUrl);
    };
    di.annotate(getServiceAPI, new di.Provide(ServiceAPI));

    var injector = new di.Injector([ getServiceAPI ]);

    // This is passed to Controller Views. This will also be exposed on the wdk
    // object this module exports. A companion `register` function will also be
    // exposed to allow wdk consumers to inject other services.
    var lookup = function lookup(token) {
      return injector.get(token);
    };

    Router.run(appRoutes, function runRoute(Handler, state){
      React.render( <Handler {...state} lookup={lookup} />, document.body);
    });
  },

  // expose libraries, e.g. wdk._ or wdk.React
  _, React, Router, appRoutes
};

export default wdk;
