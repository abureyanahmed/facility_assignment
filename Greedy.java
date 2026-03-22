import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Greedy {

    private ArrayList<Facility> facilities;
    private ArrayList<Customer> customers;
    private double totalCost;
    private double[][] distances;
    private int N;
    private int C;
    long start;
    long finish;

    public Greedy(int N, int C) { // N --- no. of facility //C --- no. of customers
        this.facilities = new ArrayList<>(N);
        this.customers = new ArrayList<>(C);
        this.totalCost = 0;
        distances = new double[C][N];
        this.N = N;
        this.C = C;
    }

    public void setFacility(Facility f) {
        this.facilities.add(f);
    }

    public void setCustomer(Customer c) {
        this.customers.add(c);
    }

    public double getDistance(int i, int j) {
        return distances[i][j];
    }

    public void setDistance(int i, int j, double dist) {
        this.distances[i][j] = dist;
    }

    public Facility getFacility(int i) {
        return facilities.get(i);
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public int getN() {
        return N;
    }

    public int getC() {
        return C;
    }
    

    public double[][] getDistances() {
        return distances;
    }

    public double makeAssignment() {

        for (int i = 0; i < C; i++) {

            double min = 999999;
            int index = -1;

            for (int j = 0; j < N; j++) {
                if (facilities.get(j).getCapacity() > 0 && distances[i][j] < min) {
                    min = distances[i][j];
                    index = j;
                }
            }
            Customer c = new Customer(i + 1);
            c.setAssignedFacility(index + 1);
            customers.add(c);
            facilities.get(index).setCustomer(c);
            facilities.get(index).setCapacity(facilities.get(index).getCapacity() - 1);

            totalCost += min;
        }
        return totalCost;
    }

    public Facility makePartialAssignment(int i) {

        double distance = 999999;
        int index = -1;

        for (int j = 0; j < N; j++) {
            if (facilities.get(j).getCapacity() > 0 && distances[i][j] < distance) {
                distance = distances[i][j];
                index = j;
            }
        }
        

        return facilities.get(index);


    }

    public void showAssignmentByFacility() {
        for (int i = 0; i < N; i++) {
            System.out.println("Facility \t  Customer");
            System.out.println(facilities.get(i).getId() + " Cap: " + facilities.get(i).getCapacity() + "  \t\t  "
                    + facilities.get(i).getCustomers());
        }
    }

    public void showAssignmentByCustomer() {
        for (int i = 0; i < C; i++) {
            System.out.println("Customer  \t  Facility");
            System.out.println(customers.get(i).getId() + "  \t \t   " + customers.get(i).getAssignedFacility());
        }
    }

    public void output() throws IOException {
        start = System.nanoTime();
        this.makeAssignment();
        finish = System.nanoTime();
        long elapsedTime = finish - start;
        BufferedWriter bw = new BufferedWriter(
                new FileWriter(".\\Single Capacity Input Output\\output1_10U.txt", true));
        bw.write(String.format("%.4f", totalCost) + "\t" + elapsedTime + "\t");
        for (int i = 0; i < C; i++) {
            bw.write(customers.get(i).getAssignedFacility() + " ");
        }
        bw.write("\n");
        bw.close();
    }
    
}