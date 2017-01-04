// Import scripts that expose global vars
import '!!script!../lib/jquery';
import '!!script!../lib/jquery-migrate-1.2.1.min';
import '!!script!../lib/jquery-ui';

import '!!script!../lib/jquery.cookie';
import '!!script!../lib/jquery.blockUI';
import '!!script!../lib/flexigrid';
import '!!script!../lib/select2.min';
import '!!script!../lib/jstree/jquery.jstree';

import '!!script!../lib/jquery.qtip.min';
import '!!script!../lib/flot/jquery.flot.min';
import '!!script!../lib/flot/jquery.flot.categories.min';
import '!!script!../lib/flot/jquery.flot.selection.min';
import '!!script!../lib/flot/jquery.flot.time.min';
import '!!script!../lib/datatables.min';
import '!!script!../lib/datatables-natural-type-plugin';
import '!!script!../lib/spin.min';
import '!!script!../lib/zynga-scroller/Animate';
import '!!script!../lib/zynga-scroller/Scroller';

// Make libraries available globally
import lodash from 'lodash';
window._ = lodash;

import * as React from 'react';
window.React = React;

import * as ReactDOM from 'react-dom';
window.ReactDOM = ReactDOM;

import * as ReactRouter from 'react-router';
window.ReactRouter = ReactRouter;

if (process.env.NODE_ENV !== 'production') {
  window.ReactPerf = require('react-addons-perf');
}
