import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;


public class SatSolver {


    SatSolver(CopyOnWriteArrayList<Variable> _globalFlipped){
        globalFlipped = _globalFlipped;
    }

    private   int NUM_VARIABLES;
    private   int NUM_CLAUSES;

    private   int DECISION_LEVEL = 0;

    private Variable forcePickVar = null;

    private   ArrayList<Clause> clauses = new ArrayList<Clause>();
    private   ArrayList<Variable> variables = new ArrayList<Variable>();

    private   ArrayList<Clause> currClauses = new ArrayList<Clause>();
    private   ArrayList<Variable> currVariables = new ArrayList<Variable>();

    //private   ArrayList<Variable> decisionVariables = new ArrayList<Variable>();

    private   ArrayList<DecisionLevel> decisionLevels = new ArrayList<DecisionLevel>();

    private   ArrayList<Assignment> satisfyingAssignments = new ArrayList<Assignment>();

    private CopyOnWriteArrayList<Variable> globalFlipped;


    private   boolean checkExist(Variable _var){
       for(Variable var:variables){
           if(var.var.equals(_var.var)){
               return true;
           }
       }
       return false;
    }

    private   void showVariables(){
        variables.forEach(variable -> System.out.println(variable.var));

    }

    private   void showClauses() {
        clauses.forEach(clause->{
            clause.showLiterals();
            System.out.println();
        });
    }

    private   void showCurrClauses() {
        currClauses.forEach(clause->{
            clause.showLiterals();
            System.out.println();
        });
    }



    private   void extractVariablesAndClauses(String [] clause_in){
        Clause clause = new Clause();
        Arrays.stream(clause_in)
        .filter(var -> !var.equals("0"))
        .forEach((var)->{
            int literal = Integer.parseInt(var);
            int rawVar = Math.abs(literal);

            Variable temp_var = new Variable("x"+String.valueOf(rawVar));
            if(!checkExist(temp_var)){
                variables.add(temp_var);
            }
            clause.addLiteral(new Literal(temp_var,(literal > 0)));
            //System.out.println(literal);

        });
        clauses.add(clause);
    }

    private   void copyOrigIntoCurrent() {
        currClauses.clear();
        currVariables.clear();
        clauses.forEach(clause->{
            currClauses.add(clause.copy());
        });
        variables.forEach(variable -> {
            currVariables.add(variable.copy());
        });
    }

    private   void parse(String FILE_NAME) throws IOException {
        Files.lines(Paths.get(FILE_NAME))
                .map(line-> line.trim().replaceAll(" +", " ").trim())
                .filter(line-> (!line.split(" ")[0].equals("c")) && (!line.split(" ")[0].equals("%")) &&
                        (!line.split(" ")[0].equals("0")) && !line.split(" ")[0].equals(""))
                .forEach((line)->{
                    // PARSE LOGIC HERE

                  // System.out.println(line);

                    String[] parts = line.split(" ");
                    switch (parts[0]) {
                        case "p":
                            NUM_VARIABLES = Integer.parseInt(parts[2]);
                            NUM_CLAUSES = Integer.parseInt(parts[3]);
                            break;
                        case "%":
                            //
                        case "0":
                            //
                        default:
                            //System.out.println(parts[0]);
                            //System.out.println(line);
                            extractVariablesAndClauses(parts);
                            break;
                    }


                });
        copyOrigIntoCurrent();
//        variables.forEach(variable -> {
//            decisionVariables.add(variable.copy());
//        });
    }



    private boolean hasUnitClause(ArrayList<Clause> clauses) {

        for(Clause clause:clauses){
            if(clause.isUnitClause()!=null){
                return true;
            }
        }
        return false;
    }

    private void markPicked(Variable v, boolean decision){
        currVariables.stream().filter(decisionVariable -> decisionVariable.var.equals(v.var)).forEach(decisionVariable -> {
            decisionVariable.picked = true;
            decisionVariable.decisionValue = decision;
        });
    }


    private void displayAssignments(ArrayList<Assignment> assignments){
        assignments.forEach(assignment -> {
            System.out.println(assignment.var.var +" " + assignment.phase);
        });
    }

