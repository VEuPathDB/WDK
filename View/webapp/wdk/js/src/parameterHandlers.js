wdk.util.namespace("window.wdk.parameterHandlers", function(ns, $) {
  "use strict";

  var dependedParams;
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

    dependedParams = [];
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
      if (!dependedParams[name]) {
        dependedParams[name] = $(this).attr('dependson');
      }
      var dependedParam = $("#" + dependedParams[name] + 
          "aaa input[name='array(" + dependedParams[name] + ")'], #" + 
          dependedParams[name] + "aaa select[name='array(" + dependedParams[name] + 
          ")']");
      dependedParam.change(function(e) {
        // supress change event until depended params are loaded
        e.stopPropagation();
        dependedValues = [];
        var paramName = getParamName($(this).attr('name'), true);
        var inputs = $("#" + paramName + "aaa input[name='array(" + paramName + 
            ")']:checked, #" + paramName + "aaa select[name='array(" + paramName + 
            ")']");
        inputs.each(function() {
          dependedValues.push($(this).val());
        });
        $.unique(dependedValues);
        updateDependentParam(name, dependedValues.join(",")).then(function() {
          wdk.event.publish("questionchange");
          dependedParam.parents("form").change();
        });
      });
      if ($(this).has('input.typeAhead').length > 0) {
        dependedParam.change();
      }

      $('input, select', this).attr('disabled',false);
    });
    
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
      if (!$(this).parent('div').hasClass('dependentParam')) {
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
  function updateDependentParam(paramName, dependedValue) {
    if (dependedValue && dependedValue != 'Choose one:') {
      var dependentParamSelector = "#" + paramName + 
          "aaa > div.dependentParam[name='" + paramName + "']";
      var dependentParam = $(dependentParamSelector);
      var questionName = dependentParam.closest("form")
          .find("input:hidden[name=questionFullName]").val();
      var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + 
          '&name=' + paramName + '&dependedValue=' + dependedValue;

      if ($('input.typeAhead',dependentParam).length > 0) {
        sendReqUrl = sendReqUrl + '&xml=true';
        $("#" + paramName + "_display").attr('disabled',true).val('Loading options...');
        return $.ajax({
          url: sendReqUrl,
          dataType: "xml",
          success: function(data) {
            $('input',dependentParam).removeAttr('disabled');
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
          },
          error: function (jqXHR, textStatus, errorThrown) {
            alert("Error retrieving dependent param: " + textStatus + "\n" + errorThrown);
          }
        });
      }
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
