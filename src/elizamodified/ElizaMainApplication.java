/**
 * Eliza Modified
 * Author: Charles Hayden
 * http://www.chayden.net/eliza/Eliza.html
 * Modified by Nikos Katertzis to use as a part of a sentiment analysis program.
 */
package elizamodified;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import java.awt.*;

import java.awt.Label;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.BoxLayout;

/**
 * Eliza main class. Stores the processed script. Does the input
 * transformations.
 */
public class ElizaMainApplication {

    final boolean echoInput = false;
    final boolean printData = false;
    final boolean printKeys = false;
    final boolean printSyns = false;
    final boolean printPrePost = false;
    final boolean printInitialFinal = false;

    /**
     * The key list
     */
    KeyList keys = new KeyList();
    /**
     * The syn list
     */
    SynList syns = new SynList();
    /**
     * The pre list
     */
    PrePostList pre = new PrePostList();
    /**
     * The post list
     */
    PrePostList post = new PrePostList();
    /**
     * Initial string
     */
    String initial = "Hello.";
    /**
     * Final string
     */
    String finl = "Goodbye.";
    /**
     * Quit list
     */
    WordList quit = new WordList();

    /**
     * Key stack
     */
    KeyStack keyStack = new KeyStack();

    /**
     * Memory
     */
    Mem mem = new Mem();

    DecompList lastDecomp;
    ReasembList lastReasemb;
    boolean finished = false;

    static final int success = 0;
    static final int failure = 1;
    static final int gotoRule = 2;

////////////////////////////////////////////////////////////////////////////////
    /**
     * Modification by Nikos Katertzis. These variables count the total
     * sentiment score.
     */
    public static int count = 0;

    int moodcount = 0;
////////////////////////////////////////////////////////////////////////////////

    public boolean finished() {
        return finished;
    }

