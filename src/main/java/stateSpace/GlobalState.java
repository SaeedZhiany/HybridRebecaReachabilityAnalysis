package stateSpace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalState {
    private String mode;

    public GlobalState() {
    }

    public List<Set<String>>  getModes() {
        List<Set<String>> globalStateModes = new ArrayList<>();

        // Adding sets to the list
        Set<String> set1 = new HashSet<>();
        set1.add("hws");
        set1.add("On");
        globalStateModes.add(set1);

//        Set<String> set3 = new HashSet<>();
//        set3.add("hws2");
//        set3.add("On");
//        globalStateModes.add(set3);

//        Set<String> set2 = new HashSet<>();
//        set2.add("hws1");
//        set2.add("Off");
//        globalStateModes.add(set2);

//        Set<String> set4 = new HashSet<>();
//        set4.add("hws");
//        set4.add("Off");
//        globalStateModes.add(set4);

        return  globalStateModes;
    }
}
