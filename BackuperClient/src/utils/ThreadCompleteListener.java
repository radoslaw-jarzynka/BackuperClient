/**
 * created on 22:35:11 2 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package utils;
//listener - slucha czy ktorys watek zakonczyl swoja prace
public interface ThreadCompleteListener {
    void notifyOfThreadComplete(final Thread thread);
}