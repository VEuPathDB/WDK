import { curry, get, keyBy } from 'lodash';
import React from 'react';
import { MesaController as Mesa } from 'mesa';

import { isRange } from './Utils/FilterServiceUtils';
import { Seq } from 'Utils/IterableUtils';
import { makeClassNameHelper } from 'Utils/ComponentUtils';

import StackedBar from './StackedBar';
import { shouldAddFilter } from './Utils';

const cx = makeClassNameHelper('wdk-MultiFieldFilter');

const getCountType = curry((countType, summary, value) =>
  get(summary.valueCounts.find(count => count.value === value), countType, NaN))
const getCount = getCountType('count');
const getFilteredCount = getCountType('filteredCount');
const toPercentage = (num, denom) => Math.round(num / denom * 100)

export default class MultiFieldFilter extends React.Component {

  // Invoke callback with filters array
  handleFieldFilterChange(field, value, includeUnknown, valueCounts) {
    this.props.onFiltersChange(this.updateFilter(this.props.filters, { field, value, includeUnknown, valueCounts }));
  }

  // Returns a new filters array with the provided filter details included
  updateFilter(filters, { field, value, includeUnknown, valueCounts }) {
    let nextFilters = filters.filter(f => f.field !== field.term);
    return shouldAddFilter(field, value, includeUnknown,
      valueCounts, this.props.selectByDefault)
      ? nextFilters.concat({ field: field.term, type: field.type, isRange: isRange(field), value, includeUnknown })
      : nextFilters;
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
    const { values } = this.props.activeField;
    const filtersByField = keyBy(this.props.filters, 'field');

    const rows = Seq.from(this.props.activeFieldSummary)
      .flatMap(summary => [
        {
          summary,
          filter: filtersByField[summary.term]
        },
        ...values.map(value => ({
          summary,
          value,
          filter: filtersByField[summary.term],
          isSelected: get(filtersByField, [ summary.term, 'value' ], []).includes(value)
        }))
      ])
      .toArray();

    return <div className={cx()}>
      <Mesa
        options={{
          useStickyHeader: true,
          tableBodyMaxHeight: '80vh',
          deriveRowClassName: row => cx(
            'Row',
            row.value == null ? 'summary' : 'value',
            row.isSelected && 'selected',
            (row.value == null
              ? row.summary.internalsFilteredCount
              : get(row.summary.valueCounts.find(count => count.value === row.value), 'filteredCount', 0)
            ) > 0 ? 'enabled' : 'disabled'
          )
        }}
        uiState={{
          sort: this.props.activeFieldState.sort
        }}
        eventHandlers={{
          onSort: (column, direction) =>
            this.props.onMemberSort(this.props.activeField, { columnKey: column.key, direction })
        }}
        rows={rows}
        filteredRows={rows}
        columns={[
          {
            key: 'display',
            sortable: true,
            width: '22em',
            name: this.props.activeField.display,
            renderCell: ({ row }) =>
              <div className={cx('ValueContainer')}>
                <div>
                  {row.value == null && this.props.fields.get(row.summary.term).display}
                </div>
                <div>
                  {this.renderRowValue(row)}
                </div>
              </div>
          },
          {
            key: 'internalsFilteredCount',
            className: cx('CountCell'),
            sortable: true,
            width: '11em',
            name: <div>Remaining {this.props.displayName}</div>,
            renderCell: ({ row }) => {
              const filteredCount = row.value == null
                ? row.summary.internalsFilteredCount
                : getFilteredCount(row.summary, row.value);
              return (
                <React.Fragment>
                  <div>
                    {filteredCount.toLocaleString()}
                  </div>
                  <div>
                    <small>({Math.round(filteredCount/(row.summary.internalsCount || this.props.dataCount) * 100)}%)</small>
                  </div>
                </React.Fragment>
              );
            }
          },
          {
            key: 'internalsCount',
            className: cx('CountCell'),
            sortable: true,
            width: '11em',
            name: <div>All {this.props.displayName}</div>,
            renderCell: ({ row }) => {
              const count = row.value == null
                ? row.summary.internalsCount
                : getCount(row.summary, row.value);
              return (
                <React.Fragment>
                  <div>
                    {count.toLocaleString()}
                  </div>
                  <div>
                    <small>({toPercentage(count, row.summary.internalsCount || this.props.dataCount)}%)</small>
                  </div>
                </React.Fragment>
              )
            }
          },
          {
            key: 'distribution',
            name: 'Distribution',
            renderCell: ({ row }) => row.value != null && (
              <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                <StackedBar
                  count={getCount(row.summary, row.value)}
                  filteredCount={getFilteredCount(row.summary, row.value)}
                  populationSize={row.summary.internalsCount || this.props.dataCount}
                />
              </div>
            )
          },
          {
            key: '%',
            width: '4em',
            name: '%',
            renderCell: ({ row }) => row.value != null && (
              <small>
                ({toPercentage(getFilteredCount(row.summary, row.value), getCount(row.summary, row.value))}%)
              </small>
            )
          }
        ]}
      />
    </div>
  }

}
