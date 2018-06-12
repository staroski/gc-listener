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
            new GcListenerExample().executar();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void executar() throws Exception {
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
