/**
 * Created by irtazasafi on 10/28/16.
 */
public class Variable {
    Variable(String _var){
        var = _var;
    }
    String var;
    boolean decisionValue;
    boolean flipped = false;
    boolean picked = false;
    boolean forcePick = false;

    int dLevel = -1;

    Variable copy(){
        Variable v = new Variable(var);
        v.decisionValue = decisionValue;
        v.flipped = flipped;
        v.picked = picked;
        return v;
    }


}
