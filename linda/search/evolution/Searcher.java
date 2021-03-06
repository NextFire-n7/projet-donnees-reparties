package linda.search.evolution;

import linda.*;
import java.util.Arrays;
import java.util.UUID;

public class Searcher implements Runnable {

    private Linda linda;
    private boolean search_done = false;

    private static final int MAX_SEARCHERS = 1; // arbitrary.

    public Searcher(Linda linda) {
        this.linda = linda;
    }

    public void run() {
        System.out.println("Ready to do a search");
        while(!search_done) {
            Tuple treq = linda.take(new Tuple(Code.Request, UUID.class, String.class, Integer.class));
            UUID reqUUID = (UUID)treq.get(1);
            String req = (String) treq.get(2);

            // if there can be more parrallel searches, re-add the request.
            int nbSearchers = (int)treq.get(3);
            if (nbSearchers < MAX_SEARCHERS - 1){
                treq.set(3, nbSearchers+1);
                linda.write(treq);
            }
            Tuple tv;
            System.out.println("Looking for: " + req);
            search_done = true;
            while ((tv = linda.tryTake(new Tuple(Code.Value, reqUUID, String.class))) != null) {
                String val = (String) tv.get(2);
                int dist = getLevenshteinDistance(req, val);
                if (dist < 10) { // arbitrary
                    linda.write(new Tuple(Code.Result, reqUUID, val, dist));
                }

                // signal the manager that this searcher is working on its request
                linda.write(new Tuple(Code.Searcher, "searching", reqUUID));

                // If the manager stops searching
                if (linda.tryRead(new Tuple(Code.Manager, reqUUID)) == null) {
                    search_done = false;
                    break;
                }
            }
            
        }
    }
    
    /*****************************************************************/

    /* Levenshtein distance is rather slow */
    /* Copied from https://www.baeldung.com/java-levenshtein-distance */
    static int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                                   + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                                   dp[i - 1][j] + 1, 
                                   dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

}

