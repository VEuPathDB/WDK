import mapColl from 'lodash/collection/map';
import React from 'react';
import Router from 'react-router';
import actionCreators from './actions';
import Dispatcher from './Dispatcher';
import Stores from './stores';
import Routes from './routes';
import HeadlessLocation from './utils/HeadlessLocation';
import ServiceAPI from './utils/ServiceAPI';
import createObjectCache from './utils/createObjectCache';

function noop() {}

function createApplication(config) {
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

  var dispatcher = Dispatcher.createDispatcher();
  var serviceAPI = ServiceAPI(serviceUrl);

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

  return runApplication({
    baseUrl,
    rootElement,
    routerLocation,
    dispatcher,
    serviceAPI,
    Stores,
    actionCreators,
    recordComponentResolver,
    cellRendererResolver
  });
}

function runApplication(opts = {}) {
  var {
    baseUrl,
    dispatcher,
    serviceAPI,
    Stores,
    actionCreators,
    rootElement,
    routerLocation,
    recordComponentResolver = noop,
    cellRendererResolver = noop
  } = opts;

  if (dispatcher == null) {
    throw Error('A dispatcher was not defined');
  }
  if (serviceAPI == null) {
    throw Error('A serviceAPI was not defined');
  }

  var routes = Routes.getRoutes(baseUrl);
  var storesMap = createStoresMap(Stores, dispatcher);
  var actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);
  var router = Router.create({
    routes: routes,
    location: routerLocation
  });
  var applicationContext = createApplicationContext(
    storesMap,
    actionCreatorsCache,
    recordComponentResolver,
    cellRendererResolver,
    router
  );
  var routerCallback = createRouterCallback(rootElement, applicationContext);
  router.run(routerCallback);
  return applicationContext;
}

// This object is passed to the top level React component, and any other
// Route handlers. This is effectively a lookup service.
//
// TODO Warn or throw when a requested object is not found.
function createApplicationContext(storesMap, actionCreatorsCache, recordComponentResolver, cellRendererResolver, router) {
  return {
    getStore(name) {
      return storesMap.get(name);
    },
    getActions(name) {
      return actionCreatorsCache.get(name);
    },
    getRecordComponent(recordClass, defaultComponent) {
      return recordComponentResolver(recordClass, defaultComponent);
    },
    getCellRenderer(recordClass, defaultRenderer) {
      return cellRendererResolver(recordClass, defaultRenderer);
    },
    getRouter() {
      return router;
    }
  };
}

// XXX Use Factory/Class as key?
function createStoresMap(Factories, dispatcher) {
  var storesMap = new Map();
  mapColl(Factories, function(Factory, storeName) {
    var store = Factory();
    store.register(dispatcher);
    storesMap.set(storeName, store);
  });
  return storesMap;
}

function createRouterCallback(rootElement, context) {
  return function runRoute(Handler, state) {
    React.render( <Handler {...state} application={context} />, rootElement);
  };
}

export default {
  createApplication
};
