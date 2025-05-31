import sparrow.visitor.*;
import sparrow.*;
import IR.token.Identifier;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
public class FastLiveVisitor implements ArgRetVisitor<Env, ReturnIntervals> {
    
    /*   List<FunctionDecl> funDecls; */
    public ReturnIntervals visit(Program n, Env arg) {
        ReturnIntervals ret = null;
        for (FunctionDecl fd: n.funDecls) {
            ret = fd.accept(this, arg);
        }
        return ret; 
    }

    /*   Program parent;
     *   FunctionName functionName;
     *   List<Identifier> formalParameters;
     *   Block block; */
    public ReturnIntervals visit(FunctionDecl n, Env arg) { 
        arg.labelToInstrs = new HashMap<String, HashSet<String>>();
        arg.labelToArgInstrs = new HashMap<String, HashSet<String>>();
        arg.scope.lineNumber+=1;
        arg.scope.functionNumber+=1;
        int numParams = 1;
        for (Identifier id: n.formalParameters) {
            numParams = numParams + 1;
            String idStr = MyUtils.getId(id,arg.scope);
            if (numParams <= 7) {
                arg.paramIdToReg.put(idStr,"a"+numParams); // Assign the first 6 parameters to registers a1-a6
                arg.paramSpillMap.put(idStr,"Register");
                arg.idStrToArgReg.put("a"+numParams+"_"+arg.scope.functionNumber,idStr); // Add the parameter to the idStrToArgReg map
                arg.scope.paramIntervals.put(idStr, new Interval(arg.scope.lineNumber, -1, idStr,arg.scope.functionNumber)); // Add the parameter to the return intervals
            } else {
                arg.scope.parameters.add(idStr);
            }
            if (arg.returnIntervals.intervals.containsKey(idStr)) {
                throw new RuntimeException("Parameter " + idStr + " already defined in function " + n.functionName.toString() + " at line " + arg.scope.lineNumber);
            }
        }
        // n.formalParameters.forEach((id) -> {
        //     String idStr = MyUtils.getId(id,arg.scope);
        //     numParams = numParams + 1;
        //     arg.scope.parameters.add(idStr); // Add the parameter to the scope
        //     // arg.returnIntervals.intervals.put(idStr, new Interval(arg.scope.lineNumber, -1, idStr)); do not add parameters to return intervals here
        // });
        numParams = 0;
        n.block.accept(this, arg);
        arg.returnIntervals.intervals.forEach((id, interval) -> {
            if (interval.end == -1) {
                interval.end = interval.start; // If the interval is not updated, set end to start
            }
        });
        // arg.scope.paramIntervals.forEach((id, interval) -> {
        //     if (interval.end == -1) {
        //         interval.end = interval.start; // If the interval is not updated, set end to start
        //     }
        // });
        // n.formalParameters.forEach((id) -> {
        //     String idStr = MyUtils.getId(id,arg.scope);
        //     arg.returnIntervals.intervals.get(idStr).end = arg.scope.lineNumber; // Set end to the current line number
        // });
        return null; 
    }

    /*   FunctionDecl parent;
     *   List<Instruction> instructions;
     *   IdentifieReturn return_id; */
    public ReturnIntervals visit(Block n, Env arg) { 
        for (Instruction inst: n.instructions) {
            inst.accept(this, arg);
            arg.scope.lineNumber+=1;
        }
        if (arg.scope.parameters.contains(MyUtils.getId(n.return_id, arg.scope))) {
            // If the return_id is a parameter, we do not need to add it to the return intervals
            arg.scope.paramIntervals.get(MyUtils.getId(n.return_id, arg.scope)).end = arg.scope.lineNumber; // Set end to the current line number
            return null;
        }
        if (arg.returnIntervals.intervals.containsKey(MyUtils.getId(n.return_id, arg.scope))){
            arg.returnIntervals.intervals.get(MyUtils.getId(n.return_id, arg.scope)).end = arg.scope.lineNumber; // Set end to the last line number
        }
        return null; 
    }

