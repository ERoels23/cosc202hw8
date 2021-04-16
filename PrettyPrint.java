import java.util.*;
import java.io.*;

public class PrettyPrint {

    public static List<Integer> splitWords(int[] lengths, int L, SlackFunctor sf) {

        /* TODO: Add pretty printing implementation here.   
        1. construct slacks[i][j]
        2. construct cost[i][j]
        3. construct costj[j] 
        4. then somehow we construct p[] from that...
        */

        // first, make sure no words are greater than L chars long...
        for (int l: lengths) {
            if (l > L) {
                return null;
            }
        }

        int num_words = lengths.length;
        int bigboi = 9999;
        int[][] cost = new int[num_words][num_words];

        // construct costs[i][j] array
        // if words i through j were in one line, how much slack?
        for (int start = 0; start < num_words; start++) {
            for (int end = 0; end < num_words; end++) {
                // for each set of start/end values
                if (start > end) {
                    // when start > end, it's meaningless, make it large
                    cost[start][end] = bigboi;
                }
                
                int spaces = end-start;
                int chars = 0;
                // get total length of all the words
                // TAKE ADVANTAGE OF PREVIOUS ENTRY TO REDUCE TIME COMPLEXITY
                for (int word = start; word <= end; word++) {
                    chars += lengths[word];
                }
                // if it's too big, insert large number (INF)
                if (chars+spaces > L) {
                    cost[start][end] = bigboi;
                } else {
                    cost[start][end] = (int) sf.f(L - chars - spaces);
                }
            }
        }

        // construct costj[j] array
        // calculates optimal (least) amount of slack when placing words 0 through j in multiple lines
        // costj[j] = min[1<i<j] (costj[j-1] + cost[i][j])
        int[] costj = new int[num_words];
        int[] breaks = new int[num_words];
        
        costj[0] = 0;
        for(int j = 1; j < num_words; j++) {
            int minimum = bigboi;
            int min_i = 0;
            for (int i = 1; i <= j; i++) {
                int exp = costj[j-1] + cost[i][j];
                if (exp <= minimum) {
                    minimum = exp;
                    min_i = i;
                }
            }
            costj[j] = minimum;
            breaks[j] = min_i;
        }

        List<Integer> breaksList = new ArrayList<Integer>();
        int line_start = breaks[num_words-1];
        while (line_start > 1) {
            // working backwards, need to reverse the List
            breaksList.add(0, line_start);
            line_start = breaks[line_start];
        }
        breaksList.add(num_words-1);

        System.out.println(Arrays.toString(breaksList.toArray()));

        return breaksList;
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