    private status checkStatus(){
       // tell the status

        int unsat = 0;
        int sat = 0;
        int unknown = 0;

        for(Clause clause:currClauses){
            status st = clause.getStatus();
            if(st == status.SATISFIED){
                sat++;
            } else if(st == status.UNKNOWN){
                unknown++;
            } else {
                unsat++;
            }
        }
        if(unknown ==0 && unsat == 0){
            return status.SATISFIED;
        } else if (unknown > 0){
            return status.UNKNOWN;
        } else if(unknown == 0 && unsat > 0){
            return status.CONFLICT;
        }

        return status.UNKNOWN;
    }

    private void bcp(ArrayList<Assignment> assignments){
        while(hasUnitClause(currClauses)){
            currClauses.forEach(clause1 -> {
                Literal lit = clause1.isUnitClause();
                if (lit != null) {
                    // if lit is negative then variable must be assigned false
                    if(!lit.phase){
                        assignments.add(new Assignment(lit.variable.copy(),false));
                        markPicked(lit.variable,false);
                        applyAssignments(lit.variable,false);
                    } else {
                        assignments.add(new Assignment(lit.variable.copy(),true));
                        markPicked(lit.variable,true);
                        applyAssignments(lit.variable,true);
                    }
                }
            });
        }
    }


    private ArrayList<Assignment> copyAssignments(ArrayList<Assignment>in){
        ArrayList<Assignment> ass = new ArrayList<Assignment>();
        in.forEach(assignment -> {
            ass.add(new Assignment(assignment.var.copy(),assignment.var.decisionValue));
        });
        return ass;
    }

    boolean doOpposite = false;

    private DecisionLevel getDecisionLevel(int _level){
        for(DecisionLevel dec:decisionLevels){
            if(dec.level == _level){
                return dec;
            }
        }
        return null;
    }

    private DecisionLevel getBackTrackLevel() {
        // get the highest decision variable not flipped.
        // sort according to level;
        for(int i = decisionLevels.size() -1; i >= 0;i--){
            DecisionLevel lvl = decisionLevels.get(i);
            if(!lvl.decisionVariable.flipped) {
                return lvl;
            }
        }
        return null;
    }

    public  int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    private void clearLevels(int ref_level){
        ArrayList<DecisionLevel> newD = new ArrayList<DecisionLevel>();
        decisionLevels.forEach(decisionLevel -> {
            if(decisionLevel.level <= ref_level){
                newD.add(decisionLevel);
            }
        });

        decisionLevels = newD;
    }

    private void fixGlobalVars(Variable assign_v) {
        for(int i = 0 ; i < currVariables.size();i++){
            if(currVariables.get(i).var.equals(assign_v.var)){
                Variable tv = assign_v.copy();
                tv.flipped = false;
                tv.picked= true;
                currVariables.set(i,tv);
            }
        }
    }

    private void restoreState(DecisionLevel level){
        copyOrigIntoCurrent();
        level.assignments.forEach(assignment -> {
            applyAssignments(assignment.var,assignment.var.decisionValue);
        });
        level.assignments.forEach(assignment -> {
            fixGlobalVars(assignment.var);
        });
    }

    public boolean InGlobalFlipped(Variable v){
        for(Variable var: globalFlipped){
            if(v.var.equals(var.var)){
                return true;
            }
        }
        return false;
    }

    private Variable getNonFlippedGlobal(){

        boolean exist = false;
        for(Variable var:variables){
            if(!InGlobalFlipped(var)){
                exist = true;
                break;
            }
        }
        if(!exist){
            return null;
        }

        while(true){
            int rand = randInt(0,variables.size()-1);
            Variable rVar = variables.get(rand);
            if(!InGlobalFlipped(rVar)){
                return rVar.copy();
            }
        }

//        for(Variable var:variables){
//            if(!InGlobalFlipped(var)){
//                return var.copy();
//            }
//        }
        //return null;
    }

    private void markForcePick(String var){
        currVariables.forEach(variable -> {
            if(variable.var.equals(var)){
                variable.forcePick = true;
            }
        });
    }

