/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbot;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 *
 * @author willi
 */
public class Pedido {

    @SerializedName("id")
    private String usuario;
    @SerializedName("tipo")
    private String tipo;
    @SerializedName("ingredientes")
    private ArrayList<String> ingredientes;
    @SerializedName("tienda")
    private String tienda;

    public Pedido() {
        this.usuario = "";
        this.tipo = "";
        this.ingredientes = new ArrayList<>();
        this.tienda = "";
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes.clear();
        String[] in = ingredientes.split(",");
        for (int i = 1; i < in.length; i++) {
            this.ingredientes.add(in[i]);
        }
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public String getUsuario() {
        return usuario;
    }

    public ArrayList<String> getIngredientes() {
        return ingredientes;
    }

    public String getTienda() {
        return tienda;
    }

}
