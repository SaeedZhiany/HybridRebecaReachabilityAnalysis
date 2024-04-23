package visitors;

import dataStructure.DiscreteBoolVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorVisitorTest {

    Literal createLiteral(Object a, String type) {
        Literal literalA = new Literal();
        literalA.setLiteralValue(String.valueOf(a));
        OrdinaryPrimitiveType typeA = new OrdinaryPrimitiveType();
        typeA.setName(type);
        literalA.setType(typeA);
        return literalA;
    }

    TermPrimary createTermPrimary(String name) {
        TermPrimary termPrimary = new TermPrimary();
        termPrimary.setName(name);
        return termPrimary;
    }

    UnaryExpression createUnaryExpression(String operator, Expression a) {
        UnaryExpression unaryExpression = new UnaryExpression();
        unaryExpression.setOperator(operator);
        unaryExpression.setExpression(a);
        return unaryExpression;
    }

    BinaryExpression creatBinaryExpression(String operator, Expression a, Expression b) {
        BinaryExpression binaryExpression = new BinaryExpression();
        binaryExpression.setOperator(operator);
        binaryExpression.setLeft(a);
        binaryExpression.setRight(b);
        return binaryExpression;
    }

    @Test
    @Tag("test binary expression int(literal) + int(literal)")
    void testBinaryExpressionIntLiteralPlusIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createLiteral(1, "int"),
                createLiteral(2, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(3, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) + int(termPrimary)")
    void testBinaryExpressionIntLiteralPlusIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createLiteral(1, "int"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(3, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(termPrimary) + int(termPrimary)")
    void testBinaryExpressionIntTermPrimaryPlusIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(2)));
        symbolTable.put("b", new DiscreteDecimalVariable("b", new BigDecimal(3)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createTermPrimary("a"),
                createTermPrimary("b"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(5, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) - int(literal)")
    void testBinaryExpressionIntLiteralMinusIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "-",
                createLiteral(1, "int"),
                createLiteral(2, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(-1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) - int(termPrimary)")
    void testBinaryExpressionIntLiteralMinusIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "-",
                createLiteral(1, "int"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(-1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(termPrimary) - int(termPrimary)")
    void testBinaryExpressionIntTermPrimaryMinusIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(3)));
        symbolTable.put("b", new DiscreteDecimalVariable("b", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "-",
                createTermPrimary("a"),
                createTermPrimary("b"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) * int(literal)")
    void testBinaryExpressionIntLiteralMultiplyIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "*",
                createLiteral(2, "int"),
                createLiteral(3, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(6, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) * int(termPrimary)")
    void testBinaryExpressionIntLiteralMultiplyIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(3)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "*",
                createLiteral(2, "int"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(6, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(termPrimary) * int(termPrimary)")
    void testBinaryExpressionIntTermPrimaryMultiplyIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(3)));
        symbolTable.put("b", new DiscreteDecimalVariable("b", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "*",
                createTermPrimary("a"),
                createTermPrimary("b"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(6, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) / int(literal)")
    void testBinaryExpressionIntLiteralDivideIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "/",
                createLiteral(6, "int"),
                createLiteral(3, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(2, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) / int(termPrimary)")
    void testBinaryExpressionIntLiteralDivideIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(3)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "/",
                createLiteral(6, "int"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(2, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(termPrimary) / int(termPrimary)")
    void testBinaryExpressionIntTermPrimaryDivideIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(6)));
        symbolTable.put("b", new DiscreteDecimalVariable("b", new BigDecimal(3)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "/",
                createTermPrimary("a"),
                createTermPrimary("b"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(2, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) % int(literal)")
    void testBinaryExpressionIntLiteralModIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "%",
                createLiteral(5, "int"),
                createLiteral(2, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(literal) % int(termPrimary)")
    void testBinaryExpressionIntLiteralModIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "%",
                createLiteral(5, "int"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression int(termPrimary) % int(termPrimary)")
    void testBinaryExpressionIntTermPrimaryModIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(5)));
        symbolTable.put("b", new DiscreteDecimalVariable("b", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "%",
                createTermPrimary("a"),
                createTermPrimary("b"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression double(literal) + double(literal)")
    void testBinaryExpressionDoubleLiteralPlusDoubleLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createLiteral(1.6, "double"),
                createLiteral(2.5, "double"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());

        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof IntervalRealVariable);
        assertEquals(4.1, ((IntervalRealVariable) actual).getLowerBound().doubleValue());
        assertEquals(4.1, ((IntervalRealVariable) actual).getUpperBound().doubleValue());
    }

    @Test
    @Tag("test binary expression double(literal) - int(termPrimary)")
    void testBinaryExpressionDoubleLiteralMinusIntTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new DiscreteDecimalVariable("a", new BigDecimal(2)));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "-",
                createLiteral(1.5, "double"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);

        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof IntervalRealVariable);
        assertEquals(-0.5, ((IntervalRealVariable) actual).getLowerBound().doubleValue());
        assertEquals(-0.5, ((IntervalRealVariable) actual).getUpperBound().doubleValue());
    }

    @Test
    @Tag("test binary expression double(literal) * double(termPrimary)")
    void testBinaryExpressionDoubleLiteralMultiplyDoubleTermPrimary() {
        Map<String, Variable> symbolTable = new HashMap<>();
        symbolTable.put("a", new IntervalRealVariable("a", 2.0, 3.0));
        BinaryExpression binaryExpression = creatBinaryExpression(
                "*",
                createLiteral(1.5, "double"),
                createTermPrimary("a"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);

        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof IntervalRealVariable);
        assertEquals(3.0, ((IntervalRealVariable) actual).getLowerBound().doubleValue());
        assertEquals(4.5, ((IntervalRealVariable) actual).getUpperBound().doubleValue());
    }

    @Test
    @Tag("test binary expression boolean(literal) && boolean(literal)")
    void testBinaryExpressionBooleanLiteralAndBooleanLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "&&",
                createLiteral(true, "boolean"),
                createLiteral(false, "boolean"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertFalse(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression boolean(literal) || boolean(literal)")
    void testBinaryExpressionBooleanLiteralOrBooleanLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "||",
                createLiteral(true, "boolean"),
                createLiteral(false, "boolean"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertTrue(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression boolean(literal) == boolean(literal)")
    void testBinaryExpressionBooleanLiteralEqualBooleanLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "==",
                createLiteral(true, "boolean"),
                createLiteral(false, "boolean"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertFalse(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression int(literal) == int(literal)")
    void testBinaryExpressionIntLiteralEqualIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "==",
                createLiteral(1, "int"),
                createLiteral(1, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertTrue(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression double(literal) == double(literal)")
    void testBinaryExpressionDoubleLiteralEqualDoubleLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "==",
                createLiteral(1.5, "double"),
                createLiteral(1.5, "double"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertTrue(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression boolean(literal) != boolean(literal)")
    void testBinaryExpressionBooleanLiteralNotEqualBooleanLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "!=",
                createLiteral(true, "boolean"),
                createLiteral(false, "boolean"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertTrue(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression int(literal) != int(literal)")
    void testBinaryExpressionIntLiteralNotEqualIntLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "!=",
                createLiteral(1, "int"),
                createLiteral(1, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertFalse(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression double(literal) != double(literal)")
    void testBinaryExpressionDoubleLiteralNotEqualDoubleLiteral() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "!=",
                createLiteral(1.5, "double"),
                createLiteral(1.5, "double"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(binaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertFalse(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test unary expression -int(literal)")
    void testUnaryExpressionMinusIntLiteral() {
        UnaryExpression unaryExpression = createUnaryExpression("-", createLiteral(1, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(-1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test unary expression +int(literal)")
    void testUnaryExpressionPlusIntLiteral() {
        UnaryExpression unaryExpression = createUnaryExpression("+", createLiteral(1, "int"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test unary expression -double(literal)")
    void testUnaryExpressionMinusDoubleLiteral() {
        UnaryExpression unaryExpression = createUnaryExpression("-", createLiteral(1.5, "double"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof IntervalRealVariable);
        assertEquals(-1.5, ((IntervalRealVariable) actual).getLowerBound().doubleValue());
        assertEquals(-1.5, ((IntervalRealVariable) actual).getUpperBound().doubleValue());
    }

    @Test
    @Tag("test unary expression !boolean(literal)")
    void testUnaryExpressionNotBooleanLiteral() {
        UnaryExpression unaryExpression = createUnaryExpression("!", createLiteral(true, "boolean"));
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof DiscreteBoolVariable);
        assertFalse(((DiscreteBoolVariable) actual).getValue());
    }

    @Test
    @Tag("test binary expression and unary expression -(2 + 3)")
    void testBinaryExpressionAndUnaryExpression() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createLiteral(2, "int"),
                createLiteral(3, "int"));
        UnaryExpression unaryExpression = createUnaryExpression("-", binaryExpression);
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(-5, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression and unary expression -(2 + -(3))")
    void testBinaryExpressionAndUnaryExpression2() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createLiteral(2, "int"),
                createUnaryExpression("-", createLiteral(3, "int")));
        UnaryExpression unaryExpression = createUnaryExpression("-", binaryExpression);
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof DiscreteDecimalVariable);
        assertEquals(1, ((DiscreteDecimalVariable) actual).getValue().intValue());
    }

    @Test
    @Tag("test binary expression and unary expression +((-2.5) + 3.1)")
    void testBinaryExpressionAndUnaryExpression3() {
        BinaryExpression binaryExpression = creatBinaryExpression(
                "+",
                createUnaryExpression("-", createLiteral(2.5, "double")),
                createLiteral(3.1, "double"));
        UnaryExpression unaryExpression = createUnaryExpression("+", binaryExpression);
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        Variable actual = expressionEvaluatorVisitor.visit(unaryExpression);
        assertTrue(actual instanceof IntervalRealVariable);
        assertEquals(0.6, ((IntervalRealVariable) actual).getLowerBound(), 0.01);
        assertEquals(0.6, ((IntervalRealVariable) actual).getUpperBound(), 0.01);
    }
}