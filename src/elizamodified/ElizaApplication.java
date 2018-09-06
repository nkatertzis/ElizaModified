/**
* Eliza Modified
* Author: Charles Hayden 
* http://www.chayden.net/eliza/Eliza.html 
* Modified by Nikos Katertzis to use as a part of a sentiment analysis program.
* This is the java application version created by Nikos Katertzis.
*/

package elizamodified;

import java.awt.*;

public class ElizaApplication extends Frame {

    ElizaMainApplication eliza;
    
    Frame f;

    public static void main(String[] args) {

        Frame f = new Frame("Eliza Modified");
        
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                
                System.exit(0);
                
            }
            
        ;}  
                
        );    
        
       ElizaApplication app = new ElizaApplication(f);
       
       app.setTitle("Eliza Modified");

    }

    public ElizaApplication(Frame w) {
        
        eliza = new ElizaMainApplication();
        
        eliza.readScript(true, "");
        
        eliza.runProgram("", w);

    }

}
