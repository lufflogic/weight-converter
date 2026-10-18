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

package com.fluffy.luffs.weight.converter.controllers.model;

import com.fluffy.luffs.weight.converter.controllers.Formula;
import com.fluffy.luffs.weight.converter.i18n.I18n;

/** Weight */
public enum Weight {
  KG("unit.kilograms") {
    @Override
    public String getFormulaResult(String value) {
      return Formula.create().kilosToStones(Double.parseDouble(value));
    }

    @Override
    public String convertInput(String value) {
      return Formula.create().poundsToKilos(Double.parseDouble(value));
    }

    @Override
    public double getKilos(String value) {
      return Double.parseDouble(value);
    }
  },
  LBS("unit.pounds") {
    @Override
    public String getFormulaResult(String value) {
      return Formula.create().poundsToStones(Double.parseDouble(value));
    }

    @Override
    public String convertInput(String value) {
      return Formula.create().kilosToPounds(Double.parseDouble(value));
    }

    public double getKilos(String value) {
      return Formula.create().kilos(Double.parseDouble(value));
    }
  },
  STONES("unit.stones"){
      @Override
      public String getFormulaResult(String value) {
          throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
      }

      @Override
      public String convertInput(String value) {
          throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
      }

      @Override
      public double getKilos(String value) {
          throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
      }
  };

  private final String nameKey;

  private Weight(String nameKey) {
    this.nameKey = nameKey;
  }

  /**
   * Get the Full Name
   *
   * @return String
   */
  public String getFullName() {
    return I18n.text(nameKey);
  }

  /**
   * Gets the string representation of the calculated result.
   *
   * @param value to be calculated.
   * @return String
   */
  public abstract String getFormulaResult(String value);

  /**
   * Gets the converted input value.
   *
   * @param value to be calculated.
   * @return String
   */
  public abstract String convertInput(String value);

  /**
   * Gets the kilo value of the input value
   * @param value to be calculated.
   * @return {@link double}
   */
  public abstract double getKilos(String value);
}
