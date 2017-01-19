package org.everit.json.schema;

import com.google.common.base.Preconditions;

/**
 * Created by fpeter on 2017. 01. 16..
 */
public abstract class Consumer<T> {

    public static final Consumer EMPTY = new Consumer<Object>() {
        @Override
        public void accept(Object o) {
        }
    };

    public abstract void accept(T t);

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    public final Consumer<T> andThen(final Consumer<? super T> after) {
        Preconditions.checkNotNull(after);
        final Consumer<T> _this = this;
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                _this.accept(t);
                after.accept(t);
            }
        };
    }
}
