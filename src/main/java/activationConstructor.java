import IR.token.Register;
import sparrowv.*;
import sparrowv.visitor.ArgRetVisitor;
public class activationConstructor<T,U> implements ArgRetVisitor<Env, String> {

    public activationConstructor() {
        super();
    }

          /*   List<FunctionDecl> funDecls; */
    @Override
    public String visit(Program n, Env env) {
        // Handle the actißvation of a program
        // This could involve initializing the program's environment or setting up its state
        // Additional logic for activation can be added here
        return "Program activated";
    }


    /*   Program parent;
   *   FunctionName functionName;
   *   List<Identifier> formalParameters;
   *   Block block; */
    @Override
    public String visit(FunctionDecl n, Env env) {
        // Handle the activation of a function declaration
        // This could involve setting up the function's parameters and local variables
        // Additional logic for activation can be added here
        return "Function activated";
    }


    /*   FunctionDecl parent;
   *   List<Instruction> instructions;
   *   Identifier return_id; */
    @Override
    public String visit(Block n, Env env) {
        // Handle the activation of a block
        // This could involve initializing the block's local variables and preparing for execution
        // Additional logic for activation can be added here
        return "Block activated";
    }

      /*   Label label; */
    @Override 
    public String visit(LabelInstr n, Env env) {
        // Handle the activation of a label instruction
        // This could involve setting up the label for use in the current scope
        // Additional logic for activation can be added here
        return "Label activated";
    }

    /*   Register lhs;
     *   int rhs; */
    @Override
    public String visit(Move_Reg_Integer n, Env env) {
        // Handle the activation of a move instruction from register to integer
        // This could involve setting the register's value to the integer
        // Additional logic for activation can be added here
        return "Move activated";
    }

    /*   Register lhs;
     *   FunctionName rhs; */
    @Override
    public String visit(Move_Reg_FuncName n, Env env) {
        // Handle the activation of a move instruction from register to function name
        // This could involve setting the register's value to the function name
        // Additional logic for activation can be added here
        return "Move activated";
    }
    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public String visit(Add n, Env env) {
        // Handle the activation of an addition instruction
        // This could involve performing the addition and storing the result in the left-hand side register
        // Additional logic for activation can be added here
        return "Addition activated";
    }
    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public String visit(Subtract n, Env env) {
        // Handle the activation of a subtraction instruction
        // This could involve performing the subtraction and storing the result in the left-hand side register
        // Additional logic for activation can be added here
        return "Subtraction activated";
    }
    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public String visit(Multiply n, Env env) {
        // Handle the activation of a multiplication instruction
        // This could involve performing the multiplication and storing the result in the left-hand side register
        // Additional logic for activation can be added here
        return "Multiplication activated";
    }
    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public String visit(LessThan n, Env env) {
        // Handle the activation of a less-than comparison instruction
        // This could involve performing the comparison and storing the result in the left-hand side register
        // Additional logic for activation can be added here
        return "LessThan activated";
    }
    /*   Register lhs;
     *   Register base;
     *   int offset; */
    @Override
    public String visit(Load n, Env env) {
        // Handle the activation of a load instruction
        // This could involve loading a value from memory into the left-hand side register
        // Additional logic for activation can be added here
        return "Load activated";
    }
    /*   Register base;
     *   int offset;
     *   Register rhs; */
    @Override
    public String visit(Store n, Env env) {
        // Handle the activation of a store instruction
        // This could involve storing a value from the right-hand side register into memory at the specified base and offset
        // Additional logic for activation can be added here
        return "Store activated";
    }
    /*   Register lhs;
     *   Register rhs; */
    @Override
    public String visit(Move_Reg_Reg n, Env env) {
        // Handle the activation of a move instruction from one register to another
        // This could involve copying the value from the right-hand side register to the left-hand side register
        // Additional logic for activation can be added here
        return "Move activated";
    }
    /*   Identifier lhs;
     *   Register rhs; */
    @Override
    public String visit(Move_Id_Reg n, Env env) {
        // Handle the activation of a move instruction from an identifier to a register
        // This could involve setting the value of the identifier to the value in the register
        // Additional logic for activation can be added here
        return "Move activated";
    }
    /*   Register lhs;
     *   Identifier rhs; */
    @Override
    public String visit(Move_Reg_Id n, Env env) {
        // Handle the activation of a move instruction from a register to an identifier
        // This could involve setting the value of the register to the value in the identifier
        // Additional logic for activation can be added here
        return "Move activated";
    }
    /*   Register lhs;
     *   Register size; */
    @Override
    public String visit(Alloc n, Env env) {
        // Handle the activation of an allocation instruction
        // This could involve allocating memory for the specified size and storing the address in the left-hand side register
        // Additional logic for activation can be added here
        return "Allocation activated";
    }
    /*   Register content; */
    @Override
    public String visit(Print n, Env env) {
        // Handle the activation of a print instruction
        // This could involve printing the value in the specified register
        // Additional logic for activation can be added here
        return "Print activated";
    }
    /*   Label label; */
    @Override
    public String visit(ErrorMessage n, Env env) {
        // Handle the activation of an error message instruction
        // This could involve printing or logging the error message
        // Additional logic for activation can be added here
        return "Error message activated";
    }
    /*Label label; */
    @Override
    public String visit(Goto n, Env env) {
        // Handle the activation of a goto instruction
        // This could involve jumping to the specified label in the program
        // Additional logic for activation can be added here
        return "Goto activated";
    }
    /*   Register condition;
   *   Label label; */
    @Override
    public String visit(IfGoto n, Env env) {
        // Handle the activation of a conditional goto instruction
        // This could involve checking the condition and jumping to the specified label if true
        // Additional logic for activation can be added here
        return "Conditional goto activated";
    }
    
    /*   Register lhs;
   *   Register callee;
   *   List<Identifier> args; */
    @Override  
    public String visit(Call n, Env env) {
        // Handle the activation of a function call instruction
        // This could involve preparing the arguments and calling the specified function
        // Additional logic for activation can be added here
        return "Function call activated";
    }
    



    
}
