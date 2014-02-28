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
      'click a': 'selectField'
    },

    template: wdk.templates['filter/field_list.handlebars'],

    render: function() {
      this.$el.html(this.template(this.model.fields.toJSON()));
      return this;
    },

    selectField: function(e) {
      e.preventDefault();
      var link = e.currentTarget;

      if ($(link).parent().hasClass('active')) {
        return;
      }

      this.$('li').removeClass('active');
      $(link).parent().addClass('active');
      var term = link.hash.slice(1);
      var field = this.model.fields.findWhere({term: term});
      this.trigger('select', field);
    }

  });

});
