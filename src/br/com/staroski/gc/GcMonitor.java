package br.com.staroski.gc;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * This class allows to detect when a object is garbage colleted.<br>
 * Two steps are necessary in order that a class can be notified about the garbage collection of some object:<br>
 * - It need to {@link #addGarbageListener(GcListener) add} an {@link GcListener listener} tho this class;<br>
 * - It need to {@link #bind(Object, Object) bind} the desired object with a key;<br>
 * If this steps are done then when a garbage collection occurs the method {@link GcListener#onGarbageCollected(Object)} will be called receiving the key of the
 * garbage collected object.
 * 
 * @author Ricardo Artur Staroski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class GcMonitor {

    /**
     * A special kind of {@link WeakReference} that holds a key that identifies the garbage collected object.
     */
    private static final class GarbageReference<R> extends WeakReference<R> {

        /**
         * The key associated to the garbage collected object
         */
        final Object key;

        // contructor that receives the key of the object
        GarbageReference(R referent, ReferenceQueue<? super R> queue, Object key) {
            super(referent, queue);
            this.key = key;
        }
    }

    // unique instance of the GcMonitor
    private static final GcMonitor INSTANCE = new GcMonitor();

    /**
     * Retrieves the unique instance of this class.
     * 
     * @return the unique instance of this class.
     */
    public static GcMonitor get() {
        return INSTANCE;
    }

    // listeners that need to be noticed when a object is garbage collected
    private final List<GcListener> listeners = new LinkedList<GcListener>();

    // reference queue where the VM puts the objects being garbage collected
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

    // private constructor to avoid that other classes try to instantiate this one
    private GcMonitor() {
        // this runnable runs forever inside a daemon thread
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                while (true) { // runs forever in a daemon thread
                    try {
                        // get the reference of the object that was garbage collected
                        GarbageReference<Object> garbage = (GarbageReference<Object>) referenceQueue.remove();
                        // get the key for that object
                        Object key = garbage.key;
                        // send the key to the listeners
                        synchronized (listeners) {
                            for (GcListener listener : listeners) {
                                listener.onGarbageCollected(key);
                            }
                        }
                    } catch (Throwable t) { // NOSONAR
                        t.printStackTrace(); // NOSONAR
                    }
                }
            }
        };
        String name = getClass().getSimpleName() + " Monitor Thread";
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Adds a {@link GcListener listener} to this {@link GcMonitor garbage monitor}.
     * 
     * @param listener
     *            The listener to be added.
     * @return This object itself.
     */
    public GcMonitor addGarbageListener(GcListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
        return this;
    }

    /**
     * Binds the specified object with the specified key and returns a {@link WeakReference} of it.
     * 
     * @param object
     *            The object to be binded with the key.
     * @param key
     *            The key of that object.
     * @return A {@link WeakReference} for the specified object.
     */
    public <R, K> WeakReference<R> bind(R object, K key) {
        return new GarbageReference<R>(object, referenceQueue, key);
    }

    /**
     * Removes a {@link GcListener listener} from this {@link GcMonitor garbage monitor}.
     * 
     * @param listener
     *            The listener to be removed.
     * @return This object itself.
     */
    public GcMonitor removeGarbageListener(GcListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
        return this;
    }
}