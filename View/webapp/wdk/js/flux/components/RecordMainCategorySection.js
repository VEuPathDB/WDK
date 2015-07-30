import React from 'react';
import RecordAttribute from './RecordAttribute';
import RecordTable from './RecordTable';
import wrappable from '../utils/wrappable';
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
    let attributeMetas = recordClass.attributes.filter(a => a.category === category.name);
    let tableMetas = recordClass.tables.filter(t => t.category === category.name);
    let headerClass = depth === 1 ? 'wdk-Record-sectionHeader' : 'wdk-Record-sectionSubHeader';
    let Header = 'h' + Math.min(depth + 1, 6);

    return (
      <div className="wdk-Record-section">
        {depth === 1 &&
          <a href="#" className="wdk-Record-sectionHeaderTopLink">Back to top</a>
        }
        <Header id={String(category.name)} className={headerClass} onClick={this.toggleCollapse}>
          <i className={'fa fa-' + (isCollapsed ? 'caret-right' : 'caret-down')}/> {category.displayName}
        </Header>
        {!isCollapsed && attributeMetas.length > 0 && 
          <div className="wdk-Record-sectionContent">
            <table className="wdk-RecordAttributeTable">
              <tbody>
                {attributeMetas.reduce(function(rows, attributeMeta) {
                  let attribute = record.attributes[attributeMeta.name];
                  if (attribute.value != null) {
                    rows.push(
                      <tr key={attribute.name}>
                        <td><strong>{attribute.displayName}</strong></td>
                        <td><RecordAttribute attribute={attribute} /></td>
                      </tr>
                    );
                  }
                  return rows;
                }, [])}
              </tbody>
            </table>
            {tableMetas.map(tableMeta => {
              let table = record.tables[tableMeta.name];
              return (
                <div key={table.name} className="wdk-RecordTableWrapper">
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
