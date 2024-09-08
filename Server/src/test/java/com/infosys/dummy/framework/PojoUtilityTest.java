package com.infosys.dummy.framework;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infosys.dummy.framework.engine.PojoUtility;
import com.infosys.dummy.entity.MenuItem;
import com.infosys.dummy.exception.BadRequest400Exception;
import com.infosys.dummy.model.MenuItemDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/9/21
 * <p>Time: 12:34 PM
 *
 * @author Miguel Muñoz
 */
public class PojoUtilityTest {

  @Test
  public void testSkipNull() {
    Iterable<?> iterable = PojoUtility.skipNull(null);
    Assertions.assertNotNull(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Assertions.fail("Iterable not empty");
    }

    String test = "TEST";
    List<String> list = Collections.singletonList(test);
    iterable = PojoUtility.skipNull(list);
    iterator = iterable.iterator();
    int count = 0;
    while (iterator.hasNext()) {
      Assertions.assertSame(test, iterator.next());
      count++;
    }
    assertEquals(1, count);
  }

  @Test
  public void decodeBadInteger() {
    assertThrows(BadRequest400Exception.class, () -> PojoUtility.confirmAndDecodeInteger("bad"));
  }

  @Test
  public void decodeBadLong() {
    assertThrows(BadRequest400Exception.class, () -> PojoUtility.confirmAndDecodeLong("bad"));
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
  
  @Test
  public void testConfirmNeverNull2() {
    //no inspection unused
    assertThrows(BadRequest400Exception.class, () -> PojoUtility.confirmNotNull(null));
  }

  @Test
  public void testEntityAssertion() {
    try {
      //noinspection unused
      PojoUtility.confirmNotNull(new MenuItem());
      Assertions.fail(); // Should fail when used on an Entity.
    } catch (AssertionError e) {
      System.out.printf("testEntityAssertion: %s%n", e.getMessage());
      assertThat(e.getMessage(), Matchers.containsString("MenuItem"));
    }
  }

  @Test
  public void testEntityAssertionFail() {
    assertThrows(BadRequest400Exception.class, () ->PojoUtility.confirmNotNull(null));
  }

  @Test
  public void testConfirmNull() {
    PojoUtility.confirmNull(null);
    assertThrows(BadRequest400Exception.class, () -> PojoUtility.confirmNull("Not null"));
  }

  @Test
  public void testConfirmEqual() {
    assertThrows(BadRequest400Exception.class, () -> PojoUtility.confirmEqual("this", "that"));
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
    //noinspection ObviousNullCheck
    String s1 = PojoUtility.emptyIfNull("not");
    assertEquals(s1, "not");
    
    String s2 = PojoUtility.emptyIfNull(null);
    assertEquals("", s2);
  }
  
  @Test
  public void testConfirmNotEmpty1() {
    assertThrows(BadRequest400Exception.class, () ->PojoUtility.confirmNotEmpty(null));
  }

  @Test
  public void testConfirmNotEmpty2() {
    assertThrows(BadRequest400Exception.class, () -> PojoUtility.confirmNotEmpty(""));
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
