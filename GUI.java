
import javax.swing.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.text.*;
import java.util.Arrays;
//AttributedCharacterIterator.Attribute;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.awt.*;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.midi.*;
import java.util.Collections;
/**
 * Write a description of class QuickInflection here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class GUI extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener
{

    public Font font;
    public JFrame display;
    public Timer clock;
    public int numberOfSigns, fontSize;
    public  double[] velocity, offsets, totals, netCenter;
    public int[] clickPoint;
    public double friction = 0.99, gravity = 0.05, bounceFriction = 0.3;
    public final double REAL_GRAVITY = 0.05, ARTIFICIAL_GRAVITY = 0.5, REAL_BF = 0.3, ARTIFICIAL_BF = 0.03;
    public boolean animating, displayFullProjection, showProgression, autoCenter;
    public Archinovica archinovica;
    public LiveReceiver myRec;
    //public ArrayList<ArrayList<SemioticFunction>> mySigns, animationQueue;
    public ArrayList<ArrayList<Letter>> letters;
    public ArrayList<MiscellaneousLetter> miscellaneousLetters;
    public ArrayList<LegacyChordProgression> legacyProgressions;
    public Color black;
    public Sequence sequence;
    public ChordProgression chordProgression;
    public final String[] NOTE_NAMES = {"C ", "Db", "D ", "Eb", "E ", "F ", "F#", "G ", "Ab", "A ", "Bb", "B "};
    public ArrayList<VerticalSet> states;
    String[] alf = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    int[] keyCodes = new int[]{KeyEvent.VK_A,KeyEvent.VK_B,KeyEvent.VK_C,KeyEvent.VK_D,KeyEvent.VK_E,KeyEvent.VK_F,KeyEvent.VK_G,
            KeyEvent.VK_H,KeyEvent.VK_I,KeyEvent.VK_J,KeyEvent.VK_K,KeyEvent.VK_L,KeyEvent.VK_M,KeyEvent.VK_N,KeyEvent.VK_O,KeyEvent.VK_P,
            KeyEvent.VK_Q,KeyEvent.VK_R,KeyEvent.VK_S,KeyEvent.VK_T,KeyEvent.VK_U,KeyEvent.VK_V,KeyEvent.VK_W,KeyEvent.VK_X,KeyEvent.VK_Y,KeyEvent.VK_Z};

    public GUI(Receiver r)
    {
        addKeyBindings();
        if(r != null)
            myRec = (LiveReceiver)r;

        display = new JFrame("Archinovica");
        display.setBounds(0, 0, 600, 600);
        display.add(this);
        display.setMenuBar(createMenu());

        showProgression = true;

        autoCenter = true;

        Archinovica.legacyBehavior = false;
        legacyProgressions = new ArrayList<LegacyChordProgression>();

        clock = new Timer(50, this);
        offsets = new double[]{0, 0};
        velocity = new double[]{0, 0};
        numberOfSigns = 0;
        totals = new double[]{0, 0};
        netCenter = new double[]{0, 0};
        // mySigns = new ArrayList<ArrayList<SemioticFunction>>();
        //animationQueue = new ArrayList<ArrayList<SemioticFunction>>();
        letters = new ArrayList<ArrayList<Letter>>();
        miscellaneousLetters = new ArrayList<MiscellaneousLetter>();
        black = Color.BLACK;

        displayFullProjection = false;

        fontSize = 14;
        //System.out.println("FONT SIZE:" + fontSize);
        font = new Font("Times New Roman", Font.BOLD, fontSize);
        //setEditable(false);
        addMouseMotionListener(this);
        addMouseListener(this);
        display.addMouseListener(this);
        display.addKeyListener(this);
        validate();

        repaint();
        clock.start();
        archinovica = new Archinovica(this);
        display.setVisible(true);
        try{
            loadStates();
        }
        catch(Exception ex){}
    }

    public MenuBar createMenu(){
        MenuBar bar = new MenuBar();
        String[] items = {"Toggle Composition Mode", "Load MIDI", "Export MIDI", "Close MIDI", "Find Legacy Updates", "Print Legacy Pedal Updates"};
        addMenu("File", items, bar);

        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        String[] items1 = new String[info.length + 1];
        for(int i = 0; i < info.length; i++)
            items1[i] = "Set Receiver to: " + i + " - " + info[i].toString();
        items1[info.length] = "USE BUILT IN MIDI";
        addMenu("Choose Receiver", items1, bar);

        String[] items2 = {"Toggle Display Full", "Set Start", "Set End", "Toggle Show Progression"};
        addMenu("Dispay", items2, bar);

        return bar;
    }

    public void addMenu(String title, String[] name, MenuBar a){
        Menu menu = new Menu(title);
        for(String aName: name){
            MenuItem item = new MenuItem(aName);
            item.addActionListener(this);
            menu.add(item);
        }
        a.add(menu);
    }

    public void setFullScreen(JFrame win){
        //win.setVisible(false);
        DisplayMode dm = new DisplayMode(WIDTH, HEIGHT, 16, DisplayMode.REFRESH_RATE_UNKNOWN);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice vc = ge.getDefaultScreenDevice();
        // win.setUndecorated(true); 
        win.setResizable(false);
        vc.setFullScreenWindow(win);  

        //win.addKeyListener(this);
        //addKeyListener(this);
        //vc.addKeyListener(this);

        fontSize = (int)(14 * getHeight() / 600.0);
        font = new Font("Times New Roman", Font.BOLD, fontSize);
        if(dm!=null && vc.isDisplayChangeSupported()){                                 
            try{
                vc.setDisplayMode(dm);                  
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(null,ex.getMessage());
            }
        }

        requestFocus();
    }

    private void addKeyBindings() {
        int condition = JPanel.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = getInputMap(condition );
        ActionMap actionMap = getActionMap();

        KeyStroke spacePressedStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false );
        String spacePressed = "space pressed";
        inputMap.put(spacePressedStroke , spacePressed);
        actionMap.put(spacePressed, new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    LiveReceiver.compositionMode = !LiveReceiver.compositionMode;
                    revalidate();
                    repaint();
                }
            });

        KeyStroke cPressedStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false);
        String cPressed = "c pressed";
        inputMap.put(cPressedStroke , cPressed);
        actionMap.put(cPressed, new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    try{
                        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage capture = new Robot().createScreenCapture(screenRect);
                        ImageIO.write(capture, "bmp", new File("images/image" + new Date() + "bmp"));
                        clearSigns();
                    }catch(Exception e){
                        System.out.println(e);}
                }
            });

        KeyStroke rStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false);
        String rPressed = "r pressed";
        inputMap.put(rStroke , rPressed);
        actionMap.put(rPressed, new AbstractAction(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    try{
                        clearSigns();
                        //LiveReceiver rec = archinovica.liveRec;
                        archinovica = new Archinovica(GUI.this);
                        LiveReceiver.transposition = 0;
                        LiveReceiver.bendAdjustment = 0;
                        //rec.archinovica = archinovica;
                    }catch(Exception e){
                        System.out.println(e);}
                }
            });

        KeyStroke pPressedStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false);
        String pPressed = "p pressed";
        inputMap.put(pPressedStroke , pPressed);
        actionMap.put(pPressed, new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    try{
                        chordProgression.printInfo();
                    }catch(Exception e){
                        System.out.println(e);}
                }
            });

        class Act extends AbstractAction{
            int myState;
            public Act(int state){
                myState = state;
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(states.size() > myState)
                    archinovica.lastSet = states.get(myState);
            }
        }

        for(int n = 0; n < 26; n++){
            KeyStroke stroke = KeyStroke.getKeyStroke(keyCodes[n], InputEvent.SHIFT_DOWN_MASK, false);
            String pressed = alf[n];
            inputMap.put(stroke , pressed);

            actionMap.put(pressed, new Act(n));
        }

    }

    public void loadStates(){
        states = new ArrayList<VerticalSet>();
        TextIO.readFile("setrecords.txt");
        VerticalSet vs = new VerticalSet();
        TextIO.getln();
        while(true){
            try{
                String line = "";
                try{
                    boolean newSet = false;
                    for(int i = 0; i < 2; i++)
                    {
                        line = TextIO.getln();
                        if(line.equals("NEW SET")){
                            states.add(vs);
                            vs = new VerticalSet();
                            newSet = true;
                            break;
                        }
                    }
                    if(newSet)
                        continue;
                }
                catch(Exception e){
                    break;
                }

                int fifths = Integer.parseInt(line);
                try{
                    for(int i = 0; i < 2; i++)
                        line = TextIO.getln();
                }
                catch(Exception e){
                    break;
                }
                int thirds = Integer.parseInt(line);
                vs.add(new RecursiveSearchPoint(new PitchClass(new int[]{fifths, thirds})));
            }
            catch(Exception e){

            }
        }
        states.add(vs);
        for(VerticalSet vs1: states)
            System.out.println(vs1);
    }

    public void displayPitches(PitchClass[] pcs){
        ArrayList<SemioticFunction> signs = new ArrayList<SemioticFunction>();
        for(PitchClass pc: pcs)
            if(pc != null)
                signs.add(pc);

        displaySigns(signs);
    }

    public void displayStaticPitches(PitchClass[] pcs){
        ArrayList<SemioticFunction> signs = new ArrayList<SemioticFunction>();
        for(PitchClass pc: pcs)
            if(pc != null)
                signs.add(pc);

        displayStaticSigns(signs);
    }

    public void displayStaticSign(SemioticFunction s){
        for(ArrayList<Letter> ls: letters)
            for(Letter l: ls)
                l.fade();
        ArrayList<Letter> ls = new ArrayList<Letter>();
        ls.add(new StaticLetter(s));
        letters.add(ls);
        repaint();
        revalidate();
    }

    public void displayStaticSigns(ArrayList<? extends SemioticFunction> s){
        for(ArrayList<Letter> ls: letters)
            for(Letter l: ls)
                l.fade();
        ArrayList<Letter> ls = new ArrayList<Letter>();
        for(SemioticFunction sf:s)
            ls.add(new StaticLetter(sf));
        letters.add(ls);
        repaint();
        revalidate();

    }

    public void clearSigns(){
        letters = new ArrayList<ArrayList<Letter>>();
        miscellaneousLetters = new ArrayList<MiscellaneousLetter>();
        repaint();
        revalidate();
    }

    public void displaySigns(ArrayList<? extends SemioticFunction> signs){
        if(clock.isRunning()) clock.stop();
        // animationQueue.add(signs);
        int a = 0;
        ArrayList<Letter> l = new ArrayList<Letter>();
        for(SemioticFunction s: signs){
            l.add(new Letter(s, new int[]{a, s.signified[1]}, -a));
            a -= 10;
        }
        for(int i = 0; i < letters.size(); i++){
            ArrayList<Letter> ls = letters.get(i);
            for(Letter aLetter: ls)
                if(aLetter.fade()){
                    letters.remove(ls);
                    break;
                }
        }
        letters.add(l);
        animating = true;
        //System.out.println("beginingAnimation" + l.size());
        clock.start();
    }

    public void animateGeneration(SemioticFunction sign, int[] a){
        if(!LiveReceiver.compositionMode)
            miscellaneousLetters.add(new MiscellaneousLetter(sign, new double[]{(double)a[0], (double)a[1]}, (int)(10 * Math.random())));
    }

    public void eraseLastChord(){
        if(letters.size() > 0)
            letters.remove(letters.size() - 1);
        repaint();
        revalidate();
    }

    public int[] getDisplayCoordinates(int[] a){
        return getDisplayCoordinates(new double[]{(double)a[0], (double)a[1]});
    }

    public int[] getDisplayCoordinates(double[] a){
        return new int[]{(int)((getHeight() / 30) * a[1] + getWidth() / 2 + offsets[1]), (int)(getHeight() / 2 - ((getHeight() / 30) * a[0] + offsets[0]))};
    }

    public int[] getCoordinateTotals(ArrayList<SemioticFunction> s){
        int[] totals = {0, 0};
        for(SemioticFunction sign: s){
            for(int i = 0; i < 2; i++)
                totals[i] += sign.signified[i];
        }
        return totals;
    }

    public void paint(Graphics g){
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        if(LiveReceiver.compositionMode){
            g.setColor(black);
            g.drawString("COMPOSITION MODE", (getHeight() / 30), (2 * getHeight() / 30));
        }

        if(chordProgression != null){
            gravity = ARTIFICIAL_GRAVITY;
            bounceFriction = ARTIFICIAL_BF;
            if(showProgression)
                chordProgression.draw(g);
        }
        else{
            gravity = REAL_GRAVITY;
            bounceFriction = REAL_BF;
        }

        //friction = 0.99 * gravity / (double)REAL_GRAVITY;

        numberOfSigns = 0;
        totals = new double[]{0, 0};
        boolean fallingLetters = false;
        for(int i = 0; i < letters.size(); i++){
            ArrayList<Letter> ls = letters.get(i);
            boolean animatedSet = false;
            double[] subTotals = new double[]{0, 0};
            for(Letter aLetter: ls){
                boolean animatedLetter = aLetter.draw(g);
                animatedSet = animatedLetter || animatedSet;
                for(int n = 0; n < 2; n++)
                    totals[n] += aLetter.mySign.signified[n];
                numberOfSigns++;
            }
            /*if(!animatedSet){
            //System.out.println("NONANIMATED SET");
            for(int n = 0; n < 2; n++)
            totals[n] += subTotals[n];
            numberOfSigns += ls.size();
            }
             */
            fallingLetters = animatedSet || fallingLetters;
        }

        //System.out.println("number of Signs: " + numberOfSigns);
        //System.out.println("totals: " + totals[0] + ", " + totals[1]);
        for(int i = 0; i < miscellaneousLetters.size(); i++){
            MiscellaneousLetter ml = miscellaneousLetters.get(i);
            try{
                if(!ml.draw(g) && ml.fade()){
                    miscellaneousLetters.remove(ml);
                    i--;
                }
            }
            catch(java.lang.NullPointerException e){
                //System.out.println("NULL POINTER:");
                //System.out.println(miscellaneousLetters);
            }
        }
        animating = fallingLetters || miscellaneousLetters.size() > 0;
        /*
        for(ArrayList<SemioticFunction> signs: mySigns){

        g.setColor(new Color(255, 255, 255, 95));
        g.fillRect(0, 0, 600, 600);

        g.setColor(black);
        for(SemioticFunction s: signs){
        int x = s.signified[1];
        int y = s.signified[0];
        g.drawString(NOTE_NAMES[s.signifier], (x * (getHeight() / 30) + (int)offsets[1]) + 300, 300 - (y * (getHeight() / 30) + (int)offsets[0]));
        numberOfSigns++;
        totals[1] += x;
        totals[0] += y;
        }

        }
        if(animating){
        boolean isAnimating = false;
        for(FallingLetter f: fallingLetters){
        System.out.println("DrawingLetter");
        isAnimating =  f.draw(g) || isAnimating;
        }
        animating = isAnimating;
        if(!animating){
        ArrayList<SemioticFunction> signs = new ArrayList<SemioticFunction>();
        for(FallingLetter f: fallingLetters)
        signs.add(f.mySign);
        mySigns.add(signs);
        if(mySigns.size() > 8)
        mySigns.remove(0);
        System.out.println("MYSIGNS: " + mySigns);
        fallingLetters = new ArrayList<FallingLetter>();

        }
        }
         */

        if(chordProgression != null)
            chordProgression.highlightIndex(g);

        if(!LiveReceiver.compositionMode)
            return;

        if(numberOfSigns == 0){
            g.setColor(black);
            g.setFont(font);
            g.drawString("PEDALING: " + archinovica.pedaling, (getHeight() / 30), (getHeight() / 30));
            //g.drawString("FONT SIZE:" + fontSize , 100, 100);
            return;
        }
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString("PEDALING: " + archinovica.pedaling, (getHeight() / 30), (getHeight() / 30));

        // g.drawString("FONT SIZE:" + fontSize , 100, 100);
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public void updateProjection(){
        clearSigns();
        archinovica = new Archinovica(GUI.this);
        LiveReceiver.transposition = 0;
        LiveReceiver.bendAdjustment = 0;

        chordProgression.project(displayFullProjection);
    }

    public void actionPerformed(ActionEvent e){
        String aCom = e.getActionCommand();
        if(aCom != null && aCom.equals("Toggle Composition Mode")){
            LiveReceiver.compositionMode = !LiveReceiver.compositionMode;
            clearSigns();
        }
        if(aCom != null && aCom.equals("Load MIDI")){
            legacyProgressions = new ArrayList<LegacyChordProgression>();
            JFileChooser jchooser = new JFileChooser();
            jchooser.showOpenDialog(this);
            File midiFile = jchooser.getSelectedFile();
            try{
                sequence = MidiSystem.getSequence(midiFile);
                System.out.println("NUMBER OF TRACKS: " + sequence.getTracks().length);
                chordProgression = new ChordProgression(sequence);

                updateProjection();
            }
            catch(Exception ex){
                System.out.println(ex);
            }

        }
        if(aCom != null && aCom.equals("Export MIDI")){
            if(sequence == null)
                return;
            JFileChooser jchooser = new JFileChooser();
            jchooser.showSaveDialog(this);
            File midiFile = jchooser.getSelectedFile();
            myRec.initializeOutFile(sequence);
            myRec.recordOutput = true;

            clearSigns();
            archinovica = new Archinovica(GUI.this);
            LiveReceiver.transposition = 0;
            LiveReceiver.bendAdjustment = 0;

            chordProgression.record();
            myRec.recordOutput = false;
            try{

                MidiSystem.write(myRec.outFile, 1, midiFile);
            }
            catch(Exception ex){System.out.println(ex);}
        }
        if(aCom != null && aCom.equals("Close MIDI")){
            chordProgression = null;
        }
        if(aCom != null && aCom.equals("Toggle Show Progression")){
            showProgression = !showProgression;
        }
        if(aCom != null && aCom.equals("Find Legacy Updates")){
            chordProgression.findNeededUpdates();
        }
        if(aCom != null && aCom.equals("Print Legacy Pedal Updates")){
            for(LegacyChordProgression lcp: legacyProgressions)
                System.out.println(lcp);

        }
        if(aCom != null && aCom.startsWith("Set Receiver to: ")){
            MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
            try{
                MidiDevice.Info inf = info[Integer.parseInt(aCom.substring(17, aCom.indexOf(" - ")))];
                System.out.println(inf);
                MidiDevice dev = MidiSystem.getMidiDevice(inf);
                System.out.println("DEV = " + dev);
                System.out.println(archinovica);
                //System.out.println(archinovica.liveRec);
                //archinovica.liveRec.setReceiver(dev);
            }
            catch(Exception ex){System.out.println("ERROR IN SETTING: " + ex);}
        }
        if(aCom != null && aCom.startsWith("USE BUILT IN MIDI")){
            myRec.useBuiltInMidi();
            System.out.println("USE BUILTIN");
        }
        if(aCom != null && aCom.startsWith("Toggle Display Full")){
            displayFullProjection = !displayFullProjection;
            updateProjection();
        }
        if(aCom != null && aCom.startsWith("Set Start")){
            if(chordProgression != null)
                chordProgression.setStart();
            updateProjection();
        }
        if(aCom != null && aCom.startsWith("Set End")){
            if(chordProgression != null)
                chordProgression.setEnd();
            updateProjection();
        }
        repaint();
        display.revalidate();
        if(numberOfSigns == 0)
            return;

        if(!autoCenter)
            return;

        int a = 0;
        int b = 0;
        for(int i = 0; i < 2; i++){

            netCenter[i] = totals[i] / numberOfSigns;
            double centerCoordinate = netCenter[i]  * (getHeight() / 30.0) + offsets[i];
            //System.out.println("CENTER CORD: " + centerCoordinate);
            // if((centerCoordinate * (getHeight() / 30) + offsets[i]) != 0)
            offsets[i] += velocity[i];
            velocity[i] -= centerCoordinate / 500.0;
            velocity[i] *= friction;
            a += Math.pow(velocity[i], 2);
            b += Math.pow(centerCoordinate, 2);
        }
        //System.out.println("VELOCITY: " + Math.sqrt(a));
        if(!clock.isRunning() && (Math.sqrt(b) >= 2 || Math.sqrt(a) >= 0.001)){
            //System.out.println("StartingClock");
            clock.start();
        }
        if(Math.sqrt(b) < 2 && Math.sqrt(a) < 0.001){
            velocity = new double[]{0, 0};
            if(clock.isRunning() && !animating){
                //System.out.println("StopingClocK");
                clock.stop();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        clickPoint = new int[]{e.getX(), e.getY()};
        autoCenter = false;
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {
        //animating = true;
    }

    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e){

        try{
            offsets[1] += e.getX() - clickPoint[0];
            offsets[0] -= e.getY() - clickPoint[1];
        }catch(NullPointerException ex){}
        clickPoint = new int[]{e.getX(), e.getY()};
        validate();
        repaint();
        display.revalidate();
    }

    public void mouseMoved(MouseEvent e){

    }

    public void keyTyped(KeyEvent e){}

    public void keyPressed(KeyEvent e){
        // System.out.println(e.getKeyCode());
        validate();
        repaint();
        switch(e.getKeyCode()){
            case KeyEvent.VK_OPEN_BRACKET:
            //System.out.println("A");
            if(chordProgression != null){
                chordProgression.addPedal(true, false);

                updateProjection();
            }
            break;
            case KeyEvent.VK_CLOSE_BRACKET:
            if(chordProgression != null){
                chordProgression.addPedal(false, false);

                updateProjection();
            }
            break;
            case KeyEvent.VK_RIGHT:
            if(chordProgression != null){
                chordProgression.next();
            }
            break;
            case KeyEvent.VK_LEFT:
            if(chordProgression != null){
                chordProgression.previous();
            }
            break;
            case KeyEvent.VK_Z:
            setFullScreen(display);
            break;
            case KeyEvent.VK_BACK_SPACE:
            if(chordProgression != null){
                chordProgression.delete();

                updateProjection();
            }
            break;
            case KeyEvent.VK_X:
            if(chordProgression != null){

                chordProgression.addPedal(false, true);
                chordProgression.addPedal(true, true);

                updateProjection();
            }
            break;
        }

    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SHIFT){
        }
        if(e.isShiftDown()){
            for(int i = 0; i < 26; i++){
                if(e.getKeyCode() == keyCodes[i]){
                    if(states.size() > i)
                    {
                        VerticalSet vs = states.get(i);
                        archinovica.lastSet = vs;

                        //vs.analyseIntervals(vs); //needs to be ordered?

                        archinovica.soundingPitchClasses = archinovica.lastSet.getArrayBySignified();
                    }
                }
            }
        }

    }

    public File openMIDI(){
        JFileChooser b = new JFileChooser();
        File midiFile = null;
        if(b.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            midiFile = b.getSelectedFile();
        return midiFile;
    }

    public class Letter{
        public double[] myCoordinates;
        public double[] myVelocity;
        public int[] dispXY;
        public int alpha, drawCount, timeStamp;
        public Color myColor;
        public SemioticFunction mySign;
        public String name;
        public boolean animatingThis, bounced;
        public Font myFont;

        public Letter(){}

        public Letter(SemioticFunction s, int[] a, int t){
            myCoordinates = new double[]{(int)a[0], (int)a[1]};
            dispXY = getDisplayCoordinates(myCoordinates);
            myVelocity = new double[]{0, 0};
            myColor = Color.BLUE;
            alpha = 255;
            name = NOTE_NAMES[s.signifier];
            animatingThis = true;
            bounced = false;
            mySign = s;
            myFont = new Font("Times New Roman", Font.PLAIN, fontSize);
            timeStamp = t;
            drawCount = 0;
        }

        public void highlight(){
            myColor = Color.BLUE;
            myFont = new Font("Times New Roman", Font.BOLD, fontSize + 4);
        }

        public void unhighlight(){
            myColor = Color.BLACK;
            myFont = new Font("Times New Roman", Font.PLAIN, fontSize);
        }

        public boolean draw(Graphics g){
            drawCount++;
            if(drawCount < timeStamp)
                return true;
            updateValues();
            Color color = g.getColor();
            g.setFont(myFont);
            g.setColor(myColor);

            g.drawString(name, dispXY[0], dispXY[1]);
            //System.out.println("DISPLAY " + dispXY[0] +"," + dispXY[1]);
            g.setFont(font);
            g.setColor(color);
            return animatingThis;
        }

        public void updateValues(){

            int red = myColor.getRed();
            if(red > 0){
                red += 1;
                red /= 2;
                red -= 1;
                myFont = new Font("Times New Roman", Font.BOLD, fontSize);
            }
            else if(animatingThis)
                myFont = myFont = new Font("Times New Roman", Font.PLAIN, fontSize);
            else
                myFont = new Font("Times New Roman", Font.BOLD, fontSize);

            if(!bounced){
                if(drawCount - timeStamp <= 100)
                    alpha = 255 * (drawCount - timeStamp) / 100;
                myColor = new Color(red / 2, myColor.getGreen(), 127 - red / 2, alpha);

            }
            else{
                myColor = new Color(red, myColor.getGreen(), myColor.getBlue(), alpha);
            }

            if(!animatingThis){
                dispXY = getDisplayCoordinates(mySign.signified);
                return;
            }
            dispXY = new int[]{getDisplayCoordinates(myCoordinates)[0], (int)myCoordinates[0]};
            myVelocity[0] += gravity;
            myCoordinates[0] += myVelocity[0];
            if(myCoordinates[0] >= getHeight() / 2 - ((getHeight() / 30) * mySign.signified[0] + offsets[0])){
                myVelocity[0] = -bounceFriction * myVelocity[0];
                velocity[0] += myVelocity[0] * bounceFriction;
                myCoordinates[0] = getHeight() / 2 - ((getHeight() / 30) * mySign.signified[0] + offsets[0]) + myVelocity[0];
                //System.out.println("BOUNCE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                if(!bounced)
                    alpha = 255;
                bounced = true;
                if(Math.random() < 0.5)
                    myColor = new Color(255, 0, 0, alpha);
                else
                    myColor = new Color(0, 0, 0, alpha);
                if(Math.abs(myVelocity[0]) < 0.5){
                    endAnimation();
                }
            }
            //System.out.println("ANIMATINGTHIS: " + animatingThis);
            return;
        }

        public void endAnimation(){
            myColor = new Color(0,0,0, alpha);
            myFont = new Font("Times New Roman", Font.BOLD, fontSize);
            animatingThis = false;
        }

        public boolean fade(){
            myColor = new Color(0,0,0, alpha);
            alpha = (int)(alpha / 1.1);
            return alpha < 4;
        }
    }

    public class StaticLetter extends Letter{
        public StaticLetter(SemioticFunction s){
            timeStamp = 0;
            drawCount = 0;
            mySign = s;
            animatingThis = true;
            myFont = new Font("Times New Roman", Font.BOLD, fontSize);
            myColor = Color.BLACK;
            name = NOTE_NAMES[s.signifier];
            alpha = 255;
        }

        @Override
        public void updateValues(){
            dispXY = getDisplayCoordinates(mySign.signified);
        }

        @Override
        public boolean fade(){
            myColor = new Color(0,0,0, alpha);
            alpha = (int)(alpha / 1.03);
            myFont = new Font("Times New Roman", Font.PLAIN, fontSize);
            return alpha < 4;
        }
    }

    public class MiscellaneousLetter extends Letter{
        double[] initialCoordinates, finalCoordinates;
        double theta;
        public MiscellaneousLetter(SemioticFunction s, double[] a, int t){
            mySign = s;
            myCoordinates = new double[]{a[0], a[1]};
            initialCoordinates = new double[]{a[0], a[1]};
            finalCoordinates = new double[]{mySign.signified[0], mySign.signified[1]};
            animatingThis = true;
            alpha = 0;
            myFont = new Font("Times New Roman", Font.PLAIN, fontSize);
            myColor = Color.BLACK;
            name = NOTE_NAMES[s.signifier];
            timeStamp = t;
            drawCount = 0;
            myVelocity = new double[]{0, 0};
            theta = Math.atan2(finalCoordinates[0] - myCoordinates[0], finalCoordinates[1] - myCoordinates[1]);
            double vNaught = 0.05;
            myVelocity[0] = vNaught * Math.sin(theta);
            myVelocity[1] = vNaught * Math.cos(theta);
            //System.out.println("ALPHA = " + alpha + " | distance = " + Math.sqrt(a));
            if(name.equals("E ")){
                //System.out.println(finalCoordinates[0] + ", " + finalCoordinates[1] + " | " + myCoordinates[0] + ", " + myCoordinates[1]);
                //System.out.println("VELOCITY VECTOR: " + myVelocity[0] + ", " + myVelocity[1]);

            }// if(Math.sqrt(Math.pow(mySign.signified[0] - myCoordinates[0], 2) + Math.pow(mySign.signified[1] - myCoordinates[1], 2)) > 1)

        }

        public void updateValues(){
            myColor = new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), alpha);
            if(animatingThis){
                double a = 0;
                double b = 0;
                for(int n = 0; n < 2; n++){
                    a += Math.pow(finalCoordinates[n] - myCoordinates[n], 2);
                    b += Math.pow(myVelocity[n], 2);
                }
                if(Math.sqrt(a) <= Math.sqrt(b)){
                    //System.out.println("END");
                    endAnimation();
                }
                else
                    alpha = (int)(32 - 32 * Math.sqrt(a));

            }
            else{
                myVelocity[0] -= gravity;
            }

            for(int n = 0; n < 2; n++)
                myCoordinates[n] += myVelocity[n];
            dispXY = getDisplayCoordinates(new double[]{myCoordinates[0], myCoordinates[1]});
        }

        public void endAnimation(){
            animatingThis = false;
            myFont = new Font("Times New Roman", Font.BOLD, fontSize);
            double v = 0.1;
            myVelocity[0] = v * Math.sin(theta);
            myVelocity[1] = v * Math.cos(theta);

        }

    }

    public class ChordProgression extends ArrayList<Chord>{
        Sequence seq;
        int index, startDisplay, endDisplay;
        ArrayList<MidiEvent[]> pitchBendRecord;

        ChordProgression(Sequence s){

            pitchBendRecord = new ArrayList<MidiEvent[]>();
            MidiEvent[] initialSet = new MidiEvent[12];
            pitchBendRecord.add(initialSet);
            seq = s;
            Track[] tracks = seq.getTracks();
            for(int n = tracks.length - 1; n < tracks.length; n++){
                Track t = tracks[n];
                for(int i = 0; i < t.size(); i++)
                    if(t.get(i).getMessage() instanceof ShortMessage){
                        ShortMessage sm = (ShortMessage)t.get(i).getMessage();
                        if(sm.getCommand() == ShortMessage.NOTE_ON
                        || sm.getCommand() == ShortMessage.NOTE_OFF
                        || (sm.getCommand() == ShortMessage.CONTROL_CHANGE
                            && (sm.getData1() == LiveReceiver.RIGHTPED
                                || sm.getData1() == LiveReceiver.LEFTPED))){
                            addEvent(t.get(i));
                            if(sm.getCommand() == ShortMessage.NOTE_ON)
                                System.out.println("NOTE ON: " + sm.getData1());
                        }
                        else{
                            /*System.out.println("C: " + sm.getCommand()
                            + " | D1: " + sm.getData1() + " | D2: " + sm.getData2());
                             */
                        }
                    }
                    else if(t.get(i).getMessage() instanceof SysexMessage){
                        SysexMessage sysex = (SysexMessage)t.get(i).getMessage();
                        for(byte b: sysex.getData())
                            System.out.print(b + ", ");
                        System.out.println();
                    }
            }
            System.out.println("NUMBER OF ORIGNINAL TRACKS: " + tracks.length);
            /*
            for(int n = 0; n < tracks.length - 2; n++){

            }
             */
            index = 0;
            startDisplay = 0;
            endDisplay = size();
        }

        public void setStart(){
            startDisplay = index;
        }

        public void setEnd(){
            endDisplay = index;
        }

        public void project(boolean projectFull){
            double actualGravity = gravity;
            gravity = 0.5; // "artificial gravity"

            boolean r = false;
            boolean l = false;
            int terminus = size();
            if(!projectFull)
                terminus = endDisplay;
            for(int i = startDisplay; i < terminus; i++){
                Chord c = get(i);
                if(c.lChange != 0 || c.rChange != 0){
                    if(c.lChange == 1)
                        l = true;
                    else if(c.lChange == -1)
                        l = false;
                    if(c.rChange == 1)
                        r = true;
                    else if(c.rChange == -1)
                        r = false;
                    int ped = 0;
                    if(r)
                        ped += 2;
                    if(l)
                        ped += 1;
                    archinovica.setPedaling(ped);
                }
                if(!c.noPitches()){
                    archinovica.updateIntonation(c.set);
                    c.setAssociatedSigns();
                }
            }
            gravity = actualGravity;
        }

        public void findNeededUpdates(){

            clearSigns();
            archinovica = new Archinovica(GUI.this);
            LiveReceiver.transposition = 0;
            LiveReceiver.bendAdjustment = 0;

            Archinovica.legacyBehavior = false;

            boolean r = false;
            boolean l = false;

            for(int i = startDisplay; i < endDisplay; i++){
                Chord c = get(i);
                if(c.lChange != 0 || c.rChange != 0){
                    if(c.lChange == 1)
                        l = true;
                    else if(c.lChange == -1)
                        l = false;
                    if(c.rChange == 1)
                        r = true;
                    else if(c.rChange == -1)
                        r = false;
                    int ped = 0;
                    if(r)
                        ped += 2;
                    if(l)
                        ped += 1;
                    archinovica.setPedaling(ped);
                }
                if(!c.noPitches()){
                    PitchSet reference = archinovica.lastSet;
                    archinovica.updateIntonation(c.set);
                    c.setAssociatedSigns();
                    c.setAssociatedPitchSet();
                    if(reference != null)
                        c.setRelativeCoordinates(reference);
                }
            }

            clearSigns();
            archinovica = new Archinovica(GUI.this);
            LiveReceiver.transposition = 0;
            LiveReceiver.bendAdjustment = 0;

            Archinovica.legacyBehavior = true;
            r = false;
            l = false;
            boolean firstChord = true;
            int chordIndex = 0;

            for(int i = startDisplay; i < endDisplay; i++){
                Chord c = get(i);
                if(c.lChange != 0 || c.rChange != 0){
                    if(c.lChange == 1)
                        l = true;
                    else if(c.lChange == -1)
                        l = false;
                    if(c.rChange == 1)
                        r = true;
                    else if(c.rChange == -1)
                        r = false;
                    int ped = 0;
                    if(r)
                        ped += 2;
                    if(l)
                        ped += 1;
                    archinovica.setPedaling(ped);
                }
                if(!c.noPitches()){
                    chordIndex++;
                    PitchSet reference = archinovica.lastSet;
                    int[] oldRelativeCoordinates = new int[2];
                    for(int n = 0; n < 2; n++)
                        oldRelativeCoordinates[n] = c.relativeCoordinates[n];
                    archinovica.updateIntonation(c.set);
                    c.setAssociatedSigns();
                    c.setAssociatedPitchSet();
                    if(!firstChord){
                        c.setRelativeCoordinates(reference);
                        for(int n = 0; n < 2; n++)
                            if(oldRelativeCoordinates[n] != c.relativeCoordinates[n]){
                                c.needsUpdate();
                                legacyProgressions.add(new LegacyChordProgression(reference, c.set, c.relativeCoordinates, chordIndex));
                            }
                    }
                    firstChord = false;
                }
            }
            Archinovica.legacyBehavior = false;
        }

        public boolean addPedal(boolean isLeft, boolean lift){
            if(index == 0)
                return false;
            try{
                int num = 0;
                if(isLeft)
                    num = LiveReceiver.LEFTPED;
                else
                    num = LiveReceiver.RIGHTPED;
                int d2 = 127;
                if(lift)
                    d2 = 0;
                ShortMessage aSM = new ShortMessage(ShortMessage.CONTROL_CHANGE, 0,num, d2);
                MidiEvent me = new MidiEvent(aSM, get(index).timeStamp - 1L);
                addEvent(me);
                index++;
            }
            catch(InvalidMidiDataException ex){
                // System.out.println("ERROR: " + e);
            }
            return true;
        }

        public boolean delete(){
            if(size() == 1 || !get(index).noPitches())
                return false;
            remove(index);
            if(index >= size())
                index = size() - 1;
            return true;
        }

        public void printInfo(){
            System.out.println(get(index));
        }

        public void next(){
            index++;
            if(index == size())
                index = 0;

        }

        public void previous(){
            index--;
            if(index == -1)
                index = size() - 1;
        }

        public void addEvent(MidiEvent me){
            int index = Collections.binarySearch(this, me);
            if(index < 0){
                index = -index - 1;
                add(index, new Chord(me));
                return;
            }
            get(index).addEvent(me);
        }

        public void recordPitchBend(ShortMessage pbM, long timeStamp){
            int index = getPitchBendSetIndex(timeStamp);
            MidiEvent[] pitchBendSet = new MidiEvent[12];
            if(index > 0){
                MidiEvent[] previousPBS = pitchBendRecord.get(index - 1);
                for(int i = 0; i < 12; i++){
                    pitchBendSet[i] = previousPBS[i];
                }
            }

            for(MidiEvent me: pitchBendRecord.get(index)){
                if(me != null){
                    if(me.getTick() == timeStamp)
                        pitchBendSet = pitchBendRecord.get(index);
                    break;
                }
            }
            /*
            MidiEvent[] lastPitchBendSet = pitchBendRecord.get(index);
            MidiEvent[] pitchBendSet = new MidiEvent[12];
            for(int i = 0; i < 12; i++){
            if(lastPitchBendSet[i] == null)
            continue;
            ShortMessage lastMessage = (ShortMessage)lastPitchBendSet[i].getMessage();
            MidiEvent event = new MidiEvent(lastMessage, timeStamp);
            pitchBendSet[i] = event;
            }
             */
            int pitchToBend = pbM.getChannel();
            if(pitchToBend == 13)
                pitchToBend = 9;
            MidiEvent event = new MidiEvent(pbM, timeStamp);
            pitchBendSet[pitchToBend] = event;

            pitchBendRecord.add(index + 1, pitchBendSet);

        }

        public MidiEvent[] getPitchBendSet(long timeStamp){
            return pitchBendRecord.get(getPitchBendSetIndex(timeStamp));
        }

        public int getPitchBendSetIndex(long timeStamp){
            long currentTime = 0L;
            int currentIndex = 0;
            for(int i = 0; i < pitchBendRecord.size(); i++){
                MidiEvent[] pbSet = pitchBendRecord.get(i);
                for(MidiEvent me: pbSet){
                    if(me != null){
                        currentTime = me.getTick();
                        break;
                    }
                }
                if(currentTime > timeStamp)
                    return currentIndex;
                currentIndex = i;
            }
            return currentIndex;
        }

        public void record(){
            Track[] tracks = seq.getTracks();
            //getRecordState messages;
            int archiTrack = tracks.length - 1;
            ArrayList<Long> recStateStamps = new ArrayList<Long>();
            for(int n = 0; n < tracks[archiTrack].size(); n++){
                MidiEvent event = tracks[archiTrack].get(n);
                if(event.getMessage() instanceof SysexMessage){
                    SysexMessage sysex = (SysexMessage)event.getMessage();
                    byte[] data = sysex.getData();
                    if(data[0] == 0X01){
                        recStateStamps.add(event.getTick());
                    }
                }
            }
            ArrayList<String> setRecords = new ArrayList<String>();
            for(Chord c: this){
                boolean l = false;
                boolean r = false;
                if(c.lChange != 0 || c.rChange != 0){
                    if(c.lChange == 1)
                        l = true;
                    else if(c.lChange == -1)
                        l = false;
                    if(c.rChange == 1)
                        r = true;
                    else if(c.rChange == -1)
                        r = false;
                    int ped = 0;
                    if(r)
                        ped += 2;
                    if(l)
                        ped += 1;
                    archinovica.setPedaling(ped);
                }
                /*myRec.commands.addAll(c);
                myRec.playNotes(c.timeStamp);
                 */
                if(recStateStamps.size() > 0 && recStateStamps.get(0) <= c.timeStamp){
                    String lastSetRecord = archinovica.lastSet.getSetRecord();
                    setRecords.add(lastSetRecord);
                    recStateStamps.remove(0);
                }
                for(ShortMessage message: c)
                    myRec.send(message, c.timeStamp);
                myRec.playNotes(c.timeStamp);
            }

            Track[] outTrks = myRec.outFile.getTracks();
            for(int i = 0; i < tracks.length - 1; i++){
                for(int n = 0; n < tracks[i].size(); n++){
                    MidiEvent event = tracks[i].get(n);
                    if(!(event.getMessage() instanceof ShortMessage))
                        continue;
                    ShortMessage sm = (ShortMessage)event.getMessage();

                    int trackNum = i + 16;
                    if(sm.getCommand() == ShortMessage.NOTE_ON
                    && trackContainsNoteOnAt(event.getTick(), outTrks[trackNum])){
                        trackNum += tracks.length - 1;
                    }
                    outTrks[trackNum].add(event);

                    //add pitchBend
                    if(sm.getCommand() == ShortMessage.NOTE_ON){
                        long timeStamp = event.getTick();
                        MidiEvent[] pbSet = getPitchBendSet(timeStamp);
                        MidiEvent pbEvent = pbSet[sm.getData1() % 12];
                        if(pbEvent != null){
                            pbEvent = new MidiEvent(pbEvent.getMessage(), timeStamp);
                            outTrks[trackNum].add(pbEvent);
                        }
                    }

                    //just in case
                    if(sm.getCommand() == ShortMessage.NOTE_OFF ||
                    (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)){
                        outTrks[i + 16 + tracks.length - 1].add(event);
                    }

                }
            }
            //tracks = seq.getTracks();
            outTrks = myRec.outFile.getTracks();
            //handel metadata
            for(int n = 0; n < tracks[0].size(); n++){
                if(!(tracks[0].get(n).getMessage() instanceof ShortMessage))
                    outTrks[0].add(tracks[0].get(n));
            }
            for(int i = 0; i < tracks.length; i++){
                for(int n = 0; n < tracks[i].size(); n++){
                    MidiEvent event = tracks[i].get(n);
                    if(!(event.getMessage() instanceof ShortMessage
                        && ((ShortMessage)event.getMessage()).getCommand() == ShortMessage.PROGRAM_CHANGE))
                        continue;
                    if(i < tracks.length - 1){
                        outTrks[16 + i].add(event); 
                        outTrks[16 + i + tracks.length - 1].add(event); 
                    }
                    else
                        for(int q = 0; q < 16; q++){
                            outTrks[q].add(event); 
                        }
                }
            }

            //handel sysex messages
            /*
             * first byte describes message:
             *  0x00 � sustain message
             *      second byte:
             *          0x00 - sus on
             *          0x01 - sus off
             */
            //archiTrack = tracks.length - 1;
            for(int n = 0; n < tracks[archiTrack].size(); n++){
                MidiEvent event = tracks[archiTrack].get(n);
                if(event.getMessage() instanceof SysexMessage){
                    SysexMessage sysex = (SysexMessage)event.getMessage();
                    byte[] data = sysex.getData();
                    switch(data[0]){
                        case 0x00:
                        try{
                            MidiMessage susMessage = new ShortMessage(ShortMessage.CONTROL_CHANGE,0,64,127);
                            if(data[1] == 0x01)
                                susMessage = new ShortMessage(ShortMessage.CONTROL_CHANGE,0,64,0);

                            MidiEvent susEvent = new MidiEvent(susMessage, event.getTick());
                            for(int q = 0; q < 16; q++){
                                outTrks[q].add(susEvent); 
                            }
                        }
                        catch(Exception exception){}
                        break;
                    }
                }
            }

            if(setRecords.size()  > 0){
                if(JOptionPane.showConfirmDialog(null, "Would you like to export Archinovica states?") == JOptionPane.YES_OPTION){
                    TextIO.writeFile("setrecords.txt");
                    for(String set: setRecords){
                        TextIO.putln("NEW SET");
                        TextIO.putln(set);
                    }
                    setRecords = new ArrayList<String>();
                }
            }

        }

        public boolean trackContainsNoteOnAt(long timeStamp, Track track){
            boolean noteIsOn = false;
            for(int i = 0; i < track.size(); i++){
                ShortMessage sm = null;
                if(track.get(i).getMessage() instanceof ShortMessage)
                    sm = (ShortMessage)track.get(i).getMessage();
                long time = track.get(i).getTick();

                if(time == timeStamp && sm != null &&
                sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() != 0)
                    return true;

                if(time > timeStamp)
                    return false;
            }
            return false;
        }

        public void highlightIndex(Graphics g){
            int terminus = size();
            if(!displayFullProjection)
                terminus = endDisplay;
            for(int i = 0; i < terminus; i++){
                Chord c = get(i);
                c.unhighlightSigns(g);
            }
            get(index).highlightSigns(g);
        }

        public void printIndexInfo(){
            System.out.println(get(index));
        }

        public void draw(Graphics g){
            int x = 30;
            int y = 30;

            for(int i = 0; i < size(); i++){
                Chord c = get(i);
                Color color = Color.BLACK;
                int n = i - 1;
                while(n > 0 && get(n).noPitches())
                    n--;
                if(n > -1 && !c.hasCommonTones(get(n)))
                    color = Color.RED;
                if(i == index)
                    color = Color.BLUE;
                c.draw(g, x, y, color);
                if(x < getWidth() - 50)
                    x += 50;
                else{
                    x = 30;
                    y += 150;
                }
            }
            if(x < getWidth() - 50)
                x += 50;
            else{
                x = 30;
                y += 150;
            }
            g.setColor(Color.BLACK);
            //g.drawString("ECONOMY: " + numberOfSigns, x, y);
        }
    }

    public class Chord extends ArrayList<ShortMessage> implements Comparable<MidiEvent>{
        public long timeStamp;
        public String name;
        public boolean[] set;
        public boolean isGreen;
        public int lChange, rChange;
        public int[] relativeCoordinates;
        public ArrayList<Letter> signs;
        public PitchSet myPitchSet;

        Chord(MidiEvent me){
            lChange = 0;
            rChange = 0;
            set = new boolean[12];
            //add((ShortMessage)me.getMessage());
            name = "";
            addEvent(me);
            timeStamp = me.getTick();
            ShortMessage sm = (ShortMessage)me.getMessage();
            name = "";
            name = getName(sm);
            relativeCoordinates = new int[2];
            isGreen = false;
        }

        public void addEvent(MidiEvent me){
            ShortMessage sm = (ShortMessage)me.getMessage();
            if(sm.getCommand() == ShortMessage.NOTE_ON
            || sm.getCommand() == ShortMessage.NOTE_OFF){
                int chnl = sm.getData1() % 12;
                if(chnl == 9)
                    chnl = 13;
                try{
                    sm.setMessage(sm.getCommand(), chnl, sm.getData1(), sm.getData2());
                }
                catch(Exception ex){
                    System.out.println(ex);
                }
            }

            add(sm);
            String n = getName(sm);
            if(!name.contains(n))
                name += n;
        }

        public String getName(ShortMessage sm){
            switch(sm.getCommand()){
                case ShortMessage.NOTE_ON:
                set[sm.getData1() % 12] = true;
                return "";
                case ShortMessage.NOTE_OFF:
                //set[sm.getData1() % 12] = false;?
                return "";
                case ShortMessage.CONTROL_CHANGE:
                switch(sm.getData1()){
                    case LiveReceiver.RIGHTPED:
                    if(sm.getData2() > 0){
                        rChange = 1;
                        return"R+";
                    }
                    else{
                        rChange = -1;
                        return "R-";
                    }
                    case LiveReceiver.LEFTPED:
                    if(sm.getData2() > 0){
                        lChange = 1;
                        return "L+";
                    }
                    else{
                        lChange = -1;
                        return"L-";
                    }
                }
                break;
            }
            return null;
        }

        @Override
        public String toString(){
            String s = "\n";
            double[] pbs = new double[12];
            int[][] signifieds = new int[12][2];
            for(Letter l: signs){
                pbs[l.mySign.signifier] = l.mySign.getMidiPb();
                signifieds[l.mySign.signifier][0] = l.mySign.signified[0];
                signifieds[l.mySign.signifier][1] = l.mySign.signified[1];
            }

            for(ShortMessage sm: this){
                if(sm.getCommand() != ShortMessage.NOTE_ON)
                    continue;
                int midiNumber = sm.getData1();
                double pb = pbs[midiNumber % 12];
                s += NOTE_NAMES[midiNumber % 12] + ": " + 440 * Math.pow(2, (midiNumber - 69) / 12.0) * Math.pow(2, 2 * (pb - 64) / (12.0 * 64.0)) 
                + " [" + signifieds[midiNumber % 12][0] + ", " + signifieds[midiNumber % 12][1] + "]\n";// + "; pb: " + pb + "\n";
            }
            return s;
        }

        public void setAssociatedSigns(){
            signs = letters.get(letters.size() - 1);
        }

        public void setAssociatedPitchSet(){
            myPitchSet = archinovica.lastSet;
        }

        public void setRelativeCoordinates(PitchSet pointOfReference){
            relativeCoordinates = myPitchSet.getCenter().getTransformation(pointOfReference.getCenter());
        }

        public void highlightSigns(Graphics g){
            if(signs != null)
                for(Letter l: signs){
                    l.highlight();
                    l.draw(g);
                }
        }

        public void unhighlightSigns(Graphics g){
            if(signs != null)
                for(Letter l: signs){
                    l.unhighlight();
                    //if(letters.contains(signs))
                    l.draw(g);
                }
        }

        public void needsUpdate(){
            isGreen = true;
            try{
                ShortMessage sm = new ShortMessage(ShortMessage.NOTE_ON, 14, 108, 127);
                add(sm);
            }
            catch(Exception ex){
                System.out.println(ex);
            }
        }

        @Override
        public int compareTo(MidiEvent me){
            if(timeStamp > me.getTick())
                return 1;
            if(timeStamp < me.getTick())
                return -1;
            return 0;
        }

        public void draw(Graphics g, int x, int y, Color c){

            Color color = g.getColor();
            g.setColor(c);
            if(isGreen)
                g.setColor(Color.GREEN);
            String displayName = name;
            for(int i = 0; i < 12; i++)
                if(set[i])
                    displayName += "\n" + NOTE_NAMES[i];
            if(name.length() > 0){
                g.drawString(name, x, y);
                y += 15;
            }
            for(int i = 11; i >= 0; i--)
                if(set[i]){
                    g.drawString(NOTE_NAMES[i], x, y);
                    y += 15;
                }
            //System.out.println("DISPLAY " + dispXY[0] +"," + dispXY[1]);
            g.setFont(font);
            g.setColor(color);
        }

        public boolean hasCommonTones(Chord c){
            for(int i = 0; i < 12; i++)
                if(set[i] && c.set[i])
                    return true;
            return false;
        }

        public boolean noPitches(){
            for(int i = 0; i < 12; i++)
                if(set[i])
                    return false;
            return true;
        }
    }

    class LegacyChordProgression{
        PitchSet mySource;
        boolean targetPitches[];
        int[] myDesiredRelativeCoordinates;
        int myIndex;

        LegacyChordProgression(PitchSet source, boolean[] target, int[] desiredRelativeCoordinates, int index){
            mySource = source;
            targetPitches = target;
            myDesiredRelativeCoordinates = desiredRelativeCoordinates;
            myIndex = index;
        }

        public int findCorrectPedaling(){
            Archinovica.legacyBehavior = false;
            for(int i = 0; i < 4; i++){
                Archinovica anArch = new Archinovica(null);
                anArch.setLastChord(mySource.clone());
                anArch.setPedaling(i);
                anArch.updateIntonation(targetPitches);
                int[] actualRelativeCoordinates = anArch.lastSet.getCenter().getTransformation(mySource.getCenter());
                boolean pedalingIsCorrect = true;
                for(int n = 0; n < 2; n++)
                    if(actualRelativeCoordinates[n] != myDesiredRelativeCoordinates[n])
                        pedalingIsCorrect = false;
                if(pedalingIsCorrect)
                    return i;
            }
            return -1;
        }

        public int getIndex(){
            return myIndex;
        }

        @Override
        public String toString(){
            String myString = "@ INDEX: " + myIndex + "\n       SOURCE: ";
            for(PitchClass pc: mySource){
                myString += NOTE_NAMES[pc.signifier] + ", ";
            }
            myString = myString.substring(0, myString.length() - 2);
            myString += "\n       DESTINATION: ";
            for(int i = 0; i < 12; i++){
                if(targetPitches[i])
                    myString += NOTE_NAMES[i] + ", ";
            }
            myString = myString.substring(0, myString.length() - 2);
            myString += "\n       RECOMMENDED PEDAL: " + findCorrectPedaling();
            return myString;
        }
    }
}

