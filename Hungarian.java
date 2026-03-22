//package hungarian;
//package com.hinguapps.graph;

import java.util.Arrays;
import java.util.Scanner;
 
public class HungarianBipartiteMatching
{
    private final double[][] costMatrix;
    private final int        rows, cols, dim;
    private final double[]   labelByWorker, labelByJob;
    private final int[]      minSlackWorkerByJob;
    private final double[]   minSlackValueByJob;
    private final int[]      matchJobByWorker, matchWorkerByJob;
    private final int[]      parentWorkerByCommittedJob;
    private final boolean[]  committedWorkers;
 
    public HungarianBipartiteMatching(double[][] costMatrix)
    {
        this.dim = Math.max(costMatrix.length, costMatrix[0].length);
        this.rows = costMatrix.length;
        this.cols = costMatrix[0].length;
        this.costMatrix = new double[this.dim][this.dim];
        for (int w = 0; w < this.dim; w++)
        {
            if (w < costMatrix.length)
            {
                if (costMatrix[w].length != this.cols)
                {
                    throw new IllegalArgumentException("Irregular cost matrix");
                }
                this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
            }
            else
            {
                this.costMatrix[w] = new double[this.dim];
            }
        }
        labelByWorker = new double[this.dim];
        labelByJob = new double[this.dim];
        minSlackWorkerByJob = new int[this.dim];
        minSlackValueByJob = new double[this.dim];
        committedWorkers = new boolean[this.dim];
        parentWorkerByCommittedJob = new int[this.dim];
        matchJobByWorker = new int[this.dim];
        Arrays.fill(matchJobByWorker, -1);
        matchWorkerByJob = new int[this.dim];
        Arrays.fill(matchWorkerByJob, -1);
    }
 
    protected void computeInitialFeasibleSolution()
    {
        for (int j = 0; j < dim; j++)
        {
            labelByJob[j] = Double.POSITIVE_INFINITY;
        }
        for (int w = 0; w < dim; w++)
        {
            for (int j = 0; j < dim; j++)
            {
                if (costMatrix[w][j] < labelByJob[j])
                {
                    labelByJob[j] = costMatrix[w][j];
                }
            }
        }
    }
 
    public int[] execute()
    {
       
        reduce();
        computeInitialFeasibleSolution();
        greedyMatch();
        int w = fetchUnmatchedWorker();
        while (w < dim)
        {
            initializePhase(w);
            executePhase();
            w = fetchUnmatchedWorker();
        }
        int[] result = Arrays.copyOf(matchJobByWorker, rows);
        for (w = 0; w < result.length; w++)
        {
            if (result[w] >= cols)
            {
                result[w] = -1;
            }
        }
        return result;
    }
 
