package mapreduce;

import org.apache.thrift.TException;

/**
 * Same as java.util.function.Function, but allows throwing TException.
 */
@FunctionalInterface
public interface ThriftFunction<T, R> {
    R apply(T t) throws TException;
}
