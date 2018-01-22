/* global __DEV__ */
import {identity, mapValues, values} from 'lodash';
import {createElement} from 'react';
import * as ReactDOM from 'react-dom';
import {createBrowserHistory} from 'history';

import Dispatcher from './dispatcher/Dispatcher';
import WdkService from './utils/WdkService';
import { isPromise } from './utils/PromiseUtils';
import { getTransitioner } from './utils/PageTransitioner';
import Root from './controllers/Root';
import { loadAllStaticData } from './actioncreators/StaticDataActionCreators';
import { updateLocation } from './actioncreators/RouterActionCreators';
import WdkStore from './stores/WdkStore';

import * as Components from './components';
import * as Stores from './stores';
import * as Controllers from './controllers';
import wdkRoutes from './routes';

/**
 * Initialize the application.
 *
 * @param {object} options
 * @param {string} options.rootUrl Root URL used by the router. If the current
 *   page's url does not begin with this option's value, the application will
 *   not render automatically.
 * @param {string|HTMLElement} options.rootElement Where to mount the
 *   application. Can be a selector string or an element. If this option does
 *   not resolve to an element after the DOMContentLoaded event is fired, the
 *   application will not render automatically.
 * @param {string} options.endpoint Base URL for WdkService.
 * @param {Function} options.wrapRoutes A function that takes a WDK Routes React
 *   Element and returns a React Element.
 * @param {object} options.storeWrappers Mapping from store name to replacement
 *   class
 * @param {Function} options.onLocationChange Callback function called whenever
 *   the location of the page changes. The function is called with a Location
 *   object.
 */
export function initialize(options) {
  let { rootUrl, rootElement, endpoint, wrapRoutes = identity, storeWrappers, onLocationChange } = options;
  let canUseRouter = location.pathname.startsWith(rootUrl);
  // define the elements of the Flux architecture

  let history = canUseRouter && createBrowserHistory({ basename: rootUrl });
  let wdkService = WdkService.getInstance(endpoint);
  let transitioner = getTransitioner(history);
  let services = { wdkService, transitioner };
  let dispatcher = new Dispatcher();
  let makeDispatchAction = getDispatchActionMaker(dispatcher, services);
  let stores = configureStores(dispatcher, storeWrappers, services);

  // load static WDK data into service cache and view stores that need it
  let dispatchAction = makeDispatchAction('global');
  dispatchAction(loadAllStaticData());

  // log all actions in dev environments
  if (__DEV__) logActions(dispatcher, stores);

  if (canUseRouter) {
    // render the root element once page has completely loaded
    document.addEventListener('DOMContentLoaded', function() {
      let container = rootElement instanceof HTMLElement
        ? rootElement
        : document.querySelector(rootElement);
      let handleLocationChange = location => {
        onLocationChange(location);
        dispatchAction(updateLocation(location));
      };
      if (container != null) {
        let applicationElement = createElement(
          Root, {
            rootUrl,
            makeDispatchAction,
            stores,
            history,
            routes: wrapRoutes(wdkRoutes),
            onLocationChange: handleLocationChange
          });
        ReactDOM.render(applicationElement, container);
      }
      else if (__DEV__) {
        console.log('Could not resolve rootElement %o. Application will not render automatically.', rootElement);
      }
    });
  }
  else if (__DEV__) {
    console.log('The current page url does not start with the rootUrl %o. Application router will not be rendered.', rootUrl);
  }

  // return WDK application components
  return { wdkService, dispatchAction, stores, makeDispatchAction };
}

/**
 * Creates a Map<StoreClass, Store>.
 *
 * @param {Dispatcher} dispatcher
 * @param {Object} storeWrappers Named functions that return store override classes
 */
function configureStores(dispatcher, storeWrappers, services) {
  const storeProviderTupleByKey = wrapStores(storeWrappers);
  const GlobalDataStore = storeProviderTupleByKey.GlobalDataStore[1];
  const globalDataStore = new GlobalDataStore(dispatcher);
  return new Map(Object.entries(storeProviderTupleByKey)
    .filter(([key]) => key !== 'GlobalDataStore')
    .map(([key, [Store, Provider]]) =>
      [Store, new Provider(dispatcher, key, globalDataStore, services)]))
}

