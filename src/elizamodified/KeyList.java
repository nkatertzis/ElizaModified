/**
* Eliza Modified
* Author: Charles Hayden 
* http://www.chayden.net/eliza/Eliza.html 
* Modified by Nikos Katertzis to use as a part of a sentiment analysis program. 
*/

package elizamodified;

import java.util.Vector; 
 
/**
 *  Eliza key list. 
 *  This stores all the keys. 
 */ 
public class KeyList extends Vector { 
 
    /**
     *  Add a new key. 
     * @param key
     * @param rank
     * @param decomp
     */ 
    public void add(String key, int rank, DecompList decomp) { 
        addElement(new Key(key, rank, decomp)); 
    } 
 
    /**
     *  Print all the keys. 
     * @param indent
     */ 
    public void print(int indent) { 
        for (int i = 0; i < size(); i++) { 
            Key k = (Key)elementAt(i); 
            k.print(indent); 
        } 
    } 
 
    /**
     *  Search the key list for a given key. 
     *  Return the Key if found, else null. 
     */ 
    Key getKey(String s) { 
        for (int i = 0; i < size(); i++) { 
            Key key = (Key)elementAt(i); 
            if (s.equals(key.key())) return key; 
        } 
        return null; 
    } 
 
    /**
     *  Break the string s into words. 
     *  For each word, if isKey is true, then push the key 
     *  into the stack. 
     * @param stack
     * @param s
     */ 
    public void buildKeyStack(KeyStack stack, String s) { 
        stack.reset(); 
        s = EString.trim(s); 
        String lines[] = new String[2]; 
        Key k; 
        while (EString.match(s, "* *", lines)) { 
            k = getKey(lines[0]); 
            if (k != null) stack.pushKey(k); 
            s = lines[1]; 
        } 
        k = getKey(s); 
        if (k != null) stack.pushKey(k); 
        
    } 
}