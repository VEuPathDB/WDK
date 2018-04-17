import React from 'react';

import Icon from 'Mesa/Components/Icon';
import { searchByQuery } from 'Mesa/State/Actions';

class TableSearch extends React.PureComponent {
  constructor (props) {
    super(props);
    this.handleQueryChange = this.handleQueryChange.bind(this);
    this.clearSearchQuery = this.clearSearchQuery.bind(this);
  }

  handleQueryChange (e) {
    let { dispatch } = this.props;
    let query = e.target.value;
    dispatch(searchByQuery(query));
  }

  clearSearchQuery () {
    let { dispatch } = this.props;
    dispatch(searchByQuery(null));
  }

  render () {
    let { state } = this.props;
    let { ui, options } = state;
    let { searchQuery } = ui;

    return (
      <div className="TableSearch">
        <Icon fa={'search'} />
        <input
          type="text"
          onChange={this.handleQueryChange}
          value={searchQuery || ''}
          placeholder={options.searchPlaceholder}
        />
        {searchQuery && (
          <button onClick={this.clearSearchQuery}>
            <Icon fa="times-circle" />
            Clear Search
          </button>
        )}
      </div>
    );
  }
};

export default TableSearch;
