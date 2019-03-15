package chatbot;

public class Producto {
    private String tipo;
    private String precio;
    
    public Producto(){
        
    }
    
    public Producto(String tipo, String precio) {
        this.tipo = tipo;
        this.precio = precio;
    }
    
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }
    
    
}
