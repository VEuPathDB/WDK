/*global RSVP */
(function($) {
  'use strict';

  var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;

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


  //
  // functions
  //

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

  var throttledLoad = _.throttle(wdk.load, 100);

  var throttledRafLoad = _.throttle(function($target) {
    requestAnimationFrame(_.partial(wdk.load,$target));
  }, 100);

  // get unique set of targets with addedNodes
  function loadUniqueMutationTargets(mutations) {
    _.uniq(mutations.reduce(function(acc, mutation) {
      return mutation.addedNodes.length > 0
        ? acc.concat([ mutation.target ])
        : acc;
    }, []))
    .forEach(function(target) {
      throttledRafLoad($(target));
    });
  }

}(jQuery));
