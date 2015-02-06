import Flux from 'flux';
import React from 'react';
import Router from 'react-router';
import createServiceAPI from './utils/createServiceAPI';
import * as stores from './stores';
import * as actionCreators from './actions';
import { appRoutes } from './router';

export function createApplication(options) {
  options = options || {};

  return {
    config(moreOptions) {
      if (moreOptions) {
        Object.assign(options, moreOptions);
      }
    },

    run(moreOptions) {
      this.config(moreOptions);
      runApplication(options);
    }
  };
}

export function createObjectCache(factories, ...deps) {
  var _cache = {};
  var get = function get(token) {
    var object = _cache[token];
    if (typeof object === 'undefined') {
      object = _cache[token] = factories[token](...deps);
    }
    return object;
  };
  return { get };
}

export function runApplication(options) {
  var serviceUrl = options.serviceUrl;

  if (!serviceUrl) {
    throw new Error('A serviceUrl must be provided');
  }

  var dispatcher = new Flux.Dispatcher();
  var serviceAPI = createServiceAPI(serviceUrl);
  var storeCache = createObjectCache(stores, dispatcher);
  var actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);

  // This is passed to Controller Views. This will also be exposed on the wdk
  // object this module exports. A companion `register` function will also be
  // exposed to allow wdk consumers to inject other services.
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
      React.render( <Handler/>, document.body);
    });
  });
}
