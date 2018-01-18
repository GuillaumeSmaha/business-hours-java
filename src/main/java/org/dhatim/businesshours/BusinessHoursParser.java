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

import java.time.DayOfWeek;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;

class BusinessHoursParser {

    /**
     * Maps containing the fields supported by the parsing The keys are the
     * field itself, while the value is an Entry containing:
     * <ul>
     * <li>A pattern allowing to identify the field range in the input
     * string</li>
     * <li>A function to parse field values into integers</li>
     * </ul>
     */
    private static final Map<ChronoField, Entry<Pattern, ToIntFunction<String>>> SUPPORTED_FIELDS
            = Collections.unmodifiableMap(new LinkedHashMap<ChronoField, Entry<Pattern, ToIntFunction<String>>>() {
                {
                    put(ChronoField.SECOND_OF_MINUTE, new SimpleImmutableEntry<>(Pattern.compile("(?:sec|second) *\\{(.*?)\\}"), Integer::parseInt));
                    put(ChronoField.MINUTE_OF_HOUR, new SimpleImmutableEntry<>(Pattern.compile("(?:min|minute) *\\{(.*?)\\}"), Integer::parseInt));
                    put(ChronoField.HOUR_OF_DAY, new SimpleImmutableEntry<>(Pattern.compile("(?:hr|hour) *\\{(.*?)\\}"), BusinessHoursParser::hourStringToInt));
                    put(ChronoField.DAY_OF_WEEK, new SimpleImmutableEntry<>(Pattern.compile("(?:wday|wd) *\\{(.*?)\\}"), BusinessHoursParser::weekDayStringToInt));
                    put(ChronoField.DAY_OF_MONTH, new SimpleImmutableEntry<>(Pattern.compile("(?:md|mday) *\\{(.*?)\\}"), Integer::parseInt));
                    put(ChronoField.DAY_OF_YEAR, new SimpleImmutableEntry<>(Pattern.compile("(?:yd|yday) *\\{(.*?)\\}"), Integer::parseInt));
                    put(ChronoField.MONTH_OF_YEAR, new SimpleImmutableEntry<>(Pattern.compile("(?:mo|month) *\\{(.*?)\\}"), BusinessHoursParser::monthStringToInt));
                    put(ChronoField.YEAR, new SimpleImmutableEntry<>(Pattern.compile("(?:yr|year) *\\{(.*?)\\}"), Integer::parseInt));
                }
            });

    private static final Pattern TWELVE_HOURS_TIME_PATTERN = Pattern.compile("(\\d{1,2})(am|noon|pm)");

    private static final Map<String, Integer> WEEKDAYS_MAPPING
            = Collections.unmodifiableMap(new HashMap<String, Integer>() {
                {
                    put("mo", DayOfWeek.MONDAY.getValue());
                    put("tu", DayOfWeek.TUESDAY.getValue());
                    put("we", DayOfWeek.WEDNESDAY.getValue());
                    put("th", DayOfWeek.THURSDAY.getValue());
                    put("fr", DayOfWeek.FRIDAY.getValue());
                    put("sa", DayOfWeek.SATURDAY.getValue());
                    put("su", DayOfWeek.SUNDAY.getValue());
                }
            });

    private static final Map<String, Integer> MONTHS_MAPPING
            = Collections.unmodifiableMap(new HashMap<String, Integer>() {
                {
                    put("jan", Month.JANUARY.getValue());
                    put("feb", Month.FEBRUARY.getValue());
                    put("mar", Month.MARCH.getValue());
                    put("apr", Month.APRIL.getValue());
                    put("may", Month.MAY.getValue());
                    put("jun", Month.JUNE.getValue());
                    put("jul", Month.JULY.getValue());
                    put("aug", Month.AUGUST.getValue());
                    put("sep", Month.SEPTEMBER.getValue());
                    put("oct", Month.OCTOBER.getValue());
                    put("nov", Month.NOVEMBER.getValue());
                    put("dec", Month.DECEMBER.getValue());
                }
            });