    protected void executePhase()
    {
        while (true)
        {
            int minSlackWorker = -1, minSlackJob = -1;
            double minSlackValue = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++)
            {
                if (parentWorkerByCommittedJob[j] == -1)
                {
                    if (minSlackValueByJob[j] < minSlackValue)
                    {
                        minSlackValue = minSlackValueByJob[j];
                        minSlackWorker = minSlackWorkerByJob[j];
                        minSlackJob = j;
                    }
                }
            }
            if (minSlackValue > 0)
            {
                updateLabeling(minSlackValue);
            }
            parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
            if (matchWorkerByJob[minSlackJob] == -1)
            {
                /*
                 * An augmenting path has been found.
                 */
                int committedJob = minSlackJob;
                int parentWorker = parentWorkerByCommittedJob[committedJob];
                while (true)
                {
                    int temp = matchJobByWorker[parentWorker];
                    match(parentWorker, committedJob);
                    committedJob = temp;
                    if (committedJob == -1)
                    {
                        break;
                    }
                    parentWorker = parentWorkerByCommittedJob[committedJob];
                }
                return;
            }
            else
            {
                /*
                 * Update slack values since we increased the size of the
                 * committed
                 * workers set.
                 */
                int worker = matchWorkerByJob[minSlackJob];
                committedWorkers[worker] = true;
                for (int j = 0; j < dim; j++)
                {
                    if (parentWorkerByCommittedJob[j] == -1)
                    {
                        double slack = costMatrix[worker][j]
                                - labelByWorker[worker] - labelByJob[j];
                        if (minSlackValueByJob[j] > slack)
                        {
                            minSlackValueByJob[j] = slack;
                            minSlackWorkerByJob[j] = worker;
                        }
                    }
                }
            }
        }
    }
 
    protected int fetchUnmatchedWorker()
    {
        int w;
        for (w = 0; w < dim; w++)
        {
            if (matchJobByWorker[w] == -1)
            {
                break;
            }
        }
        return w;
    }
 
    protected void greedyMatch()
    {
        for (int w = 0; w < dim; w++)
        {
            for (int j = 0; j < dim; j++)
            {
                if (matchJobByWorker[w] == -1
                        && matchWorkerByJob[j] == -1
                        && costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0)
                {
                    match(w, j);
                }
            }
        }
    }
 
    protected void initializePhase(int w)
    {
        Arrays.fill(committedWorkers, false);
        Arrays.fill(parentWorkerByCommittedJob, -1);
        committedWorkers[w] = true;
        for (int j = 0; j < dim; j++)
        {
            minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w]
                    - labelByJob[j];
            minSlackWorkerByJob[j] = w;
        }
    }
 
    protected void match(int w, int j)
    {
        matchJobByWorker[w] = j;
        matchWorkerByJob[j] = w;
    }
 
    protected void reduce()
    {
        for (int w = 0; w < dim; w++)
        {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++)
            {
                if (costMatrix[w][j] < min)
                {
                    min = costMatrix[w][j];
                }
            }
            for (int j = 0; j < dim; j++)
            {
                costMatrix[w][j] -= min;
            }
        }
        double[] min = new double[dim];
        for (int j = 0; j < dim; j++)
        {
            min[j] = Double.POSITIVE_INFINITY;
        }
        for (int w = 0; w < dim; w++)
        {
            for (int j = 0; j < dim; j++)
            {
                if (costMatrix[w][j] < min[j])
                {
                    min[j] = costMatrix[w][j];
                }
            }
        }
        for (int w = 0; w < dim; w++)
        {
            for (int j = 0; j < dim; j++)
            {
                costMatrix[w][j] -= min[j];
            }
        }
    }
 
    protected void updateLabeling(double slack)
    {
        for (int w = 0; w < dim; w++)
        {
            if (committedWorkers[w])
            {
                labelByWorker[w] += slack;
            }
        }
        for (int j = 0; j < dim; j++)
        {
            if (parentWorkerByCommittedJob[j] != -1)
            {
                labelByJob[j] -= slack;
            }
            else
            {
                minSlackValueByJob[j] -= slack;
            }
        }
    }
 
    public static void main(String[] args)
    {
        double[][] cost = null;
    	File myObj = new File("input/input3_30.txt");
    	Scanner myReader = new Scanner(myObj);
    	FileWriter myWriter = new FileWriter("output/output3_raf1.txt");
    	
    	for(int n=0; n<5; n++)
    	{    		
	        //Scanner myReader = new Scanner(myObj);
	        int r =myReader.nextInt();
	        int c =myReader.nextInt();
	        System.out.println(r);
	        System.out.println(c);
	        cost = null;
	        cost = new double[r][c];
	        for (int i = 0; i < r; i++)
	        {
	            for (int j = 0; j < c; j++)
	            {
	                cost[i][j] = myReader.nextDouble();
	            }
	        }
	        
	        HungarianBipartiteMatching hbm = new HungarianBipartiteMatching(cost);
	        long start = System.nanoTime();
	        int[] result = hbm.execute();
	        long finish = System.nanoTime();
	        long time = finish - start;
	        double sum=0;
	        for(int i=0;i<c;i++)
	        	sum+=cost[i][result[i]];
	        
	        myWriter.write(String. format("%.4f", sum) + "\t" + time+"\t");
	        for(int i=0;i<c;i++)
	        	myWriter.write(result[i] + " ");
	        myWriter.write("\n");
	        
//	        System.out.println("Matching: " + Arrays.toString(result));
//	        System.out.println("Total cost: " + sum + time);
    	}
    	myReader.close();
    	myWriter.close();
    }
}
