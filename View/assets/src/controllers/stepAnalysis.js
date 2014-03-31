wdk.util.namespace("window.wdk.stepAnalysis", function(ns, $) {
	"use strict";

	/*************************************************************************
	 * 
	 * This library enables the creation, execution, and deletion of Step
	 * Analysis instances.  Once run, each instance references an analysis
	 * result, which may be shared between instances.
	 * 
	 * JSON for analysis instances is defined as: {
	 *   analysisId: int
	 *   analysisName: string
	 *   stepId: int
	 *   strategyId: int
	 *   displayName: string
	 *   description: string
	 *   status: enumerated string, see org.gusdb.wdk.model.user.analysis.ExecutionStatus
	 *   params: key-value object of params
	 * }
	 * 
	 * The functions below depend on the following services (see more details in code below):
	 * 
	 *  - createAnalysis:
	 *      takes:   strategy id, step id, analysis name
	 *      returns: instance json
	 *  - copyAnalysis:
	 *      takes:   analysis id
	 *      returns: instance json for copy
	 *  - runAnalysis:
	 *      takes:   analysis form submission (including analysisId hidden param)
	 *      returns: instance json
	 *  - getAnalysis:
	 *      takes:   analysis id
	 *      returns: instance json
	 *  - renameAnalysis:
	 *      takes:   analysis id, display name
	 *      returns: instance json
	 *  - deleteAnalysis:
	 *      takes:   analysis id
	 *      returns: instance json
	 *  - getAll:
	 *      takes:   nothing
	 *      returns: json for all analysis instances and results
	 * 
	 * The following pages each take an analysis id and return HTML:
	 * 
	 *  - getPane: returns a container for an analysis form and result
	 *  - getForm: returns an unpopulated form for a specific type of analysis
	 *  - getResults: returns a DOM fragment displaying analysis results
	 * 
	 *************************************************************************/
	
	var ROUTES = {
		createAnalysis: { url: '/createStepAnalysis.do', method: 'POST', type: 'json' },
		copyAnalysis:   { url: '/copyStepAnalysis.do',   method: 'POST', type: 'json' },
		runAnalysis:    { url: '/runStepAnalysis.do',    method: 'POST', type: 'json' },
		renameAnalysis: { url: '/renameStepAnalysis.do', method: 'POST', type: 'json' },
		deleteAnalysis: { url: '/deleteStepAnalysis.do', method: 'POST', type: 'json' },
		getAnalysis:    { url: '/stepAnalysis.do',       method: 'GET',  type: 'json' },
		getPane:        { url: '/stepAnalysisPane.do',   method: 'GET',  type: 'html' },
		getForm:        { url: '/stepAnalysisForm.do',   method: 'GET',  type: 'html' },
		getResult:      { url: '/stepAnalysisResult.do', method: 'GET',  type: 'html' },
		getAll:         { url: '/stepAnalysisAll.do',    method: 'GET',  type: 'json' }
	};
	
	function doAjax(route, ajaxConfig) {
		ajaxConfig.url = wdk.webappUrl(route.url);
		ajaxConfig.type = route.method;
		ajaxConfig.dataType = route.type;
		return $.ajax(ajaxConfig);
	}
	
	function configureAnalysisViews($element) {
		// add delete buttons to step analysis tabs (must do this after tabs are applied)
		$element.find("li[id^='step-analysis']").each(function(){ addDeleteButton(this); });
  
		// add hover and click handlers for step analysis add buttons
		var newAnalysisDiv = $element.find('.new-analysis');
		var newAnalysisButton = $(newAnalysisDiv).find(".new-analysis-button");
		var newAnalysisInstr = $(newAnalysisDiv).find(".new-analysis-instr");
		var newAnalysisMenu = $(newAnalysisDiv).find(".new-analysis-menu");
		$(newAnalysisDiv).hover(
				function() {
					//newAnalysisInstr.html("Analyze This Result");
					newAnalysisButton.css("border","1px solid black");
					newAnalysisMenu.show();
				},
				function() {
					//newAnalysisInstr.html("");
					newAnalysisButton.css("border","1px solid #AAAAAA");
					newAnalysisMenu.hide();
				}
		);
		$(newAnalysisMenu).find("li").click(function(event){
			newAnalysisMenu.hide();
			var data = $(event.target).data();
			createStepAnalysis(data.analysis, data.strategy, data.step);
		});
	}

	function addDeleteButton(tabElement) {
		var errorMsg = "Cannot delete this analysis at this time.  Please " +
			"try again later, or contact us if the problem persists.";
		$(tabElement).find(".ui-closable-tab").live("click", function(e) {
			if (!confirm("Are you sure you want to delete this analysis? " +
				" You will not be able to retrieve it later.")) { return; }
			var button = e.target;
			var analysisId = $(tabElement).attr('id').substring(14);
			doAjax(ROUTES.deleteAnalysis, {
				data: { "analysisId": analysisId },
				success: function(data, textStatus, jqXHR) {
					var tabContainerDiv = $(button).closest(".ui-tabs").attr("id");
					var panelId = $(button).closest("li").remove().attr("aria-controls");
					$("#"+panelId ).remove();
					$("#"+tabContainerDiv).tabs("refresh");
				},
				error: function(jqXHR, textStatus, errorThrown) {
					handleAjaxError(errorMsg);
				}
			});
		});
	}
	
	function createStepAnalysis(analysisName, strategyId, stepId) {
		// ask server to create new step analysis with the given params
		doAjax(ROUTES.createAnalysis, {
			data: { "analysisName": analysisName, "strategyId": strategyId, "stepId": stepId },
			success: function (data, textStatus, jqXHR) {
				createAnalysisTab(data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to create new step analysis of type: " + analysisName);
			}
		});
	}
	
	function copyStepAnalysis(analysisId) {
		doAjax(ROUTES.copyAnalysis, {
			data: { "analysisId": analysisId },
			success: function(data, textStatus, jqXHR) {
				createAnalysisTab(data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to create new step analysis from existing with id: " + analysisId);
			}
		});
	}

	function createAnalysisTab(data) {
		// create, add, and select new tab representing this analysis
		var analysisId = data.analysisId;
		var displayName = data.displayName;
		var description = data.description;
		var tabUrl = wdk.webappUrl(ROUTES.getPane.url) +"?analysisId=" + analysisId;
		var tabId = "step-analysis-" + analysisId;
		var tabIndex = $('#Summary_Views ul.ui-tabs-nav > li').length - 1;
		var tabContent = '<li id="' + tabId + '">' +
			'<a href="' + tabUrl + '" title="' + description + '">' +
			displayName + '<span></span><span ' +
			'class="ui-icon ui-icon-circle-close ui-closable-tab step-analysis-close-icon"></span></a></li>';
		$('#Summary_Views ul.ui-tabs-nav > li:last').before(tabContent);
		$('#Summary_Views').tabs('refresh');
		addDeleteButton($('#Summary_Views').find('#'+tabId)[0]);
		$('#Summary_Views').tabs('option', 'active', tabIndex);
	}
	
	function loadDisplaySubpanes($element, $attrs) {
		var analysisId = $attrs.analysisId;
		// get json representing analysis (params + status, but not result)
		doAjax(ROUTES.getAnalysis, {
			data: { "analysisId": analysisId },
			success: function(data, textStatus, jqXHR) {
				// load form and (if necessary) populate selected values
				loadAnalysisForm($element, data);				
				// load results
				loadResultsPane($element, analysisId);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to retrieve step analysis json for id: " + analysisId);
			}
		});
	}
	
	function loadAnalysisForm($element, analysisObj) {
		var analysisId = analysisObj.analysisId;
		// fetch plugin's form
		doAjax(ROUTES.getForm, {
			data: { "analysisId": analysisId },
			success: function(data, textStatus, jqXHR) {
				// convert returned page into contained DOM elements
				var returnedDomElements = $.parseHTML(data);
				
				// wrap all elements with a div
				var wrappingDiv = $('<div class="stepAnalysisFormContainer"></div>');
				for (var i=0; i < returnedDomElements.length; i++) {
					$(wrappingDiv).append(returnedDomElements[i]);
				}
				
				// configure form for submission to the step analysis runner action
				var hiddenField = '<input type="hidden" name="analysisId" value="' + analysisId + '"/>';
				$(wrappingDiv).find("form").first()
					.append(hiddenField)
					.submit(function(event) {
						event.preventDefault();
						runStepAnalysis(event.target);
						return false;
					});
				
				var formPane = $element.find(".step-analysis-form-pane");
				formPane.html(wrappingDiv);
				wdk.formUtil.populateForm(formPane.find('form').first(), analysisObj.formParams);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to retrieve step analysis form for analysis with id " + analysisId);
			}
		});
	}
	
	function loadResultsPane($element, analysisId) {
		var resultsPane = $element.find('.step-analysis-results-pane');
		// clear previous results
		resultsPane.empty();
		doAjax(ROUTES.getResult, {
			data: { "analysisId": analysisId },
			success: function(data, textStatus, jqXHR) {
				if (data == "") {
					// empty result means no analysis has yet been run
					return;
				}
				
				// convert returned page into contained DOM elements
				var returnedDomElements = $.parseHTML(data);
				
				// place discovered elements in results pane
				for (var i=0; i < returnedDomElements.length; i++) {
					resultsPane.append(returnedDomElements[i]);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to load results for step analysis with id: " + analysisId);
			}
		});
	}
	
	function runStepAnalysis(form) {
		var analysisId = $(form).find('input[name=analysisId]').val();
		var $errorsPane = $(form).parents('.step-analysis-subpane').find('.step-analysis-errors-pane');
		// clear any errors from a previous submission
		$errorsPane.empty();
		doAjax(ROUTES.runAnalysis, {
			data: $(form).serialize(),
			success: function(data, textStatus, jqXHR) {
				if (data.status == "success") {
					// if success, then alert user and load results pane
					loadResultsPane($(form).parents('.step-analysis-pane'), data.context.analysisId);
				}
				else if (data.status == "validation") {
					var $newErrorList = $("<ul></ul>");
					data.errors.forEach(function(val) {
						$newErrorList.append("<li>"+val+"</li>");
					});
					$errorsPane.append($newErrorList);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to run step analysis.");
			}
		});
	}
	
	/* Guess I don't need this any more???
	function findTabIndexById(tabContainerSelector, tabId) {
		var tabs = $(tabContainerSelector).find('ul.ui-tabs-nav > li');
		for (var i=0; i < tabs.length; i++) {
			if ($(tabs[i]).attr('id') == tabId) {
				return i;
			}
		}
		return -1;
	}
	*/
	
	function analysisRefresh($obj, $attrs) {
		var analysisId = $attrs.analysisid;
		var secondsLeft = 0 + $obj.find('.countdown').html();
		doRefreshCountdown($obj, analysisId, secondsLeft);
	}
	  
	function doRefreshCountdown($obj, analysisId, secondsLeft) {
		
		if (secondsLeft == 0) {
			setTimeout(function() {
				// refresh results pane to see if results are present
				loadResultsPane($obj.parents('.step-analysis-pane'), analysisId);
			}, 1000);
		}
		else {
			// count down one second and update timer display
			setTimeout(function() {
				var newRemaining = secondsLeft - 1;
				$obj.find('.countdown').html(newRemaining);
				doRefreshCountdown($obj, analysisId, newRemaining);
			}, 1000);
		}
	}
	
	function showAllAnalyses() {
		doAjax(ROUTES.getAll, {
			success: function(data, textStatus, jqXHR) {
				var jsonDisplay = JSON.stringify(data, undefined, 2);
				var html = "<div><pre>" + jsonDisplay + "</pre></div>";
				$(html).dialog({ modal:true });
			},
			error: function(jqXHR, textStatus, errorThrown) {
				handleAjaxError("Error: Unable to retrieve all analysis json.");
			}
		});
	}
	
	function renameStepAnalysis(analysisId) {
		var newName = prompt("New name:");
		if (newName != null && newName != '') {
			doAjax(ROUTES.renameAnalysis, {
				data: { "analysisId": analysisId, "displayName": newName },
				success: function(data, textStatus, jqXHR) {
					$('#step-analysis-' + analysisId + " a").contents().filter(function() {
					    return this.nodeType == 3; //Filtering by text node
					}).first()[0].data = newName;
				},
				error: function(jqXHR, textStatus, errorThrown) {
					handleAjaxError("Error: Unable to change display name for analysis with id " + analysisId);
				}
			});
		}
	}
	
	function handleAjaxError(message) {
		alert(message);
	}
	
	ns.configureAnalysisViews = configureAnalysisViews;
	ns.loadDisplaySubpanes = loadDisplaySubpanes;
	ns.copyStepAnalysis = copyStepAnalysis;
	ns.renameStepAnalysis = renameStepAnalysis;
	ns.analysisRefresh = analysisRefresh;
	ns.showAllAnalyses = showAllAnalyses;
});
