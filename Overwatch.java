import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Overwatch
{
    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(9999);
        Socket cs;
        Server s = new Server();

        Thread mm = new Thread(new MM(s));
        mm.start();

        try {
          while (true)
          {
              cs = ss.accept();
                                      
              Thread ini = new Thread( new Inicial(cs,s) );
              ini.start();
          }
        } catch(Exception e){
          e.printStackTrace();
        }
        //ss.close();
    }
}