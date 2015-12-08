import java.util.Scanner;
import java.util.stream.IntStream;

public class Demo {
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);

        //$ Get a positive integer from the user
        System.out.print("Enter a positive integer: ");
        int n = reader.nextInt();

        //$ Prompt user for sum or product
        System.out.printf(
            "Would you like the sum of the positive integers up to %d? (y/n) ", n);

        //? Sum selected?
        if (reader.next().equals("y")) {
            //X Compute and print sum
            System.out.printf("The sum is %d.\n",
                IntStream.rangeClosed(1, n).sum());
        } else {
            //X Compute and print product
            System.out.printf("The product is %d.\n",
                IntStream.rangeClosed(1, n).reduce(1, (a,b) -> a * b));
        }
    }
}
