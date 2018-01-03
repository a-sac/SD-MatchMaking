import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class MatchMaking implements Runnable
{
  private ArrayList<Pessoa> jogadores;
  private HashMap<Pessoa,String> escolhas;
  private ArrayList<Escolha> ins;
  private Jogo j;

  public MatchMaking(ArrayList<Pessoa> jog, Jogo j){
    this.jogadores=jog;
    this.escolhas = new HashMap<Pessoa,String>();
    this.ins = new ArrayList<Escolha>();
    this.j=j;
  }

  public void run(){
    try{
      Heros h = new Heros();
      ChatLog ch = new ChatLog();
      for(Pessoa a: jogadores){
        Escolha e = new Escolha(a, h, ch);
        this.ins.add(e);
        Thread t = (new Thread(e));
        t.start();
        Thread th = (new Thread(new Outcome(a.getSocket(), ch)));
        th.start();
      }
      Thread.currentThread().sleep(30000);
      for(Escolha e: this.ins){
          e.close();
      }
      ch = null;
      this.escolhas = h.getEscolhas();
      System.out.println("equipa parou");
      this.j.incAcabou();
      synchronized(this.j){
        this.j.notifyAll();
      }
      Thread.currentThread().interrupt();
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public synchronized HashMap<Pessoa,String> getEscolhas(){
    return this.escolhas;
  }
}