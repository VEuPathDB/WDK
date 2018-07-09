/* global wdk */
import _ from 'lodash';
import { Seq } from 'Utils/IterableUtils';

// eslint-disable-next-line no-unused-vars
import { UNRECOVERABLE_PARAM_ERROR_EVENT } from 'Views/Question/LegacyParamController';

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
    initLegacyParamControllers(element);


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
      dependedValuesMap[dependedName] = dependedParam.find(':text, textarea, :checked, select').val();

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

  // Connect LegacyParamControllers to the rest of the form.
  // - Find the set of uniq depended param names
  // - Listen to change events for all of them
  // - When any changes, update all LegacyParamControllers with updated `paramValues` prop
  function initLegacyParamControllers($element) {
    let legacyParamControllers = $element.find('[data-name=LegacyParamController]').toArray();
    Seq.from(legacyParamControllers)
      .map(el => el.getAttribute('dependson'))
      .filter(dependson => dependson != null)
      .flatMap(dependson => dependson.split(/\s*,\s*/))
      .uniq()
      .forEach(name => {
        $element.on('change', `[name="array(${name})"], [name="value(${name})"]`, event => {
          legacyParamControllers.forEach(el => {
            const propsAttrValue = el.getAttribute('data-props');

            if (propsAttrValue == null) {
              console.error('Expected data-props to have a value ... skipping');
              return;
            }

            const prevProps = JSON.parse(propsAttrValue);
            const { value } = event.target;

            // `prevProps.paramValues` may be undefined
            if (_.get(prevProps, ['paramValues', name]) === value) return;

            const nextProps = Object.assign({}, prevProps, {
              paramValues: Object.assign({}, prevProps.paramValues, {
                [name]: value
              })
            });

            el.setAttribute('data-props', JSON.stringify(nextProps));
          });
        });
      });


    // We don't need the following, since we can handle validation concerns on
    // the back end, but keeping it around in case we find a case where this is
    // useful.
    /*
    $element
      .closest('#qf_content')
      .one(UNRECOVERABLE_PARAM_ERROR_EVENT, event => {
        let paramValues;
        try {
          paramValues = JSON.parse(event.target.parentElement.dataset.props).paramValues
        }
        catch(error) {
          console.warn('Bailing on replace param instructions');
          return;
        }

        $(event.currentTarget).append(makeOverlayHtml({ paramValues }))
      });
      */
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

  function createFilteredSelect(vocab, paramName, $param, keepPreviousValue) {
    var $input = $param.find('input[name="value(' + paramName + ')"]'),
        format = function(item) { return item.display; },
        displayCurrent = function(selectedObject, currentSearchTerm) {
          return currentSearchTerm;
        },
        delimRegex = /[,;\n\s]+/,
        terms = new Set(vocab.values.map(value => value.term)),
        isMultiple = $param.data('multiple');

    if (!keepPreviousValue) $input.val('');

    $input.select2({
      placeholder: 'Begin typing to see suggestions...',
      minimumInputLength: 3,
      allowClear: true,
      multiple: isMultiple,
      id: 'term',
      matcher(term, text) {
        return term.split(';').some(termPart => text.toUpperCase().indexOf(termPart.trim().toUpperCase()) > -1)
      },
      data: { results: vocab.values, text: 'display' },
      formatSelection: format,
      formatResult: format,
      nextSearchTerm: displayCurrent
    });

    // Removing this feature since it is confusing.
    // closeOnSelect doesn't work, so this is a fix
    // $input.on('select2-selecting', function() {
    //   $input.one('select2-close', function() {
    //     if (isMultiple) $input.select2('open');
    //   })
    // });

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

// eslint-disable-next-line no-unused-vars
function makeOverlayHtml({ paramValues }) {
  const style = `
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    margin: 72px 9px 0;
    padding: 2em;
    font-size: 1.4em;
    background: #ffffff;
  `;
  return `
    <div style="${style}">
      <div>
        This search is no longer valid and must be recreated. You can do so by following these steps:
      </div>
      <ol>
        <li>Copy these steps into a text document for future reference. Also, make note of the search name above.</li>
        <li>Close this popup by clicking the X in the top right corner.</li>
        <li>Add a new search to the left of this one:
          <ol>
            <li>Put your mouse cursor over the search box in the strategy panel, and click "Edit".
            <li>In the menu of the box that comes up, click "Insert step before".</li>
            <li>In the table of searches that comes up, click on the search name that matches the search name above.</li>
            <li>In the search form, fill in the values, using the values below as a reference.</li>
            <li>Once the values are filled in, click on "Get Answer".</li>
          </ol>
        </li>
        <li>Finally, delete the search to the right of the newly created search:
          <ol>
            <li>Put your mouse cursor over the search box in the strategy panel, and click "Edit".</li>
            <li>In the menu of the box that comes up, click "Delete".</li>
          </ol>
        </li>
        <li>
          You should now be able to view the results of your search.
        </li>
      </ol>
      <div>
        <pre>${JSON.stringify(paramValues, null, 4)}</pre>
      </div>
    </div>
  `;
}
