/* global wdk */
import _ from 'lodash';
import * as ReactDOM from 'react-dom';
import LazyFilterService from '../client/utils/LazyFilterService';
import { getTree } from '../client/utils/FilterServiceUtils';
import { Seq } from '../client/utils/IterableUtils';
import AttributeFilter from '../client/components/AttributeFilter';

wdk.namespace("window.wdk.parameterHandlers", function(ns, $) {

  var XHR_DATA_KEY = 'dependent-xhr';
  var QUESTION_INITIALIZE_EVENT = ns.QUESTION_INITIALIZE_EVENT = 'initialize.wdk-param';
  var PARAM_LOADING_EVENT = ns.PARAM_LOADING_EVENT = 'loading.wdk-param';
  var PARAM_DESTROY_EVENT = ns.PARAM_DESTROY_EVENT = 'destroy.wdk-param';

  //==============================================================================
  function init(element) {

    attachLoadingListener(element);
    initDependentParamHandlers(element);
    initTypeAhead(element);
    initFilterParam(element);

    // need to trigger the click event so that the stage is set correctly on revise.
    element.find("#operations input[type='radio']:checked").click();

    // Add listener for FORM_DESTROY_EVENT and trigger PARAM_DESTROY_EVENT for each params
    element.closest('form').one(wdk.addStepPopup.FORM_DESTROY_EVENT, () => {
      element.find('.param').trigger(PARAM_DESTROY_EVENT);
    });

    // trigger initialize event
    element.trigger(QUESTION_INITIALIZE_EVENT);
  }

  //==============================================================================
  //
  // Listen to PARAM_LOADING_EVENT events on question element. The event will come
  // with an additional boolean param to indicate if it is in a loading state or
  // not. This value will be set on a map. After each event, if some are loading,
  // then the submit button will be disabled; otherwise it will be enabled.
  //
  //==============================================================================
  function attachLoadingListener(element) {
    let loadingParams = new Map();
    let submit = element.closest('form').find(':input[name=questionSubmit]');
    let originalValue = submit.val();

    element.on(PARAM_LOADING_EVENT, function(event, isLoading) {
      loadingParams.set(event.target, isLoading);

      let someLoading = Array.from(loadingParams.values())
      .reduce(function(acc, isLoading) {
        return acc || isLoading;
      });

      submit.prop('disabled', someLoading).val(someLoading ? 'Loading...' : originalValue);
    });
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
    Object.keys(dependentParamsMap).forEach(function(dependedName) {
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
      // Updating 2 seconds after keyup has proven problematic, so commenting out. -dmf
      // dependedParam.keyup(_.debounce(handleChange, 2000));
    });
  }

  function onDependedParamChange(dependedParam, dependentElement, dependentParamsMap) {
    var dependedName = dependedParam.attr("name");
    var $form = dependedParam.closest("form");

    // map list of names to elements
    // then reduce to a list of $.ajax deferred objects
    var dependentDeferreds = dependentParamsMap[dependedName]
      .map(function (dependentName) {

        // return dependentParam reference
        // and set ready flag to false on all its dependent params
        return dependentElement.find(".dependentParam[name='" + dependentName + "']")
          .trigger(PARAM_DESTROY_EVENT)
          .find("input, select")
          .prop("disabled", true)
          .end();
      })
      .reduce(function (results, $dependentParam) {
        var result = updateDependentParam($dependentParam, dependentElement);
        if (result) {
          // stash promises returned by $.ajax
          results.push(result);
        }
        return results;
      }, []);

    // trigger form.change only when all deferreds are resolved
    $.when(...dependentDeferreds).then(function () {
      $form.change();
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

          getParamJson($param, sendReqUrl)
            .then(function(data) {
              createFilteredSelect(data, paramName, $param, keepPreviousValue);
            });
        }

        // add loading event handling
        $param.on(PARAM_LOADING_EVENT, (event, isLoading) => $param.find('.loading').toggle(isLoading));
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

        getParamJson($param, sendReqUrl)
        .then(createFilterParam.bind(null, $param, questionName, {}));
      }

      // add loading event handling
      $param.on(PARAM_LOADING_EVENT, (event, isLoading) => $param.find('.loading').toggle(isLoading));
    });
  }

  //==============================================================================
  function createFilterParam($param, questionName, dependedValue, filterData, keepPreviousValue) {
    var filterParamContainer = $param.find('.filter-param-container')[0];
    var $data = $param.data();
    var $loading = $param.find('.loading').remove();
    $param.one(PARAM_DESTROY_EVENT, () => {
      ReactDOM.unmountComponentAtNode(filterParamContainer)
      $param.append($loading);
    });

    var form = $param.closest('form');
    var title = $data.title;
    var filterDataTypeDisplayName = $data.filterDataTypeDisplayName;
    // var isAllowEmpty = $param.data('isAllowEmpty');
    var minSelectedCount = $data.minSelectedCount;
    var maxSelectedCount = $data.maxSelectedCount;
    var name = $param.attr('name');
    console.time('intialize render :: ' + name);
    // var defaultColumns = $data.defaultColumns ? $data.defaultColumns.split(/\s+/) : [];
    var trimMetadataTerms = $data.trimMetadataTerms;
    var input = $param.find('input');
    var previousValue;

    // get previous values
    if (keepPreviousValue) {
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
    }

    var fields = new Map(Seq.from(filterData.metadataSpec)
      .filter(field => field.term != null)
      .map(field =>
        [field.term, Object.assign({ display: field.term }, field)]));

    var [ validFilters, invalidFilters ] = _(_.get(previousValue, 'filters'))
      .partition(filter => fields.has(filter.field))
      .value();

    var filterParamOptions = { title, filterDataTypeDisplayName, trimMetadataTerms };

    var filterService = new LazyFilterService({
      name,
      fields,
      data: filterData.values,
      questionName,
      dependedValue,
      metadataUrl: wdk.webappUrl('getMetadata.do')
    });

    filterService.updateFilters(validFilters);
    filterService.updateIgnoredData(_.get(previousValue, 'ignoredData', []));
    filterService.selectField(_.get(validFilters, '0.field', _.get(findLeaf(getTree(fields.values())), 'term')));

    // This is a circular reference and potential memory leak, although jQuery seems to make this safe.
    // See http://stackoverflow.com/questions/10092619/precise-explanation-of-javascript-dom-circular-reference-issue
    $param.data('filterService', filterService);
    $param.trigger('filterParamDidMount');

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
        var sub = filterService.addListener(function() {
          $param.find('.ui-state-error').remove();
          sub.remove();
        });
        $('html, body').animate({ scrollTop: $param.offset().top - 100 }, 200);
      }
    });

    function updateInputFromFilterService() {
      var ignored = filterService.data.filter(datum => datum.isIgnored);
      var filteredData = filterService.filteredData.filter(datum => !ignored.includes(datum));
      input.val(JSON.stringify({
        values: _.map(filteredData, entry => entry.term),
        ignored: _.map(ignored, entry => entry.term),
        filters: filterService.filters
      }));

      // trigger loading event on $param
      triggerLoading($param, filterService.isLoading);
      renderFilterParam(filterService, filterParamOptions, invalidFilters, filterParamContainer);
    }

    updateInputFromFilterService();
    filterService.addListener(updateInputFromFilterService);

    console.timeEnd('intialize render :: ' + name);

    /**
     * Find first leaf of tree
     * @param {TreeNode<Field>} fields
     * @param {string?} parentTerm
     */
    function findLeaf(node) {
      return node.children.length === 0 ? node.field : findLeaf(node.children[0]);
    }
  }

  function renderFilterParam(filterService, options, invalidFilters, el) {
    let state = filterService.getState();
    ReactDOM.render(
      <AttributeFilter
        displayName={options.filterDataTypeDisplayName || options.title}

        fields={state.fields}
        filters={state.filters}
        dataCount={state.data.length}
        filteredData={state.filteredData}
        ignoredData={state.ignoredData}
        columns={state.columns}
        activeField={state.selectedField}
        activeFieldSummary={state.distributionMap[state.selectedField]}
        fieldMetadataMap={state.fieldMetadataMap}

        isLoading={state.isLoading}
        invalidFilters={invalidFilters}

        onActiveFieldChange={filterService.selectField}
        onFiltersChange={filterService.updateFilters}
        onColumnsChange={filterService.updateColumns}
        onIgnoredDataChange={filterService.updateIgnoredData}
      />, el);
  }

  function createFilteredSelect(vocab, paramName, $param, keepPreviousValue) {
    var $input = $param.find('input[name="value(' + paramName + ')"]'),
        format = function(item) { return item.display; },
        displayCurrent = function(selectedObject, currentSearchTerm) {
          return currentSearchTerm;
        },
        delimRegex = /[,;\n\s]+/,
        terms = new Set(vocab.values.map(value => value.term));

    if (!keepPreviousValue) $input.val('');

    $input.select2({
      placeholder: 'Begin typing to see suggestions...',
      minimumInputLength: 3,
      allowClear: true,
      closeOnSelect: false,
      multiple: $param.data('multiple'),
      id: 'term',
      matcher(term, text) {
        return term.split(';').some(termPart => text.toUpperCase().indexOf(termPart.trim().toUpperCase()) > -1)
      },
      data: { results: vocab.values, text: 'display' },
      formatSelection: format,
      formatResult: format,
      nextSearchTerm: displayCurrent
    });

    // closeOnSelect doesn't work, so this is a fix
    $input.on('select2-selecting', function() {
      $input.one('select2-close', function() {
        $input.select2('open');
      })
    });

    // Search for matching vocab values, then use $input.select2('val', ...)
    $input.select2('container').find('input.select2-input').on('paste', function(event) {
      event.preventDefault();
      const currentValues = $input.select2('val');
      const parsedInput = event.originalEvent.clipboardData.getData('text').trim().split(delimRegex);
      const [ known, unknown ] = _.partition(parsedInput, value => terms.has(value));

      if (known.length > 0) {
        $input.select2('val', currentValues.concat(known));
      }

      if (unknown.length > 0) {
        event.target.value = unknown.join('; ');
      } else {
        $input.select2('close');
      }
    });

    // remove invalid values from select2 inputs
    $param.closest('form').on('submit', function() {
      var $select2Container = $param.find('select2-container');
      var values = $select2Container.next().val();

      if (values) {
        $select2Container('val', values.split(','));
      }
    });
  }

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
        "[name$='(" + dependedName + ")']:hidden",  // hidden input
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
        '&name=' + paramName + '&dependedValue=' + encodeURIComponent(JSON.stringify(dependedValues));

    // Abort in-flight xhr to prevent race condition.
    var previousXhr = dependentParam.data(XHR_DATA_KEY);
    if (previousXhr) previousXhr.abort();

    // Store xhr object in element dataset.
    var xhr;

    if (dependentParam.is('[data-type="type-ahead"]')) {
      sendReqUrl = sendReqUrl + '&json=true';

      xhr = $.getJSON(sendReqUrl, function(data) {
        createFilteredSelect(data, paramName, dependentParam, keepPreviousValue);
        element.find(".param[name='" + paramName + "']").attr("ready", "");
        dependentParam
          .attr('ready', '')
          .find('input').removeAttr('disabled');
      });

    } else if (dependentParam.is('[data-type="filter-param"]')) {

      // Hide current param and show loading
      dependentParam
        .find('.filter-param').hide();

      sendReqUrl = sendReqUrl + '&json=true';
      xhr = $.getJSON(sendReqUrl, function(data) {
        createFilterParam(dependentParam, questionName, dependedValues, data, keepPreviousValue);
        dependentParam
          .find('input').removeAttr('disabled');
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

    // notify listeners that the param is loading
    triggerLoading(dependentParam, true);

    // remove xhr object when it's complete, or if it failed (including abort)
    xhr.always(function() {
      dependentParam.data(XHR_DATA_KEY, undefined);
      // notify listeners that the param is not loading
      triggerLoading(dependentParam, false);
    });

    // return a Promise
    return xhr.promise();
  }

  /**
   * Trigger a PARAM_LOADING_EVENT on the given param.
   *
   * @param {jQuery} $param The param
   * @param {boolean} isLoading The param is loading
   */
  function triggerLoading($param, isLoading) {
    $param.trigger(PARAM_LOADING_EVENT, [ !!isLoading ]);
  }

  /**
   * Utility to get param JSON. Handles errors and triggering loading events.
   */
  function getParamJson($param, url) {
    triggerLoading($param, true);
    return $.getJSON(url)
    .fail(function(jqXHR, textStatus, reason) {
      var paramName = $param.closest('.param-item').find('>label').text().trim() ||
        'An unknown param';
      var message = paramName + ' could not be loaded: ' + reason;
      alert(message);
      console.error(message);
    })
    .always(function() {
      triggerLoading($param, false);
    });
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
