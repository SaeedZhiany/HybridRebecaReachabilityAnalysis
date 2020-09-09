package converters.templates;

import dataStructure.ContinuousVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FlowstarDataModel {

    @Nonnull
    private ContinuousVariable[] vars;
    @Nonnull
    private SettingBlock settingBlock;
    @Nonnull
    private ModeDefinition modeDefinition;
    @Nonnull
    private JumpDefinition jumpDefinition;
    @Nonnull
    private String outputFileName;

    public FlowstarDataModel(
            @Nonnull List<ContinuousVariable> vars,
            @Nonnull SettingBlock settingBlock,
            @Nonnull ModeDefinition modeDefinition,
            @Nonnull JumpDefinition jumpDefinition,
            @Nonnull String outputFileName
    ) {
        this.vars = vars.toArray(new ContinuousVariable[0]);
        this.settingBlock = settingBlock;
        this.modeDefinition = modeDefinition;
        this.jumpDefinition = jumpDefinition;
        this.outputFileName = outputFileName;
    }

    @Nonnull
    public ContinuousVariable[] getVars() {
        return vars;
    }

    @Nonnull
    public SettingBlock getSettingBlock() {
        return settingBlock;
    }

    @Nonnull
    public ModeDefinition getModeDefinition() {
        return modeDefinition;
    }

    @Nonnull
    public JumpDefinition getJumpDefinition() {
        return jumpDefinition;
    }

    @Nonnull
    public String getOutputFileName() {
        return outputFileName;
    }

    public static class SettingBlock {
        @Nullable
        private Integer time;
        @Nullable
        private Float fixedSteps;
        @Nullable
        private Integer remainderEstimation;
        @Nullable
        private String[] gnuplotOctagonVars;
        @Nullable
        private Integer[] adaptiveOrders;
        @Nullable
        private Integer cutoff;
        @Nullable
        private Integer precision;
        @Nullable
        private Integer maxJumps;

        public SettingBlock(
                @Nullable Integer time,
                @Nullable Float fixedSteps,
                @Nullable Integer remainderEstimation,
                @Nullable String[] gnuplotOctagonVars,
                @Nullable Integer[] adaptiveOrders,
                @Nullable Integer cutoff,
                @Nullable Integer precision,
                @Nullable Integer maxJumps) {
            this.time = time != null ? time : 100;
            this.fixedSteps = fixedSteps != null ? fixedSteps : 0.5f;
            this.remainderEstimation = remainderEstimation != null ? remainderEstimation : 8;
            this.gnuplotOctagonVars = gnuplotOctagonVars != null ? gnuplotOctagonVars : new String[0];
            this.adaptiveOrders = adaptiveOrders != null ? adaptiveOrders : new Integer[]{1, 9};
            this.cutoff = cutoff != null ? cutoff : 6;
            this.precision = precision != null ? precision : 7;
            this.maxJumps = maxJumps != null ? maxJumps : 50;
        }

        @Nullable
        public Integer getTime() {
            return time;
        }

        @Nullable
        public Float getFixedSteps() {
            return fixedSteps;
        }

        @Nullable
        public Integer getRemainderEstimation() {
            return remainderEstimation;
        }

        @Nullable
        public String[] getGnuplotOctagonVars() {
            return gnuplotOctagonVars;
        }

        @Nullable
        public Integer[] getAdaptiveOrders() {
            return adaptiveOrders;
        }

        @Nullable
        public Integer getCutoff() {
            return cutoff;
        }

        @Nullable
        public Integer getPrecision() {
            return precision;
        }

        @Nullable
        public Integer getMaxJumps() {
            return maxJumps;
        }
    }

    public static class ModeDefinition {

        @Nullable
        private String invariantCondition;
        @Nullable
        private AssignmentExp[] ODEAssignments;

        public ModeDefinition(@Nullable String invariantCondition, @Nullable List<AssignmentExp> ODEAssignments) {
            this.invariantCondition = invariantCondition;
            this.ODEAssignments = ODEAssignments != null ? ODEAssignments.toArray(new AssignmentExp[0]) : null;
        }

        @Nullable
        public String getInvariantCondition() {
            return invariantCondition;
        }

        @Nullable
        public AssignmentExp[] getODEAssignments() {
            return ODEAssignments;
        }
    }

    public static class JumpDefinition {
        @Nullable
        private String guardCondition;
        @Nullable
        private AssignmentExp[] resetAssignments;

        public JumpDefinition(@Nullable String guardCondition, @Nullable List<AssignmentExp> resetAssignments) {
            this.guardCondition = guardCondition;
            this.resetAssignments = resetAssignments != null ? resetAssignments.toArray(new AssignmentExp[0]) : null;
        }

        @Nullable
        public String getGuardCondition() {
            return guardCondition;
        }

        @Nullable
        public AssignmentExp[] getResetAssignments() {
            return resetAssignments;
        }
    }

    public static class AssignmentExp {
        @Nonnull
        private String left;
        @Nonnull
        private String right;

        public AssignmentExp(@Nonnull String left, @Nonnull String right) {
            this.left = left;
            this.right = right;
        }

        @Nonnull
        public String getLeft() {
            return left;
        }

        @Nonnull
        public String getRight() {
            return right;
        }
    }
}
