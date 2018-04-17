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
        return {
          icon: 'table',
          title: 'No Data',
          content: (
            <div>
              <p>Whoops! Either no table data was provided, or the data provided could not be parsed.</p>
            </div>
          )
        };
      default:
        return {
          icon: 'question-circle-o',
          title: 'No Data',
          content: (
            <div>
              <p>There is no data here.</p>
            </div>
          )
        };
    }
  }

  render () {
    let { state } = this.props;
    let { columns, ui } = state;
    let { emptinessCulprit } = ui;
    let colspan = columns.filter(column => !column.hidden).length;
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
