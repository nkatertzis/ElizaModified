/**
* Eliza Modified
* Author: Charles Hayden 
* http://www.chayden.net/eliza/Eliza.html 
* Modified by Nikos Katertzis to use as a part of a sentiment analysis program. 
*/

package elizamodified;

import java.util.Vector; 
 
/**
 *  Eliza reassembly list. 
 */ 
public class ReasembList extends Vector { 
 
    /**
     *  Add an element to the reassembly list. 
     * @param reasmb
     */ 
    public void add(String reasmb) { 
        addElement(reasmb); 
    } 
 
    /**
     *  Print the reassembly list. 
     * @param indent
     */ 
    public void print(int indent) { 
        for (int i = 0; i < size(); i++) { 
            for (int j = 0; j < indent; j++) System.out.print(" "); 
            String s = (String)elementAt(i); 
            System.out.println("reasemb: " + s); 
        } 
    } 
}
