import React from 'react';

import PaginationUtils from '../Utils/PaginationUtils';

class PaginatedList extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    const { container, list, paginationState, renderItem } = this.props;
    const Container = container || 'div';
    const currentPage = PaginationUtils.getCurrentPage(list, paginationState);
    const content = Array.isArray(currentPage) ? currentPage.map(renderItem) : null;

    return (
      <Container className="PaginatedList">
        {content}
      </Container>
    );
  }
};

export default PaginatedList;
