

import java.util.ArrayList;
import java.util.Arrays;
/**
 * Aggiungi qui una descrizione della classe PitchClass
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */
public class PitchClass extends SemioticFunction
{

    //constructs a limited PC
    public PitchClass(int[] np)
    {
        super(np, -1);
    }

    public PitchClass(int[] transformation, PitchClass pc)
    {
        super(transformation, pc);
        signified = new int[2];
        for(int i = 0; i < 2; i++)
            signified[i] = pc.signified[i] + transformation[i];
        signifier = getSignifier(signified);
        limited = true;
        //myClass = this;
    }

    //constructs an unlimited PC
    public PitchClass(int emp){
        super(null, emp);
    }

    public PitchClass(SemioticFunction sf){
        super(new int[]{sf.signified[0], sf.signified[1]}, sf.signifier);
        /*if(sf instanceof SubSpace)
        globalCoordinate = ((SubSpace)sf).globalCoordinate;
        else
        globalCoordinate = sf;
         */
    }

    @Override
    public PitchClass clone(){
        if(limited)
            return new PitchClass(new int[]{signified[0], signified[1]});
        return new PitchClass(signifier);
    }

    public PitchClass transpose(Interval interval){
        /*if(interval.limited)
        interval.delimit();  // is this necessary?
         */
        for(int i = 0; i < 2; i++)
            signified[i] = signified[i] + interval.signified[i];
        signifier = getSignifier(signified);
        return this;
    }

    public int getDistance(PitchClass p){
        if(!limited || !p.limited)
            return -1;
        return getInterval(p).expressDistance();
    }

    public Interval getEmpericalInterval(PitchClass pc){
        int empInt = (pc.signifier - signifier) % 12;
        if(empInt < 0)
            empInt += 12;
        return new Interval(empInt);
    }

    public Interval getInterval(PitchClass pc){
        if(limited && pc.limited)
            return new Interval(getTransformation(pc));
        return getEmpericalInterval(pc);
    }

    public int distanceFrom(PitchClass pc){
        return getInterval(pc).expressDistance();
    }

}
