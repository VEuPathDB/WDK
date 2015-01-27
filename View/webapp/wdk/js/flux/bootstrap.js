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
import React from 'react';
import Router from 'react-router';
import { appRoutes } from './router';
import { config as configService } from './ServiceAPI';

/**
 * TODO Provide a more comprehensive configuration module. Possibly look into
 * dependency injection libraries (wire.js is one that looks good). This will
 * make it much easier to manage the needs of WDK client consumers.
 */
var wdk = {
  config(spec) {
    configService({ serviceUrl: spec.serviceUrl });
  },

  run(spec) {
    if (spec) {
      this.config(spec);
    }
    Router.run(appRoutes, function runRoute(Handler, state){
      React.render(<Handler {...state}/>, document.body);
    });
  },

  // expose libraries, e.g. wdk._ or wdk.React
  _, React, Router, appRoutes
};

export default wdk;
