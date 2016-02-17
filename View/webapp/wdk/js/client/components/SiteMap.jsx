import React from 'react';
import { wrappable } from '../utils/componentUtils';
import CheckboxTree from './CheckboxTree';
import { getTargetType, getRefName, getDisplayName, getDescription, getId } from '../utils/OntologyUtils';

let SiteMap = React.createClass({

/**
 *  "properties": {
    "scope": [
      "download"
    ],
    "recordClassName": [
      "OrganismRecordClasses.OrganismRecordClass"
    ],
    "name": [
      "is_reference_strain"
    ],
    "label": [
      "OrganismRecordClasses.OrganismRecordClass.is_reference_strain"
    ],
    "targetType": [
      "attribute"
    ]
  },

 * AttributeField JSON will have the following form:
 * {
 *   name: String,
 *   displayName: String,
 *   help: String,
 *   align: String,
 *   isSortable: Boolean,
 *   isRemovable: Boolean,
 *   type: String (comes from “type” property of attribute tag),
 *   category: String,
 *   truncateTo: Integer,
 *   isDisplayable: Boolean,
 *   isInReport: Boolean,
 *   properties: Object
 * }
 *
 * WDK Question objects have the following form:
 * {
 *   name: String,
 *   displayName: String,
 *   shortDisplayName: String,
 *   description: String,
 *   help: String,
 *   newBuild: Number,
 *   reviseBuild: Number,
 *   urlSegment: String,
 *   class: String,
 *   parameters: [ see ParamFormatters ],
 *   defaultAttributes: [ String ],
 *   dynamicAttributes: [ see AttributeFieldFormatter ],
 *   defaultSummaryView: String,
 *   summaryViewPlugins: [ String ],
 *   stepAnalysisPlugins: [ String ]
 * }
 */

 getNodeData(node) {
  let data = {};
  data.id = getId(node);
  let targetType = getTargetType(node);
  if (node.wdkReference) {
    data.displayName = targetType + ": " + node.wdkReference.displayName;
    data.description = node.wdkReference.description;
  } else {
    data.displayName = getDisplayName(node);
    data.description = getDescription(node);
  }
  if (targetType === "dataset") {

  } else if (targetType === "search") {

  } else if (targetType === "attribute" || targetType === "table") {

  } else {

  }
  return data;
},

getNodeFormValue(node) {
  return this.getNodeData(node).id
},


getNodeReactElement(node) {
  let data = this.getNodeData(node);
  return <span title={data.description}>{data.displayName}</span>
},


getNodeChildren(node) {
  return node.children;
},



  render() {
    return (

      <CheckboxTree tree={this.props.tree}
                   selectedList={[]}
                   expandedList={this.props.expandedList}
                   name="SiteMapTree"
                   onSelectedListUpdated={()=>{}}
                   onExpandedListUpdated={this.props.siteMapActions.updateExpanded.bind(this.props.siteMapActions)}
                   onDefaultSelectedListLoaded={()=>{}}
                   onCurrentSelectedListLoaded={()=>{}}
                   getNodeReactElement={this.getNodeReactElement}
                   getNodeFormValue={this.getNodeFormValue}
                   getNodeChildren={this.getNodeChildren}

     />
    );
  }
});

export default wrappable(SiteMap);
