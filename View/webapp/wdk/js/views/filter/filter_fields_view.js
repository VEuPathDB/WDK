wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FieldListView = ns.FieldListView;
  var FieldDetailView = ns.FieldDetailView;

  /**
   * A FilterView is composed of several components:
   * - A selectable list of available filterable fields.
   * - A detail view for a specific field.
   *
   */
  ns.FilterFieldsView = wdk.views.core.View.extend({

    template: wdk.templates['filter/filter_fields.handlebars'],

    className: 'filters ui-helper-clearfix',

    initialize: function() {
      this.fieldList = new FieldListView({
        className: 'field-list',
        collection: this.controller.fields,
        controller: this.controller,
        fieldTemplate: function(field) {
          return '<a href="#' + field.term + '">' + field.display + '</a>';
        },
        events: {
          'click li a': function(event) {
            event.preventDefault();

            var link = event.currentTarget;
            if ($(link).parent().hasClass('active')) {
              return;
            }

            var term = link.hash.slice(1);
            var field = this.controller.fields.findWhere({term: term});
            this.controller.selectField(field);
          }.bind(this)
        }
      });
      this.fieldDetail = new FieldDetailView({
        className: 'field-detail',
        model: this.model,
        controller: this.controller
      });

      this.listenTo(this.controller, 'select:field', this.renderDetail);

      this.render();
    },

    render: function() {
      this.$el.append(this.fieldList.el, this.fieldDetail.el);
    },

    renderDetail: function(field) {
      this.fieldDetail.render(field);
    },

    didShow: function() {
      this.fieldDetail.show();
    },

    // FIXME This logic should be in the Field model
    setFilter: function(field, filterValues) {
      var filters = this.model.filters;

      // remove previous filters for this field
      filters.remove(filters.where({ field: field.get('term') }));

      if (filterValues) {
        var filter = _.extend({
          field: field.get('term'),
          operation: field.get('filter')
        }, filterValues);

        filters.add(filter);
      }
    }

  });

});
