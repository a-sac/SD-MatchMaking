import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

class Heros
{
  private ArrayList<String> disp;
  private Map<Pessoa, String> escolhas;

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

  public synchronized Map<Pessoa,String> getEscolhas(){
    return this.escolhas;
  }
}

class Escolha implements Runnable
{
  private Socket cs;
  private Heros h;
  private Pessoa p;
  private PrintWriter out;
  private BufferedReader in;
  private MatchMaking m;

  public Escolha(Pessoa p, Heros h, MatchMaking m){
    this.cs = p.getSocket();
    this.p = p;
    this.h=h;
    this.m = m;
    try{
      this.out = new PrintWriter(this.cs.getOutputStream(), true);
      this.in = new BufferedReader(new InputStreamReader( this.cs.getInputStream()));
    }catch(IOException e){
      e.printStackTrace();
    }
  }

  public void run(){
    try{
      boolean flag = false;
      String current;
      out.println("Qual é o Hero que deseja ?");
      long startTime = System.currentTimeMillis(); //fetch starting time
      while(((current = in.readLine())!=null) && System.currentTimeMillis()<startTime+30000){
          out.println(System.currentTimeMillis()-startTime);
          flag = HeroPic(current);
          if(!flag){
            out.println("Hero indisponivel");
          } else{
            out.println("Hero escolhido com sucesso");
          }
      }
      System.out.println("escolhi");  
      this.m.incAcabou();
      synchronized(this.m){
        this.m.notifyAll();
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public boolean HeroPic(String hero){
    return this.h.pic(this.p, hero);
  }
}

class MatchMaking implements Runnable
{
  private ArrayList<Pessoa> jogadores;
  private Map<Pessoa,String> escolhas;
  private int acabou;
  private Jogo j;

  MatchMaking(ArrayList<Pessoa> jog, Jogo j){
    this.jogadores=jog;
    this.acabou = 0;
    this.j=j;
  }

  public void incAcabou(){
    this.acabou++;
  }

  public void run(){
    try{
      Heros h = new Heros();
      for(Pessoa a: jogadores){
        Escolha e = new Escolha(a, h,this);
        Thread t = (new Thread(e));
        t.start();
      }
      while(acabou<5){
        synchronized(this){
          wait();
        }
        System.out.println(this.acabou);
      }
      this.escolhas = h.getEscolhas();
      System.out.println("equipa parou");
      this.j.incAcabou();
      synchronized(this.j){
        this.j.notifyAll();
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public synchronized Map<Pessoa,String> getEscolhas(){
    return this.escolhas;
  }
}

class Jogo implements Runnable
{
  private ArrayList<Pessoa> jogadores;
  private ArrayList<Pessoa> equipa1;
  private ArrayList<Pessoa> equipa2;
  private Server s;
  private int acabou;

  public Jogo(Server s, ArrayList<Pessoa> jogadores){
    this.jogadores = new ArrayList<Pessoa>();
    this.equipa1 = new ArrayList<Pessoa>();
    this.equipa2 = new ArrayList<Pessoa>();
    this.s = s;
    this.jogadores=jogadores;
    this.acabou=0;
    System.out.println("formar equipas");
  }

  public void incAcabou(){
    this.acabou++;
  }

  public void run(){
    System.out.println("mm starting");
    System.out.println(this.jogadores);
    for(int i = 0; i<5; i++){
        equipa1.add(jogadores.get(i));
    }
    System.out.println(this.equipa1);
    for(int i = 5; i<10; i++){
        equipa2.add(jogadores.get(i));
    }
    System.out.println(this.equipa1);
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
        System.out.println(this.acabou);  
      }
    if((m1.getEscolhas().size()==5) && (m2.getEscolhas().size()==5)){
        System.out.println("começou o jogo");
        TimeUnit.SECONDS.sleep(5);
        System.out.println("acabou o jogo");
        int randomNum = ThreadLocalRandom.current().nextInt(1, 2 + 1);
        System.out.println(randomNum);
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
            System.out.println(jogadores.get(i).getInicial().getStatus());
            synchronized(jogadores.get(i).getInicial()){
                jogadores.get(i).getInicial().notifyAll();
            }
        }
    }
    else{
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

class Server
{
  private List<Pessoa> pessoas;
  private Map<Integer, ArrayList<Pessoa>> procurando;
  private Map<String,Pessoa> registos;
  private Map<String,Pessoa> autenticados;
  private int np;
  final Lock lock = new ReentrantLock();

  public Server(){
    this.pessoas = new ArrayList<Pessoa>();
    this.procurando = new HashMap<Integer, ArrayList<Pessoa>>();
    this.registos = new HashMap<String,Pessoa>();
    this.autenticados = new HashMap<String,Pessoa>();
    this.np=0;
  }

  public void Procura(){
    int i = 0;
    while(true){
      try{
        while (i==this.np){
          synchronized(this){
            wait();
          }
        }
        criarJogo();
        i++;
      } catch(Exception e){
        e.printStackTrace();
      }
    }
  }

  public void criarJogo(){
    lock.lock();
      try {
          ArrayList<Pessoa> jogadores = new ArrayList<>();
          for(int i=0; i<9; i++){
              if(this.procurando.containsKey(i)){
                  System.out.println(this.procurando.get(i).size());
                  if(this.procurando.get(i).size()>=10){
                      for(int j = 0; j<10; j++){
                          Pessoa p = this.procurando.get(i).get(j);
                          jogadores.add(p);
                      }
                      for(int j = 0; j<10; j++){
                          Pessoa p = this.procurando.get(i).get(0);
                          this.procurando.get(i).remove(p);
                      }
                      System.out.println(jogadores.size());
                      System.out.println("criar jogo");
                      (new Thread(new Jogo(this, jogadores))).start();
                  } else{
                      if(this.procurando.containsKey(i+1)){
                          if((this.procurando.get(i).size() + this.procurando.get(i+1).size())>=10){
                              for(int j = 0; j<this.procurando.get(i).size(); j++){
                                Pessoa p = this.procurando.get(i).get(j);
                                jogadores.add(p);
                              }
                              for(int j = 0; j<this.procurando.get(i).size(); j++){
                                Pessoa p = this.procurando.get(i).get(0);
                                this.procurando.get(i).remove(p);
                              }
                              int s = 10-jogadores.size();
                              for(int j=0; j<s; j++){
                                  Pessoa p = this.procurando.get(i+1).get(j);
                                  jogadores.add(p);
                              }
                              for(int j=0; j<s; j++){
                                  Pessoa p = this.procurando.get(i+1).get(0);
                                  this.procurando.get(i+1).remove(p);
                              }
                              System.out.println(jogadores.size());
                              System.out.println("criar jogo");
                              (new Thread(new Jogo(this, jogadores))).start();
                          }
                      }
                  }
              }
          } } finally {
          lock.unlock();
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

  public void addProcura(Pessoa ps){
    lock.lock();
    try {
      if (this.procurando.containsKey(ps.getRate())){
        this.procurando.get(ps.getRate()).add(ps);
      } else {
        ArrayList<Pessoa> rates = new ArrayList<Pessoa>();
        rates.add(ps);
        this.procurando.put(ps.getRate(), rates);
      }
      this.np++;
      synchronized(this){
        notifyAll();
      }
      lock.unlock();
    }catch(Exception e){
        e.printStackTrace();
        lock.unlock();
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

class Pessoa
{
  private String username;
  private String password;
  private String email;
  private int rate;
  private Socket cs;
  private Inicial i;

  public Pessoa(){
    this.username=null;
    this.password=null;
    this.email=null;
    this.rate = 5;
    this.i=null;
  }

  public Pessoa(String user, String mail, String pass, Socket cs, Inicial i){
    this.username=user;
    this.password=pass;
    this.email=mail;
    this.rate = 5;
    this.cs = cs;
    this.i=i;
  }

  public synchronized void setInicial(Inicial i){
    this.i=i;
  }

  public synchronized Inicial getInicial(){
    return this.i;
  }

  public synchronized void setSocket(Socket cs){
    this.cs=cs;
  }

  public String getUsername(){
    return this.username;
  }

  public String getMail(){
    return this.email;
  }

  public int getRate(){
    return this.rate;
  }

  public Socket getSocket(){
    return this.cs;
  }

  public String getPass(){
    return this.password;
  }

  public void setRate(int r){
    this.rate=r;
  }

  public void setUsername(String us){
    this.username=us;
  }

  public void setPassword(String pass){
    this.password=pass;
  }

  public void setMail(String mail){
    this.email=mail;
  }
}

class MM implements Runnable
{
  private Server s;

  public MM(Server s){
    this.s = s;
  }

  public void run(){
    s.Procura();
  }
}

class Inicial implements Runnable
{
    private Socket cs;
    private Server s;
    private boolean jogar;
    private Pessoa p;

    public Inicial(Socket cs, Server s){
        this.cs = cs;
        this.s = s;
        this.jogar = false;
        this.p = null;
    }

    public synchronized void run(){
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
                    this.p.setInicial(this);
                    this.s.addProcura(this.p);
                    while(this.jogar!=false){
                        wait();
                    }
                }
                if(current.equals("sair")){
                    this.s.removeAut(this.p);
                    break;
                }
            }
        }
        this.cs.close();
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


public class Serv
{
    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(9999);
        Socket cs;
        Server s = new Server();

        Thread mm = new Thread(new MM(s));
        mm.start();

        try {
          while (true)
          {
              cs = ss.accept();
                                      
              Thread ini = new Thread( new Inicial(cs,s) );
              ini.start();
          }
        } catch(Exception e){
          e.printStackTrace();
        }
        //ss.close();
    }
}