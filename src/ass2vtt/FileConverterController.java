/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt;

import ass2vtt.Converters.Ass2VttConverter;
import ass2vtt.Converters.Ass2XmlConverter;
import ass2vtt.Converters.Vtt2AssConverter;
import ass2vtt.Converters.Vtt2TtmlConverter;
import ass2vtt.Converters.iConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author helio4
 */
public class FileConverterController implements Initializable {
    
    @FXML
    private TextField file_field;
    @FXML
    private TextField folder_field;
    @FXML
    private Button select_folder;
    @FXML
    private Button convert;
    
    private File outputDirectory, file;
    
    private iConverter converter;
    
    DirectoryChooser dirChooser = new DirectoryChooser();
    FileChooser fileChooser = new FileChooser();
    
    private Stage mainStage;
    @FXML
    private Button select_file;
    @FXML
    private ToggleGroup outputFormat;
    @FXML
    private CheckBox shift60;
    @FXML
    private CheckBox fadingKaraoke;
    @FXML
    private CheckBox karaokeOnPhone;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        outputFormat.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                 if (outputFormat.getSelectedToggle() != null) {
                    RadioButton selectedRadioButton = (RadioButton) outputFormat.getSelectedToggle();
                    if(selectedRadioButton.getText().equals(".xml")) {
                        shift60.setDisable(false);
                        fadingKaraoke.setDisable(false);
                        karaokeOnPhone.setDisable(false);
                    } else {
                        shift60.setDisable(true);
                        fadingKaraoke.setDisable(true);
                        karaokeOnPhone.setDisable(true);
                    }
                 }
            }
        });
    }    
    
    @FXML
    public void convert() {
        if(file == null) {
            showAlert(AlertType.ERROR, "ERROR: No file selected", "There was an error with the file", "Please select a file to convert");
            return;
        }
        if (outputDirectory == null) {
            showAlert(AlertType.ERROR, "ERROR: No folder selected", "There was an error with the folder", "Please select the folder where the resultand vtt file will be saved");
            return;
        }
        try {
            String sourceFormat = file.getName().substring(file.getName().indexOf('.'));
            RadioButton selected = (RadioButton) outputFormat.getSelectedToggle();
            String targetFormat = selected.getText();
            PrintWriter writer = new PrintWriter(outputDirectory.getAbsoluteFile() + "\\" + file.getName().substring(0, file.getName().indexOf('.')) + targetFormat, StandardCharsets.UTF_8.name());
            converter = rightConverter(sourceFormat, targetFormat);
            if(converter == null) {
                showAlert(AlertType.ERROR, "ERROR: Conversion not supported", "Can't convert from " + sourceFormat + " to " + targetFormat, ""); 
                return;
            }
            writer.print(converter.convert(file));
            writer.close();
        } catch (FileNotFoundException e) {
            showAlert(AlertType.ERROR, "ERROR", "Couldn't find the specified file.", e.toString());
        } catch(UnsupportedEncodingException e) {
            showAlert(AlertType.ERROR, "ERROR", "Encoding not supported, please use UTF_8.", e.toString());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "ERROR", "Something went wrong!", e.toString());
        }
        showAlert(AlertType.INFORMATION, "SUCCESS", "The conversion has been successfully completed!", "");
    }
    
    @FXML
    public void selectFile() {
        fileChooser.setTitle("Choose an .ass or .vtt file");
        ArrayList<String> extensions = new ArrayList();
        extensions.add("*.ass");
        extensions.add("*.vtt");
        fileChooser.getExtensionFilters().add(new ExtensionFilter(".ass, .vtt", extensions));
        File aux = fileChooser.showOpenDialog(mainStage.getScene().getWindow());
        file = aux == null ? file : aux;
        if(file == null) file_field.setText("");
        else file_field.setText(file.getPath());
    }
    
    @FXML
    public void selectFolder() {
        dirChooser.setTitle("Select a directory to save the vtt file");
        File aux = dirChooser.showDialog(mainStage.getScene().getWindow());
        outputDirectory = aux == null ? outputDirectory : aux;
        if(outputDirectory == null) folder_field.setText("");
        else folder_field.setText(outputDirectory.getPath());
    }
    
    private iConverter rightConverter (String source, String target) {
        if (source.equals(".ass")) {
            switch(target){
                case ".vtt": return new Ass2VttConverter();
                case ".ttml": return null;
                case ".xml": return new Ass2XmlConverter(shift60.isSelected(), karaokeOnPhone.isSelected(), fadingKaraoke.isSelected());
            }
        }
        if (source.equals(".vtt")) {
            switch(target){
                case ".ass": return new Vtt2AssConverter();
                case ".ttml": return new Vtt2TtmlConverter();
                case ".xml" : return null;
            }
        }
        return null;
    }
    
    public void setStage(Stage stage) {mainStage = stage;}
    
    public Stage getStage() {return mainStage;}
    
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
    }
}
