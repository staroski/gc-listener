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
