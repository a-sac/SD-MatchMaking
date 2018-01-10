
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

public class Pessoa
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

  public synchronized void setRate(int r){
    this.rate=r;
  }

  public synchronized void setUsername(String us){
    this.username=us;
  }

  public synchronized void setPassword(String pass){
    this.password=pass;
  }

  public synchronized void setMail(String mail){
    this.email=mail;
  }
}