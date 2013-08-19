//wdk.util.namespace("wdk.reporter", function(ns, $) {
define(["jquery", "exports", "module"], function($, ns, module) {
  "use strict";

  var $form, $fields, $defaultFields;

  var init = function() {
    $form = $(this);
    $fields = $form.find("[name='selectedFields'][value!='default']");
    $defaultFields = $form.find("[name='selectedFields'][value='default']");

    // attach handlers
    $form.find("input[value='select all']").click(function(e) {
      selectFields(1);
    });
    $form.find("input[value='clear all']").click(function(e) {
      selectFields(0);
    });
    $form.find("input[value='select inverse']").click(function(e) {
      selectFields(-1);
    });

    $defaultFields.click(function(e) {
      defaultFields(this.checked);
    });
    $fields.click(function(e) {
      defaultFields(false);
    });
  };

  var defaultFields = function(/* Boolean */ use) {
    if (use) {
      // select default, unselect non-default
      $defaultFields.attr("checked", true);
      $fields.attr("checked", false);
    } else {
      // unselect default
      $defaultFields.attr("checked", false);
    }
  };

  var selectFields = function(state) {
    $fields.each(function(idx, cb) {
      cb.checked = state === -1 ? !cb.checked : Boolean(state)
    });
    $defaultFields.attr("checked", false);
  }

  ns.init = init;

});
