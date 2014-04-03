wdk.util.namespace("window.wdk.parameterHandlers", function(ns, $) {
  "use strict";

  var displayTermMap;
  var oldValues;
  var termDisplayMap;

  //==============================================================================
  function init(element) {
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

    initTypeAhead(isEdit, element);
    initDependentParamHandlers(isEdit, element);
    initFilterParam(element);

    element.closest('form').submit(function() {
      mapTypeAheads(element);
      return true;
    });

    // need to trigger the click event so that the stage is set correctly on revise.
    element.find("#operations input[type='radio']:checked").click();
  }

  //==============================================================================
  function initDependentParamHandlers(isEdit, element) {
    var dependedParams = {};

    element.find('div.dependentParam').each(function(i, node) {
      var $node = $(node);
      $node.find('input, select').prop('disabled', true);
      var name = $node.attr('name');
      // the dependson may contain a comma separated list of param names the current param depends on
      var dependedNames = $node.attr('dependson').split(",");
      for (var i=0; i < dependedNames.length; i++) {
          var dependedName = dependedNames[i];
          var dependentList = dependedParams[dependedName] ? dependedParams[dependedName] : [];
          dependentList.push(name);
          dependedParams[dependedName] = dependentList;
      }

      $node.find('input, select').prop('disabled',false);
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
                element.find(".dependentParam[name='" + dependentList[i] + "']")
                  .find("input, select").prop("disabled", true);
              }
              // fire update event
              var changed = false;
              for (var i = 0; i <  dependentList.length; i++) {
                  var dependentName = dependentList[i];
                  var result =  updateDependentParam(dependentName, element);
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

          if (dependedParam.is('[data-type="type-ahead"]').length > 0) {
              dependedParam.change();
          }
      }

    //If revising, store all of the old param values before triggering the depended 
    //   param's change function. Only for non-typeahead params
    if (isEdit) {
      for (var name in dependedParams) {
        var vals = [];
        element.find('.param[name="' + name + '"]')
          .not('[data-type="type-ahead"]')
           .find('[name$="(' + name + ')"]').each(function(i, control) {
              vals.push(control.value);
          });
          oldValues[name] = vals;
      }
    }
  }

  //==============================================================================
  function initTypeAhead(isEdit, element) {

    element.find('[data-type="type-ahead"]').each(function(i, node) {
      var $node = $(node);
      var $input = $node.find('input');
      var questionName = element.closest('form').find('input[name="questionFullName"]').val();
      var paramName = $node.attr('name');
      if (isEdit) {
        oldValues[paramName] = $input.val();
      }
      if ($node.hasClass('dependentParam')) {
        updateDependentParam(paramName, element);
      } else {
        $node.find("#" + paramName + "_display").html('Loading options...');
        var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&xml=true';
        $.ajax({
          url: sendReqUrl,
          dataType: "xml",
          success: function(data){
            // createAutoComplete(data, paramName, element);
            createFilteredSelect(data, paramName, element);
          }
        });
      }
    });
  }

  //==============================================================================
  function initFilterParam(element) {
    var form = element.closest('form');
    var filterParams = element.find('.filter-param');

    if (filterParams.length > 0) {
      // add class to move prompts to left
      element.addClass('move-left');
    }

    filterParams.each(function(i, node) {
      var $node = $(node);
      var dataId = $node.data('data-id');
      var name = $node.data('name');
      var input = $node.find('input');

      // get previous values
      try {
        var previousValue = JSON.parse(input.val());
        if (!( _.isArray(previousValue.filters) &&
               _.isArray(previousValue.values)  &&
               _.isArray(previousValue.ignored) )) {
          throw new Error('Previous value is malformed.');
        }
      } catch (e) {
        console.warn(e);
      }

      // parse data from <script>
      var jsonContainer = $(node).find('script[type="application/json"][id="' + dataId + '"]');
      var spec = JSON.parse(jsonContainer.html());
      spec = parseFilterData(spec);
      _.extend(spec, { title: name });

      if (previousValue) {
        _.extend(spec, { filters: previousValue.filters });
      }

      // instantiate the filter service
      var filterService = new wdk.models.filter.LocalFilterService(spec, {
        parse: true,
        root: 'metadata'
      });

      // set ignore: true for filteredData not in previousValues.values
      if (previousValue) {
        previousValue.ignored.forEach(function(id) {
          filterService.filteredData.get(id).set('ignored', true);
        });
      }

      // listen for change to filteredData and update input value
      filterService.filteredData.on('reset change', function() {
        var values = filterService.filteredData.where({ ignored: false })
          .map(function(d) { return d.get('term') });
        var ignored = filterService.filteredData.where({ ignored: true })
          .map(function(d) { return d.get('term') });
        var value = {
          values: values,
          ignored: ignored,
          filters: filterService.filters
        };
        input.val(JSON.stringify(value));
      });

      // create views
      var itemsView = new wdk.views.filter.FilterItemsView(filterService, { model: filterService.filters });
      var view = new wdk.views.filter.FilterView({ model: filterService });

      // attach views
      $(node)
        .append(itemsView.el)
        .append(view.el);

      itemsView.render();
      view.render(); //.collapse(true);

      form.on('submit', function(e) {
        if (filterService.filteredData.length === 0) {
          e.preventDefault();
          e.stopPropagation();
          $(node).find('.ui-state-error').remove();
          $(node).prepend(
            '<div class="ui-state-error ui-corner-all" style="padding: .3em .4em;">' +
            'Please select ' + name + ' to continue.' +
            '</div>'
          );
          filterService.filteredData.once('reset', function() {
            $(node).find('.ui-state-error').remove();
          });
        }
      });

    });
  }

  function parseFilterData(filterData) {
    var metadata = filterData.metadata;
    var metadataSpec = filterData.metadataSpec;
    var values = filterData.values;
    var numericProps = _.keys(metadataSpec)
      .filter(function(prop) {
        return metadataSpec[prop].type === 'number';
      });

    var data = {
      fields: _.values(metadata).map(_.keys)
        // get the unique list of all metadata props
        // for "One metadataSpec to Rule Them All"
        .reduce(function (a, b) { return _.union(a, b) })
        .map(function(name) {
          return _.extend({
            term: name,
            display: name
          }, metadataSpec[name]);
        }),

      data: values
        .map(function(d) {
          // type coercion
          var mdata = metadata[d.term];
          numericProps.forEach(function(prop) {
            mdata[prop] = Number(mdata[prop]);
          });
          return _.extend(d, {
            metadata: mdata
          });
        })
    };

    return data;
  }

  //==============================================================================
  function createFilteredSelect(xmlDOM, paramName, element) {
    // xmlDOM is an XML DOM object - it needs to be convered into a select list
    var values = [],
        displayDiv = element.find('#' + paramName + '_display').html(''), // may want to cache
        removeAllDiv = $('<div class="remove-all"><a href="#">Remove all</a></div>'),
        multiDelimRegExp = /\s*[,;\n\s]\s*/,
        isMultiple = displayDiv.data('multiple'),
        maxSelected = displayDiv.data('max-selected'),
        select = $('<select/>').prop('multiple', isMultiple),
        // Object[] => [{ jqElement, event, handler }, ...]
        chosenEvents = [],
        chosenOpts = {
          disable_search_threshold: 10,

          placeholder_text_multiple: 'Select some items',

          // allow eg 'kinase binding' as term
          enable_split_word_search: false,

          // TODO - allow paramRef override
          max_selected_options: maxSelected,

          // search any part of term
          search_contains: true,

          width: '35em'
        };

    if (oldValues[paramName]) {
      values = oldValues[paramName].split(/\s*,\s*/);
    }

    maxSelected = $.isNumeric(maxSelected) ? maxSelected : 1000;


    $(xmlDOM).find('term').each(function(idx, term) {
      $('<option/>')
        .val($(term).attr('id'))
        .text($(term).text())
        .prop('selected', values.indexOf($(term).attr('id')) > -1)
        .appendTo(select);
    });

    select
      .appendTo(displayDiv)

      .on('chosen:ready', function(event, chosenObj) {
        var input = chosenObj.chosen.container.find('input');

        if (isMultiple) {
          // allow for pasted list of IDs
          input[0].onpaste = function() {
            // event fires before input value is updated, so we need to
            // push the function call down the stack
            setTimeout(parsePastedInput.bind(this), 0);
          };
        }

        // if first term contains asterisk, 'turn off' plugin and use raw value
        // due to complications with SQL, wildcard support has been dropped
        //
        // input
        //   .one('keyup', function() {
        //     cacheChosenEvents(chosenObj);
        //   })

        //   .on('keyup', function(e) {
        //     if (isMultiple && select.find(':selected').length > 0) return;

        //     if (this.value.indexOf('*') > -1) {
        //       turnOffChosen(chosenObj);
        //       if (isMultiple) {
        //         chosenObj.chosen.search_field.width('35em');
        //       }
        //     } else {
        //       turnOnChosen(chosenObj);
        //     }
        //   });
      })

      // configure chosen
      .chosen(chosenOpts);

    if (isMultiple) {
      // only show Clear all if there are selected items
      select.on('change', function() {
        removeAllDiv.toggle(select.find(':selected').length > 0);
      })

      // attach behavior to Clear all link
      removeAllDiv.hide().appendTo(displayDiv)
        .on('click', 'a', function(e) {
          e.preventDefault();
          select.find(':selected').prop('selected', false);
          select.trigger('chosen:updated');
          $(e.delegateTarget).hide();
        });
    }

    // 1. split values
    // 2. select options
    // 3. refresh chosen
    function parsePastedInput() {
      var value = this.value,
          unfound = [],
          values;
      if (!multiDelimRegExp.test(value) /* || !chosen.multi */) {
        return;
      }
      values = value.split(multiDelimRegExp);

      // find values in select list, set selected to true, and pop from values
      values.forEach(function(value) {
        if (value == '') return;

        if (select.find('option[value="' + value + '"]')
          .prop('selected', true).length !== 1) {
          unfound.push(value);
        }
      });
      select.trigger('chosen:updated');

      $(this).val(unfound.join(', ') || null).focus()
    }

    function cacheChosenEvents(chosenObj) {
      chosenEvents.push({ jqElement: chosenObj.chosen.container, events: {} });
      chosenEvents.push({ jqElement: chosenObj.chosen.search_field, events: {} });
      chosenEvents.push({ jqElement: $(document), events: {} });

      chosenEvents.forEach(function(eventsObj) {
        var events = $._data(eventsObj.jqElement[0], 'events');
        for (var type in events) {
          events[type].forEach(function(o) {
            if (o.namespace === 'chosen') {
              eventsObj.events[type] = eventsObj.events[type] || [];
              eventsObj.events[type].push(o);
            }
          });
        }
      });
    }

    function turnOffChosen(chosenObj) {
      if (select.data('chosen-off')) return;

      chosenEvents.forEach(function(eventsObj) {
        for (var type in eventsObj.events) {
          eventsObj.jqElement.unbind(type + '.chosen');
        }
      });

      //chosenObj.chosen.dropdown.hide();
      chosenObj.chosen.search_results.hide();

      select.data('chosen-off', true);
    }

    function turnOnChosen(chosenObj) {
      if (!select.data('chosen-off')) return;

      chosenEvents.forEach(function(eventsObj) {
        for (var type in eventsObj.events) {
          eventsObj.events[type].forEach(function(eventObj) {
            eventsObj.jqElement.bind(type + '.chosen', eventObj.handler);
          });
        }
      });

      //chosenObj.chosen.dropdown.show();
      chosenObj.chosen.search_results.show();

      select.data('chosen-off', false);
    }

    function parsePastedInputjQuery(input, data) {
      var value = input.val(),
          unfound = [],
          multiDelimRegExp = /\s*\n\s*/,
          values;

      if (!multiDelimRegExp.test(value) /* || !chosen.multi */) {
        return;
      }

      values = value.split(multiDelimRegExp);

      // find values in select list, set selected to true, and pop from values
      for (var i = 0; i < values.length; i++) {
        if (values[i] == '') continue;

        if (data.indexOf(value[i]) === -1) {
          unfound.push(values[i]);
        }
      }

      return unfound;
    }

  }

  //==============================================================================
  function createAutoComplete(obj, name, element) {
    element.find("div.ac_results").remove(); // Remove any pre-existing type-ahead results.
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
    element.find("#" + name + "_display").autocomplete({
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
    }).data( "ui-autocomplete" )._renderItem = function( ul, item ) {
      // only change here was to replace .text() with .html()
      // and indenting 
      var content = $( "<li></li>" )
          .data( "ui-autocomplete-item", item )
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
    element.find("#" + name + "_display").val(value).removeAttr('disabled');
  }

  //==============================================================================
  function updateDependentParam(paramName, element) {
    // get the current param
    var dependentParam = element.find("div.dependentParam[name='" + paramName + "']");
    var dependedNames = dependentParam.attr('dependson').split(",");

    // check if all the depended params are ready
    for (var i=0; i < dependedNames.length; i++) {
        var dependedName = dependedNames[i];
        var notReady = element.find(".param[name='" + dependedName + "']")
            .find("input, select").prop("disabled");
        if (notReady) return;
    }
  
    var dependedValues = {};
    var hasValue = false;
    // the dependson may contain a comma separated list of param names the current param depends on
    for (var i=0; i < dependedNames.length; i++) {
      var dependedName = dependedNames[i];
      var dependedParam = element.find("#" + dependedName + 
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
      var dependentParam = element.find(dependentParamSelector);
      var questionName = dependentParam.closest("form")
          .find("input:hidden[name=questionFullName]").val();
      var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + 
          '&name=' + paramName + '&dependedValue=' + JSON.stringify(dependedValues);

      if (dependentParam.is('[data-type="type-ahead"]')) {
        sendReqUrl = sendReqUrl + '&xml=true';
        element.find("#" + paramName + "_display").html('Loading options...');
        return $.ajax({
          url: sendReqUrl,
          dataType: "xml",
          success: function(data) {
            dependentParam.find('input').removeAttr('disabled');
            element.find(".param[name='" + paramName + "']").attr("ready", "");
            // createAutoComplete(data, paramName);
            createFilteredSelect(data, paramName, element);
          }
        });
      } else {
        return $.ajax({
          url: sendReqUrl,
          type: "POST",
          data: {},
          dataType: "html",
          success: function(data) {
            var newContent = $(".param",data);
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
            element.find(".param[name='" + paramName + "']").attr("ready", "");
            dependentParam.change();
          },
          error: function (jqXHR, textStatus, errorThrown) {
            alert("Error retrieving dependent param: " + textStatus + "\n" + errorThrown);
          }
        });
      }
  }

  //==============================================================================
  function mapTypeAheads(element) {
    element.find('.param[data-type="type-ahead"]').each(function(i, param) {
      var $param = $(param);
      var paramName = $param.attr('name');
      var $select = $param.find('#' + paramName + '_display').find('select');
      var values;

      if ($select.data('chosen-off')) {
        // get values from input
        values = $select.next().find('input').val();
      } else {
        values = $select.val();
        values = _.isArray(values) ? values.join(', ') : values;
      }

      $param.find('input[name="value(' + paramName + ')"]').val(values);
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

  function adjustEnumCountSelect(enumParamId) {
    adjustEnumCountBox(enumParamId, 'option:selected');
  }

  function adjustEnumCountBoxes(enumParamId) {
    adjustEnumCountBox(enumParamId, 'input[type=checkbox]:checked');
  }
  
  function adjustEnumCountBox(enumParamId, onSelector) {
    var count = 0;
    $('#'+enumParamId).find(onSelector).each(function () {
      count++;
    });
    $('#'+enumParamId).find('span.currentlySelectedCount').html(count);
  }
  
  function adjustEnumCountTree(enumParamId, countOnlyLeaves) {
    var treeElement = $('#'+enumParamId).find('.checkbox-tree')[0];
    var itemSelector = (countOnlyLeaves ? "li.jstree-leaf.jstree-checked" : "li.jstree-checked");
    var count = $(treeElement).find(itemSelector).length;
    $('#'+enumParamId).find('span.currentlySelectedCount').html(count);
  }
  
  ns.init = init;
  ns.mapTypeAheads = mapTypeAheads;
  ns.adjustEnumCountSelect = adjustEnumCountSelect;
  ns.adjustEnumCountBoxes = adjustEnumCountBoxes;
  ns.adjustEnumCountTree = adjustEnumCountTree;

});