    /**
     * Process a line of script input.
     *
     * @param s
     */
    public void collect(String s) {
        String lines[] = new String[4];

        if (EString.match(s, "*reasmb: *", lines)) {
            if (lastReasemb == null) {
                System.out.println("Error: no last reasemb");
                return;
            }
            lastReasemb.add(lines[1]);
        } else if (EString.match(s, "*decomp: *", lines)) {
            if (lastDecomp == null) {
                System.out.println("Error: no last decomp");
                return;
            }
            lastReasemb = new ReasembList();
            String temp = lines[1];
            if (EString.match(temp, "$ *", lines)) {
                lastDecomp.add(lines[0], true, lastReasemb);
            } else {
                lastDecomp.add(temp, false, lastReasemb);
            }
        } else if (EString.match(s, "*key: * #*", lines)) {
            lastDecomp = new DecompList();
            lastReasemb = null;
            int n = 0;
            if (lines[2].length() != 0) {
                try {
                    n = Integer.parseInt(lines[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Number is wrong in key: " + lines[2]);
                }
            }
            keys.add(lines[1], n, lastDecomp);
        } else if (EString.match(s, "*key: *", lines)) {
            lastDecomp = new DecompList();
            lastReasemb = null;
            keys.add(lines[1], 0, lastDecomp);
        } else if (EString.match(s, "*synon: * *", lines)) {
            WordList words = new WordList();
            words.add(lines[1]);
            s = lines[2];
            while (EString.match(s, "* *", lines)) {
                words.add(lines[0]);
                s = lines[1];
            }
            words.add(s);
            syns.add(words);
        } else if (EString.match(s, "*pre: * *", lines)) {
            pre.add(lines[1], lines[2]);
        } else if (EString.match(s, "*post: * *", lines)) {
            post.add(lines[1], lines[2]);
        } else if (EString.match(s, "*initial: *", lines)) {
            initial = lines[1];
        } else if (EString.match(s, "*final: *", lines)) {
            finl = lines[1];
        } else if (EString.match(s, "*quit: *", lines)) {
            quit.add(" " + lines[1] + " ");
        } else {
            System.out.println("Unrecognized input: " + s);
        }
    }

    /**
     * Print the stored script.
     */
    public void print() {
        if (printKeys) {
            keys.print(0);
        }
        if (printSyns) {
            syns.print(0);
        }
        if (printPrePost) {
            pre.print(0);
            post.print(0);
        }
        if (printInitialFinal) {
            System.out.println("initial: " + initial);
            System.out.println("final:   " + finl);
            quit.print(0);
        }
    }

    /**
     * Process a line of input.
     *
     * @param s
     * @return
     */
    public String processInput(String s) {

        textfield.setText("");

        textfield.requestFocus();

        String reply;
        //  Do some input transformations first.
        s = EString.translate(s, "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "abcdefghijklmnopqrstuvwxyz");
        s = EString.translate(s, "@#$%^&*()_-+=~`{[}]|:;<>\\\"",
                "                          ");
        s = EString.translate(s, ",?!", "...");
        //  Compress out multiple speace.
        s = EString.compress(s);

////////////////////////////////////////////////////////////////////////////////
        /**
         * Modification by Nikos Katertzis. These lines of code process and
         * categorize the input phrase with the sentiment analysis tools of the
         * Stanford NLP sentiment analysis tools. The "count" variable is
         * updated according to the analysis results.
         */
        Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation annotation = pipeline.process(s);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        sentences.forEach((CoreMap sentence) -> {

            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            System.out.println("The sentence '" + sentence + "' shows " + sentiment.toLowerCase() + " sentiment.");

            switch (sentiment) {

                case "Positive":

                    count++;

                    System.out.println("The current sentiment count is: " + count + ".");

                    break;

                case "Very positive":

                    count = count + 2;

                    System.out.println("The current sentiment count is: " + count + ".");

                    break;

                case "Negative":

                    count--;

                    System.out.println("The current sentiment count is: " + count + ".");

                    break;

                case "Very negative":

                    count = count - 2;

                    System.out.println("The current sentiment count is: " + count + ".");

                    break;

            }

        });

////////////////////////////////////////////////////////////////////////////////
        String lines[] = new String[2];
        //  Break apart sentences, and do each separately.
        while (EString.match(s, "*.*", lines)) {
            reply = sentence(lines[0]);
            if (reply != null) {
                return reply;
            }
            s = EString.trim(lines[1]);
        }
        if (s.length() != 0) {
            reply = sentence(s);
            if (reply != null) {
                return reply;
            }
        }
        //  Nothing matched, so try memory.
        String m = mem.get();
        if (m != null) {
            return m;
        }

        //  No memory, reply with xnone.
        Key key = keys.getKey("xnone");
        if (key != null) {
            Key dummy = null;
            reply = decompose(key, s, dummy);
            if (reply != null) {
                return reply;
            }
        }
        //  No xnone, just say anything.
        return "I am at a loss for words.";
    }

////////////////////////////////////////////////////////////////////////////////
    /**
     * Modification by Nikos Katertzis. Getter method for the "count" variable.
     *
     * @return
     */
    public int getCount() {

        return count;

    }

////////////////////////////////////////////////////////////////////////////////
    /**
     * Process a sentence. (1) Make pre transformations. (2) Check for quit
     * word. (3) Scan sentence for keys, build key stack. (4) Try decompositions
     * for each key.
     */
    String sentence(String s) {

        s = pre.translate(s);

        s = EString.pad(s);

        if (quit.find(s)) {
            finished = true;
            return finl;
        }
        keys.buildKeyStack(keyStack, s);

        for (int i = 0; i < keyStack.keyTop(); i++) {

            Key gotoKey = new Key();

            String reply = decompose(keyStack.key(i), s, gotoKey);
            if (reply != null) {
                return reply;
            }
            //  If decomposition returned gotoKey, try it
            while (gotoKey.key() != null) {

                reply = decompose(gotoKey, s, gotoKey);

                if (reply != null) {
                    return reply;
                }
            }
        }
        return null;
    }

    /**
     * Decompose a string according to the given key. Try each decomposition
     * rule in order. If it matches, assemble a reply and return it. If assembly
     * fails, try another decomposition rule. If assembly is a goto rule, return
     * null and give the key. If assembly succeeds, return the reply;
     */
    String decompose(Key key, String s, Key gotoKey) {
        String reply[] = new String[10];
        for (int i = 0; i < key.decomp().size(); i++) {
            Decomp d = (Decomp) key.decomp().elementAt(i);
            String pat = d.pattern();
            if (syns.matchDecomp(s, pat, reply)) {
                String rep = assemble(d, reply, gotoKey);
                if (rep != null) {
                    return rep;
                }
                if (gotoKey.key() != null) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Assembly a reply from a decomp rule and the input. If the reassembly rule
     * is goto, return null and give the gotoKey to use. Otherwise return the
     * response.
     */
    String assemble(Decomp d, String reply[], Key gotoKey) {
        String lines[] = new String[3];
        d.stepRule();
        String rule = d.nextRule();
        if (EString.match(rule, "goto *", lines)) {
            //  goto rule -- set gotoKey and return false.
            gotoKey.copy(keys.getKey(lines[0]));
            if (gotoKey.key() != null) {
                return null;
            }
            System.out.println("Goto rule did not match key: " + lines[0]);
            return null;
        }
        String work = "";
        while (EString.match(rule, "* (#)*", lines)) {
            //  reassembly rule with number substitution
            rule = lines[2];        // there might be more
            int n = 0;
            try {
                n = Integer.parseInt(lines[1]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Number is wrong in reassembly rule " + lines[1]);
            }
            if (n < 0 || n >= reply.length) {
                System.out.println("Substitution number is bad " + lines[1]);
                return null;
            }
            reply[n] = post.translate(reply[n]);
            work += lines[0] + " " + reply[n];
        }
        work += rule;
        if (d.mem()) {
            mem.save(work);
            return null;
        }
        return work;
    }

////////////////////////////////////////////////////////////////////////////////
    /**
     * Modification by Nikos Katertzis. Define additional variables to hold user
     * interface elements for the Eliza Modified application.
     *
     */
    TextArea textarea, textarea1;
////////////////////////////////////////////////////////////////////////////////
    TextField textfield;
////////////////////////////////////////////////////////////////////////////////
    Button mood, sound;
////////////////////////////////////////////////////////////////////////////////
    Label label, label1;

    public void response(String str) {
        textarea.appendText(str);
        textarea.appendText("\n");

    }

    /**
     * Modification by Nikos Katertzis. Read the Eliza script from a file that
     * is placed in the Java classes folder.
     *
     */
    int readScript(boolean local, String script) {

////////////////////////////////////////////////////////////////////////////////
        InputStream inp;
////////////////////////////////////////////////////////////////////////////////
        DataInputStream in;

        try {
            if (local) {

////////////////////////////////////////////////////////////////////////////////
                inp = getClass().getResourceAsStream("eliza.script");

                in = new DataInputStream(inp);

////////////////////////////////////////////////////////////////////////////////
            } else {
                try {
                    URL url = new URL(script);
                    URLConnection connection = url.openConnection();
                    in = new DataInputStream(connection.getInputStream());
                } catch (MalformedURLException e) {
                    System.out.println("The URL is malformed: " + script);
                    return 1;
                } catch (IOException e) {
                    System.out.println("Could not read script file.");
                    return 1;
                }
            }
            while (true) {
                String s;
                s = in.readLine();
                if (s == null) {
                    break;
                }
                if (echoInput) {
                    System.out.println(s);
                }
                collect(s);
            }
        } catch (IOException e) {
            System.out.println("There was a problem reading the script file.");
            System.out.println("Tried " + script);
            return 1;
        }
        if (printData) {
            print();
        }
        return 0;
    }

    /**
     * Modification by Nikos Katertzis. Draws a modified user interface to
     * accommodate the new features of the program related to sentiment
     * analysis.
     */
    int runProgram(String test, Frame w) {

        DataInputStream in;

        if (w != null) {

///////////////////////////////////////////////////////////////////////////////
            Font myFont = new Font("Serif", Font.BOLD, 18);

            Font font = new Font("Serif", Font.ITALIC, 18);

            w.setLayout(new BoxLayout(w, BoxLayout.PAGE_AXIS));
////////////////////////////////////////////////////////////////////////////////

            textarea = new TextArea(10, 40);

            textarea.setEditable(false);
////////////////////////////////////////////////////////////////////////////////
            textarea1 = new TextArea(10, 40);

            textarea1.setEditable(false);

            label1 = new Label("Chat Room");

            label1.setAlignment((int) Component.CENTER_ALIGNMENT);

            label1.setBackground(Color.LIGHT_GRAY);

            label1.setFont(myFont);

            label = new Label("Sentiment Analysis");

            label.setAlignment((int) Component.CENTER_ALIGNMENT);

            label.setBackground(Color.LIGHT_GRAY);

            label.setFont(myFont);

            mood = new Button("Check mood");

            mood.setFont(font);

            sound = new Button("Play appropriate sound");

            sound.setFont(font);

            w.add(label1);

////////////////////////////////////////////////////////////////////////////////
            w.add(textarea);

            textfield = new TextField(15);

            w.add(textfield);

////////////////////////////////////////////////////////////////////////////////
            w.add(label);

            w.add(mood);

            w.add(textarea1);

            w.add(sound);

            // Listener for button press for the "Check mood" button. Responds
            // by updating the textarea below it with a phrase that shows the
            // current mood of the user as depicted by the "count" variable.
            mood.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    moodcount = getCount();

                    if (moodcount > 0) {

                        textarea1.setText("It seems you are in a positive mood!");

                    } else if (moodcount < 0) {

                        textarea1.setText("It seems you are in a negative mood!");

                    } else {

                        textarea1.setText("It seems you are in a neutral mood!");

                    }

                }

            });

            // Listener for button press for the "Play appropriate sound" button.
            // Responds by plying a sound that shows the current mood of the
            // user as depicted by the "count" variable.
            sound.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    moodcount = getCount();

                    if (moodcount > 0) {

                        try {

                            Synthesizer synth = MidiSystem.getSynthesizer();

                            synth.open();

                            final MidiChannel[] mc = synth.getChannels();

                            Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

                            synth.loadInstrument(instr[90]);

                            mc[5].noteOn(60, 600);

                            mc[5].noteOn(50, 300);

                        } catch (MidiUnavailableException ex) {

                            Logger.getLogger(ElizaMainApplication.class.getName()).log(Level.SEVERE, null, ex);

                        }

                    } else if (moodcount < 0) {

                        try {

                            Synthesizer synth = MidiSystem.getSynthesizer();

                            synth.open();

                            final MidiChannel[] mc = synth.getChannels();

                            Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

                            synth.loadInstrument(instr[90]);

                            mc[5].noteOn(99, 400);

                            mc[5].noteOn(69, 199);

                        } catch (MidiUnavailableException ex) {

                            Logger.getLogger(ElizaMainApplication.class.getName()).log(Level.SEVERE, null, ex);

                        }

                    } else {

                        try {

                            Synthesizer synth = MidiSystem.getSynthesizer();

                            synth.open();

                            final MidiChannel[] mc = synth.getChannels();

                            Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

                            synth.loadInstrument(instr[90]);

                            mc[5].noteOn(300, 550);

                            mc[5].noteOn(150, 770);

                        } catch (MidiUnavailableException ex) {

                            Logger.getLogger(ElizaMainApplication.class.getName()).log(Level.SEVERE, null, ex);

                        }

                    }

                }
            });

////////////////////////////////////////////////////////////////////////////////
            w.resize(600, 600);

            w.show();

            String hello = "Hello.";

            response(">> " + hello);

            response(processInput(hello));

            textfield.setText("");

            textfield.requestFocus();

////////////////////////////////////////////////////////////////////////////////
            /**
             * Modification by Nikos Katertzis. Handles the events related to
             * text input by the user at the "Chat Room" section. Call the
             * method to process the input phrase and clear the input text area
             * to await for new input. Replaces the "handleEvent" method of the
             * original Java Eliza program.
             */
            textfield.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent cl) {

                    switch (cl.getID()) {

                        case Event.ACTION_EVENT:

                            if (cl.getSource() == textfield) {

                                String input = textfield.getText();

                                System.out.println("input " + input);

                                String reply = processInput(input);

                                textfield.setText(" ");

                                textfield.setText("");

                                textfield.requestFocus();

                                response(">> " + input);

                                response(reply);

                                print();

                            }

                    }

                }

            });
////////////////////////////////////////////////////////////////////////////////
        } else {
            try {
                in = new DataInputStream(new FileInputStream(test));
                String s;
                s = "Hello.";
                while (true) {
                    System.out.println(">> " + s);
                    String reply = processInput(s);
                    System.out.println(reply);
                    if (finished) {
                        break;
                    }
                    s = in.readLine();
                    if (s == null) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Problem reading test file.");
                return 1;

            }

        }

        return 0;

    }

    /**
     * Modification by Nikos Katertzis. "handleEvent" method was deleted. Events
     * are handled by appropriate listener (textfield action listener).
     */
}
