import React,{PropTypes} from 'react';
import CheckboxTree from './CheckboxTree';
import SearchableCheckboxTreeSearchBox from './SearchableCheckboxTreeSearchBox';

/**
 * This component wraps the CheckboxTree component and introduces searchable functionality.
 */
export default class SearchableCheckboxTree extends React.Component {

  constructor(props) {
    super(props);
    this.handleSearchTextSet = this.handleSearchTextSet.bind(this);
    this.handleSearchTextReset = this.handleSearchTextReset.bind(this);
    // Partially apply SearchableCheckboxTreeSearchBox props
    this.SearchBoxComponent = props => {
      return (
        <SearchableCheckboxTreeSearchBox
          onSearchTextSet={this.handleSearchTextSet}
          onSearchTextReset={this.handleSearchTextReset}
          searchBoxPlaceholder={this.props.searchBoxPlaceholder}
        />
      );
    };
    this.state = { isSearchMode: false };
  }

  handleSearchTextSet(searchText) {
    this.setState({ isSearchMode: searchText != null && searchText.length > 0 });
    this.props.onSearchTextSet(searchText);
  }

  handleSearchTextReset(searchText) {
    this.setState({ isSearchMode: false });
    this.props.onSearchTextSet(searchText);
  }

  /**
   * The CheckboxTree rendered with appropriate search controls.  The CheckboxTree will work without these
   * search controls present for basic operation.  Search controls are needed to identify that (1) the user
   * is indeed conducting a search and (2) provide the predicate for determining which nodes match in real time.
   * During a search, expand/collapse needs to be disabed and hidden.
   * @returns {XML}
   */
  render() {
    return (
        <CheckboxTree tree = {this.props.tree}
                     removeCheckboxes = {this.props.removeCheckboxes}
                     selectedList = {this.props.selectedList}
                     expandedList = {this.props.expandedList}
                     name = {this.props.name}
                     fieldName = {this.props.fieldName}
                     onSearch = {this.props.onSearch}
                     onSelectedListUpdated = {this.props.onSelectedListUpdated}
                     onExpandedListUpdated = {this.props.onExpandedListUpdated}
                     onDefaultSelectedListLoaded = {this.props.onDefaultSelectedListLoaded}
                     onCurrentSelectedListLoaded = {this.props.onCurrentSelectedListLoaded}
                     isSearchMode = {this.state.isSearchMode}
                     SearchBoxComponent = {this.SearchBoxComponent}
                     getBasicNodeReactElement = {this.props.getBasicNodeReactElement}
                     getNodeFormValue = {this.props.getNodeFormValue}
                     getNodeChildren = {this.props.getNodeChildren}
        />
    );
  }
}


// SearchableCheckboxTree is composed of CheckboxTree and
// SearchableCheckboxTreeSearchBox. It passes its props to those Components,
// it will extend their propTypes.
SearchableCheckboxTree.propTypes = Object.assign({}, CheckboxTree.propTypes, SearchableCheckboxTreeSearchBox.propTypes);
