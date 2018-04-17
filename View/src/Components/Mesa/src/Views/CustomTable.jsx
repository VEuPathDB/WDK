import React from 'react';

import Mesa from 'Mesa/Mesa';
import TableData from 'Content/TableData';
import TableColumns from 'Content/TableColumns';


class CustomTable extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <div className="CustomTable">
        <grid-fluid>
          <box className="box-xs-12">
            <Mesa rows={TableData} columns={TableColumns} />
          </box>
        </grid-fluid>
      </div>
    );
  }
};

export default CustomTable;
