import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

class Heros
{
  ArrayList<String> disp;

  public Heros(){
    this.disp = new ArrayList<>();
    this.disp.add("ash");
    this.disp.add("scout");
  }

  public synchronized ArrayList<String> getLista(){
    return disp;
  }

  public synchronized void setLista(ArrayList<String> d){
    this.disp=d;
  }
}

class Jogar implements Runnable
{
  private Socket cs;
  private Server s;

  public Jogar(Socket cs, Server s){
    this.cs = cs;
    this.s = s;
  }

  public void run(){
    
  }
}

class Escolha implements Runnable
{
  private Socket cs;
  private Heros h;

  public Escolha(Socket s, Heros h){
    this.cs = s;
    this.h=h;
  }

  public void run(){
    try{
      PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader( cs.getInputStream()));

      BufferedReader sin = new BufferedReader(new InputStreamReader( System.in));
      boolean flag = false;
      String current;
      out.println("Qual é o Hero que deseja ?");
      while(!flag){
        current = in.readLine();
        flag = HeroPic(current);
        if(!flag){
          out.println("Hero indisponivel");
        } else{
          out.println("Hero escolhido com sucesso");
        }
      }
      System.out.println("morri");
      Thread.currentThread().interrupt();
    } catch(IOException e){

    }

  }

  public synchronized boolean HeroPic(String hero){
    ArrayList<String> disp = this.h.getLista();
    boolean flag = false;
    for (String a : disp) {
      if (hero.equals(a)){
        flag = true;
        disp.remove(a);
        this.h.setLista(disp);
      }
    }
    return flag;
  }
}

class MatchMaking implements Runnable
{
  private ArrayList<Pessoa> jogadores;
  private ArrayList<Thread> threads;

  MatchMaking(ArrayList<Pessoa> jog){
    this.jogadores=jog;
    this.threads = new ArrayList<Thread>();
  }

  public void run(){
    Heros h = new Heros();
    for(Pessoa a: jogadores){
      Thread t = (new Thread(new Escolha(a.getSocket(), h)));
      this.threads.add(t);
      t.start();
    }
    System.out.println("equipa procurou");
    int i=0;
    while (i!=1){
      i=0;
      for(Thread t: this.threads){
        if(t.isInterrupted()) i++;
      }
    }
    System.out.println("equipa morreu");
    Thread.currentThread().interrupt();
  }
}

class Jogo
{
  private ArrayList<Pessoa> jogadores;
  private ArrayList<Pessoa> equipa1;
  private ArrayList<Pessoa> equipa2;
  private ArrayList<Thread> threads;

  public Jogo(ArrayList<Pessoa> jogadores){
    this.jogadores = new ArrayList<Pessoa>();
    this.equipa1 = new ArrayList<Pessoa>();
    this.equipa2 = new ArrayList<Pessoa>();
    this.threads = new ArrayList<Thread>();
    this.jogadores=jogadores;
    System.out.println("formar equipas");
    this.formarEquipas();
  }

  public void formarEquipas(){
    int i;
    System.out.println("mm starting");
    System.out.println(this.jogadores);
    this.equipa1.add(this.jogadores.get(0));
    System.out.println(this.equipa1);
    this.equipa2.add(this.jogadores.get(1));
    System.out.println("mm starting2");
    Thread e1 = new Thread(new MatchMaking(equipa1));
    Thread e2 = new Thread(new MatchMaking(equipa2));
    System.out.println("thread criada");
    e1.start();
    e2.start();
    i=0;
    while (i!=2){
      i=0;
      if(e1.isInterrupted()) i++;
      if(e2.isInterrupted()) i++;
    }
    try{
      System.out.println("começou o jogo");
      TimeUnit.SECONDS.sleep(30);
    } catch (InterruptedException e){
      System.out.println("nao começou o jogo");
    }
  }
}

class Server
{
  private List<Pessoa> pessoas;
  private Map<Integer, ArrayList<Pessoa>> procurando;
  private List<Jogo> jogos;
  private Map<String,Pessoa> registos;
  private int np;

  public Server(){
    this.pessoas = new ArrayList<Pessoa>();
    this.procurando = new HashMap<Integer, ArrayList<Pessoa>>();
    this.jogos = new ArrayList<Jogo>();
    this.registos = new HashMap<String,Pessoa>();
    this.np=0;
  }

