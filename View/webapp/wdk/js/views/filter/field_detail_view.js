wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var RangeFilterView = ns.RangeFilterView;
  var MembershipFilterView = ns.MembershipFilterView;

  /**
   * This View delegates to other Views:
   *   - RangeFilterView
   *   - MembershipFilterView
   */
  ns.FieldDetailView = wdk.views.core.View.extend({

    // The active delegate view
    delegateView: null,

    emptyTemplate: wdk.templates['filter/field_detail_empty.handlebars'],

    initialize: function() {
      this.listenTo(this.controller.fields, 'change', this.changeField);
      this.listenTo(this.controller.fields, 'reset', this.renderEmpty);
      this.render();
    },

    render: function(field) {
      return typeof field === 'undefined'
        ? this.renderEmpty()
        : this.renderDetail(field);
    },

    renderEmpty: function() {
      this.$el.html(this.emptyTemplate({
        title: this.controller.title
      }));
      return this;
    },

    renderDetail: function(field) {
      var Delegate = this.getDelegateConstructor(field.get('filter'));

      if (this.delegateView) {
        this.delegateView.stopListening();
        this.delegateView.undelegateEvents();
        this.$el.empty();
      }

      this.delegateView = new Delegate(this.model, {
        el: this.el,
        model: field,
        controller: this.controller,
        title: this.controller.title
      }).render();

      return this;
    },

    changeField: function(field) {
      var delegateView = this.delegateView;

      if (delegateView && field === delegateView.model) {
        this.renderDetail(field);
      }
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
        throw new TypeError('Unknown filter type: ' + type);
      }

      return constructor;
    },

    didShow: function() {
      if (this.delegateView) {
        this.delegateView.show();
      }
    }

  });

});
