package jason.stdlib.mybody;

import jacamo.infra.JaCaMoAgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class act extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
       // checkArguments(args);

        //ts.getAg().abolish((Literal)args[0], un);
       // JaCaMoAgArch = ts.getAgArch().getFirstAgArch().
        //ts.getAgArch().getBody().act(args[0].toString());
       // ts.getAgArch().getNextAgArch().
         //       .realWorldAct(args[0].toString());
        //JaCaMoAgArch a = (JaCaMoAgArch) ts.getAgArch().getNextAgArch();
        ts.getAgArch().getNextAgArch().realWorldAct(args[0].toString());
        //JaCaMoAgArch chonron = (JaCaMoAgArch) ts.getAgArch();
       // chonron.realWorldAct(args[0].toString());
        //System.out.println("FUNGA "+a.getAgName());
        //a.realWorldAct(args[0].toString());
        return true;
    }
}
