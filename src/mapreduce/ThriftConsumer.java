package mapreduce;

import org.apache.thrift.TException;

/**
 * Same as java.util.function.Consumer, but allows throwing TException.
 */
@FunctionalInterface
public interface ThriftConsumer<T> {
    void accept(T t) throws TException;
}
