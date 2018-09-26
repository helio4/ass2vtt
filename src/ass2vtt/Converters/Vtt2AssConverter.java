/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt.Converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *
 * @author jesus
 */
public class Vtt2AssConverter implements iConverter {

    @Override
    public String convert(File file) throws FileNotFoundException, UnsupportedEncodingException {
        String res = "[Events]\nFormat: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n";
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
                            text += "\\N" + aux;
                        }
                    }
                    res += "Dialogue: 0," + convertTime(splitedLine[0]) + "," + convertTime(splitedLine[2]) + ",Default,,0,0,0,," + convertLine(splitedLine[0], splitedLine[2], text) + "\n";
                }
            }
        scanner.close();
        return res;
    }
    
    private String convertTime(String time) {
        return time.substring(1, time.length() - 1);
    }
    
    private String convertLine(String start, String end, String line) {
        if (!line.matches(".*<0?\\d?:?\\d\\d:\\d\\d.\\d\\d\\d>.*")) return line;
        String res = "";
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
        for(index = 0; index < timeMarks.length; index++) {
            String ms = differenceInMillis(start, timeMarks[index]);
            res += "{\\k" + ms.substring(0, ms.length() - 1) + "}" + text[index];
            start = timeMarks[index];
        }
        String ms = differenceInMillis(timeMarks[timeMarks.length - 1], end);
        res += "{\\k" + ms.substring(0, ms.length() - 1) + "}" + text[text.length - 1];
        return res;
    }
    
    private String differenceInMillis(String start, String end) {
        return (timeInMillis(end) - timeInMillis(start)) + "";
    }
    
    private int timeInMillis(String time) {
        int res = 0;
        String[] timeS = time.replace(".", ":").split(":");
        int[] timeI = new int[timeS.length];
        for(int i = 0; i < timeS.length; i++) {
            timeI[i] = Integer.parseInt(timeS[i]);
        }
        if(timeI.length == 3) {
            res += timeI[2];
            res += timeI[1] * 1000;
            res += timeI[0] * 60 * 1000;
        } else if(timeI.length == 4) {
            res += timeI[3];
            res += timeI[2] * 1000;
            res += timeI[1] * 60 * 1000;
            res += timeI[0] * 60 * 60 * 1000;
        }
        return res;
    }
}
