package group.chon;

import jssc.SerialPortList;
import java.util.ArrayList;
import java.util.List;

public class Body {
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

    public String getPercepts(){
        String percepts = "";
        for(int i = 0; i < apparatusAttached.size(); i++){
            if(apparatus[i].isOk()){
                percepts += apparatus[i].getPercepts();
            }else{

            }
        }
        return percepts;
    }

    public void act(String CMD){
        for(int i = 0; i < apparatusAttached.size(); i++){
            apparatus[i].act(CMD);
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
}
