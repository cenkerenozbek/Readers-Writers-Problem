import java.util.*;
import java.util.concurrent.Semaphore;

public class ReadWriteLock {
    private int activeReaderCount = 0; // Number of active readers
    private final Semaphore readStatusMutex = new Semaphore(1); // Protects the set of readers that have read
    private final Semaphore writeLock = new Semaphore(1);    // Ensures exclusive write access
    private final Semaphore mutex = new Semaphore(1);        // Protects safe increment/decrement of readerCount

    // List of all possible reader numbers (example: 1..4)
    private final List<Integer> allReaders = Arrays.asList(1, 2, 3, 4);
    // Keeps track of which readers have read in the current round
    private final Set<Integer> currentReaders = new HashSet<>();

    /**
     * readLock():
     *  1) Check whether the reader has already read (using currentReaders set).
     *  2) If not read yet, add it to currentReaders.
     *  3) If this is the first active reader (activeReaderCount goes from 0 to 1),
     *     acquire writeLock to block writers.
     */
    public boolean readLock() {
        int readerNo = getReaderNo();
        if (readerNo == -1) {
            System.out.println("Invalid reader number.");
            return false;
        }
        try {
            // Protect currentReaders set
            readStatusMutex.acquire();
            if (currentReaders.contains(readerNo)) {
                // This reader has already read in this round
                System.out.println("Reader " + readerNo + " has already read. Read operation canceled.");
                readStatusMutex.release();
                return false;
            } else {
                // If this reader is new to this round, add it
                currentReaders.add(readerNo);
            }
            readStatusMutex.release();

            // Increase activeReaderCount; if it's the first reader, block writers
            mutex.acquire();
            activeReaderCount++;
            if (activeReaderCount == 1) {
                // First reader acquires writeLock to prevent writers
                writeLock.acquire();
            }
            mutex.release();

            System.out.println("Reader " + readerNo + " started reading.");
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * readUnlock():
     *  1) Simulate reading by sleeping.
     *  2) Decrement activeReaderCount; if it goes to 0, release writeLock (allowing writers).
     */
    public void readUnlock() {
        int readerNo = getReaderNo();
        if (readerNo == -1) {
            System.out.println("Invalid reader number.");
            return;
        }
        try {
            System.out.println("Reader " + readerNo + " read data.");
            Thread.sleep(400); // Simulate reading

            // Decrement activeReaderCount. If it hits 0, let writers proceed
            mutex.acquire();
            activeReaderCount--;
            if (activeReaderCount == 0) {
                writeLock.release();
            }
            mutex.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * writeLockMethod():
     *  1) Writer waits until ALL readers in the list (allReaders) have read the data.
     *  2) If some readers haven't read yet, keep waiting (Thread.sleep(...) in a loop).
     *  3) Acquire writeLock to ensure exclusive writing.
     *  4) Clear currentReaders for the next round.
     */
    public boolean writeLockMethod() {
        try {
            // Continuously check if all readers have read
            while (true) {
                readStatusMutex.acquire();
                if (currentReaders.size() < allReaders.size()) {
                    // Some readers haven't read yet
                    List<Integer> yetToRead = new ArrayList<>();
                    for (int no : allReaders) {
                        if (!currentReaders.contains(no)) {
                            yetToRead.add(no);
                        }
                    }
                    System.out.println(Thread.currentThread().getName()
                            + " is waiting. Readers not finished yet: " + yetToRead);
                    readStatusMutex.release();

                    // Wait briefly and retry
                    Thread.sleep(200);
                } else {
                    // All readers have read in this round
                    readStatusMutex.release();
                    break;
                }
            }

            // Acquire the writeLock (exclusive access for writers)
            writeLock.acquire();

            // Prepare for the next round of data: clear the set of readers
            readStatusMutex.acquire();
            currentReaders.clear();
            readStatusMutex.release();

            System.out.println(Thread.currentThread().getName() + " started writing.");
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * writeUnlockMethod():
     *  - Simulate the writing process (Thread.sleep).
     *  - Release writeLock to allow other threads to proceed.
     */
    public void writeUnlockMethod() {
        try {
            Thread.sleep(500);
            System.out.println(Thread.currentThread().getName() + " finished writing.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writeLock.release();
    }

    // Helper method: extract reader number from the thread name (assuming "Reader <number>")
    private int getReaderNo() {
        String name = Thread.currentThread().getName();
        if (name.startsWith("Reader ")) {
            try {
                return Integer.parseInt(name.substring(7));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return -1;
    }
}
