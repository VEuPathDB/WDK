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

    emptyTemplate: wdk.templates['filter/field_detail_empty.handlebars'],

    delegateTemplate: wdk.templates['filter/field_detail_delegate.handlebars'],

    initialize: function() {
      this.listenTo(this.model.filters, 'add remove', function(filter, filters, opts) {
        var delegateView = this.delegateView;
        if (!delegateView) {
          return;
        }
        if (filter.get('field') === delegateView.model.get('term') && opts.origin !== delegateView) {
          this.renderDetail(delegateView.model);
        }
      });

      this.listenTo(this.model.fields, 'change', function(field) {
        var delegateView = this.delegateView;
        if (!delegateView) {
          return;
        }
        if (field === delegateView.model) {
          this.renderDetail(field);
        }
      });
    },

    render: function(field) {
      return typeof field === 'undefined'
        ? this.renderEmpty()
        : this.renderDetail(field);
    },

    renderEmpty: function() {
      this.$el.html(this.emptyTemplate(this.model.attributes));
      return this;
    },

    renderDetail: function(field) {
      var Delegate = this.getDelegateConstructor(field.get('filter'));
      var html = this.delegateTemplate(this.model.attributes);

      if (this.delegateView) {
        this.delegateView.destroy();
        this.$el.empty();
      }

      this.delegateView = new Delegate(this.model, {
        el: this.el,
        model: field,
        title: this.model.get('title')
      }).render();

      this.$el.append(html);

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
