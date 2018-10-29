/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;


 
import Modelo.Product;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
 
public class ProductDAO {
 
    private static final Map<String, Product> empMap = new HashMap<String, Product>();
 
    static {
        initEmps();
    }
 
    private static void initEmps() {
        Product p1 = new Product("1", "pizza de carne", "Peperoni,Queso,Salsa de tomate,Carne molida,Jamon, Salchichas");
        Product p2 = new Product("2", "pizza de pollo", "Pollo, Salsa marinara, Champiñones, Queso");
        Product p3 = new Product("3", "pizza mexicana", "Queso, Carne molida, Pollo, Tostacos, Salsa marinara, Tomate, Cebolla, Pimenton, Pimienta");
 
        empMap.put(p1.getId(), p1);
        empMap.put(p2.getId(), p2);
        empMap.put(p3.getId(), p3);
    }
 
    public static Product getProduct(String empNo) {
        return empMap.get(empNo);
    }
 
    public static Product addProduct(Product p) {
        empMap.put(p.getId(), p);
        return p;
    }
 
    public static Product updateProduct(Product p) {
        empMap.put(p.getId(), p);
        return p;
    }
 
    public static void deleteProduct(String id) {
        empMap.remove(id);
    }
 
    public static List<Product> getAllProducts() {
        Collection<Product> c = empMap.values();
        List<Product> list = new ArrayList<Product>();
        list.addAll(c);
        return list;
    }
     
    List<Product> list;
 
}
