/**
 * "properties": {
 *   "scope": [
 *     "download"
 *   ],
 *   "recordClassName": [
 *     "OrganismRecordClasses.OrganismRecordClass"
 *   ],
 *   "name": [
 *     "is_reference_strain"
 *   ],
 *   "label": [
 *     "OrganismRecordClasses.OrganismRecordClass.is_reference_strain"
 *   ],
 *   "targetType": [
 *     "attribute"
 *   ]
 * },
 *
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

import { wrappable } from '../utils/componentUtils';
import { Link } from 'react-router';
import CheckboxTree from './CheckboxTree';
import { getNodeChildren, getPropertyValue } from '../utils/OntologyUtils';
import { getTargetType, getRefName, getDisplayName, getDescription, getNodeId, getId, getAggregateSearchText } from '../utils/CategoryUtils';

/**
 * Displays site map page, basically just a custom expandable tree
 */
let SiteMap = props => {
  let treeProps = {
    tree: props.tree,
    getNodeId: getNodeId,
    getNodeChildren: getNodeChildren,
    showRoot: false,
    nodeComponent: SiteMapNodeElement,
    expandedList: props.expandedList,
    onExpansionChange: props.siteMapActions.updateExpanded,
    isSelectable: false,
    isSearchable: true,
    showSearchBox: true,
    searchBoxPlaceholder: "Search for data...",
    searchBoxHelp: "Each item's name and description will be searched for your exact input text",
    searchText: props.searchText,
    onSearchTextChange: props.siteMapActions.setSearchText,
    searchPredicate: siteMapSearchPredicate
  };
  return <CheckboxTree {...treeProps} />;
};

/**
 * Collects relevant data from the node, used by the search predicate and the
 * display component.
 */
let getNodeData = node => {
  let data = {};
  data.id = getId(node);
  data.targetType = getTargetType(node);
  data.siteMapSpecial = getPropertyValue('SiteMapSpecial', node);
  data.ontologyParent = getPropertyValue('ontologyParent', node);
  data.recordClassDisplayName = getPropertyValue('recordClassDisplayName', node);
  data.name = getRefName(node);
  if (node.wdkReference) {
    let tt = data.targetType === "search"? "" : " (" + data.targetType + ")";
    data.displayName = node.wdkReference.displayName + tt;
    data.description = node.wdkReference.description;
  }
  else if (data.targetType === "track"){
    data.displayName = getPropertyValue('name', node);
  }
  else if (data.targetType === "dataset"){
    data.displayName = data.targetType + ": " + getDisplayName(node);
  }
  else {
    data.displayName = getDisplayName(node);
    data.description = getDescription(node);
  }
  return data;
}

/**
 * Defines how to search for site-map nodes
 */
let siteMapSearchPredicate = (node, searchText) => {
  let data = getNodeData(node);
  let searchableText = getAggregateSearchText([ data.recordClassDisplayName, data.displayName, data.description ]);
  return (searchableText.indexOf(searchText.toLowerCase()) !== -1);
}

/**
 * Defines how to display site-map nodes
 */
let SiteMapNodeElement = ({ node }) => {
  let data = getNodeData(node);

  if (data.targetType === 'search') {
    return (
      <a href={"../showQuestion.do?questionFullName=" + data.name}>
        <span title={data.description}><em>{data.recordClassDisplayName} by {data.displayName}</em></span>
      </a>
    );
  }
  if (data.siteMapSpecial) {
    if (data.displayName.match(/ Page$/)) {
      return (
        <Link to={'/record/gene/PF3D7_1133400#' + data.ontologyParent}>
          <span title={data.description}>{data.displayName}</span>
        </Link>
      );
    }
    return ( <span title={data.description}><em>{data.displayName}</em></span> );
  }

  if (!data.targetType) {
    return ( <span title={data.description}><strong>{data.displayName}</strong></span> );
  }
  
  return ( <span title={data.description}>{data.displayName}</span> );
}

export default wrappable(SiteMap);
