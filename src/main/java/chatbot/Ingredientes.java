/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbot;

/**
 *
 * @author willi
 */
public class Ingredientes {
    
    private String ingredientes;
    private String precio;
    public Ingredientes(){
        
    }
    public Ingredientes(String ingredientes, String precio) {
        this.ingredientes = ingredientes;
        this.precio = precio;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }
}
