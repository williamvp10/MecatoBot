package chatbot;

public class Producto1 {

    private String tipo;
    private String precio;

    public Producto1() {
    }

    public Producto1(String tipo, String precio) {
        this.tipo = tipo;
        this.precio = precio;
    }

    public String gettipo() {
        return tipo;
    }

    public String getprecio() {
        return precio;
    }

    public void settipo(String tipo) {
        this.tipo = tipo;
    }

    public void setprecio(String precio) {
        this.precio = precio;
    }
}
