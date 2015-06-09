// Make libraries available globally

// Include the babel polyfill. This adds global objects expected in parts of our
// code base, such as Promise, and the runtime needed for generators.
import 'babel/polyfill';

import lodash from 'lodash';
window._ = lodash;

import Backbone from 'backbone';
window.Backbone = Backbone;
