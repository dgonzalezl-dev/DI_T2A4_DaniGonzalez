/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dit2_a4;

import entidades.Persona;
import entidades.Provincia;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;

/**
 * FXML Controller class
 *
 * @author Usuario
 */
public class FormularioController implements Initializable {

    boolean errorFormato = false;
    public static final String CARPETA_FOTOS = "src/Fotos";
    @FXML
    private GridPane tabla;
    @FXML
    private TextField nombre;
    @FXML
    private TextField apellidos;
    @FXML
    private TextField telefono;
    @FXML
    private TextField email;
    @FXML
    private TextField hijos;
    @FXML
    private TextField salario;
    @FXML
    private ChoiceBox<Provincia> provincia;
    @FXML
    private DatePicker f_nac;
    @FXML
    private RadioButton soltero;
    @FXML
    private RadioButton casado;
    @FXML
    private RadioButton viudo;
    @FXML
    private CheckBox jubilado;
    private Pane rootAgendaView;
    @FXML
    private AnchorPane root;
    private TableView tableViewPrevio;
    private Persona persona;
    private EntityManager entityManager;
    private boolean nuevaPersona;
    public static final char CASADO = 'C';
    public static final char SOLTERO = 'S';
    public static final char VIUDO = 'V';
    @FXML
    private ImageView foto;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void guardar(ActionEvent event) {
        if (!hijos.getText().isEmpty()) {
            try {
                persona.setNumHijos(Short.valueOf(hijos.getText()));
            } catch (NumberFormatException ex) {
                errorFormato = true;
                Alert alert = new Alert(AlertType.INFORMATION, "Número de hijos noválido");
                alert.showAndWait();
                hijos.requestFocus();
            }
        }

        if (!salario.getText().isEmpty()) {
            try {
                persona.setSalario(BigDecimal.valueOf(Double.valueOf(salario.getText()).doubleValue()));
            } catch (NumberFormatException ex) {
                errorFormato = true;
                Alert alert = new Alert(AlertType.INFORMATION, "Salario noválido");
                alert.showAndWait();
                salario.requestFocus();
            }
        }

        persona.setJubilado(jubilado.isSelected());

        if (casado.isSelected()) {
            persona.setEstadoCivil(CASADO);
        } else if (soltero.isSelected()) {
            persona.setEstadoCivil(SOLTERO);
        } else if (viudo.isSelected()) {
            persona.setEstadoCivil(VIUDO);
        }

        if (f_nac.getValue() != null) {
            LocalDate localDate = f_nac.getValue();
            ZonedDateTime zonedDateTime= localDate.atStartOfDay(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            Date date = Date.from(instant);
            persona.setFechaNacimiento(date);
        } else {
            persona.setFechaNacimiento(null);
        }

        if (provincia.getValue() != null) {
            persona.setProvincia(provincia.getValue());
        } else {
            Alert alert = new Alert(AlertType.INFORMATION, "Debe indicar unaprovincia");
            alert.showAndWait();
            errorFormato = true;
        }

        if (!errorFormato) { // Los datos introducidos son correctos
            try {
                StackPane rootMain = (StackPane) root.getScene().getRoot();
                rootMain.getChildren().remove(root);
                rootAgendaView.setVisible(true);
                persona.setNombre(nombre.getText());
                persona.setApellidos(apellidos.getText());
                persona.setTelefono(telefono.getText());
                persona.setEmail(email.getText());
                int numFilaSeleccionada;
                if (nuevaPersona) {
                    tableViewPrevio.getItems().add(persona);
                    numFilaSeleccionada = tableViewPrevio.getItems().size() - 1;
                    tableViewPrevio.getSelectionModel().select(numFilaSeleccionada);
                    tableViewPrevio.scrollTo(numFilaSeleccionada);
                    entityManager.persist(persona);
                } else {
                    entityManager.merge(persona);
                    numFilaSeleccionada = tableViewPrevio.getSelectionModel().getSelectedIndex();
                    tableViewPrevio.getItems().set(numFilaSeleccionada, persona);
                }
                entityManager.getTransaction().commit();
            } catch (RollbackException ex) { // Los datos introducidos no cumplenrequisitos de BD
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("No se han podido guardar los cambios. " + "Compruebe que los datos cumplen losrequisitos");
                alert.setContentText(ex.getLocalizedMessage());
                alert.showAndWait();
            }
        }

    }

    @FXML
    private void cancelar(ActionEvent event) {
        StackPane rootMain = (StackPane) root.getScene().getRoot();
        rootMain.getChildren().remove(root);
        rootAgendaView.setVisible(true);
        entityManager.getTransaction().rollback();
    }

    public void setRootAgendaView(Pane rootAgendaView) {
        this.rootAgendaView = rootAgendaView;
    }

    public void setTableViewPrevio(TableView tableViewPrevio) {
        this.tableViewPrevio = tableViewPrevio;
    }

    public void setPersona(EntityManager entityManager, Persona persona, Boolean nuevaPersona) {
        this.entityManager = entityManager;
        entityManager.getTransaction().begin();
        if (!nuevaPersona) {
            this.persona = entityManager.find(Persona.class, persona.getId());
        } else {
            this.persona = persona;
        }
        this.nuevaPersona = nuevaPersona;
    }

    public void mostrarDatos() {
        nombre.setText(persona.getNombre());
        apellidos.setText(persona.getApellidos());
        telefono.setText(persona.getTelefono());
        email.setText(persona.getEmail());
        if (persona.getNumHijos() != null) {
            hijos.setText(persona.getNumHijos().toString());
        }
        if (persona.getSalario() != null) {
            salario.setText(persona.getSalario().toString());
        }
        if (persona.getJubilado() != null) {
            jubilado.setSelected(persona.getJubilado());
        }
        if (persona.getEstadoCivil() != null) {
            switch (persona.getEstadoCivil()) {
                case CASADO:
                    casado.setSelected(true);
                    break;
                case SOLTERO:
                    soltero.setSelected(true);
                    break;
                case VIUDO:
                    viudo.setSelected(true);
                    break;
            }
        }
        if (persona.getFechaNacimiento() != null) {
            Date date = persona.getFechaNacimiento();
            Instant instant = date.toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            LocalDate localDate = zdt.toLocalDate();
            f_nac.setValue(localDate);
        }
        Query queryProvinciaFindAll = entityManager.createNamedQuery("Provincia.findAll");
        List listProvincia = queryProvinciaFindAll.getResultList();
        provincia.setItems(FXCollections.observableList(listProvincia));
        if (persona.getProvincia() != null) {
            provincia.setValue(persona.getProvincia());
        }
        provincia.setConverter(new StringConverter<Provincia>() {
            @Override
            public String toString(Provincia provincia) {
                if (provincia == null) {
                    return null;
                } else {
                    return provincia.getCodigo() + "-" + provincia.getNombre();
                }
            }

            @Override
            public Provincia fromString(String userId) {
                return null;
            }
        });
        if (persona.getFoto() != null) {
            String imageFileName = persona.getFoto();
            File file = new File(CARPETA_FOTOS + "/" + imageFileName);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                foto.setImage(image);
            } else {
                Alert alert = new Alert(AlertType.INFORMATION, "No se encuentra la imagen en " + file.toURI().toString());
                alert.showAndWait();
            }
        }
    }
    
