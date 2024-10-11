
import java.util.ArrayList;
import java.util.Arrays;
import csnd6.Csound;
/**
 *This version seeks to fix eratic behavior.
 * 
 * 
 */
public class Archinovica
{
    public PitchClass[] soundingPitchClasses, temperedScale;
    public PitchClass lastCenter; // depreciated
    public PitchSet lastSet;
    public ArrayList<PitchSet> backupSets;
    public ArrayList<PitchClass[]> backUpSoundingPCs;
    public int pedaling;
    public static Csound myCsound; // depreciated
    public static GUI gui;
    public static PitchClass ORIGIN;
    public static boolean legacyBehavior; //when true, will behave like version 0.9
    //public LiveReceiver liveRec;

    /*public Archinovica(GUI aGui){
    soundingPitchClasses = new PitchClass[12];
    backUpSoundingPCs =  new ArrayList<PitchClass[]>();
    backupSets = new ArrayList<PitchSet>();
    temperedScale = new PitchClass[12];
    initializeSpace();
    PitchClass[] initialScale = new PitchClass[12];
    gui = aGui;
    if(gui == null)
    gui = new GUI(this);
    //lastCenter = pitchSpace;
    //new PitchSet(new PitchClass[]{new PitchClass(0), null, null, null, new PitchClass(4), null, null, new PitchClass(7), null, null, null, null});

    //myCsound = new Csound();
    }
     */

    public Archinovica(GUI agui){
        soundingPitchClasses = new PitchClass[12];
        backUpSoundingPCs =  new ArrayList<PitchClass[]>();
        backupSets = new ArrayList<PitchSet>();
        temperedScale = new PitchClass[12];
        initializeSpace();
        PitchClass[] initialScale = new PitchClass[12];
        gui = agui;

        //liveRec = lr;
        //lastCenter = pitchSpace;
        //new PitchSet(new PitchClass[]{new PitchClass(0), null, null, null, new PitchClass(4), null, null, new PitchClass(7), null, null, null, null});

        //myCsound = new Csound();
    }

    /* public void updatePitches(boolean[] pitchBinary){
    // System.out.println("UPDATE PITCHES");
    commonTone = -1;
    PitchClass[] pitches = new PitchClass[12];
    for(int n = 0; n < 12; n++){
    if(pitchBinary[n]){
    if(soundingPitchClasses[n] != null){
    pitches[n] = soundingPitchClasses[n];
    commonTone = n;
    }
    else
    pitches[n] = new PitchClass(n);
    }
    else
    pitches[n] = null;
    }
    soundingPitchClasses = pitches;
    }
     */

    public void setPedaling(int a){
        pedaling = a;
        //gui.displayPedaling(pedaling);
    }

    public int getCommonTone(boolean[] pitchBinary){
        int commonTone = -1;
        PitchClass[] pitches = new PitchClass[12];
        for(int n = 0; n < 12; n++){
            if(pitchBinary[n]){
                if(soundingPitchClasses[n] != null){
                    pitches[n] = soundingPitchClasses[n];
                    commonTone = n;
                }
                else
                    pitches[n] = new PitchClass(n);
            }
            else
                pitches[n] = null;
        }
        soundingPitchClasses = pitches;
        return commonTone;
    }

    public void transposeSet(int transpositionIndex){
        PitchClass initialPitch = lastSet.getArray()[transpositionIndex];
        PitchClass finalPitch = soundingPitchClasses[transpositionIndex];
        Interval transposition = initialPitch.getInterval(finalPitch);
        if(!transposition.limited)
            transposition.delimit();
        lastSet.transpose(transposition);
    }

