/**
 * created on 22:36:43 2 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package utils;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
//klasa dziedziczaca po klasie Thread bedaca watkiem powiadamiajacym listenerow o zakonczeniu swojego dzialania
public abstract class NotifyingThread extends Thread {
	//lista listenerow
	  protected final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();
	  //dodanie listenera
	  public final void addListener(final ThreadCompleteListener listener) {
	    listeners.add(listener);
	  }
	  //usuniecie listenera
	  public final void removeListener(final ThreadCompleteListener listener) {
	    listeners.remove(listener);
	  }
	  //powiadomienie listenerow
	  private final void notifyListeners() {
	    for (ThreadCompleteListener listener : listeners) {
	      listener.notifyOfThreadComplete(this);
	    }
	  }
	  //metoda uruchamiajaca watek (jego metode doRun) i powiadamiajaca listenerow po zakonczenu jego dzialania (nawet jak cos sie nie uda i rzuci wyjatek)
	  @Override
	  public final void run() {
	    try {
	      doRun();
	    } finally {
	      notifyListeners();
	    }
	  }
	  //doRun - metoda ktorej uzywa sie zamiast metody run()
	  public abstract void doRun();
	}
