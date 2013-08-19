//wdk.util.namespace("wdk", function(ns, $) {
define(["jquery", "exports", "module"], function($, ns, module) {
  "use strict";

  // @context:Object -  The context in which to invoke callbacks.
  //   Defaults to window object
  var EventDispatcher = function(context) {


    /* Private */


    // hash of eventType => array of callback functions
    var callbacks = {},
        handleRegex = /(.*)_(\d+)$/;

    // if target is falsey, apply to window
    context = context || window;


    /* Public */


    // TODO - add ability to unsubscribe a callback?

    // subscribe to an event type
    //
    // @eventType:String - The name of an event type
    // @callback:Function - the function to invoke when event type is published
    this.subscribe = function(eventType, callback) {
      if (callbacks[eventType] === undefined) {
        callbacks[eventType] = [callback];
      } else {
        callbacks[eventType].push(callback);
      }
      return eventType + "_" + callbacks[eventType].length;
    };

    // invoke callback functions registered for event type
    //
    // @eventType:String - the event type to publish
    this.publish = function(eventType /*, arg1, arg2, ... */) {
      var cbs = callbacks[eventType] || [],
          args = Array.prototype.slice.call(arguments, 1);
      $.each(cbs, function() {
        this.apply(context, args);
      });
    };

  };

  ns.event = new EventDispatcher();
});