    /*   Label label; */
    public ReturnIntervals visit(LabelInstr n, Env arg) { 
        arg.labelToInstrs.put(n.label.toString(), new HashSet<String>());
        arg.labelToArgInstrs.put(n.label.toString(), new HashSet<String>());
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   int rhs; */
    public ReturnIntervals visit(Move_Id_Integer  n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add rhs definitions
        // None because rhs is an integer
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   FunctionName rhs; */
    public ReturnIntervals visit(Move_Id_FuncName n, Env arg) {
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add rhs uses
        // None because rhs is a function name
        return null; 
        }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(Add n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add arg1 uses
        String arg1 = MyUtils.getId(n.arg1,arg.scope);
        MyUtils.useId(arg1,arg);
        // Add arg2 uses
        String arg2 = MyUtils.getId(n.arg2,arg.scope);
        MyUtils.useId(arg2,arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(Subtract n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add arg1 uses
        String arg1 = MyUtils.getId(n.arg1,arg.scope);
        MyUtils.useId(arg1,arg);
        // Add arg2 uses
        String arg2 = MyUtils.getId(n.arg2,arg.scope);
        MyUtils.useId(arg2,arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(Multiply n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add arg1 uses
        String arg1 = MyUtils.getId(n.arg1,arg.scope);
        MyUtils.useId(arg1,arg);
        // Add arg2 uses
        String arg2 = MyUtils.getId(n.arg2,arg.scope);
        MyUtils.useId(arg2,arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(LessThan n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add arg1 uses
        String arg1 = MyUtils.getId(n.arg1,arg.scope);
        MyUtils.useId(arg1,arg);
        // Add arg2 uses
        String arg2 = MyUtils.getId(n.arg2,arg.scope);
        MyUtils.useId(arg2,arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn base;
     *   int offset; */
    public ReturnIntervals visit(Load n, Env arg) {
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add base uses
        String base = MyUtils.getId(n.base,arg.scope);
        MyUtils.useId(base,arg);
        // Add offset uses
        // None because offset is an integer
         return null;
        }

    /*   IdentifieReturn base;
     *   int offset;
     *   IdentifieReturn rhs; */
    public ReturnIntervals visit(Store n, Env arg) { 
        // Add base uses
        String base = MyUtils.getId(n.base,arg.scope);
        MyUtils.useId(base,arg);
        // Add offset uses
        // None because offset is an integer
        // Add rhs definitions
        String id = MyUtils.getId(n.rhs,arg.scope);
        MyUtils.useId(id, arg);
        // Add rhs uses
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn rhs; */
    public ReturnIntervals visit(Move_Id_Id n, Env arg) {
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add rhs uses
        String rhs = MyUtils.getId(n.rhs,arg.scope);
        MyUtils.useId(rhs,arg);
        return null; 
        }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn size; */
    public ReturnIntervals visit(Alloc n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add size uses
        String size = MyUtils.getId(n.size,arg.scope);
        MyUtils.useId(size,arg);
        return null; 
    }

    /*   IdentifieReturn content; */
    public ReturnIntervals visit(Print n, Env arg) { 
        // Add content uses
        String content = MyUtils.getId(n.content,arg.scope);
        MyUtils.useId(content,arg);        
        return null; 
    }

    /*   String msg; */
    public ReturnIntervals visit(ErrorMessage n, Env arg) { 
        // Add msg uses
        // None because msg is a string
        return null; 
    }

    /*   Label label; */
    public ReturnIntervals visit(Goto n, Env arg) {
        String curLabel = n.label.toString();
        if (arg.labelToInstrs.containsKey(curLabel)) {
            HashSet<String> ids = arg.labelToInstrs.get(curLabel);
            //Backwards jump, so we update the intervals to the current line number
            int lineNumber = arg.scope.lineNumber;
            for (String id: ids) {
                if (arg.scope.parameters.contains(id)) {
                    throw new RuntimeException("Use of parameter " + id + " in goto instruction at line " + lineNumber);
                }
                if (arg.returnIntervals.intervals.containsKey(id)){
                    arg.returnIntervals.intervals.get(id).end = lineNumber-1;
                }

            }
            for (String id: arg.labelToArgInstrs.get(curLabel)) {
                if (arg.scope.parameters.contains(id)) {
                    throw new RuntimeException("Use of parameter " + id + " in goto instruction at line " + lineNumber);
                }
                if (arg.scope.paramIntervals.containsKey(id)){
                    arg.scope.paramIntervals.get(id).end = lineNumber-1;
                }
                if (arg.scope.paramIntervals.containsKey(id)){
                    arg.scope.paramIntervals.get(id).end = lineNumber-1;
                }
            }
        // if the label is not in the map, it means that it is a forward jump
        // We do not need to do anything here for forward jumps
        }
        return null;
    }

    /*   IdentifieReturn condition;
     *   Label label; */
    public ReturnIntervals visit(IfGoto n, Env arg) { 
        String id = MyUtils.getId(n.condition,arg.scope);
        MyUtils.useId(id,arg);
        String curLabel = n.label.toString();
        if (arg.labelToInstrs.containsKey(curLabel)) {
            HashSet<String> ids = arg.labelToInstrs.get(curLabel);
            //Backwards jump, so we update the intervals to the current line number
            int lineNumber = arg.scope.lineNumber;
            for (String id2: ids) {
                arg.returnIntervals.intervals.get(id2).end = lineNumber-1;
            }
        }
        if (arg.labelToArgInstrs.containsKey(curLabel)) {
            if (arg.scope.parameters.contains(id)) {
                    throw new RuntimeException("Use of parameter " + id + " in goto instruction at line " + arg.scope.lineNumber);
                }
            if (arg.scope.paramIntervals.containsKey(id)){
                arg.scope.paramIntervals.get(id).end = arg.scope.lineNumber-1;
            }
        }
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn callee;
     *   List<Identifier> args; */
    public ReturnIntervals visit(Call n, Env arg) { 
        // Add lhs definitions
        String id = MyUtils.getId(n.lhs,arg.scope);
        MyUtils.defId(id, arg);
        // Add callee uses
        String callee = MyUtils.getId(n.callee,arg.scope);
        MyUtils.useId(callee,arg);
        // Add args uses
        for (Identifier arg1: n.args) {
            String arg2 = MyUtils.getId(arg1,arg.scope);
            MyUtils.useId(arg2,arg);
        }
        arg.callSites.add(arg.scope.lineNumber);
        return null; 
    }
}
