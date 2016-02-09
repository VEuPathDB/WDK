import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTreeController from '../controllers/checkboxTreeController';
import {
  getTree,
  nodeHasProperty,
  getPropertyValue,
  getTargetType,
  getRefName,
  getId,
  getDisplayName,
  getDescription
} from '../client/utils/OntologyUtils';
import {
  getAttributeDefinition
} from '../client/utils/WDKUtils';
import WdkService from '../client/utils/WdkService';

wdk.util.namespace("wdk.attributeCheckboxTree", function(ns) {
  "use strict";

  /**
   * Entry into checkbox tree load for the attribute checkbox tree which appears when the user
   * clicks the Add Columns button on the header of the results table.
   * @param element - div from which this function was called.
   * @param attributes - attributes derived from the div - question name, record class name, default selected list, current selected list,
   * view name
   * @returns {Promise.<T>}
   */
  function setupCheckboxTree(element, attributes) {
    let questionName = attributes.questionName;
    let recordClassName = attributes.recordClassName;
    let defaultSelectedList = attributes.defaultSelectedList.replace(/'/g,"").split(",");
    let currentSelectedList = attributes.currentSelectedList.replace(/'/g,"").split(",");
    let viewName = attributes.viewName;
    let viewMap = {'_default':'gene', 'transcript-view':'transcript'};
    viewName = viewMap[viewName];
    console.log("Set up checkbox tree for question " + questionName + " and record " + recordClassName + " and view name " + viewName);
    let ServiceUrl = window.location.href.substring(0,
        window.location.href.indexOf("showApplication.do")) + "service";
    let service = new WdkService(ServiceUrl);
    return Promise.all(
      [service.getOntology('Categories'),
       service.findQuestion(question => question.name === questionName),
       service.findRecordClass(recordClass => recordClass.name === recordClassName)]
    ).then(([categoriesOntology, question, recordClass]) => {
        let categoryTree = getTree(categoriesOntology, isQualifying(recordClassName, viewName));
        mungeTree(categoryTree.children, recordClass);
        addSearchSpecificSubtree(question, categoryTree, viewName);
        let selectedList = currentSelectedList || defaultSelectedList;
        console.log("Element: " + element[0]);
        let callback = getAttribute(recordClass);
        let controller = new CheckboxTreeController(element, "attributeList_" + viewName, categoryTree.children, selectedList, null, defaultSelectedList, callback);
        controller.displayCheckboxTree();
    }).catch(function(error) {
      throw new Error(error.message);
    });
  }

  /**
   * Create a predicate function to filter out of the Categories ontology tree those items appropriate for the
   * results page that identify attributes for the current record class.  In the case of the Transcript Record Class, a
   * distinction is made depending on whether the summary view applies to transcripts or genes.
   * @param recordClassName - full name of the current record class
   * @param viewName - either gene or transcript depending on the summary view
   */
  let isQualifying = (recordClassName, viewName) => node => {
      let qualified = nodeHasProperty('targetType', 'attribute', node)
                    && nodeHasProperty('recordClassName', recordClassName, node)
                    && nodeHasProperty('scope', 'results', node);
      if(qualified && recordClassName === 'TranscriptRecordClasses.TranscriptRecordClass') {
        qualified = nodeHasProperty('geneOrTranscript', viewName, node);
      }
      return qualified;
  }

  /**
   * Create a separate search specific subtree, based upon the question asked and tack it onto the start of top level array
   * of nodes in the ontology tree
   * @param question - question posited
   * @param categoryTree - the munged ontology tree
   * @param viewName - the name of the view (not sure how that will fly if everything else is _default
   */
  function addSearchSpecificSubtree(question, categoryTree, viewName) {
    if(question.dynamicAttributes.length > 0) {
      let subtree = {
        "id": "search-specific-subtree",
        "name": "search-specific-subtree",
        "displayName": "Search Specific",
        "EuPathDB alternative term": "Search Specific",
        "description": "Information about the " + viewName + "s returned that is specific to the search you ran, and the parameters you specified",
        "children": []
      };
      question.dynamicAttributes.forEach(attribute => {
        let node = {
          "id": attribute.name,
          "name": attribute.name,
          "displayName": attribute.displayName,
          "EuPathDB alternative term": attribute.displayName,
          "description": attribute.help,
          "children":[]
        };
        subtree.children.push(node);
      });
      categoryTree.children.unshift(subtree);
    }
  }

  function getAttribute(recordClass) {
    return node => {
      return getAttributeDefinition(recordClass, getRefName(node));
    }
  }

  /**
   * Convert filtered/compacted tree provided by ontology service into a tree organized in the
   * matter expected by the checkbox tree component.
   * @param nodes - array of the top level nodes of the filtered/compacted ontology tree
   * @param recordClass - The record class data applying to the current results page - used to properly identify the
   * attributes (leafs) of the tree.
   */
  function mungeTree(nodes, recordClass) {
    nodes.forEach((node) => {
      let targetType = getTargetType(node);
      if (targetType === 'attribute') {
        let name = getRefName(node);
        let attribute = recordClass.attributes.find(a => a.name === name);
        //if(attribute == null) throw new Error('Expected attribute for `' + name + '`, but got null');
        if(attribute == null) {
          //console.log("No attribute for " + name);
          node.displayName = name + "??";
          node.description = name + "??";
          node.id = "attribute_" + getId(node);
        }
        else {
          node.displayName = attribute.displayName;
          node.description = attribute.help;
          node.id = name;
        }

      }
      else {
        node.id = getId(node);
        node.displayName = getDisplayName(node);
        node.description = getDescription(node);
      }
      //delete(node.properties);
      if(node.children.length > 0) {
        mungeTree(node.children, recordClass);
      }
    });
  }
  
  ns.setupCheckboxTree = setupCheckboxTree;

});