  public void Procura(){
    int i = 0;
    while(true){
      try{
        while (i==this.np) wait();
        criarJogo();
        i++;
      } catch(Exception e){

      }
    }
  }

  public void criarJogo(){
    ArrayList<Pessoa> jogadores = new ArrayList<>();
    for(int i=0; i<9; i++){
      if(this.procurando.containsKey(i)){
        System.out.println(this.procurando.get(i).size());
        if(this.procurando.get(i).size()>=2){
          for(int j = 0; j<2; j++){
            Pessoa p = this.procurando.get(i).get(0);
            this.procurando.get(i).remove(p);
            jogadores.add(p);
          }
          System.out.println("criar jogo");
          new Jogo(jogadores);
        } else{
          if(this.procurando.containsKey(i+1)){
            if((this.procurando.get(i).size() + this.procurando.get(i+1).size())>=2){
              for (Pessoa a : this.procurando.get(i)) {
                jogadores.add(a);
                this.procurando.get(i).remove(a);
              }
              int s = jogadores.size();
              for(int j=s; j<=10; j++){
                Pessoa p = this.procurando.get(i+1).get(0);
                this.procurando.get(i+1).remove(p);
                jogadores.add(p);
              }
              new Jogo(jogadores);
            }
          }
        }
      } 
    }
  }

  public Map<String, Pessoa> getRegistos(){
    return this.registos;
  }

  public void addProcura(Pessoa ps){
    try {
      if (this.procurando.containsKey(ps.getRate())){
        this.procurando.get(ps.getRate()).add(ps);
      } else {
        ArrayList<Pessoa> rates = new ArrayList<Pessoa>();
        rates.add(ps);
        this.procurando.put(ps.getRate(), rates);
      }
      this.np++;
      notifyAll();
    }catch(Exception e){
        
    }
  }

  public Map<Integer, ArrayList<Pessoa>> getProcura(){
    return this.procurando;
  }

  public void addRegisto(Pessoa nova){
    this.registos.put(nova.getUsername(), nova);
  }
}

class Pessoa
{
  private String username;
  private String password;
  private String email;
  private int rate;
  private Socket cs;

  public Pessoa(){
    this.username=null;
    this.password=null;
    this.email=null;
    this.rate = 5;
  }

  public Pessoa(String user, String mail, String pass, Socket cs){
    this.username=user;
    this.password=pass;
    this.email=mail;
    this.rate = 5;
    this.cs = cs;
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
  Socket cs;
  Server s;
  Pessoa p;
    
  Inicial (Socket cs, Server s) 
  {
    this.cs = cs; 
    this.s=s;
    this.p= new Pessoa();
  }
    
  public void run() 
  {
    try 
    {
      PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader( cs.getInputStream()));

      BufferedReader sin = new BufferedReader(new InputStreamReader( System.in));

      out.println("Escreva o username");
      String user = in.readLine();
      out.println("Escreva a password");
      String pass = in.readLine();
      Map<String, Pessoa> registos = s.getRegistos();
      if(registos.containsKey(user)){
        if(registos.get(user).getPass().equals(pass)){
          this.p=registos.get(user);
          out.println("autenticado!");
        }
        else{
          out.println("Password errada!");
          cs.close();
        }
      } else{
        out.println("Não está registado. Deseja registar-se ?");
        String resposta = in.readLine();
        if(resposta.equals("Sim")){
          out.println("Escreva o username");
          user = in.readLine();
          out.println("Escreva o seu email");
          String mail = in.readLine();
          out.println("Escreva a password");
          pass = in.readLine();
          Pessoa nova = new Pessoa(user, mail, pass, this.cs);
          this.p= nova;
          this.s.addRegisto(nova);
          out.println("Registado e autenticado!");
          out.println("Deseja jogar?");
          if(in.readLine().equals("Sim")){
            out.println("Procurando Jogo...");
            procurar(this.p);
          }
        }else{
          cs.close();
        }
      }
	        
    } catch ( IOException e) { e.printStackTrace(); }
  }

  public void procurar(Pessoa ps){
    s.addProcura(ps);
  }
}


public class ChatServ
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

        }
        //ss.close();
    }
}
