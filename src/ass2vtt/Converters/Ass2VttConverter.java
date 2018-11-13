/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt.Converters;

import ass2vtt.Timer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *
 * @author jemarhe
 */
public class Ass2VttConverter implements iConverter  {

    @Override
    public String convert(File file) throws FileNotFoundException {
        String res = "WEBVTT\n\n";
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name()); 
            while(scanner.hasNextLine()) {
                //Dialogue line format: "Dialogue: Layer(0), Start(1), End(2), Style(3), Name(4), MarginL(5), MarginR(6), MarginV(7), Effect(8), Text(9)
                String line = scanner.nextLine();
                if(line.startsWith("Dialogue: ")) {
                    line = line.replaceFirst("^Dialogue: ", "");
                    String[] lineData = line.split(",", 10);
                    res += convertTime(lineData[1]) + " --> " + convertTime(lineData[2]) + "\n";
                    res += convertLine(lineData[1], lineData[9]) + "\n\n";
                }
            }
            scanner.close();
        return res;
    }
    
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
                    currentTime.add(ms * 10);
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
    
    private String convertTime(String time) {
        return "0" + time + "0";
    }
}

