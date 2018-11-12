/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt.Converters;

import ass2vtt.Timer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author jesus
 */
public class Ass2XmlConverter implements iConverter {
    
    private Map<String, String[]> styles = new HashMap();
    private final Map<String, String> supportedFonts = new HashMap(){
        {
            put("Courier New", "1");
            put("Times New Roman", "2");
            put("Deja Vu Sans Mono", "3");
            put("Comic Sans MS", "5");
            put("Monotype Corsiva", "6");
            put("Carrois Gothic SC", "7");
        }
    };
    private int lastId = 9;
    
    @Override
    public String convert(File file) throws FileNotFoundException, UnsupportedEncodingException, Exception {
        String res = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<timedtext format=\"3\">\n";
        String stylePart = "<head>\n\n<wp id=\"1\" ap=\"6\" ah=\"0\" av=\"100\" />\n" +
                        "<wp id=\"2\" ap=\"7\" ah=\"50\" av=\"100\" />\n" +
                        "<wp id=\"3\" ap=\"8\" ah=\"100\" av=\"100\" />\n" +
                        "<wp id=\"4\" ap=\"3\" ah=\"0\" av=\"50\" />\n" +
                        "<wp id=\"5\" ap=\"4\" ah=\"50\" av=\"50\" />\n" +
                        "<wp id=\"6\" ap=\"5\" ah=\"100\" av=\"50\" />\n" +
                        "<wp id=\"7\" ap=\"0\" ah=\"0\" av=\"0\" />\n" +
                        "<wp id=\"8\" ap=\"1\" ah=\"50\" av=\"0\" />\n" +
                        "<wp id=\"9\" ap=\"2\" ah=\"100\" av=\"0\" />\n\n";
        String subsPart = "<body>\n\n";
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name()); 
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //Style line format: Name(0), Fontname(1), Fontsize(2), PrimaryColour(3), SecondaryColour(4), OutlineColour(5), BackColour(6), Bold(7), Italic(8), Underline(9), StrikeOut(10), ScaleX(11), ScaleY(12), Spacing(13), Angle(14), BorderStyle(15), Outline(16), Shadow(17), Alignment(18), MarginL(19), MarginR(20), MarginV(21), Encoding(22)
                if(line.startsWith("Style: ")) {
                    line = line.replaceFirst("^Style: ", "");
                    String[] styeInfo = line.split(",", 23);
                    stylePart += convertStyle(styeInfo) + "\n";
                }
                //Dialogue line format: "Dialogue: Layer(0), Start(1), End(2), Style(3), Name(4), MarginL(5), MarginR(6), MarginV(7), Effect(8), Text(9)
                if(line.startsWith("Dialogue: ")) {
                    line = line.replaceFirst("^Dialogue: ", "");
                    String[] lineData = line.split(",", 10);
                    subsPart += convertLine(lineData[1], lineData[2], lineData[9], lineData[3]) + "\n";
                }
            }
            stylePart += "</head>\n";
            subsPart += "</body>\n";
            res += stylePart + subsPart + "</timedtext>";
            scanner.close();
        return res;
    }
    
    private String convertLine(String start, String end, String line, String style) {
        String[] styleIDs = styles.get(style);
        String res = "";
        Timer startTimer = new Timer(start);
        Timer endTimer = new Timer(end);
        if(!line.matches(".*\\{\\\\k\\d*\\}.*")) {
            int duration = endTimer.getTotalMillis() - startTimer.getTotalMillis();
            res +="<p t=\"" + startTimer.getTotalMillis() + "\" d=\"" + duration + "\" wp=\"" + styleIDs[2] + "\"><s p=\"" + styleIDs[0] + "\">" + line + "</s></p>\n\n";
        } else {
            Timer currentTimer = startTimer;
            String[] text = line.split("\\{\\\\k\\d*\\}");
            text = Arrays.copyOfRange(text, 1, text.length);
            String[] timeMarks = new String[text.length];
            int index = 0;
            while(!line.isEmpty() && index < timeMarks.length) {
                if(line.matches("\\{\\\\k\\d*\\}.*")) {
                    timeMarks[index++] = line.substring(3, line.indexOf('}'));
                    line = line.substring(line.indexOf('}') + 1);
                } else {
                    line = line.substring(1);
                }
            }
            for(index = 0; index < text.length; index++) {
                String[] leftSide = Arrays.copyOfRange(text, 0, index + 1);
                String[] rightSide = Arrays.copyOfRange(text, index + 1, text.length);
                res += "<p t=\"" + currentTimer.getTotalMillis() + "\" d=\"" + timeMarks[index] + "0\" wp=\"" + styleIDs[2] + "\"><s p=\"" + styleIDs[0] + "\">"+ String.join("", leftSide) + "</s>&#8203;<s p=\"" + styleIDs[1] + "\">" + String.join("", rightSide) + "</s></p>\n";;
                currentTimer.add(Integer.parseInt(timeMarks[index] + "0"));
            }
        }
        return res.replace("\\N", "\n");
    }
    
    private String  convertStyle(String[] styleInfo) {
        //Style line format: Name(0), Fontname(1), Fontsize(2), PrimaryColour(3), SecondaryColour(4), OutlineColour(5), BackColour(6), Bold(7), Italic(8), Underline(9), StrikeOut(10), ScaleX(11), ScaleY(12), Spacing(13), Angle(14), BorderStyle(15), Outline(16), Shadow(17), Alignment(18), MarginL(19), MarginR(20), MarginV(21), Encoding(22)
        String name = styleInfo[0];
        String res = "<!-- " + name + " -->\n";
        String font = supportedFonts.containsKey(styleInfo[1]) ? supportedFonts.get(styleInfo[1]) : "4";
        String primaryColour = hexToASS(styleInfo[3].substring(4));
        String secondaryColour = hexToASS(styleInfo[4].substring(4));
        String edgeColor = hexToASS(styleInfo[5].substring(4));
        String backColour = hexToASS(styleInfo[6].substring(4));
        String bold = styleInfo[7].replace("-", "");
        String italic = styleInfo[8].replace("-", "");
        String underline = styleInfo[9].replace("-", "");
        boolean background = styleInfo[15].equals("3");
        int foregroundOpacity = hexToOpacity(styleInfo[3].substring(2, 4));
        int secondaryOpacity = hexToOpacity(styleInfo[4].substring(2, 4));
        int backgroundOpacity = hexToOpacity(styleInfo[6].substring(2, 4));
        int edgeOpacity = hexToOpacity(styleInfo[5].substring(2, 4));
        String alignment = styleInfo[18];
        String bgInfo = background ? "bc=\"" + backColour + "\" bo=\"" + backgroundOpacity + "\""  : "bo=\"0\"";
        String edgeInfo = edgeOpacity > 500 ? "et=\"3\" ec=\"" + edgeColor + "\"" : "";
        //Primary Style
        styles.put(name, new String[]{Integer.toString(lastId + 1), Integer.toString(lastId + 2), alignment});
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + primaryColour + "\" fo=\"" + foregroundOpacity + "\" " + bgInfo + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Karaoke Style
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + secondaryColour + "\" fo=\"" + secondaryOpacity + "\" " + bgInfo + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Alignment WP
        return res;
    }
    
    private int hexToOpacity(String hex) {
        int ass = Integer.decode("0x" + hex);
        double percentage = (double) ass/255;
        double xml = percentage * 1000;
        xml = (xml - 1000) * -1;
        return (int) xml;
    }
    
    private String hexToASS (String hex) {
        String[] split = hex.split("");
        String res = "#" + split[4] + split[5] + split[2] + split[3] + split[0] + split[1];
        return res.equals("#FFFFFF") ? "#FEFEFE" : res;
    }
}