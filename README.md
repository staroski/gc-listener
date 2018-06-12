# gc-listener
Java utility to listen for garbage collection of an object.

Suppose that we have the following class and we want to track when some instance of this class is garbage collected:

    package br.com.staroski.gc.example;

    public class MyClass {

        private static int indexCounter;

        public final int indexNumber;

        public MyClass() {
            indexNumber = indexCounter++;
        }

        @Override
        protected void finalize() throws Throwable {
            indexCounter--;
        }

        @Override
        public String toString() {
            return String.format("%s(%d)", getClass().getSimpleName(), indexNumber);
        }
    }

First of all we need an GcListener to be notified when a garbage collection occurs, let's create it:

    GcListener<Integer> gcListener = objectKey -> out.printf("object binded with key %d was garbage collected%n", objectKey);

Then we need to add the GcListener to the GcMonitor:

    GcMonitor gcMonitor = GcMonitor.get(); // it's a singleton class
    gcMonitor.addGcListener(gcListener);

Let's create some instances of MyClass:

    // this list will keep the references of the created objects
    List<MyClass> objects = new LinkedList<>();

    // this list will keep the WeakReferences generated when binding the object with the GcMonitor
    List<Reference<MyClass>> weakReferences = new LinkedList<>();

    out.printf("creating objects%n");
    for (int objectKey = 0; objectKey < 100; objectKey++) {
        // creating an object
        MyClass object = new MyClass();
        objects.add(object);

        // binding that object with a key, so that we can track the garbage collection
        out.printf("binding object %s with key %d%n", object, objectKey);
        WeakReference<MyClass> weakReference = gcMonitor.bind(object, objectKey);
        weakReferences.add(weakReference);
    }

Objects created, now list the objects binded with the WeakReferences to ensure that they exist:

    out.printf("listing objects%n");
    for (Reference<MyClass> weakReference : weakReferences) {
        MyClass object = weakReference.get();
        out.printf("%s%n", object);
    }

Now kill the created objects:

    out.printf("killing objects%n");
    objects = null;
    System.gc(); // on real life it would not be explicity called

Objects created, now list the objects binded with the WeakReferences again, they should all be null at this moment:

    out.printf("listing objects%n");
    for (Reference<MyClass> weakReference : weakReferences) {
        MyClass object = weakReference.get();
        out.printf("%s%n", object); // will be null
    }
That's it.

The following class is the working example:

    package br.com.staroski.gc.example;

    import static java.lang.System.out;

    import java.lang.ref.Reference;
    import java.lang.ref.WeakReference;
    import java.util.LinkedList;
    import java.util.List;

    import br.com.staroski.gc.GcListener;
    import br.com.staroski.gc.GcMonitor;

    public class GcListenerExample {

        public static void main(String[] args) {
            try {
                GcListenerExample example = new GcListenerExample();
                example.execute();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        private void execute() throws Exception {
            // an listener that gets notified when an object is garbage collected
            GcListener<Integer> gcListener = objectKey -> out.printf("object binded with key %d was garbage collected%n", objectKey);

            // the garbage collection monitor
            GcMonitor gcMonitor = GcMonitor.get();
            gcMonitor.addGcListener(gcListener);

            List<MyClass> objects = new LinkedList<>();
            List<Reference<MyClass>> weakReferences = new LinkedList<>();

            out.printf("creating objects%n");
            for (int objectKey = 0; objectKey < 100; objectKey++) {
                // creating an object
                MyClass object = new MyClass();
                objects.add(object);
                // binding that object with a key, so that we can track the garbage collection
                out.printf("binding object %s with key %d%n", object, objectKey);
                WeakReference<MyClass> weakReference = gcMonitor.bind(object, objectKey);
                weakReferences.add(weakReference);
            }

            out.printf("listing objects%n");
            for (Reference<MyClass> weakReference : weakReferences) {
                MyClass object = weakReference.get();
                out.printf("%s%n", object);
            }

            out.printf("killing objects%n");
            objects = null;
            System.gc(); // on real life it would not be explicity called

            out.printf("listing objects%n");
            for (Reference<MyClass> weakReference : weakReferences) {
                MyClass object = weakReference.get();
                out.printf("%s%n", object); // will be null
            }
        }
    }
