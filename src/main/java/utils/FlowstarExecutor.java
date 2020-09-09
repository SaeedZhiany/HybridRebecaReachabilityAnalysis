package utils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import dataStructure.AnalysisResult;
import dataStructure.AnalysisSafetyResult;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FlowstarExecutor {

    private static final Pattern analyzeSafetyResultPattern = Pattern.compile(
            "(Result of the safety verification on the computed flowpipes: )(SAFE|UNSAFE|UNKNOWN)\\n"
    );
    private static final Pattern flowstarTimerValuePattern = Pattern.compile(
            "(time = )(\\d+(\\.\\d+)?),(?!(.*\\s*.*)+time = \\d(\\.\\d+)?)"
    );

    private static Pattern getContinuesVariableResultPattern(@Nonnull String varName) {
        return Pattern.compile(varName + "=");    // TODO write regex
    }

    public static String execute(@Nonnull String fileFullPath) throws Exception {
        Runtime rt = Runtime.getRuntime();
        String[] command = {
                "/bin/sh",
                "-c",
                "echo " +
                        Utils.getUserPassword() +
                        " | sudo -S echo | sudo -S " +
                        Constants.DIRECTORY_FLOWSTAR_RUNNABLE +
                        " < " +
                        fileFullPath
        };
        Process proc = rt.exec(command);


        proc.waitFor();

        if (proc.exitValue() == 0) {
            return CharStreams.toString(new InputStreamReader(proc.getInputStream(), Charsets.UTF_8));
        } else {
            throw new Exception(
                    "flowstar exit with code " +
                            proc.exitValue() +
                            " when execute " +
                            fileFullPath,
                    new Throwable(CharStreams.toString(new InputStreamReader(proc.getErrorStream(), Charsets.UTF_8)))
            );
        }
    }

    @Nonnull
    public static AnalysisResult analyzeFlowstarModel(
            @Nonnull String fileFullPath,
            List<String> continuesVariablesNames
    ) throws Exception {
//        final String result = execute(fileFullPath); TODO uncomment this
        final String result = new Scanner(
                new File(Paths.get(Constants.DIRECTORY_ROOT, "FlowstarOutputExample.txt").toString())
        ).useDelimiter("\\Z").next(); // TODO remove this variable

        final Matcher safetyResultMatcher = analyzeSafetyResultPattern.matcher(result);
        AnalysisSafetyResult analysisSafetyResult = AnalysisSafetyResult.UNKNOWN;
        if (safetyResultMatcher.find() && safetyResultMatcher.groupCount() > 1) {
            analysisSafetyResult = AnalysisSafetyResult.valueOf(safetyResultMatcher.group(2));
        }

        final Matcher timeResultMatcher = flowstarTimerValuePattern.matcher(result);
        float timeElapsed = 0;
        if (timeResultMatcher.find() && timeResultMatcher.groupCount() > 1) {
            timeElapsed = Float.parseFloat(timeResultMatcher.group(2));
        }

        final HashMap<String, BigDecimal> continuesVariablesValuesMapping = new HashMap<>();
        continuesVariablesNames.forEach(varName -> {
            final Matcher variableResultMatcher = getContinuesVariableResultPattern(varName).matcher(result);
            continuesVariablesValuesMapping.put(varName, new BigDecimal(0)); // TODO
        });

        return new AnalysisResult(
                analysisSafetyResult,
                timeElapsed,
                continuesVariablesValuesMapping
        );
    }
}
