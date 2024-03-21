public class HIMain {

  public static void main(String[] args) {
    try {
      // 1)get rebec file and convert into AST

      // 2)create global state : physical has a mode

      // 3) write a fun that receives the global state and AST and returns the set of flows

      HyproInterface hypro = new HyproInterface();

      String[] ODEs = { "x'=-0.5-y-1.5*x*x-0.5*x*x*x", "y'=3*x-y" };
      double[] intervals = new double[] { 0.8, 1.2, 0.8, 1.2 };

      // reachability parameters
      // Max_iter_number(int), Stop_ration, timeStepSize, Taylor_model_Order(int), Time
      double[] reachParams = new double[] { 50.0, 0.99, 0.01, 7.0, 5 };

      double[] resultArray = hypro.callHypro(ODEs, intervals, reachParams); // Create an instance and invoke the native method

      System.out.println("Results from Java");

      for (int i = 0; i < resultArray.length; i++) System.out.println(
        resultArray[i]
      );
      // 1) test : one physical actor

      // 2) two physical actors each in different modes

      // but all physicals are instances of a class

      // 3) two physical actors each in different modes, but each instance belongs to a class

      // class TimeProgress

      // extractFlows

      // class HyproAPI

      // main(){

      // \\ file .rebec convert AST

      // TP = new TimeProgress(AST)

      // new GlobalState()

      // HybroAPI.Calcultate(TP.extractFlow(),.....)

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
