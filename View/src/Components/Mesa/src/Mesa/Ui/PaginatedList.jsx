import React from 'react';

import PaginationUtils from 'Mesa/Utils/PaginationUtils';

class PaginatedList extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    const { container, list, pagination, renderItem } = this.props;
    const Container = container || 'div';
    const currentPage = PaginationUtils.getCurrentPage(list, pagination);
    const content = Array.isArray(currentPage) ? currentPage.map(renderItem) : null;

    return (
      <Container className="PaginatedList">
        {content}
      </Container>
    );
  }
};

export default PaginatedList;
