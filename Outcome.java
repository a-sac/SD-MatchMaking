import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Outcome implements Runnable
{
  private Socket cs;
  private ChatLog l;
  private boolean cancel;
    
  Outcome (Socket cs) 
  {
    this.cs = cs; 
    this.l=null;
    this.cancel =false;
  }

  public void setChat(ChatLog l){
    this.l=l;
  }
  
  public void setCancel(){
    this.cancel=true;
  }

  public void notifyChat(){
    synchronized(l){
     l.notifyAll();
    }
    this.l=null;
  }

  public void run() 
  {
    try 
    {
      PrintWriter out = new PrintWriter( cs.getOutputStream(), true );
	    
      while(true){
        synchronized(this){
          while(l==null) {
            if(cancel) break;
            wait();
          }
        }
        if(cancel) break;
        l.writeloop(out);
      }
	        
    } catch (Exception e) { e.printStackTrace(); }
  }
}