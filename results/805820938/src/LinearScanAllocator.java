import java.lang.reflect.Array;
import java.util.*;

public class LinearScanAllocator {
    // Separate pools for different register types
    Queue<String> callerSaved = new LinkedList<>(List.of("t2", "t3", "t4", "t5"));
    Queue<String> calleeSaved = new LinkedList<>(List.of("s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"));
    Queue<String> argRegisters = new LinkedList<>(List.of("a2", "a3", "a4", "a5", "a6", "a7"));

    // Map to remember which register came from which group
    Map<String, String> registerType = new HashMap<>(); // register -> caller/callee/arg

    // Final assignment of identifiers to register or stack
    HashMap<String, String> identifierToLocation = new HashMap<>();

    // Active intervals (sorted by end)
    List<Interval> active = new ArrayList<Interval>();

    // Set of instruction indices where function calls occur
    Set<Integer> callSites;

    ArrayList<Interval> intervals; // List of intervals to allocate

    // Map to remember which call site has active registers
    public HashMap<Integer,ArrayList<String>> callSiteToActiveList = new HashMap<Integer,ArrayList<String>>();
    public HashMap<Integer,HashSet<String>> functionNumToCalleeUsedRegisters = new HashMap<Integer,HashSet<String>>();

    // Map to remember which identifier is spilled
    HashMap<String, String> identifierToRegOrSpill = new HashMap<>(); // identifier -> "register" or "Spill "

    public LinearScanAllocator(Set<Integer> callSites, ArrayList<Interval> intervals) {
        this.callSites = callSites;
        this.intervals = intervals;
    }

    public void allocate() {
        this.intervals.sort(Comparator.comparingInt(i -> i.start));
        for (Interval i : this.intervals) {
            expireOldIntervals(i);
            String assigned;
            int callSite = spansCall(i);
            if (callSite > -1) {
                assigned = attemptPlacement(callerSaved,calleeSaved,registerType,"caller","callee");
            } else {
                assigned = attemptPlacement(calleeSaved,callerSaved ,registerType,"callee","caller");
            }
            if (assigned.equals("spill")) {
                spillAtInterval(i);
            } else {
                i.registerOrLocation = assigned;
                identifierToLocation.put(i.variable, assigned);
                identifierToRegOrSpill.put(i.variable, "Register");
                active.add(i);
                active.sort(Comparator.comparingInt(iv -> iv.end));
            }
        }
        // After processing all intervals, we need to handle the call sites
        for (int callSite : callSites) {
            ArrayList<String> regsToSave = new ArrayList<>();
            for (Interval iv : intervals) {
                String loc = iv.registerOrLocation;
                // only live, in-register, caller-saved values
                if (callSite > iv.start
                && callSite < iv.end
                && "caller".equals(registerType.get(loc))
                ) {
                    regsToSave.add(loc);
                }
            }
                //System.err.println("Call site " + callSite + " has active registers: " + regsToSave);
            callSiteToActiveList.put(callSite, regsToSave);
        }
        
        for (String identifier: identifierToLocation.keySet()) {
            String regOrLoc = identifierToLocation.get(identifier);
            if (registerType.containsKey(regOrLoc) && registerType.get(regOrLoc).equals("callee")){
                for (Interval iv : intervals) {
                    //We have the identifier and it's assigned register
                    if (iv.variable.equals(identifier)) {
                        int functionNum = iv.functionNum;
                        if (!functionNumToCalleeUsedRegisters.containsKey(functionNum)) {
                            functionNumToCalleeUsedRegisters.put(functionNum, new HashSet<>());
                        }
                        functionNumToCalleeUsedRegisters.get(functionNum).add(identifierToLocation.get(identifier));
                        break; // No need to check further intervals for this identifier
                    }
                }
            }
            }
    }

    private void expireOldIntervals(Interval i) {
        Iterator<Interval> it = active.iterator();
        while (it.hasNext()) {
            Interval j = it.next();
            if (j.end >= i.start) break;
            String reg = j.registerOrLocation;
            String type = registerType.get(reg);
            if ("caller".equals(type)) {
                callerSaved.add(reg);
            } else if ("callee".equals(type)) {
                calleeSaved.add(reg);
            } else if ("arg".equals(type)) {
                argRegisters.add(reg);
            }
            it.remove();
        }
    }

    private void spillAtInterval(Interval i) {
        if (active.isEmpty()) {
            String stackLoc = i.variable + "_stack";
            i.registerOrLocation = stackLoc;
            identifierToLocation.put(i.variable, stackLoc);
            identifierToRegOrSpill.put(i.variable, "Spill");
            return;
        }
        Interval spill = active.get(active.size() - 1);
        if (spill.end > i.end) {
            i.registerOrLocation = spill.registerOrLocation;
            identifierToLocation.put(i.variable, i.registerOrLocation);
            identifierToRegOrSpill.put(i.variable, "Register");
            String stackLoc = spill.variable+ "_stack";
            spill.registerOrLocation = stackLoc;
            identifierToLocation.put(spill.variable, stackLoc);
            identifierToRegOrSpill.put(spill.variable, "Spill");
            active.remove(spill);
            active.add(i);
            active.sort(Comparator.comparingInt(iv -> iv.end));
        } else {
            String stackLoc = i.variable+ "_stack";
            i.registerOrLocation = stackLoc;
            identifierToLocation.put(i.variable, stackLoc);
            identifierToRegOrSpill.put(i.variable, "Spill");
        }
    }

    public int spansCall(Interval i) {
        for (int call : callSites) {
            if (call >= i.start && call <= i.end) {
                return call;
            }
        }
        return -1;
    }

    public Map<String, String> getAssignment() {
        return identifierToLocation;
    }
    public Map<String, String> getSpillMap() {
        return identifierToRegOrSpill;
    }
    static String attemptPlacement(Queue<String> regList1, Queue<String> regList2, Map<String, String> registerType, String group1, String group2) {
        if (!regList1.isEmpty()) {
            String reg = regList1.poll();
            registerType.put(reg, group1);
            return reg;
        } else if (!regList2.isEmpty()) {
            String reg = regList2.poll();
            registerType.put(reg, group2);
            return reg;
        } else {
            // No registers available, need to spill
            return "spill";
        }
    }
}

