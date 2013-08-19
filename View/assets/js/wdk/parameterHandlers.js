//wdk.util.namespace("window.wdk.parameterHandlers", function(ns, $) {
define(["jquery", "exports", "module"], function($, ns, module) {
  "use strict";

  var dependedParams = {};
  var dependedValues;
  var displayTermMap;
  var oldValues;
  var termDisplayMap;

  //==============================================================================
  function init() {
    // TODO - make these flags attached to this namespace
    var isPopup = window.isPopup;
    var isEdit = window.isEdit;
    if (isEdit == undefined) isEdit = false;
    if (isPopup == undefined) isPopup = false;

    // unset the flags
    if (window.isPopup != undefined) {
      window.isPopup = undefined;
      // delete window.isPopup;
    }
    if (window.isEdit != undefined) {
      window.isEdit = undefined;
      // delete window.isEdit;
    }

    displayTermMap = [];
    termDisplayMap = [];
    oldValues = [];

    if (isEdit == undefined) isEdit = false;
    initTypeAhead(isEdit);
    initDependentParamHandlers(isEdit);
    if (!isPopup) {
      $("#form_question").submit(function() {
        mapTypeAheads();
        return true;
      });
    }

    // need to trigger the click event so that the stage is set correctly on revise.
    $("#form_question #operations input[type='radio']:checked").click();
  }

  //==============================================================================
  function initDependentParamHandlers(isEdit) {

    $('div.dependentParam').each(function() {
      $('input, select', this).attr('disabled', true);
      var name = $(this).attr('name');
      // the dependson may contain a comma separated list of param names the current param depends on
      var dependedNames = $(this).attr('dependson').split(",");
      for (var i=0; i < dependedNames.length; i++) {
          var dependedName = dependedNames[i];
          var dependentList = dependedParams[dependedName] ? dependedParams[dependedName] : [];
          dependentList.push(name);
          dependedParams[dependedName] = dependentList;
      }

      $('input, select', this).attr('disabled',false);
    });

      // register change event to dependedParam only once
      for (var dependedName in dependedParams) {
          var dependedParam = $("div.param[name='" + dependedName + "']");
          var dependentDeferreds = [];
          dependedParam.change(function(e) {
            e.stopPropagation();
              var dependedName = $(this).attr("name");
              // set ready flag to false on all its dependent params
              var dependentList = dependedParams[dependedName];
              for (var i = 0; i < dependentList.length; i++) {
                  $(".dependentParam[name='" + dependentList[i] + "']").find("input, select").prop("disabled", true);
              }
              // fire update event
              var changed = false;
              for (var i = 0; i <  dependentList.length; i++) {
                  var dependentName = dependentList[i];
                  var result =  updateDependentParam(dependentName);
                  if (result) {
                    // result.then(function() {
                    //   wdk.event.publish("questionchange");
                    //   dependedParam.parents("form").change();
                    // });

                    // stash promises returned by $.ajax
                    dependentDeferreds.push(result);
                  }
              }
              // trigger form.change only when all deferreds are resolved
              $.when.apply($, dependentDeferreds).then(function() {
                wdk.event.publish("questionchange");
                dependedParam.closest("form").change();
              });
          });

          if (dependedParam.has('input.typeAhead').length > 0) {
              dependedParam.change();
          }
      }

    //If revising, store all of the old param values before triggering the depended 
    //   param's change function.
    if (isEdit) {
      for (var name in dependedParams) {
        var input = $("input.typeAhead[name='value(" + name + ")']");
        if (input.length == 0) {
          input = $("div.dependentParam[name='" + name + "']").find("select");
          if (input.length > 0) {
            // If this is a select, there's only one value
            oldValues[name] = input.val();
          } else {
            // Otherwise, we have to know which option(s) are checked
            var allVals = [];
            $("div.dependentParam[name='" + name + "']").find("input:checked")
                .each(function() {
                  allVals.push($(this).val());
                });
            oldValues[name] = allVals;
          }
        }
      }
    }
  }

  //==============================================================================
  function initTypeAhead(isEdit) {

    $("input:hidden.typeAhead").each(function() {
      var questionName = $(this).closest("form").find("input:hidden[name=questionFullName]").val();
      var paramName = getParamName($(this).attr('name'));
      $("#" + paramName + "_display").attr('disabled',true);
      if (isEdit) {
        oldValues[paramName] = $(this).val();
      }
      if ($(this).parent('div').hasClass('dependentParam')) {
        updateDependentParam(paramName);
      } else {
        $("#" + paramName + "_display").val('Loading options...');
        var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&xml=true';
        $.ajax({
          url: sendReqUrl,
          dataType: "xml",
          success: function(data){
            createAutoComplete(data, paramName);
          }
        });
      }
    });
  }

  //==============================================================================
  function createAutoComplete(obj, name) {
    $("div.ac_results").remove(); // Remove any pre-existing type-ahead results.
    var def = [];
    displayTermMap[name] = [];
    termDisplayMap[name] = [];
    var term;
    var display;
    var value = '';
    if( $("term",obj).length != 0 ) {
      $("term",obj).each(function() {
        term = this.getAttribute('id');
        display = this.firstChild.data;
        def.push(display);
        displayTermMap[name][display] = term;
        termDisplayMap[name][term] = display;
      });
    }

    var odd = true;
    var noMatch = "<i>No item found</i>";
    var wildCard = "<i>Find matches using a wildcard search</i>";
    var wildCardTest = /\*/;
    $("#" + name + "_display").autocomplete({
      source: function( request, response ) {
        var result = $.ui.autocomplete.filter(def, request.term);
        if (result.length == 0) {
          result.push(wildCardTest.test(request.term) ? wildCard : noMatch);
        } else {
          var matcher = new RegExp("("+$.ui.autocomplete.escapeRegex(request.term)+")", "ig" );
          result = $.map(result, function(item){
            var display = item.replace(matcher, "<strong>$1</strong>");
            return { label: display,    value: item};
          });
        }
        odd = true;
        response(result);
      },
      minLength: 3,
      focus: function(event, ui) {
        if(ui.item.value === noMatch || ui.item.value === wildCard) return false;
      },
      select: function(event, ui){
        if(ui.item.value === noMatch || ui.item.value === wildCard) return false;
      }
    }).data( "autocomplete" )._renderItem = function( ul, item ) {
      // only change here was to replace .text() with .html()
      // and indenting 
      var content = $( "<li></li>" )
          .data( "item.autocomplete", item )
          .append("<a>" + item.label + "</a>")
          .appendTo( ul );
      if (!odd) content.addClass("even");
      odd = !odd;
      return content;
    };

    if (oldValues[name]) {
      value = termDisplayMap[name][oldValues[name]]; // Look up the display for the old value
      if (!value) value = oldValues[name]; // For typeaheads allowing arbitrary input
      oldValues[name] = null;
    }
    $("#" + name + "_display").val(value).removeAttr('disabled');
  }

  //==============================================================================
  function updateDependentParam(paramName) {
    // get the current param
    var dependentParam = $("div.dependentParam[name='" + paramName + "']");
    var dependedNames = dependentParam.attr('dependson').split(",");

    // check if all the depended params are ready
    for (var i=0; i < dependedNames.length; i++) {
        var dependedName = dependedNames[i];
        var notReady = $(".param[name='" + dependedName + "']").find("input, select").prop("disabled");
        if (notReady) return;
    }
  
    var dependedValues = {};
    var hasValue = false;
    // the dependson may contain a comma separated list of param names the current param depends on
    for (var i=0; i < dependedNames.length; i++) {
      var dependedName = dependedNames[i];
      var dependedParam = $("#" + dependedName + 
          "aaa input[name='array(" + dependedName + ")']:checked, #" + 
          dependedName + "aaa select[name='array(" + dependedName + 
          ")']");
          
      // get the selected values from depended param
      var values = [];
      var needInput = false;
      dependedParam.each(function() {
          var value = $(this).val();
          if (value == 'Choose one:') needInput = true;
          else values.push(value);
      });
      if (needInput) {
        alert("Please choose a value.");
        dependedParam.focus();
        return;
      }
        
      $.unique(values);
      if (values.length > 0) {
        dependedValues[dependedName] = values;
        hasValue = true;
      }
    }
    if (!hasValue) return;
    
    // get dependent param and question name, contruct url from them
      var dependentParamSelector = "#" + paramName + 
          "aaa > div.dependentParam[name='" + paramName + "']";
      var dependentParam = $(dependentParamSelector);
      var questionName = dependentParam.closest("form")
          .find("input:hidden[name=questionFullName]").val();
      var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + 
          '&name=' + paramName + '&dependedValue=' + JSON.stringify(dependedValues);

      if ($('input.typeAhead',dependentParam).length > 0) {
        sendReqUrl = sendReqUrl + '&xml=true';
        $("#" + paramName + "_display").attr('disabled',true).val('Loading options...');
        return $.ajax({
          url: sendReqUrl,
          dataType: "xml",
          success: function(data) {
            $('input',dependentParam).removeAttr('disabled');
            $(".param[name='" + paramName + "']").attr("ready", "");
            createAutoComplete(data, paramName);
          }
        });
      } else {
        return $.ajax({
          url: sendReqUrl,
          type: "POST",
          data: {},
          dataType: "html",
          success: function(data) {
            var newContent = $("div.param, div.param-multiPick",data);
            if (newContent.length > 0) {
              dependentParam.html(newContent.html());
            } else {
              // this case is specifically for checkbox trees
              //   calling .html() on response erases javascript, so insert directly
              dependentParam.html(data);
            }
            if (oldValues[paramName]) {
              var input = $("select",dependentParam);
              if (input.length > 0) {
                input.val(oldValues[paramName]);
              } else {
                var allVals = oldValues[paramName];
                $.each(allVals, function() {
                  $("input[value='" + this + "']", dependentParam).attr('checked',true);
                });
              }
              oldValues[name] = null;
            }
            $(".param[name='" + paramName + "']").attr("ready", "");
            dependentParam.change();
          },
          error: function (jqXHR, textStatus, errorThrown) {
            alert("Error retrieving dependent param: " + textStatus + "\n" + errorThrown);
          }
        });
      }
  }

  //==============================================================================
  function mapTypeAheads() {
    $("input:hidden.typeAhead").each(function() {
      var paramName = $(this).attr('name');
      paramName = getParamName(paramName);
      var newValue = displayTermMap[paramName][$("#" + paramName + "_display").val()];
      if (!newValue) newValue = $("#" + paramName + "_display").val();
      $(this).val(newValue);
    });
  }

  //==============================================================================
  function getParamName(inputName, multiValue) {
    var paramName;
    if (multiValue) {
      paramName = inputName.match( /array\(([^\)]+)\)/ );
    } else {
      paramName = inputName.match( /value\(([^\)]+)\)/ );
    }
    if (paramName) return paramName[1];
  }

  ns.init = init;
  ns.mapTypeAheads = mapTypeAheads;

});
