import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Inicial implements Runnable
{
    private Socket cs;
    private Server s;
    private boolean jogar;
    private Pessoa p;
    private Heros h;
    private ChatLog chat;
    private boolean ready;
    private Outcome out;
    private Thread t;

    public Inicial(Socket cs, Server s, Outcome out, Thread t){
        this.cs = cs;
        this.s = s;
        this.jogar = false;
        this.ready = false;
        this.p = null;
        this.h = null;
        this.chat = null;
        this.t = t;
        this.out = out;
    }

    public Outcome getOut(){
      return this.out;
    }

    public void setHeros(Heros h){
      this.h = h;
    }

    public void setReady(){
      this.ready=true;
    }

    public void jogoRealizado(ArrayList<Pessoa> e1, ArrayList<Pessoa> e2, HashMap<Pessoa,String> m1, HashMap<Pessoa,String> m2){
      try{
        PrintWriter out = new PrintWriter(cs.getOutputStream(), true);

        out.println("Jogo começou!");
        for(Pessoa a: e1){
          out.println(a.getUsername() + " - " + m1.get(a));
        }
        for(Pessoa p: e2){
          out.println(p.getUsername() + " - " + m2.get(p));
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    public void setChat(ChatLog l){
      this.chat = l;
    }

    public void jogoNRealizado(){
      try{
        BufferedReader in = new BufferedReader(new InputStreamReader( this.cs.getInputStream()));
        PrintWriter out = new PrintWriter(cs.getOutputStream(), true);

        out.println("Jogo não irá ser realizado pois alguém não escolheu um heroi!");
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    public void run(){
        try{
        BufferedReader in = new BufferedReader(new InputStreamReader( this.cs.getInputStream()));
        PrintWriter out = new PrintWriter(cs.getOutputStream(), true);

        boolean flag1 = false;
        String current = null;
        while (!flag1){
            out.println("Quer fazer login ou registar-se ? Escreva Login ou Registar");
            current = in.readLine();
            if(current.equals("Registar")){
                String user, pass;
                out.println("Escreva o username");
                user = in.readLine();
                out.println("Escreva o seu email");
                String mail = in.readLine();
                out.println("Escreva a password");
                pass = in.readLine();
                int i = s.Registado(user, pass);
                if(i==1){
                    out.println("ja esta registado");
                }
                if(i==2){
                    out.println("username a ser usado");
                }
                if(i==3){
                    Pessoa nova = new Pessoa(user, mail, pass, this.cs, this);
                    this.p= nova;
                    this.s.addRegisto(nova);
                    this.s.addAutenticados(nova);
                    out.println("Registado e autenticado!");
                    flag1 = true;
                }
            }
            if(current.equals("Login")){
                out.println("Escreva o username");
                String user = in.readLine();
                out.println("Escreva a password");
                String pass = in.readLine();
                int i = s.Registado(user, pass);
                if(i==1){
                    this.p=s.getUser(user);
                    boolean flag = s.addAutenticados(p);
                    if(flag){
                        out.println("autenticado!");
                        this.p.setSocket(this.cs);
                        this.p.setInicial(this);
                        flag1=true;
                    } else{
                        out.println("conta a ser usada");
                        break;
                    }
                }
                if(i==2){
                    out.println("password errada!");
                }
                if(i==3){
                    out.println("username desconhecido");
                }
            }
        }
        if(flag1){
            while(current!=null){
                out.println("Quer jogar ou sair? Escreva jogar ou sair");
                current=in.readLine();
                if(current.equals("jogar")){
                    out.println("procurando...");
                    this.jogar=true;
                    this.ready=false;
                    this.p.setInicial(this);
                    this.s.addProcura(this.p);
                    while(!this.ready){
                      synchronized(this){
                        wait();
                      }
                    }
                    Escolha e = new Escolha(this.p, this.h, this.chat);
                    e.run();
                    while(this.jogar!=false){
                      synchronized(this){
                        wait();
                      }
                    }
                    e=null;
                }
                if(current.equals("sair")){
                    this.s.removeAut(this.p);
                    break;
                }
            }
        }
        this.out.setCancel();
        synchronized(this.out){
          this.out.notify();
        }
        this.t.interrupt();
        this.cs.close();
        Thread.currentThread().interrupt();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean getStatus(){
        return this.jogar;
    }

    public void setStatus(){
        this.jogar=false;
    }
}