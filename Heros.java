import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;


public class Heros
{
  private ArrayList<String> disp;
  private HashMap<Pessoa, String> escolhas;

  public Heros(){
    this.disp = new ArrayList<>();
    this.escolhas = new HashMap<>();
    this.disp.add("ash");
    this.disp.add("scout");
    this.disp.add("doomfist");
    this.disp.add("genji");
    this.disp.add("mcree");
    this.disp.add("parah");
    this.disp.add("reaper");
    this.disp.add("soldier");
    this.disp.add("sombra");
    this.disp.add("tracer");
    this.disp.add("bastion");
    this.disp.add("hanzo");
    this.disp.add("junkrat");
    this.disp.add("mei");
    this.disp.add("torbjorn");
    this.disp.add("widowmaker");
    this.disp.add("d.va");
    this.disp.add("orisa");
    this.disp.add("reinhardt");
    this.disp.add("roadhog");
    this.disp.add("winston");
    this.disp.add("zarya");
    this.disp.add("ana");
    this.disp.add("lucio");
    this.disp.add("mercy");
    this.disp.add("moira");
    this.disp.add("symmetra");
    this.disp.add("zenyatta");
    this.disp.add("pyro");
    this.disp.add("demoman");
  }

  public synchronized boolean pic(Pessoa p, String hero){
    boolean flag = false;
    for (String a : disp) {
      if(a.equals(hero)){
        if(escolhas.containsKey(p)){
          String antigo = escolhas.get(p);
          disp.add(antigo);
          disp.remove(a);
          escolhas.put(p, hero);
          flag = true;
          return flag;
        }
        else {
          disp.remove(a);
          escolhas.put(p, hero);
          flag = true;
          return flag;
        }
      }
    } 
    return flag;
  }

  public synchronized HashMap<Pessoa,String> getEscolhas(){
    return this.escolhas;
  }
}
