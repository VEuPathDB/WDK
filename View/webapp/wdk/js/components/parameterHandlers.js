/* global _ */
wdk.util.namespace("window.wdk.parameterHandlers", function(ns, $) {

  var XHR_DATA_KEY = 'dependent-xhr';

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
  //
  // Find all depended parameters and register a change handler for each to
  // update any parameters that depend upon its value.
  //
  //==============================================================================
  function initDependentParamHandlers(element) {
    // jshint loopfunc:true

    // Map depended param names to dependent params names:
    //
    //     { string : Array<string> }
    //
    var dependentParamsMap = Object.create(null);

    // Map dependend param names to value:
    //
    //     { string: string }
    //
    var dependedValuesMap = Object.create(null);

    // Populate dependentParamsMap map by iterating over each dependent param, and
    // for each dependent param, find all params it depends on.
    //
    // Foreach dependentParam P:
    //   Foreach param D depending on P:
    //     Append P to Map[D]
    //
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
        var dependentList = dependentParamsMap[dependedName] ? dependentParamsMap[dependedName] : [];
        dependentList.push(name);
        dependentParamsMap[dependedName] = dependentList;
      }

      $dependentParam.find('input, select').prop('disabled',false);
    });

    // Register change and keyup event handlers to dependent parameter.
    Object.keys(dependentParamsMap).map(function(dependedName) {
      var dependedParam = $("div.param[name='" + dependedName + "']");

      // set previous value
      dependedValuesMap[dependedName] = dependedParam.find('input, select').val();

      var handleChange = function handleChange(e) {
        var newValue = e.target.value;
        var oldValue = dependedValuesMap[dependedName];
        e.stopPropagation();

        if (newValue != oldValue) {
          onDependedParamChange(dependedParam, element, dependentParamsMap);
        }

        dependedValuesMap[dependedName] = newValue;
      };

      dependedParam.change(handleChange);
      dependedParam.keyup(_.debounce(handleChange, 1000));
    });
  }

  function onDependedParamChange(dependedParam, dependentElement, dependentParamsMap) {
        var dependedName = dependedParam.attr("name");
        var $form = dependedParam.closest("form");
        var $submitButton = $form.find('input[type=submit]');

        $submitButton.prop('disabled', true);

        // map list of names to elements
        // then reduce to a list of $.ajax deferred objects
        var dependentDeferreds = dependentParamsMap[dependedName]
          .map(function(dependentName) {

            // return dependentParam reference
            // and set ready flag to false on all its dependent params
            return dependentElement.find(".dependentParam[name='" + dependentName + "']")
              .find("input, select")
                .prop("disabled", true)
                .end();
          })
          .reduce(function(results, $dependentParam) {
            var result =  updateDependentParam($dependentParam, dependentElement);
            if (result) {
              // stash promises returned by $.ajax
              results.push(result);
            }
            return results;
          }, []);

        // trigger form.change only when all deferreds are resolved
        $.when.apply($, dependentDeferreds).then(function() {
          $form.change();
          $submitButton.prop('disabled', false);
        });
  }

  //==============================================================================
  function initTypeAhead(element) {
    var keepPreviousValue = element.closest('form').is('.is-revise');

    element.find('[data-type="type-ahead"]')
      .each(function(i, node) {
        var $param = $(node);
        var questionName = element.closest('form').find('input[name="questionFullName"]').val();
        var paramName = $param.attr('name');

        if ($param.hasClass('dependentParam')) {
          updateDependentParam($param, element, keepPreviousValue);
        } else {
          var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&json=true';

          $.getJSON(sendReqUrl)
            .then(function(data) {
              // createAutoComplete(data, paramName, element);
              createFilteredSelect(data, paramName, $param, keepPreviousValue);
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
    var keepPreviousValue = element.closest('form').is('.is-revise');

    if (filterParams.length > 0) {
      // add class to move prompts to left
      element.addClass('move-left');
    }

    filterParams.each(function(i, node) {
      var $param = $(node);
      if ($param.hasClass('dependentParam')) {
        updateDependentParam($param, element, keepPreviousValue);
      }
      else {
        var questionName = form.find('input[name="questionFullName"]').val();
        var paramName = $param.attr('name');
        var sendReqUrl = 'getVocab.do?questionFullName=' + questionName + '&name=' + paramName + '&json=true';

        $.getJSON(sendReqUrl)
          .then(createFilterParam.bind(null, $param, questionName, {}));
      }
    });
  }

  //==============================================================================
  function createFilterParam($param, questionName, dependedValue, filterData) {
    var $data = $param.data();
    var filterParam = $data.filterParam;

    if (filterParam) filterParam.remove();

    var form = $param.closest('form');
    var title = $data.title;
    // var isAllowEmpty = $param.data('isAllowEmpty');
    var minSelectedCount = $data.minSelectedCount;
    var maxSelectedCount = $data.maxSelectedCount;
    var name = $param.attr('name');
    console.time('intialize render :: ' + name);
    var defaultColumns = $data.defaultColumns;
    var trimMetadataTerms = $data.trimMetadataTerms;
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

    filterParam = new wdk.controllers.FilterParam({
      data: filterData.values,
      metadata: filterData.metadata,
      fields: fields,
      filters: previousValue && previousValue.filters,
      ignored: previousValue && previousValue.ignored,
      trimMetadataTerms: trimMetadataTerms,
      defaultColumns: defaultColumns,
      title: title,
      name: name,
      questionName: questionName,
      dependedValue: dependedValue
    });

    $param.append(filterParam.el);

    // This is a circular reference and potential memory leak, although jQuery seems to make this safe.
    // See http://stackoverflow.com/questions/10092619/precise-explanation-of-javascript-dom-circular-reference-issue
    $param.data('filterParam', filterParam);

    filterParam.on('change:value', function(filterParam, value) {
      var ignored = value.data.filter(datum => datum.isIgnored);
      input.val(JSON.stringify({
        values: _.pluck(value.filteredData, 'term'),
        ignored: _.pluck(ignored, 'term'),
        filters: _.map(value.filters, _.partialRight(_.omit, 'selection'))
      }));
    });

    // filterParam.on('ready', function() {
      $param.find('.loading').hide();
    // });

    form.on('submit', function(e) {
      var filteredData = JSON.parse(input.val()).values;
      var filteredDataCount = filteredData.length;
      var min = minSelectedCount === -1 ? 1 : minSelectedCount;
      var max = maxSelectedCount === -1 ? Infinity : minSelectedCount;
      var condition = max === Infinity
        ? 'at least <b>' + min + '</b>'
        : 'between <b>' + min + '</b> and <b>' + max + '</b>';

      //if (!isAllowEmpty && filteredDataCount === 0) {
      if (filteredDataCount < min || filteredDataCount > max) {
        e.preventDefault();
        $param.find('.ui-state-error').remove();
        $param.prepend([
          '<div class="ui-state-error ui-corner-all" style="padding: .3em .4em;">',
           'You have selected <b>', filteredDataCount, '</b>', title + '.',
           'Please select', condition, title, 'to continue.',
           '</div>'
        ].join(' '));
        filterParam.once('change:value', function() {
          $param.find('.ui-state-error').remove();
        });
        $('html, body').animate({ scrollTop: $param.offset().top - 100 }, 200);
      }
    });

    console.timeEnd('intialize render :: ' + name);
  }

  function createFilteredSelect(vocab, paramName, $param, keepPreviousValue) {
    var $input = $param.find('input[name="value(' + paramName + ')"]'),
        keepOpen = false,
        format = function(item) { return item.display; },
        displayCurrent = function(selectedObject, currentSearchTerm) {
          return currentSearchTerm;
        };

    if (!keepPreviousValue) $input.val('');

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
  function updateDependentParam(dependentParam, element, keepPreviousValue) {
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
      var dependedParam = element.find([
        "[name$='(" + dependedName + ")']:text",    // text input
        "[name$='(" + dependedName + ")']textarea", // textrea
        "[name$='(" + dependedName + ")']:checked", // radio or checkbox
        "[name$='(" + dependedName + ")']select"    // select
      ].join(','));

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

    // Abort in-flight xhr to prevent race condition.
    var previousXhr = dependentParam.data(XHR_DATA_KEY);
    if (previousXhr) previousXhr.abort();

    // Store xhr object in element dataset.
    var xhr;

    if (dependentParam.is('[data-type="type-ahead"]')) {
      sendReqUrl = sendReqUrl + '&json=true';
      // dependentParam.find('.loading').show();

      xhr = $.getJSON(sendReqUrl, function(data) {
        // createAutoComplete(data, paramName);
        createFilteredSelect(data, paramName, dependentParam, keepPreviousValue);
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

      // Hide current param and show loading
      dependentParam
        .find('.filter-param').hide().end()
        .find('.loading').show();

      sendReqUrl = sendReqUrl + '&json=true';
      xhr = $.getJSON(sendReqUrl, function(data) {
        createFilterParam(dependentParam, questionName, dependedValues, data);
        dependentParam
          .find('input').removeAttr('disabled').end()
          .find('.loading').hide();
        element.find(".param[name='" + paramName + "']").attr("ready", "");
      });
    } else {
      xhr = $.ajax({
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
        }
      });
    }

    // store xhr object
    dependentParam.data(XHR_DATA_KEY, xhr);

    // handle failure, unless aborted
    xhr.fail(function (jqXHR, textStatus, errorThrown) {
      if (textStatus != 'abort') {
        alert("Error retrieving dependent param: " + textStatus + "\n" + errorThrown);
      }
    });

    // remove xhr object when it's complete, or if it failed (including abort)
    xhr.always(function() {
      dependentParam.data(XHR_DATA_KEY, undefined);
    });

    // return a Promise
    return xhr.promise();
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
