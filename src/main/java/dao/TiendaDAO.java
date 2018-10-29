/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;


 
import Modelo.Tienda;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
 
public class TiendaDAO {
 
    private static final Map<String, Tienda> TieMap = new HashMap<String, Tienda>();
 
    static {
        initTiendas();
    }
 
    private static void initTiendas() {
        Tienda t1 = new Tienda("1", "Domino's pizza");
        Tienda t2 = new Tienda("2", "Papa johns");
        Tienda t3 = new Tienda("3", "Jenos pizza");
 
        TieMap.put(t1.getId(), t1);
        TieMap.put(t2.getId(), t2);
        TieMap.put(t3.getId(), t3);
    }
 
    public static Tienda getTienda(String id) {
        return TieMap.get(id);
    }
 
    public static Tienda addTienda(Tienda p) {
        TieMap.put(p.getId(), p);
        return p;
    }
 
    public static Tienda updateTienda(Tienda p) {
        TieMap.put(p.getId(), p);
        return p;
    }
 
    public static void deleteTienda(String id) {
        TieMap.remove(id);
    }
 
    public static List<Tienda> getAllTiendas() {
        Collection<Tienda> c = TieMap.values();
        List<Tienda> list = new ArrayList<Tienda>();
        list.addAll(c);
        return list;
    }
     
    List<Tienda> list;
 
}
