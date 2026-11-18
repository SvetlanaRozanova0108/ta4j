/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.mocks.MockBarSeriesBuilder;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class BooleanLogicIndicatorTest extends AbstractIndicatorTest<Indicator<Num>, Num> {

    private BarSeries series;
    private Indicator<Boolean> left;
    private Indicator<Boolean> right;

    public BooleanLogicIndicatorTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Before
    public void setUp() {
        series = new MockBarSeriesBuilder().withNumFactory(numFactory).withData(1, 2, 3, 4).build();
        left = new FixedBooleanIndicator(series, true, true, false, false);
        right = new FixedBooleanIndicator(series, true, false, true, false);
    }

    @Test
    public void testAnd() {
        Indicator<Boolean> and = new BooleanLogicIndicator(left, right, BooleanLogicIndicator.Operation.AND);
        assertTrue(and.getValue(0));
        assertFalse(and.getValue(1));
        assertFalse(and.getValue(2));
        assertFalse(and.getValue(3));
    }

    @Test
    public void testOr() {
        Indicator<Boolean> or = new BooleanLogicIndicator(left, right, BooleanLogicIndicator.Operation.OR);
        assertTrue(or.getValue(0));
        assertTrue(or.getValue(1));
        assertTrue(or.getValue(2));
        assertFalse(or.getValue(3));
    }

    @Test
    public void testXor() {
        Indicator<Boolean> xor = new BooleanLogicIndicator(left, right, BooleanLogicIndicator.Operation.XOR);
        assertFalse(xor.getValue(0));
        assertTrue(xor.getValue(1));
        assertTrue(xor.getValue(2));
        assertFalse(xor.getValue(3));
    }

    @Test
    public void testNand() {
        Indicator<Boolean> nand = new BooleanLogicIndicator(left, right, BooleanLogicIndicator.Operation.NAND);
        assertFalse(nand.getValue(0));
        assertTrue(nand.getValue(1));
        assertTrue(nand.getValue(2));
        assertTrue(nand.getValue(3));
    }

    @Test
    public void testNor() {
        Indicator<Boolean> nor = new BooleanLogicIndicator(left, right, BooleanLogicIndicator.Operation.NOR);
        assertFalse(nor.getValue(0));
        assertFalse(nor.getValue(1));
        assertFalse(nor.getValue(2));
        assertTrue(nor.getValue(3));
    }

    @Test
    public void testNot() {
        Indicator<Boolean> not = new BooleanNotIndicator(left);
        assertFalse(not.getValue(0));
        assertFalse(not.getValue(1));
        assertTrue(not.getValue(2));
        assertTrue(not.getValue(3));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void serializationRoundTrip() {
        Indicator<Boolean> original = new BooleanLogicIndicator(left, right, BooleanLogicIndicator.Operation.XOR);

        String json = original.toJson();
        Indicator<Boolean> restored = (Indicator<Boolean>) Indicator.fromJson(series, json);

        assertEquals(original.toDescriptor(), restored.toDescriptor());
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertEquals(original.getValue(i), restored.getValue(i));
        }
    }
}
