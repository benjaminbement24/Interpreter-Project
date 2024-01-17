package plc.compiler;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Optional;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Ast> {

    public Scope scope;

    public Analyzer(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Ast visit(Ast.Source ast) throws AnalysisException {
        ArrayList<Ast.Statement> statements = new ArrayList<>();

        if (ast.getStatements().size() <= 0) {
            throw new AnalysisException("ast does not have any statements");
        }

        for (int i = 0; i < ast.getStatements().size(); i++) {
            statements.add(visit(ast.getStatements().get(i)));
        }
        return new Ast.Source(statements);
    }

    /**
     * Statically validates that visiting a statement returns a statement.
     */
    private Ast.Statement visit(Ast.Statement ast) throws AnalysisException {
        return (Ast.Statement) visit((Ast) ast);
    }

    @Override
    public Ast.Statement.Expression visit(Ast.Statement.Expression ast) throws AnalysisException {
        if (!(ast.getExpression() instanceof Ast.Expression.Function)) {
            throw new AnalysisException("Expression statement is not a function");
        }
        return new Ast.Statement.Expression(visit(ast.getExpression()));
    }

    @Override
    public Ast.Statement.Declaration visit(Ast.Statement.Declaration ast) throws AnalysisException {
        scope.define(ast.getName(),Stdlib.getType(ast.getType()));
        if(ast.getValue().isPresent()){
            Ast.Expression calcVal = visit(ast.getValue().get());
            checkAssignable(calcVal.getType(), Stdlib.getType(ast.getType()));
            Optional<Ast.Expression> val = Optional.of(calcVal);

            return new Ast.Statement.Declaration(ast.getName(), Stdlib.getType(ast.getType()).getJvmName(), val);
        }else{
            return new Ast.Statement.Declaration(ast.getName(), Stdlib.getType(ast.getType()).getJvmName(), ast.getValue());
        }
    }

    @Override
    public Ast.Statement.Assignment visit(Ast.Statement.Assignment ast) throws AnalysisException {
        Ast.Expression val = visit(ast.getExpression());
        Stdlib.Type returnType = this.scope.lookup(ast.getName());
        checkAssignable(returnType, val.getType());
        return new Ast.Statement.Assignment(ast.getName(), val);
    }

    @Override
    public Ast.Statement.If visit(Ast.Statement.If ast) throws AnalysisException {
        Ast.Expression cond = visit(ast.getCondition());
        checkAssignable(cond.getType(), Stdlib.Type.BOOLEAN);
        int thenStatementSize = ast.getThenStatements().size();
        if (thenStatementSize <= 0) {
            throw new AnalysisException("Then statements is empty");
        }
        ArrayList<Ast.Statement> thenStatements = new ArrayList<>();
        Scope par = this.scope;
        this.scope = new Scope(par);
        for (int i = 0; i < thenStatementSize; i++) {
            thenStatements.add(visit(ast.getThenStatements().get(i)));
        }
        this.scope = this.scope.getParent();
        ArrayList<Ast.Statement> elseStatements = new ArrayList<>();
        if (ast.getElseStatements().size() > 0) {
            Scope par2 = this.scope;
            this.scope = new Scope(par2);
            for (int j = 0; j < ast.getElseStatements().size(); j++) {
                elseStatements.add(visit(ast.getElseStatements().get(j)));
            }
            this.scope = new Scope(par);
        }
        return new Ast.Statement.If(cond, thenStatements, elseStatements);
    }

    @Override
    public Ast.Statement.While visit(Ast.Statement.While ast) throws AnalysisException {
        Ast.Expression cond = visit(ast.getCondition());
        checkAssignable(cond.getType(), Stdlib.Type.BOOLEAN);
        ArrayList<Ast.Statement> stmnts = new ArrayList<>();
        Scope par = this.scope;
        this.scope = new Scope(par);
        for (int i = 0; i < ast.getStatements().size(); i++) {
            stmnts.add(visit(ast.getStatements().get(i)));
        }
        this.scope = this.scope.getParent();
        return new Ast.Statement.While(cond, stmnts);
    }

    /**
     * Statically validates that visiting an expression returns an expression.
     */
    private Ast.Expression visit(Ast.Expression ast) throws AnalysisException {
        return (Ast.Expression) visit((Ast) ast);
    }

    @Override
    public Ast.Expression.Literal visit(Ast.Expression.Literal ast) throws AnalysisException {
        if (ast.getValue() instanceof BigInteger) {
            BigInteger val = (BigInteger) ast.getValue();
            BigInteger max = new BigInteger(String.valueOf(Integer.MAX_VALUE));
            BigInteger small = new BigInteger(String.valueOf(Integer.MIN_VALUE));
            if (val.compareTo(small) < 0 || val.compareTo(max) > 0) {
                throw new AnalysisException("Integer is out of range");
            }
            return new Ast.Expression.Literal(Stdlib.Type.INTEGER, val.intValue());
        } else if (ast.getValue() instanceof Boolean) {
            return new Ast.Expression.Literal(Stdlib.Type.BOOLEAN, ast.getValue());
        } else if (ast.getValue() instanceof BigDecimal) {
            BigDecimal val = (BigDecimal) ast.getValue();
            Double check = val.doubleValue();

            System.out.println(check);
            System.out.println(Double.POSITIVE_INFINITY);
            if (check == Double.NEGATIVE_INFINITY || check == Double.POSITIVE_INFINITY) {
                throw new AnalysisException("Decimal is out of range");
            }
            return new Ast.Expression.Literal(Stdlib.Type.DECIMAL, check);
        } else if (ast.getValue() instanceof String) {
            if(!((String) ast.getValue()).matches("[A-Za-z0-9_!?.+\\-/* ]*")){
                throw new AnalysisException("String contains invalid characters");
            }
            return new Ast.Expression.Literal(Stdlib.Type.STRING, ast.getValue());
        }
        throw new AnalysisException("Invalid Literal Type (Reached end of Literal method)");
    }

    @Override
    public Ast.Expression.Group visit(Ast.Expression.Group ast) throws AnalysisException {
        Ast.Expression expression = visit(ast.getExpression()); //visit first, then getType
        return new Ast.Expression.Group(expression.getType(), expression);
    }

    @Override
    public Ast.Expression.Binary visit(Ast.Expression.Binary ast) throws AnalysisException {
        Ast.Expression leftval = visit(ast.getLeft());
        Ast.Expression rightval = visit(ast.getRight());
        if (ast.getOperator().equals("==") || ast.getOperator().equals("!=")) {
            if (leftval.getType().equals(Stdlib.Type.VOID) || rightval.getType().equals(Stdlib.Type.VOID)) {
                throw new AnalysisException("for == or != left or right values cannot be of type VOID");
            }
            return new Ast.Expression.Binary(Stdlib.Type.BOOLEAN, ast.getOperator(), leftval, rightval);
        } else if (ast.getOperator().equals("+")) {
            if (leftval.getType().equals(Stdlib.Type.VOID) || rightval.getType().equals(Stdlib.Type.VOID)) {
                throw new AnalysisException("for + left or right values cannot be of type VOID");
            }
            if (leftval.getType().equals(Stdlib.Type.STRING) || rightval.getType().equals(Stdlib.Type.STRING)) {
                return new Ast.Expression.Binary(Stdlib.Type.STRING, ast.getOperator(), leftval, rightval);
            } else if (leftval.getType().equals(Stdlib.Type.INTEGER) && rightval.getType().equals(Stdlib.Type.INTEGER)) {
                return new Ast.Expression.Binary(Stdlib.Type.INTEGER, ast.getOperator(), leftval, rightval);
            } else if (leftval.getType().equals(Stdlib.Type.DECIMAL) || rightval.getType().equals(Stdlib.Type.DECIMAL)) {
                return new Ast.Expression.Binary(Stdlib.Type.DECIMAL, ast.getOperator(), leftval, rightval);
            } else {
                throw new AnalysisException("unsupported left or right val type for +");
            }
        } else if (ast.getOperator().equals("-") || ast.getOperator().equals("*") || ast.getOperator().equals("/")) {
            if (leftval.getType().equals(Stdlib.Type.INTEGER) && rightval.getType().equals(Stdlib.Type.INTEGER)) {
                return new Ast.Expression.Binary(Stdlib.Type.INTEGER, ast.getOperator(), leftval, rightval);
            } else if (leftval.getType().equals(Stdlib.Type.DECIMAL) || rightval.getType().equals(Stdlib.Type.DECIMAL)) {
                return new Ast.Expression.Binary(Stdlib.Type.DECIMAL, ast.getOperator(), leftval, rightval);
            } else {
                throw new AnalysisException("unsupported operation for - * or /");
            }
        }
        throw new AnalysisException("unsupported operation for binary expression");
    }

    @Override
    public Ast.Expression.Variable visit(Ast.Expression.Variable ast) throws AnalysisException {
        return new Ast.Expression.Variable(this.scope.lookup(ast.getName()), ast.getName());
    }

    @Override
    public Ast.Expression.Function visit(Ast.Expression.Function ast) throws AnalysisException {
        Stdlib.Function func = Stdlib.getFunction(ast.getName(), ast.getArguments().size());
        ArrayList<Ast.Expression> returnValues = new ArrayList<>();
        if (func.getParameterTypes().size() != ast.getArguments().size()) {
            throw new AnalysisException("argument size does not fit parameter size");
        }
        for (int i = 0; i < ast.getArguments().size(); i++) {
            Ast.Expression calculatedVal = visit(ast.getArguments().get(i));
            System.out.println(calculatedVal.getType().toString());
            System.out.println(func.getParameterTypes().get(i).toString());
            checkAssignable(calculatedVal.getType(),func.getParameterTypes().get(i));
            returnValues.add(calculatedVal);
        }
        return new Ast.Expression.Function(func.getReturnType(), func.getJvmName(), returnValues);
    }

    /**
     * Throws an AnalysisException if the first type is NOT assignable to the target type. * A type is assignable if and only if one of the following is true:
     *  - The types are equal, as according to Object#equals
     *  - The first type is an INTEGER and the target type is DECIMAL
     *  - The first type is not VOID and the target type is ANY
     */
    public static void checkAssignable(Stdlib.Type type, Stdlib.Type target) throws AnalysisException {

        if (type.equals(target) ||
                (type.equals(Stdlib.Type.INTEGER) && target.equals(Stdlib.Type.DECIMAL)) ||
                (!type.equals(Stdlib.Type.VOID) && target.equals(Stdlib.Type.ANY))) {
            // Do nothing
        } else {
            throw new AnalysisException("Check assignable failed for both types");
        }
    }

}
