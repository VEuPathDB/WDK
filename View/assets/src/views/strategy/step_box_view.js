wdk.namespace('wdk.views.strategy', function(ns) {
  'use strict';

  var preventEvent = wdk.fn.preventEvent,
      killEvent = wdk.fn.killEvent;

  ns.StepBoxView = Backbone.View.extend({
    events: {
      'click.step .results_link'    : 'showResults',
      'click.step .edit-icon'       : 'showDetails',
      'click.strategy .invalidStep' : 'reviseInvalidStep'
    },

    initialize: function(options) {
      this.controller = options.controller;
      this.stepDetailView = options.stepDetailView;
      this.strategy = options.strategy;
      this.isBoolean = options.isBoolean;
    },

    showDetails: killEvent(function() {
      //wdk.step.showDetails(e.currentTarget);
      $('.crumb_details').hide();
      this.stepDetailView.show();
    }),

    showResults: preventEvent(function() {
      if (this.model.isValid) {
        this.controller.newResults(this.strategy.frontId, this.model.frontId,
                              this.isBoolean);
      }
    }),

    reviseInvalidStep: preventEvent(function() {
      //reviseInvalidSteps(e.currentTarget);
      this.showDetails();
      this.$el.closest('.diagram').find("#invalid-step-text").remove();
    })
  });
});
