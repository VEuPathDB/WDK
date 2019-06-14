package org.gusdb.wdk.model.toolbundle.filter.config;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RangeConfig<T> {
  private T left;
  private T right;
  private boolean leftClosed;
  private boolean rightClosed;
  private boolean complement;

  public T getLeft() {
    return left;
  }

  @JsonAlias("min")
  public RangeConfig<T> setLeft(T left) {
    this.left = left;
    return this;
  }

  public T getRight() {
    return right;
  }

  @JsonAlias("max")
  public RangeConfig<T> setRight(T right) {
    this.right = right;
    return this;
  }

  public boolean isLeftClosed() {
    return leftClosed;
  }

  public RangeConfig<T> setLeftClosed(boolean leftClosed) {
    this.leftClosed = leftClosed;
    return this;
  }

  public boolean isRightClosed() {
    return rightClosed;
  }

  public RangeConfig<T> setRightClosed(boolean rightClosed) {
    this.rightClosed = rightClosed;
    return this;
  }

  public boolean isComplement() {
    return complement;
  }

  public RangeConfig<T> setComplement(boolean complement) {
    this.complement = complement;
    return this;
  }

  public RangeConfig<T> copy() {
    return new RangeConfig<T>()
      .setLeft(left)
      .setRight(right)
      .setLeftClosed(leftClosed)
      .setRightClosed(rightClosed)
      .setComplement(complement);
  }
}
