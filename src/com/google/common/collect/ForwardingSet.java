/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import java.util.Set;

/**
 * A set which forwards all its method calls to another set. Subclasses should
 * override one or more methods to modify the behavior of the backing set as
 * desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author Kevin Bourrillion
 */
public abstract class ForwardingSet<E> extends ForwardingCollection<E>
    implements Set<E> {

  @Override protected abstract Set<E> delegate();

  @Override public boolean equals(Object obj) {
    return (this == obj) || delegate().equals(obj);
  }

  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
