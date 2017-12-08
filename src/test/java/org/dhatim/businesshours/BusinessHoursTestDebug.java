/**
 * Copyright 2015 Dhatim
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dhatim.businesshours;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BusinessHoursTestDebug {


    // @Test
    // public void fieldSecond() {
    //     BusinessHours bh = new BusinessHours("sec{0-29}");
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-25T00:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T00:00:29")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T00:00:30")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-26T00:00:59")), false);
    // }


    // @Test
    // public void fieldDayOfMonth() {
    //     BusinessHours bh = new BusinessHours("mday{10}");
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-09T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-10T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-04-11T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-05-09T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-05-10T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2014-05-11T12:00:00")), false);
    // }


    // @Test
    // public void fieldDayOfYear() {
    //     BusinessHours bh = new BusinessHours("yday{50}");
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-18T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-19T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-20T12:00:00")), false);
    // }


    @Test
    public void fieldMonthOfYear() {
        BusinessHours bh = new BusinessHours("mo{jan nov}");
        System.out.println("-----------------------------------");
        System.out.println("-----------------------------------");
        System.out.println("-----------------------------------");
        bh.getOpeningCrons();
        bh.getClosingCrons();
        System.out.println("-----------------------------------");
        System.out.println("-----------------------------------");
        System.out.println("-----------------------------------");
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-01-18T12:00:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-01-19T12:00:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-01-20T12:00:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-18T12:00:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-19T12:00:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-20T12:00:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-11-18T12:00:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-11-19T12:00:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-11-20T12:00:00")), true);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-12-18T12:00:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-12-19T12:00:00")), false);
        assertEquals(bh.isOpen(LocalDateTime.parse("2018-12-20T12:00:00")), false);
    }

    // @Test
    // public void fieldCombineDaysOf() {
    //     BusinessHours bh = new BusinessHours("yday{50}, mday{15}");
    //     System.out.println("-----------------------------------");
    //     System.out.println("-----------------------------------");
    //     System.out.println("-----------------------------------");
    //     bh.getOpeningCrons();
    //     bh.getClosingCrons();
    //     System.out.println("-----------------------------------");
    //     System.out.println("-----------------------------------");
    //     System.out.println("-----------------------------------");
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-01-13T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-01-14T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-01-15T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-13T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-14T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-15T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-18T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-19T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-02-20T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-12-13T12:00:00")), false);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-12-14T12:00:00")), true);
    //     assertEquals(bh.isOpen(LocalDateTime.parse("2018-12-15T12:00:00")), false);
    // }
}
