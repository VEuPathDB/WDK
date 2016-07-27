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
import './vendor';
import './core';
import './user';
import './models';
import './plugins';
import './components';
import './controllers';

import { initialize as initializeContext, wrapComponents } from './client/main';
import * as Components from './client/components';
import * as Stores from './client/stores';
import * as Controllers from './client/controllers';
import * as ComponentUtils from './client/utils/componentUtils';
import * as IterableUtils from './client/utils/IterableUtils';
import * as ReporterUtils from './client/utils/reporterUtils';
import * as TreeUtils from './client/utils/TreeUtils';
import * as OntologyUtils from './client/utils/OntologyUtils';
import * as CategoryUtils from './client/utils/CategoryUtils';
import * as StaticDataUtils from './client/utils/StaticDataUtils';
import * as FormSubmitter from './client/utils/FormSubmitter';

/**
 * Initialize the Wdk application.
 */
export function initialize(...args) {
  let context = initializeContext(...args);
  // Make dispatcher for legacy integration
  window.wdk.context = Object.assign({}, context, {
    dispatchAction: context.makeDispatchAction('__LEGACY__')
  });
  return context;
}

export {
  wrapComponents,
  CategoryUtils,
  ComponentUtils,
  Components,
  Controllers,
  FormSubmitter,
  IterableUtils,
  OntologyUtils,
  ReporterUtils,
  StaticDataUtils,
  Stores,
  TreeUtils
};
