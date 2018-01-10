import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Server
{
  private Map<Integer, ArrayList<Pessoa>> procurando;
  private Map<String,Pessoa> registos;
  private Map<String,Pessoa> autenticados;
  final Lock lock = new ReentrantLock();

  public Server(){
    this.procurando = new HashMap<Integer, ArrayList<Pessoa>>();
    this.registos = new HashMap<String,Pessoa>();
    this.autenticados = new HashMap<String,Pessoa>();
  }

  public void criarJogo(int i){
          ArrayList<Pessoa> jogadores = new ArrayList<>();
          if((this.procurando.containsKey(i-1)) && (this.procurando.containsKey(i)) && ((this.procurando.get(i-1).size() + this.procurando.get(i).size())>=10)){
                  for(int j = 0; j<this.procurando.get(i-1).size() && j<10; j++){
                    Pessoa p = this.procurando.get(i-1).get(j);
                    jogadores.add(p);
                  }
                  for(int j = 0; j<this.procurando.get(i-1).size() && j<10; j++){
                    Pessoa p = this.procurando.get(i-1).get(0);
                    this.procurando.get(i-1).remove(p);
                  }
                  int jog1 = jogadores.size();
                  int s = 10-jogadores.size();
                  for(int j=0; j<s; j++){
                      Pessoa p = this.procurando.get(i).get(j);
                      jogadores.add(p);
                  }
                  for(int j=0; j<s; j++){
                      Pessoa p = this.procurando.get(i).get(0);
                      this.procurando.get(i).remove(p);
                  }
                  System.out.println("criar jogo");
                  (new Thread(new Jogo(this, jogadores, jog1, s))).start();
          } 
          else{
              if(this.procurando.containsKey(i) && this.procurando.get(i).size()>=10){
                      for(int j = 0; j<10; j++){
                          Pessoa p = this.procurando.get(i).get(j);
                          jogadores.add(p);
                      }
                      for(int j = 0; j<10; j++){
                          Pessoa p = this.procurando.get(i).get(0);
                          this.procurando.get(i).remove(p);
                      }
                      System.out.println("criar jogo");
                      (new Thread(new Jogo(this, jogadores, 10, 0))).start();
                  } 
                  else{
                      if(this.procurando.containsKey(i) && this.procurando.containsKey(i+1) && (this.procurando.get(i).size() + this.procurando.get(i+1).size())>=10){
                              for(int j = 0; j<this.procurando.get(i).size() && j<10; j++){
                                Pessoa p = this.procurando.get(i).get(j);
                                jogadores.add(p);
                              }
                              for(int j = 0; j<this.procurando.get(i).size() && j<10; j++){
                                Pessoa p = this.procurando.get(i).get(0);
                                this.procurando.get(i).remove(p);
                              }
                              int jog1 = jogadores.size();
                              int s = 10-jogadores.size();
                              for(int j=0; j<s; j++){
                                  Pessoa p = this.procurando.get(i+1).get(j);
                                  jogadores.add(p);
                              }
                              for(int j=0; j<s; j++){
                                  Pessoa p = this.procurando.get(i+1).get(0);
                                  this.procurando.get(i+1).remove(p);
                              }
                              System.out.println("criar jogo");
                              (new Thread(new Jogo(this, jogadores, jog1, s))).start();
                          }
                      }
              }
  }

  public synchronized Map<String, Pessoa> getRegistos(){
    return this.registos;
  }

  public synchronized int Registado(String user, String pass){
    if(registos.containsKey(user)){
      if(registos.get(user).getPass().equals(pass)){
        return 1;
      }
      else {
        return 2;
      }
    }
    else{
      return 3;
    }
  }

  public synchronized boolean addAutenticados(Pessoa e){
    try{
      String nome = e.getUsername();
      if(autenticados.containsKey(nome)) return false;
      else{
        autenticados.put(nome, e);
        return true;
      }
    }catch(Exception ex){
      ex.printStackTrace();
      return false;
    }
  }

  public synchronized void addProcura(Pessoa ps){
    try {
      if (this.procurando.containsKey(ps.getRate())){
        this.procurando.get(ps.getRate()).add(ps);
      } else {
        ArrayList<Pessoa> rates = new ArrayList<Pessoa>();
        rates.add(ps);
        this.procurando.put(ps.getRate(), rates);
      }
      criarJogo(ps.getRate());
    }catch(Exception e){
        e.printStackTrace();
    }
  }

  public synchronized Map<Integer, ArrayList<Pessoa>> getProcura(){
    return this.procurando;
  }

  public synchronized void addRegisto(Pessoa nova){
    this.registos.put(nova.getUsername(), nova);
  }

  public synchronized void removeAut(Pessoa p){
    if(autenticados.containsKey(p.getUsername())){
      autenticados.remove(p.getUsername());
    }
  }

  public synchronized Pessoa getUser(String user){
    return this.registos.get(user);
  }
}