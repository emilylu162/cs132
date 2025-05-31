import java.util.*;
import sparrowv.Instruction;
import sparrowv.Move_Reg_Id;
import sparrowv.Move_Reg_Reg;
import IR.token.Identifier;
import IR.token.Register;;
public class ArgumentShuffler {

    public static List<Instruction> shuffle(Map<String, String> moves, String tempVar) {
        List<Instruction> instructions = new ArrayList<>();
        Map<String, String> currentMoves = new HashMap<>(moves);
        Set<String> visited = new HashSet<>();

        while (!currentMoves.isEmpty()) {
            boolean progress = false;

            // Step 1: Apply all non-conflicting moves (dest not used as source elsewhere)
            Iterator<Map.Entry<String, String>> iter = currentMoves.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String src = entry.getKey();
                String dst = entry.getValue();

                boolean dstUsedAsSrc = currentMoves.containsKey(dst);
                if (!dstUsedAsSrc) {
                    if (dst.endsWith("_stack_saved")){
                        throw new IllegalArgumentException("Cycle detected with stack saved variable: " + dst);
                    }
                    if (src.endsWith("_stack_saved")) {
                        instructions.add(new Move_Reg_Id(
                            new Register(dst),
                            new Identifier(removeEnding(src, "_stack_saved"))
                        ));
                    } else {
                        instructions.add(new Move_Reg_Reg(
                            new Register(dst),
                            new Register(src)
                        ));
                    }
                    // instructions.add(new Move_Reg_Reg(
                    //     new Register(dst),
                    //     new Register(src)
                    // ));
                    visited.add(dst);
                    iter.remove();
                    progress = true;
                }
            }

            // Step 2: Handle cycle
            if (!progress && !currentMoves.isEmpty()) {
                // Pick any move in the cycle
                Map.Entry<String, String> first = currentMoves.entrySet().iterator().next();
                String cycleStart = first.getKey();
                String cycleDest = first.getValue();

                // Save the value at the cycle start in temp
                if (cycleStart.endsWith("_stack_saved")){
                    instructions.add(new Move_Reg_Id(
                        new Register(tempVar),
                        new Identifier(removeEnding(cycleStart, "_stack_saved"))
                    ));
                } else {
                    instructions.add(new Move_Reg_Reg(
                        new Register(tempVar),
                        new Register(cycleStart)
                    ));
                }
                // Follow the cycle and shift values
                String prev = cycleStart;
                String curr = cycleDest;

                while (!curr.equals(cycleStart)) {
                    if (prev.endsWith("_stack_saved")){
                        throw new IllegalArgumentException("Cycle detected with stack saved variable: " + prev);
                    }
                    if (curr.endsWith("_stack_saved")){
                        instructions.add(new Move_Reg_Id(
                            new Register(prev),
                            new Identifier(removeEnding(curr, "_stack_saved"))
                        ));
                    } else {
                        instructions.add(new Move_Reg_Reg(
                            new Register(prev),
                            new Register(curr)
                        ));
                    }
                    prev = curr;
                    curr = currentMoves.get(curr);
                    currentMoves.remove(prev);
                }

                // Complete the cycle
                if (prev.endsWith("_stack_saved")){
                        throw new IllegalArgumentException("Cycle detected with stack saved variable: " + prev);
                    }
                instructions.add(new Move_Reg_Reg(
                        new Register(prev),
                        new Register(tempVar)
                    ));
                currentMoves.remove(cycleStart);
            }
        }

        return instructions;
    }
    public static String removeEnding(String str, String suffix) {
    if (str.endsWith(suffix)) {
        return str.substring(0, str.length() - suffix.length());
    }
    return str;
}
}

