package neck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.RevisionFailedException;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.util.*;

public class Body {
    Logger logger;
    String[] apparatusAvailables;
    List<String> apparatusAttached = new ArrayList<>();
    Integer apparatusAvailablesInt = 0;
    Apparatus[] apparatus = new Apparatus[128];
    String bodyName;

    private Body(){
       // System.out.println("CRIANDO UM CORPO...");
    }

    public Body(String bodyName) {
        this();
        this.bodyName = bodyName;
        this.logger = Logger.getLogger(bodyName);
    }

    public void attachApparatus(Apparatus implementation, String apparatusName) {
        //String address = implementation.toString(); // placeholder — prefira app.getAddress() se existir.
        String address = implementation.getAddress();

        if (address != null && apparatusAttached.contains(address)) {
            System.out.println("Apparatus in " + address + " already attached");
            return;
        }

        int idx = apparatusAttached.size();
        if (idx >= apparatus.length) {
            throw new IllegalStateException("Capacidade de apparatus esgotada (" + apparatus.length + ")");
        }

        apparatus[idx] = implementation;
        if (address != null) apparatusAttached.add(address);
       // System.out.println("Attaching custom Apparatus: " + implementation.getClass().getName() +
       //         (address != null ? " @ " + address : ""));

        apparatus[idx].setApparatusName(apparatusName);
    }

//    public void attachApparatus(String address){
//        if (!apparatusAttached.contains(address)){
//            apparatus[apparatusAttached.size()] = new Apparatus(address);
//            System.out.println("Attaching Apparatus in "+address);
//            apparatusAttached.add(address);
//        }else{
//            System.out.println("Apparatus in "+address+" already attached");
//        }
//    }


//    public List<Literal> getPerceptsList() {
////        List<Literal> jPercept = new ArrayList<Literal>();
////        // separa em ';' tolerando espaços antes/depois
////        String[] perception = getPercepts().split("\\s*;\\s*");
////        for (String p : perception) {
////            if (p == null) continue;
////            String t = p.trim();
////            if (t.isEmpty()) continue; // ignora último vazio por causa do ';' final
////            try {
////                jPercept.add(parseLiteral("mybody::"+normalizeSourceTag(t)));
////                //getTS().getAg().getBB().add(Literal.parseLiteral(rwPercepts.replace("[e]","[source(exteroception)]")));
////            } catch (Exception e) {
////                logger.log(Level.WARNING, "Parse error when parsing "+t);
////            }
////        }
////        return jPercept;
//        logger.log(Level.WARNING, "Nao esta percebendo ainda");
//
//        return null;
//    }

    private List<Literal> getPercepts(){
        List<Literal> list = new ArrayList<>();
        for(int i = 0; i < apparatusAttached.size(); i++){
            if(apparatus[i].getStatus()){
                //logger.log(Level.SEVERE,"Apparatus ["+apparatusAttached.get(i).toString()+"] OK");
                apparatus[i].perceive();
                list.addAll(apparatus[i].getAllPerceptions());
            }else{
                logger.log(Level.SEVERE,"Apparatus ["+apparatusAttached.get(i).toString()+"] not OK");
            }
        }
         return list;
    }

//    public void updatePercepts(TransitionSystem ts){
//        try {
//            removeBeliefsBySource("interoception",ts);
//            removeBeliefsBySource("proprioception",ts);
//            removeBeliefsBySource("exteroception",ts);
//        } catch (RevisionFailedException e) {
//            throw new RuntimeException(e);
//        }
//
//        List<Literal> newPercepts = getPercepts();
//        for (Literal perception : newPercepts) {
//            try {
//                ts.getAg().addBel(perception);
//            } catch (RevisionFailedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    private void removeBeliefsBySource(String source, TransitionSystem ts) throws RevisionFailedException {
//        for (Literal belief : ts.getAg().getBB()) {
//            if (belief.hasAnnot()) {
//                for (Term annotation : belief.getAnnots()) {
//                    if (annotation.isStructure()) {
//                        Structure annot = (Structure) annotation;
//                        if (annot.getFunctor().equals("source") && annot.getTerm(0).equals(Literal.parseLiteral(source))) {
//                            ts.getAg().delBel(belief);
//                        }
//                    }
//                }
//            }
//        }
//    }

