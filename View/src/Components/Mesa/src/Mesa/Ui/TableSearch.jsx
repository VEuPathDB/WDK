import React from 'react';

import Icon from 'Mesa/Components/Icon';
import { searchByQuery } from 'Mesa/State/Actions';

class TableSearch extends React.PureComponent {
  constructor (props) {
    super(props);
    this.handleQueryChange = this.handleQueryChange.bind(this);
  }

  handleQueryChange (e) {
    let { dispatch } = this.props;
    let query = e.target.value;
    dispatch(searchByQuery(query));
  }

  render () {
    let { state } = this.props;
    let { ui, options } = state;

    return (
      <div className="TableSearch">
        <Icon fa={'search'} />
        <input
          type="text"
          onChange={this.handleQueryChange}
          value={ui.searchQuery || ''}
          placeholder={options.searchPlaceholder}
        />
      </div>
    );
  }
};

export default TableSearch;
