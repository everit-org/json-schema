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
     * ThreadLocal because this class doesn't bother with stack overflows across multiple threads, if that
     * is even possible.
     * <br>
     * A <i>weak</i> map so that this consumes less memory.
     * <br>
     * For each ongoing equality check via {@link #equalsWithoutCycle(Object, Object, boolean, BiFunction)},
     * maps the <code>this</code> pointer of the <code>equals</code> invocation to all of the objects it is
     * being compared against. Each mapping is removed when `equals` returns.
     * <br>
     * This way, when {@link Object#equals(Object)} is called with the same parameters (this and the other reference)
     * a second time before the first invocation has returned (= cyclic!), it can be detected and handled.
     */
    private static final ThreadLocal<WeakHashMap<Identity, Set>> ongoingEqualityChecks = ThreadLocal.withInitial(WeakHashMap::new);

    /**
     * Use to break cycles in equality checks. For example:
     *
     * <pre>
     *     class A {
     *         B b;
     *
     *         public boolean equals(Object o) {
     *             if (!(o instanceof A)) {
     *                 return false;
     *             }
     *
     *             return this.b.equals(((A) o).b);
     *         }
     *     }
     *     class B {
     *         int i;
     *         A a;
     *
     *         public boolean equals(Object o) {
     *             if (!(o instanceof B)) {
     *                 return false;
     *             }
     *
     *             B that = (B) o;
     *             if (i != that.i) {
     *                 return false;
     *             }
     *
     *             return EqualsCycleBreaker.equalsWithoutCycle(this, that, true, B::equalsPossiblyCyclic);
     *         }
     *
     *         private boolean equalsPossiblyCyclic(B that) {
     *             return this.a.equals(that.a);
     *         }
     *     }
     * </pre>
     *
     * If you now construct a cyclic object tree and call equals on it, it will not explode with a stack overflow:
     * <pre>
     *     A a = new A();
     *     B b = new B();
     *     b.i = 10;
     *     b.a = a;
     *     a.b = b;
     *
     *     b.equals(b); // returns true
     * </pre>
     *
     * @param self The receiver of an invocation to {@link Object#equals(Object)}. E.g. in <code>a.equals(b)</code>, this
     *             parameter is <code>a</code>.
     * @param other The parameter of an invocation to {@link Object#equals(Object)}. E.g. in <code>a.equals(b)</code>, this
     *              parameter is <code>b</code>.
     * @param equalsOnCycle What this method should return when it detects a cycle
     * @param equalityFunction The part of the equality check that can cause cyclic invocations / stack overflows.
     * @return If this method is called in a cycle, returns <code>equalsOnCycle</code>. Otherwise defers to <code>equalityFunction</code>.
     */
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
