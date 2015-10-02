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
    let { recordClass, category, isCollapsed } = this.props;
    this.props.recordActions.toggleCategoryCollapsed({
      recordClass,
      category,
      isCollapsed: !isCollapsed
    });
  },

  toggleTableCollapse(tableName, isCollapsed) {
    let { recordClass } = this.props;
    this.props.recordActions.toggleTableCollapsed({
      recordClass,
      tableName,
      isCollapsed: !isCollapsed
    });
  },

  render() {
    let { category, depth, record, recordClass, isCollapsed, collapsedTables } = this.props;
    let attributes = recordClass.attributes.filter(a => a.category === category.name);
    let tableMetas = recordClass.tables.filter(t => t.category === category.name);
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
              {tableMetas.map(tableMeta => {
                let { name, displayName } = tableMeta;
                let table = record.tables[name];
                let isCollapsed = collapsedTables.includes(name)
                if (table.length === 0) return null;
                return (
                  <div key={name} className="wdk-RecordTableWrapper">
                    <h3 style={{ cursor: 'pointer' }}
                      onClick={() => this.toggleTableCollapse(name, isCollapsed)}>
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
