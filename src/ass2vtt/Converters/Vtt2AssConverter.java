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
                    line = line.replace(" ", "");
                    String[] times = line.split("-->");
                    String text = "";
                    int numLines = 1;
                    while(scanner.hasNext()) {
                        String aux = scanner.nextLine();
                        if ("".equals(aux)) break;
                        text += aux + "\\N";
                    }
                    res += "Dialogue: 0," + convertTime(times[0]) + "," + convertTime(times[1]) + ",Default,,0,0,0,," + text + "\n";
                }
            }
        scanner.close();
        return res;
    }
    
    private String convertTime(String time) {
        return time.substring(1, time.length() - 1);
    }
}
