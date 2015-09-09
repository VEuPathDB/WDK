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

  render() {
    let { category, depth, record, recordClass, isCollapsed } = this.props;
    let attributes = recordClass.attributes.filter(a => a.category === category.name);
    let tableMetas = recordClass.tables.filter(t => t.category === category.name);
    let headerClass = depth === 1 ? 'wdk-Record-sectionHeader' : 'wdk-Record-sectionSubHeader';
    let Header = 'h' + Math.min(depth + 1, 6);

    return (
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
        {!isCollapsed &&
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
              let table = record.tables[tableMeta.name];
              if (table.length === 0) return null;
              return (
                <div key={tableMeta.name} className="wdk-RecordTableWrapper">
                  <h3>{tableMeta.displayName}</h3>
                  <RecordTable table={table} tableMeta={tableMeta}/>
                </div>
              );
            })}
          </div>
        }
        {!isCollapsed && this.props.children}
      </div>
    );
  }

});

export default wrappable(RecordMainCategorySection);
