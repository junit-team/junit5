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
    
    public int complexMethod(int a) {
        switch (a) {
            case 1 : return 1;
            case 2 : return 2;
            case 3 : return 3;
            case 4 : return 4;
            case 5 : return 5;
            case 6 : return 6;
            case 7 : return 7;
            case 8 : return 8;
            case 9 : return 9;
            case 10 : return 10;
            case 11 : return 11;
            default : throw new AssertionError(e);
        } 
    }
    
    public int complexMethod2(int a) {
        switch (a) {
            case 1 : return 1;
            case 2 : return 2;
            case 3 : return 3;
            case 4 : return 4;
            case 5 : return 5;
            case 6 : return 6;
            case 7 : return 7;
            case 8 : return 8;
            case 9 : return 9;
            case 10 : return 10;
            case 11 : return 11;
            default : throw new AssertionError(e);
        } 
    }
}
