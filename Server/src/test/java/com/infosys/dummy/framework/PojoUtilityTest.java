package com.infosys.dummy.framework;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infosys.dummy.engine.PojoUtility;
import com.infosys.dummy.entity.MenuItem;
import com.infosys.dummy.exception.BadRequest400Exception;
import com.infosys.dummy.model.MenuItemDto;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/9/21
 * <p>Time: 12:34 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class PojoUtilityTest {
  @Test(expected = AssertionError.class)
  public void neverNullAssertionTest() {
    // NOTE: This test assumes assertions are turned on during testing. If assertions are off, this test will fail.
    PojoUtility.confirmNotNull(new MenuItem());
    testIfAssertionsAreOn(); // makes test pass if assertions are off.
  }

  @SuppressWarnings("ErrorNotRethrown")
  private static void testIfAssertionsAreOn() {
    try {
      assert false;
    } catch (AssertionError ignore) {
      return;
    }
    System.out.println("Warning: Assertions are off. This test needs assertions to be on.");
    throw new AssertionError("Assertions are off");
  }
  
  @Test
  public void testSkipNull() {
    Iterable<?> iterable = PojoUtility.skipNull(null);
    assertNotNull(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      fail("Iterable not empty");
    }

    String test = "TEST";
    List<String> list = Collections.singletonList(test);
    iterable = PojoUtility.skipNull(list);
    iterator = iterable.iterator();
    int count = 0;
    while (iterator.hasNext()) {
      assertSame(test, iterator.next());
      count++;
    }
    assertEquals(1, count);
  }

  @Test(expected = BadRequest400Exception.class)
  public void decodeBadInteger() {
    PojoUtility.confirmAndDecodeInteger("bad");
  }

  @Test(expected = BadRequest400Exception.class)
  public void decodeBadLong() {
    PojoUtility.confirmAndDecodeLong("bad");
  }
  
  @Test
  public void testDecodeGood() {
    assertEquals(Integer.valueOf(52), PojoUtility.confirmAndDecodeInteger("52"));
    assertEquals(Integer.valueOf(142857), PojoUtility.confirmAndDecodeInteger("142857"));
    assertEquals(Integer.valueOf(-34), PojoUtility.confirmAndDecodeInteger("-34"));
    assertEquals(Long.valueOf(52), PojoUtility.confirmAndDecodeLong("52"));
    assertEquals(Long.valueOf(-52), PojoUtility.confirmAndDecodeLong("-52"));
    assertEquals(Long.valueOf(142857142857142857L), PojoUtility.confirmAndDecodeLong("142857142857142857"));
    assertEquals(Long.valueOf(-142857142857142857L), PojoUtility.confirmAndDecodeLong("-142857142857142857"));
  }
  
  @Test
  public void testConfirmNeverNull() {
    String text = "0text".substring(1);
    String confirmed = PojoUtility.confirmNotNull(text);
    assertEquals("1text".substring(1), confirmed);
  }
  
  @Test(expected = BadRequest400Exception.class)
  public void testConfirmNeverNull2() {
    //noinspection unused
    PojoUtility.confirmNotNull(null);
    fail();
  }

  @Test
  public void testEntityAssertion() {
    try {
      //noinspection unused
      PojoUtility.confirmNotNull(new MenuItem());
      fail(); // Should fail when used on an Entity.
    } catch (AssertionError e) {
      System.out.printf("testEntityAssertion: %s%n", e.getMessage());
      assertThat(e.getMessage(), Matchers.containsString("MenuItem"));
    }
  }

  @Test(expected = BadRequest400Exception.class)
  public void testEntityAssertionFail() {
    PojoUtility.confirmNotNull(null);
  }

  @Test
  public void testConfirmNull() {
    PojoUtility.confirmNull(null);
    try {
      PojoUtility.confirmNull("Not null");
      fail("confirmNull failed");
    } catch (BadRequest400Exception ignored) { }
  }

  @Test(expected = BadRequest400Exception.class)
  public void testConfirmEqual() {
    PojoUtility.confirmEqual("this", "that");
  }

  @Test
  public void testConfirmEqual2() {
    PojoUtility.confirmEqual("1this".substring(1), "2this".substring(1));
  }

  @Test
  public void testConfirmEqualMsg() {
    PojoUtility.confirmEqual("unused msg", "1this".substring(1), "2this".substring(1));
    final String msg = "nog wsa wfl rwb xfp";
    try {
      PojoUtility.confirmEqual(msg, "this", "that");
    } catch (BadRequest400Exception e) {
      assertThat(e.getMessage(), Matchers.containsString(msg));
    }
  }
  
  @Test
  public void testEmptyIfNull() {
    String s1 = PojoUtility.emptyIfNull("not");
    assertEquals(s1, "not");
    
    String s2 = PojoUtility.emptyIfNull(null);
    assertEquals("", s2);
  }
  
  @Test(expected = BadRequest400Exception.class)
  public void testConfirmNotEmpty1() {
    PojoUtility.confirmNotEmpty(null);
  }

  @Test(expected = BadRequest400Exception.class)
  public void testConfirmNotEmpty2() {
    PojoUtility.confirmNotEmpty("");
  }

  @Test
  public void testConfirmNotEmpty() {
    PojoUtility.confirmNotEmpty("not null");
  }

  @Test
  public void testConvertList() {
    MenuItemDto menuItemDto1 = makeMenuItem(1, "one", "1.50");
    MenuItemDto menuItemDto2 = makeMenuItem(2, "two", "2.50");
    MenuItemDto menuItemDto3 = makeMenuItem(3, "three", "3.50");

    List<MenuItemDto> dtoList = Arrays.asList(menuItemDto1, menuItemDto2, menuItemDto3);
    List<MenuItem> menuItemList = PojoUtility.convertList(dtoList, new TypeReference<List<MenuItem>>() { });
    for (int i=0; i<dtoList.size(); ++i) {
      MenuItem menuItem = menuItemList.get(i);
      MenuItemDto dto = dtoList.get(i);
      assertEquals(dto.getId(), menuItem.getId());
      assertEquals(dto.getName(), menuItem.getName());
      assertEquals(dto.getItemPrice(), menuItem.getItemPrice());
    }
    assertEquals(dtoList.size(), menuItemList.size());
  }
  
  @Test
  public void testAsSet() {
    Set<String> set = PojoUtility.asSet("Red", "White", "Blue");
    assertThat(set, Matchers.containsInAnyOrder("Red", "White", "Blue"));
    assertThat(set, Matchers.hasSize(3));
  }

  @Test
  public void testAsSet2() {
    Set<String> set = PojoUtility.asSet(TreeSet::new, "Red", "White", "Blue");
    assertThat(set, Matchers.containsInRelativeOrder("Blue", "Red", "White"));
    assertThat(set, Matchers.hasSize(3));
  }

  private static MenuItemDto makeMenuItem(int id, String name, String price) {
    return new MenuItemDto()
            .id(id)
            .name(name)
            .itemPrice(new BigDecimal(price));
  }
}