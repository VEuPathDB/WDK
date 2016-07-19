/**
 * Short-term namespace for the future Flux-based Wdk object. This serves to allow integrating
 * with the legacy code-base more easily.
 */

/* global wdk */
import * as WdkClient from './client/main';

wdk.namespace('wdk.client', ns => {
  let initialized = false;

  // Define runtime property such that when it is accessed, a warning error is
  // logged indicating that it has not been initialized. The runtime property
  // is updated in `initialize` below.
  Object.defineProperty(ns, 'runtime', {
    configurable: true,
    get() {
      let error = new Error("Warning: Attempting to access Wdk client " +
                            "runtime before it is initialized.");
      console.error(error);
    }
  });

  /** initialize the client, and expose runtime objects */
  function initialize(config) {
    if (initialized) {
      let error = new Error("Warning: Wdk client has already been " +
                            "initialized. Doing nothing.");
      console.error(error);
      return;
    }
    let runtime = WdkClient.initialize(config);
    // create and add dispatchAction function with channel 'legacy'
    Object.assign(runtime, { dispatchAction: runtime.makeDispatchAction('legacy')})
    Object.defineProperty(ns, 'runtime', {
      value: runtime
    });
    initialized = true;
    return runtime;
  }

  // Copy properties from WdkClient into namespace, and override initialize with
  // the local proxy.
  Object.assign(ns, WdkClient, { initialize });
});
