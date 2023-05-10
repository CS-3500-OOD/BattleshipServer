package game;

/**
 * You already know what it is
 * @param <T>
 * @param <V>
 */
public class Pair<T, V>{

    private final T key;
    private V val;


    public Pair(T key, V val) {
        this.key = key;
        this.val = val;
    }


    public void put(V val){
        this.val = val;
    }

    public T getKey() {
        return key;
    }

    public V getVal() {
        return val;
    }
}
