import { wrappable } from '../utils/componentUtils';
import { getNodeChildren } from '../utils/OntologyUtils';
import { getNodeId, nodeSearchPredicate, BasicNodeComponent } from '../utils/CategoryUtils';
import CheckboxTree from './CheckboxTree';

let CategoriesCheckboxTree = props => {

  let { title, searchBoxPlaceholder, tree, selectedLeaves, expandedBranches,
    isMultiPick, searchTerm, onChange, onUiChange, onSearchTermChange } = props;

  if (isMultiPick === undefined) isMultiPick = true;

  if (tree.children.length == 0) {
    return ( <noscript/> );
  }

  let treeProps = {

    // set help
    searchBoxHelp: "Each column name will be searched. The column names will contain all your terms. Your terms are partially matched; for example, the term typ will match typically, type, atypical.",

    // set hard-coded values for searchable, selectable, expandable tree
    isSearchable: true, isSelectable: true,

    // set values from category utils since we know tree is a category tree
    getNodeId, getNodeChildren, searchPredicate: nodeSearchPredicate, nodeComponent: BasicNodeComponent,

    // set current data in the tree
    tree, isMultiPick, selectedList: selectedLeaves, expandedList: expandedBranches, searchBoxPlaceholder, searchTerm,

    // set event handlers
    onSelectionChange: onChange, onExpansionChange: onUiChange, onSearchTermChange
  };

  return (
    <div className="wdk-CategoriesCheckboxTree">
      <h3 className="wdk-CategoriesCheckboxTreeHeading">{title}</h3>
      <div className="wdk-CategoriesCheckboxTreeWrapper">
        <CheckboxTree {...treeProps} />
      </div>
    </div>
  );
};

export default wrappable(CategoriesCheckboxTree);