    private boolean backTrack(){ // chronological.
        // flip the highest decision variable not flipped.
        DecisionLevel lvl = getBackTrackLevel();
        if(lvl == null){

            return false; // we're done.
        }
        if(lvl.level == 0){
            System.out.println("************LEVEL ZERO HIT**********");
            System.out.println("GLOBAL FLIPPED SIZE " + globalFlipped.size());
            // one half explored, restore to initial state but mark level 0 as flipped.
            //lvl.decisionVariable.flipped = true;
            // get Level 1 and flip this decision, signal to decideNextBranch what to pick
            DecisionLevel lvlOne = getDecisionLevel(1);
            if(lvlOne.decisionVariable.flipped){
                //return false;
                globalFlipped.add(lvlOne.decisionVariable.copy());
                if(globalFlipped.size() == NUM_VARIABLES) {
                    return false;
                }
                forcePickVar = getNonFlippedGlobal();
                System.out.println(forcePickVar.var + " " + forcePickVar.decisionValue);
                doOpposite = false;
                clearLevels(0);
                DECISION_LEVEL = 1;
                copyOrigIntoCurrent();
                if(forcePickVar == null){
                   System.out.println("GLOBAL FLIPPED SIZE " + globalFlipped.size());
                    return false;
                }
                return true;

            }
            forcePickVar = lvlOne.decisionVariable;
            //lvlOne.decisionVariable.flipped = true;
            //forcePickVar.decisionValue = lvlOne.decisionVariable.decisionValue;

            // clear ALL Decision levels > 0
            clearLevels(0);
            DECISION_LEVEL = 1;

            // completely restore solver state
            copyOrigIntoCurrent();
            return true;
        } else {
            lvl.decisionVariable.flipped = true;
            forcePickVar = lvl.decisionVariable;

            //System.out.println("FORCE PICK VAR IS " + forcePickVar.var);
            clearLevels(lvl.level -1);

            // restore state to previous level
            DecisionLevel prvLvl = getDecisionLevel(lvl.level-1);
            restoreState(prvLvl);

            // mark that this variable has been picked in globalVar and can only be forceReturned

            DECISION_LEVEL = lvl.level;
            return true;
        }
    }

    private void showCurrVars(){
        currVariables.forEach(variable -> {
            System.out.println(variable.var + " DECISION: " + variable.decisionValue);
        });
    }

    private void showDecisionLevels(){
        decisionLevels.forEach(decisionLevel -> {
            decisionLevel.show();
        });
    }

    private boolean allPicked(){
        for(int i = 0 ; i < currVariables.size();i++){
            if(!currVariables.get(i).picked){
                return false;
            }
        }
        return true;
    }

    private  Variable decideNextBranch() {

        //System.out.println("FORCE PICK VAR in DECIDE IS " + forcePickVar.var);
        if(forcePickVar!=null){
            Variable v;
            for(Variable var:currVariables){
                if(var.var.equals(forcePickVar.var)){
                    v = var;
                    if(doOpposite) {
                        v.decisionValue = false;
                        doOpposite = false;
                    } else {
                        v.decisionValue = !forcePickVar.decisionValue;
                    }
                    v.picked = true;
                    v.flipped = true;

                    forcePickVar = null;
                    return v;
                }
            }
        }

        if(allPicked()){
            return null;
        } else {

            while(true){
                int r = randInt(0,currVariables.size()-1);
                Variable var = currVariables.get(r);
                if(!var.picked){
                    r = randInt(0,1);
                    var.decisionValue = r != 0;
                    //var.decisionValue = true;
                    var.picked = true;
                    var.flipped = false;
                    return var;
                }
            }
        }

//        for (Variable var : currVariables) {
//            if (!var.picked) {
//                var.decisionValue = true;
//                var.picked = true;
//                var.flipped = false;
//                return var;
//            }
//        }
//        return null;
    }

    private status  DPLL() {
        DECISION_LEVEL = 0;
        decisionLevels.add(new DecisionLevel(DECISION_LEVEL, new Variable("START")));
        DECISION_LEVEL += 1;

        int count = 0;
        status INSTANCE_STATE = null;

        long startTime = System.currentTimeMillis();


        while (true) {
//            count++;
//            if(count == 10000){
//                System.out.println("Processing..");
//                count = 0;
//            }

//            if(System.currentTimeMillis() -startTime > 300000){
//                return status.TIMEOUT;
//            }

            // 5 minutes = 300000
            // 3 minutes = 180000

            Variable branchVar = decideNextBranch();
            //System.out.print(' ');
            //System.out.println("BRANCH VAR : " + DECISION_LEVEL+" " + branchVar.var + " " + branchVar.decisionValue);
            ArrayList<Assignment> levAssigns = copyAssignments(getDecisionLevel(DECISION_LEVEL - 1).assignments);
            applyAssignments(branchVar, branchVar.decisionValue);
            levAssigns.add(new Assignment(branchVar, branchVar.decisionValue));
            bcp(levAssigns);
            decisionLevels.add(new DecisionLevel(DECISION_LEVEL,branchVar,levAssigns));
            //showDecisionLevels();
            INSTANCE_STATE = checkStatus();
            if (INSTANCE_STATE == status.SATISFIED) {
                //System.out.println(DECISION_LEVEL+" " + branchVar.var + " " + branchVar.decisionValue);
                satisfyingAssignments = getDecisionLevel(DECISION_LEVEL).assignments;
                return status.GLOBAL_SAT;
            } else if (INSTANCE_STATE == status.CONFLICT) {
                //System.out.println();
               // System.out.println("********************************Backtrack***************************");
                boolean check = backTrack();
                if(check == false){
                    return status.UNSATISFIED;
                }
                //System.out.println();
            } else {

                DECISION_LEVEL++;
            }
        }
    }