    public void updatePercepts(TransitionSystem ts) {
        try {
            // 1) Novas percepções (já com as anotações source(i|p|e) conforme seu pipeline)
            List<Literal> incoming = getPercepts();
            Set<String> incomingKeys = new HashSet<>();
            for (Literal lit : incoming) {
                incomingKeys.add(keyFor(lit));
            }

            // 2) Coleta crenças atuais com source(i|p|e) e identifica as que devem sair
            List<Literal> toDelete = new ArrayList<>();
            Set<String> currentKeys = new HashSet<>();
            for (Literal belief : ts.getAg().getBB()) {
                if (!isFromKnownSource(belief)) continue; // só mexe nas crenças dessas origens
                String k = keyFor(belief);
                currentKeys.add(k);
                if (!incomingKeys.contains(k)) {
                    toDelete.add(belief); // estava antes e não veio agora → remover
                }
            }

            // 3) Remove as que sumiram
            for (Literal b : toDelete) {
                ts.getAg().delBel(b);
            }

            // 4) Adiciona apenas o que é novo
            for (Literal lit : incoming) {
                String k = keyFor(lit);
                if (!currentKeys.contains(k)) {
                    ts.getAg().addBel(lit);
                }
            }

        } catch (RevisionFailedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Verifica se a crença tem anotação source(i|p|e). */
    private boolean isFromKnownSource(Literal belief) {
        if (!belief.hasAnnot()) return false;
        for (Term ann : belief.getAnnots()) {
            if (ann.isStructure()) {
                Structure s = (Structure) ann;
                if ("source".equals(s.getFunctor()) && s.getArity() == 1) {
                    String src = s.getTerm(0).toString(); // "interoception"/"proprioception"/"exteroception"
                    if ("interoception".equals(src) || "proprioception".equals(src) || "exteroception".equals(src)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Chave canônica: functor + termos + source(...) — garante comparação estável. */
    private String keyFor(Literal l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getFunctor()).append('(');
        for (int i = 0; i < l.getArity(); i++) {
            if (i > 0) sb.append(',');
            sb.append(l.getTerm(i).toString());
        }
        sb.append(')');
        sb.append("#src=").append(extractSource(l));
        return sb.toString();
    }

    private String extractSource(Literal l) {
        if (l.hasAnnot()) {
            for (Term ann : l.getAnnots()) {
                if (ann.isStructure()) {
                    Structure s = (Structure) ann;
                    if ("source".equals(s.getFunctor()) && s.getArity() == 1) {
                        return s.getTerm(0).toString();
                    }
                }
            }
        }
        return ""; // nenhum source
    }



    public void act(String CMD){
        for(int i = 0; i < apparatusAttached.size(); i++){
            apparatus[i].act(CMD);
            logger.log(Level.SEVERE,"[body] actinging "+CMD+" in "+apparatus[i].getAddress());
        }
    }






//    private String[] discover(){
//        String[] portNames = SerialPortList.getPortNames();
//        apparatusAvailablesInt = portNames.length;
//
//        if (portNames.length == 0) {
//            System.out.println("Nenhuma porta serial encontrada.");
//        }else{
//            System.out.println("Portas seriais encontradas:");
//            for (String portName : portNames) {
//                System.out.println("- " + portName);
//            }
//        }
//
//        this.apparatusAvailables = new String[portNames.length];
//        this.apparatusAvailables = portNames;
//        return portNames;
//    }

//    public static String normalizeSourceTag(String s) {
//
//        // já está no formato [source(...)]? (case-insensitive, espaços tolerados)
//        if (s.matches("(?i).*\\[\\s*source\\s*\\(.*\\)\\s*]\\s*$")) {
//            return s;
//        }
//
//        // separa "head" e o conteúdo opcional dos colchetes
//        Matcher m = Pattern.compile("^\\s*(.*?)\\s*(?:\\[(.*?)\\])?\\s*$").matcher(s);
//        if (!m.matches()) {
//            // fallback: se não casar, adiciona default
//            return s + "[source(interoception)]";
//        }
//
//        String head = m.group(1).trim();
//        String tag  = m.group(2) == null ? "" : m.group(2).trim();
//
//        String src;
//        if (tag.equalsIgnoreCase("e"))      src = "exteroception";
//        else if (tag.equalsIgnoreCase("p")) src = "proprioception";
//        else                                src = "interoception"; // inclui [i] e sem colchetes
//
//        return head + "[source(" + src + ")]";
//    }

}


//       ANDAMENTO

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


//    public void disattachApparatus(String address){
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