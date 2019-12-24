package util;

import java.io.Serializable;

/**
 * A generic class for making immutable pairs.
 * @param <L> The class of the left value of the Pair.
 * @param <R> The class of the right value of the Pair.
 */
public class Pair<L,R> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The left value of the Pair.
     */
    private L left;

    /**
     * The right value of the pair.
     */
    private R right;

    /**
     * A constructor for creating a Pair with a given left and right value.
     * @param left The left value of the Pair.
     * @param right The right value of the Pair.
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left value of the Pair.
     * @return The left value of the Pair.
     */
    public L getLeft() { return left; }
    /**
     * Returns the right value of the Pair.
     * @return The right value of the Pair.
     */
    public R getRight() { return right; }

    /**
     * Sets the left value of the Pair.
     * @param left The left value of the Pair.
     */
    public void setLeft(L left) {
        this.left = left;
    }

    /**
     * Sets the right value of the Pair.
     * @param right The right value of the Pair.
     */
    public void setRight(R right) {
        this.right = right;
    }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.left.equals(pairo.getLeft()) &&
                this.right.equals(pairo.getRight());
    }

    @Override
    public String toString(){
        return "(" + getLeft()+ " ," + getRight() + ")";
    }

}
