package plc.compiler;

import java.io.PrintWriter;
import java.util.Optional;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {

        // TODO:  Generate Java to handle Source node.
        //System.out.print(" Source ");
        writer.println("public final class Main {");
        newline(++indent);
        writer.print("public static void main(String[] args) {");
        if (ast.getStatements().size() > 0) {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++) {
                visit(ast.getStatements().get(i));
                //writer.print(";");
                if ((i + 1) != ast.getStatements().size()) {
                    newline(indent);
                }
            }
            newline(--indent);
            writer.println("}");
        } else {
            writer.println("}");
            //writer.print("}");
        }
        newline(0);
        writer.println("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {

        // TODO:  Generate Java to handle Expression node.
        //System.out.print(" Expression ");
        visit(ast.getExpression());
        writer.print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {

        // TODO:  Generate Java to handle Declaration node.
        //System.out.print(" Declaration ");
        writer.print(ast.getType() + " " + ast.getName());
        //System.out.print(ast.getValue().get());
        if(ast.getValue().isPresent())
        {
            writer.print(" = ");
            visit(ast.getValue().get());
        }
        writer.print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {

        // TODO:  Generate Java to handle Assignment node.
        //System.out.print(" Assignment ");
        writer.print(ast.getName() + " = ");
        visit(ast.getExpression());
        writer.print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {

        // TODO:  Generate Java to handle If node.
        //System.out.print(" If ");
        writer.print("if (");
        visit(ast.getCondition());
        writer.print(") {");
        if(!ast.getThenStatements().isEmpty()){
            newline(++indent);
            for(int i = 0; i < ast.getThenStatements().size(); i++)
            {
                visit(ast.getThenStatements().get(i));
                if((i+1) != ast.getThenStatements().size()){
                    newline(indent);
                }
            }
            newline(--indent);
            writer.print("}");
        }else {
            writer.print("}");
        }
        if(!ast.getElseStatements().isEmpty()){
            writer.print(" else {");
            newline(++indent);
            for(int i = 0; i < ast.getElseStatements().size(); i++)
            {
                visit(ast.getElseStatements().get(i));
                if((i+1) != ast.getElseStatements().size()){
                    newline(indent);
                }
            }
            newline(--indent);
            writer.print("}");
        }


        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {

        // TODO:  Generate Java to handle While node.
        //System.out.print(" While ");
        writer.print("while (");
        visit(ast.getCondition());
        writer.print(") {");
        if(!ast.getStatements().isEmpty()){
            newline(++indent);
            for(int i = 0; i < ast.getStatements().size(); i++)
            {
                visit(ast.getStatements().get(i));
                if((i+1) != ast.getStatements().size()){
                    newline(indent);
                }
            }
            newline(--indent);
            writer.print("}");
        }else{
            //            newline(++indent);
            //            newline(--indent);
            writer.print("}");
        }


        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {

        // TODO:  Generate Java to handle Literal node.
        //System.out.print(" Literal ");
        if(ast.getValue() instanceof String){
            writer.print("\"" + ast.getValue() + "\"");
        }else{
            writer.print(ast.getValue());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {

        // TODO:  Generate Java to handle Group node.
        //System.out.print(" Group ");
        writer.print("(");
        visit(ast.getExpression());
        writer.print(")");


        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {

        // TODO:  Generate Java to handle Binary node.
        //System.out.print(" Binary ");
        //writer.print(ast.getLeft().toString() + " " + ast.getOperator() + " ");
        //visit(ast.getRight());
        visit(ast.getLeft());
        writer.print(" " + ast.getOperator() + " ");
        visit(ast.getRight());

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Variable ast) {

        // TODO:  Generate Java to handle Variable node.
        //System.out.print(" Variable ");
        writer.print(ast.getName());

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {

        // TODO:  Generate Java to handle Function node.
        //System.out.print(" Function ");
        writer.print(ast.getName() + "(");
        //visit(ast.getArguments().get(0));
        for(int i = 0; i < ast.getArguments().size(); i++){
            visit(ast.getArguments().get(i));
            if((i+1) != ast.getArguments().size()){
                writer.print(", ");
            }
        }
        writer.print(")");

        return null;
    }

}
