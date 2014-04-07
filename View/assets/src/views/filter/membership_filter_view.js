wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var MemberView = wdk.views.View.extend({

    events: {
      'click': 'toggleSelected'
    },

    className: 'member',

    tagName: 'tr',

    template: wdk.templates['filter/member.handlebars'],

    initialize: function(options) {
      this.options = options;
      this.listenTo(this.model, 'change', this.render);
    },

    render: function() {
      this.$el.html(this.template(this.model.attributes));
      this.$el.tooltip({ title: this.model.get('value'), placement: 'left', delay: 400 });
      this.$el.toggleClass('selected', this.model.get('selected'));
      return this;
    },

    toggleSelected: function() {
      this.model.set('selected', !this.model.get('selected'));
    }

  });

  var MembershipFilterView = ns.MembershipFilterView = wdk.views.View.extend({

    template: wdk.templates['filter/membership_filter.handlebars'],

    constructor: function(filterService) {
      var initArgs = [].slice.call(arguments, 1);
      this.filterService = filterService;
      wdk.views.View.apply(this, initArgs);
    },

    initialize: function(options) {
      this.options = options;
    },

    render: function() {
      var _this = this;
      var field = this.model;
      var filterService = this.filterService;
      var filter = filterService.filters.findWhere({
        field: field.get('term')
      });
      var filterValues = filter ? filter.get('values') : [];

      // unfiltered dist
      var values = field.get('values');
      var counts = _.countBy(values);
      var names = field.get('type') === 'number'
        ? _.keys(counts).map(Number).sort(function(a, b) { return a - b; })
        : _.keys(counts).sort();

      var scale = _.max(counts) + 10;

      // filtered dist
      var fvalues = field.get('filteredValues');
      var fcounts = _.countBy(fvalues);

      this.$el.html(this.template({
        field: this.model.attributes,
        options: this.options
      }));

      var members = new Backbone.Collection();

      _(names).forEach(function(name) {
        var count = counts[name];
        var fcount = fcounts[name] || 0;
        var member = members.add({
          value: name,
          count: count,
          percent: (count / values.length * 100).toFixed(2),
          distribution: (count / scale * 100).toFixed(2),
          filteredDistribution: (fcount / scale * 100).toFixed(2),
          selected: !!(_.contains(filterValues, name))
        });
        var memberView = new MemberView({ model: member }).render();
        _this.$('.membership-filter').append(memberView.$el);
      });

      members.on('change:selected', function(member, selected) {
        var type = _this.model.get('type');
        var values = members.where({selected: true}).map(function(member) {
          return type === 'number'
            ? Number(member.get('value'))
            : member.get('value');
        });
        var filters = filterService.filters;
        filters.remove(filters.where({ field: field.get('term') }), { origin: this });
        if (values.length) {
          filters.add({
            field: field.get('term'),
            operation: field.get('filter'),
            values: values
          }, { origin: this });
        }
      });

      this.$('.tabs').tabs();

      return this;
    }

  });

});
