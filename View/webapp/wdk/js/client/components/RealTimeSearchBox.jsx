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
    this.handleSearchTermChange = this.handleSearchTermChange.bind(this);
    this.handleResetClick = this.handleResetClick.bind(this);
    this.debounceOnSearchTermSet = debounce(this.props.onSearchTermChange, this.props.delayMs);
    this.state = { searchTerm: this.props.initialSearchTerm };
  }

  /**
   * Update the state of this Component, and call debounced onSearchTermSet
   * callback.
   */
  handleSearchTermChange(e) {
    let searchTerm = e.target.value;
    this.setState({ searchTerm });
    this.debounceOnSearchTermSet(searchTerm);
  }

  /**
   * Update the state of this Component, and call onSearchTermSet callback
   * immediately.
   */
  handleResetClick(e) {
    e.preventDefault();
    this.setState({ searchTerm: '' });
    this.props.onSearchTermChange('');
  }

  render() {
    let { helpText, placeholderText } = this.props;
    let searchTerm = this.state.searchTerm;
    let isActiveSearch = searchTerm.length > 0;
    let searchBoxClass = isActiveSearch ? "wdk-CheckboxTree-searchBoxEnabled" : "wdk-CheckboxTree-searchBoxDisabled";
    let showHelpIcon = (helpText != null && helpText != '');
    let cancelButtonStyle = { visibility: isActiveSearch ? 'visible' : 'hidden' };
    return (
      <div className="wdk-searchBoxInfo">
        <span className={searchBoxClass}>
          <input type="text"
            onChange={this.handleSearchTermChange}
            placeholder={placeholderText}
            value={searchTerm}
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
  initialSearchTerm: '',
  onSearchTermChange: () => {},
  placeholderText: '',
  helpText: '',
  delayMs: 250,
}

RealTimeSearchBox.propTypes = {

  /** Initial search text; defaults to ''.  After mounting, search text is maintained by the component */
  initialSearchTerm: PropTypes.string,

  /** Called when user alters text in the search box  */
  onSearchTermChange: PropTypes.func,

  /** The placeholder string if no search text is present; defaults to ''. */
  placeholderText: PropTypes.string,

  /** Text to appear as tooltip of help icon, should describe how the search is performed. Defaults to empty (no icon) */
  helpText: PropTypes.string,

  /** Delay in milliseconds after last character typed until onSearchTermChange is called.  Defaults to 250. */
  delayMs: PropTypes.number
}

