/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt;

import ass2vtt.Converters.Vtt2TtmlConverter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jesus
 */
public class Vtt2TtmlFormController implements Initializable {

    @FXML
    private TextField syllableColor;
    @FXML
    private TextField defaultColor;
    
    private Vtt2TtmlConverter converter;
    private String[] supportedColors;
    private Stage mainStage;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.supportedColors = new String[]{"^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$", "transparent", "black", "silver", "gray", "white", "maroon", "red", "purple", "fuchsia", "magenta", "green", "lime", "olive", "yellow", "navy", "blue", "teal", "aqua", "cyan"};
    }    
    
    public void initData(Vtt2TtmlConverter converter) {
        this.converter = converter;
    }
    
    @FXML
    public void okay() {
        String sColor = syllableColor.getText();
        String dColor = "".equals(defaultColor.getText()) ? "white" : defaultColor.getText();
        if(!isValidColor(sColor) || !isValidColor(dColor)) {
            showAlert(Alert.AlertType.ERROR, "ERROR", "Color not valid!", "");
            return;
        }
        converter.setSyllableColor(sColor);
        converter.setLineColor(dColor);
        Stage stage = (Stage) defaultColor.getScene().getWindow();
        stage.close();
    }
    
    public void setStage(Stage stage) {mainStage = stage;}
    
     private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
    }
     
    private boolean isValidColor(String color) {
        for(String regrex: supportedColors) {
            if(color.matches(regrex)) return true;
        }
        return false;
    }
}
