import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Outcome implements Runnable
{
  Socket cs;
  ChatLog l;
    
  Outcome (Socket cs, ChatLog l) 
  {
    this.cs = cs; this.l=l;
  }
    
  public void run() 
  {
    try 
    {
      PrintWriter out = new PrintWriter( cs.getOutputStream(), true );
	        
      l.writeloop(out);
	        
    } catch ( IOException e) { e.printStackTrace(); }
  }
}
