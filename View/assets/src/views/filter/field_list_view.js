wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  /**
   * Renders a list of links
   *
   * When a link is clicked, a 'select' event is triggered by the view.
   * Event handlers will recieve the field model as an argument.
   *
   * ```
   * // In some other view ...
   * var fieldList = new FieldListView({ model: this.model });
   * view.listenTo(fieldList, 'select', view.renderDetail);
   * ```
   */
  ns.FieldListView = wdk.views.View.extend({

    events: {
      'click a': 'triggerSelect'
    },

    template: wdk.templates['filter/field_list.handlebars'],

    initialize: function() {
      this.listenTo(this.model.fields, 'select', this.selectField);
    },

    render: function() {
      this.$el.html(this.template(this.model.fields.toJSON()));
      return this;
    },

    triggerSelect: function(e) {
      e.preventDefault();

      var link = e.currentTarget;
      if ($(link).parent().hasClass('active')) {
        return;
      }

      var term = link.hash.slice(1);
      var field = this.model.fields.findWhere({term: term});
      field.select();
    },

    selectField: function(field) {
      var term = field.get('term');
      var link = this.$('a[href="#' + term + '"]');
      this.$('li').removeClass('active');
      $(link).parent().addClass('active');
    }

  });

});
