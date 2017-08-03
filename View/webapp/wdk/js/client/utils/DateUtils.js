/**
 * Just some constants for use below
 * @private epoch : indicates the default local 'beginning of time'
 *    set to 1970 initially, following UNIX's lead
 * @private currentYear : the current calendar year
 *    also indicates the default local 'end of time' (dec 31 $currentYear)
 * @private monthNames
 *    duh, obvious
**/
const epoch = 1970;
const currentYear = (new Date()).getFullYear();
const monthNames = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December'
];

const DateUtils = {
  epoch,
  currentYear,
  _defaultValues: {
    year: currentYear,
    month: 1,
    day: 1
  },
  generateYearList (start = DateUtils.epoch, end = DateUtils.currentYear) {
    let output = [];
    let cursor = start;
    while (cursor <= end) { output.push(cursor++); };
    return output;
  },
  generateMonthList (start = 1, end = 12) {
    let output = [];
    let cursor = start;
    while (cursor <= end) { output.push(cursor++); }
    return output;
  },
  generateMonthNameList (start = 1, end = 12) {
    let list = DateUtils.generateMonthList(start, end);
    return list.map(DateUtils.getMonthName);
  },
  getMonthName (monthNumber = 1) {
    let monthIndex = monthNumber - 1;
    return monthNames[monthIndex];
  },
  getDaysInMonth (monthNumber = 1, year = currentYear) {
    let analog = new Date(year, monthNumber, 0);
    return analog.getDate();
  },
  generateDayListByMonth (monthNumber = 1, year = currentYear) {
    let dayCount = DateUtils.getDaysInMonth(monthNumber, year);
    let cursor = 1;
    let output = [];
    while (cursor <= dayCount) { output.push(cursor++); }
    return output;
  },
  formatDate (year = currentYear, month = 1, day = 1) {
    year = year.toString().length === 4 ? year.toString() : currentYear.toString();
    month = (month.toString().length === 1 ? '0' : '') + month.toString();
    day = (day.toString().length === 1 ? '0' : '') + day.toString();
    return `${year}-${month}-${day}`;
  },
  isValidDateString (dateString) {
    if (typeof dateString !== 'string') return false;
    if (!dateString.match(/([0-9]{4,4})-([0-9]{2,2})-([0-9]{2,2})/)) return false;
    let [ year, month, day ] = dateString.split('-').map(x => x * 1);
    if (month > 12 || day > DateUtils.getDaysInMonth(month, year)) return false;
    return true;
  },
  isValidDateObject ({ year, month, day }) {
    return ![ year, month, day ].filter(a => typeof a !== 'number').length;
  },
  monthHasDay (day, month, year = currentYear) {
    if (typeof day === 'undefined' || typeof month === 'undefined') return false;
    if (day <= 28) return true;
    return DateUtils.generateDayListByMonth(month, year).indexOf(day) >= 0;
  },
  conformDayWithinMonth (day, month, year) {
    if ([day, month, year].filter(val => typeof val !== 'number').length) return;
    let cutoff = DateUtils.getDaysInMonth(month, year);
    return day < cutoff ? day : cutoff;
  },
  conformDateToBounds ({ year, month, day }, { start, end }) {
    start = DateUtils.isValidDateObject(start) ? start : DateUtils.getEpochStart();
    end = DateUtils.isValidDateObject(end) ? end : DateUtils.getEpochEnd();
    day = DateUtils.conformDayWithinMonth(day, month, year);

    if (year < start.year) year = start.year;
    else if (year > end.year) year = end.year;

    if (year === start.year && month < start.month) month = start.month;
    else if (year === end.year && month > end.month) month = end.month;

    if (year === start.year && month === start.month && day < start.day) day = start.day;
    else if (year === end.year && month === end.month && day > end.day) day = end.day;

    return { year, month, day };
  },
  parseDate (dateString) {
    if (!DateUtils.isValidDateString(dateString)) return null;
    let [ year, month, day ] = dateString.split('-').map(x => x * 1);
    return { year, month, day };
  },

  getDefaults () {
    return DateUtils._defaultValues;
  },
  setDefaults (replacement) {
    DateUtils._defaultValues = Object.assign({}, DateUtils._defaultValues, replacement);
  },

  getEpochStart (year = epoch, month = 1, day = 1) {
    return { year, month, day };
  },
  getEpochEnd (year = currentYear, month = 12, day = 31) {
    return { year, month, day };
  },

  getPreviousMonth (monthNumber) {
    return monthNumber > 1 ? --monthNumber : 12;
  },
  getNextMonth (monthNumber) {
    return monthNumber < 12 ? ++monthNumber : 1;
  },

  getPreviousDay ({ day, month, year }) {
    if (day > 1) {
      day--;
    } else {
      month = DateUtils.getPreviousMonth(month);
      if (month === 12) year--;
      day = DateUtils.getDaysInMonth(month, year);
    }
    return { day, month, year };
  },
  getPreviousDayString (dateString) {
    let date = DateUtils.parseDate(dateString);
    return date ? DateUtils.formatDate(DateUtils.getPreviousDay(date)) : undefined;
  },

  getNextDay ({ day, month, year }) {
    let lastDay = DateUtils.getDaysInMonth(month, year);
    if (day < lastDay) {
      day++;
    } else {
      month = DateUtils.getNextMonth(month);
      if (month === 1) year++;
      day = 1;
    }
    return { day, month, year };
  },
  getNextDayString (dateString) {
    let date = DateUtils.parseDate(dateString);
    return date ? DateUtils.formatDate(DateUtils.getNextDay(date)) : undefined;
  },

  dateIsAfter (dateString, checkAgainstDateString) {
    const date = DateUtils.parseDate(dateString);
    const check = DateUtils.parseDate(checkAgainstDateString);
    if (!date || !check) return null;
    return (date.year > check.year) ||
           (date.year === check.year && date.month > check.month) ||
           (date.year === check.year && date.month === check.month && date.day > check.day);
  },
  dateIsBefore (dateString, checkAgainstDateString) {
    const date = DateUtils.parseDate(dateString);
    const check = DateUtils.parseDate(checkAgainstDateString);
    if (!date || !check) return null;
    return (date.year < check.year) ||
           (date.year === check.year && date.month < check.month) ||
           (date.year === check.year && date.month === check.month && date.day < check.day);
  }
};

export default DateUtils;
