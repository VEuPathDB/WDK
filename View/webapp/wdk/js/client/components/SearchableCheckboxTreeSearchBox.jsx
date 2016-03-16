import React,{PropTypes} from 'react';
import debounce from 'lodash/function/debounce';
/**
 * The search box React Component injected into the CheckboxTree
 */
export default class SearchableCheckboxTreeSearchBox extends React.Component {

  constructor(props) {
    super(props);
    this.handleSearchTextChange = this.handleSearchTextChange.bind(this);
    this.handleResetClick = this.handleResetClick.bind(this);
    this.debounceOnSearchTextSet = debounce(this.props.onSearchTextSet, 250);
    this.state = { searchText: '' };
  }

  /**
   * Update the state of this Component, and call debounced onSearchTextSet
   * callback.
   */
  handleSearchTextChange(e) {
    let searchText = e.target.value;
    this.setState({ searchText });
    this.debounceOnSearchTextSet(searchText);
  }

  /**
   * Update the state of this Component, and call onSearchTextSet callback
   * immediately.
   */
  handleResetClick(e) {
    e.preventDefault();
    this.setState({ searchText: '' });
    this.props.onSearchTextSet('');
  }

  render() {
    let searchText = this.state.searchText;
    let isSearchMode = searchText != null && searchText.length > 0;
    let searchBoxClass = isSearchMode
      ? "wdk-CheckboxTree-searchBoxEnabled" : "wdk-CheckboxTree-searchBoxDisabled";
    return (
      <div className={searchBoxClass}>
        <input type="text"
          onChange={this.handleSearchTextChange}
          placeholder={this.props.searchBoxPlaceholder || ""}
          value={searchText}
        />
        {isSearchMode ?
          <span onClick={this.handleResetClick}>
            <i className="fa fa-lg fa-times"></i>
          </span>
          : ""
        }
      </div>
    );
  }

}

SearchableCheckboxTreeSearchBox.propTypes = {

  /** Called when user alters text in the search box  */
  onSearchTextSet: PropTypes.func,

  /**
   *  Called when user clicks close button in the search box.  Empties the search box of text, restores
   *  normal operation, and applies business rules to intial expansion of tree.
   */
  onSearchTextReset: PropTypes.func,

  /**
   * The placeholder string to use in the search box.  If no string is provided the search
   * box have no placeholder text.
   */
  searchBoxPlaceholder: PropTypes.string

}

