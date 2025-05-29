import sparrow.*;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import IR.token.Register;  
import IR.token.Identifier;

public class MyUtils {
    static String getId(Identifier id,IntervalScope scope) {
        return id.toString()+"_"+scope.functionNumber;
    }
    static void defId(String defId,Env arg) {
        // Add to labels in case of loop 
        for (HashSet<String> idsPerLabel: arg.labelToInstrs.values()) {
            if (!arg.scope.parameters.contains(defId)) {
                idsPerLabel.add(defId);
            }
        }
        if (!arg.returnIntervals.intervals.containsKey(defId)) {
            arg.returnIntervals.intervals.put(defId, new Interval(arg.scope.lineNumber, -1, defId, arg.scope.functionNumber));
        }
    }
    static void useId(String useId,Env arg) {
        // Add to labels in case of loop 
        for (HashSet<String> idsPerLabel: arg.labelToInstrs.values()) {
            if (!arg.scope.parameters.contains(useId)) {
                idsPerLabel.add(useId);
        }
    }
        if (arg.returnIntervals.intervals.containsKey(useId)) {
            arg.returnIntervals.intervals.get(useId).end = arg.scope.lineNumber;
        } else {
            if (arg.scope.parameters.contains(useId)) {
                //arg.scope.paramIntervals.get(useId).end = arg.scope.lineNumber;
               // throw new RuntimeException("Use of identifier " + useId + " before definition in scope " + arg.scope);
            }
        }   
    }
    
    static ArrayList<String> sortByStartPoint(HashMap<String, Interval> intervals) {
        ArrayList<String> sortedIntervals = new ArrayList<String>(intervals.keySet());
        sortedIntervals.sort((a, b) -> {
            int startA = intervals.get(a).start;
            int startB = intervals.get(b).start;
            return Integer.compare(startA, startB);
        });
        return sortedIntervals;
    }
    // Allocates a register for the given identifier.
    // If the identifier is already allocated to a register, it moves it to the new register.
    // If the identifier is spilled, it allocates it to the identifier on the stack.
    public static void assignt0ToLhs(Identifier id, Env arg) {
        String idSaltedLHS = MyUtils.getId(id, arg.scope);
        sparrowv.Instruction instr;

        Map<String, String> spillMap = arg.linearScanAllocator.getSpillMap();
        Map<String,String> idToRegister = arg.linearScanAllocator.getAssignment();
        if (arg.scope.parameters.contains(idSaltedLHS)) {
            // If the id is a parameter, we need to assign t0 to the identifier on the stack
            instr = new sparrowv.Move_Id_Reg(
                new Identifier(idSaltedLHS),
                new Register("t0")
            );
            arg.addInst(instr, new Register("t0"));
            return;
        }
        if (!spillMap.containsKey(idSaltedLHS)) {
            // If the id is not in the spill map, we need to add it to the spill map
            System.err.println("Id " + idSaltedLHS + " is not in the spill map, adding it now.");
        }
        
        if (spillMap.containsKey(idSaltedLHS) && spillMap.get(idSaltedLHS).equals("Spill")) {
            // If the id is already spilled, we allocate it to the identifier on the stack
            instr = new sparrowv.Move_Id_Reg(
                new Identifier(idSaltedLHS),
                new Register("t0")
            );
            arg.addInst(instr, new Register("t0"));
            return;
        }
        if (spillMap.containsKey(idSaltedLHS) && spillMap.get(idSaltedLHS).equals("Register")) {
            // If the id is allocated to a register, we need to use that register instead, and put the refister into there
            if (!idToRegister.containsKey(idSaltedLHS)) {
                throw new RuntimeException("Id " + idSaltedLHS + " is not allocated to a register?!/1" +
                    " This should not happen if the linear scan allocator is working correctly.");
            }
            String allocatedReg = idToRegister.get(idSaltedLHS);
            instr = new sparrowv.Move_Reg_Reg(
                new Register(allocatedReg),
                new Register("t0")
            );
            arg.addInst(instr, new Register(allocatedReg));
            return;
        }
        if (spillMap.containsKey(idSaltedLHS) && spillMap.get(idSaltedLHS).equals("arg")) {
            return;
        }
        throw new RuntimeException("Unknown allocation state for id: " + idSaltedLHS);
    }
    // We need a return ID to be type ID, so we need to return the ID to the stack.
    // If the ID is spilled, we do not need to do anything, as it is already on the stack.
    // If the ID is in a register, we move it from the register to the ID.
    // If the ID is a parameter, we do not need to allocate it to a register, as it is already in the stack.
    public static void returnToID(Identifier id,Env arg) {
        String idSalted = getId(id, arg.scope);
        if (arg.scope.parameters.contains(idSalted)) {
            // If the id is a parameter, we do not need to allocate it to a register
            return;
        }
        Map<String, String> spillMap = arg.linearScanAllocator.getSpillMap();
        Map<String,String> idToRegister = arg.linearScanAllocator.getAssignment();
        if (spillMap.get(idSalted).equals("Register")) {
            // If the value is in a register, we grab it and move it from the register to the id
            sparrowv.Instruction inst = new sparrowv.Move_Id_Reg(
                new Identifier(idSalted),
                new Register(idToRegister.get(idSalted))
            );
            arg.addInst(inst, new Register(idToRegister.get(idSalted)));
        }
        // No need to handle the spill case here, as the id is already on the stack
    }



