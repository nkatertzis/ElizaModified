/**
* Eliza Modified
* Author: Charles Hayden 
* http://www.chayden.net/eliza/Eliza.html 
* Modified by Nikos Katertzis to use as a part of a sentiment analysis program. 
*/

package elizamodified;

/**
 *  Eliza memory class 
 */ 
 
public class Mem { 
 
    /** The memory size */ 
    final int memMax = 20; 
    /** The memory */ 
    String memory[] = new String[memMax]; 
    /** The memory top */ 
    int memTop = 0; 
 
    public void save(String str) { 
        if (memTop < memMax) { 
            memory[memTop++] = str; 
        } 
    } 
 
    public String get() { 
        if (memTop == 0) return null; 
        String m = memory[0]; 
        for (int i = 0; i < memTop-1; i++) 
            memory[i] = memory[i+1]; 
        memTop--; 
        return m; 
    } 
}