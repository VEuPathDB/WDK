import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import RecordAttribute from './RecordAttribute';
import RecordTable from './RecordTable';
import { wrappable } from '../utils/componentUtils';

let RecordMainCategorySection = React.createClass({

  propTypes: {
    isCollapsed: React.PropTypes.bool.isRequired,
    category: React.PropTypes.object.isRequired,
    depth: React.PropTypes.number,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired
  },

  mixins: [ PureRenderMixin ],

  toggleCollapse() {
    if (this.props.depth > 1) {
      let { category, isCollapsed } = this.props;
      this.props.onCategoryToggle(category, !isCollapsed);
    }
  },

  toggleTableCollapse(table, isCollapsed) {
    this.props.onTableToggle(table, !isCollapsed);
  },

  render() {
    let {
      category,
      depth,
      record,
      attributes,
      tables,
      isCollapsed,
      collapsedTables,
      enumeration
    } = this.props;
    let Header = 'h' + Math.min(depth + 1, 6);
    let headerClass = classnames({
      'wdk-RecordSectionHeader': depth === 1,
      'wdk-RecordSectionSubHeader': depth !== 1,
      'wdk-RecordSectionSubHeader__collapsed': depth !== 1 && isCollapsed
    });

    return isCollapsed && depth === 1 ? null : (
      <div id={String(category.name)} className="wdk-RecordSection">
        <Header className={headerClass} onClick={this.toggleCollapse}>
          <span className="wdk-RecordSectionEnumeration">{enumeration}</span> {category.displayName}
        </Header>
        {isCollapsed ? null : (
          <div>
            <div className="wdk-RecordSectionContent">
              {attributes.length > 0 &&
                <div className="wdk-RecordAttributeSection">
                  {attributes.reduce(function(rows, attribute) {
                    let { name, displayName } = attribute;
                    let value = record.attributes[name]
                    if (value != null) {
                      rows.push(
                        <div className="wdk-RecordAttributeSectionItem" key={name}>
                          <div className="wdk-RecordAttributeName">
                            <strong>{displayName}</strong>
                          </div>
                          <div className="wdk-RecordAttributeValue">
                            <RecordAttribute value={value} />
                          </div>
                        </div>
                      );
                    }
                    return rows;
                  }, [])}
                </div>
              }
              {tables.map(tableMeta => {
                let { name, displayName } = tableMeta;
                let table = record.tables[name];
                let isCollapsed = collapsedTables.includes(name)
                let headerClass = classnames({
                  'wdk-RecordTableHeader': true,
                  'wdk-RecordTableHeader__collapsed': isCollapsed
                });
                if (table.length === 0) return null;
                return (
                  <div key={name} className="wdk-RecordTableWrapper">
                    <div className={headerClass}
                      onClick={() => this.toggleTableCollapse(tableMeta, isCollapsed)}>
                      {' ' + displayName}
                    </div>
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
