/*global RSVP */

import './core';
import './user';
import './models';
import './plugins';
import './components';
import './views';
import './controllers';

import React from 'react';
import Router from 'react-router';
import { routes } from './flux/router';

(function($) {
  'use strict';

  var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;

  // uncomment next line to enable query string flag to force loadOnInterval
  // if (/\b__interval=true\b/.test(location.search)) MutationObserver = false;

  // Don't allow RSVP to "swallow" errors.
  // This will cause inspectors to break
  // here, from which one can look up the
  // call chain.
  RSVP.on('error', function(err) {
    console.assert(false, err);
  });

  // Start the application. The ready callback is invoked
  // once the DOM has finished rendering.
  var app = wdk.application = wdk.app = wdk.core.Application.create({

    ready: function wdkReady() {

      /**
       * XXX This is transitional code and will be deprecated
       *
       * The current use case is to move pages piecemeal into
       * the new architecture. For instance, the Datasets page
       * used by EuPathDB sites will point to a specific Answer
       * page: /answer/DataQuestions.AllDatasets.
       */
      $('[data-route]').each((index, el) => {
        var route = el.getAttribute('data-route');
        // run the router
        Router.run(routes, route, Handler => React.render(<Handler/>, el));
      });

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
  wdk.questionView = function registerQuestionView() {
    var name = 'question:' + arguments[0];
    var rest = [].slice.call(arguments, 1);
    var args = [].concat(name, rest);
    app.registerView.apply(app, args);
    return wdk;
  };

  // Global event handlers
  // need to call draw on dataTables that are children of a tab panel
  $(document).on('tabsactivate', function() {
    $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
  });

  $(window).on('resize', _.throttle(function() {
    $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
  }, 100));


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
      return mutation.addedNodes.length > 0
        ? acc.concat([ mutation.target ])
        : acc;
    }, []));
    if (targets.length > 0) rafLoad($(targets));
  }

}(jQuery));
