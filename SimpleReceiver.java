
import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import javax.swing.JOptionPane;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Write a description of class SimpleReceiver here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class SimpleReceiver implements Receiver
{
    public Receiver rec;
    public TimerTask timerTask;
    public Timer clock;
    public LinkedBlockingQueue<ShortMessage> commands;
    public int transposition, bendAdjustment;
    public GUI gui;
    public long delay;
    public static final int MIDPED = 66, LEFTPED = 67, RIGHTPED = 64;
    public boolean rightPedal, leftPedal, middlePedal;
    public boolean useDelay = true, sustain = true;
    public int[] chanTrans;
    /*
     * In theory, it may be possible to avoid any delay window,
     * by sending the note on message immediately, followed by
     * all necessary pitch bends once a delay has passed (and chord is
     * detected)
     */

    public static void main(String[] args){
        new SimpleReceiver();
    }

    public SimpleReceiver()
    {
        try{
            rec = MidiSystem.getReceiver();
            Transmitter trans = MidiSystem.getTransmitter();
            trans.setReceiver(this);
        }catch(MidiUnavailableException e){
            JOptionPane.showMessageDialog(null,
                "No MIDI device found. Don't panic! If you're trying to perform\nlive on the Archinoivca, please connect a MIDI keyboard to the \ncomputer, and then restart the software.");
        }

        transposition = 0; // transpose by 0 EQ'd semitones to begin
        bendAdjustment = 0;
        chanTrans = new int[16];

        delay = 30L;
        rightPedal = false;
        leftPedal = false;
        middlePedal = false;

        clock = new Timer();
        commands = new LinkedBlockingQueue<ShortMessage>();
        gui = new GUI(null);

        JOptionPane.showMessageDialog(null,
            "rec = " + rec);
            GUI.simpRec = this;
    }
    
      public void setupDataFlow(){

        try{
            Transmitter trans = null;
            boolean transmitterFound = false;

            do{
                MidiDevice.Info transInfo = (MidiDevice.Info)JOptionPane.showInputDialog(null, 
                        "Please select a source. From which device should\nthe archinovica RECEIVE messages?", "MIDI IN", 
                        JOptionPane.QUESTION_MESSAGE, null, MidiSystem.getMidiDeviceInfo(), MidiSystem.getMidiDeviceInfo()[0]);
                List<Transmitter> ts = MidiSystem.getMidiDevice(transInfo).getTransmitters();
                if(ts.size() == 1){
                    trans = ts.get(0);
                    transmitterFound = true;
                }
                else if(ts.size() == 0){
                    int selection = JOptionPane.showConfirmDialog(null,"The device you've selected doesn't send midi messages.\nPlease select a different device", "Invalid Selection", JOptionPane.OK_CANCEL_OPTION);
                    transmitterFound = false;
                    if(selection == JOptionPane.CANCEL_OPTION)
                        return;
                }
                else{

                    Integer[] options = new Integer[ts.size()];
                    for(int i = 0; i < options.length; i++){
                        options[i] = i+1;
                    }
                    int def = -1;

                    try{
                        Transmitter sysTrans = MidiSystem.getTransmitter();
                        for(int i = 0; i < ts.size(); i++){
                            Transmitter aTrans = ts.get(i);
                            if(aTrans == sysTrans)
                                def = i + 1;
                        }
                    }
                    catch(MidiUnavailableException e){

                    }
                    String aStr = "";
                    if(def > -1)
                        aStr = "/nThe system default is " + def;
                    Integer transPort = (Integer)JOptionPane.showInputDialog(null, 
                            transInfo + " can send Midi data from multiple ports. Please select one to use." + aStr, "MIDI IN", 
                            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    trans = ts.get(transPort - 1);
                    transmitterFound = true;
                }
            }
            while(!transmitterFound);

            boolean recFound = true;

            do{
                Object[] recOptions = new Object[MidiSystem.getMidiDeviceInfo().length + 1];
                for(int i = 0; i < MidiSystem.getMidiDeviceInfo().length; i++)
                    recOptions[i] = MidiSystem.getMidiDeviceInfo()[i];
                recOptions[recOptions.length - 1] = "SYSTEM DEFAULT";
                Object recInfo = JOptionPane.showInputDialog(null, 
                        "Please select a destination. To which device\nshould the archinovica SEND messages?", "MIDI OUT", 
                        JOptionPane.QUESTION_MESSAGE, null, recOptions, recOptions[0]);

                List<Receiver> rs = new ArrayList<Receiver>();
                if(recInfo instanceof MidiDevice.Info){
                    MidiDevice dev = MidiSystem.getMidiDevice((MidiDevice.Info)recInfo);
                    rs = dev.getReceivers();
                }
                else
                    rs.add(new DefaultSynth());
                //dev.open();
                //dev = MidiSystem.getSynthesizer();

                if(rs.size() == 1){
                    rec = rs.get(0);
                    recFound = true;
                }
                else if(rs.size() == 0){
                    int selection = JOptionPane.showConfirmDialog(null,"The device you've selected doesn't receive midi messages.\nPlease select a different device", "Invalid Selection", JOptionPane.OK_CANCEL_OPTION);
                    recFound = false;
                    if(selection == JOptionPane.CANCEL_OPTION)
                        return;
                }
                else{

                    Integer[] options = new Integer[rs.size()];
                    for(int i = 0; i < options.length; i++){
                        options[i] = i+1;
                    }
                    int def = -1;
                    try{
                        Receiver sysRec = MidiSystem.getReceiver();
                        for(int i = 0; i < rs.size(); i++){
                            Receiver aRec = rs.get(i);
                            if(aRec == sysRec)
                                def = i + 1;
                        }
                    }
                    catch(MidiUnavailableException e){}
                    String aStr = "";
                    if(def > -1)
                        aStr = "/nThe system default is " + def;
                    Integer recPort = (Integer)JOptionPane.showInputDialog(null, 
                            recInfo + " can receive Midi data from multiple ports. Please select one to use." + aStr, "MIDI OUT", 
                            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    rec = rs.get(recPort - 1);
                    recFound = true;
                }   
            }while(!recFound);

        }catch(MidiUnavailableException e){
            JOptionPane.showMessageDialog(null,
                "No MIDI device found. Don't panic! If you're trying to perform\nlive on the Archinoivca, please connect a MIDI keyboard to the \ncomputer, and then restart the software.");
        }
    }

    public void send(MidiMessage message, long timeStamp){
        if(message instanceof ShortMessage){
            ShortMessage sm = (ShortMessage)message;

            switch(sm.getCommand()){
                case ShortMessage.NOTE_ON:

                //determine appropriate parameters for message to send
                int channel = sm.getData1() % 12; // why was there + 1?
                if(channel == 9)
                    channel = 13;

                //synonym for note off (ignored)
                if(sm.getData2() == 0){
                    if(sustain)
                        return;
                    else{
                        try{
                            ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_ON, channel, sm.getData1() + 2 * chanTrans[channel], sm.getData2());
                            rec.send(aSM, 0L);
                        }catch(InvalidMidiDataException e){
                            JOptionPane.showMessageDialog(null,
                                "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
                        }
                    }
                }

                //it's really a noteON, so cancel any pending commands
                if(timerTask != null)
                    timerTask.cancel();

                int type = ShortMessage.NOTE_ON;
                try{
                    ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_ON, channel, sm.getData1() + 2 * transposition, sm.getData2());
                    modifyCommands(false, aSM);
                    chanTrans[channel] = transposition;
                }catch(InvalidMidiDataException e){
                    JOptionPane.showMessageDialog(null,
                        "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
                }

                timerTask = new TimerTask(){
                    public void run(){
                        modifyCommands(true, null);
                    };
                };
                clock.schedule(timerTask, delay);

                break;
                case ShortMessage.NOTE_OFF:
                if(sustain)
                    return;
                channel = sm.getData1() % 12; // why was there + 1?
                if(channel == 9)
                    channel = 13;
                try{
                    ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_OFF, channel, sm.getData1() + 2 * chanTrans[channel], sm.getData2());
                    rec.send(aSM, 0L);
                }catch(InvalidMidiDataException e){
                    JOptionPane.showMessageDialog(null,
                        "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
                }
                break;
                case ShortMessage.CONTROL_CHANGE:
                switch(sm.getData1()){
                    case RIGHTPED:
                    int pedaling = 0;
                    if(leftPedal)
                        pedaling += 1;
                    if(sm.getData2() > 0)
                        pedaling += 2;

                    if(rightPedal != sm.getData2() > 0){
                        gui.archinovica.setPedaling(pedaling);
                    }
                    rightPedal = sm.getData2() > 0;
                    break;
                    case LEFTPED:
                    pedaling = 0;
                    if(sm.getData2() > 0)
                        pedaling += 1;
                    if(rightPedal)
                        pedaling += 2;
                    if(leftPedal != (sm.getData2() > 0)){
                        gui.archinovica.setPedaling(pedaling);
                    }

                    leftPedal = sm.getData2() > 0;

                    if(middlePedal && leftPedal){
                        gui.archinovica.undoProgression();
                    }
                    break;
                    case MIDPED:
                    //System.out.println(sm.getData2());
                    middlePedal = sm.getData2() > 0;
                    if(middlePedal){
                        for(int chnl = 0; chnl < 14; chnl++){
                            try{
                                rec.send( new ShortMessage(ShortMessage.CONTROL_CHANGE, chnl, 120, 0), 0);
                            }
                            catch(InvalidMidiDataException e){
                                JOptionPane.showMessageDialog(null,
                                    "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
                            }
                        }
                        /* try{
                        Synthesizer synth = MidiSystem.getSynthesizer();
                        MidiChannel[] chnls = synth.getChannels();
                        for(MidiChannel mc: chnls)
                        mc.allNotesOff();
                        System.out.println("SYNTH KILLED");
                        }
                        catch(Exception e){}
                        for(int chnl = 0; chnl < 15; chnl++)
                        for(int noteNum = 0; noteNum < 128; noteNum++){
                        try{
                        ShortMessage aSM = new ShortMessage(ShortMessage.NOTE_OFF, chnl, noteNum, 0);
                        rec.send(aSM, 0L);
                        }catch(InvalidMidiDataException e){
                        JOptionPane.showMessageDialog(null,
                        "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
                        }
                        }
                         */
                    }
                    break;
                }
                break;
            }
        }

    }

    public synchronized void modifyCommands(boolean play, ShortMessage aSM){
        if(play){
            ArrayList<ShortMessage> playable = new ArrayList<ShortMessage>();
            boolean[] ps = new boolean[12];
            while(commands.size() > 0)
            {
                try{
                    ShortMessage sm = commands.take();
                    playable.add(sm);
                    ps[(sm.getData1() - transposition * 2) % 12] = true;
                }
                catch(InterruptedException e){
                    JOptionPane.showMessageDialog(null,
                        "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
                }
            }

            playable = updateIntonation(ps, playable);
            for(ShortMessage sm: playable){
                rec.send(sm, 0L);
            }
            return;
        }
        try{
            commands.put(aSM);
        }
        catch(InterruptedException e){
            JOptionPane.showMessageDialog(null,
                "Something went wrong!  I'm not sure what :-/\nMaybe you can figure it out?");
        }

    }

    public void close(){}

    public ArrayList<ShortMessage> updateIntonation(boolean[] ps, ArrayList<ShortMessage> sms){
        PitchClass[] pcs = gui.archinovica.updateIntonation(ps);
        for(int i = 0; i < 12; i++){ // calculate the transposition of all pitches
            PitchClass p = pcs[i];
            if(p != null)
                try{

                    //synth.getChannels()[channel].setPitchBend((int)(p.getMidiPb() * 128));
                    //System.out.println(p);

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
                            //keyTransposition[sm.getData1() - originalTransposition] = transposition;
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
                    rec.send(pbM, 0L);
                }
                catch(Exception e){}
            }
        }
        return sms;
    }

}
