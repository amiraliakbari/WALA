/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.util.intset;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.debug.Assertions;

/**
 * A Set backed by a set of integers.
 * 
 * @author sfink
 */
public class OrdinalSet<T> implements Iterable<T> {

  private final IntSet S;

  private final OrdinalSetMapping<T> mapping;

  private final static OrdinalSet EMPTY = new OrdinalSet();

  @SuppressWarnings("unchecked")
  public static <T> OrdinalSet<T> empty() {
    return EMPTY;
  }

  private OrdinalSet() {
    S = null;
    mapping = null;
  }

  /**
   */
  public OrdinalSet(IntSet S, OrdinalSetMapping<T> mapping) {
    this.S = S;
    this.mapping = mapping;
  }

  /**
   */
  public boolean containsAny(OrdinalSet<T> that) {
    if (S == null || that.S == null) {
      return false;
    }
    return S.containsAny(that.S);
  }

  /**
   */
  public int size() {
    return (S == null) ? 0 : S.size();
  }

  /**
   */
  public Iterator<T> iterator() {
    if (S == null) {
      return EmptyIterator.instance();
    } else {

      return new Iterator<T>() {
        IntIterator it = S.intIterator();

        public boolean hasNext() {
          return it.hasNext();
        }

        public T next() {
          return mapping.getMappedObject(it.next());
        }

        public void remove() {
          Assertions.UNREACHABLE();
        }
      };
    }
  }

  /**
   * @return a new OrdinalSet instances
   * @throws IllegalArgumentException  if A is null
   */
  public static <T> OrdinalSet<T> intersect(OrdinalSet<T> A, OrdinalSet<T> B) {
    if (A == null) {
      throw new IllegalArgumentException("A is null");
    }
    if (Assertions.verifyAssertions) {
      if (A.size() != 0 && B.size() != 0) {
        Assertions._assert(A.mapping.equals(B.mapping));
      }
    }
    if (A.S == null || B.S == null) {
      return new OrdinalSet<T>(null, A.mapping);
    }
    IntSet isect = A.S.intersection(B.S);
    return new OrdinalSet<T>(isect, A.mapping);
  }

  /**
   * Creates the union of two ordinal sets.
   * @param A ordinal set a
   * @param B ordinal set b
   * @return union of a and b
   * @throws IllegalArgumentException iff A or B is null
   */
  public static <T> OrdinalSet<T> unify(OrdinalSet<T> A, OrdinalSet<T> B) {
    if (A == null) {
      throw new IllegalArgumentException("A is null");
    }
    if (B == null) {
      throw new IllegalArgumentException("B is null");
    }
    if (Assertions.verifyAssertions) {
      if (A.size() != 0 && B.size() != 0) {
        Assertions._assert(A.mapping.equals(B.mapping));
      }
    }
    
    if (A.S == null) {
      return (B.S == null) ? 
          OrdinalSet.<T>empty() : new OrdinalSet<T>(B.S, B.mapping);
    } else if (B.S == null) {
      return (A.S == null) ? 
          OrdinalSet.<T>empty() : new OrdinalSet<T>(A.S, A.mapping);
    }

    IntSet union = A.S.union(B.S);
    return new OrdinalSet<T>(union, A.mapping);
  }

  @Override
  public String toString() {
    return Iterator2Collection.toCollection(iterator()).toString();
  }

  /**
   */
  public SparseIntSet makeSparseCopy() {
    return (S == null) ? new SparseIntSet() : new SparseIntSet(S);
  }

  /**
   * Dangerous. Added for performance reasons. Use this only if you really know
   * what you are doing.
   */
  public IntSet getBackingSet() {
    return S;
  }

  /**
   * @param object
   * @return true iff this set contains object
   */
  public boolean contains(T object) {
    if (this == EMPTY || S == null) {
      return false;
    }
    int index = mapping.getMappedIndex(object);
    return (index == -1) ? false : S.contains(index);
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * @param instances
   * @throws NullPointerException  if instances is null
   */
  public static <T> Collection<T> toCollection(OrdinalSet<T> instances) throws NullPointerException {
    return Iterator2Collection.toCollection(instances.iterator());
  }

  /**
   * Precondition: the ordinal set mapping has an index for every element of c
   * Convert a "normal" collection to an OrdinalSet, based on the given mapping.
   * @throws IllegalArgumentException  if c is null
   */
  public static <T> OrdinalSet<T> toOrdinalSet(Collection<T> c, OrdinalSetMapping<T> m) {
    if (c == null) {
      throw new IllegalArgumentException("c is null");
    }
    MutableSparseIntSet s = MutableSparseIntSet.makeEmpty();
    for (Iterator<T> it = c.iterator(); it.hasNext();) {
      int index = m.getMappedIndex(it.next());
      if (Assertions.verifyAssertions) {
        Assertions._assert(index >= 0);
      }
      s.add(index);
    }
    return new OrdinalSet<T>(s, m);
  }

  public OrdinalSetMapping<T> getMapping() {
    return mapping;
  }

}