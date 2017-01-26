__webpack_public_path__ = wdkConfig.assetsUrl + '/';

import $ from 'jquery';
import _ from 'lodash';

// Public javascript API for working with Wdk
export * from './wdk';

// window.wdk is internal and deprecated for public use
let wdk = window.wdk;


// Bootstrap
// ---------

var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver; // jshint ignore:line

// uncomment next line to enable query string flag to force loadOnInterval
// if (/\b__interval=true\b/.test(location.search)) MutationObserver = false;

// Start the application. The ready callback is invoked
// once the DOM has finished rendering.
$(function wdkReady() {
  wdk.cookieTest();
  wdk.setUpDialogs();
  wdk.setUpPopups();
  wdk.load();

  if (MutationObserver) {
    wdk._renderObserver = loadOnMutation(); // expose for debugging
  } else {
    wdk._renderInterval = loadOnInterval();
  }
});


// Global event handlers
// ---------------------

// Break bfcache. The handler doesn't have to do anything for the desired
// effect. See redmine #18839.
$(window).on('unload', function() { });


// Helper functions
// ----------------

/**
 * call wdk.load using setInterval
 */
function loadOnInterval() {
  return setInterval(wdk.load, 200);
}

/**
 * EXPERIMENTAL
 * call wdk.load based on DOM mutation
 */
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

/**
 * Wrap wdk.load in an animation frame.
 */
function rafLoad($target) {
  requestAnimationFrame(_.partial(wdk.load, $target));
}

/**
 * get unique set of targets with addedNodes
 */
function loadUniqueMutationTargets(mutations) {
  var targets = _.uniq(mutations.reduce(function(acc, mutation) {
    if (mutation.addedNodes.length > 0) acc.push(mutation.target);
    return acc;
  }, []));
  if (targets.length > 0) rafLoad($(targets));
}
