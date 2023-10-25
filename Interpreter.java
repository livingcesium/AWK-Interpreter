import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter {
    private static class LineManager{
        List<String> lines;
        
        public LineManager(List<String> lines){
            this.lines = lines;
        }
        
        public boolean splitAndAssign(){
            if(lines.isEmpty())
                return false;
            
            String line = getNext();
            String separator = globalVariables.get("FS").value;
            String[] split = line.split(separator);
            
            int i = 0;
            for(String s : split){
                globalVariables.put("$"+ i, new InterpreterDataType(s));
            }
            // NF = i + 1
            globalVariables.put("NF", new InterpreterDataType(Integer.toString(i + 1)));
            // NR++
            globalVariables.put("NR", new InterpreterDataType(Integer.toString(Integer.parseInt(globalVariables.get("NR").value) + 1)));
            return true;
        }
        private String getNext(){
            globalVariables.put("FNR", new InterpreterDataType(Integer.toString(Integer.parseInt(globalVariables.get("FNR").value) + 1)));
            return lines.remove(0);
        }
    }
    
    public static HashMap<String, InterpreterDataType> globalVariables;
    Map.Entry<String, FunctionDefinitionNode> functions;
    
    
}
