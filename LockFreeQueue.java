public interface LockFreeQueue {

    public boolean add(T item);

    public T poll();    

    public boolean isEmpty();
}