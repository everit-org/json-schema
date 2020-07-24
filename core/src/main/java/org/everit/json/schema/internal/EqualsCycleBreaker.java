package org.everit.json.schema.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

public final class EqualsCycleBreaker
{
    private EqualsCycleBreaker()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * For each invocation of <code>a.equals(b)</code> that uses this class,
     */
    private static final ThreadLocal<WeakHashMap<Identity, Set>> ongoingEqualityChecks = ThreadLocal.withInitial(WeakHashMap::new);

    public static <T> boolean equalsWithoutCycle(T self, T other, boolean equalsOnCycle, BiFunction<T, T, Boolean> equalityFunction) {
        Set<T> localOngoingEqualityChecks = ongoingEqualityChecks.get()
            .computeIfAbsent(new Identity<>(self), (_k) -> new HashSet<>());
        if (localOngoingEqualityChecks.add(other)) {
            try {
                return equalityFunction.apply(self, other);
            }
            finally {
                localOngoingEqualityChecks.remove(other);
                if (localOngoingEqualityChecks.isEmpty()) {
                    ongoingEqualityChecks.remove();
                }
            }
        } else {
            return equalsOnCycle;
        }
    }

    private static class Identity<E> {
        private final E e;

        public Identity(E e)
        {
            this.e = e;
        }

        public int hashCode() {
            return System.identityHashCode(e);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Identity)) {
                return false;
            }
            return ((Identity<?>) o).e == e;
        }
    }
}
