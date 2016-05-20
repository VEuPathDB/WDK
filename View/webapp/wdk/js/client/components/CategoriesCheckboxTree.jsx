import {PropTypes} from 'react';
import { wrappable } from '../utils/componentUtils';
import { getNodeChildren } from '../utils/OntologyUtils';
import { getNodeId, nodeSearchPredicate, BasicNodeComponent } from '../utils/CategoryUtils';
import CheckboxTree from './CheckboxTree';

let CategoriesCheckboxTree = props => {

  let { title, searchBoxPlaceholder, tree, selectedLeaves, expandedBranches, nodeComponent,
    isMultiPick, searchTerm, onChange, onUiChange, onSearchTermChange, isSelectable, leafType } = props;

  if (tree.children.length == 0) {
    return ( <noscript/> );
  }

  let treeProps = {

    // set help
    searchBoxHelp: `Each ${leafType} name will be searched. The ${leafType} names will contain all your terms. Your terms are partially matched; for example, the term typ will match typically, type, atypical.`,

    // set hard-coded values for searchable, selectable, expandable tree
    isSearchable: true, isSelectable,

    // set values from category utils since we know tree is a category tree
    getNodeId, getNodeChildren, searchPredicate: nodeSearchPredicate, nodeComponent,

    // set current data in the tree
    tree, isMultiPick, selectedList: selectedLeaves, expandedList: expandedBranches, searchBoxPlaceholder, searchTerm,

    // set event handlers
    onSelectionChange: onChange, onExpansionChange: onUiChange, onSearchTermChange
  };

  return (
    <div className="wdk-CategoriesCheckboxTree">
      {title && <h3 className="wdk-CategoriesCheckboxTreeHeading">{title}</h3>}
      <div className="wdk-CategoriesCheckboxTreeWrapper">
        <CheckboxTree {...treeProps} />
      </div>
    </div>
  );
};

CategoriesCheckboxTree.propTypes = {
  title: PropTypes.string,
  searchBoxPlaceholder: PropTypes.string,
  tree: CheckboxTree.propTypes.tree,
  /** String name representing what is being searched */
  leafType: PropTypes.string.isRequired,
  selectedLeaves: PropTypes.array,
  expandedBranches: PropTypes.array,
  nodeComponent: PropTypes.func,
  isMultiPick: PropTypes.bool,
  searchTerm: PropTypes.string,
  onChange: PropTypes.func,
  onUiChange: PropTypes.func,
  onSearchTermChange: PropTypes.func,
  isSelectable: PropTypes.bool
};
CategoriesCheckboxTree.defaultProps = {
  nodeComponent: BasicNodeComponent,
  isMultiPick: true,
  isSelectable: true,
  leafType: 'column' // remove once all consumers are passing in a value for this
}

export default wrappable(CategoriesCheckboxTree);
