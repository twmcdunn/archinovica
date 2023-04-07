

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Write a description of class DesignGUI here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class DesignGUI extends JPanel implements KeyListener, ActionListener, MouseMotionListener, MouseListener
{
    public ArrayList<Pitch> myPitches;
    public final String[] NOTE_NAMES = {"C ", "Db", "D ", "Eb", "E ", "F ", "F#", "G ", "Ab", "A ", "Bb", "B "};
    public String[][] noteSpace;
    public JFrame display;
    public boolean sandboxMode, shiftIsOn, animating;
    public int[] clickPoint;
    public Sandbox mySandbox;
    public CompositionSpace myCompositionSpace;
    public Menu focusObjectMenu, highlightGroupMenu;
    public static String PATH = new File("Harmonic Design.jar").getAbsolutePath().replace("Harmonic Design.jar", "");
    //public String[][] sandbox;

    public DesignGUI()
    {
        //buildSandBox(0, 0);
        Archinovica.legacyBehavior = false;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        display = new JFrame("Harmonic Design");

        display.setBounds(0, 0,  (int)screenSize.getHeight(), (int)screenSize.getHeight());

        display.addMouseListener(this);
        addMouseListener(this);
        display.add(this);
        display.setMenuBar(createMenu());

        mySandbox = new Sandbox();
        myCompositionSpace = new CompositionSpace();

        sandboxMode = true;
        buildNoteSpace();
        animating = true; // save cpu

        Timer clock = new Timer(50, (ActionListener)this);
        clock.start();
        shiftIsOn = false;

        display.addKeyListener(this);
        addMouseMotionListener(this);

        display.setVisible(true);
    }

    public void recordPitches(){
        TextIO.writeFile("/Users/peanutbutter/Documents/pitches.txt");
        for(PitchGroup pg: myCompositionSpace.myProgression){
             TextIO.putln("G");
            for(Pitch p: pg){
                TextIO.putln(p.getMidi());
            }
        }
    }

    public static void main(String[] args){
        new DesignGUI();
    }

    public void buildNoteSpace(){
        int originX = 0;
        int originY = 0;
        noteSpace = new String[12][3];
        while(originX < 0)
            originX += 12;
        while(originY < 0)
            originY += 12;
        for(int n = 0; n < 3; n++)
            for(int i = 0; i < 12; i++)
                noteSpace[i][n] = NOTE_NAMES[((n + originX) * 4 + (i + originY) * 7) % 12];
    }

    /*public void buildSandbox(int originX, int originY){
    buildNoteSpace(originX, originY);
    sandbox = new String[50][50];

    }
     */

    public MenuBar createMenu(){
        MenuBar bar = new MenuBar();
        String[] items = {"Save As...", "Open", "New"};
        addMenu("File", items, bar);

        String[] items1 = {"Show Sandbox", "Hide Sandbox", "Export Object"};
        addMenu("Sandbox", items1, bar);

        focusObjectMenu = new Menu("Focus On");
        MenuItem compositeFocus = new MenuItem("Composite");
        compositeFocus.setActionCommand("FOCUS: Composite");
        compositeFocus.addActionListener(this);
        focusObjectMenu.add(compositeFocus);
        bar.add(focusObjectMenu);

        String[] items2 = {"Iterate Fractal", "Delete Object", "Transpose Object", "Duplicate Object", "Set Alpha"};
        addMenu("Composition Object", items2, bar);

        String[] items3 = {"Create Highlighted Group", "Stop Highlighting", "Delete Highlighted Group", "Add Progression Pitches"};
        highlightGroupMenu = addMenu("Highlight", items3, bar);

        return bar;
    }

    public Menu addMenu(String title, String[] name, MenuBar a){
        Menu menu = new Menu(title);
        for(String aName: name){
            MenuItem item = new MenuItem(aName);
            item.addActionListener(this);
            menu.add(item);
        }
        a.add(menu);
        return menu;
    }

    @Override
    public void paint(Graphics g){
        if(sandboxMode)
            mySandbox.display(g);
        else
            myCompositionSpace.display(g);
    }

    public void actionPerformed(ActionEvent e){
        String aCom = e.getActionCommand();
        if(aCom != null && aCom.equals("Iterate Fractal")){
            myCompositionSpace.iterate();
        }
        if(aCom != null && aCom.equals("Delete Object")){
            myCompositionSpace.deleteObject();
        }
        if(aCom != null && aCom.equals("Set Alpha")){
            myCompositionSpace.setAlpha();
        }
        else if(aCom != null && aCom.equals("Show Sandbox")){
            sandboxMode = true;
        }
        else if(aCom != null && aCom.equals("Hide Sandbox")){
            sandboxMode = false;
        }
        else if(aCom != null && aCom.equals("Export Object")){
            if(sandboxMode)
                mySandbox.exportObject();
        }
        else if(aCom != null && aCom.startsWith("FOCUS: ")){
            if(!sandboxMode){
                myCompositionSpace.focusOnObject(aCom.substring(7, aCom.length()));
            }
        }
        else if(aCom != null && aCom.equals("Add Progression Pitches")){
            myCompositionSpace.highlightProgression();
        }
        else if(aCom != null && aCom.startsWith("HIGHLIGHT: ")){
            if(!sandboxMode){
                myCompositionSpace.highlightGroup(aCom.substring(11, aCom.length()));
            }
        }
        else if(aCom != null && aCom.equals("Duplicate Object")){
            if(!sandboxMode)
                myCompositionSpace.duplicate();
        }
        else if(aCom != null && aCom.equals("Create Highlighted Group")){
            if(!sandboxMode){
                myCompositionSpace.createHighlightedGroup();
            }
        }
        else if(aCom != null && aCom.equals("Add Progression Pitches")){
            myCompositionSpace.addProgressionPitches();
        }
        else if(aCom != null && aCom.equals("Stop Highlighting")){
            if(!sandboxMode){
                myCompositionSpace.stopHighlighting();
            }
        }
        else if(aCom != null && aCom.equals("Delete Highlighted Group")){
            if(!sandboxMode){
                myCompositionSpace.deleteHighlightedGroup();
            }
        }
        else if(aCom != null && aCom.equals("Save As...")){
            JFileChooser jchooser = new JFileChooser(PATH + "/Composition Objects/");
            if(jchooser.showSaveDialog(this)  == JFileChooser.APPROVE_OPTION){
                myCompositionSpace.saveAs(jchooser.getSelectedFile());
            }
        }
        else if(aCom != null && aCom.equals("Open")){
            JFileChooser jchooser = new JFileChooser(PATH + "/Composition Objects/");
            if(jchooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                myCompositionSpace.close();
                myCompositionSpace = new CompositionSpace();
                myCompositionSpace.open(jchooser.getSelectedFile());
                sandboxMode = false;
            }
        }
        else if(aCom != null && aCom.equals("New")){
            sandboxMode = true;
            myCompositionSpace.close();
            myCompositionSpace = new CompositionSpace();
            mySandbox = new Sandbox();
        }
        /*if(animating && aCom == null){
        repaint();
        display.revalidate();
        }
         */
        if(aCom == null){
            myCompositionSpace.callCursor();
        }
        else{
            validate();
            repaint();
            display.revalidate();
        }
    }

    public void mousePressed(MouseEvent e) {
        clickPoint = new int[]{e.getX(), e.getY()};
        System.out.println(clickPoint);
        if(!sandboxMode){
            if(shiftIsOn)
                myCompositionSpace.addToChord();
            else
                myCompositionSpace.removeFromChord();
            System.out.println("CLICKED");
        }
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {
        //animating = true;
    }

    public void mouseExited(MouseEvent e) {
        //animating = false;
        myCompositionSpace.regressCursor();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e){
        try{
            myCompositionSpace.moveScreen(e.getX() - clickPoint[0], e.getY() - clickPoint[1]);
        }catch(NullPointerException ex){}
        clickPoint = new int[]{e.getX(), e.getY()};
        validate();
        repaint();
        display.revalidate();
    }

    public void mouseMoved(MouseEvent e){
        if(!sandboxMode){
            myCompositionSpace.setCursorToNearestGroup(e.getX(), e.getY());
            //System.out.println(e.getX());
        }

    }

    public void keyTyped(KeyEvent e){

    }

    public void keyPressed(KeyEvent e){
        // System.out.println(e.getKeyCode());
        validate();
        repaint();
        switch(e.getExtendedKeyCode()){
            case KeyEvent.VK_OPEN_BRACKET:

            return;
            case KeyEvent.VK_UP:
            if(sandboxMode)
                mySandbox.moveUp();
            else
                myCompositionSpace.moveUp();
            return;
            case KeyEvent.VK_DOWN:
            if(sandboxMode)
                mySandbox.moveDown();
            else
                myCompositionSpace.moveDown();
            return;
            case KeyEvent.VK_RIGHT:
            if(sandboxMode)
                mySandbox.moveRight();
            else
                myCompositionSpace.moveRight();
            return;

            case KeyEvent.VK_LEFT:
            if(sandboxMode)
                mySandbox.moveLeft();
            else
                myCompositionSpace.moveLeft();
            return;
            case KeyEvent.VK_SPACE:
            if(sandboxMode){
                if(shiftIsOn)
                    mySandbox.addToSubgroup();
                else
                    mySandbox.selectOrDeselectPitch();
            }
            else{
                if(shiftIsOn)
                    myCompositionSpace.addToChord();
                else
                    myCompositionSpace.removeFromChord();
            }

            return;
            case KeyEvent.VK_BACK_SPACE:
            if(!sandboxMode)
                myCompositionSpace.deleteChord();
            return;
            case KeyEvent.VK_R:
            if(sandboxMode)
                mySandbox.reset();
            else
                myCompositionSpace.resetProgression();
            return;
            case KeyEvent.VK_ENTER:
            if(sandboxMode)
                mySandbox.exportObject();
            return;
            case KeyEvent.VK_SHIFT:
            shiftIsOn = true;
            return;
            case KeyEvent.VK_W:
            if(sandboxMode){

            }
            else
                myCompositionSpace.transpose(1, 0);
            return;
            case KeyEvent.VK_S:
            if(sandboxMode){

            }
            else
                myCompositionSpace.transpose(-1, 0);
            return;
            case KeyEvent.VK_D:
            if(sandboxMode){

            }
            else
                myCompositionSpace.transpose(0, 1);
            return;
            case KeyEvent.VK_A:
            if(sandboxMode){

            }
            else
                myCompositionSpace.transpose(0, -1);
            return;
            case KeyEvent.VK_G:
            if(!sandboxMode)
                myCompositionSpace.moveBySubgroups = true;
            return;
            case KeyEvent.VK_P:
            if(!sandboxMode)
                myCompositionSpace.moveBySubgroups = false;
            return;
            case KeyEvent.VK_1:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(1);
            return;
            case KeyEvent.VK_2:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(2);
            return;
            case KeyEvent.VK_3:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(3);
            return;
            case KeyEvent.VK_4:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(4);
            return;
            case KeyEvent.VK_5:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(5);
            return;
            case KeyEvent.VK_6:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(6);
            return;
            case KeyEvent.VK_7:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(7);
            return;
            case KeyEvent.VK_8:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(8);
            return;
            case KeyEvent.VK_9:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(9);
            return;
            case KeyEvent.VK_0:
            if(!sandboxMode)
                myCompositionSpace.focusOnObject(10);
            return;
            case KeyEvent.VK_E:
            recordPitches();
            JOptionPane.showMessageDialog(this,"EXPORTED PITCHES");
            break;
        }

        if(e.getKeyChar() == '+' || e.getKeyChar() == '='){
            if(!sandboxMode){
                myCompositionSpace.zoomIn();
            }
        }
        else if(e.getKeyChar() == '-' || e.getKeyChar() == '_'){
            if(!sandboxMode){
                myCompositionSpace.zoomOut();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SHIFT){
            shiftIsOn = false;
            if(sandboxMode)
                mySandbox.completeSubgroup();
            else{
                myCompositionSpace.exportChord();
            }
            validate();
            repaint();
            display.revalidate();
        }

    }

    class Pitch{
        int myFifths, myThirds;
        public Pitch(int fifths, int thirds){
            myFifths = fifths;
            myThirds = thirds;
        }

        public Pitch(){

        }

        public int getFifths(){
            return myFifths;
        }

        public int getThirds(){
            return myThirds;
        }

        public void transpose(int fifths, int thirds){
            myFifths += fifths;
            myThirds += thirds;
        }

        public int[] getTransformation(Pitch p){
            int[] transformation = {p.getFifths() - myFifths, p.getThirds() - myThirds};
            return transformation;
        }

        public double euclidianDistanceTo(Pitch destination){
            return Math.pow(Math.pow(myFifths - destination.getFifths(), 2)
                + Math.pow(myThirds - destination.getThirds(), 2), 0.5);
        }

        public String getNoteName(){
            int fifths = myFifths;
            int thirds = myThirds;
            while(fifths < 0)
                fifths += 12;
            while(thirds < 0)
                thirds += 3;
            return noteSpace[fifths % 12][thirds % 3];
        }

        public int getMidi(){

            int fifths = myFifths;
            int thirds = myThirds;
            while(fifths < 0)
                fifths += 12;
            while(thirds < 0)
                thirds += 3;
            return 60 + (fifths * 7 + thirds * 4) % 12;

        }

        @Override
        public Pitch clone(){
            return new Pitch(myFifths, myThirds);
        }

        @Override
        public String toString(){
            String a = "Simple Pitch {\n";
            a += myFifths + "\n";
            a += myThirds + "\n";
            a+= "}\n";
            return a;
        }

        @Override
        public boolean equals(Object o){
            return o instanceof Pitch && ((Pitch)o).getFifths() == myFifths &&  ((Pitch)o).getThirds() == myThirds;
        }

        public void load(){
            myFifths = Integer.parseInt(TextIO.getln());
            myThirds = Integer.parseInt(TextIO.getln());
            TextIO.getln();
        }

    }

    class PitchGroup extends ArrayList<Pitch>{
        private String myName;
        public ArrayList<PitchGroup> mySubgroups;
        private int[] myDimensions;
        public PitchGroup mySeed;
        public int myPedaling, cursorCalls;
        public MenuItem myItem;
        public Color myColor;
        public PitchGroup(String name){
            myName = name;
            mySubgroups = new ArrayList<PitchGroup>();
            // for example, the four triads constitute subgroups of the seed of my fractal
            myPedaling = -1;
            myColor = new Color(0,0,0,127);
            cursorCalls = 0;
        }

        public PitchGroup(Pitch p){
            myName = "NO NAME";
            mySubgroups = new ArrayList<PitchGroup>();
            add(p);
            myPedaling = -1;
            myColor = new Color(0,0,0,127);
            cursorCalls = 0;
        }

        @Override
        public boolean add(Pitch p){
            myDimensions = null;
            return super.add(p);
        }

        public void addBySubgroup(ArrayList<PitchGroup> subgroups){
            myDimensions = null;
            for(PitchGroup pg: subgroups)
                addAll(pg);
            mySubgroups = subgroups;
        }

        public void addSubgroup(PitchGroup subgroup){
            myDimensions = null;
            addAll(subgroup);
            mySubgroups.add(subgroup);
        }

        //returns fifthsmin, fifthsMax, thirdsMin, thirdsMax
        public int[] getDimensions(){
            if(myDimensions != null)
                return myDimensions;
            int fifthsMin = Integer.MAX_VALUE;
            int fifthsMax = -Integer.MAX_VALUE;
            int thirdsMin = Integer.MAX_VALUE;
            int thirdsMax = -Integer.MAX_VALUE;
            for(Pitch p: this){
                fifthsMin = Math.min(fifthsMin, p.getFifths());
                fifthsMax = Math.max(fifthsMax, p.getFifths());
                thirdsMin = Math.min(thirdsMin, p.getThirds());
                thirdsMax = Math.max(thirdsMax, p.getThirds());
            }
            myDimensions = new int[]{fifthsMin, fifthsMax, thirdsMin, thirdsMax};
            return myDimensions;
        }

        public int getHeight(){
            int[] dims = getDimensions();
            return 1 + dims[1] - dims[0];
        }

        public int getWidth(){
            int[] dims = getDimensions();
            return 1 + dims[3] - dims[2];
        }

        public int getMinFifths(){
            return getDimensions()[0];
        }

        public int getMinThirds(){
            return getDimensions()[2];
        }

        public boolean[] getPitchBinary(){
            boolean[] binary = new boolean[12];
            for(int i = 0; i < 12; i++)
                binary[i] = false;
            for(Pitch p: this){
                int fifths = p.getFifths();
                int thirds = p.getThirds();
                while(fifths < 0)
                    fifths += 12;
                while(thirds < 0)
                    thirds += 12;
                binary[((fifths * 7) + (thirds * 4)) % 12] = true;
            }
            return binary;
        }

        public Pitch[] getArray(){
            Pitch[] pitches = new Pitch[12];
            for(Pitch p: this){
                int fifths = p.getFifths();
                int thirds = p.getThirds();
                while(fifths < 0)
                    fifths += 12;
                while(thirds < 0)
                    thirds += 12;
                pitches[((fifths * 7) + (thirds * 4)) % 12] = p;
            }
            return pitches;
        }

        public void transpose(int fifths, int thirds){
            myDimensions = null;
            for(Pitch p: this)
                p.transpose(fifths, thirds);
        }

        public void createSeed(){
            mySeed = clone();
        }

        public void iterate(){
            int fifthsScale = getHeight();
            int thirdsScale = getWidth();
            Pitch center = mySeed.getCenter();
            int centerFifths = center.getFifths();
            int centerThirds = center.getThirds();
            ArrayList<PitchGroup> transformations = new ArrayList<PitchGroup>();
            for(Pitch p: mySeed){
                int fifths = p.getFifths();
                int thirds = p.getThirds();
                if(!(fifths == centerFifths && thirds == centerThirds)){
                    PitchGroup aTransformation = clone();
                    aTransformation.transpose((fifths - centerFifths) * fifthsScale,
                        (thirds - centerThirds) * thirdsScale);
                    transformations.add(aTransformation);
                }
            }
            //removeAll(this);
            //mySubgroups = new ArrayList<PitchGroup>();
            for(PitchGroup pg: transformations){
                addAll(pg);
                mySubgroups.addAll(pg.mySubgroups);
            }
            myDimensions = null;
        }

        public void setMySubgroups(ArrayList<PitchGroup> subgroups){
            mySubgroups = subgroups;
        }

        public ArrayList<PitchGroup> getSubroups(){
            return mySubgroups;
        }

        public PitchGroup raggruppaElementi(ArrayList<PitchGroup> subgroups, String name){//unnecesary?
            PitchGroup superGroup = new PitchGroup(name);
            superGroup.addBySubgroup(subgroups);
            return superGroup;
        }

        public Pitch[][] getGrid(){
            Pitch[][] grid = new Pitch[getHeight()][getWidth()];
            for(Pitch p:this)
                grid[p.getFifths() + getMinFifths()][p.getThirds() + getMinThirds()] = p;
            return grid;
        }

        public void sort(Pitch origin){
            Collections.sort(this, new RadialDistance(origin));
        }

        public void sortSubgroups(PitchGroup origin){
            Collections.sort(mySubgroups, new RadialDistanceGroup(origin));
        }

        public void sortSubgroups(double fifths, double thirds){
            Collections.sort(mySubgroups, new SortByDistance(fifths, thirds));
        }

        public PitchGroup getClosestGroup(double fifths, double thirds){
            PitchGroup closest = mySubgroups.get(0);
            SortByDistance comparator = new SortByDistance(fifths, thirds);
            for(PitchGroup pg: mySubgroups){
                if(comparator.compare(pg, closest) < 0){
                    closest = pg;
                }
            }
            return closest;
        }

        public PitchGroup getClosestPitch(double fifths, double thirds){
            PitchGroup closest = new PitchGroup(get(0));
            SortByDistance comparator = new SortByDistance(fifths, thirds);
            for(Pitch p: this){
                PitchGroup pg = new PitchGroup(p);
                if(comparator.compare(pg, closest) < 0){
                    closest = pg;
                }
            }
            return closest;
        }

        public Pitch getCenter(){
            sort(new Pitch((int)Math.rint(getMinFifths() + (getHeight() * 0.5)),
                    (int)Math.rint(getMinThirds() + (getWidth() * 0.5))));
            return get(0);
        }

        public double[] getCenterPoint(){
            return new double[]{getMinFifths() + (getHeight() * 0.5), getMinThirds() + (getWidth() * 0.5)};
        }

        @Override
        public PitchGroup clone(){ // make a deep copy
            PitchGroup clone = new PitchGroup(myName);
            if(mySubgroups.size() != 0)
                for(PitchGroup subgroup: mySubgroups)
                    clone.addSubgroup(subgroup.clone());
            else
                for(Pitch p: this)
                    clone.add(p.clone());
            return clone;
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof PitchGroup))
                return false;
            PitchGroup pg = (PitchGroup)o;
            for(Pitch p1: pg){
                boolean found = false;
                for(Pitch p2: this){
                    if(p1.equals(p2)){
                        found = true;
                        break;
                    }
                }
                if(!found)
                    return false;
            }

            return true;
        }

        public Pitch getOrigin(){
            return  new Pitch(getMinFifths(), getMinThirds());
        }

        class RadialDistance implements Comparator<Pitch>{
            public Pitch myOrigin;
            public RadialDistance(Pitch origin){
                myOrigin = origin;
            }

            public int compare(Pitch p1, Pitch p2){
                double distance1 = myOrigin.euclidianDistanceTo(p1);
                double distance2 = myOrigin.euclidianDistanceTo(p2);
                int difference = (int)((distance1 - distance2) * 10000);
                if(difference == 0){
                    distance1 = Math.atan((myOrigin.getFifths() - p1.getFifths())
                        / ((double)(myOrigin.getThirds() - p1.getThirds()))); 
                    distance2 = Math.atan((myOrigin.getFifths() - p2.getFifths())
                        / ((double)(myOrigin.getThirds() - p2.getThirds()))); 
                    difference = (int)((distance1 - distance2) * 10000);
                }
                return difference;
            }
        }

        class RadialDistanceGroup implements Comparator<PitchGroup>{
            public Pitch myOrigin;
            public RadialDistanceGroup(PitchGroup origin){
                myOrigin = origin.getCenter();
            }

            public int compare(PitchGroup pg1, PitchGroup pg2){
                Pitch p1 = pg1.getCenter();
                Pitch p2 = pg2.getCenter();
                double distance1 = myOrigin.euclidianDistanceTo(p1);
                double distance2 = myOrigin.euclidianDistanceTo(p2);
                int difference = (int)((distance1 - distance2) * 10000);
                if(difference == 0){
                    distance1 = Math.atan((myOrigin.getFifths() - p1.getFifths())
                        / ((double)(myOrigin.getThirds() - p1.getThirds()))); 
                    distance2 = Math.atan((myOrigin.getFifths() - p2.getFifths())
                        / ((double)(myOrigin.getThirds() - p2.getThirds()))); 
                    difference = (int)((distance1 - distance2) * 10000);
                }
                return difference;
            }
        }

        class SortByDistance implements Comparator<PitchGroup>{
            double fifths, thirds;
            public SortByDistance(double f, double t){
                fifths = f;
                thirds = t;
            }

            public int compare(PitchGroup pg1, PitchGroup pg2){
                double dF = pg1.getCenterPoint()[0] - fifths;
                double dT = pg1.getCenterPoint()[1] - thirds;
                double distance1 = Math.pow(dF * dF + dT * dT, 0.5);

                dF = pg2.getCenterPoint()[0] - fifths;
                dT = pg2.getCenterPoint()[1] - thirds;
                double distance2 = Math.pow(dF * dF + dT * dT, 0.5);

                int difference = (int)((distance1 - distance2) * 10000);

                return difference;
            }
        }

        public void setMenuItem(MenuItem item){
            myItem = item;
        }

        public void removeFocusMenuItem(){
            focusObjectMenu.remove(myItem);
        }

        public void removeHighlightMenuItem(){
            highlightGroupMenu.remove(myItem);
        }

        @Override
        public String toString(){
            String a = "Pitch Group " + myName + "{\n";
            for(PitchGroup subgroup: mySubgroups){
                a+= "Subgroup {\n";
                for(Pitch p: subgroup){
                    a += p;
                }
                a+= "}\n";
            }
            for(Pitch p: this){
                boolean grouped = false;
                for(PitchGroup subgroup: mySubgroups)
                    if(subgroup.contains(p)){
                        grouped = true;
                        break;
                    }
                if(!grouped)
                    a += p;
            }
            a+= "}\n";
            return a;
        }

        public void load(){
            String line = TextIO.getln();
            while(line.startsWith("Subgroup ")){
                PitchGroup subgroup = new PitchGroup("subgroup");
                subgroup.load();
                mySubgroups.add(subgroup);
                addAll(subgroup);
                line = TextIO.getln();
            }
            while(line.startsWith("Simple Pitch ")){
                Pitch p = new Pitch();
                p.load();
                add(p);
                line = TextIO.getln();
            }
        }

        public void display(Graphics g, int x, int y){
            boolean[] pitchBinary = getPitchBinary();
            int distanceBySharps = 0;
            int distanceByFlats = 0;
            for(int i = 0; i < 12; i++){
                if(pitchBinary[(i * 7) % 12]){
                    distanceBySharps += i;
                    distanceByFlats += 12 - i;
                }
            }
            boolean useSharps = distanceBySharps < distanceByFlats;
            g.setColor(Color.BLACK);
            g.setFont(new Font("Times New Roman", Font.BOLD, 20));

            class Sketcher{
                int collisions, misspellings;
                ArrayList<Drawable> myDrawbles;
                public Sketcher(){
                    collisions = Integer.MAX_VALUE;
                    misspellings = Integer.MAX_VALUE;
                }

                public ArrayList<Drawable> getDrawables(boolean useSharps1){
                    ArrayList<Drawable> drawables = new ArrayList<Drawable>();
                    int myCollisions = 0;
                    boolean centered = true;
                    int lastY = -1;
                    for(int i = 0; i < 12; i++){
                        int pitch = (i + 2) % 12;
                        int myX = x;
                        int lineOrSpace = getLineOrSpace(pitch, useSharps1);
                        int myY = y - 10 + lineOrSpace;
                        int positionNumber = ((40 - lineOrSpace) / 5) % 7;
                        if(pitchBinary[pitch]){
                            if(myY - lastY == -5 || myY - lastY == 0){
                                if(centered){
                                    myX += 10;
                                    centered = false;
                                }
                                myCollisions++;
                            }
                            else
                                centered = true;
                            if(!isNatural(pitch))
                                if(useSharps1)
                                    drawables.add(new Drawable(myX, myY + 11, 1, positionNumber, pitch));
                                else
                                    drawables.add(new Drawable(myX, myY + 11, 2, positionNumber, pitch));
                            drawables.add(new Drawable(myX + 20, myY, 0, positionNumber, pitch));
                            lastY = myY;
                        }
                    }

                    int myMisspellings = 0;
                    for(int i = 0; i < drawables.size(); i++){
                        for(int n = i + 1; n < drawables.size(); n++){
                            Drawable d1 = drawables.get(i);
                            Drawable d2 = drawables.get(n);
                            if(d1.myType == 0 && d2.myType == 0 && d1.intervalIsMisspelled(d2))
                                myMisspellings++;
                        }
                    }
                    if(myMisspellings < misspellings){
                        misspellings = myMisspellings;
                        myDrawbles = drawables;
                        return drawables;
                    }
                    return myDrawbles;
                }
            }
            Sketcher sketcher = new Sketcher();
            ArrayList<Drawable> drawables = sketcher.getDrawables(useSharps);
            drawables = sketcher.getDrawables(!useSharps);
            for(Drawable d: drawables)
                d.draw(g);

            String pedaling = "";
            switch(myPedaling){
                case 0:
                pedaling = "";
                break;
                case 1:
                pedaling = "L";
                break;
                case 2:
                pedaling = "R";
                break;
                case 3:
                pedaling = "LR";
                break;
            }
            g.drawString(pedaling, x, y + 80);
        }

        class Drawable{
            int myX, myY, myType, myPositionNumber, myPitch;
            public Drawable(int x, int y, int type, int positionNumber, int pitch){
                myX = x;
                myY = y;
                myType = type;
                myPositionNumber = positionNumber;
                myPitch = pitch;
            }

            public void draw(Graphics g){
                switch(myType){
                    case 0:
                    g.fillOval(myX, myY, 10, 10);
                    break;
                    case 1:
                    g.drawString("#", myX, myY);
                    break;
                    case 2:
                    g.drawString("b", myX, myY);
                    break;
                }
            }

            public boolean intervalIsMisspelled(Drawable d){
                int intervalType = getIntervalType(d);
                if(intervalType == 4)
                    return false;
                return intervalType != getWrittenIntervalType(d);
            }

            public int getWrittenIntervalType(Drawable d){
                int intervalType = d.myPositionNumber - myPositionNumber;
                while(intervalType < 0)
                    intervalType += 7;
                intervalType = intervalType % 7;
                if(intervalType > 3)
                    intervalType = 7 - intervalType;
                return intervalType;
            }

            public int getIntervalType(Drawable d){
                int interval = d.myPitch - myPitch;
                while(interval < 0)
                    interval += 12;
                interval = interval % 12;
                if(interval > 6)
                    interval = 12 - interval;
                switch(interval){
                    case 0:
                    return 0;
                    case 1:
                    return 1;
                    case 2:
                    return 1;
                    case 3:
                    return 2;
                    case 4:
                    return 2;
                    case 5:
                    return 3;
                    case 6:
                    return 4;
                }
                return - 1;
            }
        }

        public int getLineOrSpace(int pitch, boolean useSharps){
            if(!isNatural(pitch))
                if(useSharps)
                    pitch--;
                else{
                    if(pitch == 1)
                        return 15;
                    else
                        pitch++;
                }
            switch(pitch){
                case 0:
                return 20;
                case 2:
                return 50;
                case 4:
                return 45;
                case 5:
                return 40;
                case 7:
                return 35;
                case 9:
                return 30;
                case 11:
                return 25;
            }
            return -1;
        }
    }

    public boolean isNatural(int pitch){
        switch(pitch){
            case 0:
            return true;
            case 2:
            return true;
            case 4:
            return true;
            case 5:
            return true;
            case 7:
            return true;
            case 9:
            return true;
            case 11:
            return true;
        }
        return false;
    }

    class Sandbox{
        public int cursorX, cursorY;
        public PitchGroup selectedPoints, mySubgroup;
        public Sandbox(){
            cursorX = 20;
            cursorY = 20;
            selectedPoints = new PitchGroup("New Object");
            mySubgroup = new PitchGroup("My Subgroup");
        }

        public void selectOrDeselectPitch(){
            for(Pitch p: selectedPoints){
                if(p.getFifths() == cursorY && p.getThirds() == cursorX){
                    selectedPoints.remove(p);
                    for(PitchGroup subgroup: selectedPoints.mySubgroups)
                        subgroup.remove(p);
                    return;
                }
            }
            selectedPoints.add(new Pitch(cursorY, cursorX));
        }

        public void addToSubgroup(){
            for(Pitch p:selectedPoints)
                if(p.getFifths() == cursorY & p.getThirds() == cursorX)
                    if(mySubgroup.contains(p))
                        mySubgroup.remove(p);
                    else
                        mySubgroup.add(p);
        }

        public void completeSubgroup(){
            if(mySubgroup.size() == 0)
                return;
            selectedPoints.mySubgroups.add(mySubgroup);
            mySubgroup = new PitchGroup("My Subgroup");
        }

        public void exportObject(){
            if(selectedPoints.size() == 0)
                return;
            JOptionPane optionPane = new JOptionPane();
            String name = optionPane.showInputDialog("Object Name:");
            if(name == null)
                return;

            selectedPoints.myName = name;
            selectedPoints.createSeed();
            myCompositionSpace.addObject(selectedPoints);
            selectedPoints = new PitchGroup("New Object");
            sandboxMode = false;
        }

        public void reset(){
            selectedPoints = new PitchGroup("New Object");

            mySubgroup = new PitchGroup("My Subgroup");
        }

        public void moveUp(){
            cursorY++;
        }

        public void moveDown(){
            if(cursorY > 0)
                cursorY--;
        }

        public void moveRight(){
            cursorX++;
        }

        public void moveLeft(){
            if(cursorX > 0)
                cursorX--;
        }

        public void display(Graphics g){
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font("Times New Roman", Font.PLAIN, 16));

            g.setColor(Color.BLACK);
            for(int x = 0; x < getWidth() / 40.0 - 1; x++)
                for(int y = 1; y < getWidth() / 40.0; y++)
                    g.drawString(noteSpace[y % 12][x % 3], 40 + x * 40, getWidth() - (40 + y * 40));

            g.setFont(new Font("Times New Roman", Font.BOLD, 16));
            g.setColor(Color.RED);
            for(Pitch p: selectedPoints)
                g.drawString(p.getNoteName(), 40 + p.getThirds() * 40, getWidth() - (40 + p.getFifths() * 40));
            g.setColor(Color.PINK);
            for(PitchGroup subgroup: selectedPoints.mySubgroups)
                for(Pitch p: subgroup)
                    g.drawString(p.getNoteName(), 40 + p.getThirds() * 40, getWidth() - (40 + p.getFifths() * 40));

            g.setColor(Color.GREEN);
            for(Pitch p: mySubgroup)
                g.drawString(p.getNoteName(), 40 + p.getThirds() * 40, getWidth() - (40 + p.getFifths() * 40));

            g.setColor(Color.BLUE);
            g.drawString(noteSpace[cursorY % 12][cursorX % 3], 40 + cursorX * 40, getWidth() - (40 + cursorY * 40));

        }
    }

    class CompositionSpace{
        public ArrayList<PitchGroup> myObjects, myHighlightedGroups;
        public boolean moveBySubgroups;
        public PitchGroup focusObject, cursorObject, myChord, myHighlightedGroup, lastStableCursor;
        public double scaleDisplay;
        public int displaceDisplayX, displaceDisplayY;
        public Progression myProgression;
        public BufferedImage imageWithoutCursor;
        public Pitch[][] grid;
        public PitchGroup[][] subgroupGrid;
        public int gridOriginFifths, gridOriginThirds, cursorFifths, cursorThirds;

        public CompositionSpace(){
            myObjects = new ArrayList<PitchGroup>();
            moveBySubgroups = true;
            scaleDisplay = 1;
            displaceDisplayX = 0;
            displaceDisplayY = 0;
            myProgression = new Progression("Progression");
            myChord = new PitchGroup("Chord");
            myHighlightedGroups = new ArrayList<PitchGroup>();
            updateGrid();
        }

        public void addObject(PitchGroup object){
            object.sort(object.getOrigin());
            myObjects.add(object);
            focusObject = object;
            moveBySubgroups = focusObject.mySubgroups.size() > 0;
            setCursorToFocusObject();
            MenuItem item = new MenuItem(object.myName + "  -  " + myObjects.size());
            object.setMenuItem(item);
            item.addActionListener(DesignGUI.this);
            item.setActionCommand("FOCUS: " + object.myName);
            focusObjectMenu.add(item);
            updateGrid();
        }

        public void removeObject(String name){
            updateGrid();
        }

        public void close(){
            for(PitchGroup object: myObjects){
                object.removeFocusMenuItem();
            }
            for(PitchGroup object: myHighlightedGroups){
                object.removeHighlightMenuItem();
            }
        }

        public void setCursorToFocusObject(){
            if(focusObject.size() == 0)
                return;
            if(moveBySubgroups){
                if(cursorObject != null)
                    focusObject.sortSubgroups(cursorObject);
                cursorObject = focusObject.mySubgroups.get(0);
                cursorObject.cursorCalls = 0;
                lastStableCursor = cursorObject;
            }
            else{
                if(cursorObject != null)
                    focusObject.sort(cursorObject.get(0));
                cursorObject = new PitchGroup("Cursor");
                cursorObject.add(focusObject.get(0));
                cursorObject.cursorCalls = 0;
                lastStableCursor = cursorObject;
            }
        }

        public void updateGrid(){
            PitchGroup composite = getComposite();
            grid = new Pitch[composite.getHeight()][composite.getWidth()];
            gridOriginFifths = composite.getMinFifths();
            gridOriginThirds = composite.getMinThirds();
            for(Pitch p: composite)
                grid[p.getFifths() - gridOriginFifths][p.getThirds() - gridOriginThirds] = p;
            updateSubgroupGrid();
        }

        public void updateSubgroupGrid(){
            if(focusObject == null)
                return;
            subgroupGrid = new PitchGroup[grid.length][grid[0].length];
            for(PitchGroup subgroup: focusObject.mySubgroups){
                for(Pitch p: subgroup)
                    subgroupGrid[p.getFifths() - gridOriginFifths][p.getThirds() - gridOriginThirds] = subgroup;
            }
        }

        public PitchGroup getComposite(){
            PitchGroup composite = new PitchGroup("Composite");
            for(PitchGroup object: myObjects){
                composite.addAll(object);
                for(PitchGroup subgroup: object.mySubgroups){
                    if(!containsSubgroup(composite.mySubgroups, subgroup))
                        composite.mySubgroups.add(subgroup);
                }
            }
            composite.createSeed();
            return composite;
        }

        public void focusOnObject(String name){
            if(name.equals("Composite")){

                focusObject = getComposite();
                setCursorToFocusObject();
                moveBySubgroups = focusObject.mySubgroups.size() > 0;

                updateSubgroupGrid();
                return;
            }
            for(PitchGroup object: myObjects)
                if(object.myName.equals(name)){
                    focusObject = object;
                    setCursorToFocusObject();
                    moveBySubgroups = focusObject.mySubgroups.size() > 0;
                    updateSubgroupGrid();
                    return;
                }

        }

        public void focusOnObject(int i){
            focusObject = myObjects.get((i - 1) % myObjects.size());
            setCursorToFocusObject();
            moveBySubgroups = focusObject.mySubgroups.size() > 0;
            updateSubgroupGrid();
        }

        public boolean containsSubgroup(ArrayList<PitchGroup> subgroups, PitchGroup subgroup){
            for(PitchGroup sg: subgroups)
                if(sg.equals(subgroup))
                    return true;
            return false;
        }

        public void highlightGroup(String name){
            for(PitchGroup object: myHighlightedGroups)
                if(object.myName.equals(name)){
                    myHighlightedGroup = object;
                }
        }

        public void stopHighlighting(){
            if(!myHighlightedGroups.contains(myHighlightedGroup))
                myHighlightedGroups.add(myHighlightedGroup);
            myHighlightedGroup = null;
        }

        public void iterate(){
            focusObject.iterate();
            updateGrid();
            updateSubgroupGrid();
        }

        public void transpose(int fifths, int thirds){
            focusObject.transpose(fifths, thirds);
            //cursorObject.transpose(fifths, thirds);
        }

        public void callCursor(){
            if(cursorObject == null)
                return;
            if(cursorObject.cursorCalls < 10){
                cursorObject.cursorCalls++;
                return;
            }
            lastStableCursor = cursorObject;
            animating = true;
        }

        public void move(int direction){
            double desiredAngle = -Math.PI / 2.0;
            switch(direction){
                case 1:
                desiredAngle = Math.PI / 2.0;
                break;
                case 2:
                desiredAngle =  Math.PI;
                break;
                case 3:
                desiredAngle =  0;
                break;
            }
            if(moveBySubgroups){
                focusObject.sortSubgroups(cursorObject);
                ArrayList<PitchGroup> neighbors = new ArrayList<PitchGroup>();

                for(PitchGroup subgroup: focusObject.mySubgroups){
                    if(!subgroup.equals(cursorObject) && !containsSubgroup(neighbors, subgroup)){
                        neighbors.add(subgroup);
                        if(neighbors.size() == 4)
                            break;
                    }
                }

                Collections.sort(neighbors, new CompareAngleGroup(cursorObject, desiredAngle));
                System.out.println(neighbors);
                cursorObject = neighbors.get(0);
                cursorObject.cursorCalls = 0;
                lastStableCursor = cursorObject;
            }
            else{
                focusObject.sort(cursorObject.get(0));
                ArrayList<Pitch> neighbors = new ArrayList<Pitch>();

                for(Pitch p: focusObject){
                    if(!new PitchGroup(p).equals(cursorObject)){
                        boolean isUnique = true;
                        for(Pitch p1: neighbors)
                            if(p1.equals(p)){
                                isUnique = false;
                                break;
                            }
                        if(isUnique)
                            neighbors.add(p);
                        if(neighbors.size() == 4)
                            break;
                    }
                }

                Collections.sort(neighbors, new CompareAngle(cursorObject.getCenter(), desiredAngle));
                cursorObject = new PitchGroup("Cursor");
                cursorObject.add(neighbors.get(0));
                lastStableCursor = cursorObject;
            }
        }

        public void moveUp(){
            move(0);
        }

        public void moveDown(){
            move(1);
        }

        public void moveRight(){
            move(2);
        }

        public void moveLeft(){
            move(3);
        }

        class CompareAngle implements Comparator<Pitch>{
            public Pitch myOrigin;
            public double myAngle;
            public CompareAngle(Pitch origin, double desiredAngle){
                myOrigin = origin;
                myAngle = desiredAngle;
            }

            public int compare(Pitch p1, Pitch p2){
                double distance1 = Math.abs(Math.atan2((myOrigin.getFifths() - p1.getFifths()),
                            ((double)(myOrigin.getThirds() - p1.getThirds()))) - myAngle); 
                if(distance1 > Math.PI)
                    distance1 = 2 * Math.PI - distance1;
                double distance2 = Math.abs(Math.atan2((myOrigin.getFifths() - p2.getFifths()),
                            ((double)(myOrigin.getThirds() - p2.getThirds())))  - myAngle); 
                if(distance2 > Math.PI)
                    distance2 = 2 * Math.PI - distance2;
                int difference = (int)((distance1 - distance2) * 10000);
                return difference;
            }
        }

        class CompareAngleGroup implements Comparator<PitchGroup>{
            public double[] myOrigin;
            public double myAngle;

            public CompareAngleGroup(PitchGroup origin, double desiredAngle){
                myOrigin = origin.getCenterPoint();
                myAngle = desiredAngle;
            }

            public int compare(PitchGroup pg1, PitchGroup pg2){
                double[] p1 = pg1.getCenterPoint();
                double[] p2 = pg2.getCenterPoint();

                double distance1 = Math.abs(Math.atan2(myOrigin[0] - p1[0], myOrigin[1] - p1[1])
                        - myAngle); 
                if(distance1 > Math.PI)
                    distance1 = 2 * Math.PI - distance1;
                double distance2 = Math.abs(Math.atan2(myOrigin[0] - p2[0], myOrigin[1] - p2[1])
                        - myAngle); 
                if(distance2 > Math.PI)
                    distance2 = 2 * Math.PI - distance2;
                int difference = (int)((distance1 - distance2) * 10000);
                return difference;
            }
        }

        public void createHighlightedGroup(){
            JOptionPane optionPane = new JOptionPane();
            String name = optionPane.showInputDialog("Group Name:");
            if(name == null)
                return;
            myHighlightedGroup = new PitchGroup(name);
            MenuItem item = new MenuItem(name);
            item.addActionListener(DesignGUI.this);
            item.setActionCommand("HIGHLIGHT: " + name);
            highlightGroupMenu.add(item);
            myHighlightedGroup.setMenuItem(item);
        }

        public void addProgressionPitches(){
            if(myHighlightedGroup == null)
                createHighlightedGroup();
            for(PitchGroup chord: myProgression)
                myHighlightedGroup.addAll(chord);

        }

        public void deleteHighlightedGroup(){
            myHighlightedGroups.remove(myHighlightedGroup);
            myHighlightedGroup.removeHighlightMenuItem();
            stopHighlighting();
        }

        public void deleteObject(){
            myObjects.remove(focusObject);
            focusObject.removeFocusMenuItem();
            if(myObjects.size() > 0){
                focusObject = myObjects.get(0);
                setCursorToFocusObject();
            }
            updateGrid();
        }

        public void addToChord(){
            PitchGroup group = myChord;
            if(myHighlightedGroup != null)
                group = myHighlightedGroup;
            for(Pitch p: cursorObject)
                if(!group.contains(p) || myHighlightedGroup != null)
                    group.add(p);
        }

        public void removeFromChord(){//currenlty only used for highlighting
            PitchGroup group = myChord;
            if(myHighlightedGroup != null)
                group = myHighlightedGroup;
            for(Pitch p: cursorObject)
                if(group.contains(p))
                    group.remove(p);

        }

        public void resetChord(){
            myChord = new PitchGroup("Chord");
        }

        public void deleteChord(){
            myProgression.remove(myProgression.size() - 1);

        }

        public void exportChord(){
            /*if(myHighlightedGroup != null && myHighlightedGroup.size() > 0){
            myHighlightedGroups.add(myHighlightedGroup);
            myHighlightedGroup = null;
            }
            else 
             */
            if(myChord.size() > 0){
                myProgression.add(myChord);
                myProgression.calculatePedals();
                resetChord();

            }
        }

        public void resetProgression(){
            myProgression = new Progression("Progression");
        }

        public void setAlpha(){
            JOptionPane optionPane = new JOptionPane();
            JSlider slider = getSlider(optionPane);
            optionPane.setMessage(new Object[] { "Select alpha value: ", slider });
            optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
            optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog(display, "Set Alpha of "+ focusObject.myName);
            dialog.setVisible(true);
            System.out.println("Input: " + optionPane.getInputValue());
            if(optionPane.getValue().equals(JOptionPane.CANCEL_OPTION))
                return;
            int value = 50;
            if(optionPane.getInputValue() instanceof Integer){
                value = (Integer)optionPane.getInputValue();
            }
            if(focusObject.myName.equals("Composite")){
                for(PitchGroup object: myObjects)
                    object.myColor = new Color(0, 0, 0, (int)Math.rint(255 * value / 100.0));
            }
            else
                focusObject.myColor = new Color(0, 0, 0, (int)Math.rint(255 * value / 100.0));
            focusOnObject("Composite");
        }

        public void setCursorToNearestGroup(int dispX, int dispY){
            if(focusObject.mySubgroups.size() == 0)
                return;
            double thirds = (getWidth() * 0.5  - displaceDisplayX 
                    - ((getWidth() * 0.5 - dispX) / scaleDisplay)) /  30.0;

            double fifths =  (((dispY - getWidth() * 0.5) / scaleDisplay) 
                    - (getWidth() * 0.5) + displaceDisplayY) / -30.0;

            //focusObject.sortSubgroups(fifths, thirds);
            //cursorObject = focusObject.mySubgroups.get(0);

            if(thirds == cursorThirds && fifths == cursorFifths)
                return;

            cursorThirds = (int)thirds;// - 1;
            cursorFifths = (int)fifths;

            if(moveBySubgroups){
                //cursorObject = focusObject.getClosestGroup(fifths, thirds);
                PitchGroup pg;
                try{
                    pg = subgroupGrid[cursorFifths - gridOriginFifths][cursorThirds - gridOriginThirds];
                }catch(Exception ex){
                    return;
                }
                if(pg == null)
                    return;
                cursorObject = pg;
            }
            else{
                //cursorObject = focusObject.getClosestPitch(fifths, thirds);
                Pitch p;

                try{
                    p = grid[cursorFifths - gridOriginFifths][cursorThirds - gridOriginThirds];
                }catch(Exception ex){
                    return;
                }
                if(p == null)
                    return;
                cursorObject = new PitchGroup(p);
            }
            cursorObject.cursorCalls = 0;

            getGraphics().drawImage(imageWithoutCursor, 0, 0, null);
            displayCursor(getGraphics());

        }

        public void display(Graphics panelGraphics){
            int w = getWidth();
            int h = getHeight();
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bi.createGraphics();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            if(myObjects.size() == 0)
                return;
            g.setFont(new Font("Times New Roman", Font.PLAIN, (int)(20 * scaleDisplay)));

            for(PitchGroup object: myObjects){
                g.setColor(object.myColor);
                for(Pitch p: object)
                    g.drawString(p.getNoteName(), (int)(getWidth() * 0.5 -
                            (getWidth() * 0.5 - (p.getThirds() * 30 + displaceDisplayX))
                            * scaleDisplay),
                        (int)(getWidth() - (getWidth() * 0.5 -
                                (getWidth() * 0.5 - (p.getFifths() * 30 + displaceDisplayY))
                                * scaleDisplay)));
            }
            if(!focusObject.myName.equals("Composite")){
                g.setColor(Color.BLACK);
                for(Pitch p: focusObject)
                    g.drawString(p.getNoteName(), (int)(getWidth() * 0.5 -
                            (getWidth() * 0.5 - (p.getThirds() * 30 + displaceDisplayX))
                            * scaleDisplay),
                        (int)(getWidth() - (getWidth() * 0.5 -
                                (getWidth() * 0.5 - (p.getFifths() * 30 + displaceDisplayY))
                                * scaleDisplay)));
            }

            if(shiftIsOn){
                g.setColor(Color.GREEN);
                for(PitchGroup chord: myProgression)
                    for(Pitch p: chord)
                        g.drawString(p.getNoteName(), (int)(getWidth() * 0.5 -
                                (getWidth() * 0.5 - (p.getThirds() * 30 + displaceDisplayX))
                                * scaleDisplay),
                            (int)(getWidth() - (getWidth() * 0.5 -
                                    (getWidth() * 0.5 - (p.getFifths() * 30 + displaceDisplayY))
                                    * scaleDisplay)));
            }

            if(myHighlightedGroup != null){
                g.setColor(Color.GREEN);
                for(Pitch p: myHighlightedGroup)
                    g.drawString(p.getNoteName(), (int)(getWidth() * 0.5 -
                            (getWidth() * 0.5 - (p.getThirds() * 30 + displaceDisplayX))
                            * scaleDisplay),
                        (int)(getWidth() - (getWidth() * 0.5 -
                                (getWidth() * 0.5 - (p.getFifths() * 30 + displaceDisplayY))
                                * scaleDisplay)));
            }

            myProgression.display(g);

            imageWithoutCursor = bi;
            panelGraphics.drawImage(imageWithoutCursor, 0, 0, null);
            displayCursor(panelGraphics);

            Pitch p = lastStableCursor.getCenter();
            double xCenter = getWidth() * 0.5 -
                (getWidth() * 0.5 - (p.getThirds() * 30 + displaceDisplayX))
                * scaleDisplay;
            double yCenter = getWidth() - (getWidth() * 0.5 -
                    (getWidth() * 0.5 - (p.getFifths() * 30 + displaceDisplayY))
                    * scaleDisplay);

            double xDistance = getWidth() * 0.5 - xCenter;
            double yDistance = getWidth() * 0.5 - yCenter;

            if(animating && false){
                displaceDisplayX += xDistance * 0.1 * scaleDisplay;
                displaceDisplayY -= yDistance * 0.1 * scaleDisplay;
            }

            animating = Math.pow(xDistance * xDistance
                + yDistance * yDistance, 0.5) > 20;

        }

        public void displayCursor(Graphics g){
            g.setFont(new Font("Times New Roman", Font.BOLD, (int)(20 * scaleDisplay)));
            g.setColor(Color.BLUE);
            for(Pitch p: cursorObject)
                g.drawString(p.getNoteName(), (int)(getWidth() * 0.5 -
                        (getWidth() * 0.5 - (p.getThirds() * 30 + displaceDisplayX))
                        * scaleDisplay),
                    (int)(getWidth() - (getWidth() * 0.5 -
                            (getWidth() * 0.5 - (p.getFifths() * 30 + displaceDisplayY))
                            * scaleDisplay)));
        }

        public void moveScreen(int deltaX, int deltaY){
            displaceDisplayX += deltaX / scaleDisplay;
            displaceDisplayY -= deltaY / scaleDisplay;
        }

        public void regressCursor(){
            if(lastStableCursor == null)
                return;
            cursorObject = lastStableCursor;
            getGraphics().drawImage(imageWithoutCursor, 0, 0, null);
            displayCursor(getGraphics());

        }

        public void duplicate(){
            addObject(focusObject.clone());
        }

        public void zoomIn(){
            scaleDisplay *= 1.25;
        }

        public void zoomOut(){
            scaleDisplay *= 4/5.0;
        }

        public void highlightProgression(){
            String[] highlightGroupNames = new String[myHighlightedGroups.size() + 1];
            highlightGroupNames[0] = "NEW";
            for(int i = 0; i < myHighlightedGroups.size(); i++){
                PitchGroup hg = myHighlightedGroups.get(i);
                highlightGroupNames[i + 1] = hg.myName;
            }
            String highlightedProgressionName = (String)JOptionPane.showInputDialog(
                    display,
                    "Kindly choose a highlight group:",
                    "Highlight Progression",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    highlightGroupNames, "NEW");
            if(highlightedProgressionName == null) 
                return;

            if(highlightedProgressionName.equals("NEW")){
                createHighlightedGroup();

            }
            else{
                int index = 1;
                while(index < highlightGroupNames.length && !highlightGroupNames[index].equals(highlightedProgressionName))
                    index++;
                myHighlightedGroup = myHighlightedGroups.get(index - 1);
            }

            for(PitchGroup chord: myProgression)
                myHighlightedGroup.addAll(chord);
        }

        @Override
        public String toString(){
            String a = "CompositionSpace {\n";
            for(PitchGroup g: myObjects)
                a+= g;
            if(myHighlightedGroups.size() > 0){
                a += "Highlighted Groups {\n";
                for(PitchGroup g: myHighlightedGroups)
                    a+= g;
                a+= "}\n";
            }
            a+= "}\n";
            return a;
        }

        public void saveAs(File file){
            TextIO.writeFile(file + "");
            TextIO.putln(this);
        }

        public void open(File file){
            TextIO.readFile(file + "");
            TextIO.getln();
            String line = TextIO.getln();
            while(line.startsWith("Pitch Group ")){
                PitchGroup pg = new PitchGroup(line.substring(12, line.length() - 1));
                pg.load();
                addObject(pg);
                line = TextIO.getln();
            }
            //line = TextIO.getln();
            if(line.startsWith("Highlighted Groups {")){
                line = TextIO.getln();
                while(line.startsWith("Pitch Group ")){
                    PitchGroup pg = new PitchGroup(line.substring(12, line.length() - 1));
                    pg.load();
                    myHighlightedGroups.add(pg);
                    MenuItem item = new MenuItem(pg.myName);
                    item.addActionListener(DesignGUI.this);
                    item.setActionCommand("HIGHLIGHT: " + pg.myName);
                    highlightGroupMenu.add(item);
                    pg.setMenuItem(item);
                    line = TextIO.getln();
                }
                line = TextIO.getln();
            }
            if(line.startsWith("Progressions {")){
            }
        }

    }

    class Progression extends ArrayList<PitchGroup>{
        String myName;
        BufferedImage treble;
        public Progression(String name){
            myName = name;
            try{
                treble = resizeImage(ImageIO.read(new File("treble.png")), 53, 70);
            }
            catch(java.io.IOException e){

                JOptionPane optionPane = new JOptionPane();
                optionPane.showMessageDialog(DesignGUI.this, "I can't seem to find treble.png...\nAwkward...\n" + e);
            }
        }

        public void calculatePedals(){
            Archinovica anArch = new Archinovica(null);
            PitchClass archiPointOfReference = null;
            Pitch progressionPointOfReference = null;
            for(int index = 0; index < size(); index++){
                PitchGroup chord = get(index);
                boolean[] pitchBinary = chord.getPitchBinary();
                Pitch[] progressionArray = chord.getArray();

                chord.myPedaling = 0;
                anArch.setPedaling(chord.myPedaling);
                anArch.updateIntonation(pitchBinary);
                PitchClass[] archiArray = anArch.soundingPitchClasses;

                if(!areFormallyEqual(progressionArray, archiArray)){
                    JOptionPane optionPane = new JOptionPane();
                    optionPane.showMessageDialog(DesignGUI.this, "Warning: no solution @ index.\nChord violates wellformedness." + index);
                    return;
                }

                int destinationIndex = -1;
                Pitch progressionDestination = null;
                PitchClass archiDestination = null;
                for(int i = 0; i < 12; i++)
                    if(progressionArray[i] != null){
                        progressionDestination = progressionArray[i];
                        archiDestination = archiArray[i];
                        destinationIndex = i;
                    }
                if(progressionPointOfReference != null){
                    int[] progressionTransformation = progressionPointOfReference.getTransformation(progressionDestination);
                    int[] archiTransformation = archiPointOfReference.getTransformation(archiDestination);
                    boolean equalTransforms = progressionTransformation[0] == archiTransformation[0] &&
                        progressionTransformation[1] == archiTransformation[1];
                    for(int i = 1; !equalTransforms && i < 4; i++){
                        anArch.undoProgression();
                        chord.myPedaling = i;
                        anArch.setPedaling(chord.myPedaling);
                        anArch.updateIntonation(pitchBinary);
                        archiDestination = anArch.soundingPitchClasses[destinationIndex];
                        progressionTransformation = progressionPointOfReference.getTransformation(progressionDestination);
                        archiTransformation = archiPointOfReference.getTransformation(archiDestination);
                        equalTransforms = progressionTransformation[0] == archiTransformation[0] &&
                        progressionTransformation[1] == archiTransformation[1];
                    }

                    if(!equalTransforms){
                        JOptionPane optionPane = new JOptionPane();
                        optionPane.showMessageDialog(DesignGUI.this, "Warning: no solution @ index.\nNo available pedaling." + index);
                        return;
                    }

                }
                progressionPointOfReference = progressionDestination;
                archiPointOfReference = archiDestination;
            }
        }

        public boolean areFormallyEqual(Pitch[] chord, PitchClass[] set){
            Pitch chordRef = null;
            PitchClass setRef = null;
            for(int i = 0; i < 12; i++){
                if(chord[i] == null)
                    continue;
                if(chordRef == null){
                    chordRef = chord[i];
                    setRef = set[i];
                }
                else{
                    int[] chordTrans = chordRef.getTransformation(chord[i]);//relative coordinates
                    int[] setTrans = setRef.getTransformation(set[i]);
                    if(chordTrans[0] != setTrans[0] || chordTrans[1] != setTrans[1])
                        return false;
                }
            }

            return true;
        }

        public void display(Graphics g){
            if(size() == 0)
                return;
            int topOfStaff = 30;
            drawStaff(g, topOfStaff);
            int x = 103;
            for(PitchGroup chord: this){
                chord.display(g, x, topOfStaff);
                x += 50;
                if(x > getWidth() - 50){
                    x = 103;
                    topOfStaff += 100;
                    drawStaff(g, topOfStaff);
                }
            }

        }

        public void drawStaff(Graphics g, int startY){
            g.drawImage(treble, 20, startY - 10, Color.WHITE, null);
            g.setColor(Color.BLACK);
            for(int y = startY; y < startY + 50; y += 10)
                g.drawLine(0, y, getWidth(), y);
        }
    }

    public BufferedImage createImage(JPanel panel) {
        int w = panel.getWidth();
        int h = panel.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        panel.paint(g);
        g.dispose();
        return bi;
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int w, int h){
        int type = originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(w, h, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, w, h, null);
        g.dispose();    
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

    static JSlider getSlider(final JOptionPane optionPane) {
        JSlider slider = new JSlider();
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        ChangeListener changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    JSlider theSlider = (JSlider) changeEvent.getSource();
                    if (!theSlider.getValueIsAdjusting()) {
                        optionPane.setInputValue(new Integer(theSlider.getValue()));
                    }
                }
            };
        slider.addChangeListener(changeListener);
        return slider;
    }

    public static void test(){
        System.out.println(Math.atan2(2,1.0));
        System.out.println(Math.atan2(1,2.0));
        System.out.println(Math.atan2(-2,1.0));
        System.out.println(Math.atan2(-1,-2.0));
    }

}
