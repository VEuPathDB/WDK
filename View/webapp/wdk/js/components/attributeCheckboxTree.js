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
import WdkService from '../client/utils/WdkService';

wdk.util.namespace("wdk.attributeCheckboxTree", function(ns) {
  "use strict";
  
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
        let categoryTree = getTree(categoriesOntology, isQualifying(recordClassName));
        mungeTree(categoryTree.children, recordClass);
        addSearchSpecificSubtree(question, categoryTree);
        let selectedList = currentSelectedList || defaultSelectedList;
        let controller = new CheckboxTreeController(element, "attributeList_" + viewName, categoryTree.children, selectedList, null, defaultSelectedList);
        controller.displayCheckboxTree();
    }).catch(function(error) {
      throw new Error(error.message);
    });
  }

  let isQualifying = (recordClassName, viewName) => node => {
      let qualified = nodeHasProperty('targetType', 'attribute', node)
                    && nodeHasProperty('recordClassName', recordClassName, node)
                    && nodeHasProperty('scope', 'results', node);
      if(recordClassName === 'TranscriptRecordClasses.TranscriptRecordClass') {
        qualified === qualified && nodeHasProperty('geneOrTranscript', viewName, node);
      }
      return qualified;
  }

  //TODO replace XXXXs
  function addSearchSpecificSubtree(question, categoryTree) {
    if(question.dynamicAttributes.length > 0) {
      let subtree = {
        "id": "search-specific-subtree",
        "displayName": "Search Specific",
        "description": "Information about the XXXXs returned that is specific to the search you ran, and the parameters you specified",
        "children": []
      };
      question.dynamicAttributes.forEach(attribute => {
        let node = {
          "id": attribute.name,
          "displayName": attribute.displayName,
          "description": attribute.help,
          "children":[]
        }
        subtree.children.push(node);
      })
      categoryTree.children.unshift(subtree);
    }
  }


  function mungeTree(nodes, recordClass) {
    nodes.forEach((node) => {
      let targetType = getTargetType(node);
      if (targetType === 'attribute') {
        let name = getRefName(node);
        let attribute = recordClass.attributes.find(a => a.name === name);
        //if(attribute == null) throw new Error('Expected attribute for `' + name + '`, but got null');
        if(attribute == null) {
          console.log("No attribute for " + name);
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
      delete(node.properties);
      if(node.children.length > 0) {
        mungeTree(node.children, recordClass);
      }
    });
  }
  
  ns.setupCheckboxTree = setupCheckboxTree;

});