package org.gusdb.wdk.model.answer.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.junit.jupiter.api.Test;

public class FilterOptionListTest {

  @Test
  public void doTest() {
    FilterOptionListBuilder folb = FilterOptionList.builder();
    folb.add(FilterOption.builder().setFilterName("a"));
    folb.add(FilterOption.builder().setFilterName("b"));
    assertEquals(2, folb.size());
    folb.removeAll(f -> f.getFilterName().equals("a"));
    assertEquals(1, folb.size());
  }
}
