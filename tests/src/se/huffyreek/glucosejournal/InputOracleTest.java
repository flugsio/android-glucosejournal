package se.huffyreek.glucosejournal;

import junit.framework.TestCase;
import junit.framework.Assert;

public class InputOracleTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testNoConditions() {
        InputOracle inputOracle = new InputOracle();
        Assert.assertFalse(inputOracle.check(""));
        Assert.assertFalse(inputOracle.check("333"));
    }

    public void testLengthIs() {
        InputOracle inputOracle = new InputOracle();

        InputOracle ret = inputOracle.lengthGE(3);

        Assert.assertEquals("should return itself for easy chaining",
                inputOracle, ret);

        Assert.assertFalse(inputOracle.check(""));
        Assert.assertFalse(inputOracle.check("22"));
        Assert.assertTrue(inputOracle.check("333"));
        Assert.assertTrue(inputOracle.check("4444"));
    }
}
