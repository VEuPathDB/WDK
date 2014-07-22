wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.FilterCollapsedView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_collapsed.handlebars'],

    render: function() {
      this.$el.html(this.template(this.model));
      return this;
    },

    didShow: function() {
      this.render();
    }
  });

});
