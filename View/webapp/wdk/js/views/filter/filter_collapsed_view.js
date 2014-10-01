wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.FilterCollapsedView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_collapsed.handlebars'],

    render: function() {
      this.$el.html(this.template({
        title: this.controller.title,
        filters: this.model.filters
      }));
      return this;
    },

    didShow: function() {
      this.render();
    }
  });

});
