import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import minijava.MiniJavaParser;
import IR.syntaxtree.Node;
import IR.token.Identifier;
import java.util.Arrays;
import sparrowv.*;
import IR.token.Register;

public class S2SV {
    public static void main(String [] args) throws Exception {
        try {
            Boolean debugMode = false; // Set to true if you want to see the debug output
            Boolean fileInputMode = false; // Set to true if you want to read from a file
            //normal input mode
            InputStream in = System.in;

            // load the file into InputStream bc debugger weird

            //comment out below this if you want to run the program normally
            if (fileInputMode) {
                File file = new File("testcases/hw4/T7.sparrow");
                in = new FileInputStream(file);
            } 
            new SparrowParser(in);
            Node root = SparrowParser.Program();
            SparrowConstructor constructor = new SparrowConstructor();
            root.accept(constructor);
            sparrow.Program program = constructor.getProgram();
            //Print the program to the console
            // System.err.println(program.toString());
            Env env = new Env(new IntervalScope());
            program.accept(new FastLiveVisitor(),env);
            // System.err.println("Return intervals: ");
            int numberOfRegisters = 15;
            LinearScanAllocator linearScanAllocator = new LinearScanAllocator(env.callSites, new ArrayList<Interval>(env.returnIntervals.intervals.values()));
            // System.err.println("env.returnIntervals.intervals.get(\"aux02_10\"): " + env.returnIntervals.intervals.get("aux02_10").start + " - " + env.returnIntervals.intervals.get("aux02_10").end);
            linearScanAllocator.allocate();
            // System.err.println("env.returnIntervals.intervals.get(\"ret_val_10\"): " + env.returnIntervals.intervals.get("ret_val_10").start + " - " + env.returnIntervals.intervals.get("ret_val_10").end);
            // System.err.println("env.returnIntervals.intervals.get(\"v100_10\"): " + env.returnIntervals.intervals.get("v100_10").start + " - " + env.returnIntervals.intervals.get("v100_10").end);
            env.linearScanAllocator = linearScanAllocator;
            env.debugMode = debugMode;
            env.linearScanAllocator.getAssignment().putAll(env.paramIdToReg);
            env.linearScanAllocator.getSpillMap().putAll(env.paramSpillMap);
            // System.err.println(linearScanAllocator.getAssignment().get("aux02_10"));
            // System.err.println(linearScanAllocator.getAssignment().get("ret_val_10"));
            // System.err.println(linearScanAllocator.getAssignment().get("v100_10"));
            // System.err.println("Allocated registers: ");
            TranslateVisitor translateVisitor = new TranslateVisitor();
            program.accept(translateVisitor, env);
            sparrowv.Program translatedProgram = env.program;
            System.out.println(translatedProgram.toString());
        }
            catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



// Definitions
enum DefOrUse {
    Definition,
    Use
}
class IntervalScope {
    int lineNumber;
    DefOrUse defOrUse;
    int functionNumber;
    public HashMap<String, Interval> paramIntervals = new HashMap<String, Interval>(); // Map of parameter intervals for the current function
    HashSet<String> parameters = new HashSet<String>(); // List of parameters for the current function
    public IntervalScope() {
        this.lineNumber = 0;
        this.functionNumber = 0;
        this.defOrUse = null;
    }
}

class Env {
    IntervalScope scope;
    
    ReturnIntervals returnIntervals;
    HashMap<String, HashSet<String>> labelToInstrs = new HashMap<String, HashSet<String>>();
    HashSet<Integer> callSites = new HashSet<Integer>(); 
    ArrayList<String> allocated = new ArrayList<String>();
    sparrowv.Program program;
    ArrayList<sparrowv.Instruction> curInstList;
    sparrowv.Block curBlock;
    ArrayList<String> callerSaved = new ArrayList<>(List.of("t2", "t3", "t4", "t5"));
    ArrayList<String> calleeSaved = new ArrayList<>(List.of("s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"));
    ArrayList<String> argRegisters = new ArrayList<>(List.of("a2", "a3", "a4", "a5", "a6", "a7"));
    LinearScanAllocator linearScanAllocator;
    boolean debugMode = false; // Set to true if you want to see the debug output
    public HashMap<String, String> paramIdToReg = new HashMap<String, String>(); // Map to store parameter identifiers to registers
    
    public HashMap<String, String> paramSpillMap = new HashMap<String, String>(); // Map to store parameter identifiers to stack locations
    // Adding linear scan allocator to the environment
    public Env(IntervalScope scope) {
        this.scope = scope;
        this.returnIntervals = new ReturnIntervals();
        this.labelToInstrs = new HashMap<String, HashSet<String>>();
        this.callSites = new HashSet<Integer>();
        this.program = new sparrowv.Program(new ArrayList<FunctionDecl>());
        this.curInstList = null;
        this.curBlock = null;

    }

    public void addInst(Instruction inst, Register printVal) {
        this.curInstList.add(inst);
        if (this.debugMode){
            if (printVal != null) {
                System.err.println("Adding instruction: " + inst.toString() + " with print value: " + printVal.toString());
                this.curInstList.add(new sparrowv.Print(printVal));
            } else {
                System.err.println("Adding instruction: " + inst.toString() + " with no print value.");
            }
        }

    }

}

class ReturnIntervals {
    public HashMap<String, Interval> intervals;
    public Instruction inst; // The instruction that returns from the function

    public ReturnIntervals() {
        this.intervals = new HashMap<String,Interval>();
    }
    public ReturnIntervals(Instruction inst) {
        this.inst = inst;
    }
}
class Interval {
    int start;
    int end;
    String variable; // Variable name for identification
    String registerOrLocation; // Register or memory location assigned to this interval
    int functionNum; // Name of the function this interval belongs to
    public Interval(int start, int end, String idString, int functionNumber) {
        this.start = start;
        this.end = end;
        this.variable = idString; // Will be set later
        this.functionNum = functionNumber;
    }
}
class RegisterCategories {
    // Caller-saved registers: save/restore around each function call
    
}