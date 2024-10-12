import javax.sound.midi.*;
/**
 * Write a description of class DefaultSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class DefaultSynth implements Receiver
{
    public MidiChannel[] channels;
    Synthesizer synth;
    public DefaultSynth()
    {
        try{
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();
        }catch(MidiUnavailableException e){

        }

    }

    public void test(){

        try{
            send(new ShortMessage(ShortMessage.PITCH_BEND, 1, 64,64), 0L);
            send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 64), 0L);
        }
        catch(InvalidMidiDataException e){
            System.out.println(e);
        }
        System.out.println(synth.isOpen());
    }

    public void send(MidiMessage m, long delay){
        if(!(m instanceof ShortMessage))
            return;
        ShortMessage sm = (ShortMessage)m;
        switch(sm.getCommand()){
            case ShortMessage.NOTE_ON:
            channels[sm.getChannel()].noteOn(sm.getData1(), sm.getData2());
            break;
            case ShortMessage.PITCH_BEND:
            channels[sm.getChannel()].setPitchBend(128 * sm.getData2() + sm.getData1());
            break;
            case ShortMessage.CONTROL_CHANGE:
            channels[sm.getChannel()].controlChange(sm.getData1(), sm.getData2());
            break;

        }
    }

    public void close(){}
}
