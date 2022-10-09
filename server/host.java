import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.Path;

public class host {
    static String path = "C:/Users/tar/Desktop/test/server";

    public static void main(String[] args) throws IOException {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress("localhost", 8083));
        while (true) {
            System.out.println(" ---  Wait client  ---");
            SocketChannel socketChannel = ssc.accept();
            System.out.println("connected. client channel: " + socketChannel);
            Thread thread_Server = new Thread(new hostRequest(socketChannel));
            thread_Server.start();

        }
    }

    static class hostRequest implements Runnable {
        DataInputStream dis;
        DataOutputStream dos;
        static File[] fList;
        SocketChannel socketChannel;
        String Username;

        public hostRequest(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        public void run() {
            try {
                dis = new DataInputStream(socketChannel.socket().getInputStream());
                dos = new DataOutputStream(socketChannel.socket().getOutputStream());
                File directory = new File("D:/CODE/test/server"); // set path server
                fList = directory.listFiles();
                Username = dis.readUTF();
                System.out.println(Username + " running ");
                String[] book = new String[1000];
                int n = 0;
                for (File file : fList) {
                    book[n] = file.getName();
                    n++;
                }
                dos.writeInt(n);
                for (int j = 0; j < n; j++) {
                    dos.writeUTF(book[j]);
                }
                dos.flush();
                String str;
                while (true) {
                    int Index = dis.readInt();
                    int zero_copy = dis.readInt();
                    str = zero_copy == 1 ? "Send File by Zero_copy" : "Send File by Basic_copy";
                    System.out.println(str);
                    if (Index <= 0) {
                        break;
                    }
                    Index -= 1;
                    if (zero_copy == 1) {
                        sendFile_ZeroCopy(Index);
                    } else {
                        sendFile(Index);
                    }
                 
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        }

        private void sendFile_ZeroCopy(int index) throws IOException {
            System.out.println("----ready to send----");
            FileChannel fileChannel = null;
            try {
                long start = System.currentTimeMillis();
                File Fp = new File(fList[index].getPath());
                String Fname = Fp.getName();
                dos.writeUTF(Fname);
                dos.writeLong(Fp.length());
                FileInputStream filein = new FileInputStream(Fp);
                fileChannel = filein.getChannel();
                long totalBytesTransferred = 0;
                int i=0;
                while (totalBytesTransferred < Fp.length()) {
                    System.out.println(i++);
                    long bytesTransferred = fileChannel.transferTo(totalBytesTransferred,
                            Fp.length() - totalBytesTransferred, socketChannel);
                    totalBytesTransferred += bytesTransferred;
                }
                System.out.println("test");
                long end = System.currentTimeMillis();
                long time = end - start;
                dos.writeLong(time);
            } catch (Exception e) {
                e.printStackTrace();
            }   

            finally {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            }

        }

        private void sendFile(int index) throws IOException {
            System.out.println("----ready to send----");
            try {
                long start = System.currentTimeMillis();
                File Fp = new File(fList[index].getPath());
                System.out.println("Path File in server : " + Fp.getPath());
                String Fname = Fp.getName();
                FileInputStream filein = new FileInputStream(Fp);
                byte BF[] = new byte[(int) Fp.length()];
                filein.read(BF);
                dos.writeUTF(Fname);
                dos.writeInt(BF.length);
                dos.write(BF,0, BF.length);
                dos.flush();
                System.out.println("Send to File " + Fname + " " + Username + " ");
                long end = System.currentTimeMillis();
                long time = end - start;
                dos.writeLong(time);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("File not found");
            }

        }

    }

}