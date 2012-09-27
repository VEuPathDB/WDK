(function($) {
  "use strict";

  // @context:Object - The object to apply callback invokations to.
  //                   Defaults to window object.
  var EventDispatcher = function(context) {

    /* Private */

    // hash of eventType => array of callback functions
    var callbacks = {},
        handleRegex = /(.*)_(\d+)$/;

    // if conext is undefined, set it to window
    context = (context === undefined) ? window : context;


    /* Public */

    // TODO - add ability to unsubscribe a callback?

    // subscribe to an event type
    //
    // @eventType:String - The name of an event type
    // @callback:Function - the function to invoke when event type is published
    this.subscribe = function(eventType, callback) {
      if ( !(callback instanceof Function)) {
        return false;
      }
      if (callbacks[eventType] === undefined) {
        callbacks[eventType] = [callback];
      } else {
        callbacks[eventType].push(callback);
      }
      // return a handle for callback. Might be useful if we implement
      // unsubscribe feature.
      return eventType + "_" + callbacks[eventType].length;
    };

    // invoke callback functions registered for event type
    //
    // @eventType:String - the event type to publish
    this.publish = function(eventType /*, arg1, arg2, ... */) {
      var cbs = callbacks[eventType],
          args = Array.prototype.slice.call(arguments, 1);
      
      if (cbs !== undefined) {
        $.each(cbs, function() {
          this.apply(context, args);
        });
      }

      return this;
    };

  };

  window.wdkEvent = new EventDispatcher();

}(jQuery));