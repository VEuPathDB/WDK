wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var MemberView = wdk.views.View.extend({

    events: {
      'click': 'toggleSelected'
    },

    className: 'member',

    template: Handlebars.compile(
      '<div class="fill" style="width:{{distribution}}%">  </div>' +
      '<div class="fill filtered" style="width:{{filteredDistribution}}%">  </div>' +
      '<div class="value">{{value}}</div>' +
      '<div class="count">{{count}} <span class="percent">({{percent}}%)</span></div>'
    ),

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

  var MemberView2 = wdk.views.View.extend({

    events: {
      'click': 'toggleSelected'
    },

    className: 'member',

    tagName: 'tr',

    template: Handlebars.compile(
      '<td><input type="checkbox" name="value" value="{{value}}" {{#if selected}} checked {{/if}} /></td>' +
      '<td><span class="value">{{value}}</span></td>' +
      '<td><span class="frequency">{{count}}</span></td>' +
      '<td><span class="percent">{{percent}}%</span></td>' +
      '<td><div class="bar">' +
      '  <div class="fill" style="width:{{distribution}}%">  </div>' +
      '  <div class="fill filtered" style="width:{{filteredDistribution}}%">  </div>' +
      '</div></td>'
    ),

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

    template: Handlebars.compile(
      '<h3>Filter {{options.title}} by {{model.display}}</h3>' +
      '<p>Select one or more items below.</p>' +
      '<ul class="nav nav-tabs">' +
      '  <li class="active"><a href="#condensed" data-toggle="tab">Condensed</a></li>' +
      '  <li><a href="#large" data-toggle="tab">Large</a></li>' +
      '</ul>' +

      '<div class="tab-content" style="padding:1em">' +
      ' <div class="tab-pane fade in active" id="condensed"><form><table><tbody class="membership-filter2"></tbody></table></form></div>' +
      ' <div class="tab-pane fade membership-filter" id="large"><p>Select value for filtering by clicking below.</p> </div>' +
      '</div>'
    ),

    initialize: function(options) {
      this.options = options;
      this.listenTo(this.model, 'change', function(field, options) {
        if (!options.fromDetailView) {
          this.render();
        }
      });
    },

    render: function() {
      var view = this;
      var field = this.model;
      var filterValues = field.get('filterValues');

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
        model: this.model.attributes,
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
          selected: !!(filterValues && _.contains(filterValues.values, name))
        });

        var memberView2 = new MemberView2({ model: member }).render();
        var memberView = new MemberView({ model: member }).render();

        view.$('.membership-filter2').append(memberView2.$el);
        view.$('.membership-filter').append(memberView.$el);

        //view.listenTo(member, 'change:selected', view.select);
      });

      members.on('change:selected', function(member, selected) {
        var type = view.model.get('type');
        var values = members.where({selected: true}).map(function(member) {
          return type === 'number'
            ? Number(member.get('value'))
            : member.get('value');
        });
        if (values.length) {
          view.model.set('filterValues', { values: values }, { fromDetailView: true });
        } else {
          view.model.set('filterValues', null, { fromDetailView: true });
        }
      });

      return this;
    }

  });

});
