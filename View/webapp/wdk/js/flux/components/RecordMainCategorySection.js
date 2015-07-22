import React from 'react';
import RecordAttribute from './RecordAttribute';
import RecordTable from './RecordTable';
import wrappable from '../utils/wrappable';
import {
  formatAttributeValue
} from '../utils/stringUtils';

let RecordMainCategorySection = React.createClass({

  propTypes: {
    category: React.PropTypes.object.isRequired,
    depth: React.PropTypes.number,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired
  },

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { category, depth, record, recordClass } = this.props;
    let attributeMetas = recordClass.attributes.filter(a => a.category === category.name);
    let tableMetas = recordClass.tables.filter(t => t.category === category.name);
    let rootClass = depth === 1 ? '' : 'wdk-Record-mainCategorySection';
    let headerClass = depth === 1 ? 'wdk-Record-sectionHeader' : 'wdk-Record-sectionSubHeader';
    let Header = 'h' + Math.min(depth + 1, 6);
    return (
      <div className={rootClass}>
        {depth === 1 &&
          <a href="#" className="wdk-Record-sectionHeaderTopLink">Back to top</a>
        }
        <Header id={String(category.name)} className={headerClass}>
          {category.displayName}
        </Header>
        {attributeMetas.length > 0 &&
          <div className="wdk-Record-sectionContent">
            <table className="wdk-RecordAttributeTable">
              <tbody>
                {attributeMetas.map(attributeMeta => {
                  let attribute = record.attributes[attributeMeta.name];
                  if (attribute.value == '' || attribute.value == null) return null;
                  return (
                    <tr key={attribute.name}>
                      <td><strong>{attribute.displayName}</strong></td>
                      <td><RecordAttribute attribute={attribute} /></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            {tableMetas.map(tableMeta => {
              let table = record.tables[tableMeta.name];
              return (
                <div key={table.name} className="wdk-RecordTableWrapper">
                  <h4>{tableMeta.displayName}</h4>
                  <RecordTable table={table} tableMeta={tableMeta}/>
                </div>
              );
            })}
          </div>
        }
      </div>
    );
  }

});

export default wrappable(RecordMainCategorySection);
