import $ from 'jquery';
import _ from 'lodash';

import './vendor';
import './core';
import './user';
import './models';
import './plugins';
import './components';
import './views';
import './controllers';

var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver; // jshint ignore:line

// uncomment next line to enable query string flag to force loadOnInterval
// if (/\b__interval=true\b/.test(location.search)) MutationObserver = false;

// Start the application. The ready callback is invoked
// once the DOM has finished rendering.
var app = wdk.application = wdk.app = wdk.core.Application.create({

  ready: function wdkReady() {
    wdk.cookieTest();
    wdk.setUpDialogs();
    wdk.setUpPopups();
    wdk.load();

    if (MutationObserver) {
      wdk._renderObserver = loadOnMutation(); // expose for debugging
    } else {
      wdk._renderInterval = loadOnInterval();
    }
  }

});

// Sugar to register custom question views.
// wdk.views.core.QuestionView delegates to these views
wdk.questionView = function registerQuestionView(questionName, ...rest) {
  var name = 'question:' + questionName;
  app.registerView(name, ...rest);
  return wdk;
};

// Global event handlers
// need to call draw on dataTables that are children of a tab panel
$(document).on('tabsactivate', function() {
  $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
});

// Break bfcache. The handler doesn't have to do anything for the desired
// effect. See redmine #18839.
$(window).on('unload', function() { });

// Helper functions
// ----------------

// call wdk.load using setInterval
/* jshint unused:false */
function loadOnInterval() {
  return setInterval(wdk.load, 200);
}

/** EXPERIMENTAL **/
// call wdk.load based on DOM mutation
/* jshint unused:false */
function loadOnMutation() {
  var target = document.body;
  var config = { childList: true, subtree: true };
  var observer = new MutationObserver(loadUniqueMutationTargets);

  observer.observe(target, config);

  $(window).on('beforeunload', function() {
    observer.disconnect();
  });

  return observer;
}

function rafLoad($target) {
  requestAnimationFrame(_.partial(wdk.load, $target));
}

var throttledLoad = _.throttle(wdk.load, 100);

var throttledRafLoad = _.throttle(rafLoad, 100);

// get unique set of targets with addedNodes
function loadUniqueMutationTargets(mutations) {
  var targets = _.uniq(mutations.reduce(function(acc, mutation) {
    return mutation.addedNodes.length > 0 && !mutation.target.getAttribute('data-reactid')
      ? acc.concat([ mutation.target ])
      : acc;
  }, []));
  if (targets.length > 0) rafLoad($(targets));
}
