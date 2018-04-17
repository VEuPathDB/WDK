import React from 'react';

import Defaults from 'Mesa/Defaults';
import Icon from 'Mesa/Components/Icon';
import Store from 'Mesa/State/Store';
import { searchByQuery } from 'Mesa/State/Actions';

class TableSearch extends React.Component {
  constructor (props) {
    super(props);
    let { searchQuery } = Store.getState();
    this.state = { searchQuery };

    this.unsubscribe = Store.subscribe(() => {
      let state = Store.getState();
      if (state.searchQuery !== this.state.searchQuery) {
        this.setState({ searchQuery: state.searchQuery });
      };
    });
  }

  componentWillUnmount () {
    this.unsubscribe();
  }

  handleQueryChange (e) {
    let value = e.target.value;
    Store.dispatch(searchByQuery(value));
  }

  render () {
    let { searchQuery } = this.state;
    return (
      <div className="TableSearch">
        <Icon fa={'search'} />
        <input
          type="text"
          onChange={this.handleQueryChange}
          value={searchQuery || ''}
          placeholder={Defaults.searchPlaceholder}
        />
      </div>
    );
  }
};

export default TableSearch;
