package group.chon;

import group.chon.javino.Javino;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Apparatus {
    private boolean atuando = false;
    private boolean percebendo = false;
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

    public String getPort() {
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
        while(this.atuando){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.percebendo = true;
        if(javino.requestData(getPort(),"getPercepts")){
            String percepts = javino.getData();
            if(verify(percepts)){
                this.percebendo = false;
                return percepts;
            }
        }
        this.percebendo = false;
        return null;
    }

    public void act(String CMD){
        while(this.percebendo){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.atuando = true;
        javino.sendCommand(getPort(),CMD);
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.atuando = false;

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
