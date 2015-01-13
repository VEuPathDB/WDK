/**
 * Generic table with UI features.
 *
 * Features:
 *   - Sort columns
 *   - Reorder columns
 *   - Show/Hide columns
 *   - Paging
 *
 * NB: A View-Controller will need to pass handlers to this component.
 *
 * Handlers:
 *   - onSort(columnName: string, direction: string(asc|desc))
 *   - onReorder(columnName: string, newPosition: number)
 *   - onShowColumns(columnNames: Array<string>)
 *   - onHideColumns(columnNames: Array<string>)
 *   - onNewPage(offset: number, numRecords: number)
 */

import React from 'react';
import _ from 'lodash';

var noop = () => {};
var PropTypes = React.PropTypes;

var sortClassMap = {
  asc:  'ui-icon ui-icon-arrowthick-1-n',
  desc: 'ui-icon ui-icon-arrowthick-1-s'
};

export default React.createClass({

  propTypes: {
    meta: PropTypes.object.isRequired,
    displayInfo: PropTypes.object.isRequired,
    records: PropTypes.array.isRequired,
    onSort: PropTypes.func,
    onReorder: PropTypes.func,
    onShowColumns: PropTypes.func,
    onHideColumns: PropTypes.func,
    onNewPage: PropTypes.func
  },

  getDefaultProps() {
    return {
      onSort: noop,
      onReorder: noop,
      onShowColumns: noop,
      onHideColumns: noop,
      onNewPage: noop,
      displayInfo: {
        sorting: {},
        pagination: {}
      }
    };
  },

  handleSort(e, name) {
    e.preventDefault();
    this.props.onSort(name);
  },

  handleReorder() {
  },

  handleShowColumns() {
  },

  handleHideColumns(e, columnNames) {
    e.preventDefault();
    this.props.onHideColumns(columnNames);
  },

  handleNewPage() {
  },

  render() {
    var { meta, records, displayInfo: { sorting } } = this.props;

    _.defaults(sorting, {
      columnName: meta.attributes[0],
      direction: 'asc'
    });

    return (
      <div className="wdk-DataTable-Wrapper">
        <table>
          <thead>
            <tr ref="headerRow">
              {_.map(meta.attributes, attribute => {
                var sortClass = sorting.columnName === attribute.name
                  ? sortClassMap[sorting.direction]
                  : 'ui-icon ui-icon-blank';

                var sort = _.partialRight(this.handleSort, attribute.name);
                var remove = _.partialRight(this.handleHideColumns, [attribute.name]);

                return (
                  <th key={attribute.name}
                    className={[attribute.name, attribute.className].join(' ')}
                    title={'Sort table by ' + attribute.displayName}
                    onClick={this.handleSort} >
                    <span className="ui-icon ui-icon-close"
                      style={{float: 'right', zIndex: 1}}
                      title="Remove column"
                      onClick={remove}/>
                    <span>{attribute.displayName}</span>
                    <span className={sortClass} style={{marginRight: '1em'}}/>
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody>
            {records.map(record => {
              // TODO Handle display records inline, which might just be a dump of attrs and tables
              return (
                <tr key={record.id}>
                  {record.attributes.map(attribute => {
                    return <td>{formatAttribute(attribute)}</td>
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

});

// TODO Look up or inject custom formatters
function formatAttribute(attribute) {
  switch(attribute.type) {
    case 'text': return attribute.value
    case 'link': return <a href={attribute.url}>{attribute.display}</a>
  }
}
