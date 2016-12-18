package com.company;

import com.company.ServerInteraction.JMessage;
import com.company.ServerInteraction.Client;
import com.company.ServerInteraction.Server;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by keithmartin on 11/25/15.
 */
public class Jacobi extends RecursiveAction {
    static final double EPSILON = 0.001; // convergence criterion
    final Cell root;


    private ForkJoinPool pool = new ForkJoinPool();
    final int maxSteps;

    public Jacobi(MetalAlloy region, double[][] A, double[][] B, int loCol, int hiCol, int loRow, int hiRow, int maxSteps, boolean splitWorkToServer) {
        this.maxSteps = maxSteps;
        root = new Cell(region, A, B, loCol, hiCol, loRow, hiRow, splitWorkToServer);
    }


    public void compute(){
        double rawResult = 0.1;
        System.out.println("\nRunning Jacobi for a maximum of "+ maxSteps + " times");
        for (int i = 0; i < maxSteps; ++i) {
            pool.invoke(root);
            //System.out.println("MAXDIFF: " + root.getRawResult());
            rawResult = root.getRawResult();
            if (rawResult < EPSILON) {
                System.out.println("\n\nJacobi > #[End Program] *Converged after " + i + " steps with " + rawResult);
                endProgram();
                return;
            } else {
                //System.out.println("*md: " + root.getRawResult());
                root.reinitialize();
            }
        }
        System.out.printf("\n\nJacobi > #[End Program] maxDiff of %s Did not converge after " + maxSteps + " steps", rawResult);
        endProgram();
    }

    private void endProgram() {
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
