package neck;

import jason.asSyntax.Literal;
import java.util.ArrayList;
import java.util.List;

public abstract class Apparatus {
    private String address = "setAddress";
    private boolean status = true;
    private String apparatusName = null;
    private final String namespace = "myBody::";
    private final String INTEROCEPTION = "source(interoception)";
    private final String EXTEROCEPTION = "source(exteroception)";
    private final String PROPRIOCEPTION = "source(proprioception)";

    List<Literal> interoceptions    = new ArrayList<>();
    List<Literal> exteroceptions    = new ArrayList<>();
    List<Literal> proprioceptions   = new ArrayList<>();

    public Apparatus() {}

    public Apparatus(String address) {this.address = address;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public boolean getStatus() {return status;}

    public void setStatus(boolean status) {this.status = status;}

    //public String getApparatusName() {return apparatusName;}

    public void setApparatusName(String apparatusName) {this.apparatusName = apparatusName;}

    private Literal getLiteralWithSourceBBAnotation(Literal l, String source) {
        return Literal.parseLiteral(this.namespace+l.toString()+"["+source+",apparatus("+apparatusName+")]");
    }

    //

    /* TODO */
    public abstract void act(String CMD);

    public abstract void perceive();

    public List<Literal> getInteroceptions(){
        List<Literal> list = new ArrayList<>();
        list = interoceptions;
        abolishInteroceptions();
        return list;
    }

    public List<Literal> getProprioceptions(){
        List<Literal> list = new ArrayList<>();
        list = proprioceptions;
        abolishProprioceptions();
        return list;
    }

    public List<Literal> getExteroceptions(){
        List<Literal> list = new ArrayList<>();
        list = exteroceptions;
        abolishExteroceptions();
        return list;
    }

    public List<Literal> getAllPerceptions() {
    //    System.out.println("getAllPerceptions");
        List<Literal> list = new ArrayList<>();
        list.addAll(interoceptions);
        list.addAll(proprioceptions);
        list.addAll(exteroceptions);

        abolishInteroceptions();
        abolishProprioceptions();
        abolishExteroceptions();

        return list;
    }

    public void addInteroception(Literal l){
        interoceptions.add(getLiteralWithSourceBBAnotation(l,INTEROCEPTION));
    }

    public void addExteroception(Literal l){
        exteroceptions.add(getLiteralWithSourceBBAnotation(l,EXTEROCEPTION));
    }

    public void addProprioception(Literal l){
        proprioceptions.add(getLiteralWithSourceBBAnotation(l,PROPRIOCEPTION));
    }

    private void abolishProprioceptions(){proprioceptions.clear();}

    private void abolishInteroceptions(){interoceptions.clear();}

    private void abolishExteroceptions(){exteroceptions.clear();}
}