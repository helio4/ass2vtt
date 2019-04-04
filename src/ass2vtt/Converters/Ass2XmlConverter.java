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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author jesus
 */
public class Ass2XmlConverter implements iConverter {
    
    private Map<String, String[]> styles = new HashMap();
    private Map<String, String[]> stylesInfo = new HashMap();
    private Map<String, Integer> wpIDs = new HashMap();
    private Set<String> usedStyles = new HashSet();
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
    private final Map<String, String> alignmentMap = new HashMap(){
        {   
            put("1", "6");
            put("2", "7");
            put("3", "8");
            put("4", "3");
            put("5", "4");
            put("6", "5");
            put("7", "0");
            put("8", "1");
            put("9", "2");
        }
    };
    private int lastId = 9;
    private int lastWp = 9;
    private int resX, resY;
    private String stylePart = "", subsPart = "";
    private boolean shift60;
    private boolean phoneKaraokeFix;
    private boolean fadingKaraoke;
    
    public Ass2XmlConverter(boolean shift60, boolean phoneKaraokeFix, boolean fadingKaraoke) {
        this.shift60 = shift60;
        this.phoneKaraokeFix = phoneKaraokeFix;
        this.fadingKaraoke = fadingKaraoke;
    }
    
    @Override
    public String convert(File file) throws FileNotFoundException, UnsupportedEncodingException, Exception {
        resX = 0;
        resY = 0;
        String res = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<timedtext format=\"3\">\n";
        stylePart = "<head>\n\n<wp id=\"1\" ap=\"6\" ah=\"0\" av=\"100\" />\n" +
                        "<wp id=\"2\" ap=\"7\" ah=\"50\" av=\"100\" />\n" +
                        "<wp id=\"3\" ap=\"8\" ah=\"100\" av=\"100\" />\n" +
                        "<wp id=\"4\" ap=\"3\" ah=\"0\" av=\"50\" />\n" +
                        "<wp id=\"5\" ap=\"4\" ah=\"50\" av=\"50\" />\n" +
                        "<wp id=\"6\" ap=\"5\" ah=\"100\" av=\"50\" />\n" +
                        "<wp id=\"7\" ap=\"0\" ah=\"0\" av=\"0\" />\n" +
                        "<wp id=\"8\" ap=\"1\" ah=\"50\" av=\"0\" />\n" +
                        "<wp id=\"9\" ap=\"2\" ah=\"100\" av=\"0\" />\n\n" +
                        "<ws id=\"0\" ju=\"0\" />\n" +
                        "<ws id=\"1\" ju=\"1\" />\n" +
                        "<ws id=\"2\" ju=\"2\" />\n\n";
        subsPart = "<body>\n\n";
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name()); 
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.startsWith("PlayResX: ")) resX = Integer.parseInt(line.replaceFirst("^PlayResX: ", ""));
                if(line.startsWith("PlayResY: ")) resY = Integer.parseInt(line.replaceFirst("^PlayResY: ", ""));
                //Style line format: Name(0), Fontname(1), Fontsize(2), PrimaryColour(3), SecondaryColour(4), OutlineColour(5), BackColour(6), Bold(7), Italic(8), Underline(9), StrikeOut(10), ScaleX(11), ScaleY(12), Spacing(13), Angle(14), BorderStyle(15), Outline(16), Shadow(17), Alignment(18), MarginL(19), MarginR(20), MarginV(21), Encoding(22)
                if(line.startsWith("Style: ")) {
                    line = line.replaceFirst("^Style: ", "");
                    String[] styleInfo = line.split(",", 23);
                    stylePart += convertStyle(styleInfo) + "\n";
                    stylesInfo.put(styleInfo[0], styleInfo);
                }
                //Dialogue line format: "Dialogue: Layer(0), Start(1), End(2), Style(3), Name(4), MarginL(5), MarginR(6), MarginV(7), Effect(8), Text(9)
                if(line.startsWith("Dialogue: ")) {
                    line = line.replaceFirst("^Dialogue: ", "");
                    String[] lineData = line.split(",", 10);
                    subsPart += convertLine(lineData[1], lineData[2], lineData[9], lineData[3]) + "\n";
                }
            }
            stylePart += "</head>";
            subsPart += "\n</body>\n";
            stylePart = removeUnusedStyles(stylePart);
            res += stylePart.replace("\n", "") + subsPart + "</timedtext>";
            scanner.close();
        return res;
    }
    
    private String convertLine(String start, String end, String line, String style) {
        String[] styleIDs = styles.get(style);
        String wpID = styleIDs[2];
        String res = "";
        String posTag = "";
        Timer startTimer = new Timer(start);
        Timer endTimer = new Timer(end);
        if(shift60) {
            startTimer.add(-60);
        }
        if(line.matches(".*\\{\\\\pos\\(\\d+(.\\d+)?,\\d+(.\\d+)?\\)\\}.*") && resX > 0 && resY > 0) {
            String aux = line;
            boolean found = false;
            while(aux.length() > 0 && !found) {
                if (aux.matches("^\\{\\\\pos\\(\\d+(.\\d+)?,\\d+(.\\d+)?\\)\\}.*")) {
                    posTag = aux.substring(0, aux.indexOf("}") + 1);
                    found = true;
                }
                aux = aux.substring(1);
            }
            line = String.join("", line.split("\\{\\\\pos\\(\\d+(.\\d+)?,\\d+(.\\d+)?\\)\\}"));
            wpID = Integer.toString(posTagToWP(posTag, styleIDs[2]));
        }
        //Normal Lines
        if(!lineIsKaraoke(line)) {
            String shakeTag = "";
            String fadTag = "";
            int duration = endTimer.getTotalMillis() - startTimer.getTotalMillis();
            line = symbolToEscapeSequenceXML(line);
            //".*\\{\\\\fad\\(\\d+,\\d+\\)\\}.*"
            if(line.matches(".*\\{\\s*\\\\fad\\s*\\(\\d+\\s*,\\s*\\d+\\s*\\)\\s*\\}.*")) {
                String aux = line;
                boolean found = false;
                while(aux.length() > 0 && !found) {
                    if (aux.matches("^\\{\\s*\\\\fad\\s*\\(\\d+\\s*,\\s*\\d+\\s*\\)\\s*\\}.*")) {
                        fadTag = aux.substring(0, aux.indexOf("}") + 1);
                        found = true;
                    }
                    aux = aux.substring(1);
                }
                String[] argsTag = fadTag.substring((3 + "fad".length()), fadTag.indexOf("}") - 1).split(",");
                int inTime = Integer.parseInt(argsTag[0]);
                int outTime = Integer.parseInt(argsTag[1]);
                line = String.join("", line.split("\\{\\s*\\\\fad\\s*\\(\\d+\\s*,\\s*\\d+\\s*\\)\\s*\\}"));
                res += fadingSubtitle(removeEveryAssTag(line), startTimer.getTotalMillis(), duration, outTime, style, wpID, false, "");
                //res += fadSubtitle(removeEveryAssTag(line), inTime, outTime, startTimer.getTotalMillis(), duration, movement, speed, style, posTag, wpID);
            }
            else if(line.matches(".*\\{\\s*\\\\shake\\s*\\(\\d+(.\\d+)?\\s*,\\s*\\d+(.\\d+)?\\s*\\)\\s*\\}.*")) {
                String aux = line;
                boolean found = false;
                while(aux.length() > 0 && !found) {
                    if (aux.matches("^\\{\\s*\\\\shake\\s*\\(\\d+(.\\d+)?\\s*,\\s*\\d+(.\\d+)?\\s*\\)\\s*\\}.*")) {
                        shakeTag = aux.substring(0, aux.indexOf("}") + 1);
                        found = true;
                    }
                    aux = aux.substring(1);
                }
                String[] argsTag = shakeTag.substring((3 + "shake".length()), shakeTag.indexOf("}") - 1).split(",");
                int movement = Integer.parseInt(argsTag[0]);
                int speed = Integer.parseInt(argsTag[1]);
                line = String.join("", line.split("\\{\\s*\\\\shake\\s*\\(\\d+(.\\d+)?\\s*,\\s*\\d+(.\\d+)?\\s*\\)\\s*\\}"));
                res += shakingSubtitle(removeEveryAssTag(line), startTimer.getTotalMillis(), duration, movement, speed, style, posTag, wpID);
            } else {
                res +="<p t=\"" + startTimer.getTotalMillis() + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[2]) + "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[0] + "\">" + removeEveryAssTag(line) + "</s></p>\n\n";
            }
            String hola = styleIDs[0];
            usedStyles.add(styleIDs[0]);
        //Karaoke Lines
        } else {
            Timer currentTimer = startTimer;
            String[] text = line.split("\\{\\\\k\\d*\\}");
            text = Arrays.copyOfRange(text, 1, text.length);
            Pattern pattern = Pattern.compile("\\{\\\\k\\d*\\}");
            Matcher matcher = pattern.matcher(line);
            int count = 0;
            while (matcher.find())count++;
            String[] timeMarks = new String[count];
            int index = 0;
            while(!line.isEmpty() && index < timeMarks.length) {
                if(line.matches("\\{\\\\k\\d*\\}.*")) {
                    timeMarks[index++] = line.substring(3, line.indexOf('}'));
                    line = line.substring(line.indexOf('}') + 1);
                } else {
                    line = line.substring(1);
                }
            }
            int lastTimeMark = Integer.parseInt(timeMarks[text.length - 1]);
            for(int i = 0; i < timeMarks.length - text.length; i++) {
                lastTimeMark += Integer.parseInt(timeMarks[text.length + i]); 
            }
            timeMarks[text.length - 1] = lastTimeMark + "";
            if(styleIDs[2].equals("8") || styleIDs[2].equals("9") || styleIDs[2].equals("7")) {
                if(styleIDs[2].equals("8")) wpID = Integer.toString(posFix(String.join("", text), posTag));
                int duration = endTimer.getTotalMillis() - startTimer.getTotalMillis();
                String pcKaraoke = "";
                String phoneKaraoke = "";
                if(styleIDs[2].equals("8") || styleIDs[2].equals("9")) {
                    if(fadingKaraoke) {
                        pcKaraoke += "<p t=\"" + startTimer.getTotalMillis() + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[2]) + "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[0]) + 6) + "\">" + String.join("", text) + "</s></p>\n";
                        usedStyles.add((Integer.parseInt(styleIDs[0]) + 6) + "");
                    } else {
                        pcKaraoke += "<p t=\"" + startTimer.getTotalMillis() + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[2]) + "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[0] + "\">" + String.join("", text) + "</s></p>\n";
                        usedStyles.add(styleIDs[0]);
                    }
                } else {
                    pcKaraoke += "<p t=\"" + startTimer.getTotalMillis() + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[2]) + "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[1]) + 4) + "\">" + String.join("", text) + "</s></p>\n";
                    usedStyles.add((Integer.parseInt(styleIDs[1]) + 4) + "");
                }
                if(!phoneKaraokeFix) phoneKaraoke += "<p t=\"" + startTimer.getTotalMillis() + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[2]) + "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[0]) + 2) + "\">" + String.join("", text) + "</s></p>\n";
                for(index = 0; index < text.length; index++) {
                    String[] leftSide = Arrays.copyOfRange(text, 0, index + 1);
                    String[] rightSide = Arrays.copyOfRange(text, index + 1, text.length);
                    if(styleIDs[2].equals("8") || styleIDs[2].equals("9")) {
                        if(fadingKaraoke) {
                            pcKaraoke += fadingSubtitle(leftSide[leftSide.length - 1] + String.join("", rightSide), currentTimer.getTotalMillis(), Integer.parseInt(timeMarks[index] + "0"), Integer.parseInt(timeMarks[index] + "0"), style, wpID, true, String.join("", rightSide));
                        } else {
                            pcKaraoke += "<p t=\"" + currentTimer.getTotalMillis() + "\" d=\"" + timeMarks[index] + "0\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[1] + "\">" + symbolToEscapeSequenceXML(String.join("", rightSide)) + "</s></p>\n";
                            usedStyles.add(styleIDs[1]);
                        }
                    } else {
                        pcKaraoke += "<p t=\"" + currentTimer.getTotalMillis() + "\" d=\"" + timeMarks[index] + "0\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[0]) + 4) + "\">" + symbolToEscapeSequenceXML(String.join("", leftSide)) + "</s></p>\n";        
                        usedStyles.add((Integer.parseInt(styleIDs[0]) + 4) + "");
                    }
                    if(phoneKaraokeFix) {
                        phoneKaraoke += "<p t=\"" + currentTimer.getTotalMillis() + "\" d=\"" + timeMarks[index] + "0\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[0]) + 2) + "\">"+ symbolToEscapeSequenceXML(String.join("", leftSide)) + "</s>&#8203;<s p=\"" + (Integer.parseInt(styleIDs[1]) + 2) + "\">" + symbolToEscapeSequenceXML(String.join("", rightSide)) + "</s></p>\n";
                        usedStyles.add((Integer.parseInt(styleIDs[0]) + 2) + "");
                        usedStyles.add((Integer.parseInt(styleIDs[1]) + 2) + "");
                    }
                    currentTimer.add(Integer.parseInt(timeMarks[index] + "0"));
                }
                res += pcKaraoke;
                res += phoneKaraoke;
            } else {
                for(index = 0; index < text.length; index++) {
                    String[] leftSide = Arrays.copyOfRange(text, 0, index + 1);
                    String[] rightSide = Arrays.copyOfRange(text, index + 1, text.length);
                    res += "<p t=\"" + currentTimer.getTotalMillis() + "\" d=\"" + timeMarks[index] + "0\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[0] + "\">"+ symbolToEscapeSequenceXML(String.join("", leftSide)) + "</s>&#8203;<s p=\"" + styleIDs[1] + "\">" + symbolToEscapeSequenceXML(String.join("", rightSide)) + "</s></p>\n";;
                    currentTimer.add(Integer.parseInt(timeMarks[index] + "0"));
                    usedStyles.add(styleIDs[0]);
                    usedStyles.add(styleIDs[1]);
                }
            }
        }
        return res.replace("\\N", "\n");
    }
    
    private String  convertStyle(String[] styleInfo) {
        //Style line format: Name(0), Fontname(1), Fontsize(2), PrimaryColour(3), SecondaryColour(4), OutlineColour(5), BackColour(6), Bold(7), Italic(8), Underline(9), StrikeOut(10), ScaleX(11), ScaleY(12), Spacing(13), Angle(14), BorderStyle(15), Outline(16), Shadow(17), Alignment(18), MarginL(19), MarginR(20), MarginV(21), Encoding(22)
        String name = styleInfo[0];
        String res = "<!-- " + name.replace("-->", "→") + " -->\n";
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
        String edgeInfo = "";
        if(edgeOpacity > 127) {edgeInfo = "et=\"3\" ec=\"" + edgeColor + "\"";}
        else if(!background && backgroundOpacity != 0) {edgeInfo = "et=\"1\" ec=\"" + backColour + "\"";}
        //Primary Style (Right Alignment)
        styles.put(name, new String[]{Integer.toString(lastId + 1), Integer.toString(lastId + 2), alignment});
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + primaryColour + "\" fo=\"" + foregroundOpacity + "\" " + bgInfo + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Karaoke Style (Right Alignment)
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + secondaryColour + "\" fo=\"" + secondaryOpacity + "\" " + "bo=\"0\"" + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Karaoke Phone Primary
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + primaryColour + "\" fo=\"" + 0 + "\" " + "bo=\"0\"" + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Karaoke Phone Secondary
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + secondaryColour + "\" fo=\"" + 0 + "\" " + "bo=\"0\"" + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n"; 
        //Primary Style (Left Alignment)
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + primaryColour + "\" fo=\"" + foregroundOpacity + "\" " + "bo=\"0\"" + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Karaoke Style (Left Alignment)
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + secondaryColour + "\" fo=\"" + secondaryOpacity + "\" " + bgInfo + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Just Background
        res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + backColour + "\" fo=\"" + 0 + "\" " + bgInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        //Opacity Primary Style (Left Alignment)
        for(int i = 0; i <= 255; i++) {
            res += "<pen id=\"" + ++lastId + "\" fs=\"" + font + "\" fc=\"" + primaryColour + "\" fo=\"" + i + "\" " + "bo=\"0\"" + " " + edgeInfo + " b=\"" + bold + "\" i=\"" + italic + "\" u=\"" + underline  + "\" />\n";
        }
        res += "\n";
        return res;
    }
    
    private int hexToOpacity(String hex) {
        int ass = Integer.decode("0x" + hex);
        int xml = (ass - 255) * -1;
        return xml;
    }
    
    private String hexToASS (String hex) {
        String[] split = hex.split("");
        String res = "#" + split[4] + split[5] + split[2] + split[3] + split[0] + split[1];
        return res.equals("#FFFFFF") ? "#FEFEFE" : res;
    }
    
    private int posTagToWP (String posTag, String alignment) {
        // X = Horizontal, Y = Vertical
        if(resX <= 0 || resY <= 0) return 0;
        String[] posXY = posTag.substring(6, posTag.indexOf("}") - 1).split(",");
        double posX = Double.parseDouble(posXY[0]);
        double posY = Double.parseDouble(posXY[1]);
        double realAH = (posX / resX) * 100.0;
        double realAV = (posY / resY) * 100.0;
        int specifiedAH = (int) ((realAH - 2) / 0.96);
        int specifiedAV = (int) ((realAV - 2) / 0.96);
        specifiedAH = Math.max(specifiedAH, 0);
        specifiedAH = Math.min(specifiedAH, 100);
        specifiedAV = Math.max(specifiedAV, 0);
        specifiedAV = Math.min(specifiedAV, 100);
        String key = specifiedAH + "," + specifiedAV + "," + alignmentMap.get(alignment);
        if(!wpIDs.containsKey(key)) {
            stylePart += "<wp id=\"" + ++lastWp + "\" ap=\"" + alignmentMap.get(alignment) + "\" ah=\"" + specifiedAH + "\" av=\"" + specifiedAV + "\" />\n";
            wpIDs.put(key, lastWp);
            return lastWp;
        } else {
            return wpIDs.get(key);
        }
        
    }
    
    private String symbolToEscapeSequenceXML(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    
    private String alignmentWS (String alignment) {
        if(alignment.equals("7") || alignment.equals("1") || alignment.equals("4")) return "0";
        if(alignment.equals("9") || alignment.equals("6") || alignment.equals("3")) return "1";
        return "2";
    }
    
    private double calculateLineWidth(String line) {
        Text text = new Text(line);
        new Scene(new Group(text));
        text.setFont(Font.font("Roboto Medium", 28));
        double width = text.getLayoutBounds().getWidth();
        if(resX != 640) width += 85.33;
        if(resX == 640) width -= 63; //106        
        return width;
    }
    
    private boolean lineIsKaraoke(String line) {
        return line.matches(".*\\{\\\\k\\d*\\}.*");
    }
    
    //We assume alignment is 8 and font is default (Roboto Medium)
    private int posFix(String line, String posTag) {
        // X = Horizontal, Y = Vertical
        if(resX <= 0 || resY <= 0) return 0;
        if(posTag.equals("")) {
            posTag = "{\\pos(" + (resX/2.0) + "," + 0 + ")}";
        }
        String[] posXY = posTag.substring(6, posTag.indexOf("}") - 1).split(",");
        double posX = Double.parseDouble(posXY[0]);
        double posY = Double.parseDouble(posXY[1]);
        String posTag2 = "{\\pos(";
        double lineWidth = calculateLineWidth(removeEveryAssTag(line));
        posX = posX + (lineWidth/2.0);
        posTag2 += Double.toString(posX) + "," + Double.toString(posY) + ")}";
        return posTagToWP(posTag2, "9");
    }
    
    private String fadingSubtitle(String text, int startTime, int duration, int outDuration, String style, String wpID, boolean isKaraoke, String rightSide) {
        String res = "";
        int increment = 66;
        if (duration <= 0) return "";
        String[] styleIDs = styles.get(style);
        //Style line format: Name(0), Fontname(1), Fontsize(2), PrimaryColour(3), SecondaryColour(4), OutlineColour(5), BackColour(6), Bold(7), Italic(8), Underline(9), StrikeOut(10), ScaleX(11), ScaleY(12), Spacing(13), Angle(14), BorderStyle(15), Outline(16), Shadow(17), Alignment(18), MarginL(19), MarginR(20), MarginV(21), Encoding(22)
        String[] styleInfo = stylesInfo.get(style);
        int startingOpacity = hexToOpacity(styleInfo[3].substring(2, 4));
        if(outDuration > duration) outDuration = duration;
        int times = Math.max(outDuration/increment, 1);
        int decrement = (int) Math.round(startingOpacity/(double)times);
        if(outDuration != duration) {
            if(!isKaraoke) {
                res += res += "<p t=\"" + startTime + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[0]) +  "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[0]) + 6) + "\">" + symbolToEscapeSequenceXML(text) + "</s></p>\n";
                usedStyles.add((Integer.parseInt(styleIDs[0]) + 6) + "");
            }
            res += "<p t=\"" + startTime + "\" d=\"" + (duration - outDuration) + "\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + (Integer.parseInt(styleIDs[0]) + 4) + "\">" + symbolToEscapeSequenceXML(text) + "</s></p>\n";
            if(isKaraoke) {
                res += "<p t=\"" + startTime + "\" d=\"" + (duration - outDuration) + "\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[1] + "\">" + symbolToEscapeSequenceXML(String.join("", rightSide)) + "</s></p>\n";
                usedStyles.add(styleIDs[1]);
            }
            usedStyles.add((Integer.parseInt(styleIDs[0]) + 4) + "");
        }
        int currentTime = (startTime + (duration - outDuration));
        int styleID = Integer.parseInt(styleIDs[0]) + 7 + (startingOpacity - decrement);
        for (int i = 0; i < times; i++) {
            res += "<p t=\"" + currentTime + "\" d=\"" + increment + "\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + styleID + "\">" + symbolToEscapeSequenceXML(text) + "</s></p>\n";
            if(isKaraoke) {
                res += "<p t=\"" + currentTime + "\" d=\"" + increment + "\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[1] + "\">" + symbolToEscapeSequenceXML(String.join("", rightSide)) + "</s></p>\n";
               usedStyles.add(styleIDs[1]);
            }
            usedStyles.add(styleID + "");
            usedStyles.add(styleIDs[2]);
            currentTime += increment;
            styleID -= decrement;
            if(styleID < (Integer.parseInt(styleIDs[0]) + 7)) styleID = Integer.parseInt(styleIDs[0]) + 7;
        }
        return res;
    }
    
    private String fadSubtitle(String text, int inTime, int outTime, int duration, String style, String wpID) {
        String res = "";
        //Fade in
        if(inTime + outTime >= duration) {
            inTime = Math.min(duration, inTime);
            outTime = Math.max(0, duration - inTime);
        }
        int fadeInDuration = duration - outTime;
        res += fadingSubtitle(text, 0, fadeInDuration, inTime, style, wpID, false, "");
        //Fade out
        res += fadingSubtitle(text, 0, outTime, outTime, style, wpID, false, "");
        
        return res;
    }
    
    private String shakingSubtitle(String text, int startTime, int duration, int movement, int speed, String style, String posTag, String wpID) {
        // X = Horizontal, Y = Vertical
        String[] styleIDs = styles.get(style);
        if(resX <= 0 || resY <= 0) {
            return "<p t=\"" + startTime + "\" d=\"" + duration + "\" ws=\"" + alignmentWS(styleIDs[2]) + "\" wp=\"" + wpID + "\"><s p=\"" + styleIDs[0] + "\">" + removeEveryAssTag(text) + "</s></p>\n\n";
        }
        movement = movement < 0 ? 0 : movement;
        speed = speed < 0 ? 0 : speed;
        String res = "";
        int[][] wpIDs = new int[3][3];
        //Style line format: Name(0), Fontname(1), Fontsize(2), PrimaryColour(3), SecondaryColour(4), OutlineColour(5), BackColour(6), Bold(7), Italic(8), Underline(9), StrikeOut(10), ScaleX(11), ScaleY(12), Spacing(13), Angle(14), BorderStyle(15), Outline(16), Shadow(17), Alignment(18), MarginL(19), MarginR(20), MarginV(21), Encoding(22)
        String[] styleInfo = stylesInfo.get(style);
        if(posTag.equals("")) {
            switch (styleInfo[18]) {
                case "1": posTag = "{\\pos(" + 0 + "," + resY + ")}";
                          break;
                case "2": posTag = "{\\pos(" + (resX/2.0) + "," + resY + ")}";
                          break;
                case "3": posTag = "{\\pos(" + resX + "," + resY + ")}";
                          break;
                case "4": posTag = "{\\pos(" + 0 + "," + (resY/2.0) + ")}";
                          break;
                case "5": posTag = "{\\pos(" + (resX/2.0) + "," + (resY/2.0) + ")}";
                          break;
                case "6": posTag = "{\\pos(" + resX + "," + (resY/2.0) + ")}";
                          break;
                case "7": posTag = "{\\pos(" + 0 + "," + 0 + ")}";
                          break;
                case "8": posTag = "{\\pos(" + (resX/2.0) + "," + 0 + ")}";
                          break;
                case "9": posTag = "{\\pos(" + resX + "," + 0 + ")}";
                          break;
            }
        }
        int times = (int) Math.ceil( (double) duration/speed);
        times = Math.max(1, times);
        //Center Middle
        String[] posXY = posTag.substring(6, posTag.indexOf("}") - 1).split(",");
        double posX = Double.parseDouble(posXY[0]);
        double posY = Double.parseDouble(posXY[1]);
        String auxTag = "{\\pos(";
        auxTag += Double.toString(posX) + "," + Double.toString(posY) + ")}";
        wpIDs[1][1] = posTagToWP(auxTag, styleInfo[18]);
        //Left Middle
        double x = posX - movement;
        double y = posY;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[0][1] = posTagToWP(auxTag, styleInfo[18]);
        //Right Middle
        x = posX + movement;
        y = posY;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[2][1] = posTagToWP(auxTag, styleInfo[18]);
        //Center Top
        x = posX;
        y = posY - movement;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[1][0] = posTagToWP(auxTag, styleInfo[18]);
        //Left Top
        x = posX - movement;
        y = posY - movement;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[0][0] = posTagToWP(auxTag, styleInfo[18]);
        //Right Top
        x = posX + movement;
        y = posY - movement;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[2][0] = posTagToWP(auxTag, styleInfo[18]);
        //Center Bottom
        x = posX;
        y = posY + movement;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[1][2] = posTagToWP(auxTag, styleInfo[18]);
        //Left Bottom
        x = posX - movement;
        y = posY + movement;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[0][2] = posTagToWP(auxTag, styleInfo[18]);
        //Right Bottom
        x = posX + movement;
        y = posY + movement;
        auxTag = "{\\pos(";
        auxTag += Double.toString(x) + "," + Double.toString(y) + ")}";
        wpIDs[2][2] = posTagToWP(auxTag, styleInfo[18]);
        //Shaking
        int currentTime = startTime;
        
        speed = Math.max(10, speed);
        speed = Math.min(duration, speed);
        String[] sequence = {Integer.toString(wpIDs[0][0]), Integer.toString(wpIDs[0][2]), Integer.toString(wpIDs[2][1]), Integer.toString(wpIDs[0][1]), Integer.toString(wpIDs[2][2]), Integer.toString(wpIDs[2][0]), Integer.toString(wpIDs[1][2]), Integer.toString(wpIDs[1][0])};
        int index = 0;
        for(int i = 0; i < times; i++) {
            speed = Math.min(speed, ((startTime + duration) - currentTime));
            res += "<p t=\"" + currentTime + "\" d=\"" + speed + "\" ws=\"" + alignmentWS(styleIDs[2]) +  "\" wp=\"" + sequence[index++] + "\"><s p=\"" + styleIDs[0] + "\">" + symbolToEscapeSequenceXML(text) + "</s></p>\n";
            currentTime += speed;
            currentTime = Math.min(currentTime, (startTime + duration));
            if(index >= sequence.length) index = 0;
        }
        return res;
    }
    
    private String removeEveryAssTag(String text) {
        return text.replaceAll("\\{.*?\\}", "");
    }
    
    private String removeUnusedStyles(String styles) {
        String aux = "";
        while(!styles.isEmpty()) {
            if(styles.startsWith("<pen")) {
                boolean isUsed = false;
                for(Iterator<String> it = usedStyles.iterator(); it.hasNext() && !isUsed; ) {
                    //styles.matches("^<\\s*pen\\s+id=\"" + it.next() + "\".*/>.*")
                    if(styles.startsWith("<pen id=\"" + it.next() + "\"")) {
                        isUsed = true;
                    }
                }
                if(!isUsed) {
                    styles = styles.replaceFirst("^<\\s*pen.*/>", "");
                } else {
                    aux += styles.substring(0, styles.indexOf('>') + 1);
                    styles = styles.substring(styles.indexOf('>') + 1);
                }
            } else {
                aux += styles.charAt(0);
                styles = styles.substring(1);
            }
        }
        return aux;
    }
}
