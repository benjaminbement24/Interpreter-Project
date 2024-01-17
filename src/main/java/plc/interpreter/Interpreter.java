package plc.interpreter;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Interpreter {

    /**
     * The VOID constant represents a value that has no useful information. It
     * is used as the return value for functions which only perform side
     * effects, such as print, similar to Java.
     */
    public static final Object VOID = new Function<List<Ast>, Object>() {

        @Override
        public Object apply(List<Ast> args) {
            return VOID;
        }

    };

    public final PrintWriter out;
    public Scope scope;

    public Interpreter(PrintWriter out, Scope scope) {
        this.out = out;
        this.scope = scope;
        init();
    }

    /**
     * Delegates evaluation to the method for the specific instance of AST. This
     * is another approach to implementing the visitor pattern.
     */
    public Object eval(Ast ast) {
        if (ast instanceof Ast.Term) {
            return eval((Ast.Term) ast);
        } else if (ast instanceof Ast.Identifier) {
            return eval((Ast.Identifier) ast);
        } else if (ast instanceof Ast.NumberLiteral) {
            return eval((Ast.NumberLiteral) ast);
        } else if (ast instanceof Ast.StringLiteral) {
            return eval((Ast.StringLiteral) ast);
        } else {
            throw new AssertionError(ast.getClass());
        }
    }

    /**
     * Evaluations the Term ast, which returns the value resulting by calling
     * the function stored under the term's name in the current scope. You will
     * need to check that the type of the value is a {@link Function}, and cast
     * to the type {@code Function<List<Ast>, Object>}.
     */
    private Object eval(Ast.Term ast) {
        return requireType(Function.class, scope.lookup(ast.getName())).apply(ast.getArgs());
    }

    /**
     * Evaluates the Identifier ast, which returns the value stored under the
     * identifier's name in the current scope.
     */
    private Object eval(Ast.Identifier ast) {
        return this.scope.lookup(ast.getName());
    }

    /**
     * Evaluates the NumberLiteral ast, which returns the stored number value.
     */
    private BigDecimal eval(Ast.NumberLiteral ast) {
        return ast.getValue();
    }

    /**
     * Evaluates the StringLiteral ast, which returns the stored string value.
     */
    private String eval(Ast.StringLiteral ast) {
        return ast.getValue();
    }

    /**
     * Initializes the given scope with fields and functions in the standard
     * library.
     */
    private void init() {
        scope.define("print", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            evaluated.forEach(out::print);
            out.println();
            return VOID;
        });
        scope.define("-", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream()
                    .map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            if (evaluated.isEmpty()) {
                throw new EvalException("Arguments to - cannot be empty");
            } else if (evaluated.size() == 1) {
                return evaluated.get(0).negate();
            } else {
                BigDecimal num = evaluated.get(0);
                for (int i = 1; i < evaluated.size(); i++) {
                    num = num.subtract(evaluated.get(i));
                }
                return num;
            }
        });

        scope.define("+", (Function<List<Ast>, Object>) args -> {
            /*List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            if (evaluated.isEmpty()) {
                return BigDecimal.ZERO;
            } else {
                BigDecimal num = evaluated.get(0);
                for (int i = 1; i < evaluated.size(); i++) {
                    num = num.add(evaluated.get(i));
                }
                return num;
            }*/

            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ZERO;
            for (Object obj : evaluated) {
                result = result.add(requireType(BigDecimal.class, obj));
            }
            return result;
        });

        scope.define("/", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            if (evaluated.isEmpty()) {
                throw new EvalException("Empty Arguments for divide function");
            } else if (evaluated.size()== 1){
                BigDecimal num = BigDecimal.ONE.divide(evaluated.get(0), RoundingMode.HALF_EVEN);
                return num;
            } else {
                BigDecimal num = evaluated.get(0);
                int maxscale = num.scale();
                for (int i = 1; i < evaluated.size(); i++) {
                    maxscale = Math.max(evaluated.get(i).scale(), maxscale);
                    num = num.divide(evaluated.get(i), maxscale, RoundingMode.HALF_EVEN);
                }
                return num;
            }

        });
        scope.define("*", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            if (evaluated.isEmpty()) {
                return BigDecimal.ONE;
            } else {
                BigDecimal num = BigDecimal.ONE;
                for (int i = 0; i < evaluated.size(); i++) {
                    num = num.multiply(evaluated.get(i));
                }
                return num;
            }
        });
        scope.define("true", new Boolean(true));
        scope.define("false", new Boolean(false));
        scope.define("equals?", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2) {
                throw new EvalException("Can only have 2 arguments fore equals term name");
            }
            Object arg1 = eval(args.get(0));
            Object arg2 = eval(args.get(1));
            return new Boolean(Objects.deepEquals(arg1,arg2));
        });
        scope.define("not", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 1) {
                throw new EvalException("Can only have 2 arguments fore equals term name");
            }
            Object val = eval(args.get(0));
            if (!(val instanceof Boolean)) {
                throw new EvalException("Argument has to be of type boolean");
            }
            Boolean ret = (Boolean) val;
            if (ret) {
                return new Boolean(false);
            } else {
                return new Boolean(true);
            }
        });
        scope.define("and", (Function<List<Ast>, Object>) args -> {
            for (Ast node : args) {
                if (!(eval(node) instanceof Boolean)) {
                    return new Boolean(false);
                } else {
                    Boolean res = (Boolean) eval(node);
                    if (!res) {
                        return false;
                    }
                }
            }
            return new Boolean(true);
        });
        scope.define("or", (Function<List<Ast>, Object>) args -> {
            for (Ast node : args) {
                if (!(eval(node) instanceof Boolean)) {
                    throw new EvalException("Need a boolean value to evaluate or statement");
                } else {
                    Boolean res = (Boolean) eval(node);
                    if (res) {
                        return true;
                    }
                }
            }
            return false;
        });

        scope.define("list", (Function<List<Ast>, Object>) args -> {
            List<Object> values = new LinkedList<>();
            for (Ast node : args) {
                values.add(eval(node));
            }
            return values;
        });
        scope.define("range", (Function<List<Ast>, Object>) args -> {

            if (args.size() == 0) {
                throw new EvalException(("Expected values to be there"));
            }
            if (args.size() != 2) {
                throw new EvalException("Expected range size to be 2");
            }
            for (Ast dv : args) { //type checking
                if (eval(requireType(Ast.NumberLiteral.class, dv)).scale() > 0) {
                    throw new EvalException("Expected Number Literal and an Integer");
                }
            }
            BigDecimal first = eval((Ast.NumberLiteral) args.get(0));
            BigDecimal last =  eval((Ast.NumberLiteral) args.get(1));
            if (first == last) {
                return new LinkedList<BigDecimal>();
            }
            if (last.compareTo(first) < 0) {
                throw new EvalException("Expected range to be increasing");
            }
            List<BigDecimal> values = new LinkedList<>();
            //TODO CAN WE HAVE A LIST OF STRINGS????
            for (BigDecimal i = first; i.compareTo(last) < 0; i = i.add(BigDecimal.ONE)) {
                values.add(i);
            }
            return values;
        });
        scope.define("set!", (Function<List<Ast>, Object>) args -> {
            //TODO DON'T KNOW IF THIS IS RIGHT

            if (args.size() != 2) {
                throw new EvalException("Need 2 arguments for set");
            }
            if (!(args.get(0) instanceof Ast.Identifier)) {
                throw new EvalException("Need Identifier for argument");
            }
            this.scope.set(requireType(Ast.Identifier.class, args.get(0)).getName(), eval(args.get(1)));
            return VOID;
        });
        scope.define("define", (Function<List<Ast>, Object>) args -> {
            if(args.size() != 2) {
                throw new EvalException("Invalid number of arguments");
            }
            if (args.get(0) instanceof Ast.Identifier) {
                scope.define(((Ast.Identifier) args.get(0)).getName(), eval(args.get(1)));
            } else if (args.get(0) instanceof Ast.Term) {
                String name = ((Ast.Term) args.get(0)).getName();
                List<String> params = ((Ast.Term) args.get(0)).getArgs().stream()
                        .map(a -> requireType(Ast.Identifier.class, a).getName())
                        .collect(Collectors.toList());
                Scope parent = scope;
                scope.define(name, (Function<List<Ast>, Object>) arguments -> {
                    List<Object> evaluated = arguments.stream().map(this::eval).collect(Collectors.toList());
                    if (params.size() != evaluated.size()) {
                        throw new EvalException("Invalid number of arguments");
                    }
                    Scope current = scope;
                    scope = new Scope(parent);
                    for (int i = 0; i < params.size(); i++) {
                        scope.define(params.get(i), evaluated.get(i));
                    }
                    Object result = eval(args.get(1));
                    scope = current;
                    return result;
                });
            } else {
                throw new EvalException("Invalid first arguments");
            }
            return VOID;
        });

        scope.define("while", (Function<List<Ast>, Object>) args -> {
            if ( args.size() != 2 ) {
                throw new EvalException( "Expected 2 arguments, received " + args.size() + "." );
            }
            while ( requireType( Boolean.class, eval( args.get(0) ) ) ) {
                eval( args.get(1) );
            }
            return VOID;
        });
        scope.define("for", (Function<List<Ast>, Object>) args -> {
            Scope oldScope = this.scope;
            this.scope = new Scope(oldScope);
            //TODO Maybe buggy we'll see
            if (args.size() != 2) {
                throw new EvalException("Need 2 arguments for for loop");
            }

            Ast.Term value = requireType(Ast.Term.class, args.get(0)); // gets the variable e.g i
            String identifierName = value.getName();
            Object potential_list = eval(value.getArgs().get(0));
            if (!(potential_list instanceof LinkedList)) {
                throw new EvalException("Need a linked list for the for loop");
            }
            List<Object> true_list = (LinkedList) potential_list;
            if (true_list.size() == 0) {
                return VOID;
            }
            this.scope.define(identifierName, true_list.get(0));
            for (Object object_val : true_list) {
                this.scope.set(identifierName, object_val);
                eval(args.get(1));
            }
            this.scope = this.scope.getParent();
            return VOID;
        });
        scope.define("do", (Function<List<Ast>, Object>) args -> {
            //TODO DEF NEED
            scope = new Scope(scope);
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            scope = scope.getParent();
            return !evaluated.isEmpty() ? evaluated.get(evaluated.size()-1) : VOID;
        });
        scope.define("<", (Function<List<Ast>, Object>) args->
        {

            int size = args.size();
            if (size == 0)
            {
                return true;
            }
            if(args.get(0) instanceof Ast.Identifier && requireType(Ast.Identifier.class, args.get(0)).getName().matches("true|false|equals?|not|and|or|<|<=|>|>=|\\+|-|/|\\*|define|list|range|set!|do|for|while|")){
                throw new EvalException("Identifier should not be keyword");
            }
            if (size == 1) {
                Object evaluated = eval(args.get(0));
                return new Boolean(true);
            }
            boolean shortcircuit = false;
            for (int i = 1; i < size; i++){
                Object val = eval(args.get(i-1));
                Object val2 = eval(args.get(i));
                int result;
                if(val instanceof BigDecimal && val2 instanceof BigDecimal){
                    BigDecimal v = (BigDecimal) val;
                    BigDecimal v2 = (BigDecimal) val2;
                    result = v.compareTo(v2);
                }else if (val instanceof String && val2 instanceof String){
                    String v = (String) val;
                    String v2 = (String) val2;
                    result = v.compareTo(v2);
                }else {
                    throw new EvalException("values have to be of same type");
                }
                if (result >= 0 && !shortcircuit) {
                    shortcircuit = true;
                }
            }
            return (shortcircuit)? new Boolean(false) : new Boolean(true);
        });
        scope.define(">", (Function<List<Ast>, Object>)args->
        {

            int size = args.size();
            if (size == 0)
            {
                return true;
            }
            if(args.get(0) instanceof Ast.Identifier && requireType(Ast.Identifier.class, args.get(0)).getName().matches("true|false|equals?|not|and|or|<|<=|>|>=|\\+|-|/|\\*|define|list|range|set!|do|for|while|")){
                throw new EvalException("Identifier should not be keyword");
            }
            if (size == 1) {
                Object evaluated = eval(args.get(0));
                return new Boolean(true);
            }
            boolean shortcircuit = false;
            for (int i = 1; i < size; i++){
                Object val = eval(args.get(i-1));
                Object val2 = eval(args.get(i));
                int result;
                if(val instanceof BigDecimal && val2 instanceof BigDecimal){
                    BigDecimal v = (BigDecimal) val;
                    BigDecimal v2 = (BigDecimal) val2;
                    result = v.compareTo(v2);
                }else if (val instanceof String && val2 instanceof String){
                    String v = (String) val;
                    String v2 = (String) val2;
                    result = v.compareTo(v2);
                }else {
                    throw new EvalException("values have to be of same type");
                }
                if (result <= 0 && !shortcircuit) {
                    shortcircuit = true;
                }
            }
            return (shortcircuit)? new Boolean(false) : new Boolean(true);
        });
        scope.define("<=", (Function<List<Ast>, Object>)args->
        {

            int size = args.size();
            int num = 1;
            if (size == 0)
            {
                return true;
            }
            if(args.get(0) instanceof Ast.Identifier && requireType(Ast.Identifier.class, args.get(0)).getName().matches("true|false|equals?|not|and|or|<|<=|>|>=|\\+|-|/|\\*|define|list|range|set!|do|for|while|")){
                throw new EvalException("Identifier should not be keyword");
            }
            if (size == 1) {
                Object evaluated = eval(args.get(0));
                return new Boolean(true);
            }
            boolean shortcircuit = false;
            for (int i = 1; i < size; i++){
                Object val = eval(args.get(i-1));
                Object val2 = eval(args.get(i));
                int result;
                if(val instanceof BigDecimal && val2 instanceof BigDecimal){
                    BigDecimal v = (BigDecimal) val;
                    BigDecimal v2 = (BigDecimal) val2;
                    result = v.compareTo(v2);
                }else if (val instanceof String && val2 instanceof String){
                    String v = (String) val;
                    String v2 = (String) val2;
                    result = v.compareTo(v2);
                }else {
                    throw new EvalException("values have to be of same type");
                }
                if (result > 0 && !shortcircuit) {
                    shortcircuit = true;
                }
            }
            return (shortcircuit)? new Boolean(false) : new Boolean(true);

        });
        scope.define(">=", (Function<List<Ast>, Object>)args->
        {
            int size = args.size();
            if (size == 0)
            {
                return true;
            }
            if(args.get(0) instanceof Ast.Identifier && requireType(Ast.Identifier.class, args.get(0)).getName().matches("true|false|equals?|not|and|or|<|<=|>|>=|\\+|-|/|\\*|define|list|range|set!|do|for|while|")){
                throw new EvalException("Identifier should not be keyword");
            }
            if (size == 1) {
                Object evaluated = eval(args.get(0));
                return new Boolean(true);
            }
            boolean shortcircuit = false;
            for (int i = 1; i < size; i++){
                Object val = eval(args.get(i-1));
                Object val2 = eval(args.get(i));
                int result;
                if(val instanceof BigDecimal && val2 instanceof BigDecimal){
                    BigDecimal v = (BigDecimal) val;
                    BigDecimal v2 = (BigDecimal) val2;
                    result = v.compareTo(v2);
                }else if (val instanceof String && val2 instanceof String){
                    String v = (String) val;
                    String v2 = (String) val2;
                    result = v.compareTo(v2);
                }else {
                    throw new EvalException("values have to be of same type");
                }
                if (result < 0 && !shortcircuit) {
                    shortcircuit = true;
                }
            }
            return (shortcircuit)? new Boolean(false) : new Boolean(true);
        });

        //TODO: Additional standard library functions
    }

    /**
     * A helper function for type checking, taking in a type and an object and
     * throws an exception if the object does not have the required type.
     *
     * This function does a poor job of actually identifying where the issue
     * occurs - in a real interpreter, we would have a stacktrace to provide
     * that implementation. For now, this is the simple-but-not-ideal solution.
     */
    private static <T> T requireType(Class<T> type, Object value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new EvalException("Expected " + value + " to have type " + type.getSimpleName() + ".");
        }
    }

}
