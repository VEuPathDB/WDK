/**
 * This module will eventually be used to expose a public API for interacting
 * with WDK. At this point in time, a lot of WDK functions are publically
 * available that don't need to be, and public facing documentation is lacking.
 *
 * In addition to defining public functions, this module can also serve as an
 * entry point for generating API documentation.
 */

import './vendor';
import './core';
import './user';
import './models';
import './plugins';
import './components';
import './controllers';
import './client';

export default window.wdk;
