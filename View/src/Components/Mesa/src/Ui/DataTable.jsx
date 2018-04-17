import React from 'react';
import PropTypes from 'prop-types';

import HeadingRow from '../Ui/HeadingRow';
import DataRowList from '../Ui/DataRowList';

class DataTable extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { rows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, options, columns, actions, uiState, eventHandlers };

    return (
      <div className="Mesa">
        <div className="DataTable">
          <table cellSpacing="0" cellPadding="0">
            <tbody>
              <HeadingRow {...props} />
            </tbody>
            <DataRowList {...props} />
          </table>
        </div>
      </div>
    );
  }
};

DataTable.propTypes = {
  rows: PropTypes.array,
  columns: PropTypes.array,
  options: PropTypes.object,
  actions: PropTypes.arrayOf(PropTypes.shape({
    element: PropTypes.oneOfType([ PropTypes.func, PropTypes.node, PropTypes.element ]),
    handler: PropTypes.func,
    callback: PropTypes.func
  })),
  uiState: PropTypes.object,
  eventHandlers: PropTypes.objectOf(PropTypes.func)
};

export default DataTable;
