package org.gusdb.wdk.model.dataset;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.DatasetParser.DatasetIterator;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class AbstractDatasetIterator implements DatasetIterator {
  private final Pattern rowDiv;
  private final Set<Integer> hashSet;
  private String next;
  private boolean exhausted;

  protected final DatasetContents contents;
  protected Scanner contentScanner;
  protected int rowNum;

  public AbstractDatasetIterator(final DatasetContents contents, final String rowDiv) {
    this.contents = contents;
    this.rowDiv   = Pattern.compile(rowDiv);
    this.hashSet  = new HashSet<>();
  }

  @Override
  public String[] next() throws WdkModelException, WdkUserException {
    initScanner();
    var tmp = parseRow(next);
    nextRow();
    return tmp;
  }

  @Override
  public boolean hasNext() throws WdkModelException, WdkUserException {
    if (exhausted)
      return false;

    initScanner();

    if (next == null)
      nextRow();

    return !exhausted;
  }

  /**
   * Predicate to use when determining whether or not input
   * rows should be skipped.
   *
   * @param row Row to test
   *
   * @return true if the row is usable, false if it should
   *         be skipped.
   */
  protected boolean rowFilter(final String row) {
    return !row.isBlank();
  }

  /**
   * Custom filter to determine if the end of the usable
   * input has been reached.
   *
   * @param row line to test
   *
   * @return true of the file should be considered exhausted
   *         or false if reading can continue.
   */
  protected boolean atEndOfInput(final String row) {
    return false;
  }

  private void nextRow() {
    if (exhausted)
      return;

    while(contentScanner.hasNext()) {
      var tmp = contentScanner.next().trim();
      var ths = tmp.hashCode();
      rowNum++;

      // Skip duplicate entries.
      if (hashSet.contains(ths))
        continue;
      else
        hashSet.add(ths);

      if (rowFilter(tmp)) {
        next = tmp;
        return;
      }

      if (!atEndOfInput(tmp))
        break;
    }

    exhausted = true;
  }

  protected abstract String[] parseRow(final String row)
    throws WdkModelException, WdkUserException;

  private void initScanner() throws WdkModelException {
    if (contentScanner == null)
      contentScanner = new Scanner(contents.getContentReader())
        .useDelimiter(rowDiv);
  }
}