    private static int hourStringToInt(String hour) {
        try {
            // if the hour is in 24h format
            return Integer.parseInt(hour);
        } catch (NumberFormatException e) {
            Matcher matcher = TWELVE_HOURS_TIME_PATTERN.matcher(hour);
            if (matcher.matches()) {
                // 12 hours format
                int result = Integer.parseInt(matcher.group(1));
                String dayHalf = matcher.group(2);
                if ("am".equals(dayHalf) && result == 12) {
                    // 12am = midnight
                    result = 0;
                } else if ("pm".equals(dayHalf) && result != 12) {
                    // add 12 hours in the afternoon to convert in 24 hours format
                    // (except for 12pm which is noon)
                    result += 12;
                }
                return result;
            } else {
                throw new IllegalArgumentException("Invalid hour format: " + hour);
            }
        }
    }

    private static int weekDayStringToInt(String weekDay) {
        int result;
        try {
            // if the weekday is numeral
            result = Integer.parseInt(weekDay);
        } catch (NumberFormatException e) {
            // if the week day is in letters, only the first two letters are significant
            result = Optional.of(weekDay)
                    .filter(wd -> wd.length() >= 2)
                    .map(wd -> wd.toLowerCase(Locale.ENGLISH).substring(0, 2))
                    .map(WEEKDAYS_MAPPING::get)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid weekday value: " + weekDay));
        }
        return result;
    }

    private static int monthStringToInt(String month) {
        int result;
        try {
            // if the weekday is numeral
            result = Integer.parseInt(month);
        } catch (NumberFormatException e) {
            // if the month is in letters, only the first three letters are significant
            result = Optional.of(month)
                    .filter(wd -> wd.length() >= 3)
                    .map(wd -> wd.toLowerCase(Locale.ENGLISH).substring(0, 3))
                    .map(MONTHS_MAPPING::get)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid month value: " + month));
        }
        return result;
    }

    private static Set<String> getStringRanges(String subPeriod, Pattern pattern) {
        Set<String> ranges = new HashSet<>();
        Matcher matcher = pattern.matcher(subPeriod);
        while (matcher.find()) {
            String match = matcher.group(1).trim();
            Arrays.stream(match.split(" ")).forEach(ranges::add);
        }
        return ranges;
    }

    private static Stream<ValueRange> getRange(String stringRange, ValueRange fullRange, ToIntFunction<String> valueParser) {
        int start;
        int end;
        String[] boundaries = stringRange.split("-");
        switch (boundaries.length) {
            case 1:
                start = end = valueParser.applyAsInt(boundaries[0]);
                break;
            case 2:
                start = valueParser.applyAsInt(boundaries[0]);
                end = valueParser.applyAsInt(boundaries[1]);
                break;
            default:
                throw new IllegalArgumentException("Invalid range: " + stringRange);
        }
        return start <= end
                ? Stream.of(ValueRange.of(start, end))
                : Stream.of(ValueRange.of(start, fullRange.getMaximum()), ValueRange.of(fullRange.getMinimum(), end));
    }

    /**
     * compute every possible field range combination
     *
     * @param acceptedRanges the accepted ranges for each field. (The map must
     * be sorted to guarantee a consistent iteration order)
     * @return a set containing all the possible combinations of field ranges
     */
    private static Set<NavigableMap<ChronoField, ValueRange>> getRangeCombinations(SortedMap<ChronoField, List<ValueRange>> acceptedRanges) {
        int combinationNb = acceptedRanges.values().stream()
                .mapToInt(List::size)
                .reduce(1, (a, b) -> a * b);

        Set<NavigableMap<ChronoField, ValueRange>> combinations = new HashSet<>(combinationNb);
        for (int i = 0; i < combinationNb; i++) {
            int divisor = 1;
            NavigableMap<ChronoField, ValueRange> combination = new TreeMap<>();
            for (Map.Entry<ChronoField, List<ValueRange>> entry : acceptedRanges.entrySet()) {
                List<ValueRange> ranges = entry.getValue();
                combination.put(entry.getKey(), ranges.get((i / divisor) % ranges.size()));
                divisor *= entry.getValue().size();
            }
            combinations.add(combination);
        }

        return combinations;
    }

    private static int getRangeLength(ValueRange range) {
        return range.checkValidIntValue(range.getMaximum(), null)
                - range.checkValidIntValue(range.getMinimum(), null) + 1;
    }

