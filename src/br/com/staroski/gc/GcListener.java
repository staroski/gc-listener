package br.com.staroski.gc;

/**
 * Objects of this interface are added to a {@link GcMonitor} to receive notifications when objects are garbage collected.<BR>
 * In order to receive a notification of a garbage collected object, that object needs to be {@link GcMonitor#bind(Object, Object) binded} with a key.
 * 
 * @author Ricardo Artur Staroski
 *
 * @param <K>
 *            The data type of the key from the object that was garbage collected.
 */
public interface GcListener<K> {

    /**
     * Called by the {@link GcMonitor} when the object binded to the specified key was garbage collected.
     * 
     * @param key
     *            The key that was {@link GcMonitor#bind(Object, Object) binded} with the object.
     */
    public void onGarbageCollected(K key);
}