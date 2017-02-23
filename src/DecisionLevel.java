import java.util.ArrayList;

/**
 * Created by irtazasafi on 11/5/16.
 */
public class DecisionLevel {

    ArrayList<Assignment> assignments; // at this level, following variables have been assigned
    int level;
    Variable decisionVariable;

    ArrayList<Variable> level_vars;
    ArrayList<Clause> level_clause;

    DecisionLevel(int _level, Variable var){
        decisionVariable = var;
        level = _level;
        assignments = new ArrayList<Assignment>();


        level_vars = new ArrayList<Variable>();
        level_clause = new ArrayList<Clause>();
    }
    DecisionLevel(int _level, Variable var, ArrayList<Assignment> _in){
        decisionVariable = var;
        level = _level;
        assignments = _in;
    }

    void addAssignment(Variable var, boolean phase){
        assignments.add(new Assignment(var,phase));
    }

    void initLevel(ArrayList<Variable> cvars,ArrayList<Clause> cclauses){
        cvars.forEach(var->{
            level_vars.add(var.copy());
        });
        cclauses.forEach(clause->{
            cclauses.add(clause.copy());
        });
    }

    void show(){
        System.out.println(String.valueOf(level) + "--------- " + decisionVariable.var + " Decision " + decisionVariable.decisionValue + " Flipped: " + decisionVariable.flipped);
    }
}
