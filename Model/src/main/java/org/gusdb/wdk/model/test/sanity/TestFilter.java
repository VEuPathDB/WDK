package org.gusdb.wdk.model.test.sanity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class TestFilter {

  private static final Logger LOG = Logger.getLogger(TestFilter.class);

  private static class Range {

    private Integer _start;
    private Integer _end;

    public Range setStart(int start) {
      check(start, _end);
      _start = start;
      return this;
    }

    public Range setEnd(int end) {
      check(_start, end);
      _end = end;
      return this;
    }

    private static void check(Integer start, Integer end) throws NumberFormatException {
      if ((start != null && start < 0) || (end != null && end < 0))
        throw new NumberFormatException("Filter ranges do not accept negative numbers");
      if (start != null && end != null && start > end) {
        throw new NumberFormatException("Start value (" + start + ") must be less than end value (" + end + ")");
      }
    }

    public boolean numberInRange(int number) {
      if (_start == null && _end == null) return false;
      if (_start == null) return (number <= _end);
      if (_end == null) return (number >= _start);
      return (number >= _start && number <= _end);
    }

    @Override
    public String toString() {
      return "[ " + _start + ", " + _end + " ]";
    }
  }
  
  private final String _originalString;
  private final List<Range> _ranges;
  
  public TestFilter(String testFilterString) throws NumberFormatException {
    if (testFilterString == null || testFilterString.trim().isEmpty()) {
      _originalString = "";
      _ranges = new ArrayList<>();
    }
    else {
      _originalString = testFilterString;
      _ranges = parseFilterString(testFilterString);
    }
  }

  private static List<Range> parseFilterString(String listStr) {
    try {
      LOG.debug("Parsing filter ranges: " + listStr);
      List<Range> ranges = new ArrayList<>();
      listStr = listStr.replaceAll("\\s", "");

      LOG.debug("Filter string not empty.  Translated string: " + listStr);
      String[] rangeStrs = listStr.trim().split(",");
      for (String rangeStr : rangeStrs) {
        Range range = new Range();
        int dashIndex = rangeStr.indexOf('-');
        switch (dashIndex) {
          // no dash found in range (e.g. 4, selects only test #4
          case -1:
            int num = Integer.parseInt(rangeStr);
            range.setStart(num).setEnd(num);
            break;
          // dash is first character (e.g. -15, selects all tests up to #15)
          case 0:
            range.setEnd(Integer.parseInt(rangeStr.substring(1)));
            break;
          // dash present and not first char
          //   (e.g. 4-10 or 20-, selects tests #4-10 and all past #20 (inclusive) respectively)
          default:
            range.setStart(Integer.parseInt(rangeStr.substring(0, dashIndex)));
            if (dashIndex < rangeStr.length() - 1)
              // dash is not last char, add end range value
              range.setEnd(Integer.parseInt(rangeStr.substring(dashIndex + 1)));
        }
        LOG.debug("Adding range: " + range);
        ranges.add(range);
      }
      return ranges;
    }
    catch (IndexOutOfBoundsException e) {
      throw new NumberFormatException("Could not parse filter string: " + e.getMessage());
    }
  }

  public boolean filterOutTest(int testId) {
    if (_ranges.isEmpty()) return false;
    for (Range range : _ranges) {
      if (range.numberInRange(testId)) return false;
    }
    return true;
  }

  public String getOriginalString() {
    return _originalString;
  }
}
