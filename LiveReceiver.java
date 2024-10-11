import javax.sound.midi.*;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
/**
 * Write a description of class MyReceiver here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class LiveReceiver implements Receiver
{
    public Receiver rec;
    public Synthesizer synth;
    public boolean damper, recordOutput, middlePedal;
    public static boolean compositionMode, damperAlmostOff, rightPedal,
    leftPedal, sustainMode, useBuiltIn;
    public int pedaling;
    public static int transposition, bendAdjustment;
    public static int[] keyTransposition, PitchBendCatalogue = new int[16]; //value of 'transposition' at the time of noteOn
    public long delay = 40L;
    public boolean[] pitchSet;
    public ArrayList<ShortMessage> damperMessages, commands, undeterminedSet;
    public ArrayList<Integer> soundingPitches;
    public GUI gui;
    public Timer clock;
    public long time, recTime;
    public Cunctator verrucosus;
    public Sequence outFile;
    //public AdditiveSynthesizer addSynth;
    public static final int MIDPED = 66, LEFTPED = 67, RIGHTPED = 64;


    public boolean useVirtualPiano;

    public LiveReceiver()
    {
        useVirtualPiano = false;//depreciated?
        compositionMode = false; // defalut is performance mode
        useBuiltIn = false;



        damper = true; // always resonating unless told to clear (middle pedal)
        rightPedal = false;
        leftPedal = false;
        middlePedal = false;
        sustainMode = false;//true;//for Polymath default
        transposition = 0; // transpose by 0 EQ'd semitones to begin
        bendAdjustment = 0;

        keyTransposition = new int[128];

        Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    rec.close();
                }
            });

        undeterminedSet = new ArrayList<ShortMessage>();
        soundingPitches = new ArrayList<Integer>();

        time = System.currentTimeMillis();
        recordOutput = false;
        pedaling = 0;
        verrucosus = new Cunctator();
        clock = new Timer();
        commands = new ArrayList<ShortMessage>();
        damperMessages = new ArrayList<ShortMessage>();
        gui = new GUI(this);
        pitchSet = new boolean[12];
        for(int i = 0; i < 12; i++)
            pitchSet[i] = false;
        try{
            if(useVirtualPiano)
                rec = new Piano();
            else
                rec = MidiSystem.getReceiver();
            synth = MidiSystem.getSynthesizer();
            Transmitter trans = MidiSystem.getTransmitter();
            trans.setReceiver(this);
        }catch(MidiUnavailableException e){
            System.out.println(e);
            System.out.println("CREATING VIRTUAL INSTRUMENT");
            rec = new Piano();
        }

        //addSynth = new AdditiveSynthesizer();

        System.out.println(synth.getChannels().length);
        /*while(true){
        if(time != -1L && System.currentTimeMillis() - time >= delay)
        playNotes();
        else
        System.out.print("");
        }
         */
    }

    public static void main(String[] args){
        new LiveReceiver();
    }

    public static int getKeyTransValue(int index){
        return keyTransposition[index];
    }

    public void test(){
        try{

            //rec.send(new ShortMessage(ShortMessage.PITCH_BEND, 1, 127, 80), 0L);
            rec.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 96), 0L);
            synth.getChannels()[1].setPitchBend(0);
            System.out.println(synth.getChannels()[1].getPitchBend());

            System.out.println(synth.getChannels()[1].getPitchBend());
        }
        catch(Exception e){}
    }

    public void setReceiver(MidiDevice r){
        try{

            List<Receiver> rs = r.getReceivers();
            System.out.println(rs);
            System.out.println(" DEFAULT REC: " + r.getReceiver());

            ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 60);
            rec = r.getReceiver();
            System.out.println("REC = " + rec);
            rec.send(aSM, 0L);
        }
        catch(Exception e){
            System.out.println("REC NOT SET: " + e);
        }
    }

    //depreciated
    public void record(){
        recordOutput = true;
        try{
            outFile = new Sequence(javax.sound.midi.Sequence.PPQ,24);
        }catch(Exception e){}
    }

    public void initializeOutFile(Sequence s){
        try{
            outFile = new Sequence(s.getDivisionType(), s.getResolution(), 16 + 2 * (s.getTracks().length - 1));
            //s.getTracks().length);
        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }

    public void processFile(){

    }

    public void close(){}

    public void useBuiltInMidi(){
        rec = new Piano();
        useBuiltIn = true;
    }

    public void send(MidiMessage message, long timeStamp){
        /*
        byte[] aMess = message.getMessage();
        System.out.print("[");
        for(byte aByte: aMess)
        System.out.print(" " + aByte);
        System.out.println(" ]");
         */
        if(useBuiltIn)
            transposition = 0;
        if(message instanceof ShortMessage){
            ShortMessage sm = (ShortMessage)message;

            if(sm.getChannel() != 8){
                //System.out.println(" | " + sm.getChannel() + " | " + sm.getData1() + " | " + sm.getData2());
            }
            if(sm.getData1() == 4){
                //System.out.println(sm.getCommand());
            }

            // System.out.println("MESSAGE RECEIVED!");
            switch(sm.getCommand()){
                case ShortMessage.NOTE_ON:
                try{
                    int channel = sm.getData1() % 12; // why was there + 1?
                    if(channel == 9)
                        channel = 13;
                    int type = ShortMessage.NOTE_ON;
                    if(sm.getData2() == 0){
                        type = ShortMessage.NOTE_OFF;
                        System.out.println("VELOCITY 0");
                    }
                    ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_ON, channel, sm.getData1() + 2 * transposition, sm.getData2());
                    //System.out.println(sm.getData2());

                    if(type == ShortMessage.NOTE_ON){
                        //pitchCalculator.cancel();
                        //clock.purge();(InterruptedException e){}
                        //System.out.println("NOTE_ON: " + sm.getData1());
                        keyTransposition[sm.getData1()] = transposition; //save transposition data related to this midi input key
                        if(recordOutput){
                            commands.add(aSM);
                            //playNotes(timeStamp);
                        }
                        else{
                            verrucosus.cancel();
                            commands.add(aSM);
                            verrucosus = new Cunctator();
                            clock.schedule(verrucosus, delay);
                        }
                        //System.out.println(commands);
                        //time = System.currentTimeMillis();
                        //clock.start();
                    }
                    else if(type == ShortMessage.NOTE_OFF){
                        /*if(!damper){
                        processMessage(aSM, delay);
                        }
                        else{
                        damperMessages.add(aSM);
                        }
                         */
                        //access transposition value associated with this midi input key
                        aSM.setMessage(ShortMessage.NOTE_ON, channel, sm.getData1() + 2 * keyTransposition[sm.getData1()], sm.getData2());
                        if(compositionMode){
                            verrucosus.cancel();
                            undeterminedSet = new ArrayList<ShortMessage>();
                            commands = new ArrayList<ShortMessage>();
                            //System.out.println("RESET UNDETERMINED SET");
                        }

                        if(recordOutput){
                            commands.add(aSM);
                            //playNotes(timeStamp);
                        }
                        else
                            damperMessages.add(aSM);

                        //System.out.println("DAMPER: " + damperMessages);
                    }
                    pitchSet[sm.getData1() % 12] = (type == ShortMessage.NOTE_ON);
                }catch(InvalidMidiDataException e){
                    //System.out.println("ERROR: " + e);
                }
                /*
                landini.play(sm.getData1(), sm.getData2(), false);
                if(sm.getData1() % 12 == 11){
                System.out.println(" B natural command: " + sm.getCommand()
                + " Velocity: " + sm.getData2());
                try{
                ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_ON, 1, sm.getData1(), sm.getData2());

                processMessage(aSM, (long)0);

                }catch(InvalidMidiDataException e){
                System.out.println("ERROR: " + e);
                }

                }
                myGen.getPitch(sm.getData1());
                System.out.println(sm.getData1());

                /*try{
                ShortMessage sm1 = new ShortMessage();
                sm1.setMessage(ShortMessage.PITCH_BEND, 0, 50, 0);
                processMessage(sm1, timeStamp);
                }
                catch(InvalidMidiDataException e){System.out.println("AHHHH!  Exception: " + e);}

                processMessage(sm, timeStamp);
                if(sm.getData2() == 0){
                try{
                ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_OFF, 1, sm.getData1(), sm.getData2());

                processMessage(aSM, (long)0);

                }catch(InvalidMidiDataException e){
                System.out.println("ERROR: " + e);
                }
                }
                 */
                break;
                case ShortMessage.CONTROL_CHANGE:
                //System.out.println("CONTROL CHANGE!  Data1:" + sm.getData1() + ", Data2:" + sm.getData2());

                //System.out.println("TYPE: " +sm.getType());

                int pedalOn = -1;
                int pedal = 0;
                if(sm.getData2() > 0)
                    pedalOn = 1;
                switch(sm.getData1()){
                    case RIGHTPED:

                    pedaling = 0;
                    if(leftPedal)
                        pedaling += 1;
                    if(sm.getData2() > 0)
                        pedaling += 2;

                    if(rightPedal != sm.getData2() > 0){
                        gui.archinovica.setPedaling(pedaling);
                        if(compositionMode)
                            playUndeterminedSet();
                    }
                    rightPedal = sm.getData2() > 0;
                    //System.out.println("RP: " + rightPedal);
                    /*if(sm.getData2() == 12)
                    damperAlmostOff = true;
                    else if(sm.getData2() > 12)
                    damperAlmostOff = false;
                    if(pedalOn <= 0){
                    if(damperAlmostOff){
                    damper = false;
                    damperAlmostOff = false;
                    }
                    }
                    else
                    damper = true;
                    if(!damper){
                    for(ShortMessage aSM: damperMessages)
                    processMessage(aSM, delay);
                    damperMessages = new ArrayList<ShortMessage>();
                    }
                     */
                    if(middlePedal && rightPedal){
                        sustainMode = !sustainMode;  
                        if(!sustainMode)
                            dampen();
                    }
                    break;
                    case LEFTPED:

                    pedaling = 0;
                    if(sm.getData2() > 0)
                        pedaling += 1;
                    if(rightPedal)
                        pedaling += 2;
                    if(leftPedal != (sm.getData2() > 0)){
                        gui.archinovica.setPedaling(pedaling);
                        if(compositionMode)
                            playUndeterminedSet();
                    }

                    leftPedal = sm.getData2() > 0;

                    if(middlePedal && leftPedal){
                        gui.archinovica.undoProgression();
                        if(compositionMode)
                            playCurrentSet();
                    }
                    //System.out.println("LP: " + leftPedal);
                    //System.out.println("PEDALING: " + pedaling);
                    break;
                    case MIDPED:
                    middlePedal = pedalOn > 0;
                    if(sm.getData2() < 125 && !leftPedal){
                        if(damperMessages.size() > 0){
                            //System.out.println("DAMPEN");
                            verrucosus.cancel();
                            damperMessages.addAll(commands);
                            commands = damperMessages;
                            verrucosus = new Cunctator();
                            clock.schedule(verrucosus, delay);
                            //System.out.println(commands);
                            damperMessages = new ArrayList<ShortMessage>();
                        }
                    }

                    if(middlePedal && leftPedal){
                        gui.archinovica.undoProgression();
                        if(compositionMode)
                            playCurrentSet();
                    }

                    if(middlePedal && rightPedal){
                        sustainMode = !sustainMode;  
                        if(!sustainMode)
                            dampen();
                    }
                    break;
                }
                break;
                case ShortMessage.PITCH_BEND:
                /*System.out.println("PITCH BEND!  Data1:" + sm.getData1() +
                ", Data2:" + sm.getData2());
                 */
                break;
                case ShortMessage.NOTE_OFF:
                //System.out.println("note off: " + sm.getData1() + ", " + sm.getData2());
                int channel = sm.getData1() % 12; // + 1;?
                if(channel == 9)
                    channel = 13;
                try{
                    //System.out.println("channel = " + channel);
                    //access transposition value associated with this midi input key
                    int thisKeyTrans = keyTransposition[sm.getData1()];//for debugging
                    int thisKey = sm.getData1();
                    ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_OFF, channel, sm.getData1() + 2 * keyTransposition[sm.getData1()], sm.getData2());

                    //if(damper)
                    if(recordOutput){
                        commands.add(aSM);
                        //playNotes(timeStamp);
                    }
                    else
                        damperMessages.add(aSM);
                    /*else
                    processMessage(aSM, delay);
                     */
                }
                catch(InvalidMidiDataException e){
                    // System.out.println("ERROR: " + e);
                }
                pitchSet[sm.getData1() % 12] = false;
                break;

            }
            if(sm.getCommand() == 240)
            // System.out.println("240 message: " + sm.getData1());
                return;
            //System.out.println(sm.getCommand());

        }
      

        //System.out.println("Send: " +message);
    }

    public void playNotes(long timeStamp){
        if(commands.size() == 0){
            time = -1L;
            return;
        }
        if(useBuiltIn)
            transposition = 0;
        //boolean containsOnEvent = false;
        boolean[] ps = new boolean[12];
        ArrayList<ShortMessage> noteOns = new ArrayList<ShortMessage>();
        Iterator iter = commands.iterator();
        while(iter.hasNext()){
            ShortMessage sm = (ShortMessage)iter.next();
            if(sm.getCommand() == ShortMessage.NOTE_ON){
                ps[(sm.getData1() - transposition * 2) % 12] = true;
                noteOns.add(sm);
                iter.remove();
            }
        }

        /* for(int i = 0; i < 12; i++) // what's the point of this
        ps[i] = ps[i] || pitchSet[i];
         */
        if(noteOns.size() > 0 && true){// for cons Milano, avoid staccato !!!!!!!!!!!!!!!!!!!!!!!!!!
            recTime = timeStamp;
            commands.addAll(updateIntonation(ps, noteOns));
            if(!sustainMode){
                for(int i = 0; i < damperMessages.size(); i++){
                    boolean aboutToBeSounded = false;
                    /*
                    for(int n = 0; n < commands.size(); n++)
                    if(commands.get(n).getData1() == damperMessages.get(i).getData1()){
                    aboutToBeSounded = true;
                    break;
                    }

                    if(!aboutToBeSounded && !sustainMode){
                     */
                    commands.add(0, damperMessages.get(i));
                    damperMessages.remove(i);
                    i--;

                    //}
                }
                damperMessages = new ArrayList<ShortMessage>();//will I realistically reach overflow?
            }

            /*commands.addAll(damperMessages);// turn off old notes second
            damperMessages = new ArrayList<ShortMessage>();
             */
        }
        else{
            //System.out.println("NO NOTE ON");
        }

        if(compositionMode)
            undeterminedSet.addAll(commands);
        while(commands.size() > 0){
            ShortMessage sm = commands.get(0);
            commands.remove(0);
            processMessage(sm, timeStamp);
        }
        //commands = new ArrayList<ShortMessage>();
        time = -1L;
    }

    public void dampen(){
        while(commands.size() > 0){
            ShortMessage sm = damperMessages.get(0);
            damperMessages.remove(0);
            processMessage(sm, 0L);
        }
    }

    public void playUndeterminedSet(){
        //System.out.println("PLAY UNDETERMINED SET");
        if(undeterminedSet.size() == 0)
            return;
        boolean[] ps = new boolean[12];
        for(ShortMessage sm: undeterminedSet)
            if(sm.getCommand() == ShortMessage.NOTE_ON)
                ps[sm.getData1() % 12] = true;

        for(int i = 0; i < 12; i++) // what's the point of this
            ps[i] = ps[i] || pitchSet[i];

        gui.archinovica.undoProgression();
        updateIntonation(ps, undeterminedSet);
        for(ShortMessage sm: undeterminedSet)
            processMessage(sm, 0L);

        time = -1L;//what's the point of this?
    }

    public void playCurrentSet(){
        PitchClass[] pcs = gui.archinovica.soundingPitchClasses;
        for(int i = 0; i < 12; i++){
            PitchClass p = pcs[i];
            if(p != null)
                try{
                    int channel = i;
                    if(channel == 9)
                        channel = 13;
                    synth.getChannels()[channel].setPitchBend((int)(p.getMidiPb() * 128));
                }catch(Exception e){}
        }
        for(int i = 0; i < 12; i++){
            if(pcs[i] != null){
                try{
                    rec.send(new ShortMessage(ShortMessage.NOTE_ON, i + 60, 63), 0L);
                    damperMessages.add(new ShortMessage(ShortMessage.NOTE_OFF, i + 60, 0));
                }catch(Exception e){}
            }
        }
    }

    public void processMessage(MidiMessage m, long d){
        if(m instanceof ShortMessage){
            ShortMessage sm = (ShortMessage)m;
            if(sm.getCommand() == ShortMessage.NOTE_ON){

                try{
                    ShortMessage noteOff = new ShortMessage(ShortMessage.NOTE_OFF, sm.getChannel(), sm.getData1(), 0);
                    if(!recordOutput && true)//for cons Milano avoid staccato!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        rec.send(noteOff, d);
                    /*for(ShortMessage dm: damperMessages){
                    if(dm.getData1() == sm.getData1()){
                    damperMessages.remove(dm);
                    break;
                    }
                    }
                     */
                    if(recordOutput)
                        outFile.getTracks()[0].add(new MidiEvent(noteOff, d));
                }
                catch(Exception e){//System.out.println(e);
                }
            }

        }
        if(!recordOutput){
            rec.send(m, d);
            //addSynth.send(m, d);
        }
        if(recordOutput){
            //recTime = d;
            MidiEvent event = new MidiEvent(m, d);
            Track[] trks = outFile.getTracks();
            trks[((ShortMessage)m).getChannel()].add(event);
            /*Track[] trks = outFile.getTracks();
            int tkNum = 0;
            class sortByTimeStamp implements Comparator<MidiEvent>{
            public int compare(MidiEvent e1, MidiEvent e2){
            if(e2.getTick() > e1.getTick())
            return 1;
            if(e2.getTick() < e1.getTick())
            return -1;
            return 0;
            }
            }
            Track track = trks[tkNum];
            ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
            for(int i = 0; i < track.size(); i++)
            events.add(track.get(i));
            int index = Collections.binarySearch(events, event, new sortByTimeStamp());
            if(index < 0)
            index = -index - 2;
            if(index < 0)
            index = 0;
            while(track.get(index).getTick() == recTime){
            tkNum++;
            track = trks[tkNum];
            events = new ArrayList<MidiEvent>();
            for(int i = 0; i < track.size(); i++)
            events.add(track.get(i));
            index = Collections.binarySearch(events, event, new sortByTimeStamp());
            if(index < 0)
            index = -index - 2;
            if(index < 0)
            index = 0;
            }
            trks[tkNum].add(event);
             */
        }
    }

    public void recordPitchBend(ShortMessage pbM, long timeStamp){
        Track[] tks = outFile.getTracks();
        try{
            //probably not necessary
            //ShortMessage bendChannelZero = new ShortMessage(pbM.getCommand(), 0, pbM.getData1(), pbM.getData2());

            tks[pbM.getChannel()].add(new MidiEvent(pbM, timeStamp));
            /*
            //add pitchbend to non-archinovica instruments
            for(int n = 1; n * 16 + pbM.getChannel() < tks.length; n++){
            //ShortMessage apbM = new ShortMessage(ShortMessage.PITCH_BEND, channel, 127, bend + bendAdjustment);
            tks[pbM.getChannel() + n * 16].add(new MidiEvent(pbM, timeStamp));
            }
             */

            gui.chordProgression.recordPitchBend(pbM, timeStamp);
        }
        catch(Exception e){}
    }

    /*
    public void updateIntonation(boolean[] ps){
    PitchClass[] pcs = gui.archinovica.updateIntonation(ps);
    for(int i = 0; i < 12; i++){
    PitchClass p = pcs[i];
    if(p != null)
    try{
    int channel = i;
    if(channel == 9)
    channel = 13;
    //synth.getChannels()[channel].setPitchBend((int)(p.getMidiPb() * 128));
    System.out.println(p);

    int bend = (int)p.getMidiPb();
    while(bend + bendAdjustment > 127){
    bendAdjustment -= 127;
    transposition += 1;
    }
    while(bend + bendAdjustment < 0){
    bendAdjustment += 127;
    transposition -= 1;
    }

    rec.send(new ShortMessage(ShortMessage.PITCH_BEND, channel, 127, bend + bendAdjustment), 0L);
    }catch(Exception e){}
    }
    }
     */

    public ArrayList<ShortMessage> updateIntonation(boolean[] ps, ArrayList<ShortMessage> sms){
        PitchClass[] pcs = gui.archinovica.updateIntonation(ps);
        for(int i = 0; i < 12; i++){ // calculate the transposition of all pitches
            PitchClass p = pcs[i];
            if(p != null && !useBuiltIn)
                try{

                    //synth.getChannels()[channel].setPitchBend((int)(p.getMidiPb() * 128));
                    System.out.println(p);

                    int bend = (int)p.getMidiPb();
                    int deltaTransposition = 0;
                    int originalTransposition = transposition;
                    while(bend + bendAdjustment > 127){
                        bendAdjustment -= 64;
                        transposition++;
                        deltaTransposition++;
                    }
                    while(bend + bendAdjustment < 0){
                        bendAdjustment += 64;
                        transposition--;
                        deltaTransposition--;
                    }
                    if(deltaTransposition != 0)
                        for(ShortMessage sm: sms){
                            int thisKey = sm.getData1();
                            int thisKeyTransIndex = sm.getData1() - originalTransposition;
                            keyTransposition[sm.getData1() - originalTransposition] = transposition;
                            sm.setMessage(sm.getCommand(), sm.getChannel(),
                                sm.getData1() + 2 * deltaTransposition, sm.getData2());
                        }
                }catch(Exception e){}
        }

        for(int i = 0; i < 12; i++){//after values are stable (transpostion and bendAdjustment) send PB messages
            PitchClass p = pcs[i];
            if(p != null){
                int channel = i;//IS THIS RIGHT????????
                if(channel == 9)
                    channel = 13;
                int bend = (int)p.getMidiPb();
                try{
                    ShortMessage pbM = new ShortMessage(ShortMessage.PITCH_BEND, channel, 127, bend + bendAdjustment);
                    if(!recordOutput){
                        rec.send(pbM, 0L);
                        PitchBendCatalogue[channel] = bend + bendAdjustment;
                    }
                    if(recordOutput){
                        recordPitchBend(pbM, recTime);//+1
                    }
                }
                catch(Exception e){}
            }
        }
        return sms;
    }

    class Cunctator extends TimerTask{
        public boolean isRunning;
        public Cunctator(){
            isRunning = false;
        }

        @Override
        public void run(){
            isRunning = true;
            playNotes(0L);
            isRunning = false;
        }
    }

    public void displayPitchSet(){
        for(boolean b: pitchSet)
            if(b)
                System.out.print("1");
            else
                System.out.print("0");
        System.out.println();
    }

    public class Piano implements Receiver
    {
        public Synthesizer synth;
        public MidiChannel[] channels;
        public Piano(){
            try{
                synth = MidiSystem.getSynthesizer();
                synth.open();
                channels = synth.getChannels();
            }
            catch(MidiUnavailableException e){
                System.out.println("VI CREATION FAILED: " + e);
            };
        }

        public void close(){}

        public void send(MidiMessage message, long timeStamp){

            if(message instanceof ShortMessage){
                ShortMessage sm = (ShortMessage)message;

                // System.out.println("MESSAGE RECEIVED!");
                switch(sm.getCommand()){
                    case ShortMessage.NOTE_ON:
                    channels[sm.getChannel()].noteOn(sm.getData1(), sm.getData2());
                    break;
                    case ShortMessage.CONTROL_CHANGE:

                    break;
                    case ShortMessage.PITCH_BEND:
                    channels[sm.getChannel()].setPitchBend(sm.getData1());
                    break;
                    case ShortMessage.NOTE_OFF:
                    //System.out.println("NOTE OFF. Channel: " + sm.getChannel());
                    channels[sm.getChannel()].noteOff(sm.getData1());
                    break;

                }
            }
        }
    }

    public void displayDamper(){
        return;

        /*System.out.print("DAMPER: ");
        for(ShortMessage sm: damperMessages){
        System.out.print(sm.getData1() + ", " );
        }
        System.out.println();
         */
    }


}
