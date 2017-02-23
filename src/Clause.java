import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.Pack200;
import java.util.stream.Stream;

/**
 * Created by irtazasafi on 10/28/16.
 */
public class Clause {

    public ArrayList<Literal> literals;


    Clause(){
        literals = new ArrayList<Literal>();
    }

    void addLiteral(Literal literal){
        literals.add(literal);
    }

    void showLiterals(){
        literals.forEach(literal -> {
            String out = "";
            if(literal.phase){
                if(literal.assigned){
                    out = out + (literal.phase) + " ";
                } else {
                    out = out + literal.variable.var + " ";
                }
            } else {
                if(literal.assigned){
                    out = out + ((literal.phase)) + " ";
                } else {
                    out = out + "-" + literal.variable.var + " ";
                }
            }
            System.out.print(out);
        });

        System.out.print(" --------" +getStatus() + "  " + isUnitClause());
    }

    Clause copy(){
        Clause c = new Clause();
        literals.forEach(literal -> c.addLiteral(literal.copy()));
        return c;
    }

    status getStatus(){

        int pVars =0;
        int nVars =0;
        int uVars =0;

        for(Literal literal:literals){
            if(literal.assigned && literal.phase){
                pVars++;
            } else if(literal.assigned && !literal.phase){
                nVars++;
            } else if(!literal.assigned){
                uVars++;
            }
        }

        if(pVars > 0){
            return status.SATISFIED;
        } else if(uVars >= 1){
            return status.UNKNOWN;
        } else if(nVars  == literals.size()) {
            return status.UNSATISFIED;
        }
        return status.SATISFIED;
    }

    void applyAssignment(Variable v, boolean apply_phase){
        literals.forEach(literal -> {
            if(literal.variable.var.equals(v.var)){
                literal.assigned = true;
                if(literal.phase){
                    literal.phase = apply_phase;
                } else {
                    literal.phase = !apply_phase;
                }
            }
        });
    }

    Literal isUnitClause(){
        // if all are assigned then it's not.
        boolean allAssigned = true;
        int numNotAssigned = 0;
        int notAssignedIndex = 0;
        int numFalse = 0;
        int numTrue = 0;
        for(int i = 0 ; i < literals.size();i++){
            if(!literals.get(i).assigned){
                numNotAssigned++;
                notAssignedIndex = i;
            } else {
                if(!literals.get(i).phase){
                    numFalse++;
                }
            }
        }

        if(!(numNotAssigned == 1) || numFalse!= literals.size()-1){
            return null;
        }
        return literals.get(notAssignedIndex);
    }
}
