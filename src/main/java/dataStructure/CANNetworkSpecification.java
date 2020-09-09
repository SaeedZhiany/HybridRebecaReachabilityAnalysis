package dataStructure;

import java.util.HashMap;

/**
 * TODO: this class is temporary and must be integrated within hybrid rebeca compiler
 */
public class CANNetworkSpecification {

    /**
     * key: concatenation of "source", "destination", and "message" names
     * value: delay specified in main block of HRebeca code for the related key.
     */
    private HashMap<String, Float> delaySpecs;

    /**
     * key: concatenation of "source", "destination", and "message" names
     * value: priority specified in main block of HRebeca code for the related key.
     */
    private HashMap<String, Integer> prioritySpecs;

    public CANNetworkSpecification(HashMap<String, Float> delaySpecs, HashMap<String, Integer> prioritySpecs) {
        this.delaySpecs = delaySpecs;
        this.prioritySpecs = prioritySpecs;
    }

    public Integer getPrioritySpec(String src, String dest, String message) {
        return this.prioritySpecs.get(src + dest + message);
    }

    public Float getDelaySpec(String src, String dest, String message) {
        return this.delaySpecs.get(src + dest + message);
    }
}
