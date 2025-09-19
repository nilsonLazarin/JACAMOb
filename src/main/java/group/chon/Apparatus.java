package group.chon;

import group.chon.javino.Javino;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Apparatus {
    private int timeouts=0;
    private String status="on";
    private Instant lastTimeout = Instant.ofEpochSecond(0);
    private Javino javino;
    private String serialPort = "";
    private Pattern pattern;
    private Matcher matcher;

    public Apparatus(String port) {
        serialPort = port;
        javino = new Javino();
    }

    private void newTimeout(){
        Instant now = Instant.now();
        Duration duration = Duration.between(this.lastTimeout,now);
        if(duration.toMinutes() >= 1){
            this.lastTimeout = now;
            this.timeouts = 0;
        }else{
            this.timeouts++;
        }
    }

    private void setStatus(String status){
        this.status=status;
    }

    private String getPort() {
        return serialPort;
    }

    public boolean isOk(){
        if(this.status.equals("on")){
            return true;
        }else{
            return false;
        }
    }

    public String getPercepts(){
        if(javino.requestData(getPort(),"getPercepts")){
            String percepts = javino.getData();
            if(verify(percepts)){
                return percepts;
            }
        }
        return null;
    }

    public void act(String CMD){
        javino.sendCommand(getPort(),CMD);
    }

    private boolean verify(String beliefString){
        String regex = "port\\(([^,]+),(.+?)\\);";

        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(beliefString);

        if (matcher.find()) {
            String portName = matcher.group(1);
            String status   = matcher.group(2);
            if ("on".equals(status)) {
                return true;
            } else if ("timeout".equals(status)) {
                if(this.timeouts<4){
                    newTimeout();
                   return true;
                }else{
                   setStatus(status);
                   return false;
                }
            } else if ("off".equals(status)) {
                setStatus(status);
                return false;
            }
        }
        setStatus("unknown");
        return false;
    }

}
