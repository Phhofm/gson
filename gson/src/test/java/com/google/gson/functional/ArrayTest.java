/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.common.MoreAsserts;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithObjects;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Functional tests for Json serialization and deserialization of arrays.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
@RunWith(Theories.class)
public class ArrayTest extends TestCase {

  @DataPoint
  public static Gson theoryGson = new Gson();

  @DataPoint
  public static int[] inputTopLevelArrayOfInts = {1, 2, 3, 4, 5, 6, 7, 8, 9};

  @DataPoint
  public static String inputTopLevelArrayOfIntsString = "[1,2,3,4,5,6,7,8,9]";

  @DataPoint
  public static int[] inputEmptyArray = {};

  @Theory
  public void testTopLevelArrayOfIntsSerialization() {
    Assume.assumeNotNull(theoryGson, inputTopLevelArrayOfInts);
    String target = theoryGson.toJson(inputTopLevelArrayOfInts);
    Assert.assertEquals(inputTopLevelArrayOfIntsString, target);
  }

  @Theory
  public void testTopLevelArrayOfIntsDeserialization() {
    Assume.assumeNotNull(theoryGson, inputTopLevelArrayOfInts);
    int[] target = theoryGson.fromJson(inputTopLevelArrayOfIntsString, int[].class);
    Assert.assertArrayEquals(inputTopLevelArrayOfInts,target);
  }

  @Theory
  public void testInvalidArrayDeserialization() {
    String json = "[1, 2 3, 4, 5]";
    try {
      theoryGson.fromJson(json, int[].class);
      fail("Gson should not deserialize array elements with missing ,");
    } catch (JsonParseException expected) {
    }
  }

  @Theory
  public void testEmptyArraySerialization() {
    Assume.assumeNotNull((Object) inputEmptyArray);
    Assert.assertEquals("[]", theoryGson.toJson(inputEmptyArray));
  }

  @Theory
  public void testEmptyArrayDeserialization() {
    int[] actualObject = theoryGson.fromJson("[]", int[].class);
    assertTrue(actualObject.length == 0);

    Integer[] actualObject2 = theoryGson.fromJson("[]", Integer[].class);
    assertTrue(actualObject2.length == 0);

    actualObject = theoryGson.fromJson("[ ]", int[].class);
    assertTrue(actualObject.length == 0);
  }

  @Theory
  public void testNullsInArraySerialization() {
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = theoryGson.toJson(array);
    assertEquals(expected, json);
  }

  @Theory
  public void testNullsInArrayDeserialization() {
    String json = "[\"foo\",null,\"bar\"]";
    String[] expected = {"foo", null, "bar"};
    String[] target = theoryGson.fromJson(json, String[].class);
    Assert.assertArrayEquals(expected, target);
  }

  @Theory
  public void testSingleNullInArraySerialization() {
    BagOfPrimitives[] array = new BagOfPrimitives[1];
    //array[0] = null;
    String json = theoryGson.toJson(array);
    assertEquals("[null]", json);
  }

  @Theory
  public void testSingleNullInArrayDeserialization() {
    BagOfPrimitives[] array = theoryGson.fromJson("[null]", BagOfPrimitives[].class);
    assertNull(array[0]);
  }

  @Theory
  public void testNullsInArrayWithSerializeNullPropertySetSerialization() {
    theoryGson = new GsonBuilder().serializeNulls().create();
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = theoryGson.toJson(array);
    assertEquals(expected, json);
  }

  @Theory
  public void testArrayOfStringsSerialization() {
    String[] target = {"Hello", "World"};
    assertEquals("[\"Hello\",\"World\"]", theoryGson.toJson(target));
  }

