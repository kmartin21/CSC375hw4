package com.company;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by keithmartin on 11/25/15.
 */
abstract class JTree extends RecursiveAction {
    volatile double maxDiff; // for convergence check
    public void reset() {
        maxDiff = 0;
    }
}

class Interior extends JTree {
    private final JTree[] quads;
    private ForkJoinPool pool = new ForkJoinPool();
    Interior(JTree q1, JTree q2, JTree q3, JTree q4) {
        quads = new JTree[] { q1, q2, q3, q4 };
    }

    public void compute() {
        pool.invoke(quads[0]);
        double md = 0.0;
        for (int i = 0; i < quads.length; ++i) {
            md = Math.max(md,quads[i].maxDiff);
            quads[i].reset();
        }
        maxDiff = md;
    }



}
