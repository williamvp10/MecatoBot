package chatbot;

public class Tiendas1 {

    private String id;
    private String nombre;
    private String direccion;
    private String url;
    private String telefono;

    public Tiendas1() {
    }

    public Tiendas1(String id, String nombre, String direccion, String url, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.url = url;
        this.telefono = telefono;
    }

    public String getid() {
        return id;
    }

    public String getnombre() {
        return nombre;
    }

    public String getdireccion() {
        return direccion;
    }

    public String geturl() {
        return url;
    }

    public String gettelefono() {
        return telefono;
    }

    public void setid(String id) {
        this.id = id;
    }

    public void setnombre(String nombre) {
        this.nombre = nombre;
    }

    public void setdireccion(String direccion) {
        this.direccion = direccion;
    }

    public void seturl(String url) {
        this.url = url;
    }

    public void settelefono(String telefono) {
        this.telefono = telefono;
    }
}
