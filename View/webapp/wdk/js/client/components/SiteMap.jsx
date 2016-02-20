import React from 'react';
import { wrappable } from '../utils/componentUtils';
import CheckboxTree from './CheckboxTree';
import { getTargetType, getRefName, getDisplayName, getDescription, getId, getPropertyValue } from '../utils/OntologyUtils';

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
  data.targetType = getTargetType(node);
  data.siteMapSpecial = getPropertyValue('SiteMapSpecial', node)
  if (node.wdkReference) {
    let tt = data.targetType === "search"? "" : " (" + data.targetType + ")";
    data.displayName = node.wdkReference.displayName + tt;
    data.description = node.wdkReference.description;
  } else if (data.targetType === "track"){
    data.displayName = getPropertyValue('name', node);
  } else if (data.targetType === "dataset"){
    data.displayName = data.targetType + ": " + getDisplayName(node);
  } else {
    data.displayName = getDisplayName(node);
    data.description = getDescription(node);
  }
  return data;
},

getNodeFormValue(node) {
  return this.getNodeData(node).id
},


getNodeReactElement(node) {
  let data = this.getNodeData(node);
  if (data.siteMapSpecial) return <span title={data.description}><em>{data.displayName}</em></span>
  if (!data.targetType) return <span title={data.description}><strong>{data.displayName}</strong></span>
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
