/*
 * This code is placed in the public domain by its author, Tim Peierls.
 */
package server;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * Wrapper for ExecutorService that imposes a hard bound on the number of running tasks, rejecting
 * submissions when the bound is reached. This may be appropriate when the wrapped service has no
 * bound on pool size, and you want to limit thread creation (and you are willing to have tasks
 * rejected at submission time in order to enforce that limit).
 */
public class BoundedExecutorService extends AbstractExecutorService {

  public BoundedExecutorService(ExecutorService exec, int permits) {
    this.exec = exec;
    this.sem = new Semaphore(permits); // fairness irrelevant here
  }

  @Override
  public void execute(final Runnable runnable) {
    if (!sem.tryAcquire()) {
      throw new RejectedExecutionException("Too many tasks running.");
    }
    exec.execute(new Runnable() {
      public void run() {
        try {
          runnable.run();
        } finally {
          sem.release();
        }
      }
    });
  }

  @Override
  public boolean isTerminated() {
    return exec.isTerminated();
  }

  @Override
  public boolean isShutdown() {
    return exec.isShutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return exec.shutdownNow();
  }

  @Override
  public void shutdown() {
    exec.shutdown();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    return exec.awaitTermination(timeout, unit);
  }

  private final ExecutorService exec;
  private final Semaphore sem;
}