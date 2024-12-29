import java.util.concurrent.Semaphore;

public class ReadWriteLock {

    // -----------------------------------------------------
    // Semaphores and counters
    // -----------------------------------------------------
    
    // turnstile: Blocks new readers if a writer is waiting.
    private Semaphore turnstile = new Semaphore(1);

    // Protects access to readerCount.
    private Semaphore readerMutex = new Semaphore(1);

    // Allows only one writer at a time.
    private Semaphore writerMutex = new Semaphore(1);

    // Number of readers currently reading.
    private int readerCount = 0;
    
    // Number of readers that have read the current data.
    private int readersReadCurrentData = 0;

    // Total readers that need to read the current data.
    private int totalReaders = 0;

    // Protects access to readersReadCurrentData and totalReaders.
    private Semaphore dataReadMutex = new Semaphore(1);


    /**
     * readLock():
     *  1) If a writer is waiting, new readers are blocked (turnstile).
     *  2) If this is the first reader, it acquires writerMutex to block writers.
     *  3) Increments totalReaders for the current data cycle.
     */
    public void readLock() {
        try {
            // Block if a writer is waiting.
            turnstile.acquire();
            turnstile.release();

            // Safely increase readerCount.
            readerMutex.acquire();
            readerCount++;
            if (readerCount == 1) {
                // First reader blocks writer
                writerMutex.acquire();
            }
            readerMutex.release();

            // Increase the number of total readers for this cycle.
            dataReadMutex.acquire();
            totalReaders++;
            dataReadMutex.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * readUnlock():
     *  1) Once a reader finishes, it increments the count of readers who have read this data.
     *  2) If this is the last reader, it releases writerMutex.
     */
    public void readUnlock() {
        try {
            // This reader finished reading the current data.
            dataReadMutex.acquire();
            readersReadCurrentData++;
            dataReadMutex.release();

            // Safely decrease readerCount.
            readerMutex.acquire();
            readerCount--;
            if (readerCount == 0) {
                // Last reader releases the writer
                writerMutex.release();
            }
            readerMutex.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * writeLock():
     *  1) Acquire the turnstile so no new readers can enter.
     *  2) Acquire writerMutex so that no other writer can proceed.
     *  3) Wait until all readers have finished reading the previous data 
     *     (readersReadCurrentData < totalReaders).
     *  4) Reset counters for the new data cycle.
     */
    public void writeLock() {
        try {
            // Writer locks the turnstile so new readers are blocked.
            turnstile.acquire();
            // Prevent other writers from writing simultaneously.
            writerMutex.acquire();

            // Wait for all readers to finish reading old data.
            dataReadMutex.acquire();
            while (readersReadCurrentData < totalReaders) {
                dataReadMutex.release();
                Thread.sleep(50); // Give readers time to finish
                dataReadMutex.acquire();
            }

            // Reset counters for the new write cycle.
            readersReadCurrentData = 0;
            totalReaders = 0;
            dataReadMutex.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * writeUnlock():
     *  1) Release writerMutex and turnstile, letting readers enter again.
     */
    public void writeUnlock() {
        writerMutex.release();   
        turnstile.release();     
    }
}
