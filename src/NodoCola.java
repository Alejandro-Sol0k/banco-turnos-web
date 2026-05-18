public class NodoCola<T> {
    private T info;
    private NodoCola<T> sig;

    public T getInfo() {
        return info;
    }

    public NodoCola<T> getSig() {
        return sig;
    }

    public void setInfo(T info) {
        this.info = info;
    }

    public void setSig(NodoCola<T> sig) {
        this.sig = sig;
    }
}