  @Theory
  public void testArrayOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    String[] target = theoryGson.fromJson(json, String[].class);
    assertEquals("Hello", target[0]);
    assertEquals("World", target[1]);
  }

  @Theory
  public void testSingleStringArraySerialization() throws Exception {
    String[] s = { "hello" };
    String output = theoryGson.toJson(s);
    assertEquals("[\"hello\"]", output);
  }

  @Theory
  public void testSingleStringArrayDeserialization() throws Exception {
    String json = "[\"hello\"]";
    String[] arrayType = theoryGson.fromJson(json, String[].class);
    assertEquals(1, arrayType.length);
    assertEquals("hello", arrayType[0]);
  }

  @Theory
  @SuppressWarnings("unchecked")
  public void testArrayOfCollectionSerialization() throws Exception {
    StringBuilder sb = new StringBuilder("[");
    int arraySize = 3;

    Type typeToSerialize = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] arrayOfCollection = new ArrayList[arraySize];
    for (int i = 0; i < arraySize; ++i) {
      int startValue = (3 * i) + 1;
      sb.append('[').append(startValue).append(',').append(startValue + 1).append(']');
      ArrayList<Integer> tmpList = new ArrayList<Integer>();
      tmpList.add(startValue);
      tmpList.add(startValue + 1);
      arrayOfCollection[i] = tmpList;

      if (i < arraySize - 1) {
        sb.append(',');
      }
    }
    sb.append(']');

    String json = theoryGson.toJson(arrayOfCollection, typeToSerialize);
    assertEquals(sb.toString(), json);
  }

  @Theory
  public void testArrayOfCollectionDeserialization() throws Exception {
    String json = "[[1,2],[3,4]]";
    Type type = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] target = theoryGson.fromJson(json, type);

    assertEquals(2, target.length);
    MoreAsserts.assertEquals(new Integer[] { 1, 2 }, target[0].toArray(new Integer[0]));
    MoreAsserts.assertEquals(new Integer[] { 3, 4 }, target[1].toArray(new Integer[0]));
  }

  @Theory
  public void testArrayOfPrimitivesAsObjectsSerialization() throws Exception {
    Object[] objs = new Object[] {1, "abc", 0.3f, 5L};
    String json = theoryGson.toJson(objs);
    assertTrue(json.contains("abc"));
    assertTrue(json.contains("0.3"));
    assertTrue(json.contains("5"));
  }

  @Theory
  public void testArrayOfPrimitivesAsObjectsDeserialization() throws Exception {
    String json = "[1,'abc',0.3,1.1,5]";
    Object[] objs = theoryGson.fromJson(json, Object[].class);
    assertEquals(1, ((Number)objs[0]).intValue());
    assertEquals("abc", objs[1]);
    assertEquals(0.3, ((Number)objs[2]).doubleValue());
    assertEquals(new BigDecimal("1.1"), new BigDecimal(objs[3].toString()));
    assertEquals(5, ((Number)objs[4]).shortValue());
  }

  @Theory
  public void testObjectArrayWithNonPrimitivesSerialization() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String classWithObjectsJson = theoryGson.toJson(classWithObjects);
    String bagOfPrimitivesJson = theoryGson.toJson(bagOfPrimitives);

    Object[] objects = new Object[] { classWithObjects, bagOfPrimitives };
    String json = theoryGson.toJson(objects);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
  }

  @Theory
  public void testArrayOfNullSerialization() {
    Object[] array = new Object[] {null};
    String json = theoryGson.toJson(array);
    assertEquals("[null]", json);
  }

  @Theory
  public void testArrayOfNullDeserialization() {
    String[] values = theoryGson.fromJson("[null]", String[].class);
    assertNull(values[0]);
  }

  /**
   * Regression tests for Issue 272
   */
  @Theory
  public void testMultidimenstionalArraysSerialization() {
    String[][] items = new String[][]{
        {"3m Co", "71.72", "0.02", "0.03", "4/2 12:00am", "Manufacturing"},
        {"Alcoa Inc", "29.01", "0.42", "1.47", "4/1 12:00am", "Manufacturing"}
    };
    String json = theoryGson.toJson(items);
    assertTrue(json.contains("[[\"3m Co"));
    assertTrue(json.contains("Manufacturing\"]]"));
  }

  @Theory
  public void testMultiDimenstionalObjectArraysSerialization() {
    Object[][] array = new Object[][] { new Object[] { 1, 2 } };
    assertEquals("[[1,2]]", theoryGson.toJson(array));
  }

  /**
   * Regression test for Issue 205
   */
  @Theory
  public void testMixingTypesInObjectArraySerialization() {
    Object[] array = new Object[] { 1, 2, new Object[] { "one", "two", 3 } };
    assertEquals("[1,2,[\"one\",\"two\",3]]", theoryGson.toJson(array));
  }

  /**
   * Regression tests for Issue 272
   */
  @Theory
  public void testMultidimenstionalArraysDeserialization() {
    String json = "[['3m Co','71.72','0.02','0.03','4/2 12:00am','Manufacturing'],"
      + "['Alcoa Inc','29.01','0.42','1.47','4/1 12:00am','Manufacturing']]";
    String[][] items = theoryGson.fromJson(json, String[][].class);
    assertEquals("3m Co", items[0][0]);
    assertEquals("Manufacturing", items[1][5]);
  }

  /** http://code.google.com/p/google-gson/issues/detail?id=342 */
  @Theory
  public void testArrayElementsAreArrays() {
    Object[] stringArrays = {
        new String[] {"test1", "test2"},
        new String[] {"test3", "test4"}
    };
    assertEquals("[[\"test1\",\"test2\"],[\"test3\",\"test4\"]]",
        new Gson().toJson(stringArrays));
  }

  @Theory
  public void testJaggedArraySerialization(){
    //declaring 2D array with odd columns
    int[][] jaggedArray = new int[3][];
    jaggedArray[0] = new int[3];
    jaggedArray[1] = new int[4];
    jaggedArray[2] = new int[2];
    //initializing jagged array
    int count = 0;
    for (int i=0; i<jaggedArray.length; i++) {
      for (int j = 0; j < jaggedArray[i].length; j++) {
        jaggedArray[i][j] = count++;
      }
    }
    String target = theoryGson.toJson(jaggedArray);
    String expected = "[[0,1,2],[3,4,5,6],[7,8]]";
    Assert.assertEquals(expected, target);
  }

  @Theory
  public void testJaggedArrayDeserialization(){
    String json = "[[0,1,2],[3,4,5,6],[7,8]]";
    int[][] target = theoryGson.fromJson(json, int[][].class);
    //declaring 2D array with odd columns
    int[][] expected = new int[3][];
    expected[0] = new int[3];
    expected[1] = new int[4];
    expected[2] = new int[2];
    //initializing jagged array
    int count = 0;
    for (int i=0; i<expected.length; i++) {
      for (int j = 0; j < expected[i].length; j++) {
        expected[i][j] = count++;
      }
    }
    Assert.assertEquals(expected, target);
  }

  @Theory
  public void testIntHexValueSerialization(){
    int hexVal = 0x1a;
    String expected = "26";
    String target = theoryGson.toJson(hexVal);
    Assert.assertEquals(expected, target);
  }

  @Theory
  public void testIntBinValueSerialization(){
    int binVal = 0b11010;
    String expected = "26";
    String target = theoryGson.toJson(binVal);
    Assert.assertEquals(expected, target);
  }

  @Theory
  public void testUnderscoreCharsInNumericLiteralsSerialization(){
    int underscore = 5___________2____________3;
    System.out.println(underscore);
    String target = theoryGson.toJson(underscore);
    String expected = "523";
    Assert.assertEquals(expected, target);
  }


}
