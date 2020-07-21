/*
MIT License

Copyright (c) 2020 Chris Luff

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 *
 * PastWeight
 */
public class PastWeight {

    private final String weight;
    private final LocalDateTime date;
    private final long id;

    /**
     * Constructor
     * @param id unique id of the stored weight.
     * @param weight formatted string of the stored weight.
     * @param date date of the stored weight.
     */
    public PastWeight(long id, String weight, LocalDateTime date) {
        this.weight = weight;
        this.date = date;
        this.id = id;
    }

    /**
     * Get the value of the stored weight.
     * @return {@link String}
     */
    public String getWeight() {
        return weight;
    }

    /**
     * Get the date of the stored weight.
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Get the weekday of the stored weight.
     * @return {@link String}
     */
    public String getPastWeightWeekDay() {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }

    /**
     * Get the date of the stored weight in the format dd MMM YYYY.
     * @return {@link String}
     */
    public String getPastWeightFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd MMM YYYY"));
    }

    /**
     * Get the unique id of the stored weight.
     * @return {@ long}
     */
    public long getId() {
        return id;
    }

}
