import React,{PropTypes} from 'react';
import TextBox from './TextBox';
import CheckboxTree from './CheckboxTree';
import throttle from 'lodash/function/throttle';

/**
 * This component wraps the CheckboxTree component and introduces searchable functionality.
 */
export default class SearchableCheckboxTree extends React.Component {

  constructor(props) {
    super(props);
    this.setSearchText = this.setSearchText.bind(this);
    // Throttle used to reduce 'jump' while user types in a search criterion
    this.setSearchTextUpdate = throttle(this.setSearchTextUpdate.bind(this),500, {leading: false});
    this.resetSearchText = this.resetSearchText.bind(this);
    this.getSearchBoxReactElement = this.getSearchBoxReactElement.bind(this);
    this.state = {searchText: ""};
  }

  /**
   * Responds to user altering text.  This input has to maintain state so that we can apply a throttle, echo the
   * characters immediately but still allow the text to be reset by a 'close' button
   * @param e - event
   */
  setSearchText(e) {
    this.setState({searchText : e.target.value});
    this.setSearchTextUpdate();
  }

  /**
   * Throttled function that sends the search textbox value back via the callback
   */
  setSearchTextUpdate() {
    this.props.onSearchTextSet(this.state.searchText);
  }

  /**
   * Responds to user clicking 'close' button in search text box.  Causes the text box to empty.  In so
   * doing, nominal (non-search) behavior is restored.
   */
  resetSearchText() {
    this.setState({searchText : ""});
    this.props.onSearchTextReset();
  }

  /**
   * The search box reach component injected into the CheckboxTree
   * @returns {XML}
   */
  getSearchBoxReactElement() {
    let searchText = this.state.searchText;
    let isSearchMode = (searchText != undefined || searchText != null) && searchText.length > 0;
    let searchBoxClass = isSearchMode
      ? "wdk-CheckboxTree-searchBoxEnabled" : "wdk-CheckboxTree-searchBoxDisabled";
    return (
      <div className={searchBoxClass}>
        <input type="text" onChange={this.setSearchText} name="search" placeholder="Search Columns" value={searchText}  />
        {isSearchMode ?
          <span onClick={this.resetSearchText}>
            <i className="fa fa-lg fa-times"></i>
          </span>
          : ""
        }
      </div>
    );
  }

  /**
   * The CheckboxTree rendered with appropriate search controls.  The CheckboxTree will work without these
   * search controls present for basic operation.
   * @returns {XML}
   */
  render() {
    return (
        <CheckboxTree tree = {this.props.tree}
                     selectedList = {this.props.selectedList}
                     expandedList = {this.props.expandedList}
                     name = {this.props.name}
                     fieldName = {this.props.fieldName}
                     onSearch = {this.props.onSearch}
                     onSelectedListUpdated = {this.props.onSelectedListUpdated}
                     onExpandedListUpdated = {this.props.onExpandedListUpdated}
                     onDefaultSelectedListLoaded = {this.props.onDefaultSelectedListLoaded}
                     onCurrentSelectedListLoaded = {this.props.onCurrentSelectedListLoaded}
                     isSearchMode = {this.state.searchText.length > 0}
                     getSearchBox = {this.getSearchBoxReactElement}
                     getBasicNodeReactElement = {this.props.getBasicNodeReactElement}
                     getNodeFormValue = {this.props.getNodeFormValue}
                     getNodeChildren = {this.props.getNodeChildren}
        />
    );
  }
}


SearchableCheckboxTree.propTypes = {

  /** Value to use for name of checkbox tree - used as id of enclosing div  */
  name: PropTypes.string,

  /** Array representing top level nodes in the checkbox tree **/
  tree: PropTypes.array.isRequired,

  /** Value to use for the name of the checkboxes in the tree */
  fieldName: PropTypes.string,

  /** List of selected nodes as represented by their ids. */
  selectedList: PropTypes.array,

  /** List of expanded nodes as represented by their ids. */
  expandedList: PropTypes.array,

  /**
   * Called when the set of selected (leaf) nodes changes.
   * The function will be called with the array of the selected node
   * ids.
   */
  onSelectedListUpdated: PropTypes.func,

  /**
   * Called when the set of expanded (branch) nodes changes.
   * The function will be called with the array of the expanded node
   * ids.
   */
  onExpandedListUpdated: PropTypes.func,

  /**
   * Called when the user reverts to the default select list.
   * The function will be called with no arguments as the default
   * state is maintained by the store.
   */
  onDefaultSelectedListLoaded: PropTypes.func,

  /**
   * Called when the user reverts to the select list with which he/she started.
   * The function will be called with no arguments as the original
   * state is maintained by the store.
   */
  onCurrentSelectedListLoaded: PropTypes.func,

  /** Called during rendering to create the react element holding the display name and tooltip for the current node */
  getBasicNodeReactElement: PropTypes.func,

  /** Called during rendering to provide the input value for the current node */
  getNodeFormValue: PropTypes.func,

  /** Called during rendering to provide the children for the current node */
  getNodeChildren:  PropTypes.func,

  /** Called during rendering to identify whether a given node should be made visible based upon whether it or
   *  its ancestors match the search criteria.
   */
  onSearch: PropTypes.func,

  /** Called when user alters text in the search box  */
  onSearchTextSet: PropTypes.func,

  /**
   *  Called when user clicks close button in the search box.  Empties the search box of text, restores
   *  normal operation, and applies business rules to intial expansion of tree.
   */
  onSearchTextReset: PropTypes.func

}