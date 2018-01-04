import React from 'react';
import { get, memoize } from 'lodash';
import { MesaController as Mesa, ModalBoundary } from 'mesa';
import 'mesa/dist/css/mesa.css';

import { safeHtml } from '../../../utils/componentUtils';
import Toggle from '../../Toggle';
import FieldFilter from './FieldFilter';
import FilterLegend from './FilterLegend';

const UNKNOWN_ELEMENT = <em>Not specified</em>;

/**
 * Membership field component
 */
export default class MembershipField extends React.Component {
  static getHelpContent(props) {
    var displayName = props.displayName;
    var fieldDisplay = props.field.display;
    return (
      <div>
        You may add or remove {displayName} with specific {fieldDisplay} values
        from your overall selection by checking or unchecking the corresponding
        checkboxes.
      </div>
    );
  }

  constructor(props) {
    super(props);
    this.handleItemClick = this.handleItemClick.bind(this);
    this.handleSelectAll = this.handleSelectAll.bind(this);
    this.handleRemoveAll = this.handleRemoveAll.bind(this);
    this.toFilterValue = this.toFilterValue.bind(this);
    this.getKnownValues = memoize(this.getKnownValues);
    this.isItemSelected = memoize(this.isItemSelected);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.fieldSummary !== nextProps.fieldSummary) {
      this.getKnownValues.cache.clear();
    }
    if (this.props.filter !== nextProps.filter) {
      this.isItemSelected.cache.clear();
    }
  }

  toFilterValue(value) {
    return this.props.field.type === 'string' ? String(value)
      : this.props.field.type === 'number' ? Number(value)
      : this.props.field.type === 'date' ? Date(value)
      : value;
  }

  getKnownValues() {
    return this.props.fieldSummary.valueCounts
      .filter(({ value }) => value != null)
      .map(({ value }) => value);
  }

  getValuesForFilter() {
    return get(this.props, 'filter.value');
  }

  isItemSelected(value) {
    let { filter, selectByDefault } = this.props;

    return filter == null ? selectByDefault
      // value is null (ie, unknown) and includeUnknown selected
      : value == null ? filter.includeUnknown
      // filter.value is null (ie, all known values), or filter.value includes value
      : filter.value == null || filter.value.includes(value);
  }

  handleItemClick(item, addItem = !this.isItemSelected(item.value)) {
    let { selectByDefault } = this.props;
    let { value } = item;
    if (value == null) {
      this.handleUnknownChange(addItem);
    }
    else {
      const currentFilterValue = this.props.filter == null
        ? (selectByDefault ? this.getKnownValues() : [])
        : this.getValuesForFilter() || this.getKnownValues();
      const filterValue = addItem
        ? currentFilterValue.concat(value)
        : currentFilterValue.filter(v => v !== value);

      this.emitChange(
        filterValue.length === this.getKnownValues().length ? undefined : filterValue,
        get(this.props, 'filter.includeUnknown', true)
      );
    }
  }

  handleUnknownChange(addUnknown) {
    this.emitChange(this.getValuesForFilter(), addUnknown);
  }

  handleSelectAll() {
    this.emitChange(undefined, true);
  }

  handleRemoveAll() {
    this.emitChange([], false);
  }

  handleSort(nextSort) {
    let sort = Object.assign({}, this.props.fieldState.sort, nextSort);
    this.props.onSort(this.props.field, sort);
  }

  emitChange(value, includeUnknown) {
    this.props.onChange(this.props.field, value, includeUnknown,
      this.props.fieldSummary.valueCounts);
  }

  render() {
    var useSort = (
      this.props.fieldState &&
      this.props.fieldState.sort &&
      typeof this.props.onSort === 'function'
    );

    return (
      <ModalBoundary>
        <div className="membership-filter">
          { useSort ? (
            <div className="membership-actions">
              <div className="membership-action membership-action__group-selected">
                <button
                  style={{
                    background: 'none',
                    border: 'none'
                  }}
                  type="button"
                  onClick={() => {
                    this.handleSort(Object.assign(this.props.fieldState.sort, {}, {
                      groupBySelected: !this.props.fieldState.sort.groupBySelected
                    }));
                  }}
                >
                  <Toggle
                    on={this.props.fieldState.sort.groupBySelected}
                  /> Keep selected values at top
               </button>

              </div>
            </div>
          ) : null }

          <Mesa
            options={{
              isRowSelected: (item) => this.isItemSelected(item.value),
              deriveRowClassName: (item) => 'member' +
                (item.filteredCount === 0 ? ' member__disabled' : '') +
                (this.isItemSelected(item.value) ? ' member__selected' : ''),
              onRowClick: (item) => this.handleItemClick(item),
              useStickyHeader: true,
              tableBodyMaxHeight: '80vh'
            }}
            uiState={this.props.fieldState}
            actions={[]}
            eventHandlers={{
              onRowSelect: (item) => this.handleItemClick(item, true),
              onRowDeselect: (item) => this.handleItemClick(item, false),
              onMultipleRowSelect: () => this.handleSelectAll(),
              onMultipleRowDeselect: () => this.handleRemoveAll(),
              onSort: ({key: columnKey}, direction) => useSort && this.handleSort({columnKey, direction})
            }}
            rows={this.props.fieldSummary.valueCounts}
            filteredRows={this.props.fieldSummary.valueCounts}
            columns={[
              {
                key: 'value',
                inline: true,
                name: this.props.field.display,
                sortable: useSort,
                renderCell: ({ value }) =>
                  <div>{value == null ? UNKNOWN_ELEMENT : safeHtml(String(value))}</div>
              },
              {
                key: 'filteredCount',
                sortable: useSort,
                width: '11em',
                helpText: (
                  <div>
                    The number of <em>{this.props.displayName}</em> that match the criteria chosen for other qualities, <br/>
                    and that have the given <em>{this.props.field.display}</em> value.
                  </div>
                ),
                wrapCustomHeadings: ({ headingRowIndex }) => headingRowIndex === 0,
                renderHeading: this.props.fieldSummary.internalsFilteredCount ? [
                  () => (
                    <div style={{display: 'flex', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                      <div>Matching</div>
                      <div style={{marginLeft: '.6ex', maxWidth: '6em', overflow: 'hidden', textOverflow: 'ellipsis'}}>{this.props.displayName}</div>
                    </div>
                  ),
                  () => (
                    <div>
                      {this.props.fieldSummary.internalsFilteredCount.toLocaleString()}
                      <small style={{ display: 'inline-block', width: '50%', textAlign: 'center' }}>(100%)</small>
                    </div>
                  )
                ] : () => (
                  <div style={{display: 'flex', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                    <div>Matching</div>
                    <div style={{marginLeft: '.6ex', maxWidth: '6em', overflow: 'hidden', textOverflow: 'ellipsis'}}>{this.props.displayName}</div>
                  </div>
                ),
                renderCell: ({ value }) => (
                  <div>
                    {value.toLocaleString()}
                    &nbsp;
                    {this.props.fieldSummary.internalsFilteredCount && (
                      <small style={{ display: 'inline-block', width: '50%', textAlign: 'center' }}>
                        ({Math.round(value/this.props.fieldSummary.internalsFilteredCount * 100)}%)
                      </small>
                    )}
                  </div>
                )
              },
              {
                key: 'count',
                sortable: useSort,
                width: '11em',
                helpText: (
                  <div>
                    The number of <em>{this.props.displayName}</em> with the
                    given <em>{this.props.field.display}</em> value.
                  </div>
                ),
                wrapCustomHeadings: ({ headingRowIndex }) => headingRowIndex === 0,
                renderHeading: this.props.fieldSummary.internalsCount ? [
                  () => (
                    <div style={{display: 'flex', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                      <div>All</div>
                      <div style={{marginLeft: '.6ex', maxWidth: '6em', overflow: 'hidden', textOverflow: 'ellipsis'}}>{this.props.displayName}</div>
                    </div>
                  ),
                  () => (
                    <div>
                      {this.props.fieldSummary.internalsCount.toLocaleString()}
                      <small style={{ display: 'inline-block', width: '50%', textAlign: 'center'}}>(100%)</small>
                    </div>
                  )
                ] : () => (
                  <div style={{display: 'flex', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                    <div>All</div>
                    <div style={{marginLeft: '.6ex', maxWidth: '6em', overflow: 'hidden', textOverflow: 'ellipsis'}}>{this.props.displayName}</div>
                  </div>
                ),
                renderCell: ({ value }) => (
                  <div>
                    {value.toLocaleString()}
                    &nbsp;
                    {this.props.fieldSummary.internalsCount && (
                      <small style={{ display: 'inline-block', width: '50%', textAlign: 'center' }}>
                        ({Math.round(value/this.props.fieldSummary.internalsCount * 100)}%)
                      </small>
                    )}
                  </div>
                )
              },
              {
                key: 'distribution',
                name: 'Distribution',
                width: '30%',
                helpText: <FilterLegend {...this.props} />,
                renderCell: ({ row }) => (
                  <div className="bar">
                    <div className="fill" style={{
                      width: (row.count / (this.props.fieldSummary.internalsCount || this.props.dataCount) * 100) + '%'
                    }}/>
                    <div className="fill filtered" style={{
                      width: (row.filteredCount / (this.props.fieldSummary.internalsCount || this.props.dataCount) * 100) + '%'
                    }}/>
                  </div>
                )
              },
              {
                key: '%',
                name: '',
                width: '4em',
                helpText: (
                  <div>
                    <em>Matching {this.props.displayName}</em> out of <em>Total {this.props.displayName}</em><br/>
                    with the given <em>{this.props.field.display}</em> value.
                  </div>
                ),
                renderCell: ({ row }) => (
                  <small title={`Matching ${row.value} / All ${row.value}`}>
                    ({Math.round(row.filteredCount / row.count * 100)}%)
                  </small>
                )
              }
            ]}
          >
          </Mesa>
        </div>
      </ModalBoundary>
    );
  }
}

MembershipField.propTypes = FieldFilter.propTypes