/**
 * Apply WDK Store wrappers. Keys of `storeWrappers` should correspond to WDK
 * Store names. Values of `storeWrappers` are functions that take the current
 * Store class and return a new Store class.
 *
 * If a Store wrapper provides an unknown key, it will be created as a new
 * application store, and it will be passed WdkStore as a base implementation.
 *
 * This function returns an object whose keys are a union of the keys from
 * `Stores` and the keys from `storeWrappers`, and whose values are a 2-element
 * array where the first element is the original Store, and whose second
 * element is the wrapped Store. In the case of a new application Store, both
 * elements will be the application Store.
 *
 * @param {Object} storeWrappers
 * @return {Record<key, StoreProviderTuple>}
 */
function wrapStores(storeWrappers) {
  // init with noop wdk store tuple
  const finalStoreProviders = mapValues(Stores, Store => [Store, Store]);

  if (storeWrappers != null) {
    Object.entries(storeWrappers).forEach(function([key, storeWrapper]) {
      const Store = Stores[key];
      if (Store == null) {
        console.log("Creating new application store: `%s`.", key);
      }
      const storeWrapperType = typeof storeWrapper;
      if (storeWrapperType !== 'function') {
        console.error("Expected Store wrapper for %s to be a `function`, " +
          "but is `%s`. Skipping...", key, storeWrapperType);
        return;
      }
      const Provider = storeWrapper(Store == null ? WdkStore : Store);

      finalStoreProviders[key] = [ Store == null ? Provider : Store, Provider ];
    });
  }

  return finalStoreProviders;
}

/**
 * Create a function that takes a channel and creates a dispatch function
 * `dispatchAction` that forwards calls to `dispatcher.dispatch` using the
 * channel as a scope for the audience of the action.  In dispatchAction:
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
 * @param {String} rootUrl
 * @param {Dispatcher} dispatcher
 * @param {Object?} serviceSubset
 */
export function getDispatchActionMaker(dispatcher, services) {
  let logError = console.error.bind(console, 'Error in dispatchAction:');
  return function makeDispatchAction(channel) {
    if (channel === undefined) {
      console.warn("Call to makeDispatchAction() with no channel defined.");
    }
    return function dispatchAction(action) {
      if (typeof action === 'function') {
        // Call the function with dispatchAction and services
        // TODO Change this to `dispatchAction(action(services))`. Doing this alone will make it impossible
        // for an ActionCreator to dispatch multiple actions. We can either handle an array as a case below,
        // and call dispatchAction on each item of the array, or more generally we can support iterables.
        // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols
        return action(dispatchAction, services);
      }
      else if (isPromise(action)) {
        return action.then(result => dispatchAction(result)).then(undefined, logError);
      }
      else if (action == null) {
        console.error("Warning: Action received is not defined or is null");
      }
      else if (action.type == null) {
        console.error("Warning: Action received does not have a `type` property", action);
      }
      if (action != null) {
        // assign channel if requested
        action.channel = (action.isBroadcast ? undefined : channel);
      }
      return dispatcher.dispatch(action);
    };
  };
}

/**
 * Apply Component wrappers to WDK components and controllers. Keys of
 * 'componentWrappers' should correspond to Component or Controller names in
 * WDK. Values of `componentWrappers` are factories that return a new component.
 *
 * Note that this function applies wrappers "globally", meaning that all apps
 * returned by initialize will use the wrapped components, regardless of when
 * initialize and wrapComponents are called.
 *
 * @param {Object} componentWrappers
 */
export function wrapComponents(componentWrappers) {
  for (let key in componentWrappers) {
    // look in Components for class by this name
    let Component = Components[key];
    // if not found, look in Controllers
    if (Component == null) {
      Component = Controllers[key];
    }
    // if still not found, warn and skip
    if (Component == null) {
      console.warn("Cannot wrap unknown WDK Component '" + key + "'.  Skipping...");
      continue;
    }
    // if found component/controller is not wrappable, log error and skip
    if (!("wrapComponent" in Component)) {
      console.error("Warning: WDK Component `%s` is not wrappable.  WDK version will be used.", key);
      continue;
    }
    // wrap found component/controller
    Component.wrapComponent(componentWrappers[key]);
  }
}

/**
 * Log all actions and Store state changes to the browser console.
 *
 * @param {Dispatcher} dispatcher
 * @param {Object} stores
 */
function logActions(dispatcher, storeMap) {
  return;
  let stores = Array.from(storeMap.values())
    .reduce(function(stores, store) {
      return Object.assign(stores, {[store.channel]: store});
    }, {});
  dispatcher.register(action => {
    dispatcher.waitFor(values(stores).map(s => s.getDispatchToken()));
    console.group(action.type);
    console.info("dispatching", action);
    console.info("state", mapValues(stores, store => store.getState()));
    console.groupEnd(action.type);
  });
}
