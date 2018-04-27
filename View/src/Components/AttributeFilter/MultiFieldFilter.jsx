import { bindAll, curry, escapeRegExp, get, keyBy } from 'lodash';
import { MesaController as Mesa } from 'Components/Mesa';
import React from 'react';

import Icon from 'Components/Icon/IconAlt';
import RealTimeSearchBox from 'Components/SearchBox/RealTimeSearchBox';
import { makeClassNameHelper } from 'Utils/ComponentUtils';
import { Seq } from 'Utils/IterableUtils';

import StackedBar from './StackedBar';
import { shouldAddFilter, isRange } from './Utils';

const cx = makeClassNameHelper('wdk-MultiFieldFilter');

const getCountType = curry((countType, summary, value) =>
  get(summary.valueCounts.find(count => count.value === value), countType, NaN))
const getCount = getCountType('count');
const getFilteredCount = getCountType('filteredCount');
const toPercentage = (num, denom) => Math.round(num / denom * 100)

export default class MultiFieldFilter extends React.Component {

  constructor(props) {
    super(props);
    bindAll(this, [
      'deriveRowClassName',
      'handleTableSort',
      'renderDisplayHeadingName',
      'renderDisplayHeadingSearch',
      'renderDisplayCell',
      'renderCountCell',
      'renderDistributionCell',
      'renderPercentCell'
    ]);
  }

  // Event handlers

  // Invoke callback with filters array
  handleFieldFilterChange(field, value, includeUnknown, valueCounts) {
    this.props.onFiltersChange(this.updateFilter(this.props.filters, { field, value, includeUnknown, valueCounts }));
  }

  handleTableSort(column, direction) {
    this.props.onMemberSort(this.props.activeField, { columnKey: column.key, direction });
  }

  // Returns a new filters array with the provided filter details included
  updateFilter(filters, { field, value, includeUnknown, valueCounts }) {
    const filter = { field: field.term, type: field.type, isRange: isRange(field), value, includeUnknown };
    const nextFilters = filters.filter(f => f.field !== field.term);
    return shouldAddFilter(filter, valueCounts, this.props.selectByDefault)
      ? nextFilters.concat(filter)
      : nextFilters;
  }

  deriveRowClassName(row) {
    return cx(
      'Row',
      row.value == null ? 'summary' : 'value',
      row.isSelected && 'selected',
      row.isLast && 'last-value',
      (row.value == null
        ? row.summary.internalsFilteredCount
        : get(row.summary.valueCounts.find(count => count.value === row.value), 'filteredCount', 0)
      ) > 0 ? 'enabled' : 'disabled'
    );
  }

  renderDisplayHeadingName() {
    return this.props.activeField.display;
  }

  renderDisplayHeadingSearch() {
    return (
      <div
        style={{
          width: '15em',
          fontSize: '.8em',
          fontWeight: 'normal',
        }}
        onMouseUp={event => {
          event.stopPropagation();
        }}
      >
        <RealTimeSearchBox
          searchTerm={this.props.activeFieldState.searchTerm}
          placeholderText="Find items"
          onSearchTermChange={searchTerm => this.props.onMemberSearch(this.props.activeField, searchTerm)}
        />
      </div>
    )
  }

  renderDisplayCell({ row }) {
    return (
      <div className={cx('ValueContainer')}>
        <div>
          {row.value == null && this.props.fields.get(row.summary.term).display}
        </div>
        <div>
          {this.renderRowValue(row)}
        </div>
      </div>
    )
  }

  renderCountCell({ key, row }) {
    const count = row.value == null
      ? ( key === 'count' ? row.summary.internalsCount : row.summary.internalsFilteredCount )
      : ( key === 'count' ? getCount(row.summary, row.value) : getFilteredCount(row.summary, row.value) );
    return (
      <React.Fragment>
        <div>
          {count.toLocaleString()}
        </div>
        <div>
          <small>({toPercentage(count, row.summary.internalsCount || this.props.dataCount)}%)</small>
        </div>
      </React.Fragment>
    );
  }

  renderDistributionCell({ row }) {
    const unknownCount = this.props.dataCount - row.summary.internalsCount;
    return row.value != null
      ? (
        <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <StackedBar
            count={getCount(row.summary, row.value)}
            filteredCount={getFilteredCount(row.summary, row.value)}
            populationSize={row.summary.internalsCount || this.props.dataCount}
          />
        </div>
      )
      : unknownCount > 0 && (
        <div style={{ fontWeight: 300 }}>
          <b>{unknownCount}</b> {this.props.displayName} have no data
        </div>
      )
  }

