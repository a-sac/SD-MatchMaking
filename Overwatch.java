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

        try {
          while (true)
          {
              cs = ss.accept();
              Outcome c = new Outcome(cs);
              Thread out = new Thread(c);
              out.start();
              Thread ini = new Thread( new Inicial(cs,s,c,out) );
              ini.start();
          }
        } catch(Exception e){
          e.printStackTrace();
        }
        //ss.close();
    }
}