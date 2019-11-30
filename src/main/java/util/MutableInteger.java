package util;


/**
 * Class used to store an Integer object that is mutable after creation.
 */
public class MutableInteger {
    private int value;

    public MutableInteger(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int intValue() {
        return this.value;
    }

}
