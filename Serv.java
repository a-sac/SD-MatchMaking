import java.io.*;
import java.net.*;
import java.util.*;
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
      while(((current = in.readLine())!=null) && System.currentTimeMillis()<startTime+10000){
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
      while(acabou<1){
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
    this.equipa1.add(this.jogadores.get(0));
    System.out.println(this.equipa1);
    this.equipa2.add(this.jogadores.get(1));
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
    if((m1.getEscolhas().size()==1) && (m2.getEscolhas().size()==1)){
        System.out.println("começou o jogo");
        TimeUnit.SECONDS.sleep(5);
        System.out.println("acabou o jogo");
        System.out.println(this.jogadores);
        for(int i=0; i<this.jogadores.size(); i++){
          Thread ini = new Thread(new Inicial(s, this.jogadores.get(i)));
          ini.start();
        }
    }
    else{
      System.out.println("jogo cancelado");
      for(int i=0; i<this.jogadores.size(); i++){
        Thread ini = new Thread(new Inicial(s, this.jogadores.get(i)));
        ini.start();
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
          (new Thread(new Jogo(this, jogadores))).run();
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
              new Jogo(this, jogadores);
            }
          }
        }
      } 
    }
    lock.unlock();
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
      this.np++;
      notifyAll();
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

class Menu implements Serializable{
  // variáveis de instância
  private List<String> opcoes;
  private int op;
  private int choiceMenu;
  private Socket cs;
  private PrintWriter out;
  private BufferedReader in;

  /**
   * Constructor for objects of class Menu
   */
  public Menu(String[] opcoes, Socket cs) {
    try{
      this.opcoes = Arrays.asList(opcoes);
      this.op = 0;
      this.cs = cs;
      this.out = new PrintWriter(this.cs.getOutputStream(), true);
      this.in = new BufferedReader(new InputStreamReader( this.cs.getInputStream()));
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Método para apresentar o menu e ler uma opção.
   *
   */
  public synchronized void executaHomeMenu() {
      this.choiceMenu = 1;
      do {
          showMenu();
          this.op = lerOpcao();
      } while (this.op == -1);
  }

  public synchronized void executaClientMenu() {
      this.choiceMenu = 2;
      do {
          showMenu();
          this.op = lerOpcao();
      } while (this.op == -1);
  }

  private synchronized void showMenu() {
    try{
    
    switch(this.choiceMenu){
      case 1: this.out.println("\n*** Bem-vindo ao OverWatch ***");
      for (int i=0; i<this.opcoes.size(); i++) {
          this.out.print((i+1));
          this.out.print(" - ");
          this.out.println(this.opcoes.get(i));
      }
      this.out.println("0 - Sair");
      break;

      case 2: this.out.println("\n*** Cliente ***");
      for (int i=0; i<this.opcoes.size(); i++) {
          this.out.print((i+1));
          this.out.print(" - ");
          this.out.println(this.opcoes.get(i));
      }
      this.out.println("0 - Sair");
      break;
    }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  private synchronized int lerOpcao(){
    try{
      int op;
      out.print("Opção: ");
      try {
          String current = this.in.readLine();
          op = Integer.parseInt(current);
      }
      catch (Exception e) { // Não foi inscrito um int
          op = -1;
      }
      if (op<0 || op>this.opcoes.size()) {
          this.out.println("Opção Inválida!!!");
          op = -1;
      }
      return op;
    } catch(Exception e){
      return -1;
    }
  }

    /**
     * Método para obter a última opção lida
     */
  public synchronized int getOpcao() {
    return this.op;
  }
}

class Inicial implements Runnable
{
  private Socket cs;
  private Server s;
  private Pessoa p;
  private static Menu homeMenu, clientMenu;
  final Lock lock = new ReentrantLock();

  Inicial (Socket cs, Server s) 
  {
    this.cs = cs; 
    this.s=s;
    this.p= new Pessoa();
  }

  Inicial (Server s, Pessoa pe) 
  {
    this.cs = pe.getSocket();
    this.s=s;
    this.p= pe;
  }

  private void loadMenus(Socket cs) {
		String[] main = {"Iniciar Sessão",
		"Registar utilizador"};

    String[] client = {"Jogar"};
    
    homeMenu = new Menu(main, cs);
    clientMenu = new Menu(client, cs);
  }
  
  private void runHomeMenu() {
    try{
      PrintWriter out = new PrintWriter(this.cs.getOutputStream(), true);
      loadMenus(this.cs);

      do {
        homeMenu.executaHomeMenu();
        switch(homeMenu.getOpcao()) {
          case 0: try{
            this.cs.close();
          } catch (Exception e){
            out.println("There was a problem during the last request.  Could not close the game");
          }
          break;

          case 1: try {
            login();
          } catch (Exception e){
            out.println("There was a problem during the last request.  Could not login");
          }
          break;

          case 2: try {
            signUp();
          } catch (Exception e){
            out.println("There was a problem during the last request. Could not sign up");
          }
          break;
        }
        break;
      } while(homeMenu.getOpcao() != 0);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void runClientMenu() {
    try{
      PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader( cs.getInputStream()));

      loadMenus(this.cs);
      do {
        clientMenu.executaClientMenu();
        switch(clientMenu.getOpcao()) {
          case 0: try {
            out.println("Tem a certeza que quer sair?");
            String current = in.readLine();
            if(current.equals("Sim")){
              s.removeAut(this.p);
              this.cs.close();
            }else{
              runClientMenu();
            }
          } catch (Exception e){
            out.println("There was a problem during the last request.  Could not close the game");
          }
          break;

          case 1: try {
            out.println("Tem a certeza que quer jogar");
            String current = in.readLine();
            if(current.equals("Sim")){
              procurar(this.p);
              out.println("procurando...");
            }else{
              runClientMenu();
            }
          } catch (Exception e){
            out.println("There was a problem during the last request.  Could not login");
          }
          break;
        }
        break;
      } while(clientMenu.getOpcao() != 0);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void login(){
    try{
      PrintWriter out = new PrintWriter(this.cs.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader( this.cs.getInputStream()));

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
            runClientMenu();
          } else{
            out.println("conta a ser usada");
            cs.close();
          }
      }
      if(i==2){
        out.println("password errad!");
        runHomeMenu();
      }
      if(i==3){
        out.println("username desconhecido");
        runHomeMenu();
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void signUp(){
    try{
      PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader( cs.getInputStream()));

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
        runHomeMenu();
      }
      if(i==2){
        out.println("username a ser usado");
        runHomeMenu();
      }
      if(i==3){
        Pessoa nova = new Pessoa(user, mail, pass, this.cs);
        this.p= nova;
        this.s.addRegisto(nova);
        this.s.addAutenticados(nova);
        out.println("Registado e autenticado!");
        runClientMenu();
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public synchronized void run() 
  {
    try 
    {
      if(this.p.getUsername()==null) {
        try {
          runHomeMenu();
        } catch (NullPointerException e){
          System.out.println("Problema ao carregar Menu inicial");
        }
      }
      else{
        runClientMenu();
      }
    } catch (Exception e) { e.printStackTrace(); }
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
          e.printStackTrace();
        }
        //ss.close();
    }
}
