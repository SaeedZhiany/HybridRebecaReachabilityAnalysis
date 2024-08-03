package visitors;

import dataStructure.DiscreteBoolVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.min;
import static org.apache.commons.lang3.math.NumberUtils.max;

public class ExpressionExtractorVisitor extends Visitor<Variable> {
    Map<String, Variable> symbolTable;

    public ExpressionExtractorVisitor(Map<String, Variable> variablesValuation) {
        this.symbolTable = variablesValuation;
    }

    private IntervalRealVariable intervalMultiply(IntervalRealVariable left, IntervalRealVariable right) {
        return (new IntervalRealVariable("", (
                min(left.getLowerBound() * right.getLowerBound(),
                        left.getLowerBound() * right.getUpperBound(),
                        left.getUpperBound() * right.getLowerBound(),
                        left.getUpperBound() * right.getUpperBound())),
                max(left.getLowerBound() * right.getLowerBound(),
                        left.getLowerBound() * right.getUpperBound(),
                        left.getUpperBound() * right.getLowerBound(),
                        left.getUpperBound() * right.getUpperBound())));
    }

    private Boolean isDefinite(DiscreteBoolVariable left, DiscreteBoolVariable right, String operator) {
        if (left.getDefinite() == right.getDefinite())
            return left.getDefinite();
        DiscreteBoolVariable definite = left.getDefinite() ? left : right;
        DiscreteBoolVariable indefinite = left.getDefinite() ? right : left;
        switch (operator) {
            case "&&": case "&":{
                return definite.getDefinite() && !definite.getValue();
            }
            case "||": case "|": {
                return definite.getDefinite() && definite.getValue();
            }
            default:
                return false;
        }
    }