    public PitchClass[] updateIntonation(boolean[] pitchBinary){

        boolean isntEmptySet = false;
        for(boolean b: pitchBinary)
            if(b){
                isntEmptySet = true;
                break;
            }
        if(!isntEmptySet)
            return new PitchClass[12];
        if(lastSet != null){
            backupSets.add(lastSet.clone());
            if(backupSets.size() > 10){
                backupSets.remove(0);
            }
            backUpSoundingPCs.add(soundingPitchClasses.clone());
            if(backUpSoundingPCs.size() > 10){
                backUpSoundingPCs.remove(0);
            }
        }
        int setting = pedaling;
        //System.out.println("UPDATE INTONATION");
        int commonTone = getCommonTone(pitchBinary);
        GenerativeSearcher searcher;
        if(commonTone == -1){
            if(lastSet == null){
                lastSet = new VerticalSet(soundingPitchClasses);
                searcher = new VerticalSearcher(lastSet);
                lastSet = searcher.limitSet(setting);
                //System.out.println(lastSet);
                int i = 0;
                while(soundingPitchClasses[i] == null)
                    i++;
                transposeSet(i);
            }
            else{
                HorizontalSet projectedSet = new HorizontalSet(soundingPitchClasses);
                searcher = new HorizontalSearcher(lastSet, projectedSet);
                lastSet = searcher.limitSet(setting);
            }
            //lastSet = lastSet.transformSet(transformationSet);
        }
        else{
            lastSet = new VerticalSet(soundingPitchClasses);
            searcher = new VerticalSearcher(lastSet);
            lastSet = searcher.limitSet(setting);
            transposeSet(commonTone);
        }
        soundingPitchClasses = lastSet.getArray();
        for(int i = 0; i < 12; i++)
            if(soundingPitchClasses[i] != null && !soundingPitchClasses[i].equals(temperedScale[i])){
                temperedScale[i] = soundingPitchClasses[i];
                int channel = soundingPitchClasses[i].signifier + 1;
                if(channel == 10) // a hack for MIDI issues (see MIDI module)
                    channel = 13;
                try{ myCsound.InputMessage("i \"pitchBend\" 0 0 " + channel + " " + soundingPitchClasses[i].getMidiPb());}
                catch(NullPointerException e){}
            }
        //System.out.println("DONE");
        //System.out.println("SCALE: " + Arrays.asList(temperedScale));
        //System.out.println("UPDATED SOUNDING PCS: " + Arrays.asList(soundingPitchClasses));
        if(gui != null)
            if(LiveReceiver.compositionMode)
                gui.displayStaticPitches(soundingPitchClasses);
            else
                gui.displayPitches(soundingPitchClasses);

        return soundingPitchClasses;
        //System.out.println("PITCHES FOUND: " + currentSet.getFoundPitches());
        //System.out.println("PITCHES PROJECTED: " + currentSet.totalProjectedPitches);
    }
    //debuging
    public static void print(Object o){

    }

    public void randomSpotCheck(){
        boolean[] pitchBinary = new boolean[12];
        for(int i = 0; i < 12; i++){
            pitchBinary[i] = (Math.random() > 0.5);
        }
        //updatePitches(pitchBinary);
        updateIntonation(pitchBinary);

        System.out.println(Arrays.asList(pitchBinary));
    }

    public void inifinitTest(){
        while(true)
            randomSpotCheck();
    }

    public static PitchClass[] parsePitchBinary(String binary){
        if(binary.length() != 12){
            System.out.println("Error. Invalid input. No big deal, it happens to everyone.");
            return null;
        }
        PitchClass[] pitchBinary = new PitchClass[12];
        for(int i = 0; i < 12; i++){
            if(binary.substring(i, i+1).equals("1"))
                pitchBinary[i] = new PitchClass(i);
        }
        initializeSpace();
        return pitchBinary;
    }

    public static void initializeSpace(){
        ORIGIN = new PitchClass(new int[]{0, 0});
    }

    public static void animateGeneration(SemioticFunction sf, int[] signified){
        if(gui != null){
            gui.animateGeneration(sf, signified);
        }
    }

    public static void displaySigns(ArrayList<SemioticFunction> s){
        if(gui != null)
            gui.displaySigns(s);
    }


    public static void displaySign(SemioticFunction s){
        ArrayList<SemioticFunction> signs = new ArrayList<SemioticFunction>();
        signs.add(s);
        displaySigns(signs);
    }

    public void undoProgression(){
        System.out.println("UNDO PROGRESSION");
        if(backupSets.size() == 0 || backUpSoundingPCs.size() == 0)
            return;

        lastSet = backupSets.get(backupSets.size() - 1);
        backupSets.remove(backupSets.size() - 1);
        soundingPitchClasses = backUpSoundingPCs.get(backUpSoundingPCs.size() - 1);
        backUpSoundingPCs.remove(backUpSoundingPCs.size() - 1);
        if(gui != null)
            gui.eraseLastChord();
    }

    public void setLastChord(PitchSet aSet){
        soundingPitchClasses = aSet.getArray();
        lastSet = aSet;
    }

}
