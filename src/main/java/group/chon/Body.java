package group.chon;

import jssc.SerialPortList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jason.asSyntax.Literal;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jason.asSyntax.Literal.parseLiteral;

public class Body {
    Logger logger;
    String[] apparatusAvailables;
    List<String> apparatusAttached = new ArrayList<>();
    Integer apparatusAvailablesInt = 0;
    Apparatus[] apparatus = new Apparatus[16];
    String bodyName;

    private Body(){
        System.out.println("CRIANDO UM CORPO...");
    }

    public Body(String bodyName) {
        this();
        this.bodyName = bodyName;
        this.logger = Logger.getLogger(bodyName);
    }

//    public Body(Mode mode) {
//        this();
//        if (mode == Mode.AUTODISCOVERY) {
//            String[] apparatusList = discover();
//            for (int i=0;i<apparatusList.length;i++){
//                attachApparatus(apparatusList[i]);
//            }
//
//        }
//    }

    public void attachApparatus(String address){
        if (!apparatusAttached.contains(address)){
            apparatus[apparatusAttached.size()] = new Apparatus(address);
            System.out.println("Attaching Apparatus in "+address);
            apparatusAttached.add(address);
        }else{
            System.out.println("Apparatus in "+address+" already attached");
        }
    }

//    public void disattachApparatus(String address){
//       ANDAMENTO
//
//        if (apparatusAttached.contains(address)){
//            for(int i = 0; i < apparatusAttached.size(); i++){
//                if(apparatus[i].getPort()){
//
//                }
//            }
//            apparatusAttached.remove(address);
//            apparatus[apparatusAttached.size()] = new Apparatus(address);
//            System.out.println("Attaching Apparatus in "+address);
//            apparatusAttached.add(address);
//        }
//    }

    public List<Literal> getPerceptsList() {
        List<Literal> jPercept = new ArrayList<Literal>();
        // separa em ';' tolerando espaços antes/depois
        String[] perception = getPercepts().split("\\s*;\\s*");
        for (String p : perception) {
            if (p == null) continue;
            String t = p.trim();
            if (t.isEmpty()) continue; // ignora último vazio por causa do ';' final
            try {
                jPercept.add(parseLiteral("mybody::"+normalizeSourceTag(t)));
                //getTS().getAg().getBB().add(Literal.parseLiteral(rwPercepts.replace("[e]","[source(exteroception)]")));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Parse error when parsing "+t);
            }
        }
        return jPercept;
    }



    private String getPercepts(){
        String percepts = "";
        for(int i = 0; i < apparatusAttached.size(); i++){
            if(apparatus[i].isOk()){
                percepts += apparatus[i].getPercepts();
            }else{
                logger.log(Level.SEVERE,"Apparatus ["+apparatusAttached.get(i).toString()+"] not OK");
            }
        }
        return percepts;
    }

    public void act(String CMD){
        for(int i = 0; i < apparatusAttached.size(); i++){
            apparatus[i].act(CMD);
            logger.log(Level.SEVERE,"[body] actinging "+CMD+" in "+apparatus[i].getPort());
        }
    }



    private String[] discover(){
        String[] portNames = SerialPortList.getPortNames();
        apparatusAvailablesInt = portNames.length;

        if (portNames.length == 0) {
            System.out.println("Nenhuma porta serial encontrada.");
        }else{
            System.out.println("Portas seriais encontradas:");
            for (String portName : portNames) {
                System.out.println("- " + portName);
            }
        }

        this.apparatusAvailables = new String[portNames.length];
        this.apparatusAvailables = portNames;
        return portNames;
    }

    public static String normalizeSourceTag(String s) {

        // já está no formato [source(...)]? (case-insensitive, espaços tolerados)
        if (s.matches("(?i).*\\[\\s*source\\s*\\(.*\\)\\s*]\\s*$")) {
            return s;
        }

        // separa "head" e o conteúdo opcional dos colchetes
        Matcher m = Pattern.compile("^\\s*(.*?)\\s*(?:\\[(.*?)\\])?\\s*$").matcher(s);
        if (!m.matches()) {
            // fallback: se não casar, adiciona default
            return s + "[source(interoception)]";
        }

        String head = m.group(1).trim();
        String tag  = m.group(2) == null ? "" : m.group(2).trim();

        String src;
        if (tag.equalsIgnoreCase("e"))      src = "exteroception";
        else if (tag.equalsIgnoreCase("p")) src = "proprioception";
        else                                src = "interoception"; // inclui [i] e sem colchetes

        return head + "[source(" + src + ")]";
    }

}
