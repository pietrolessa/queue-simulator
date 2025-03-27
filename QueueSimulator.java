import java.util.*;

public class QueueSimulator {

    static final int MAX_QUEUE = 5;
    static final int ARRIVAL = 0;
    static final int DEPARTURE = 1;

    static long a = 17, c = 43, M = 10000, seed = 1234;
    static long previous = seed;

    static double currentTime = 0.0;
    static int count = 100000;
    static int queueSize = 0;
    static int servers = 1; // Change to 2 for G/G/2/5
    static int busyServers = 0;
    static int lostClients = 0;

    static double nextArrival = 2.0;
    static double[] nextDeparture;
    static double[] stateTimes = new double[MAX_QUEUE + 1];
    static double lastEventTime = 0.0;

    public static double nextRandom() {
        previous = (a * previous + c) % M;
        return (double) previous / M;
    }

    public static double getArrivalTime() {
        return 2 + nextRandom() * (5 - 2);
    }

    public static double getServiceTime() {
        return 3 + nextRandom() * (5 - 3);
    }

    public static void updateStateTimes() {
        double delta = currentTime - lastEventTime;
        if (queueSize <= MAX_QUEUE) {
            stateTimes[queueSize] += delta;
        }
        lastEventTime = currentTime;
    }

    public static void scheduleNextArrival() {
        double interval = getArrivalTime();
        nextArrival = currentTime + interval;
    }

    public static void scheduleDeparture() {
        double serviceTime = getServiceTime();
        for (int i = 0; i < servers; i++) {
            if (nextDeparture[i] < 0) {
                nextDeparture[i] = currentTime + serviceTime;
                break;
            }
        }
    }

    public static int getNextDepartureIndex() {
        int index = -1;
        double minTime = Double.MAX_VALUE;
        for (int i = 0; i < servers; i++) {
            if (nextDeparture[i] >= 0 && nextDeparture[i] < minTime) {
                minTime = nextDeparture[i];
                index = i;
            }
        }
        return index;
    }

    public static void simulate() {
        nextDeparture = new double[servers];
        Arrays.fill(nextDeparture, -1.0);
        scheduleNextArrival();

        while (count > 0) {
            int event;
            double nextEventTime = nextArrival;
            event = ARRIVAL;

            int departureIndex = getNextDepartureIndex();
            if (departureIndex != -1 && nextDeparture[departureIndex] < nextEventTime) {
                nextEventTime = nextDeparture[departureIndex];
                event = DEPARTURE;
            }

            updateStateTimes();
            currentTime = nextEventTime;

            if (event == ARRIVAL) {
                scheduleNextArrival();
                count--;
                if (queueSize < MAX_QUEUE) {
                    queueSize++;
                    if (busyServers < servers) {
                        busyServers++;
                        scheduleDeparture();
                    }
                } else {
                    lostClients++;
                }
            } else {
                nextDeparture[departureIndex] = -1.0;
                queueSize--;
                if (queueSize >= servers) {
                    scheduleDeparture();
                } else {
                    busyServers--;
                }
            }
        }
    }

    public static void printResults() {
        System.out.println("\n--- Simulation Results ---");
        double totalTime = currentTime;
        for (int i = 0; i <= MAX_QUEUE; i++) {
            double percentage = (stateTimes[i] / totalTime) * 100;
            System.out.printf("State %d: %.2f (%.2f%%)%n", i, stateTimes[i], percentage);
        }
        System.out.println("Lost clients: " + lostClients);
        System.out.printf("Total simulation time: %.2f\n", totalTime);
    }

    public static void main(String[] args) {
        simulate();
        printResults();
    }
}
