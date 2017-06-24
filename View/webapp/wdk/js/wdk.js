/**
 * This module will eventually be used to expose a public API for interacting
 * with WDK. At this point in time, a lot of WDK functions are publicly
 * available via the global `wdk` object that don't need to be, and public
 * facing documentation is lacking.
 *
 * In addition to defining public functions, this module can also serve as an
 * entry point for generating API documentation.
 */

// create internal packages
import 'babel-polyfill';
import './vendor';
import './core';
import './user';
import './plugins';
import './components';
import './controllers';

import { initialize as initializeContext, wrapComponents } from './client/main';

import * as Stores from './client/stores';
import * as Controllers from './client/controllers';
import * as Components from './client/components';
import * as ActionCreators from './client/actioncreators';

import * as ComponentUtils from './client/utils/componentUtils';
import * as IterableUtils from './client/utils/IterableUtils';
import * as PromiseUtils from './client/utils/PromiseUtils';
import * as ReporterUtils from './client/utils/reporterUtils';
import * as TreeUtils from './client/utils/TreeUtils';
import * as OntologyUtils from './client/utils/OntologyUtils';
import * as CategoryUtils from './client/utils/CategoryUtils';
import * as StaticDataUtils from './client/utils/StaticDataUtils';
import * as FormSubmitter from './client/utils/FormSubmitter';
import LazyFilterService from './client/utils/LazyFilterService';
import * as AuthUtil from './client/utils/AuthUtil';
import WdkService from './client/utils/WdkService';

/**
 * Initialize the Wdk application.
 */
export function initialize(...args) {
  return window.wdk.context = initializeContext(...args);
}

export {
  wrapComponents,
  Controllers,
  Components,
  ActionCreators,
  ComponentUtils,
  IterableUtils,
  PromiseUtils,
  ReporterUtils,
  OntologyUtils,
  CategoryUtils,
  StaticDataUtils,
  FormSubmitter,
  Stores,
  TreeUtils,
  LazyFilterService,
  WdkService,
  AuthUtil
};
