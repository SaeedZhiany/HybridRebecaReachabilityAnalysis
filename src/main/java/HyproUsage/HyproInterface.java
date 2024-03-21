public class HyproInterface { // Save as HyproInterface.java

  public static double[] callHypro(
    String[] ODEs,
    double[] intervals,
    double[] reachParams
  ) {
    HelloJNI hj = new HelloJNI();

    double[] resultArray = hj.sayHello(ODEs, intervals, reachParams);

    // System.out.println("Results from Java");

    // for (int i = 0; i<resultArray.length; i++)
    //    System.out.println(resultArray[i]);

    return resultArray;
  }

  public static void main(String[] args) {}
}
