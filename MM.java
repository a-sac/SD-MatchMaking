import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class MM implements Runnable
{
  private Server s;

  public MM(Server s){
    this.s = s;
  }

  public void run(){
    s.Procura();
  }
}