    private DiscreteBoolVariable intervalComparison(IntervalRealVariable left, IntervalRealVariable right, String operator) {
        switch (operator) {
            case ">": {
                if (left.getLowerBound() > right.getUpperBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getUpperBound() <= right.getLowerBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case "<": {
                if (left.getUpperBound() < right.getLowerBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getLowerBound() >= right.getUpperBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case ">=": {
                if (left.getLowerBound() >= right.getUpperBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getUpperBound() < right.getLowerBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case "<=": {
                if (left.getUpperBound() <= right.getLowerBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getLowerBound() > right.getUpperBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            default:
                return null;
        }
    }

    private DiscreteBoolVariable intervalDiscreteComparison(IntervalRealVariable left, DiscreteDecimalVariable right, String operator) {
        switch (operator) {
            case ">": {
                if (left.getLowerBound() > right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", true);
                else if (left.getUpperBound() <= right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case "<": {
                if (left.getUpperBound() < right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", true);
                else if (left.getLowerBound() >= right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case ">=": {
                if (left.getLowerBound() >= right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", true);
                else if (left.getUpperBound() < right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case "<=": {
                if (left.getUpperBound() <= right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", true);
                else if (left.getLowerBound() > right.getValue().doubleValue())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            default:
                return null;
        }
    }

    private DiscreteBoolVariable intervalDiscreteComparison(DiscreteDecimalVariable left, IntervalRealVariable right, String operator) {
        switch (operator) {
            case ">": {
                if (left.getValue().doubleValue() > right.getUpperBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getValue().doubleValue() <= right.getLowerBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case "<": {
                if (left.getValue().doubleValue() < right.getLowerBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getValue().doubleValue() >= right.getUpperBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case ">=": {
                if (left.getValue().doubleValue() >= right.getUpperBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getValue().doubleValue() < right.getLowerBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            case "<=": {
                if (left.getValue().doubleValue() <= right.getLowerBound())
                    return new DiscreteBoolVariable("", true);
                else if (left.getValue().doubleValue() > right.getUpperBound())
                    return new DiscreteBoolVariable("", false);
                else
                    return new DiscreteBoolVariable("", false, false);
            }
            default:
                return null;
        }
    }

    public Variable visit(Expression expression) {
        if (expression instanceof DotPrimary) {
            return visit((DotPrimary) expression);
        } else if (expression instanceof UnaryExpression) {
            return visit((UnaryExpression) expression);
        } else if (expression instanceof BinaryExpression) {
            return visit((BinaryExpression) expression);
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
        switch (operator) {
            case "+": {
                if (operand instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) operand).getValue());
                } else if (operand instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) operand).getLowerBound(), ((IntervalRealVariable) operand).getUpperBound());
                }
            }
            case "-": {
                if (operand instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) operand).getValue().negate());
                } else if (operand instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", -((IntervalRealVariable) operand).getUpperBound(), -((IntervalRealVariable) operand).getLowerBound());
                }
            }
            case "!": {
                if (operand instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", !((DiscreteBoolVariable) operand).getValue(), ((DiscreteBoolVariable) operand).getDefinite());
                }
            }
            default:
                return null;
        }
    }

    @Override
    public Variable visit(BinaryExpression binaryExpression) {
        Variable left = this.visit(binaryExpression.getLeft());
        Variable right = this.visit(binaryExpression.getRight());
        String operator = binaryExpression.getOperator();
        switch (operator) {
            case "+": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) left).getValue().add(((DiscreteDecimalVariable) right).getValue()));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() + ((IntervalRealVariable) right).getLowerBound(), ((IntervalRealVariable) left).getUpperBound() + ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) right).getLowerBound() + ((DiscreteDecimalVariable) left).getValue().doubleValue(), ((IntervalRealVariable) right).getUpperBound() + ((DiscreteDecimalVariable) left).getValue().doubleValue());
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() + ((DiscreteDecimalVariable) right).getValue().doubleValue(), ((IntervalRealVariable) left).getUpperBound() + ((DiscreteDecimalVariable) right).getValue().doubleValue());
                }
            }
            case "-": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) left).getValue().subtract(((DiscreteDecimalVariable) right).getValue()));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() - ((IntervalRealVariable) right).getLowerBound(), ((IntervalRealVariable) left).getUpperBound() - ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((DiscreteDecimalVariable) left).getValue().doubleValue() - ((IntervalRealVariable) right).getLowerBound(), ((DiscreteDecimalVariable) left).getValue().doubleValue() - ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() - ((DiscreteDecimalVariable) right).getValue().doubleValue(), ((IntervalRealVariable) left).getUpperBound() - ((DiscreteDecimalVariable) right).getValue().doubleValue());
                }
            }
            case "*": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) left).getValue().multiply(((DiscreteDecimalVariable) right).getValue()));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return intervalMultiply((IntervalRealVariable) left, (IntervalRealVariable) right);
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((DiscreteDecimalVariable) left).getValue().doubleValue() * ((IntervalRealVariable) right).getLowerBound(), ((DiscreteDecimalVariable) left).getValue().doubleValue() * ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() * ((DiscreteDecimalVariable) right).getValue().doubleValue(), ((IntervalRealVariable) left).getUpperBound() * ((DiscreteDecimalVariable) right).getValue().doubleValue());
                }
            }
            case "/": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) left).getValue().divide(((DiscreteDecimalVariable) right).getValue(), RoundingMode.CEILING));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return intervalMultiply((IntervalRealVariable) left, new IntervalRealVariable("", (1 / ((IntervalRealVariable) right).getUpperBound()), (1 / ((IntervalRealVariable) right).getLowerBound())));
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((DiscreteDecimalVariable) left).getValue().doubleValue() / ((IntervalRealVariable) right).getLowerBound(), ((DiscreteDecimalVariable) left).getValue().doubleValue() / ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() / ((DiscreteDecimalVariable) right).getValue().doubleValue(), ((IntervalRealVariable) left).getUpperBound() / ((DiscreteDecimalVariable) right).getValue().doubleValue());
                }
            }
            case "%": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", ((DiscreteDecimalVariable) left).getValue().remainder(((DiscreteDecimalVariable) right).getValue()));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() % ((IntervalRealVariable) right).getLowerBound(), ((IntervalRealVariable) left).getUpperBound() % ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new IntervalRealVariable("", ((DiscreteDecimalVariable) left).getValue().doubleValue() % ((IntervalRealVariable) right).getLowerBound(), ((DiscreteDecimalVariable) left).getValue().doubleValue() % ((IntervalRealVariable) right).getUpperBound());
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new IntervalRealVariable("", ((IntervalRealVariable) left).getLowerBound() % ((DiscreteDecimalVariable) right).getValue().doubleValue(), ((IntervalRealVariable) left).getUpperBound() % ((DiscreteDecimalVariable) right).getValue().doubleValue());
                }
            }
            case "&&": {
                if (left instanceof DiscreteBoolVariable && right instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteBoolVariable) left).getValue() && ((DiscreteBoolVariable) right).getValue(), isDefinite((DiscreteBoolVariable) left, (DiscreteBoolVariable) right, "&&"));
                }
            }
            case "||": {
                if (left instanceof DiscreteBoolVariable && right instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteBoolVariable) left).getValue() || ((DiscreteBoolVariable) right).getValue(), isDefinite((DiscreteBoolVariable) left, (DiscreteBoolVariable) right, "||"));
                }
            }
            case "==": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteDecimalVariable) left).getValue().equals(((DiscreteDecimalVariable) right).getValue()));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return new DiscreteBoolVariable("", ((IntervalRealVariable) left).getLowerBound().equals(((IntervalRealVariable) right).getLowerBound()) && ((IntervalRealVariable) left).getUpperBound().equals(((IntervalRealVariable) right).getUpperBound()));
                } else if (left instanceof DiscreteBoolVariable && right instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteBoolVariable) left).getValue().equals(((DiscreteBoolVariable) right).getValue()), isDefinite((DiscreteBoolVariable) left, (DiscreteBoolVariable) right, "=="));
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new DiscreteBoolVariable("", (((DiscreteDecimalVariable) left).getValue().doubleValue() == ((IntervalRealVariable) right).getLowerBound()) && (((DiscreteDecimalVariable) left).getValue().doubleValue() == ((IntervalRealVariable) right).getUpperBound()));
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", (((IntervalRealVariable) left).getLowerBound() == ((DiscreteDecimalVariable) right).getValue().doubleValue()) && (((IntervalRealVariable) left).getUpperBound() == ((DiscreteDecimalVariable) right).getValue().doubleValue()));
                }
            }
            case "!=": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", !((DiscreteDecimalVariable) left).getValue().equals(((DiscreteDecimalVariable) right).getValue()));
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return new DiscreteBoolVariable("", !(((IntervalRealVariable) left).getLowerBound().equals(((IntervalRealVariable) right).getLowerBound()) && ((IntervalRealVariable) left).getUpperBound().equals(((IntervalRealVariable) right).getUpperBound())));
                } else if (left instanceof DiscreteBoolVariable && right instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", !((DiscreteBoolVariable) left).getValue().equals(((DiscreteBoolVariable) right).getValue()), isDefinite((DiscreteBoolVariable) left, (DiscreteBoolVariable) right, "!="));
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return new DiscreteBoolVariable("", !(((DiscreteDecimalVariable) left).getValue().doubleValue() == ((IntervalRealVariable) right).getLowerBound()) && (((DiscreteDecimalVariable) left).getValue().doubleValue() == ((IntervalRealVariable) right).getUpperBound()));
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", !(((IntervalRealVariable) left).getLowerBound() == ((DiscreteDecimalVariable) right).getValue().doubleValue()) && (((IntervalRealVariable) left).getUpperBound() == ((DiscreteDecimalVariable) right).getValue().doubleValue()));
                }
            }
            case "&": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", new BigDecimal(((DiscreteDecimalVariable) left).getValue().intValue() & ((DiscreteDecimalVariable) right).getValue().intValue()));
                } else if (left instanceof DiscreteBoolVariable && right instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteBoolVariable) left).getValue() & ((DiscreteBoolVariable) right).getValue(), isDefinite((DiscreteBoolVariable) left, (DiscreteBoolVariable) right, "&"));
                }
            }
            case "|": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteDecimalVariable("", new BigDecimal(((DiscreteDecimalVariable) left).getValue().intValue() | ((DiscreteDecimalVariable) right).getValue().intValue()));
                } else if (left instanceof DiscreteBoolVariable && right instanceof DiscreteBoolVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteBoolVariable) left).getValue() | ((DiscreteBoolVariable) right).getValue(), isDefinite((DiscreteBoolVariable) left, (DiscreteBoolVariable) right, "|"));
                }
            }
            case ">=": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteDecimalVariable) left).getValue().compareTo(((DiscreteDecimalVariable) right).getValue()) >= 0);
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return intervalComparison((IntervalRealVariable) left, (IntervalRealVariable) right, ">=");
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return intervalDiscreteComparison((DiscreteDecimalVariable) left, (IntervalRealVariable) right, ">=");
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return intervalDiscreteComparison((IntervalRealVariable) left, (DiscreteDecimalVariable) right, ">=");
                }
            }
            case "<=": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteDecimalVariable) left).getValue().compareTo(((DiscreteDecimalVariable) right).getValue()) <= 0);
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return intervalComparison((IntervalRealVariable) left, (IntervalRealVariable) right, "<=");
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return intervalDiscreteComparison((DiscreteDecimalVariable) left, (IntervalRealVariable) right, "<=");
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return intervalDiscreteComparison((IntervalRealVariable) left, (DiscreteDecimalVariable) right, "<=");
                }
            }
            case "<": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteDecimalVariable) left).getValue().compareTo(((DiscreteDecimalVariable) right).getValue()) < 0);
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return intervalComparison((IntervalRealVariable) left, (IntervalRealVariable) right, "<");
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return intervalDiscreteComparison((DiscreteDecimalVariable) left, (IntervalRealVariable) right, "<");
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return intervalDiscreteComparison((IntervalRealVariable) left, (DiscreteDecimalVariable) right, "<");
                }
            }
            case ">": {
                if (left instanceof DiscreteDecimalVariable && right instanceof DiscreteDecimalVariable) {
                    return new DiscreteBoolVariable("", ((DiscreteDecimalVariable) left).getValue().compareTo(((DiscreteDecimalVariable) right).getValue()) > 0);
                } else if (left instanceof IntervalRealVariable && right instanceof IntervalRealVariable) {
                    return intervalComparison((IntervalRealVariable) left, (IntervalRealVariable) right, ">");
                } else if (left instanceof DiscreteDecimalVariable && right instanceof IntervalRealVariable) {
                    return intervalDiscreteComparison((DiscreteDecimalVariable) left, (IntervalRealVariable) right, ">");
                } else if (left instanceof IntervalRealVariable && right instanceof DiscreteDecimalVariable) {
                    return intervalDiscreteComparison((IntervalRealVariable) left, (DiscreteDecimalVariable) right, ">");
                }
            }

            default:
                return null;

        }
    }

    @Override
    public Variable visit(TermPrimary termPrimary) {
        return symbolTable.get(termPrimary.getName());
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
