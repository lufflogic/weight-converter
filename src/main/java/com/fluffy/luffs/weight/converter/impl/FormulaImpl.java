package com.fluffy.luffs.weight.converter.impl;

import com.fluffy.luffs.weight.converter.Formula;
import java.math.BigDecimal;
import java.math.RoundingMode;


public class FormulaImpl implements Formula {

    private static final double KILO_TO_STONE = 6.350293;
    private static final int STONES_TO_LBS = 14;
    private static final int LBS_TO_OUNCES = 16;
    private static final double KILO_TO_POUNDS = 0.45359237;
    private static final double POUNDS_TO_KILOS = 2.20462262185;
    
    @Override
    public String kilosToStones(double kilos) {
        BigDecimal stonesDecimal = new BigDecimal(kilos/KILO_TO_STONE);
        int stones = stonesDecimal.intValue();
        BigDecimal lbsDecimal = stonesDecimal.subtract(new BigDecimal(stones)).multiply(new BigDecimal(STONES_TO_LBS));
        int lbs = lbsDecimal.intValue();
        int ounces = lbsDecimal.subtract(new BigDecimal(lbs)).multiply(new BigDecimal(LBS_TO_OUNCES)).setScale(0, RoundingMode.HALF_UP).intValue();
        
        return new StringBuilder().append(stones).append("st ").append(lbs).append("lb ").append(ounces).append("oz").toString();
    }

    @Override
    public String poundsToStones(double pounds) {
        BigDecimal stonesDecimal = new BigDecimal(pounds/STONES_TO_LBS);
        int stones = stonesDecimal.intValue();
        BigDecimal lbsDecimal = stonesDecimal.subtract(new BigDecimal(stones)).multiply(new BigDecimal(STONES_TO_LBS));
        int lbs = lbsDecimal.intValue();
        int ounces = lbsDecimal.subtract(new BigDecimal(lbs)).multiply(new BigDecimal(LBS_TO_OUNCES)).setScale(0, RoundingMode.HALF_UP).intValue();
        
        return new StringBuilder().append(stones).append("st ").append(lbs).append("lb ").append(ounces).append("oz").toString();
    }

    @Override
    public String kilosToPounds(double kilos) {
        return new BigDecimal(kilos/KILO_TO_POUNDS).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public String poundsToKilos(double pounds) {
        return new BigDecimal(pounds/POUNDS_TO_KILOS).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
    
}
