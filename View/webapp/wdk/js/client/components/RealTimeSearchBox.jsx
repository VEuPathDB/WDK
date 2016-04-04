/**
 * A 'real time' search box.  Changes are throttled by 'debounce' so text
 * change events are delayed to prevent repetitive costly searching.  Useful
 * when expensive operations are performed (e.g. search) in real time as the
 * user types in the box.  Also provides reset button to clear the box.
 */

import { Component, PropTypes } from 'react';
import debounce from 'lodash/function/debounce';
import Tooltip from './Tooltip';

export default class RealTimeSearchBox extends Component {

  constructor(props) {
    super(props);
    this.handleSearchTextChange = this.handleSearchTextChange.bind(this);
    this.handleResetClick = this.handleResetClick.bind(this);
    this.debounceOnSearchTextSet = debounce(this.props.onSearchTextChange, this.props.delayMs);
    this.state = { searchText: this.props.initialSearchText };
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
    this.props.onSearchTextChange('');
  }

  render() {
    let { helpText, placeholderText } = this.props;
    let searchText = this.state.searchText;
    let isActiveSearch = searchText.length > 0;
    let searchBoxClass = isActiveSearch ? "wdk-CheckboxTree-searchBoxEnabled" : "wdk-CheckboxTree-searchBoxDisabled";
    let showHelpIcon = (helpText != null && helpText != '');
    let cancelButtonStyle = { visibility: isActiveSearch ? 'visible' : 'hidden' };
    return (
      <div className="wdk-searchBoxInfo">
        <span className={searchBoxClass}>
          <input type="text"
            onChange={this.handleSearchTextChange}
            placeholder={placeholderText}
            value={searchText}
          />
          <span style={cancelButtonStyle} onClick={this.handleResetClick}>
            <i className="fa fa-lg fa-times"></i>
          </span>
        </span>
        {showHelpIcon &&
          <Tooltip content={helpText}>
            <i className="fa fa-question-circle fa-lg wdk-searchboxInfoIcon"/>
          </Tooltip>
        }
      </div>
    );
  }
}

RealTimeSearchBox.defaultProps = {
  initialSearchText: '',
  onSearchTextChange: () => {},
  placeholderText: '',
  helpText: '',
  delayMs: 250,
}

RealTimeSearchBox.propTypes = {

  /** Initial search text; defaults to ''.  After mounting, search text is maintained by the component */
  initialSearchText: PropTypes.string,

  /** Called when user alters text in the search box  */
  onSearchTextChange: PropTypes.func,

  /** The placeholder string if no search text is present; defaults to ''. */
  placeholderText: PropTypes.string,

  /** Text to appear as tooltip of help icon, should describe how the search is performed. Defaults to empty (no icon) */
  helpText: PropTypes.string,

  /** Delay in milliseconds after last character typed until onSearchTextChange is called.  Defaults to 250. */
  delayMs: PropTypes.number
}