    // static ArrayList<String> LinearScanRegisterAllocation(HashMap<String, Interval> idToInterval,int numRegisters,Env argu) {
    //     ArrayList<String> sortedIds = sortByStartPoint(idToInterval);
    //     ArrayList<String> allocatedIdToReg = new ArrayList<String>();
    //     Queue<String> activeIdToRegs = new java.util.LinkedList<String>();
    //     for (String id: sortedIds) {
    //         MyUtils.expireOldIntervals(activeIdToRegs, idToInterval, argu.scope.lineNumber);
    //         if (activeIdToRegs.size() == numRegisters) {
    //             MyUtils.spillAtInterval(id, activeIdToRegs, argu.returnIntervals, allocatedIdToReg,activeIdToRegs);
    //         } else {

    //         }
    //         // Assign to active intervals from free registers
    //         if (MyUtils.spansCall(argu.returnIntervals.intervals.get(id), argu.callSites)){
    //             //if the interval spans a call, we take from callee saved registers first
    //             MyUtils.attemptPlacement(argu.CALLEE_SAVED, argu.ARGUMENT_REGISTERS, argu.CALLER_SAVED, allocatedRegisters);
    //         } else {
    //             // If it does not span a call, we can use caller saved registers first
    //             MyUtils.attemptPlacement(argu.CALLER_SAVED, argu.ARGUMENT_REGISTERS, argu.CALLEE_SAVED, allocatedRegisters);
    //         }
    //     }
    //     return allocatedRegisters;
    // }

    // static boolean spansCall(Interval interval, Set<Integer> callSites) {
    //     for (int call : callSites) {
    //         if (call >= interval.start && call <= interval.end) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // // Spill the interval with the latest end point
    // static void spillAtInterval(String id, ArrayList<String> idToInterval, ReturnIntervals returnIntervals,ArrayList<String> allocatedIdToReg, ArrayList<String> activeIdsToRegs) {
    //     // This is the interval we will spill
    //     String spillRegName = activeIdsToRegs.poll();
    //     Interval idInterval = returnIntervals.intervals.get(id);
    //     Interval spillInterval = returnIntervals.intervals.get(spillRegName);
    //     if (spillInterval.end > idInterval.end) {
    //         idToRegister.
    //         activeIntervals.add(spill); // Reinsert if it has a later end point
    //     } 
    //     activeIntervals.remove(spill);
    //     char spillClass = spill.charAt(0);
    //     if (spillClass == 't'){
    //         argu.CALLEE_SAVED.add(spill);
    //     } else if (spillClass == 's') {
    //         argu.CALLER_SAVED.add(spill);
    //     } else if (spillClass == 'a') {
    //         argu.ARGUMENT_REGISTERS.add(spill);
    //     } else {
    //         throw new RuntimeException("Unknown register class for spill: " + spill);
    //     }
    //     // Optionally, handle the spill (e.g., mark as spilled in Env or elsewhere)
    // }

    // // Removes intervals from activeIntervals whose end is before the current line number
    // static void expireOldIntervals(Queue<String> activeIntervals, HashMap<String, Interval> intervals, int currentLine) {
    //     ArrayList<String> toRemove = new ArrayList<>();
    //     for (String intervalId : activeIntervals) {
    //         Interval interval = intervals.get(intervalKey);
    //         if (interval != null && interval.end < currentLine) {
    //             toRemove.add(intervalKey);
    //         }
    //     }
    //     activeIntervals.removeAll(toRemove);
    // }

    // static boolean attemptPlacement(ArrayList<String> regList1 , ArrayList<String> regList2,ArrayList<String> regList3,ArrayList<String> allocatedRegisters) {
    //     if (regList1.size() > 0) {
    //         String reg = regList1.remove(0);
    //         allocatedRegisters.add(reg);
    //     } else if (regList2.size() > 0) {
    //         String reg = regList2.remove(0);
    //         allocatedRegisters.add(reg);
    //     } else if (regList3.size() > 0) {
    //         String reg = regList3.remove(0);
    //         allocatedRegisters.add(reg);
    //     } else {
    //         // spill
    //         return false;
    //     }
    //     return true;
    // }
}
