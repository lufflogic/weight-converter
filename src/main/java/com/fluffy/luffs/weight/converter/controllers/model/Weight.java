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
import java.util.regex.Pattern;

/**
 *
 * Weight
 */
public enum Weight {

        KG("Kilograms") {
            @Override
            public Pattern getCompilePattern() {
                return Pattern.compile("-?((\\d{0,3})|(\\d+\\.\\d{0,2}))");
            }

            @Override
            public String getFormulaResult(String value) {
                return Formula.create().kilosToStones(Double.parseDouble(value));
            }

            @Override
            public String convertInput(String value) {
                return Formula.create().poundsToKilos(Double.parseDouble(value));
            }
        },
        LBS("Pounds") {
            @Override
            public Pattern getCompilePattern() {
                return Pattern.compile("-?(\\d{0,3})");
            }

            @Override
            public String getFormulaResult(String value) {
                return Formula.create().poundsToStones(Double.parseDouble(value));
            }

            @Override
            public String convertInput(String value) {
                return Formula.create().kilosToPounds(Double.parseDouble(value));
            }

        };

        private final String fullName;

        private Weight(String fullName) {
            this.fullName = fullName;
        }

        /**
         * Get the Full Name
         *
         * @return String
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * Gets the accepted characters regex compilation.
         *
         * @return {@link Pattern}
         */
        public abstract Pattern getCompilePattern();

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
    }
