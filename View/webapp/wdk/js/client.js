/**
 * Short-term namespace for the future Flux-based Wdk object. This serves to allow integrating
 * with the legacy code-base more easily.
 */

import * as WdkClient from './client/main';

wdk.namespace('wdk.client', ns => {
  let initialized = false;

  /** initialize the client, and expose runtime objects */
  function initialize(config) {
    let runtime = WdkClient.initialize(config);
    Object.assign(ns, { runtime });
    return runtime;
  }

  Object.assign(ns, WdkClient, { initialize });
});
