package util;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;

public class ListHelper {

    /**
     * get the last value of the list
     * @param list the list
     * @param <N> Type of elements contained in the list
     * @return the last element if at list one is present
     * @throws NoSuchElementException is the list is empty
     */
    public static <N> N getLast(@NotNull List<N> list) {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(list.size()-1);
    }
}
