/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt.Converters;

import ass2vtt.FileConverterController;
import ass2vtt.Vtt2TtmlFormController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author jesus
 */
public class Vtt2TtmlConverter implements iConverter {
    
    private String syllableColor = "red", lineColor = "#ffffee";
    
    @Override
    public String convert(File file) throws FileNotFoundException, UnsupportedEncodingException, Exception {
        //Calling the Form
        System.out.println(getClass().getResource("/ass2vtt/Vtt2TtmlForm.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ass2vtt/Vtt2TtmlForm.fxml"));
        Parent root = (Parent)loader.load();
        Vtt2TtmlFormController controller = (Vtt2TtmlFormController)loader.getController();
        Stage stage = new Stage();
        controller.setStage(stage);
        
        Scene scene = new Scene(root);
        
        stage.setTitle("vtt to ttml options form");
        stage.setScene(scene);
        controller.initData(this);
        stage.showAndWait();
        
        //Actual conversion
        String res = "<body>\n<div>\n";
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name());
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.matches("0?\\d?:?\\d\\d:\\d\\d.\\d\\d\\d --> 0?\\d?:?\\d\\d:\\d\\d.\\d\\d\\d.*")) {
                String[] splitedLine = line.split(" "); 
                String text = "";
                if(scanner.hasNextLine()) {
                    String aux = scanner.nextLine();
                    text += aux;
                    while(!aux.equals("") && scanner.hasNextLine()) {
                        aux = scanner.nextLine();
                        if(aux.equals("")) break;
                        text += "<br/>" + aux;
                    }
                }
                res += convertLine(text, splitedLine[0], splitedLine[2]) + "\n";
            }
        }
        res += "</div>\n</body>";
        scanner.close();
        return res;
    }
    
    private String convertLine(String line, String start, String end) {
        String res = "";
        if(line.matches(".*<0?\\d?:?\\d\\d:\\d\\d.\\d\\d\\d>.*")) {
            String currentTime = start;
            String[] text = line.split("<0?\\d?:?\\d\\d:\\d\\d.\\d\\d\\d>");
            String[] timeMarks = new String[text.length - 1];
            int index = 0;
            while(!line.isEmpty() && index < timeMarks.length) {
                if(line.matches("<0?\\d?:?\\d\\d:\\d\\d.\\d\\d\\d>.*")) {
                    timeMarks[index++] = line.substring(1, line.indexOf('>'));
                    line = line.substring(line.indexOf('>') + 1);
                } else {
                    line = line.substring(1);
                }
            }
            for(index = 0; index < text.length; index++) {
                String[] leftSide = Arrays.copyOfRange(text, 0, index + 1);
                String[] rightSide = Arrays.copyOfRange(text, index + 1, text.length);
                String wholeText = String.join("", text);
                String additionalInvisibleCharacters = "";
                for(int i = 30 - wholeText.length(); i > 0; i--) {
                    additionalInvisibleCharacters += "​";
                }
                if(index == text.length - 1 && (currentTime == null ? end != null : !currentTime.equals(end))) {
                    res += "<p begin=\"" + currentTime + "\" end=\"" + end + "\" tts:origin=\"100% 0%\"><span tts:color=\"" + lineColor + "\">​" + additionalInvisibleCharacters + "<span tts:color=\"" + syllableColor + "\">";
                    res += String.join("", leftSide) +"</span>" + String.join("", rightSide) + "</span></p>\n";
                } else if(currentTime == null ? timeMarks[index] != null : !currentTime.equals(timeMarks[index])) {
                    res += "<p begin=\"" + currentTime + "\" end=\"" + timeMarks[index] + "\" tts:origin=\"100% 0%\"><span tts:color=\"" + lineColor + "\">​" + additionalInvisibleCharacters + "<span tts:color=\"" + syllableColor + "\">";
                    currentTime = timeMarks[index];
                    res += String.join("", leftSide) +"</span>" + String.join("", rightSide) + "</span></p>\n";
                }
            }
        } else {
            String wholeText = line.replaceAll("<(.*?)>", "");
            String additionalInvisibleCharacters = "";
            for(int i = 30 - wholeText.length(); i > 0; i--) {
                additionalInvisibleCharacters += "​";
            }
            res += "<p begin=\"" + start + "\" end=\"" + end + "\" tts:origin=\"-100% 14%\"><span tts:fontStyle=\"italic\">" + additionalInvisibleCharacters + line + "</span></p>\n";
        }
        return res;
    }
    
    public void setSyllableColor(String color) {
        this.syllableColor = color;
    }
    
    public void setLineColor(String color) {
        this.lineColor = color;
    }
}
