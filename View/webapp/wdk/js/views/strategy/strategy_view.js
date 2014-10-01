wdk.namespace('wdk.views.strategy', function(ns) {
  'use strict';

  var preventEvent = wdk.fn.preventEvent;

  // gets a strategy object as `model` attribtue
  // Note: this is *not* a Backbone.Model object
  ns.StrategyView = Backbone.View.extend({
    events: {
      'click.strategy .closeStrategy a'   : 'close',
      'click.strategy [href="#rename"]'   : 'rename',
      'click.strategy [href="#share"]'    : 'share',
      'click.strategy [href="#save"]'     : 'save',
      'click.strategy [href="#copy"]'     : 'copy',
      'click.strategy [href="#delete"]'   : 'destroy',
      'click.strategy [href="#describe"]' : 'showDescription'
    },

    initialize: function(options) {
      this.controller = options.controller;
    },

    close: preventEvent(function() {
      this.controller.closeStrategy(this.model.frontId);
    }),

    rename: preventEvent(function() {
      this.$('.strategy-name').editable('show');
    }),

    share: preventEvent(function(e) {
      var target = e.currentTarget,
          id = this.model.backId,
          url = wdk.exportBaseURL() + this.model.importId,
          isSaved = this.model.isSaved,
          isGuest = wdk.user.isGuest();

      if (isSaved) {
        wdk.history.showHistShare(target, id, url);
      } else if (isGuest) {
        wdk.user.login();
      } else {
        if (confirm('Before you can share your strategy, you need to save it.' +
                    ' Would you like to do that now?')) {
          wdk.history.showUpdateDialog(target, true, true);
        }
      }
    }),

    save: preventEvent(function(e) {
      if (wdk.user.isGuest()) {
        wdk.user.login();
      } else {
        wdk.history.showUpdateDialog(e.currentTarget, true, false);
      }
    }),

    copy: preventEvent(function() {
      this.controller.copyStrategy(this.model.backId);
    }),

    destroy: preventEvent(function() {
      this.controller.deleteStrategy(this.model.backId);
    }),

    showDescription: preventEvent(function(e) {
      var target = e.currentTarget;

      if (this.model.description) {
        wdk.history.showDescriptionDialog(target, !this.model.isSaved, false, this.model.isSaved);
      } else {
        wdk.history.showUpdateDialog(target, !this.model.isSaved, false);
      }
    })
  });

});
