import React,{PropTypes} from 'react';
import TextBox from './TextBox';
import CheckboxTree from './CheckboxTree';


export default class SearchableCheckboxTree extends React.Component {

  constructor(props) {
    super(props);
    this.setSearchText = this.setSearchText.bind(this);
    this.resetSearchText = this.resetSearchText.bind(this);
    this.getSearchBoxReactElement = this.getSearchBoxReactElement.bind(this);
  }

  setSearchText(e) {
    this.props.onSearchTextSet(e.target.value);
  }

  resetSearchText() {
    this.props.onSearchTextReset();
  }

  getSearchBoxReactElement() {
    let searchText = this.props.searchText;
    let isSearchMode = (searchText != undefined || searchText != null) && searchText.length > 0;
    let searchBoxClass = isSearchMode
      ? "wdk-CheckboxTree-searchBoxEnabled" : "wdk-CheckboxTree-searchBoxDisabled";
    return (
      <div className={searchBoxClass}>
        <input type="text" onChange={this.setSearchText} name="search" placeholder="Search Columns" value={this.props.searchText} />
        {isSearchMode ?
          <span onClick={this.resetSearchText}>
            <i className="fa fa-lg fa-times"></i>
          </span>
          : ""
        }
      </div>
    );
  }

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
                     isSearchMode = {this.props.searchText.length > 0}
                     getSearchBox = {this.getSearchBoxReactElement}
                     getBasicNodeReactElement = {this.props.getBasicNodeReactElement}
                     getNodeFormValue = {this.props.getNodeFormValue}
                     getNodeChildren = {this.props.getNodeChildren}
        />
    );
  }
}
