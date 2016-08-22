import { wrappable } from '../utils/componentUtils';
import WdkServiceJsonReporterForm from './WdkServiceJsonReporterForm';

let DownloadForm = props => ( <WdkServiceJsonReporterForm {...props}/> );

export default wrappable(DownloadForm);
