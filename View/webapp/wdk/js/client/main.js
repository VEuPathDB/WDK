import './exposeModules';

import mapValues from 'lodash/object/mapValues';
import values from 'lodash/object/values';

import Dispatcher from './dispatcher/Dispatcher';
import WdkService from './utils/WdkService';
import * as Router from './router';
import * as ActionCreators from './actioncreators';
import * as Components from './components';
import * as Stores from './stores';
import * as ComponentUtils from './utils/componentUtils';
import * as IterableUtils from './utils/IterableUtils';
import * as ReporterUtils from './utils/reporterUtils';
import * as TreeUtils from './utils/TreeUtils';
import * as OntologyUtils from './utils/OntologyUtils';
import * as SearchableTreeUtils from './utils/SearchableTreeUtils';
import * as FormSubmitter from './utils/FormSubmitter';
import * as WdkUtils from './utils/WdkUtils';

export { Components, ComponentUtils, ReporterUtils, FormSubmitter, WdkUtils, IterableUtils, TreeUtils, OntologyUtils, SearchableTreeUtils };

/**
 * Run the application.
 *
 * @param {string} option.rootUrl Root URL used by the router.
 * @param {string} option.endpoint Base URL for WdkService.
 * @param {HTMLElement} option.rootElement DOM node to render the applicaiton.
 * @param {Array} option.applicationRoutes Addtional routes to register with the Router.
 */
export function run({ rootUrl, endpoint, rootElement, applicationRoutes }) {
  let dispatcher = new Dispatcher;
  let wdkService = new WdkService(endpoint);
  let dispatchAction = makeDispatchAction(dispatcher, { wdkService });
  let stores = mapValues(Stores, Store => new Store(dispatcher));

  let context = {
    dispatchAction,
    stores,
  };

  if (__DEV__) logActions(dispatcher, stores);

  let router = Router.start(rootUrl, rootElement, context, applicationRoutes);

  return Object.assign({ router }, context);
}

/**
 * Create a dispatch function `dispatchAction` that forwards calls to
 * `dispatcher.dispatch`.
 *
 * If `action` is a function, it will be called with `dispatchAction` and
 * `services`. Calling it with `dispatchAction` allows for composability since
 * an action function can in turn call another action function. This is useful
 * for creating higher-order dispatch helpers, such as latest, once, etc.
 *
 * If `action` is an object, `dispatcher.dispatch` will be called with it.
 *
 * An `action` function should ultimately return an object to invoke a dispatch.
 *
 * @param {Dispatcher} dispatcher
 * @param {any?} services
 */
function makeDispatchAction(dispatcher, services) {
  return function dispatchAction(action) {
    if (typeof action === 'function') {
      // Call the function with dispatchAction and services
      return action(dispatchAction, services);
    }
    else {
      return dispatcher.dispatch(action);
    }
  }
}

export function wrapComponents(componentWrappers) {
  for (let key in componentWrappers) {
    let Component = Components[key];
    if (Component == null) {
      console.warn("Cannot wrap unknown WDK Component '" + key + "'.  Skipping...");
      continue;
    }
    if (!("wrapComponent" in Components[key])) {
      console.warn("WDK Component '" + key + "' is not wrappable.  WDK version will be used.");
      continue;
    }
    Components[key].wrapComponent(componentWrappers[key]);
  }
}

export function wrapStores(storeWrappers) {
  for (let key in storeWrappers) {
    let Store = Stores[key];
    if (Store == null) {
      console.warn("Cannot wrap unknown WDK Store '" + key + "'.  Skipping...");
      continue;
    }
    // wrapper should be an object with two function properties
    let override = storeWrappers[key];
    [ 'getInitialState', 'reduce' ].forEach(method => {
      if (method in override) {
        // actually reset prototype method to a custom implementation
        Store.prototype[method] = override[method](Store.prototype[method]);
      }
    });
  }
}

function logActions(dispatcher, stores) {
  // Debug logging - TODO Only enable in development environments
  dispatcher.register(action => {
    dispatcher.waitFor(values(stores).map(s => s.getDispatchToken()));
    console.group(action.type);
    console.info("dispatching", action);
    console.info("state", mapValues(stores, store => store.getState()));
    console.groupEnd(action.type);
  });
}
