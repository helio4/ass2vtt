/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ass2vtt;

/**
 *
 * @author helio4
 */
public class Timer {
    private int hours, minutes, seconds, ms;
    
    public Timer() {
        hours = 0;
        minutes = 0;
        seconds = 0;
        ms = 0; 
    }
    
    //Asumes time is well formated
    public Timer(String time) {
        time = time.replace('.', ':');
        String[] splited = time.split(":");
        hours = Integer.parseInt(splited[0]);
        minutes = Integer.parseInt(splited[1]);
        seconds = Integer.parseInt(splited[2]);
        ms = Integer.parseInt(splited[3]);
    }
    
    public void add(int milliseconds) {
        ms = ms + milliseconds;
        if(ms >= 100) {
            seconds = seconds + (ms/100);
            ms = ms%100;
        }
        if(seconds >= 60) {
            minutes = minutes + (seconds/60);
            seconds = seconds%60;
        }
        if(minutes >= 60) {
            hours = hours + (minutes/60);
            minutes = minutes%60;
        }
    }
    
    @Override
    public String toString(){
        String res = "";
        if(hours < 10) res += "0" + hours + ":";
        else res += hours + ":";
        if (minutes < 10) res += "0" + minutes + ":";
        else res += minutes + ":";
        if(seconds < 10) res += "0" + seconds + ".";
        else res += seconds + ".";
        if(ms < 10) res += "0" + ms + "0";
        else res += ms + "0";
        return res;
    }
    
    public int getMinutes() {return minutes;}
    public int getSeconds() {return seconds;}
    public int getMilliseconds() {return ms;}
}
