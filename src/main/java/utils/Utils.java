package utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Utils {

    @Nullable
    private static String password = null;

    @Nonnull
    public static String getUserPassword() {
        if (password == null) {
            Scanner scanner = new Scanner(System.in);
            do {
                System.out.print("Password: ");
                password = scanner.nextLine();
            } while (password == null || password.equals(""));
        }
        return password;
    }

    public static void removeFile(String path) {
        final File file = new File(path);
        if (!file.delete()) {
            try {
                Runtime rt = Runtime.getRuntime();
                final String flag = file.isDirectory() ? "-r " : "";
                String[] command = {
                        "/bin/sh",
                        "-c",
                        "echo " + Utils.getUserPassword() + " | sudo -S echo | sudo -S rm " + flag + path + "/"
                };
                rt.exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String removeTailingZeros(@Nonnull String number) {
        return number.contains(".") ? number.replaceAll("0*$", "").replaceAll("\\.$", "") : number;
    }

    @Nonnull
    public static String removeEndLetter(@Nonnull String number) {
        final String newNumber = number.endsWith("f") || number.endsWith("d")
                ? number.substring(0, number.length() - 1)
                : number;
        return removeTailingZeros(newNumber);
    }

    public static boolean isNumber(String number) {
        return isInteger(number) || isFloat(number) || isDouble(number);
    }

    public static boolean isInteger(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isFloat(String number) {
        try {
            Float.parseFloat(number);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isDouble(String number) {
        try {
            Double.parseDouble(number);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Nonnull
    public static String manipulateArithmeticExpression(@Nonnull String left, @Nonnull String op, @Nonnull String right) {
        final String newLeft = removeTailingZeros(left);
        final String newRight = removeTailingZeros(right);
        switch (op) {
            case "+": {
                switch (newRight.substring(0, 1)) {
                    case "+":
                        return removeTailingZeros(manipulateSumExpression(newLeft, newRight.substring(1)));
                    case "-":
                        return removeTailingZeros(manipulateSubExpression(newLeft, newRight.substring(1)));
                    default:
                        return removeTailingZeros(manipulateSumExpression(newLeft, newRight));
                }
            }
            case "-": {
                switch (newRight.substring(0, 1)) {
                    case "+":
                        return removeTailingZeros(manipulateSubExpression(newLeft, newRight.substring(1)));
                    case "-":
                        return removeTailingZeros(manipulateSumExpression(newLeft, newRight.substring(1)));
                    default:
                        return removeTailingZeros(manipulateSubExpression(newLeft, newRight));
                }
            }
            case "*":
                return removeTailingZeros(manipulateMultiplyExpression(newLeft, newRight));
            case "/": {
                return removeTailingZeros(manipulateDivideExpression(newLeft, newRight));
            }
            default:
                return "(" + newLeft + " " + op + " " + newRight + ")";
        }
    }

    @Nonnull
    private static String manipulateSumExpression(@Nonnull String left, @Nonnull String right) {
        return new BigDecimal(left).add(new BigDecimal(right)).toPlainString();
    }

    @Nonnull
    private static String manipulateSubExpression(@Nonnull String left, @Nonnull String right) {
        return new BigDecimal(left).subtract(new BigDecimal(right)).toPlainString();
    }

    @Nonnull
    private static String manipulateMultiplyExpression(@Nonnull String left, @Nonnull String right) {
        return new BigDecimal(left).multiply(new BigDecimal(right)).toPlainString();
    }

    @Nonnull
    private static String manipulateDivideExpression(@Nonnull String left, @Nonnull String right)
            throws ArithmeticException {
        return new BigDecimal(left).divide(new BigDecimal(right), RoundingMode.UNNECESSARY).toPlainString();
    }
}
