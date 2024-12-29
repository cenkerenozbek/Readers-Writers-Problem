import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Test {
    public static void main(String[] args) {
        ReadWriteLock lock = new ReadWriteLock();

        Runnable writer = () -> {
            boolean canWrite = lock.writeLockMethod();
            if (canWrite) {
                try {
                    lock.writeUnlockMethod();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable reader = () -> {
            boolean canRead = lock.readLock();
            if (canRead) {
                try {
                    lock.readUnlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };        

        // 4 readers
        Thread reader1 = new Thread(reader, "Reader 1");
        Thread reader2 = new Thread(reader, "Reader 2");
        Thread reader3 = new Thread(reader, "Reader 3");
        Thread reader4 = new Thread(reader, "Reader 4");

        // 2 writers
        Thread writer1 = new Thread(writer, "Writer 1");
        Thread writer2 = new Thread(writer, "Writer 2");

        // Combine all threads into a single list
        List<Thread> threads = Arrays.asList(
            reader1, reader2, reader3, reader4,
            writer1, writer2
        );

        // Shuffle the start order randomly
        Collections.shuffle(threads, new Random());

        // Start all threads
        for (Thread t : threads) {
            t.start();
            try {
                // Short delay between thread starts
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            //Check if readers read the data only once.
            Thread repeatReader1 = new Thread(reader, "Reader 1");
            Thread repeatReader2 = new Thread(reader, "Reader 2");
            Thread repeatReader3 = new Thread(reader, "Reader 3");
            Thread repeatReader4 = new Thread(reader, "Reader 4");
            
    
            repeatReader1.start();
            repeatReader2.start();
            repeatReader3.start();
            repeatReader4.start();

                try {
                for (Thread t : threads) {
                    t.join();
                }
                repeatReader1.join();
                repeatReader2.join();
                repeatReader3.join();
                repeatReader4.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
