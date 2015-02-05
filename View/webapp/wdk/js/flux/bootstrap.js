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
import Flux from 'flux';
import React from 'react';
import Router from 'react-router';
import createServiceAPI from './utils/createServiceAPI';
import { appRoutes } from './router';
import * as stores from './stores';
import * as actions from './actions';

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
      Object.assign(this._spec, spec);
    }

    var serviceUrl = this._spec.serviceUrl;

    if (!serviceUrl) {
      throw new Error('A serviceUrl must be provided');
    }

    // Create a provider for a configured ServiceAPI.
    // This will cause injector.get(ServiceAPI) to
    // return the result of getServiceAPI().
    var serviceAPIProvider = function() { return createServiceAPI(serviceUrl) };
    di.annotate(serviceAPIProvider, new di.Provide('serviceAPI'));

    // Wrap Flux.Dispatcher so we don't pollute its static properties
    var dispatcherProvider = function() { return new Flux.Dispatcher() };
    di.annotate(dispatcherProvider, new di.Provide('dispatcher'));

    var storeProviders = Object.keys(stores).map(name => {
      var store = stores[name];
      di.annotate(store, new di.Provide(name));
      return store;
    });

    var actionCreatorsProviders = Object.keys(actions).map(name => {
      var action = actions[name];
      di.annotate(action, new di.Provide(name));
      return action;
    });

    var injector = new di.Injector([
      serviceAPIProvider,
      dispatcherProvider,
      ...storeProviders,
      ...actionCreatorsProviders
    ]);

    // This is passed to Controller Views. This will also be exposed on the wdk
    // object this module exports. A companion `register` function will also be
    // exposed to allow wdk consumers to inject other services.
    var lookup = function lookup(token) {
      return injector.get(token);
    };

    Router.run(appRoutes, function runRoute(Handler){
      React.withContext( { lookup }, function() {
        React.render( <Handler/>, document.body);
      });
    });
  },

  // expose libraries, e.g. wdk._ or wdk.React
  _, React, Router, appRoutes
};

export default wdk;
