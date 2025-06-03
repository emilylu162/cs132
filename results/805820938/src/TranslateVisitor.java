import sparrow.visitor.*;
import sparrow.*;
import IR.syntaxtree.FunctionName;
import IR.token.Identifier;
import IR.token.Label;
import IR.token.Register;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TranslateVisitor implements ArgRetVisitor<Env,ReturnIntervals>{
    /*   List<FunctionDecl> funDecls; */
    public ReturnIntervals visit(Program n, Env argu) { 
        argu.scope.functionNumber = 0;
        argu.scope.lineNumber = 0;
        n.funDecls.forEach((fd) -> {
            fd.accept(this, argu);
        });
        return null; 
    }

    /*   Program parent;
     *   FunctionName functionName;
     *   List<Identifier> formalParameters;
     *   Block block; */
    public ReturnIntervals visit(FunctionDecl n, Env argu) { 
        IR.token.FunctionName functionName = n.functionName;
        List<Identifier> formalParameters = new ArrayList<>();
        argu.scope.functionNumber += 1;
        argu.scope.lineNumber += 1;
        argu.curInstList = new ArrayList<sparrowv.Instruction>(); 

        //Store callee saved registers
        if (!argu.linearScanAllocator.functionNumToCalleeUsedRegisters.containsKey(argu.scope.functionNumber)) {
            argu.linearScanAllocator.functionNumToCalleeUsedRegisters.put(argu.scope.functionNumber, new HashSet<String>());
        }
        // if (n.functionName.name == null){
        //     throw new RuntimeException("Function name is null in function declaration: " + n);
        // }
        
        for (String reg : argu.linearScanAllocator.functionNumToCalleeUsedRegisters.get(argu.scope.functionNumber)) {
            if (argu.scope.functionNumber == 1) {
                // Skip the return address register in main function
                continue;
            }
            argu.addInst(new sparrowv.Move_Id_Reg(
                new Identifier(reg+"_stack"),
                new Register(reg)
            ), new Register(reg));
        }

        // Put paramaters into correct names
        int paramIndex = 1;
        for (Identifier param : n.formalParameters) {
            paramIndex += 1;
            // Allocate registers for formal parameters
            if (paramIndex > 7){
                String paramId = MyUtils.getId(param, argu.scope);
                formalParameters.add(new Identifier(paramId));
            }
            // if (argu.linearScanAllocator.getSpillMap().get(paramId).equals("Register")) {
            //     // If the parameter is allocated to a register, we move it to the register
            //     Register reg = new Register(MyUtils.useReg(param, argu));
            //     argu.addInst(new sparrowv.Move_Reg_Id(reg, new Identifier(MyUtils.getId(param, argu.scope))), reg);
            // // If the parameter is not spilled, we do nothing, as it is already allocated to the stack
            // }
        }

        // Build the block of the function
        n.block.accept(this, argu);


        // Restore callee saved registers
        if (!(argu.scope.functionNumber == 1)) {
            for (String reg : argu.linearScanAllocator.functionNumToCalleeUsedRegisters.get(argu.scope.functionNumber)) {
                argu.addInst(new sparrowv.Move_Reg_Id(
                    new Register(reg),
                    new Identifier(reg+"_stack")
                ), new Register(reg));
            }
    }
        
        
        sparrowv.FunctionDecl fd = new sparrowv.FunctionDecl(functionName,formalParameters,argu.curBlock);
        argu.program.funDecls.add(fd);
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
        // Handle return instruction
        MyUtils.returnToID(n.return_id, arg);
        sparrowv.Block block = new sparrowv.Block(arg.curInstList, new Identifier(MyUtils.getId(n.return_id, arg.scope)));
        arg.curBlock = block;
        return null; 
    }

    /*   Label label; */
    public ReturnIntervals visit(LabelInstr n, Env arg) {
        String label = n.label.toString();
        arg.addInst(new sparrowv.LabelInstr(new Label(label)),null);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   int rhs; */
    public ReturnIntervals visit(Move_Id_Integer  n, Env arg) {
        sparrowv.Instruction inst = new sparrowv.Move_Reg_Integer(
            new Register("t0"),
            n.rhs
        );
        arg.addInst(inst,new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
        
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   FunctionName rhs; */
    public ReturnIntervals visit(Move_Id_FuncName n, Env arg) {
        sparrowv.Instruction inst = new sparrowv.Move_Reg_FuncName(
            new Register("t0"),
            n.rhs
        );
        arg.addInst(inst, new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null; 
        }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(Add n, Env arg) { 
        assignTot0(n.arg1, arg);
        assignToReg(n.arg2, arg, new Register("t1"));
        // Add the addition instruction
        sparrowv.Instruction inst = new sparrowv.Add(
            new Register("t0"),
            new Register("t0"),
            new Register("t1")
        );
        arg.addInst(inst,new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null;  
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(Subtract n, Env arg) { 
        assignTot0(n.arg1, arg);
        assignToReg(n.arg2, arg, new Register("t1"));
        // Add the addition instruction
        sparrowv.Instruction inst = new sparrowv.Subtract(
            new Register("t0"),
            new Register("t0"),
            new Register("t1")
        );
        arg.addInst(inst,new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(Multiply n, Env arg) { 
        assignTot0(n.arg1, arg);
        assignToReg(n.arg2, arg, new Register("t1"));
        // Add the addition instruction
        sparrowv.Instruction inst = new sparrowv.Multiply(
            new Register("t0"),
            new Register("t0"),
            new Register("t1")
        );
        arg.addInst(inst,new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn arg1;
     *   IdentifieReturn arg2; */
    public ReturnIntervals visit(LessThan n, Env arg) { 
        assignTot0(n.arg1, arg);
        assignToReg(n.arg2, arg, new Register("t1"));
        // Add the addition instruction
        sparrowv.Instruction inst = new sparrowv.LessThan(
            new Register("t0"),
            new Register("t0"),
            new Register("t1")
        );
        arg.addInst(inst,new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn base;
     *   int offset; */
    public ReturnIntervals visit(Load n, Env arg) {
        assignTot0(n.base, arg);
        sparrowv.Instruction inst = new sparrowv.Load(
            new Register("t0"),
            new Register("t0"),
            n.offset
        );
        arg.addInst(inst, new Register("t0"));
        // Add lhs definitions
        MyUtils.assignt0ToLhs(n.lhs, arg);
         return null;
        }

    /*   IdentifieReturn base;
     *   int offset;
     *   IdentifieReturn rhs; */
    public ReturnIntervals visit(Store n, Env arg) { 
        assignToReg(n.base, arg, new Register("t1"));
        // inst = new sparrowv.Move_Reg_Id(
        //     new Register("t0"),
        //     new Identifier(MyUtils.useReg(n.rhs, arg))
        // );
        // arg.addInst(inst, new Register("t0"));
        assignTot0(n.rhs, arg);
        // Add the addition instruction
        sparrowv.Instruction inst = new sparrowv.Store(
            new Register("t1"),
            n.offset,
            new Register("t0")
        );
        arg.addInst(inst, new Register("t0"));
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn rhs; */
    public ReturnIntervals visit(Move_Id_Id n, Env arg) {
        assignTot0(n.rhs, arg);
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null; 
        }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn size; */
    public ReturnIntervals visit(Alloc n, Env arg) { 
        // sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
        //     new Register("t0"),
        //     new Identifier(MyUtils.useReg(n.size, arg))
        // );
        assignTot0(n.size, arg);
        // Add the allocation instruction
        sparrowv.Instruction inst = new sparrowv.Alloc(
            new Register("t0"),
            new Register("t0")
        );
        arg.addInst(inst, new Register("t0"));
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null; 
    }

    /*   IdentifieReturn content; */
    public ReturnIntervals visit(Print n, Env arg) { 
        // Add content uses
        // sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
        //     new Register("t0"),
        //     new Identifier(MyUtils.useReg(n.content, arg))
        // );
        // arg.addInst(inst, new Register("t0"));
        assignTot0(n.content, arg);
        sparrowv.Instruction inst = new sparrowv.Print(
            new Register("t0")
        );    
        arg.addInst(inst, null);
        return null; 
    }

    /*   String msg; */
    public ReturnIntervals visit(ErrorMessage n, Env arg) { 
        // Add msg uses
        // None because msg is a string
        sparrowv.Instruction inst = new sparrowv.ErrorMessage(
            n.msg
        );
        arg.addInst(inst, null);
        return null; 
    }

    /*   Label label; */
    public ReturnIntervals visit(Goto n, Env arg) {
        sparrowv.Instruction inst = new sparrowv.Goto(
            new Label(n.label.toString())
        );
        arg.addInst(inst, null);
        return null;
    }

    /*   IdentifieReturn condition;
     *   Label label; */
    public ReturnIntervals visit(IfGoto n, Env arg) { 
        assignTot0(n.condition, arg);
        sparrowv.Instruction inst = new sparrowv.IfGoto(
            new Register("t0"),
            new Label(n.label.toString())
        );
        arg.addInst(inst, null);
        return null; 
    }

    /*   IdentifieReturn lhs;
     *   IdentifieReturn callee;
     *   List<Identifier> args; */
    public ReturnIntervals visit(Call n, Env arg) { 
        // Do the function call
        // Save into temp register the callee function name
        //Get the register for all the args
        List<Identifier> argumentList = new ArrayList<>();
        Map<String, String> spillMap = arg.linearScanAllocator.getSpillMap();
        int funcNum = 1;
        int highestArgReg = 1 ;
        for (Identifier funcArg:n.args){
            funcNum+=1;
            if (funcNum < 8){
                sparrowv.Instruction inst = new sparrowv.Move_Id_Reg(
                        new Identifier("a"+(funcNum)+"_stack"),
                        new Register("a"+(funcNum))
                    );
                    arg.addInst(inst, new Register("a"+(funcNum)));
                highestArgReg = funcNum;
            }
        }
        funcNum = 1;
        ArrayList<Identifier> regFunctionArgs = new ArrayList<>();
        regFunctionArgs.add(null);
        regFunctionArgs.add(null);
        for (Identifier funcArg:n.args){
            funcNum+=1;
            sparrowv.Instruction instr = null;
            if (funcNum < 8){
                String idSaltedString = MyUtils.getId(funcArg, arg.scope);
                Identifier idSalted = new Identifier(idSaltedString);
                String argReg = arg.linearScanAllocator.getAssignment().get(idSaltedString);
                if (arg.scope.paramIntervals.containsKey(argReg) &&
                arg.linearScanAllocator.spansCall(arg.scope.paramIntervals.get(idSaltedString)) == -1){
                    continue; // If the argument is a parameter and does not span the call, we skip it
                }
                if (arg.linearScanAllocator.getAssignment().containsKey(idSaltedString) && 
                argReg.charAt(0) == 'a' &&
                argReg.charAt(1)-'0' < highestArgReg 
                ){
                    instr = new sparrowv.Move_Reg_Id(
                        new Register("t0"),
                        new Identifier(arg.linearScanAllocator.getAssignment().get(idSaltedString)+"_stack")
                    ); 
                    arg.addInst(instr, new Register("t0"));
                } else {
                    assignTot0(funcArg, arg);
                }
                arg.addInst(new sparrowv.Move_Reg_Reg(
                    new Register("a"+(funcNum)),
                    new Register("t0")
                ), new Register("a"+(funcNum)));
            } else {
                String saltedFuncArgID = MyUtils.getId(funcArg, arg.scope);
                if (arg.scope.parameters.contains(saltedFuncArgID) || spillMap.get(saltedFuncArgID).equals("Spill")) {
                    // If the argument is spilled, we don't need to do anyrhing, just pass it as an identifier
                    argumentList.add(new Identifier(saltedFuncArgID));
                } else {
                    // If the argument is allocated to a register, we need to first assign the register value to a stack ID in our code
                    //, and then use that stack identifier as an argument
                    String reg = arg.linearScanAllocator.getAssignment().get(saltedFuncArgID);
                    if (reg.charAt(0) == 'a'){
                        sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
                        new Register("t0"),
                        new Identifier(reg+"_stack")
                        );
                        arg.addInst(inst, new Register("t0"));
                        inst = new sparrowv.Move_Id_Reg(
                        new Identifier(saltedFuncArgID+"_stack"),
                        new Register("t0")
                        );
                        arg.addInst(inst, new Register(reg));
                    } else {
                        sparrowv.Instruction inst = new sparrowv.Move_Id_Reg(
                            new Identifier(saltedFuncArgID+"_stack"),
                            new Register(reg)
                        );
                        arg.addInst(inst, new Register(arg.linearScanAllocator.getAssignment().get(saltedFuncArgID)));
                    } 
                    //Need to fix this to not have it be at the start of the function list
                    argumentList.add(new Identifier(saltedFuncArgID+"_stack"));
                }
            }  
        
        }
        

        // Assign the callee function name to t0
        assignTot0(n.callee, arg);
        // Add caller Storage 
        int callerLineNumber = arg.scope.lineNumber;
        if (arg.linearScanAllocator.callSiteToActiveList.containsKey(callerLineNumber)){
            ArrayList<String> callersToSave = arg.linearScanAllocator.callSiteToActiveList.get(callerLineNumber);
            for (String reg : callersToSave){
                arg.addInst(new sparrowv.Move_Id_Reg(
                    new Identifier(reg+"_stack"),
                    new Register(reg)
                ), new Register(reg));
            }
        }
        // for (String reg : arg.callerSaved){
        //     arg.addInst(new sparrowv.Move_Id_Reg(
        //         new Identifier(reg+"_stack"),
        //         new Register(reg)
        //     ), new Register(reg));
        // }
        // Add arguments Stack storage: shouldn't be necessary if we are using the stack for arguments
        // for (String reg : arg.argRegisters){
        //     arg.addInst(new sparrowv.Move_Id_Reg(
        //         new Identifier(reg+"_stack"),
        //         new Register(reg)
        //     ), new Register(reg));
        // }
        sparrowv.Instruction inst = new sparrowv.Call(
            new Register("t0"),
            new Register("t0"),
            argumentList
        );
        arg.addInst(inst, new Register("t0"));

        // restore arument registers
        funcNum = 1;
        for (Identifier funcArg:n.args){
            funcNum+=1;
            if (funcNum < 8){
                inst = new sparrowv.Move_Reg_Id(
                    new Register("a"+(funcNum)),
                    new Identifier("a"+(funcNum)+"_stack")
                );
                arg.addInst(inst, new Register("a"+(funcNum)));
            }
        }

        // Restore caller saved registers
        if (arg.linearScanAllocator.callSiteToActiveList.containsKey(callerLineNumber)){
            ArrayList<String> callersToSave = arg.linearScanAllocator.callSiteToActiveList.get(callerLineNumber);
            for (String reg : callersToSave){
                arg.addInst(new sparrowv.Move_Reg_Id(
                    new Register(reg),
                    new Identifier(reg+"_stack")
                ), new Register(reg));
            }
        }

        // for (String reg : arg.callerSaved){
        //     arg.addInst(new sparrowv.Move_Reg_Id(
        //         new Register(reg),
        //         new Identifier(reg+"_stack")
        //     ), new Register(reg));
        // }

        
        // Restore argument registers
        // for (String reg : arg.argRegisters){
        //     arg.addInst(new sparrowv.Move_Reg_Id(
        //         new Register(reg),
        //         new Identifier(reg+"_stack")
        //     ), new Register(reg));
        // }
        MyUtils.assignt0ToLhs(n.lhs, arg);
        return null;        
    }

    public static String assignTot0(Identifier arg1, Env arg) {
        String id = MyUtils.getId(arg1, arg.scope);
        Map<String, String> spillMap = arg.linearScanAllocator.getSpillMap();
        Map<String,String> idToRegister = arg.linearScanAllocator.getAssignment();
        if (arg.scope.parameters.contains(id)) {
            // If the id is a parameter, we assign it to t0 directly
            sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
            new Register("t0"),
            new Identifier(id)
            );
            arg.addInst(inst, new Register("t0"));
            return id;
        }
        String isSpill = spillMap.get(id);
        if (isSpill.equals("Spill")) {
            // If the id is spilled, we load it from the stack, so we just assign the id to t0
            sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
                new Register("t0"),
                new Identifier(id)
            );
            arg.addInst(inst, new Register("t0"));
            return id;
        } else if (isSpill.equals("Register")) {
            // If the id is already allocated to a register, we use that register
            Register regRhs = new Register(idToRegister.get(id));
            sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
                new Register("t0"),
                new Identifier(regRhs.toString())
            );
            arg.addInst(inst, new Register("t0"));
            return id;
        } else {
            throw new RuntimeException("Unknown allocation state for id: " + id);
        }
    }

    public static String assignToReg(Identifier arg1, Env arg, Register reg) {
        String id = MyUtils.getId(arg1, arg.scope);
        Map<String, String> spillMap = arg.linearScanAllocator.getSpillMap();
        Map<String,String> idToRegister = arg.linearScanAllocator.getAssignment();
        if (arg.scope.parameters.contains(id)) {
            // If the id is a parameter, we assign it to t0 directly
            sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
            reg,
            new Identifier(id)
            );
            arg.addInst(inst, reg);
            return id;
        }
        String isSpill = spillMap.get(id);
        if (isSpill.equals("Spill")) {
            // If the id is spilled, we load it from the stack, so we just assign the id to t0
            sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
                reg,
                new Identifier(id)
            );
            arg.addInst(inst, reg);
            return id;
        } else if (isSpill.equals("Register")) {
            // If the id is already allocated to a register, we use that register
            Register regRhs = new Register(idToRegister.get(id));
            sparrowv.Instruction inst = new sparrowv.Move_Reg_Id(
                reg,
                new Identifier(regRhs.toString())
            );
            arg.addInst(inst, reg);
            return id;
        } else {
            throw new RuntimeException("Unknown allocation state for id: " + id);
        }
    }
}
