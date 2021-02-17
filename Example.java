public class Example {

    public void longAndComplexMethod(int a, int b, int c, int d) {
        if (a == b) {
            if (b == c) {
                if (c == d) {
                    System.out.println("All equal");
                } else {
                    System.out.println("Not all equal");
                }
            } else {
                System.out.println("Not all equal");
            }
        } else {
            if (b == c) {
                if (c == d) {
                    System.out.println("C and D equal");
                } else {
                    System.out.println("Not equal");
                }
            } else {
                System.out.println("Not equal");
            }
        }
    }
}
