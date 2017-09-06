import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.Future;
/*
* Determines the execution of SpotifyQueries
*/

class SpotifyExecutor extends ThreadPoolExecutor{

  //TODO Fix these numbers, only for testing
  private static int corePoolSize = 5;
  private static int maximumPoolSize = 8;
  private static long keepAlive = 1000;
  private static TimeUnit keepAliveUnit = TimeUnit.MILLISECONDS;

  private boolean isPaused;
  private ReentrantLock pauseLock = new ReentrantLock();
  private Condition unpaused = pauseLock.newCondition();

  public SpotifyExecutor(){
    super(corePoolSize, maximumPoolSize, keepAlive, keepAliveUnit, new LinkedBlockingQueue<Runnable>());
    isPaused = false;
  }

  @Override
  public void beforeExecute(Thread threadForJob, Runnable job){
    super.beforeExecute(threadForJob, job);
    //Check we aren't waiting as part of a backOff
    //If we are paused, monitor the pause condition
    pauseLock.lock();
    try{
      while(isPaused){
        unpaused.await(); //Must have lock to be able to do this
      }
    } catch(InterruptedException ex){
      threadForJob.interrupt();
    } finally {
      pauseLock.unlock();
    }
  }

  // @Override
  // public void afterExecute(Runnable r, Throwable t){
  //   super.afterExecute(r, t);
  //   //If there was no execution problem
  //
  //   if (t != null) {
  //       //Now we need to interogate what we get back
  //       System.out.println("After execute");
  //       t.printStackTrace();
  //   } else {
  //     System.out.println(r instanceof Future<?>);
  //   }
  // }

  //TODO Could be improved with a ReadWrite lock
  public void backOff(int backOffTime){
    pauseLock.lock(); //Blocks for the lock but never mind
    try{
      isPaused = true;
      System.out.println("Waiting for " + backOffTime + " seconds");
      //Hold the lock for a sufficient amount of Time
      //Plus a little bit more to save us going back too early
      //TimeUnit.SECONDS.sleep(backOffTime + 2);
      unpaused.await(backOffTime + 2, TimeUnit.SECONDS);
    } catch (InterruptedException ex){
      //TODO What to do here...
      ex.printStackTrace();
    } finally {
      //Always unlock and tell everyone we are good to try again
      isPaused = false;
      unpaused.signalAll(); //Need lock for this
      pauseLock.unlock();
    }

  }


}
