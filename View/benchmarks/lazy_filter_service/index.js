var RUNS = 10;
var metadata = require('./fixtures').metadata;

require('babel-core/register');

var utils = require('../../webapp/wdk/js/models/filter/utils');


run();

function run() {
  rule();
  logAverage(measureUtil('countByValues'));
  rule();
  logAverage(measureUtil('uniqMetadataValues'));
  rule();
}

function measureUtil(fnName) {
  var fn = utils[fnName];
  var runs = RUNS;
  var times = [];
  while(runs--) {
    var time = measure(fnName, function() {
      fn(metadata);
    });
    times.push(time);
  }
  return times;
}

function measure(tag, fn) {
  var start = process.hrtime();
  fn();
  var hrtime = process.hrtime(start);
  logTime(tag, hrtime);
  return hrtime;
}

function logTime(tag, hrtime) {
  console.log('%s: %dms', tag, hrtimeToSeconds(hrtime));
}

function hrtimeToSeconds(hrtime) {
  return (hrtime[0] * 1e9 + hrtime[1]) / 1e6;
}

function logAverage(hrtimes) {
  var average = hrtimes.map(hrtimeToSeconds).reduce(sum) / hrtimes.length;
  console.log();
  console.log('average time: %dms', average);
  console.log();
}

function sum(a, b) {
  return a + b;
}

function rule() {
  console.log('--------------------------------------------------------------------------------')
}
