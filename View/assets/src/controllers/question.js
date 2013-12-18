// =============================================================================
// The js related to the display on question page
// $(document).ready(initializeQuestion);

wdk.util.namespace("window.wdk.question", function(ns, $) {
  "use strict";

  var onRegistry = {};

  function init(element, attrs) {
    var question = new WdkQuestion();

    question.registerGroups();

    if (attrs.showParams === true) {
      wdk.parameterHandlers.init(element);
    }
 
    wdk.onloadQuestion();

    wdk.event.publish("questionload");

    if ('undefined' !== typeof onRegistry[null]) {
      onRegistry[null].forEach(function(cb) {
        cb(element.closest('form'));
      });
    }

    if ('undefined' !== typeof onRegistry[attrs.questionFullName]) {
      onRegistry[attrs.questionFullName].forEach(function(cb) {
        cb(element.closest('form'), attrs.questionFullName);
      });
    }

  }

  function WdkQuestion() {

    this.registerGroups = function() {
      $(".param-group[type='ShowHide']").each(function() {
        // register the click event
        var name = $(this).attr("name");
        var expire = 365;   // in days
        $(this).find(".group-handle").click(function(event) {
          // only call once
          event.stopImmediatePropagation();

          var handle = this;
          var path = handle.src.substr(0, handle.src.lastIndexOf("/"));
          var detail = $(this).parents(".param-group").children(".group-detail");
          detail.toggle();
          if (detail.css("display") == "none") {
            handle.src = path + "/plus.gif";
            window.wdk.createCookie(name, "hide", expire);
          } else {
            handle.src = path + "/minus.gif";
            window.wdk.createCookie(name, "show", expire);
          }
        });

        // decide whether need to change the display or not
        var showFlag = window.wdk.readCookie(name);
        if (showFlag == null) return;

        var status = $(this).children(".group-detail").css("display");
        if ((showFlag == "show") && (status == "none")) {   
          // should show, but it is hidden
          $(this).find(".group-handle").trigger("click");
        } else if ((showFlag == "hide") && (status != "none")) {
          // should hide, bit it is shown
          $(this).find(".group-handle").trigger("click");
        }
      });
    };
  }

  /**
   * Register a callback function for specified questions.
   * If questions is not defined (i.e., a function), then
   * cb will be called for all questions.
   *
   * cb will get the following paramter:
   * - jQuery wrapped form element
   *
   * @param questions (optional) [string|Array]
   * @param cb [function]
   */
  function on(questions, cb) {
    if ('function' === typeof questions) {
      cb = questions;
      questions = [null];
    } else if ('string' === typeof questions) {
      questions = [questions];
    } else if (!(questions instanceof Array)) {
      throw new TypeError('Unsupported paramter type: ' + typeof questions);
    }

    questions.forEach(function(question) {
      if ('string' !== typeof question && question !== null) {
        throw new TypeError('Invalid question identifier: %s.', question);
      }

      if ('undefined' === typeof onRegistry[question]) {
        onRegistry[question] = [];
      }

      onRegistry[question].push(cb);

    });

  }

  ns.WdkQuestion = WdkQuestion;
  ns.init = init;
  ns.on = on;

});
