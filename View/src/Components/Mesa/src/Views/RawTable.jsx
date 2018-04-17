import React from 'react';

import Mesa from 'Mesa/Mesa';
import TableData from 'Content/TableData';


class RawTable extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <div className="RawTable">
        <grid-fluid>
          <box className="box-xs-12">
            <Mesa rows={TableData} />
          </box>
        </grid-fluid>
      </div>
    );
  }
};

export default RawTable;
