import java.util.Random;

public class Test {

    // Read-Write Lock
    private static ReadWriteLock rwLock = new ReadWriteLock();

    public static void main(String[] args) {

        // Arrays for 5 reader threads and 5 writer threads.
        Thread[] readers = new Thread[5];
        Thread[] writers = new Thread[5];

        for (int i = 0; i < 5; i++) {
            readers[i] = new Thread(new Reader("Reader-" + (i + 1)));
            writers[i] = new Thread(new Writer("Writer-" + (i + 1)));
        }

        // Combine all threads into one array.
        Thread[] allThreads = new Thread[10];
        System.arraycopy(readers, 0, allThreads, 0, 5);
        System.arraycopy(writers, 0, allThreads, 5, 5);

        // Randomly shuffle the threads.
        Random random = new Random();
        for (int i = 0; i < allThreads.length; i++) {
            int swapIndex = random.nextInt(allThreads.length);
            Thread temp = allThreads[i];
            allThreads[i] = allThreads[swapIndex];
            allThreads[swapIndex] = temp;
        }

        // Start all threads
        for (Thread thread : allThreads) {
            thread.start();
        }
    }

    /**
     * Reader Thread:
     * Reads between readLock() / readUnlock().
     */
    static class Reader implements Runnable {
        private String name;

        public Reader(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                rwLock.readLock();  // Acquire read lock
                try {
                    System.out.println(name + " is reading.");
                    Thread.sleep(1000); // Simulate reading
                    System.out.println(name + " finished reading.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    rwLock.readUnlock();  // Release read lock
                }

                // Pause for a random interval between reads
                try {
                    Thread.sleep(new Random().nextInt(2000) + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Writer Thread:
     * Writes between writeLock() / writeUnlock().
     */
    static class Writer implements Runnable {
        private String name;

        public Writer(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                rwLock.writeLock();  // Acquire write lock
                try {
                    System.out.println(name + " is writing...");
                    Thread.sleep(500); // Simulate writing
                    System.out.println(name + " finished writing.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    rwLock.writeUnlock(); // Release write lock
                }

                // Pause for a random interval between writes
                try {
                    Thread.sleep(new Random().nextInt(2000) + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
