package by.bsu.fpmi.dnfp.main.model.factory;

import by.bsu.fpmi.dnfp.main.model.NumerableObject;

import java.util.Map;

/**
 * @author Igor Loban
 */
public interface NumerableObjectFactory<T extends NumerableObject, S extends NumerableObject> {
    T createStub(T source);

    void fillStub(T stub, T source, Map<Integer, T> sourcePool, Map<Integer, S> addingPool);
}
