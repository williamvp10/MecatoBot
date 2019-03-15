package chatbot;

public class Ingredientes1 {

    private String ingredientes;
    private String precio;

    public Ingredientes1() {
    }

    public Ingredientes1(String ingredientes, String precio) {
        this.ingredientes = ingredientes;
        this.precio = precio;
    }

    public String getingredientes() {
        return ingredientes;
    }

    public String getprecio() {
        return precio;
    }

    public void setingredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public void setprecio(String precio) {
        this.precio = precio;
    }
}
