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
import { createApplication } from './runtime';
import { appRoutes } from './router';

// expose libraries, e.g. wdk._ or wdk.React
export default { createApplication, appRoutes, _, React, Router };
