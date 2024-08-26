package visitors;

import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridTermPrimary;

import java.math.BigDecimal;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class ExpressionExtractorVisitor extends Visitor<Variable> {
    private String nameOfPhisicalVarible = "";

    public void setNameOfPhisicalVarible(String nameOfPhisicalVarible) {
        this.nameOfPhisicalVarible = (nameOfPhisicalVarible.equals("")) ? "" : nameOfPhisicalVarible + "_";
    }

    private StringVariable convertToStringVarible(Variable variable) {
        if (variable instanceof StringVariable)
            return (StringVariable) variable;
        else if (variable instanceof DiscreteDecimalVariable)
            return new StringVariable("", ((DiscreteDecimalVariable) variable).getValue().toString());
        else if (variable instanceof DiscreteBoolVariable)
            return new StringVariable("", ((DiscreteBoolVariable) variable).getValue().toString());
        else if (variable instanceof IntervalRealVariable)
            return new StringVariable("", ((IntervalRealVariable) variable).getLowerBound().toString());

        return null;
    }

    public Variable visit(Expression expression) {
        if (expression instanceof DotPrimary) {
            return visit((DotPrimary) expression);
        } else if (expression instanceof UnaryExpression) {
            return visit((UnaryExpression) expression);
        } else if (expression instanceof BinaryExpression) {
            return visit((BinaryExpression) expression);
        } else if (expression instanceof HybridTermPrimary) {
            return visit((HybridTermPrimary) expression);
        } else if (expression instanceof TermPrimary) {
            return visit((TermPrimary) expression);
        } else if (expression instanceof Literal) {
            return visit((Literal) expression);
        }
        return null;
    }

    @Override
    public Variable visit(UnaryExpression unaryExpression) {
        Variable operand = this.visit(unaryExpression.getExpression());
        String operator = unaryExpression.getOperator();
        return new StringVariable("", operator + '(' + convertToStringVarible(operand).getValue()+ ')');
    }

    @Override
    public Variable visit(BinaryExpression binaryExpression) {
        Variable left = this.visit(binaryExpression.getLeft());
        Variable right = this.visit(binaryExpression.getRight());
        String operator = binaryExpression.getOperator();
        return new StringVariable("", convertToStringVarible(left).getValue() + operator + convertToStringVarible(right).getValue());
    }

    @Override
    public Variable visit(HybridTermPrimary hybridTermPrimary) {
        return new StringVariable("", this.nameOfPhisicalVarible +
                hybridTermPrimary.getName() +
                String.valueOf('\'').repeat(hybridTermPrimary.getDerivativeOrder())
        );
    }

    @Override
    public Variable visit(TermPrimary termPrimary) {
        return new StringVariable("", this.nameOfPhisicalVarible + termPrimary.getName());
    }

    @Override
    public Variable visit(Literal literal) {
        String type = ((OrdinaryPrimitiveType)literal.getType()).getName();
        switch (type) {
            case "int": case "byte": case "short":
                return new DiscreteDecimalVariable("", new BigDecimal(literal.getLiteralValue()));
            case "float":  case "double":
                return new IntervalRealVariable("", new Double(literal.getLiteralValue()));
            case "boolean":
                return new DiscreteBoolVariable("", new Boolean(literal.getLiteralValue()));
            default:
                return null;
        }
    }
}
