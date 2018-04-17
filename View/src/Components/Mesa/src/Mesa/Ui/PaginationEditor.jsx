import React from 'react';

import SelectBox from 'Mesa/Components/SelectBox';
import { setPaginatedItemsPerPage } from 'Mesa/State/Actions';

class PaginationEditor extends React.PureComponent {
  constructor (props) {
    super(props);
    this.handleItemsPerPageChange = this.handleItemsPerPageChange.bind(this);
  }

  handleItemsPerPageChange (itemsPerPage) {
    const { dispatch } = this.props;
    itemsPerPage = parseInt(itemsPerPage);
    dispatch(setPaginatedItemsPerPage(itemsPerPage));
  }

  render () {
    const { pagination } = this.props;
    let options = [ 5, 10, 20, 35, 50, 100 ];

    return (
      <div className="PaginationEditor">
        <span>Rows per page: </span>
        <SelectBox
          options={options}
          selected={pagination.itemsPerPage}
          onChange={this.handleItemsPerPageChange}
        />
      </div>
    );
  }
};

export default PaginationEditor;
