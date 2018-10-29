/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public class Product {
    
    private String id;
    private String nom_produc;
    private String ingredientes;

    public Product(String id, String nom_produc, String ingredientes) {
        this.id = id;
        this.nom_produc = nom_produc;
        this.ingredientes = ingredientes;
    }
    public Product(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom_produc() {
        return nom_produc;
    }

    public void setNom_produc(String nom_produc) {
        this.nom_produc = nom_produc;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }


    
}
