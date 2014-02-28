wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var RangeFilterView = ns.RangeFilterView;
  var MembershipFilterView = ns.MembershipFilterView;

  /**
   * This View delegates to other Views:
   *   - RangeFilterView
   *   - MembershipFilterView
   */
  ns.FieldDetailView = wdk.views.View.extend({

    // The active delegate view
    delegateView: null,

    // Default options that can be overridden
    options: {
      emptyText: 'Get started by choosing a filter in the panel at left'
    },

    initialize: function(options) {
      _.extend(this.options, options);
    },

    render: function(field) {
      return typeof field === 'undefined'
        ? this.renderEmpty()
        : this.renderDetail(field);
    },

    renderEmpty: function() {
      this.$el.html('<h4 style="margin-top: 2em;">' + this.options.emptyText + '</h4>');
      return this;
    },

    renderDetail: function(field) {
      var Delegate = this.getDelegateConstructor(field.get('filter'));

      if (this.delegateView) {
        this.delegateView.undelegateEvents();
        this.delegateView.stopListening();
        this.$el.empty();
      }

      this.delegateView = new Delegate({
        el: this.el,
        model: field,
        title: this.model.get('title')
      }).render();

      return this;
    },

    getDelegateConstructor: function(type) {
      var constructor;

      if (type === 'range') {
        constructor = RangeFilterView;
      }

      if (type === 'membership') {
        constructor = MembershipFilterView;
      }

      if (typeof constructor === 'undefined') {
        throw new TypeError('Uknown filter type: ' + type);
      }

      return constructor;
    }

  });

});
