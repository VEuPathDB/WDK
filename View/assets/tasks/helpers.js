var flagr = /^(.*)!(.*)$/;

function filterByFlag(name, value, patterns) {
  name = name.toUpperCase();
  value = value.toUpperCase();
  // check for env flags; only include script if env = dev
  return patterns.map(function(pattern) {
    return matchesFlag(name, value, pattern);
  }).filter(function(e) {
    return e !== null
  });
}

function matchesFlag(name, value, pattern) {
  if (flagr.test(pattern)) {
    var match = pattern.match(flagr);
    flag = match[1].split(':');
    if (flag[0].toUpperCase() === name && flag[1].toUpperCase() === value) {
      pattern = match[2];
    } else {
      return null;
    }
  }
  return pattern;
}

module.exports.filterByFlag = filterByFlag;
