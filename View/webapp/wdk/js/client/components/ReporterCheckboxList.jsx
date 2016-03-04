import { wrappable } from '../utils/componentUtils';
import CheckboxList from './CheckboxList';

let ReporterCheckboxList = props => {
  let { title, onChange, fields, selectedFields } = props;
  if (fields.length == 0) {
    return ( <noscript/> );
  }
  let mappedFields = fields.map(val => ({ value: val.name, display: val.displayName }));
  return (
    <div>
      <h3>{title}</h3>
      <div style={{padding: '0 2em'}}>
        <CheckboxList onChange={onChange} items={mappedFields} value={selectedFields}/>
      </div>
    </div>
  );
};

export default wrappable(ReporterCheckboxList);
