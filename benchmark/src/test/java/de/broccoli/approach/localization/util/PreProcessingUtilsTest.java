package de.broccoli.approach.localization.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PreProcessingUtilsTest {

    @Test
    public void testCamelCaseDetection() {
        String textWithCamelCases = "test test Kein Camel Case CamelCase JavaCla MethodeGetterCoolerSache methodeGetterCoolerSache smallStartIch sanllSTATIC punktIstOkay. Utf8Serializer";
        List<String> result = PreProcessingUtils.instance.findCamelCaseWords(textWithCamelCases);
        Assert.assertEquals(8, result.size());

        Assert.assertTrue(result.contains("CamelCase"));
        Assert.assertTrue(result.contains("JavaCla"));
        Assert.assertTrue(result.contains("MethodeGetterCoolerSache"));
        Assert.assertTrue(result.contains("methodeGetterCoolerSache"));
        Assert.assertTrue(result.contains("smallStartIch"));
        Assert.assertTrue(result.contains("sanllSTATIC"));
        Assert.assertTrue(result.contains("punktIstOkay"));
        Assert.assertTrue(result.contains("Utf8Serializer"));
    }

    @Test
    public void testWordsWithDots() {
        String textWithCamelCases = "test.test .testNicht Find.Mich find.mich.auch Mich. nicht com.invalid..ff invalid...f ";
        List<String> result = PreProcessingUtils.instance.findWordsWithDots(textWithCamelCases);
        Assert.assertEquals(3, result.size());

        Assert.assertTrue(result.contains("test.test"));
        Assert.assertTrue(result.contains("Find.Mich"));
        Assert.assertTrue(result.contains("find.mich.auch"));
    }

    @Test
    public void testNaturalProcessing() {
        String text = "Add Gora-Cassandra testsAs per this thread [1], it is absolutely essential that we get some tests for the Cassandra module. Therefore this task should act as a summary task to manage all test which are hereby proposed.\n" +
                "\n" +
                "Realistically, this is going to take a good while, so I have marked it for 0.2 and 0.3-incubating releases.\n" +
                "\n" +
                "[1] http://www.mail-archive.com/gora-dev@incubator.apache.org/msg00241.html";
        List<String> result = PreProcessingUtils.instance.preProcessNaturalLanguage(text);
        Assert.assertTrue(result.size() > 2);
        result.forEach(s -> Assert.assertEquals(s, s.toLowerCase()));

    }

    @Test
    public void testMethodProcessing() {
        String methods = "Dirty fields are not correctly applied after serialization and map clearanceDirty fields are not correctly applied in these situations:\n" +
                "\n" +
                "-All entries in the map are incorrectly made dirty when a Persistent is deserialized.\n" +
                "-Also, due to a separate bug all non-map entries that are deserialized are made dirty too. (This is pure coincidence as the bugs have different causes).\n" +
                "-Finally, when a map is cleared but some entries that are put afterwards are actually the same as before, it should be noticed as non-dirty. (This happens in Nutchgora a lot: getInlinks.clear(), getOutlinks.clear(), getHeaders.clear() etc.).\n" +
                "\n" +
                "Because of this a lot of fields are dirty when they in fact are not. It is never the other way around. Therefore, this bug purely affects performance.\n" +
                "\n" +
                "Patch will follow shortly. Let me know if you have any comments.";
        List<String> result2 = PreProcessingUtils.instance.findJavaMethods(methods);
        Assert.assertTrue(result2.size() > 2);
        Assert.assertTrue(result2.contains("getInlinks.clear"));
        Assert.assertTrue(result2.contains("getOutlinks.clear"));
        Assert.assertTrue(result2.contains("getHeaders.clear"));

        Assert.assertTrue(result2.contains("getInlinks"));
        Assert.assertTrue(result2.contains("getOutlinks"));
        Assert.assertTrue(result2.contains("getHeaders"));
    }

    @Test
    public void testPackageProcessing() {
        String packageAndClassName = "Creates org.apache.gora.cassandra.serializers package in order to clean the code of store and query packages and to support additional types in future.It has been discussed at GORA-81, and a kind of fix has been committed as a part of GORA-138 patch.\n" +
                "Since it is one of the main functionalities of gora-cassandra to handle Serializers, it seems a good idea to separate those code from store and query packages.\n" +
                "I will be attaching my patch shortly.\n" +
                "1) Utf8Serializer makes simple to handle Type.STRING;\n" +
                "2) GoraSerializerTypeInferer is a single point go get Serializer, and it is extensible to support additional types in future;\n" +
                "3) Type.BOOLEAN is supported.";

        List<String> result3 = PreProcessingUtils.instance.findJavaMethods(packageAndClassName);
        Assert.assertTrue(result3.size() > 2);
        Assert.assertTrue(result3.contains("org.apache.gora.cassandra.serializers"));
        Assert.assertTrue(result3.contains("GoraSerializerTypeInferer"));
        Assert.assertTrue(result3.contains("Utf8Serializer"));
    }

    @Test
    public void testFileNameProcessing() {
        String fileNames = "Dirty fields are not correctly .gitlab-yil applied after serialization and map clearanceDirty fields are not correctly applied in these situations:\n" +
                "\n" +
                "-All entries in the map are file.java incorrectly made dirty when a Persistent is deserialized.\n" +
                "-Also, due to a separate bug all non-map entries that are deserialized are made dirty too. (This is pure coincidence as the bugs have different causes).\n" +
                "-Finally, when a map is cleared but some entries that are put afterwards are actually the same as before, it should be noticed as non-dirty. (This happens in Nutchgora a lot: getInlinks.clear(), getOutlinks.clear(), getHeaders.clear() etc.).\n" +
                "\n" +
                "Because of this a text.xml lot of fields are dirty when they in fact are not. It is never the other way around. Therefore, this bug purely affects performance.\n" +
                "\n" +
                "Patch will follow shortly. Let me know if you have any comments.";
        List<String> result4 = PreProcessingUtils.instance.findFileNames(fileNames);
        Assert.assertTrue(result4.size() > 2);
        Assert.assertTrue(result4.contains(".gitlab-yil"));
        Assert.assertTrue(result4.contains("text.xml"));
        Assert.assertTrue(result4.contains("file.java"));
    }

    @Test
    public void testJavaNameProcessing() {
        String text = "";
        List<String> result = PreProcessingUtils.instance.preProcessNaturalLanguage(text);
    }

}
