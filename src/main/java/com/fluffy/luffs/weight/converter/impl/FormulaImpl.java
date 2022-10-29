/*
MIT License

Copyright (c) 2022  Fluffy Luffs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.fluffy.luffs.weight.converter.impl;

import com.fluffy.luffs.weight.converter.controllers.Formula;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formula implementation.
 */
public class FormulaImpl implements Formula {

    private static final double KILO_TO_STONE = 6.350293;
    private static final int STONES_TO_LBS = 14;
    private static final int LBS_TO_OUNCES = 16;
    private static final double KILO_TO_POUNDS = 0.45359237;
    private static final double POUNDS_TO_KILOS = 2.20462262185;
    
    @Override
    public String kilosToStones(double kilos) {
        return convertToStones(kilos, KILO_TO_STONE);
    }

    @Override
    public String poundsToStones(double pounds) {
        return convertToStones(pounds, STONES_TO_LBS);
    }

    private String convertToStones(double value, double divisor) {
        BigDecimal stonesDecimal = new BigDecimal(value/divisor);
        int stones = stonesDecimal.intValue();
        BigDecimal lbsDecimal = stonesDecimal.subtract(new BigDecimal(stones)).multiply(new BigDecimal(STONES_TO_LBS));
        int lbs = lbsDecimal.intValue();
        int ounces = lbsDecimal.subtract(new BigDecimal(lbs)).multiply(new BigDecimal(LBS_TO_OUNCES)).setScale(0, RoundingMode.UP).intValue();
        
        return new StringBuilder().append(stones).append("st ").append(lbs).append("lb ").append(ounces).append("oz").toString();
    }
    
    @Override
    public String kilosToPounds(double kilos) {
        return convertUnit(kilos, KILO_TO_POUNDS, 0);
    }

    @Override
    public String poundsToKilos(double pounds) {
        return convertUnit(pounds, POUNDS_TO_KILOS, 2);
    }
    
    private String convertUnit(double value, double divsor, int scale) {
        return new BigDecimal(value/divsor).setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

}