  renderPercentCell({ row }) {
    return row.value != null && (
      <small>
        ({toPercentage(getFilteredCount(row.summary, row.value), getCount(row.summary, row.value))}%)
      </small>
    )
  }

  renderRowValue(row) {
    const { value, filter, summary, isSelected } = row;
    if (value == null) return null;
    const filterValue = get(filter, 'value', []);
    const fieldValues = summary.valueCounts.map(count => count.value);
    const isDisabled = !fieldValues.includes(value) || getFilteredCount(summary, value) <= 0;
    const handleChange = event =>
      this.handleFieldFilterChange(
        this.props.fields.get(summary.term),
        ( event.target.checked
          ? [value].concat(filterValue)
          : filterValue.filter(item => item !== value)
        ),
        false,
        summary.valueCounts
      );
    return (
      <label>
        <input
          type="checkbox"
          disabled={isDisabled}
          checked={isSelected && !isDisabled}
          onChange={handleChange}
        /> {value}
      </label>
    )
  }

  render() {
    const {
      values = Seq.from(this.props.activeFieldState.summary)
        .flatMap(summary => summary.valueCounts)
        .map(count => count.value)
        .uniq()
        .toArray()
    } = this.props.activeField;
    const { searchTerm = '' } = this.props.activeFieldState;
    const searchRe = new RegExp(escapeRegExp(searchTerm), 'i');
    const filtersByField = keyBy(this.props.filters, 'field');

    const rows = Seq.from(this.props.activeFieldState.summary)
      .flatMap(summary => [
        {
          summary,
          filter: filtersByField[summary.term]
        },
        ...values.map((value, index) => ({
          summary,
          value,
          filter: filtersByField[summary.term],
          isSelected: get(filtersByField, [ summary.term, 'value' ], []).includes(value),
          isLast: index === values.length - 1
        }))
      ])

    const filteredRows = rows
      .filter(({ summary }) =>
        pathToTerm(summary.term, this.props.activeField.term, this.props.fields)
          .some(item => searchRe.test(item.display)))

    return <div className={cx()}>
      <button
        type="button"
        className={cx('UpdateCountsButton') + " btn"}
        disabled={!this.props.activeFieldState.invalid || this.props.activeFieldState.loading}
        onClick={() => this.props.onFieldCountUpdateRequest(this.props.activeField.term)}
      >
        {this.props.activeFieldState.loading
          ? <div><Icon fa="circle-o-notch" className="fa-spin"/> Loading...</div>
          : 'Update counts'}
      </button>
      <Mesa
        options={{
          useStickyHeader: true,
          tableBodyMaxHeight: '80vh',
          deriveRowClassName: this.deriveRowClassName
        }}
        uiState={{
          sort: this.props.activeFieldState.sort
        }}
        eventHandlers={{
          onSort: this.handleTableSort
        }}
        rows={rows.toArray()}
        filteredRows={filteredRows.toArray()}
        columns={[
          {
            key: 'display',
            sortable: true,
            width: '22em',
            wrapCustomHeadings: ({ headingRowIndex }) => headingRowIndex === 0,
            renderHeading: [ this.renderDisplayHeadingName, this.renderDisplayHeadingSearch ],
            renderCell: this.renderDisplayCell
          },
          {
            key: 'filteredCount',
            className: cx('CountCell'),
            sortable: true,
            width: '11em',
            name: <div>Remaining {this.props.displayName}</div>,
            renderCell: this.renderCountCell
          },
          {
            key: 'count',
            className: cx('CountCell'),
            sortable: true,
            width: '11em',
            name: <div>All {this.props.displayName}</div>,
            renderCell: this.renderCountCell
          },
          {
            key: 'distribution',
            name: 'Distribution',
            renderCell: this.renderDistributionCell
          },
          {
            key: '%',
            width: '4em',
            name: '%',
            renderCell: this.renderPercentCell
          }
        ]}
      />
    </div>
  }

}

function pathToTerm(leafTerm, rootTerm, fields) {
  const item = fields.get(leafTerm);
  const root = fields.get(rootTerm);
  return item == null || item === root
    ? Seq.empty()
    : pathToTerm(item.parent, rootTerm, fields).concat(Seq.of(item));
}
