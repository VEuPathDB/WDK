wdk.namespace('wdk.views.strategy', function(ns) {
  'use strict';

  var preventEvent = wdk.fn.preventEvent;

  // fn decorator to handle event with fn,
  // then to hide the detail box.
  var handleThenHide = function(fn) {
    return preventEvent(function(e) {
      var disabled = $(e.currentTarget).hasClass('disabled');

      if (!disabled) {
        if (_.isFunction(fn)) fn.apply(this, arguments);
        this.hide();
      }
    });
  };

  ns.StepDetailView = Backbone.View.extend({
    events: {
      'click.step .view_step_link'          : 'showResults',
      'click.step .rename_step_link'        : 'rename',
      'click.step .analyze_step_link'       : 'analyze',
      'click.step .edit_step_link'          : 'edit',
      'click.step .expand_step_link'        : 'expand',
      'click.step .collapse_step_link'      : 'collapse',
      'click.step .insert_step_link'        : 'insertStep',
      'click.step .delete_step_link'        : 'destroy',
      'click.step .close_link'              : 'hideDetails',
      'click.step .weight-button'           : 'setWeight',
      'submit.step form[name=questionForm]' : 'updateOperation'
    },

    initialize: function(options) {
      var name = this.model.customName;
      var filteredName = "";

      this.controller = options.controller;
      this.strategy = options.strategy;
      this.previousStep = options.previousStep;
      this.isBoolean = options.isBoolean;

      if (this.model.filtered) {
        filteredName = "<span class='medium'><b>Applied Filter:&nbsp;</b>" +
          this.model.filterName + "</span><hr>";
      }

      if (this.model.isCollapsed) {
        name = this.model.strategy.name;
      } else if (this.model.isboolean) {
        if (this.model.step.isCollapsed) {
          name = this.model.step.strategy.name;
        } else {
          name = this.model.step.customName;
        }
      }

      var collapsedName = encodeURIComponent(name);//"Nested " + name;

      if (this.model.isboolean && !this.model.isCollapsed) {
        name = "<ul class='question_name'><li>STEP " + this.model.frontId +
          " : Step " + (this.model.frontId - 1) + "</li><li class='operation " +
          this.model.operation + "'></li><li>" + name + "</li></ul>";
      } else {
        name = "<p class='question_name'><span>STEP " + this.model.frontId +
          " : " + name + "</span></p>";
      }

      this.name = name;
      this.collapsedName = collapsedName;

      this.render();
    },

    render: function() {
      wdk.util.setDraggable(this.$el, ".crumb_menu");

      $('#strategy_results').find('[id="' + this.el.id + '"]').remove();
      this.$el.appendTo('#strategy_results');
      this.$el.css({
        top: 10,
        left: ($(window).width() - this.$el.width()) / 2,
        position: "absolute"
      });

      wdk.util.initShowHide(this.$el);

      // not sure what this is for...
      var op = $(".question_name .operation", this.$el);
      if (op.length > 0) {
        var opstring = op.removeClass("operation").attr('class');
        op.addClass("operation");
        $("input[value='" + opstring + "']", this.$el).attr('checked','checked');
      }

      if (this.$el.hasClass('crumb_name')) {
        this.$el.children("img").attr("src",wdk.assetsUrl("wdk/images/minus.gif"));
      }
    },

    // display the detail box
    show: function() {
      $("body > div.crumb_details").hide();
      this.$el.show();
    },

    // hide the detail box
    hide: function() {
      this.$el.hide();
    },

    showResults: handleThenHide(function() {
      if (this.model.isValid) {
        this.controller.newResults(this.strategy.frontId, this.model.frontId,
                              this.isBoolean);
      }
    }),

    rename: handleThenHide(function(e) {
      wdk.step.Rename_Step(e.currentTarget, this.strategy.frontId,
                           this.model.frontId);
    }),

    analyze: handleThenHide(function() {
      var $button = $('#add-analysis button');

      $button.trigger('click');

      // scroll to section
      $(window).scrollTop($button.offset().top - 10);
    }),

    // aka, revise
    edit: handleThenHide(function(e) {
      var step = this.model.frontId === 1 || this.isBoolean || this.model.istransform
        ? this.model
        : this.model.step;

      wdk.step.Edit_Step(e.currentTarget, step.questionName,
                         step.urlParams,
                         step.isBoolean,
                         step.isTransform || step.frontId === 1,
                         step.assignedWeight);
    }),

    expand: handleThenHide(function(e) {
      this.controller.ExpandStep(e.currentTarget, this.strategy.frontId,
                            this.model.frontId, this.collapsedName);
    }),

    collapse: handleThenHide(function(e) {
      if (this.model.isUncollapsible) return;
      this.controller.ExpandStep(e.currentTarget, this.strategy.frontId,
                            this.model.frontId, this.collapsedName, true);
    }),

    insertStep: handleThenHide(function(e) {
      var recordName = this.previousStep
        ? this.previousStep.dataType
        : this.model.dataType;

      wdk.step.Insert_Step(e.currentTarget, recordName);
    }),

    destroy: handleThenHide(function() {
      if (this.model.frontId === 1 && this.strategy.nonTransformLength === 1) {
        this.controller.deleteStrategy(this.strategy.backId, false);
      } else {
        this.controller.DeleteStep(this.strategy.frontId, this.model.frontId);
      }
    }),

    hideDetails: handleThenHide(),

    setWeight: handleThenHide(function(e) {
      this.controller.SetWeight(e.currentTarget, this.strategy.frontId,
                           this.model.frontId);
    }),

    updateOperation: preventEvent(function(e) {
      var url = 'wizard.do?action=revise&step=' + this.model.id + '&';

      wdk.addStepPopup.callWizard(url, e.currentTarget, null, null, 'submit',
                                  this.strategy.frontId);
    })
  });

});
