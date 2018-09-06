/**
* Eliza Modified
* Author: Charles Hayden 
* http://www.chayden.net/eliza/Eliza.html 
* Modified by Nikos Katertzis to use as a part of a sentiment analysis program. 
* This is the java applet version, modified to run by Nikos Katertzis.
*/

package elizamodified;

import java.applet.*;
import java.awt.*;

public class ElizaApplet extends Applet {

    static String scriptPathname = "";
    static String testPathname = "";
    static String scriptURL = "https://github.com/codeanticode/eliza/blob/master/data/eliza.script";
    static String testURL = "https://github.com/codeanticode/eliza/blob/master/data/eliza.script";
  
    boolean useWindow = true;
    boolean local = true;

    ElizaMainApplet eliza;

    @Override
    public void init() {
        
        showStatus("Loading Eliza");
        
        eliza = new ElizaMainApplet();     

    } 

    @Override
    public void start() {
        String script = getScriptParam();
        String test = getTestParam();
        if (local) {
            script = scriptPathname;
            test = testPathname;
        }
        showStatus("Loading script from " + script);
        eliza.readScript(local, script);
        showStatus("Ready");
        
        if (useWindow)
            eliza.runProgram(test, this);
        else
            eliza.runProgram(test, null);
    }

    @Override
    public boolean handleEvent(Event e) {
        return eliza.handleEvent(e);
    }

    String getScriptParam() {
        String script = getParameter("script");
        if (script == null) script = scriptURL;
        return script;
    }

    String getTestParam() {
        String test = getParameter("test");
        if (test == null) test = testURL;
        return test;
    }

    @Override
    public String[][] getParameterInfo() {
        String[][] info = {
            {"script", "URL", "URL of script file"},
            {"test", "URL", "URL of test file"}
        };
        return info;
    }

    @Override
    public String getAppletInfo() {
        return "Eliza v0.1 written by Charles Hayden chayden@monmouth.com";
    }

    public String getAppletModifiedInfo() {
        return "ElizaModified v0.1 written by Charles Hayden chayden@monmouth.com "
                + "and modivied by Nikos Katertzis nkatertz@gmail.com";
    }

}
