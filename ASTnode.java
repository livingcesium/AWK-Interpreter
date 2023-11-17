import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class ASTnode extends Node implements StatementNode{
    
    protected Node condition;
    protected BlockNode statements;
    
    private String position;
    
    ASTnode(String position){
        this(new ConstantNode<Boolean>(true), null, position);
    }
    ASTnode(BlockNode statements, String position){
        this(new ConstantNode<Boolean>(true), statements, position);
    }
    ASTnode(Node condition, BlockNode statements, String position){
        this.condition = condition;
        this.statements = statements;
        this.position = position;
    }
    
    public Node getCondition(){
        return condition;
    }
    
    public BlockNode getStatements(){
        return statements;
    }
    
    public String reportPosition(){
        return position;
    }
    public static class IfNode extends ASTnode {
        private Optional<IfNode> elseNode = Optional.empty();
        
        
        IfNode(BlockNode statements, String position) {
            super(statements, position);
        }
        IfNode(BlockNode statements) {
            this(statements, "[debug node]");
        }
        IfNode(Node condition, BlockNode statements, String position) {
            super(condition, statements, position);
        }
        IfNode(Node condition, BlockNode statements) {
            this(condition, statements, "[debug node]");
        }
        IfNode(Node condition, BlockNode statements, IfNode elseNode, String position) {
            super(condition, statements, position);
            this.elseNode = Optional.of(elseNode);
        }
        
        IfNode(Node condition, BlockNode statements, IfNode elseNode) {
            this(condition, statements, elseNode, "[debug node]");
        }
        
        public void setElse(IfNode next) {
            elseNode = Optional.of(next);
        }

        public Optional<IfNode> getElse(){
            return elseNode;
        }
        
        public String toString(){
            if(elseNode.isPresent())
                return String.format("IfNode: if(%s) %s else %s", condition, statements, elseNode.get());
            return String.format("IfNode: if(%s) %s", condition, statements);
        }
        
        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof IfNode))
                return false;
            IfNode other = (IfNode) o;
            if(elseNode.isPresent())
                return other.elseNode.isPresent() && condition.equals(other.condition) && statements.equals(other.statements) && elseNode.get().equals(other.elseNode.get());
            return condition.equals(other.condition) && statements.equals(other.statements);
        }
    }
    
    public static class WhileNode extends ASTnode {
        
        boolean doWhile = false;
        WhileNode(Node condition, BlockNode statements, String position) {
            super(condition, statements, position);
        }
        WhileNode(Node condition, BlockNode statements) {
            this(condition, statements, "[debug node]");
        }
        
        WhileNode(Node condition, BlockNode statements, boolean doWhile, String position) {
            super(condition, statements, position);
            this.doWhile = doWhile;
        }
        WhileNode(Node condition, BlockNode statements, boolean doWhile) {
            this(condition, statements, doWhile, "[debug node]");
        }
        
        public String toString(){
            if(doWhile)
                return String.format("WhileNode: do %s while(%s)", statements, condition);
            return String.format("WhileNode: while(%s) %s", condition, statements);
        }
        
        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof WhileNode))
                return false;
            WhileNode other = (WhileNode) o;
            return doWhile == other.doWhile && condition.equals(other.condition) && statements.equals(other.statements);
        }
    }
    
    public static class ForNode extends ASTnode {
        private Node init;
        private Node update;
        public final boolean forIn;
        private VariableReferenceNode member;
        private Node collection;
        
        ForNode(Node init, Node condition, Node update,BlockNode statements, String position) {
            super(condition, statements, position);
            this.init = init;
            this.update = update;
            this.forIn = false;
        }
        ForNode(Node init, Node condition, Node update,BlockNode statements) {
            this(init, condition, update, statements, "[debug node]");
        }
        
        ForNode(VariableReferenceNode member, Node collection, BlockNode statements, String position) {
            super(statements, position);
            this.member = member;
            this.collection = collection;
            this.forIn = true;
        }
        ForNode(VariableReferenceNode member, Node collection, BlockNode statements) {
            this(member, collection, statements, "[debug node]");
        }
        public Node getInit() {
            return init;
        }

        public Node getUpdate() {
            return update;
        }

        public VariableReferenceNode getMember(){
            return member;
        }

        public Node getCollection(){
            return collection;
        }

        public String toString(){
            if(forIn)
                return String.format("ForNode: for(%s in %s) %s", member, collection, statements);
            return String.format("ForNode: for(%s; %s; %s) %s", init, condition, update, statements);
        }
        
        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof ForNode))
                return false;
            ForNode other = (ForNode) o;
            if(forIn != other.forIn)
                return false;
            
            if(forIn)
                return member.equals(other.member) && collection.equals(other.collection) && statements.equals(other.statements);
            return init.equals(other.init) && condition.equals(other.condition) && update.equals(other.update) && statements.equals(other.statements);
        }
        
    }
    
    public static class ContinueNode extends ASTnode {
        ContinueNode(String position) {
            super(position);
        }
        ContinueNode() {
            this("[debug node]");
        }

        public String toString(){
            return "ContinueNode";
        }

        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof ContinueNode))
                return false;
            return true;
        }
    }
    
    public static class BreakNode extends ASTnode {
        BreakNode(String position) {
            super(position);
        }
        BreakNode() {
            this("[debug node]");
        }
        
        public String toString(){
            return "BreakNode";
        }
        
        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof BreakNode))
                return false;
            return true;
        }
    }
    
    public static class DeleteNode extends ASTnode {
        VariableReferenceNode target;
        Collection<Node> indices;
        
        DeleteNode(VariableReferenceNode target, String position) {
            super(position);
            this.target = target;
            this.indices = List.of();
        }
        DeleteNode(VariableReferenceNode target) {
            this(target, "[debug node]");
        }
        
        DeleteNode(VariableReferenceNode target, Collection<Node> indices, String position) {
            super(position);
            this.target = target;
            this.indices = indices;
        }
        DeleteNode(VariableReferenceNode target, Collection<Node> indices) {
            this(target, indices, "[debug node]");
        }
        
        public String toString(){
            return String.format("DeleteNode: %s", target);
        }
        
        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof DeleteNode))
                return false;
            DeleteNode other = (DeleteNode) o;
            return target.equals(other.target) && indices.equals(other.indices);
        }
    }
    
    public static class ReturnNode extends ASTnode {
        public final Node value;
        public ReturnNode(Node value, String position) {
            super(position);
            this.value = value;
        }
        public ReturnNode(Node value) {
            this(value, "[debug node]");
        }
        
        public String toString(){
            return String.format("ReturnNode: %s", value);
        }
        
        public boolean equals(Object o){
            if(o == this)
                return true;
            if(!(o instanceof ReturnNode))
                return false;
            ReturnNode other = (ReturnNode) o;
            return value.equals(other.value);
        }
    }
}
