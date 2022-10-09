
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Scanner;

public class user {
    SocketChannel socketChannel;
    public static void main(String[] args) throws IOException, InterruptedException {
        new user();
    }
    user(){
        run();
    }
    void run(){
        try {
            SocketAddress socketAddress = new InetSocketAddress("localhost", 8083);
              socketChannel = SocketChannel.open();
            socketChannel.connect(socketAddress);
                Scanner scan = new Scanner(System.in);
    
                System.out.print("Port  Connect : ");
                int port = scan.nextInt();
                System.out.println("User name ");
                String name = scan.next();
                System.out.println("-----Connecting to Server-----");
                DataOutputStream dos = new DataOutputStream(socketChannel.socket().getOutputStream());
                DataInputStream dis  = new DataInputStream(socketChannel.socket().getInputStream());
                dos.writeUTF(name);
                int n = dis.readInt();
                System.out.println(n+" FIle in server");
                for (int j = 0; j < n; j++) {
                    String x = dis.readUTF();
                    System.out.println("Select : "+(j + 1) + " |" + x + "|");
                }
                System.out.println();
                while (true) {
                    
                    try {
                        System.out.print("Select INDEX  : ");
                        int Index = scan.nextInt();
                        if(Index<=0||Index>n)
                        {
                            System.exit(1);
                        }
                        System.out.print("chose : 1 for zero_copy , 0 for copy :");
                        int zero_copy = scan.nextInt();
                        dos.writeInt(Index);
                        dos.writeInt(zero_copy);
                        dos.flush();
                      
                        if(zero_copy==1){
                            read_zero();
                        }else{
                            read();
                        }
                    } catch (Exception e) {
                        System.out.println("input error - " + n);
                        System.exit(1);
                    }
                    Long time = dis.readLong();
                System.out.println("TIME : " + time + " Millisecond");
    
                }
            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }

    }

    private void read_zero() throws IOException{
        String filename;
        FileChannel fileChannel =null;
        try {
            DataInputStream datain = new DataInputStream(socketChannel.socket().getInputStream());
            filename = datain.readUTF();
            long length = datain.readLong();
            System.out.println(filename + "\n---on Server---");
            File dowload = new File(filename);
            dowload.delete();
            FileOutputStream fos  = new FileOutputStream(dowload);
            fileChannel = fos.getChannel();
            long totalBytesTransferFrom = 0;
            while (totalBytesTransferFrom < length) {
                long transferFromByteCount = fileChannel.transferFrom(socketChannel, totalBytesTransferFrom, length-totalBytesTransferFrom);
                if (transferFromByteCount <= 0){
                    break;
                }
                totalBytesTransferFrom += transferFromByteCount;
            }

        } catch (Exception e) {
            // TODO: handle exception   
        }
        finally{
            if(fileChannel!=null){fileChannel.close();}
    }

    }
    public  void read() {

        String  filename;
        try {
            DataInputStream datain = new DataInputStream(socketChannel.socket().getInputStream());
                    filename = datain.readUTF();
                    System.out.println(filename + "\n---on Server---");
                    int len = datain.readInt();
                    if (len > 0) {
                        File dowload = new File(filename);
                        dowload.delete();
                        int byteread=0;
                        int l = len;
                        byte BF[] = new byte[1024];
                        FileOutputStream Fos = new FileOutputStream(dowload);
                        try {
                            while(l>0  && byteread!=-1){
                                byteread =  datain.read(BF, 0, Math.min(BF.length, l));
                                Fos.write(BF);
                                l-=byteread ;
                            }
                           
                            
                            Fos.close();
                            
                        } catch (Exception e) {
                            System.out.println("OUT");
                            System.exit(1);
                        }
                    }
                
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }







}