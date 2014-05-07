// functional utility functions
wdk.namespace('wdk.fn', function(ns) {

  var __slice = [].slice;

  // Decorates (or wraps) a function such that it stops event propagation.
  //
  // The canonical use case is, you want to attach a function as an event
  // handler, but the only thing you want to do with the event is stop it.
  //
  //     $(selector).on('click', preventEvent(callback));
  //
  //     function callback() {
  //       // do something interesting
  //     }
  function preventEvent(fn) {
    return function(event) {
      _.result(event, 'preventDefault');
      fn.apply(this, arguments);
    };
  };

  function stopEvent(fn) {
    return function(event) {
      _.result(event, 'stopPropagation');
      fn.apply(this, arguments);
    };
  }

  _.extend(ns, {
    preventEvent: preventEvent
  });

});
