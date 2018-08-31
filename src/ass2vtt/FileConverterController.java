/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

    private TextField file_field;
    @FXML
    private TextField folder_field;
    @FXML
    private Button select_folder;
    @FXML
    private Button convert;
    
    private File outputFolder, assFile;
    
    private Stage mainStage;
    @FXML
    private TextArea ass_area;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    public void convert() {
        if (outputFolder == null) {
            showAlert(AlertType.ERROR, "ERROR: No folder selected", "There was an error with the folder", "Please select the folder where the resultand vtt file will be saved");
            return;
        }
        try {
            Scanner scanner = new Scanner(ass_area.getText()); 
            PrintWriter writer = new PrintWriter(outputFolder.getAbsoluteFile() + "\\Ass2VttOutput.vtt", StandardCharsets.UTF_8.name());
            writer.print("WEBVTT\n\n");
            while(scanner.hasNextLine()) {
                //Dialogue line format: "Dialogue: Layer(0), Start(1), End(2), Style(3), Name(4), MarginL(5), MarginR(6), MarginV(7), Effect(8), Text(9)
                String line = scanner.nextLine();
                if(line.startsWith("Dialogue: ")) {
                    line = line.replaceFirst("^Dialogue: ", "");
                    String[] lineData = line.split(",", 10);
                    writer.print(convertTime(lineData[1]) + " --> " + convertTime(lineData[2]) + "\n");
                    writer.print(convertLine(lineData[1], lineData[9]) + "\n\n");
                }
            }
            scanner.close();
            writer.close();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "ERROR", "There was an error while converting", e.toString());
        }
        showAlert(AlertType.INFORMATION, "SUCCESS", "The conversion was successfully completed!", "");
    }
    
    public void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an .ass file");
        fileChooser.getExtensionFilters().add(new ExtensionFilter(".ass", "*.ass"));
        assFile = fileChooser.showOpenDialog(mainStage.getScene().getWindow());
        file_field.setText(assFile.getPath());
    }
    
    @FXML
    public void selectFolder() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select a directory to save the vtt file");
        outputFolder = dirChooser.showDialog(mainStage.getScene().getWindow());
        folder_field.setText(outputFolder.getPath());
    }
    
    public void setStage(Stage stage) {mainStage = stage;}
    
    public Stage getStage() {return mainStage;}

    private String convertLine(String start, String line) {
        if (line.startsWith("{\\")) {
            String res = "";
            Timer currentTime = new Timer(start);
            line = reformatLine(line);
            while(!line.isEmpty()) {
                String aux = "";
                int i = 0;
                if (line.startsWith("{")){
                    for(i = 3; !aux.matches("\\{\\S{2}\\d+\\}"); i++) {
                         aux = line.substring(0, i);
                    }
                }
                if(!aux.equals("")) {
                    aux = aux.substring(3, aux.length() - 1);
                    int ms = Integer.parseInt(aux);
                    currentTime.add(ms);
                    res += "<" + currentTime.toString() + ">";
                    line = line.substring(i - 1);
                } else {
                    res += line.charAt(0);
                    line = line.substring(1);
                }

            }
            return res.replace("\\N", "\n");
        } else {
            return line.replace("\\N", "\n");
        }
    }
    
    private String convertTime(String time) {
        return "0" + time + "0";
    }
    
    private String reformatLine(String line) {
        String res = "";
        String[] syllables = line.split("\\{\\S{2}\\d+\\}");
        String[] timeMarks = new String[syllables.length - 1];
        int index = 0;
        while(!line.isEmpty() && index < timeMarks.length) {
            String aux = "";
            int i = 0;
            if (line.startsWith("{")){
                for(i = 3; !aux.matches("\\{\\S{2}\\d+\\}"); i++) {
                     aux = line.substring(0, i);
                }
            }
            if(!aux.equals("")) {
                timeMarks[index++] = aux;
                line = line.substring(i - 1);
            } else {
                line = line.substring(1);
            }
        }
        timeMarks[timeMarks.length-1] = "";
        for (int i = 0; i < timeMarks.length; i++) {
            res += syllables[i + 1] + timeMarks[i];
        }
        return res;
    }
    
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
    }
}
