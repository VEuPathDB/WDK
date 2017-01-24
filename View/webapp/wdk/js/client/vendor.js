// Import scripts that expose global vars
import 'lib/jquery';
import 'lib/jquery-migrate';
import 'lib/jquery-ui';
import 'lib/jquery-qtip';
import 'lib/jquery-datatables';
import 'lib/jquery-datatables-natural-type-plugin';

if (process.env.NODE_ENV !== 'production') {
  require('react-addons-perf');
}
