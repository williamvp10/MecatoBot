/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

/**
 *
 * @author Carlos Alberto
 */
import VO.Producto;
import VO.Tienda;
import java.util.ArrayList;

public class test {

    public static void main(String[] args) {

        ArrayList<Producto> prods= new ArrayList<>();
        ArrayList<String> tipos= new ArrayList<>();
        ArrayList<String> ingredientes1= new ArrayList<>();
        ArrayList<String> ingredientes2= new ArrayList<>();
        ArrayList<Producto> ingpizza= new ArrayList<>();
        ArrayList<Producto> ingham= new ArrayList<>();
        
        Tienda t = new Tienda();
        Tienda t2 = new Tienda();
        Tienda t3 = new Tienda();

        t.setId("1");
        t2.setId("2");
        t3.setId("3");
        
        
        Producto p = new Producto();
        Producto p2 = new Producto();
        Producto p3 = new Producto();
        Producto p4 = new Producto();
        Producto p5 = new Producto();
        Producto p6 = new Producto();
        
        p.setTipo("pizza");
        p2.setTipo("pizza");
        p3.setTipo("pizza");
        p4.setTipo("pizza");
        p5.setTipo("Hamburguesa");
        p6.setTipo("Hamburguesa");
        
        p.setIngredientes("Queso,masa,champiñones,pollo,bocadillo");
        p2.setIngredientes("Queso,masa,piña,bocadillo");
        p3.setIngredientes("Queso,masa,Tomates,oregano,bocadillo");
        p4.setIngredientes("Queso,masa,pollo,bocadillo,carne,salchichon");
        p5.setIngredientes("pan,queso,cebolla,salsa especial,tocino,carne");
        p6.setIngredientes("pan,queso,carne,aros de cebolla,salsa de la casa, lechuga");
        
        prods.add(p);
        prods.add(p2);
        prods.add(p3);
        prods.add(p4);
        prods.add(p5);
        prods.add(p6);
        
        
        ingpizza.add(p);
        ingpizza.add(p2);
        ingpizza.add(p3);
        ingpizza.add(p4);
        
        ingham.add(p5);
        ingham.add(p6);
        
        productoDAO dao = new productoDAO();
        
        tipos = dao.obtenerTipos(prods);
        ingredientes1 = dao.obtenerIngredientes(ingpizza);
        ingredientes2 = dao.obtenerIngredientes(ingham);
        
        
        
        
        
        System.out.println("/////////////////TIPOS////////////////////////");
        for (int i = 0; i < tipos.size(); i++) {
            System.out.println(tipos.get(i));
        }
        System.out.println("///////////////////////////////////////////////");
        System.out.println(" ");
        System.out.println("/////////////////ING PIZZAS////////////////////////");
        for (int i = 0; i < ingredientes1.size(); i++) {
            System.out.println(ingredientes1.get(i));
        }
        System.out.println("///////////////////////////////////////////////");
        System.out.println(" ");
        System.out.println("/////////////////ING HAM////////////////////////");
        for (int i = 0; i < ingredientes2.size(); i++) {
            System.out.println(ingredientes2.get(i));
        }
        
    
    }

}
