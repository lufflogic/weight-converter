package com.fluffy.luffs.weight.converter;

import com.fluffy.luffs.weight.converter.impl.FormulaImpl;


public interface Formula {
    
    public String kilosToStones(double kilos);
    
    public String kilosToPounds(double kilos);
    
    public String poundsToKilos(double pounds);
    
    public String poundsToStones(double pounds);
    
    public static FormulaImpl create() {
        return new FormulaImpl();
    }
    
}
