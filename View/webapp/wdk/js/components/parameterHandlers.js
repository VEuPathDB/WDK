wdk.util.namespace("window.wdk.parameterHandlers", function(ns, $) {
  "use strict";

  var displayTermMap;
  var termDisplayMap;

  //==============================================================================
  function init(element) {
    displayTermMap = [];
    termDisplayMap = [];

    initTypeAhead(element);
    initDependentParamHandlers(element);
    initFilterParam(element);

    // need to trigger the click event so that the stage is set correctly on revise.
    element.find("#operations input[type='radio']:checked").click();
  }

  //==============================================================================
  function initDependentParamHandlers(element) {
    // jshint loopfunc:true
    var dependedParams = {};

    element.find('div.dependentParam').each(function(index, node) {
      var $dependentParam = $(node);
      var name = $dependentParam.attr('name');
      $dependentParam.find('input, select').prop('disabled', true);
      // TODO Use a space-delimited list. This is more canonical for multiple values for an attribute
      // and will allow for more concise jQuery selectors: $('[dependson~="param-name"]')
      //
      // the dependson may contain a comma separated list of param names the current param depends on
      var dependedNames = $dependentParam.attr('dependson').split(",");
      for (var i=0; i < dependedNames.length; i++) {
        var dependedName = dependedNames[i];
        var dependentList = dependedParams[dependedName] ? dependedParams[dependedName] : [];
        dependentList.push(name);
        dependedParams[dependedName] = dependentList;
      }

      $dependentParam.find('input, select').prop('disabled',false);
    });

    // register change event to dependedParam only once
    for (var dependedName in dependedParams) {
      var dependedParam = $("div.param[name='" + dependedName + "']");
      dependedParam.change(function(e) {
        e.stopPropagation();
        var dependedName = $(this).attr("name");

        // map list of names to elements
        // then reduce to a list of $.ajax deferred objects
        var dependentDeferreds = dependedParams[dependedName]
          .map(function(dependentName) {

            // return dependentParam reference
            // and set ready flag to false on all its dependent params
            return element.find(".dependentParam[name='" + dependentName + "']")
              .find("input, select")
                .prop("disabled", true)
                .end();
          })
          .reduce(function(results, $dependentParam) {
            var result =  updateDependentParam($dependentParam, element);
            if (result) {
              // stash promises returned by $.ajax
              results.push(result);
            }
            return results;
          }, []);

        // trigger form.change only when all deferreds are resolved
        $.when.apply($, dependentDeferreds).then(function() {
          dependedParam.closest("form").change();
        });
      });

      if (dependedParam.is('[data-type="type-ahead"]').length > 0) {
        dependedParam.change();
      }
    }
  }

  //==============================================================================
  function initTypeAhead(element) {
    element.find('[data-type="type-ahead"]')
      .each(function(i, node) {
        var $param = $(node);
        var questionName = element.closest('form').find('input[name="questionFullName"]').val();
        var paramName = $param.attr('name');

        if ($param.hasClass('dependentParam')) {
          updateDependentParam($param, element);
        } else {
          var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&json=true';

          $.getJSON(sendReqUrl)
            .then(function(data) {
              // createAutoComplete(data, paramName, element);
              createFilteredSelect(data, paramName, $param);
            })
            .done(function() {
              $param.find('.loading').hide();
            });
        }
      });
  }

  //==============================================================================
  function initFilterParam(element) {
    var form = element.closest('form');
    var filterParams = element.find('[data-type="filter-param"]');

    if (filterParams.length > 0) {
      // add class to move prompts to left
      element.addClass('move-left');
    }

    filterParams.each(function(i, node) {
      var $param = $(node);
      var questionName = form.find('input[name="questionFullName"]').val();
      var paramName = $param.attr('name');
      var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&json=true';

      $.getJSON(sendReqUrl)
        .then(createFilterParam.bind(null, $param, questionName));
    });
  }

  //==============================================================================
  function createFilterParam($param, questionName, filterData) {
    var form = $param.closest('form');
    var title = $param.data('title');
    var name = $param.attr('name');
    console.time('intialize render :: ' + name);
    var defaultColumns = $param.data('default-columns');
    var trimMetadataTerms = $param.data('trim-metadata-terms');
    var input = $param.find('input');
    var previousValue;

    defaultColumns = defaultColumns ? defaultColumns.split(/\s+/) : [];

    // get previous values
    try {
      previousValue = JSON.parse(input.val());
      if (!( _.isArray(previousValue.filters) &&
             _.isArray(previousValue.values)  &&
             _.isArray(previousValue.ignored) )) {
        previousValue = undefined;
        throw new Error('Previous value is malformed.');
      }
    } catch (e) {
      console.warn(e);
    }

    // parse data from <script>
    // var jsonContainer = $(node).find('script[type="application/json"][id="' + dataId + '"]');
    // console.time('parse JSON :: ' + name);
    // var filterData = JSON.parse(jsonContainer.html());
    // console.timeEnd('parse JSON :: ' + name);

    // console.time('validation not null queries :: ' + name);
    // // validation
    // [ 'metadata', 'metadataSpec', 'values' ]
    //   .forEach(function(prop) {
    //     var msg;
    //     if (_.isEmpty(filterData[prop])) {
    //       msg = 'Invalid data: ' + prop + ' may not be empty.';
    //       alert(msg);
    //       throw new Error(msg);
    //     }
    //   });
    // console.timeEnd('validation not null queries :: ' + name);

    // console.time('massage data :: ' + name);
    // filterData = parseFilterData(filterData);
    // console.timeEnd('massage data :: ' + name);

    var fields = _.keys(filterData.metadataSpec)
      .map(function(name) {
        return _.extend({
          //filterable: _.contains(usedMetadata, name),
          term: name,
          display: name,
          visible: _.contains(defaultColumns, name)
        }, filterData.metadataSpec[name]);
      });

    var filterParam = new wdk.controllers.FilterParam({
      el: $param,
      data: filterData.values,
      metadata: filterData.metadata,
      fields: fields,
      filters: previousValue && previousValue.filters,
      ignored: previousValue && previousValue.ignored,
      trimMetadataTerms: trimMetadataTerms,
      defaultColumns: defaultColumns,
      title: title,
      name: name,
      questionName: questionName
    });

    filterParam.on('change:value', function(filterParam, value) {
      input.val(JSON.stringify(value));
    });

    filterParam.on('ready', function() {
      $param.find('.loading').hide();
    });

    form.on('submit', function(e) {
      var filteredData = filterParam.getSelectedData();
      if (filteredData.length === 0) {
        e.preventDefault();
        $param.find('.ui-state-error').remove();
        $param.prepend(
          '<div class="ui-state-error ui-corner-all" style="padding: .3em .4em;">' +
          'Please select ' + name + ' to continue.' +
          '</div>'
        );
        filterParam.once('change:value', function() {
          $param.find('.ui-state-error').remove();
        });
      }
    });

    console.timeEnd('intialize render :: ' + name);
  }

  function updateFilterParam(paramNode, data) {
    paramNode.data('filterService').reset(data);
  }

  function parseFilterData(filterData) {
    // var Field = wdk.models.filter.Field;
    var metadata = filterData.metadata;
    var metadataSpec = filterData.metadataSpec;
    var values = filterData.values;
    // var unknowns = [];

    // unique set of metadata properties found
    // in metadata object values
    // console.time('  v pick unique terms');
    // FIXME Make this more efficient.
    //
    // We need the keys for all 
    // var metadataTerms = _.values(metadata)
    //   .map(_.keys)
    //   .reduce(function (a, b) { return _.union(a, b); })
    //   .filter(function(name) {
    //     return !!metadataSpec[name];
    //   });
    // var metadataTerms = _.chain(metadata)
    //   .values()
    //   .map(Object.keys)
    //   .flatten()
    //   .uniq()
    //   .filter(function(name) { return metadataSpec[name]; })
    //   .value();
    // console.timeEnd('  v pick unique terms');

    // console.time('usedMetadata');
    // var usedMetadata = _.uniq([].concat.apply([], _.values(metadata).map(_.keys)));
    // console.timeEnd('usedMetadata');

    // TODO remove
    console.time('  v mark leaves');
    var fields = _.keys(metadataSpec)
      .map(function(name) {
        return _.extend({
          //filterable: _.contains(usedMetadata, name),
          term: name,
          display: name
        }, metadataSpec[name]);
      });
    console.timeEnd('  v mark leaves');

    // TODO remove
    console.time('  v map data to fields');
    var data = values
      .map(function(d) {
        var mdata = metadata[d.term],
            missingMsg = 'Missing metadata for "' + d.term + '".';

        if (mdata === undefined) {
          _.defer(alert, '/!\\ ERROR /!\\\n\n' + missingMsg);
          throw new Error(missingMsg);
        }

        // FIXME Defer counting unknowns and type coercion to
        // detail view render time
        // _.where(fields, { leaf: 'true' })
        //   .forEach(function(field) {
        //     var property = field.term,
        //         value = mdata[property];
        //     if (!value) {
        //       mdata[property] = Field.UNKNOWN_VALUE;
        //     } else if (metadataSpec[property].type === 'number') {
        //       // type coercion
        //       mdata[property] = Number(value);
        //     }
        //   });

        // if (_.every(mdata, function(m) { return m === Field.UNKNOWN_VALUE; })) {
        //   unknowns.push(d);
        // }

        return _.extend(d, {
          metadata: mdata
        });
      });
    console.timeEnd('  v map data to fields');

    // TODO remove
    // if (unknowns.length) {
    //   _.defer(alert, '/!\\ WARNING /!\\\n\nThe following items contian only UNKNOWN values: ' +
    //     _.pluck(unknowns, 'term').join(', '));
    // }

    return { fields: fields, data: data };
  }

  function createFilteredSelect(vocab, paramName, $param) {

    // FIXME Using a class to determin if we are revising is a hack.
    // Need to consider a cleaner solution.
    //
    // The class is set by the Wizard JSP
    var isNew = !$param.closest('form').is('.is-revise');

    var $input = $param.find('input[name="value(' + paramName + ')"]'),
        keepOpen = false,
        format = function(item) { return item.display; },
        displayCurrent = function(selectedObject, currentSearchTerm) {
          return currentSearchTerm;
        };

    if (isNew) {
      $input.val('');
    }

    $input.select2({
      placeholder: 'Begin typing to see suggestions...',
      minimumInputLength: 3,
      allowClear: true,
      multiple: $param.data('multiple'),
      id: 'term',
      createSearchChoice: function(term) {
        return _.findWhere(vocab.values, { term: term.trim() });
      },
      tokenSeparators: [ ',', ';', '\n' ],
      data: { results: vocab.values, text: 'display' },
      formatSelection: format,
      formatResult: format,
      nextSearchTerm: displayCurrent
    });

    if ($param.data('multiple')) {
      $param
        .on('keydown', function(e) {
          if (e.ctrlKey || e.metaKey) keepOpen = true;
        })
        .on('keyup', function(e) {
          if (!(e.ctrlKey || e.metaKey)) keepOpen = false;
        })
        .on('change', function() {
          if (keepOpen) $input.select2('open');
        });
    }

    // remove invalid values from select2 inputs
    $param.closest('form').on('submit', function() {
      var $select2Container = $param.find('select2-container');
      var values = $select2Container.next().val();

      if (values) {
        $select2Container('val', values.split(','));
      }
    });
  }

  // TODO Delete chosen-based function when we know select2-based is adequate.
  //==============================================================================
  // function createFilteredSelect_old(xmlDOM, paramName, element) {
  //   // xmlDOM is an XML DOM object - it needs to be convered into a select list
  //   var values = [],
  //       displayDiv = element.find('#' + paramName + '_display').html(''), // may want to cache
  //       removeAllDiv = $('<div class="remove-all"><a href="#">Remove all</a></div>'),
  //       multiDelimRegExp = /\s*[,;\n\s]\s*/,
  //       isMultiple = displayDiv.data('multiple'),
  //       maxSelected = displayDiv.data('max-selected'),
  //       select = $('<select/>').prop('multiple', isMultiple),
  //       chosenEvents = [], // jshint ignore:line
  //       chosenOpts = {
  //         disable_search_threshold: 10,

  //         placeholder_text_multiple: 'Select some items',

  //         // allow eg 'kinase binding' as term
  //         enable_split_word_search: false,

  //         // TODO - allow paramRef override
  //         max_selected_options: maxSelected,

  //         // search any part of term
  //         search_contains: true,

  //         width: '35em'
  //       };

  //   maxSelected = $.isNumeric(maxSelected) ? maxSelected : 1000;

  //   $(xmlDOM).find('term').each(function(idx, term) {
  //     $('<option/>')
  //       .val($(term).attr('id'))
  //       .text($(term).text())
  //       .prop('selected', values.indexOf($(term).attr('id')) > -1)
  //       .appendTo(select);
  //   });

  //   select
  //     .appendTo(displayDiv)

  //     .on('chosen:ready', function(event, chosenObj) {
  //       var input = chosenObj.chosen.container.find('input');

  //       if (isMultiple) {
  //         // allow for pasted list of IDs
  //         input[0].onpaste = function() {
  //           // event fires before input value is updated, so we need to
  //           // push the function call down the stack
  //           setTimeout(parsePastedInput.bind(this), 0);
  //         };
  //       }

  //       // if first term contains asterisk, 'turn off' plugin and use raw value
  //       // due to complications with SQL, wildcard support has been dropped
  //       //
  //       // input
  //       //   .one('keyup', function() {
  //       //     cacheChosenEvents(chosenObj);
  //       //   })

  //       //   .on('keyup', function(e) {
  //       //     if (isMultiple && select.find(':selected').length > 0) return;

  //       //     if (this.value.indexOf('*') > -1) {
  //       //       turnOffChosen(chosenObj);
  //       //       if (isMultiple) {
  //       //         chosenObj.chosen.search_field.width('35em');
  //       //       }
  //       //     } else {
  //       //       turnOnChosen(chosenObj);
  //       //     }
  //       //   });
  //     })

  //     // configure chosen
  //     .chosen(chosenOpts);

  //   if (isMultiple) {
  //     // only show Clear all if there are selected items
  //     select.on('change', function() {
  //       removeAllDiv.toggle(select.find(':selected').length > 0);
  //     });

  //     // attach behavior to Clear all link
  //     removeAllDiv.hide().appendTo(displayDiv)
  //       .on('click', 'a', function(e) {
  //         e.preventDefault();
  //         select.find(':selected').prop('selected', false);
  //         select.trigger('chosen:updated');
  //         $(e.delegateTarget).hide();
  //       });
  //   }

  //   // 1. split values
  //   // 2. select options
  //   // 3. refresh chosen
  //   function parsePastedInput() {
  //     // jshint validthis:true
  //     var value = this.value,
  //         unfound = [],
  //         values;
  //     if (!multiDelimRegExp.test(value) /* || !chosen.multi */) {
  //       return;
  //     }
  //     values = value.split(multiDelimRegExp);

  //     // find values in select list, set selected to true, and pop from values
  //     values.forEach(function(value) {
  //       if (value === '') return;

  //       if (select.find('option[value="' + value + '"]')
  //         .prop('selected', true).length !== 1) {
  //         unfound.push(value);
  //       }
  //     });
  //     select.trigger('chosen:updated');

  //     $(this).val(unfound.join(', ') || null).focus();
  //   }

  //   // jshint ignore:start
  //   function cacheChosenEvents(chosenObj) {
  //     chosenEvents.push({ jqElement: chosenObj.chosen.container, events: {} });
  //     chosenEvents.push({ jqElement: chosenObj.chosen.search_field, events: {} });
  //     chosenEvents.push({ jqElement: $(document), events: {} });

  //     chosenEvents.forEach(function(eventsObj) {
  //       var events = $._data(eventsObj.jqElement[0], 'events');
  //       for (var type in events) {
  //         events[type].forEach(function(o) {
  //           if (o.namespace === 'chosen') {
  //             eventsObj.events[type] = eventsObj.events[type] || [];
  //             eventsObj.events[type].push(o);
  //           }
  //         });
  //       }
  //     });
  //   }

  //   function turnOffChosen(chosenObj) {
  //     if (select.data('chosen-off')) return;

  //     chosenEvents.forEach(function(eventsObj) {
  //       for (var type in eventsObj.events) {
  //         eventsObj.jqElement.unbind(type + '.chosen');
  //       }
  //     });

  //     //chosenObj.chosen.dropdown.hide();
  //     chosenObj.chosen.search_results.hide();

  //     select.data('chosen-off', true);
  //   }

  //   function turnOnChosen(chosenObj) {
  //     // jshint loopfunc:true
  //     if (!select.data('chosen-off')) return;

  //     chosenEvents.forEach(function(eventsObj) {
  //       for (var type in eventsObj.events) {
  //         eventsObj.events[type].forEach(function(eventObj) {
  //           eventsObj.jqElement.bind(type + '.chosen', eventObj.handler);
  //         });
  //       }
  //     });

  //     //chosenObj.chosen.dropdown.show();
  //     chosenObj.chosen.search_results.show();

  //     select.data('chosen-off', false);
  //   }

  //   function parsePastedInputjQuery(input, data) {
  //     var value = input.val(),
  //         unfound = [],
  //         multiDelimRegExp = /\s*\n\s*/,
  //         values;

  //     if (!multiDelimRegExp.test(value) /* || !chosen.multi */) {
  //       return;
  //     }

  //     values = value.split(multiDelimRegExp);

  //     // find values in select list, set selected to true, and pop from values
  //     for (var i = 0; i < values.length; i++) {
  //       if (values[i] === '') continue;

  //       if (data.indexOf(value[i]) === -1) {
  //         unfound.push(values[i]);
  //       }
  //     }

  //     return unfound;
  //   }
  //   // jshint ignore:end

  // }

  //==============================================================================
  // jshint ignore:start
  function createAutoComplete(obj, name, element) {
    element.find("div.ac_results").remove(); // Remove any pre-existing type-ahead results.
    var def = [];
    displayTermMap[name] = [];
    termDisplayMap[name] = [];
    var term;
    var display;
    var value = '';
    if( $("term",obj).length !== 0 ) {
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
        if (result.length === 0) {
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

    element.find("#" + name + "_display").val(value).removeAttr('disabled');
  }
  // jshint ignore:end

  //==============================================================================
  function updateDependentParam(dependentParam, element) {
    // jshint loopfunc:true
    // get the current param
    var paramName = dependentParam.attr('name');
    var dependedNames = dependentParam.attr('dependson').split(",");
    var dependedName;
    var i;

    // check if all the depended params are ready
    for (i=0; i < dependedNames.length; i++) {
      dependedName = dependedNames[i];
      var notReady = element.find(".param[name='" + dependedName + "']")
        .find("input, select").prop("disabled");
      if (notReady) return;
    }
  
    var dependedValues = {};
    var hasValue = false;
    // the dependson may contain a comma separated list of param names the current param depends on
    for (i=0; i < dependedNames.length; i++) {
      dependedName = dependedNames[i];
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
    dependentParam = element.find(dependentParamSelector);
    var questionName = dependentParam.closest("form")
        .find("input:hidden[name=questionFullName]").val();
    var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + 
        '&name=' + paramName + '&dependedValue=' + JSON.stringify(dependedValues);

    if (dependentParam.is('[data-type="type-ahead"]')) {
      sendReqUrl = sendReqUrl + '&json=true';
      // dependentParam.find('.loading').show();

      return $.getJSON(sendReqUrl)
        .then(function(data) {
          // createAutoComplete(data, paramName);
          createFilteredSelect(data, paramName, dependentParam);
        })
        .done(function() {
          element.find(".param[name='" + paramName + "']").attr("ready", "");
          dependentParam
            .attr('ready', '')
            .find('input')
              .removeAttr('disabled')
              .end()
            .find('.loading')
              .hide();
        });

    } else if (dependentParam.is('[data-type="filter-param"]')) {
      sendReqUrl = sendReqUrl + '&json=true';
      return $.getJSON(sendReqUrl)
        .then(parseFilterData)
        .then(updateFilterParam.bind(null, dependentParam))
        .done(function() {
          dependentParam.find('input').removeAttr('disabled');
          element.find(".param[name='" + paramName + "']").attr("ready", "");
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
