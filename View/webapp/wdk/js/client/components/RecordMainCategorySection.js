import React from 'react';
import RecordAttribute from './RecordAttribute';
import RecordTable from './RecordTable';
import { wrappable } from '../utils/componentUtils';
import {
  formatAttributeValue
} from '../utils/stringUtils';

let RecordMainCategorySection = React.createClass({

  propTypes: {
    isCollapsed: React.PropTypes.bool.isRequired,
    category: React.PropTypes.object.isRequired,
    depth: React.PropTypes.number,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired
  },

  mixins: [ React.addons.PureRenderMixin ],

  toggleCollapse() {
    let { category, isCollapsed } = this.props;
    this.props.onCategoryToggle(category, !isCollapsed);
  },

  toggleTableCollapse(table, isCollapsed) {
    this.props.onTableToggle(table, !isCollapsed);
  },

  render() {
    let { category, depth, record, attributes, tables, isCollapsed, collapsedTables } = this.props;
    let headerClass = depth === 1 ? 'wdk-Record-sectionHeader' : 'wdk-Record-sectionSubHeader';
    let Header = 'h' + Math.min(depth + 1, 6);

    return isCollapsed && depth === 1 ? null : (
      <div className="wdk-Record-section">
        {depth === 1 ? (
          <Header id={String(category.name)} className={headerClass}>
            {category.displayName}
          </Header>
        ) : (
          <Header id={String(category.name)} className={headerClass} onClick={this.toggleCollapse}>
            <i className={'fa fa-' + (isCollapsed ? 'caret-right' : 'caret-down')}/> {category.displayName}
          </Header>
        )}
        {isCollapsed ? null : (
          <div>
            <div className="wdk-Record-sectionContent">
              {attributes.length > 0 &&
                <table className="wdk-RecordAttributeTable">
                  <tbody>
                    {attributes.reduce(function(rows, attribute) {
                      let { name, displayName } = attribute;
                      let value = record.attributes[name]
                      if (value != null) {
                        rows.push(
                          <tr key={name}>
                            <td><strong>{displayName}</strong></td>
                            <td><RecordAttribute value={value} /></td>
                          </tr>
                        );
                      }
                      return rows;
                    }, [])}
                  </tbody>
                </table>
              }
              {tables.map(tableMeta => {
                let { name, displayName } = tableMeta;
                let table = record.tables[name];
                let isCollapsed = collapsedTables.includes(name)
                if (table.length === 0) return null;
                return (
                  <div key={name} className="wdk-RecordTableWrapper">
                    <h3 style={{ cursor: 'pointer' }}
                      onClick={() => this.toggleTableCollapse(tableMeta, isCollapsed)}>
                      <i className={'fa fa-' + (isCollapsed? 'caret-right' : 'caret-down')}/>
                      {' ' + displayName}
                    </h3>
                    {isCollapsed? null : <RecordTable table={table} tableMeta={tableMeta}/>}
                  </div>
                );
              })}
            </div>
            {this.props.children}
          </div>
        )}
      </div>
    );
  }

});

export default wrappable(RecordMainCategorySection);
