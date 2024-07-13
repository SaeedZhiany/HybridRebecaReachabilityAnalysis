package visitors;

import dataStructure.DiscreteBoolVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;

import java.math.BigDecimal;
import java.util.Map;

public class BlockStatementExecutorVisitor extends Visitor<Void> {
    Map<String, Variable> symbolTable;
    ExpressionEvaluatorVisitor expressionEvaluatorVisitor;
    String mode;

    public BlockStatementExecutorVisitor(Map<String, Variable> variablesValuation, String mode) {
        this.symbolTable = variablesValuation;
        this.expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(variablesValuation);
        this.mode = mode;
    }

    public BlockStatementExecutorVisitor(Map<String, Variable> variablesValuation) {
        this.symbolTable = variablesValuation;
        this.expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(variablesValuation);
        this.mode = "";
    }

    public String getMode() {
        return mode;
    }

    @Override
    public Void visit(BlockStatement blockStatement) {
        for (Statement statement : blockStatement.getStatements()) {
            if (statement instanceof DotPrimary) {
                return visit((DotPrimary) statement);
            } else if (statement instanceof UnaryExpression) {
                return visit((UnaryExpression) statement);
            } else if (statement instanceof BinaryExpression) {
                return visit((BinaryExpression) statement);
            } else if (statement instanceof TermPrimary) {
                return visit((TermPrimary) statement);
            } else if (statement instanceof Literal) {
                return visit((Literal) statement);
            } else if (statement instanceof FieldDeclaration) {
                return visit((FieldDeclaration) statement);
            }
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        if (binaryExpression.getOperator().equals("=")) {
            Variable rightValue = expressionEvaluatorVisitor.visit(binaryExpression.getRight());
            symbolTable.put(
                    ((TermPrimary) binaryExpression.getLeft()).getName(),
                    rightValue
            );
        }
        return null;
    }

    @Override
    public Void visit(TermPrimary termPrimary) {
        if (termPrimary.getName().equals("setMode")) {
            String mode = ((TermPrimary) termPrimary.getParentSuffixPrimary().getArguments().get(0)).getName();
            this.mode = mode;
        }
        return null;
    }

    @Override
    public Void visit(FieldDeclaration fieldDeclaration) {
        String type = ((OrdinaryPrimitiveType) fieldDeclaration.getType()).getName();
        for (VariableDeclarator variableDeclarator : fieldDeclaration.getVariableDeclarators()) {
            switch (type) {
                case "int":
                case "byte":
                case "short": {
                    symbolTable.put(variableDeclarator.getVariableName(),
                            new DiscreteDecimalVariable(variableDeclarator.getVariableName(),
                                    variableDeclarator.getVariableInitializer() != null ?
                                            ((DiscreteDecimalVariable) expressionEvaluatorVisitor.visit(((OrdinaryVariableInitializer) variableDeclarator.getVariableInitializer()).getValue())).getValue()
                                            : new BigDecimal(0)));
                }
                case "float":
                case "double": {
                    symbolTable.put(variableDeclarator.getVariableName(),
                            new IntervalRealVariable(variableDeclarator.getVariableName(),
                                    variableDeclarator.getVariableInitializer() != null ?
                                            ((IntervalRealVariable) expressionEvaluatorVisitor.visit(((OrdinaryVariableInitializer) variableDeclarator.getVariableInitializer()).getValue())).getLowerBound()
                                            : 0.0));

                }
                case "boolean": {
                    symbolTable.put(variableDeclarator.getVariableName(),
                            new DiscreteBoolVariable(variableDeclarator.getVariableName(),
                                    variableDeclarator.getVariableInitializer() != null ?
                                            ((DiscreteBoolVariable) expressionEvaluatorVisitor.visit(((OrdinaryVariableInitializer) variableDeclarator.getVariableInitializer()).getValue())).getValue()
                                            : false));
                }
            }
        }
        return null;
    }
}
