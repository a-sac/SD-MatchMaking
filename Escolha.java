import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;


public class Escolha implements Runnable
{
  private Socket cs;
  private Heros h;
  private Pessoa p;
  private ChatLog ch;
  private PrintWriter out;
  private BufferedReader in;

  public Escolha(Pessoa p, Heros h, ChatLog ch){
    this.cs = p.getSocket();
    this.p = p;
    this.h=h;
    this.ch=ch;
    try{
      this.out = new PrintWriter(this.cs.getOutputStream(), true);
      this.in = new BufferedReader(new InputStreamReader( this.cs.getInputStream()));
    }catch(IOException e){
      e.printStackTrace();
    }
  }

  public synchronized void run(){
    try{
      boolean flag = false;
      String current;
      out.println("Qual é o Hero que deseja ?");
      long startTime = System.currentTimeMillis(); //fetch starting time
      while(((current = in.readLine())!=null) && System.currentTimeMillis()<startTime+30000){
          this.ch.add(p.getUsername() + ": "+ current);
          flag = HeroPic(current);
          if(!flag){
            out.println("Hero indisponivel");
          } else{
            out.println("Hero escolhido com sucesso");
          }
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public boolean HeroPic(String hero){
    return this.h.pic(this.p, hero);
  }
}