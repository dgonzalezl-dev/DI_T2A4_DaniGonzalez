/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dit2_a4;

import entidades.Persona;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class AgendaViewController implements Initializable {

    private Persona personaGuardar;
    private EntityManager entityManager;
    @FXML
    private TableView<Persona> Tabla;
    @FXML
    private TableColumn<Persona,String> Nombre;
    @FXML
    private TableColumn<Persona,String> apellidos;
    @FXML
    private TableColumn<Persona,String> email;
    @FXML
    private TableColumn<Persona,String> provincias;
    @FXML
    private TextField nombre;
    @FXML
    private TextField apellido;
    @FXML
    private AnchorPane total;
    
    public void setEntityManager(EntityManager entityManager){
        this.entityManager=entityManager;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        provincias.setCellValueFactory( cellData->{
            SimpleStringProperty property=new SimpleStringProperty();
            if (cellData.getValue().getProvincia()!=null){
                property.setValue(cellData.getValue().getProvincia().getNombre());
            }
            return property;
        });
        Tabla.getSelectionModel().selectedItemProperty().addListener((observable,oldValue,newValue)->{
            personaGuardar=newValue;
            if (personaGuardar != null){
                nombre.setText(personaGuardar.getNombre());
                apellido.setText(personaGuardar.getApellidos());
            } else{
                nombre.setText("");
                apellido.setText("");
            }
        });
    }    
    
    public void cargarTodasPersonas(){
        Query queryPersonaFindAll=entityManager.createNamedQuery("Persona.findAll");
        List<Persona> listPersona=queryPersonaFindAll.getResultList();
        Tabla.setItems(FXCollections.observableArrayList(listPersona));
    }
    
    @FXML
    public void guardar(){
        if (personaGuardar != null){
            personaGuardar.setNombre(nombre.getText());
            personaGuardar.setApellidos(apellido.getText());
            entityManager.getTransaction().begin();
            entityManager.merge(personaGuardar);
            entityManager.getTransaction().commit();
            int numFilaSeleccionada =Tabla.getSelectionModel().getSelectedIndex();
            Tabla.getItems().set(numFilaSeleccionada,personaGuardar);
        }
    }

    @FXML
    private void nuevo(ActionEvent event) {
        try{
            // Cargar la vista de detalle
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("Formulario.fxml"));
            Parent rootDetalleView=fxmlLoader.load();
            // Ocultar la vista de la lista
            total.setVisible(false);
             //Añadir la vista detalle al StackPane principal para que se muestre
            StackPane rootMain =(StackPane) total.getScene().getRoot();
            rootMain.getChildren().add(rootDetalleView);
            FormularioController personaDetalleViewController = (FormularioController) fxmlLoader.getController();
            personaDetalleViewController.setRootAgendaView(total);
            personaDetalleViewController.setTableViewPrevio(Tabla);
            personaGuardar = new Persona();
            personaDetalleViewController.setPersona(entityManager,personaGuardar,true);
            personaDetalleViewController.mostrarDatos();
        } catch (IOException ex){
            Logger.getLogger(AgendaViewController.class.getName()).log(Level.SEVERE,null,ex);
        }    
    }

    @FXML
    private void editar(ActionEvent event) {
        try{
            // Cargar la vista de detalle
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("Formulario.fxml"));
            Parent rootDetalleView=fxmlLoader.load();
            // Ocultar la vista de la lista
            total.setVisible(false);
             //Añadir la vista detalle al StackPane principal para que se muestre
            StackPane rootMain =(StackPane) total.getScene().getRoot();
            rootMain.getChildren().add(rootDetalleView);
            FormularioController personaDetalleViewController = (FormularioController) fxmlLoader.getController();
            personaDetalleViewController.setRootAgendaView(total);
            personaDetalleViewController.setTableViewPrevio(Tabla);
            personaDetalleViewController.setPersona(entityManager,personaGuardar,false);
            personaDetalleViewController.mostrarDatos();
        } catch (IOException ex){
            Logger.getLogger(AgendaViewController.class.getName()).log(Level.SEVERE,null,ex);
        }
    }

    @FXML
    private void suprimir(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("¿Desea suprimir el siguiente registro?");
        alert.setContentText(personaGuardar.getNombre() + " "+ personaGuardar.getApellidos());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            entityManager.getTransaction().begin();
            entityManager.merge(personaGuardar);
            entityManager.remove(personaGuardar);
            entityManager.getTransaction().commit();
            Tabla.getItems().remove(personaGuardar);
            Tabla.getFocusModel().focus(null);
            Tabla.requestFocus();
        } else {
            int numFilaSeleccionada=
            Tabla.getSelectionModel().getSelectedIndex();
            Tabla.getItems().set(numFilaSeleccionada,personaGuardar);
            TablePosition pos = new TablePosition(Tabla,numFilaSeleccionada,null);
            Tabla.getFocusModel().focus(pos);
            Tabla.requestFocus();
        }
    }  
}
