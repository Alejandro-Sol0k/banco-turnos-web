public class Cola<T> {
    private NodoCola<T> cabeza;
    private NodoCola<T> fondo;
    private int tamanio;

    public Cola() {
        cabeza = null;
        fondo = null;
        tamanio = 0;
    }

    public boolean vacia() {
        return cabeza == null;
    }

    public void insertar(T valor) {
        NodoCola<T> nuevo = new NodoCola<>();
        nuevo.setInfo(valor);
        nuevo.setSig(null);
        if (vacia()) {
            cabeza = nuevo;
            fondo = nuevo;
        } else {
            fondo.setSig(nuevo);
            fondo = nuevo;
        }
        tamanio++;
    }

    public T extraer() {
        if (vacia()) {
            return null;
        }
        T valor = cabeza.getInfo();
        if (cabeza == fondo) {
            cabeza = null;
            fondo = null;
        } else {
            cabeza = cabeza.getSig();
        }
        tamanio--;
        return valor;
    }

    public T cabeza() {
        if (cabeza == null) {
            return null;
        }
        return cabeza.getInfo();
    }

    public int tamanio() {
        return tamanio;
    }

    public void clear() {
        cabeza = null;
        fondo = null;
        tamanio = 0;
    }

    public void recorrer(Visitante<T> visitante) {
        NodoCola<T> actual = cabeza;
        while (actual != null) {
            visitante.aceptar(actual.getInfo());
            actual = actual.getSig();
        }
    }
}
