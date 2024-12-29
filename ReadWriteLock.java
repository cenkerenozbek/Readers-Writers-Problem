import java.util.concurrent.Semaphore;

public class ReadWriteLock {

    // Semaphore to block new readers if a writer is waiting
    private Semaphore turnstile = new Semaphore(1);

    // Semaphore to protect access to reader count
    private Semaphore readerMutex = new Semaphore(1);

    // Semaphore to ensure only one writer at a time
    private Semaphore writerMutex = new Semaphore(1);

    // Number of readers currently reading
    private int readerCount = 0;

    // Number of readers who have read the current data
    private int readersReadCurrentData = 0;

    // Total number of readers to read the current data
    private int totalReaders = 0;

    // Semaphore to protect access to readersReadCurrentData and totalReaders
    private Semaphore dataReadMutex = new Semaphore(1);

    public void readLock() {
        try {
            // Allow readers to proceed if no writer is waiting
            turnstile.acquire();
            turnstile.release();

            // Increment reader count and total readers for current data
            readerMutex.acquire();
            readerCount++;
            totalReaders++;
            if (readerCount == 1) {
                writerMutex.acquire();
            }
            readerMutex.release();

            // Increment the number of readers who have read the current data
            dataReadMutex.acquire();
            readersReadCurrentData++;
            dataReadMutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void readUnlock() {
        try {
            // Decrement reader count
            readerMutex.acquire();
            readerCount--;
            if (readerCount == 0) {
                writerMutex.release();
            }
            readerMutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeLock() {
        try {
            // Block new readers from proceeding
            turnstile.acquire();
            writerMutex.acquire();

            // Wait for all readers to finish reading the current data
            dataReadMutex.acquire();
            while (readersReadCurrentData < totalReaders) {
                dataReadMutex.release();
                Thread.sleep(50); // Wait for readers to finish
                dataReadMutex.acquire();
            }
            // Reset counters for the next write operation
            readersReadCurrentData = 0;
            totalReaders = 0;
            dataReadMutex.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeUnlock() {
        writerMutex.release();
        turnstile.release();
    }
}