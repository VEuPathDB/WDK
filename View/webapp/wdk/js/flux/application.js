import noop from 'lodash/utility/noop';
import React from 'react';
import Router from 'react-router';
import { getRoutes } from './router';
import createObjectCache from './utils/createObjectCache';

var createRouterCallback = function createRouterCallback(rootElement, context) {
  return function runRoute(Handler, state) {
    React.render( <Handler {...state} application={context} />, rootElement);
  };
};

var runApplication = function runApplication({
  baseUrl,
  dispatcher,
  serviceAPI,
  stores,
  actionCreators,
  rootElement,
  routerLocation,
  recordComponentResolver = noop,
  cellRendererResolver = noop
}) {

  if (dispatcher == null) {
    throw Error('A dispatcher was not defined');
  }
  if (serviceAPI == null) {
    throw Error('A serviceAPI was not defined');
  }

  var routes = getRoutes(baseUrl);
  var storeCache = createObjectCache(stores, dispatcher);
  var actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);

  // This object is passed to the top level React component, and any other
  // Route handlers. This is effectively a lookup service.
  //
  // TODO Warn or throw when a requested object is not found.
  var applicationContext = {
    getStore(name) {
      return storeCache.get(name).asObservable();
    },
    getActions(name) {
      return actionCreatorsCache.get(name);
    },
    getRecordComponent(recordClass, defaultComponent) {
      return recordComponentResolver(recordClass, defaultComponent);
    },
    getCellRenderer(recordClass, defaultRenderer) {
      return cellRendererResolver(recordClass, defaultRenderer);
    }
  };

  var routerCallback = createRouterCallback(rootElement, applicationContext);
  Router.run(routes, routerLocation, routerCallback);
};

export { runApplication };
