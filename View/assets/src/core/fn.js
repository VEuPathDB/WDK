// functional utility functions
wdk.namespace('wdk.fn', function(ns) {
  'use strict';

  var compose = _.compose,
      result = _.result;

  // Decorates (or wraps) a function such that it prevents default event
  // behavior.
  //
  // The canonical use case is, you want to attach a function as an event
  // handler, but the only thing you want to do with the event is prevent its
  // default behavior.
  //
  //     $(selector).on('click', preventEvent(callback));
  //
  //     function callback() {
  //       // do something interesting
  //     }
  function preventEvent(fn) {
    return function(event) {
      result(event, 'preventDefault');
      fn.apply(this, arguments);
    };
  }

  // Decorates (or wraps) a function such that it stops event propagation.
  //
  // The canonical use case is, you want to attach a function as an event
  // handler, but the only thing you want to do with the event is stop it.
  //
  //     $(selector).on('click', stopEvent(callback));
  //
  //     function callback() {
  //       // do something interesting
  //     }
  function stopEvent(fn) {
    return function(event) {
      result(event, 'stopPropagation');
      fn.apply(this, arguments);
    };
  }

  // Helper to stop and prevent event behavior
  var killEvent = compose(preventEvent, stopEvent);

  _.extend(ns, {
    preventEvent: preventEvent,
    stopEvent: stopEvent,
    killEvent: killEvent
  });

});
