import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Jogo implements Runnable
{
  private ArrayList<Pessoa> jogadores;
  private ArrayList<Pessoa> equipa1;
  private ArrayList<Pessoa> equipa2;
  private Server s;
  private int jog1;
  private int jog2;
  private int acabou;

  public Jogo(Server s, ArrayList<Pessoa> jogadores, int jog1, int jog2){
    this.jogadores = new ArrayList<Pessoa>();
    this.equipa1 = new ArrayList<Pessoa>();
    this.equipa2 = new ArrayList<Pessoa>();
    this.s = s;
    this.jogadores=jogadores;
    this.acabou=0;
    this.jog1=jog1;
    this.jog2=jog2;
    System.out.println("formar equipas");
  }

  public void incAcabou(){
    this.acabou++;
  }

  public void run(){
    int n,m;
    n = jog1/2;
    m= jog2/2;
    for(int i = 0; i<n; i++){
        equipa1.add(jogadores.get(i));
    }
    for(int i = jog1; i<jog1+m; i++){
        equipa1.add(jogadores.get(i));
    }
    for(int i = n; i<jog1; i++){
        equipa2.add(jogadores.get(i));
    }
    for(int i = jog1+m; i<10; i++){
        equipa2.add(jogadores.get(i));
    }
    System.out.println("mm starting");
    MatchMaking m1 = new MatchMaking(equipa1, this);
    MatchMaking m2 = new MatchMaking(equipa2, this);
    Thread e1 = new Thread(m1);
    Thread e2 = new Thread(m2);
    e1.start();
    e2.start();
    try{
      while(this.acabou<2){
        synchronized(this){
          wait();
        } 
      }
    if((m1.getEscolhas().size()==5) && (m2.getEscolhas().size()==5)){
        for(Pessoa a: jogadores){
          a.getInicial().jogoRealizado(equipa1, equipa2,m1.getEscolhas(),m2.getEscolhas());
        }
        System.out.println("começou o jogo");
        TimeUnit.SECONDS.sleep(5);
        System.out.println("acabou o jogo");
        int randomNum = ThreadLocalRandom.current().nextInt(1, 2 + 1);
        if(randomNum==1){
            for(Pessoa p: equipa1){
                if(p.getRate()!=9){
                    p.setRate(p.getRate()+1);
                }
            }
            for(Pessoa p: equipa2){
                if(p.getRate()!=0){
                    p.setRate(p.getRate()-1);
                }
            }
        }else{
                for(Pessoa p: equipa2){
                    if(p.getRate()!=9){
                        p.setRate(p.getRate()+1);
                    }
                }
                for(Pessoa p: equipa1){
                    if(p.getRate()!=0){
                        p.setRate(p.getRate()-1);
                    }
                }
        }
        for(int i=0; i<jogadores.size(); i++){
            jogadores.get(i).getInicial().setStatus();
            synchronized(jogadores.get(i).getInicial()){
                jogadores.get(i).getInicial().notifyAll();
            }
        }
    }
    else{
      for(Pessoa a: jogadores){
        a.getInicial().jogoNRealizado();
      }
      System.out.println("jogo cancelado");
      for(int i=0; i<jogadores.size(); i++){
        jogadores.get(i).getInicial().setStatus();
        System.out.println(jogadores.get(i).getInicial().getStatus());
        synchronized(jogadores.get(i).getInicial()){
            jogadores.get(i).getInicial().notifyAll();
        }
    }
    }   
    } catch (InterruptedException e){
      e.printStackTrace();
    }
  }
}
