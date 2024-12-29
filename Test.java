import java.util.Random;

public class Test {


    // Okuma-yazma kilidi
    private static ReadWriteLock rwLock = new ReadWriteLock();

    public static void main(String[] args) {

        // Okuyucular ve Yazarlar için Thread dizileri
        Thread[] readers = new Thread[5];
        Thread[] writers = new Thread[5];

        for (int i = 0; i < 5; i++) {
            readers[i] = new Thread(new Reader("Reader-" + (i + 1)));
            writers[i] = new Thread(new Writer("Writer-" + (i + 1)));
        }

        // Tüm okuyucu ve yazar thread'lerini rastgele sırayla başlat
        Random random = new Random();
        Thread[] allThreads = new Thread[10];
        System.arraycopy(readers, 0, allThreads, 0, 5);
        System.arraycopy(writers, 0, allThreads, 5, 5);

        // Thread'leri karıştır
        for (int i = 0; i < allThreads.length; i++) {
            int swapIndex = random.nextInt(allThreads.length);
            Thread temp = allThreads[i];
            allThreads[i] = allThreads[swapIndex];
            allThreads[swapIndex] = temp;
        }

        // Thread'leri başlat
        for (Thread thread : allThreads) {
            thread.start();
        }
    }

    /**
     * Reader (Okuyucu) Thread:
     * - Sık sık readLock() alıp veriyi okur, sonra readUnlock() çağırır.
     */
    static class Reader implements Runnable {
        private String name;

        public Reader(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                rwLock.readLock();  // Okumaya başla
                try {
                    System.out.println(name + " is reading.");
                    Thread.sleep(1000); // Okuma işlemi (simülasyon)
                    System.out.println(name + " finished reading.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    rwLock.readUnlock();  // Okumayı bitir
                }

                // Okumalar arasında biraz bekleyelim
                try {
                    Thread.sleep(new Random().nextInt(2000) + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Writer (Yazar) Thread:
     * - Sık sık writeLock() alıp veriyi yazar, sonra writeUnlock() çağırır.
     */
    static class Writer implements Runnable {
        private String name;

        public Writer(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                rwLock.writeLock();  // Yazmaya başla
                try {
                    System.out.println(name + " is writing...");
                    Thread.sleep(500); // Yazma işlemi (simülasyon)
                    System.out.println(name + " finished writing.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    rwLock.writeUnlock(); // Yazmayı bitir
                }

                // Yazmalar arasında biraz bekleyelim
                try {
                    Thread.sleep(new Random().nextInt(2000) + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
