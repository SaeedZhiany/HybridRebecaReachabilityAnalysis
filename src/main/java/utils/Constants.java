package utils;

import java.nio.file.Paths;

public class Constants {

    // --------------------------- Directories ------------------------------------
    public static final String DIRECTORY_ROOT = System.getProperty("user.dir");
    public static final String DIRECTORY_SAMPLES =
            Paths.get(DIRECTORY_ROOT, "hybrid rebeca sample codes").toString();
    public static final String DIRECTORY_TEMPLATES =
            Paths.get(DIRECTORY_ROOT, "src", "main", "java", "converters", "templates").toString();
    public static final String DIRECTORY_OUTPUTS =
            Paths.get(DIRECTORY_ROOT, "outputs").toString();
    public static final String DIRECTORY_COUNTER_EXAMPLES =
            Paths.get(DIRECTORY_ROOT, "counterexamples").toString();
    public static final String DIRECTORY_IMAGES =
            Paths.get(DIRECTORY_ROOT, "images").toString();
    public static final String DIRECTORY_FLOWSTAR =
            Paths.get(DIRECTORY_ROOT, "flowstar_2_1_0").toString();
    public static final String DIRECTORY_FLOWSTAR_RUNNABLE =
            Paths.get(DIRECTORY_FLOWSTAR, "flowstar").toString();

    // --------------------------- Console messages colors ------------------------
    public static String DefaultColor = "\u001B[0m";
    public static String Green = "\u001B[32m";
}
