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
  private Jogo j;

  MatchMaking(ArrayList<Pessoa> jog, Jogo j){
    this.jogadores=jog;
    this.escolhas = new HashMap<Pessoa,String>();
    this.j=j;
  }

  public synchronized void run(){
    try{
      Heros h = new Heros();
      ChatLog ch = new ChatLog();
      for(Pessoa a: jogadores){
        a.getInicial().setChat(ch);
        a.getInicial().setHeros(h);
        a.getInicial().getOut().setChat(ch);
        a.getInicial().setReady();
        synchronized(a.getInicial().getOut()){
          a.getInicial().getOut().notifyAll();
        }
        synchronized(a.getInicial()){
          a.getInicial().notifyAll();
        }
      }
      Thread.currentThread().sleep(30000);
      for(Pessoa a: jogadores){
        a.getInicial().getOut().notifyChat();
      }
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