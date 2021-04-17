import java.util.*;
import java.io.*;

public class PrettyPrint {

    public static List<Integer> splitWords(int[] lengths, int L, SlackFunctor sf) {

        // first, make sure no words are greater than L chars long...
        for (int l: lengths) {
            if (l > L) {
                return null;
            }
        }

        int num_words = lengths.length;
        int bigboi = 9999;
        
        // construct costj[j] array
        // calculates optimal (least) amount of slack when placing words 0 through j in multiple lines
        // costj[j] = min[1<i<j] (costj[j-1] + cost[i][j])
        int[] costj = new int[num_words];
        int[] breaks = new int[num_words];
        
        for(int j = 0; j < num_words; j++) {
            int minimum = bigboi;
            int min_i = 0;
            boolean tooLong = false;
            int runningTotal = 0;

            for (int i = 0; i <= j; i++) {
                int exp = 0;
                if (j == 0) {
                    exp = getCost(i, lengths, L);
                    runningTotal += exp;
                    exp = (int) sf.f(exp);
                } else if (!tooLong) {
                    exp = costj[j-1] + runningTotal + getCost(i, lengths, L);
                    runningTotal += exp;
                    exp = (int) sf.f(exp);
                }
                if (exp < minimum) {
                    minimum = exp;
                    min_i = i;
                }
                if (exp == bigboi) {
                    tooLong = true;
                }
            }
            costj[j] = minimum;
            breaks[j] = min_i - 1;
        }

        List<Integer> breaksList = new ArrayList<Integer>();
        int line_start = breaks[num_words-1];
        while (line_start > 1) {
            // working backwards, need to reverse the List
            breaksList.add(0, line_start);
            line_start = breaks[line_start];
        }
        // this neglects the final word on the final line...
        breaksList.add(num_words-1);

        System.out.println(Arrays.toString(breaks));
        System.out.println(Arrays.toString(breaksList.toArray()));

        return breaksList;
    }

    public static int getCost(int ind, int[] lengths, int L) {
        int bigboi = 9999;
        int chars = lengths[ind];
        // if it's too big, insert large number (INF)
        if (chars+1 > L) {
            return bigboi;
        } else {
            return L - chars - 1;
        }
    }

    public static String help_message() {
        return
            "Usage: java PrettyPrint line_length [inputfile] [outputfile]\n" +
            "  line_length (required): an integer specifying the maximum length for a line\n" +
            "  inputfile (optional): a file from which to read the input text (stdin if a hyphen or omitted)\n" +
            "  outputfile (optional): a file in which to store the output text (stdout if omitted)"
            ;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: required argument line_length missing");
            System.err.println(help_message());
            System.exit(1);
            return;
        }

        int line_length;
        try {
            line_length = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error: could not find integer argument line_length");
            System.err.println(help_message());
            System.exit(2);
            return;
        }

        Scanner input = null;
        PrintStream output = null;
        if (args.length < 2) {
            input = new Scanner(System.in);
        }
        else if (args.length >= 2) {
            if (args[1].equals("-")) {
                input = new Scanner(System.in);
            } else {
                try {
                    input = new Scanner(new File(args[1]));
                } catch (FileNotFoundException e) {
                    System.err.println("Error: could not open input file");
                    System.err.println(help_message());
                    System.exit(3);
                    return;
                }
            }
        }

        if (args.length >= 3) {
            try {
                output = new PrintStream(args[2]);
            } catch (FileNotFoundException e) {
                System.err.println("Error: could not open output file");
                System.err.println(help_message());
                input.close();
                System.exit(4);
                return;
            }
        } else {
            output = System.out;
        }

        ArrayList<String> words = new ArrayList<String>();
        while (input.hasNext()) {
            words.add(input.next());
        }
        input.close();

        int[] lengths = new int[words.size()];
        for (int i = 0; i < words.size(); i++) {
            lengths[i] = words.get(i).length();
        }
        
        List<Integer> breaks = splitWords(lengths, line_length,
            new SlackFunctor() {
                public double f(int slack) { return slack * slack; }
            });

        if (breaks != null) {
            int current_word = 0;
            for (int next_break : breaks) {
                while (current_word < next_break) {
                    output.print(words.get(current_word++));
                    output.print(" ");
                }
                output.print(words.get(current_word++));
                output.println();
            }

            output.close();
            System.exit(0);
            return;
    
        } else {
            System.err.println("Error: formatting impossible; an input word exceeds line length");
            output.close();
            System.exit(5);
            return;
        }
    }
}