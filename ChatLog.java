import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class ChatLog
{
   Vector<String> log = new Vector<String>(); // We do sync but Vector is thread safe

  public synchronized void add(String s)
  {
    log.add(s);
    //System.out.println("get "+log.elementAt(log.size()-1) );

    notifyAll();
  }

  public void writeloop(PrintWriter pw) 
  { 
    int i=0; 
    String s;
    try 
    {
      long startTime = System.currentTimeMillis();
      while(System.currentTimeMillis()<(startTime+30000)){
        synchronized (this)
        { 
          while (i >= log.size() && (System.currentTimeMillis()<startTime+30000)) wait();
          if(!(System.currentTimeMillis()<startTime+30000)) break;
          s=log.elementAt(i);
        }

        pw.println(s); 
        i++; 
      }
    } catch (InterruptedException e) {} 
//    System.out.println("Write Loop Ended");
  } 
}