    @FXML
    private void onActionButtonExaminar(ActionEvent event) {
        File carpetaFotos = new File(CARPETA_FOTOS);
        if (!carpetaFotos.exists()) {
            carpetaFotos.mkdir();
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes (jpg, png)", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                Files.copy(file.toPath(), new File(CARPETA_FOTOS + "/" + file.getName()).toPath());
                persona.setFoto(file.getName());
                Image image = new Image(file.toURI().toString());
                foto.setImage(image);
            } catch (FileAlreadyExistsException ex) {
                Alert alert = new Alert(AlertType.WARNING, "Nombre de archivo duplicado");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(AlertType.WARNING, "No se ha podido guardar la imagen");
                alert.showAndWait();
                ex.printStackTrace();
            }
        }
    }
    
    @FXML
    private void onActionButtonSuprimirFoto(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar supresión de imagen");
        alert.setHeaderText("¿Desea SUPRIMIR el archivo asociado a la imagen, \n"
                + "quitar la foto pero MANTENER el archivo, \no CANCELAR la operación?");
        alert.setContentText("Elija la opción deseada:");

        ButtonType buttonTypeEliminar = new ButtonType("Suprimir");
        ButtonType buttonTypeMantener = new ButtonType("Mantener");
        ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeEliminar, buttonTypeMantener, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeEliminar) {
            String imageFileName = persona.getFoto();
            File file = new File(CARPETA_FOTOS + "/" + imageFileName);
            if (file.exists()) {
                file.delete();
            }
            persona.setFoto(null);
            foto.setImage(null);
        } else if (result.get() == buttonTypeMantener) {
            persona.setFoto(null);
            foto.setImage(null);
        }
    }
}