    private void applyAssignments(Variable v, boolean assign_phase){
        markPicked(v,assign_phase);
        currClauses.forEach(clause->{
            clause.applyAssignment(v,assign_phase);
        });
    }



    public static void main(String[] args) throws IOException, InterruptedException {

        CopyOnWriteArrayList<Variable> globalFlipped = new CopyOnWriteArrayList<Variable>();
        SatSolver solver = new SatSolver(globalFlipped);
//        Thread t1 = new Thread(()->{
//            Scanner scanner = new Scanner(System.in);
//            while(true) {
//                System.out.println("bitch");
//                int val = scanner.nextInt();
//                System.out.println("input");
//                if (val == 1) {
//                    solver.showCurrClauses();
//                } else if (val == 2) {
//                    System.exit(1);
//                }
//            }
//        });
//
//        t1.start();


//        System.out.println("TESTING 20 VARIABLES");
//        solver.parse("uf20-01.cnf");
//        System.out.println(solver.DPLL());
//        //solver.displayAssignments(solver.satisfyingAssignments);
//        globalFlipped.clear();
//        solver.displayAssignments(solver.satisfyingAssignments);
//
//        System.out.println();
//        System.out.println();
//        System.out.println();


        System.out.println("TESTING 200 VARIABLES");
        solver = new SatSolver(globalFlipped);
        solver.parse("uf200-01.cnf");
        System.out.println(solver.DPLL());
        //solver.displayAssignments(solver.satisfyingAssignments);
        globalFlipped.clear();
        solver.displayAssignments(solver.satisfyingAssignments);

        System.out.println();
        System.out.println();
        System.out.println();


//        System.out.println("TESTING 50 VARIABLES-(UNSATISFIABLE)");
//        solver = new SatSolver(globalFlipped);
//        solver.parse("uuf50-01.cnf");
//        System.out.println(solver.DPLL());
//        //solver.displayAssignments(solver.satisfyingAssignments);
//        globalFlipped.clear();
//        solver.displayAssignments(solver.satisfyingAssignments);
//
//        System.out.println();
//        System.out.println();
//        System.out.println();
//
//
//        System.out.println("TESTING 75 VARIABLES");
//        solver = new SatSolver(globalFlipped);
//        solver.parse("uf75-01.cnf");
//        System.out.println(solver.DPLL());
//        //solver.displayAssignments(solver.satisfyingAssignments);
//        globalFlipped.clear();
//        solver.displayAssignments(solver.satisfyingAssignments);
//
//        System.out.println();
//        System.out.println();
//        System.out.println();
//
//
//        System.out.println("TESTING 100 VARIABLES");
//        solver = new SatSolver(globalFlipped);
//        solver.parse("uf100-01.cnf");
//        System.out.println(solver.DPLL());
//        //solver.displayAssignments(solver.satisfyingAssignments);
//        globalFlipped.clear();
//        solver.displayAssignments(solver.satisfyingAssignments);
//
//        System.out.println();
//        System.out.println();
//        System.out.println();
//
//
//        System.out.println("*************TESTING 200 VARIABLES************");
//        solver = new SatSolver(globalFlipped);
//        solver.parse("uf200-01.cnf");
//        System.out.println(solver.DPLL());
//        //solver.displayAssignments(solver.satisfyingAssignments);
//        globalFlipped.clear();
//        solver.displayAssignments(solver.satisfyingAssignments);


//            while(true) {
//                status sol = solver.DPLL();
//                if (sol != status.TIMEOUT) {
//                    System.out.println(sol);
//                    break;
//                } else {
//                    System.out.println("TIMEOUT");
//                }
//            }






    }
}
