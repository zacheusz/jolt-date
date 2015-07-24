package eu.zacheusz.jolt.date;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Removr;
import com.bazaarvoice.jolt.exception.SpecException;

import java.io.IOException;
import java.util.Map;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @author zacheusz@adobe.com
 * @date 7/22/2015
 */
public class DaterTest {
    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
                {"timestampComplex"},
                {"formats"}


        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName) throws IOException {

        String testPath = "/json/date/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.classpathToMap(testPath + ".json");

        Object input = testUnit.get("input");
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Dater dater = new Dater( spec );
        Object actual = dater.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

//    @DataProvider
//    public Object[][] getNegativeTestCaseNames() {
//        return new Object[][] {
//                {"negativeTestCases"}
//        };
//    }
//
//    @Test(dataProvider = "getNegativeTestCaseNames", expectedExceptions = SpecException.class)
//    public void runNegativeTestCases(String testCaseName) throws IOException {
//
//        String testPath = "/json/removr/" + testCaseName;
//        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath + ".json" );
//
//        Object spec = testUnit.get( "spec" );
//        new Removr( spec );
//    }
}
