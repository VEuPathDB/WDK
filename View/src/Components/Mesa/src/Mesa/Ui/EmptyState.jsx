import React from 'react';

import Icon from 'Mesa/Components/Icon';
import { disableAllColumnFilters } from 'Mesa/State/Actions';

class EmptyState extends React.PureComponent {
  constructor (props) {
    super(props);
    this.getCulprit = this.getCulprit.bind(this);
  }

  getCulprit () {
    const { state, dispatch } = this.props;
    const { emptinessCulprit, searchQuery } = state.ui;
    switch (emptinessCulprit) {
      case 'search':
        return {
          icon: 'search',
          title: 'No Results',
          content: (
            <div>
              <p>Sorry, "{searchQuery}" returned no results.</p>
            </div>
          )
        };
      case 'nocolumns':
        return {
          icon: 'columns',
          title: 'No Columns Shown',
          content: (
            <div>
              <p>Whoops, looks like you've hidden all columns. Use the column editor to show some columns.</p>
            </div>
          )
        };
      case 'filters':
        return {
          icon: 'filter',
          title: 'No Filter Results',
          content: (
            <div>
              <p>No rows exist that match all of your column filter settings.</p>
              <button onClick={() => dispatch(disableAllColumnFilters())}>
                Disable All Filters <Icon fa={'times-rectangle'} />
              </button>
            </div>
          )
        };
      case 'nodata':
      default:
        return {
          icon: 'table',
          title: 'No Data',
          content: (
            <div>
              <p>Whoops! Either no table data was provided, or the data provided could not be parsed.</p>
            </div>
          )
        };
    }
  }

  render () {
    let { state } = this.props;
    let { columns, ui, actions } = state;
    let { emptinessCulprit } = ui;
    let colspan = columns.filter(column => !column.hidden).length + (actions.length ? 1 : 0);
    let culprit = this.getCulprit();

    return (
      <tr className="EmptyState">
        <td colSpan={colspan}>
          <div className="EmptyState-Body-Wrapper">
            <div className="EmptyState-Body">
              <Icon fa={culprit.icon} className="EmptyState-Icon" />
              <h2>{culprit.title}</h2>
              {culprit.content}
            </div>
          </div>
        </td>
      </tr>
    )
  }
};

export default EmptyState;