    /**
     * Break down the provided ranges combination into a set of continuous
     * business periods
     *
     * @param ranges the range combination
     * @return a stream of {@link BusinessPeriod}
     */
    private static Stream<BusinessPeriod> toBusinessPeriods(NavigableMap<ChronoField, ValueRange> ranges) {
        //no need to break the least significant range since it already is continuous
        ChronoField firstField = ranges.firstKey();
        ValueRange firstRange = ranges.get(firstField);
        NavigableMap<ChronoField, ValueRange> remainingRanges = ranges.tailMap(firstField, false);

        //compute the total number of periods
        int periodNb = remainingRanges
                .values()
                .stream()
                .mapToInt(BusinessHoursParser::getRangeLength)
                .reduce(1, (a, b) -> a * b);
        List<BusinessPeriod> periods = new ArrayList<>();

        //compute all the periods
        for (int i = 0; i < periodNb; i++) {
            int divisor = 1;
            Map<ChronoField, Integer> startFields = new HashMap<>();
            Map<ChronoField, Integer> endFields = new HashMap<>();
            startFields.put(firstField, firstRange.checkValidIntValue(firstRange.getMinimum(), firstField));
            endFields.put(firstField, firstRange.checkValidIntValue(firstRange.getMaximum(), firstField));
            for (Map.Entry<ChronoField, ValueRange> entry : remainingRanges.entrySet()) {
                ChronoField field = entry.getKey();
                ValueRange range = entry.getValue();
                int rangeLength = getRangeLength(range);
                int value = range.checkValidIntValue(range.getMinimum(), field) + (i / divisor) % rangeLength;
                startFields.put(field, value);
                endFields.put(field, value);
                divisor *= rangeLength;
            }

            // Add missing first fields with max range
            for (ChronoField field : Arrays.asList(ChronoField.SECOND_OF_MINUTE, ChronoField.MINUTE_OF_HOUR, ChronoField.HOUR_OF_DAY)) {
                if (!startFields.containsKey(field)) {
                    ValueRange fieldRange = field.range();
                    startFields.put(field, fieldRange.checkValidIntValue(fieldRange.getMinimum(), field));
                    endFields.put(field, fieldRange.checkValidIntValue(fieldRange.getMaximum(), field));
                }
                else {
                    break;
                }
            }

            // // Add missing first fields with max range
            // for (Map.Entry<ChronoField, Entry<Pattern, ToIntFunction<String>>> entry : SUPPORTED_FIELDS.entrySet()) {
            //     ChronoField field = entry.getKey();
            //     if (!startFields.containsKey(field)) {
            //         ValueRange fieldRange = field.range();
            //         startFields.put(field, fieldRange.checkValidIntValue(fieldRange.getMinimum(), field));
            //         endFields.put(field, fieldRange.checkValidIntValue(fieldRange.getMaximum(), field));
            //     }
            //     else {
            //         break;
            //     }
            // }


            periods.add(new BusinessPeriod(BusinessTemporal.of(startFields), BusinessTemporal.of(endFields)));
        }

        return periods.stream();
    }

    public static Set<BusinessPeriod> parse(String businessHours) {
        //split the string in distinct sub periods,
        //convert them in business periods
        //and merge intersecting periods
        return BusinessPeriod.merge(
                Arrays.stream(businessHours.split(","))
                .flatMap(BusinessHoursParser::parseSubBusinessHours)
                .collect(Collectors.toSet()));
    }

    private static Stream<BusinessPeriod> parseSubBusinessHours(String subPeriod) {
        //compute the list of acceptable ranges for each field
        SortedMap<ChronoField, List<ValueRange>> acceptedRanges = new TreeMap<ChronoField, List<ValueRange>>();
        SUPPORTED_FIELDS.forEach(
                (field, parsingElts) -> {
                    List<ValueRange> ranges = getStringRanges(subPeriod, parsingElts.getKey())
                        .stream()
                        .flatMap(stringRange -> getRange(stringRange, field.range(), parsingElts.getValue()))
                        .collect(Collectors.toList());
                    if (ranges.size() > 0) {
                        acceptedRanges.put(field, ranges);
                    }
                });


        // When there is no field, insert a default field from 0 to 60 minutes
        if (acceptedRanges.size() == 0) {
            acceptedRanges.put(ChronoField.MINUTE_OF_HOUR, Collections.singletonList(ChronoField.SECOND_OF_MINUTE.range()));
        }

        //get all range combination and convert them to business periods
        return getRangeCombinations(acceptedRanges)
                .stream()
                .flatMap(BusinessHoursParser::toBusinessPeriods);
    }

}
