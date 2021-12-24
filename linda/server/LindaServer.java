package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import linda.AsynchronousCallback;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

/**
 * Public interface to a Linda implementation.
 * 
 * @author philippe.queinnec@enseeiht.fr
 */
public interface LindaServer extends Remote {

    /** Adds a tuple t to the tuplespace. */
    public void write(Tuple t) throws RemoteException;

    /**
     * Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found.
     * 
     * @throws RemoteException
     */
    public Tuple take(Tuple template) throws RemoteException;

    /**
     * Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found.
     * 
     * @throws RemoteException
     */
    public Tuple read(Tuple template) throws RemoteException;

    /**
     * Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found.
     * 
     * @throws RemoteException
     */
    public Tuple tryTake(Tuple template) throws RemoteException;

    /**
     * Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found.
     * 
     * @throws RemoteException
     */
    public Tuple tryRead(Tuple template) throws RemoteException;

    /**
     * Returns all the tuples matching the template and removes them from the
     * tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between takeAll and
     * other methods;
     * for instance two concurrent takeAll with similar templates may split the
     * tuples between the two results.
     * 
     * @throws RemoteException
     */
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException;

    /**
     * Returns all the tuples matching the template and leaves them in the
     * tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between readAll and
     * other methods;
     * for instance (write([1]);write([2])) || readAll([?Integer]) may return only
     * [2].
     * 
     * @throws RemoteException
     */
    public Collection<Tuple> readAll(Tuple template) throws RemoteException;

    /**
     * Registers a callback which will be called when a tuple matching the template
     * appears.
     * If the mode is Take, the found tuple is removed from the tuplespace.
     * The callback is fired once. It may re-register itself if necessary.
     * If timing is immediate, the callback may immediately fire if a matching tuple
     * is already present; if timing is future, current tuples are ignored.
     * Beware: a callback should never block as the calling context may be the one
     * of the writer (see also {@link AsynchronousCallback} class).
     * Callbacks are not ordered: if more than one may be fired, the chosen one is
     * arbitrary.
     * Beware of loop with a READ/IMMEDIATE re-registering callback !
     *
     * @param mode     read or take mode.
     * @param timing   (potentially) immediate or only future firing.
     * @param template the filtering template.
     * @param callback the callback to call if a matching tuple appears.
     * @throws RemoteException
     */
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, RemoteCallback remoteCallback)
            throws RemoteException;

}
