/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dit2_a4;

import entidades.Provincia;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author Usuario
 */
public class DIT2_A4 {

    public static void main(String[] args) {
        //Creamos la conexion con la base de datos.
        EntityManagerFactory emf=Persistence.createEntityManagerFactory("DIT2_A4PU");
        EntityManager em=emf.createEntityManager();
        
        em.getTransaction().begin();
        Query queryProvincias = em.createNamedQuery("Provincia.findAll");
        List<Provincia> listProvincias = queryProvincias.getResultList();
        em.getTransaction().commit();
        
        for(int i=0;i<listProvincias.size();i++){
            Provincia provincia=listProvincias.get(i);
            System.out.println(provincia.getNombre());
        }
        
        em.close();
        emf.close();
        try{
            DriverManager.getConnection("jdbc:derby:BDAgenda;shutdown=true");
        } catch (SQLException ex){}
    }
    
}
