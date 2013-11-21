/**
 * created on 19:01:59 14 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package utils;

public final class OsUtils
{
   private static String OS = null;
   public static String getOsName() {
      if(OS == null) { 
    	  OS = System.getProperty("os.name"); 
      }
      return OS;
   }
   
   public static boolean isWindows() {
      return getOsName().startsWith("Windows");
   }

   public static boolean isMacOsX() {
	   return getOsName().startsWith("Mac");
   }
   
   public static boolean isLinux() {
	   return (getOsName().indexOf("nix") >= 0 || getOsName().indexOf("nux") >= 0 || getOsName().indexOf("aix") > 0 );
   }

public static boolean isSolaris() {
	return (getOsName().indexOf("sunos") >= 0);
	}
}