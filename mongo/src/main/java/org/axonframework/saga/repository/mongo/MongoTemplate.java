package org.axonframework.saga.repository.mongo;

import com.mongodb.DBCollection;

/**
 * <p>Generic template for accessing Mongo for the axon sagas.</p>
 * <p/>
 * <p>You can ask for the collection of domain events as well as the collection of sagas and association values. We
 * use the mongo client mongo-java-driver. This is a wrapper around the standard mongo methods. For convenience the
 * interface also gives access to the database that contains the axon saga collections.</p>
 * <p/>
 * <p>Implementations of this interface must provide the connection to Mongo.</p>
 *
 * @author Jettro Coenradie
 * @since 2.0
 */
public interface MongoTemplate {
    /**
     * Returns a reference to the collection containing the saga instances.
     *
     * @return DBCollection containing the sagas
     */
    DBCollection sagaCollection();

}