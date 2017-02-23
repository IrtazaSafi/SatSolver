/**
 * Created by irtazasafi on 10/28/16.
 */
public class Literal {
    Literal(Variable _var, boolean _phase){
        variable = _var;
        phase = _phase;
        assigned = false;
    }
    Variable variable;
    boolean phase;
    boolean assigned;

    Literal copy(){

        Literal lit = new Literal(variable,phase);
        lit.assigned = assigned;
        return lit;
    }
}
