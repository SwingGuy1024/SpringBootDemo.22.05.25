package com.infosys.dummy.api;

import com.infosys.dummy.ServerMaster;
import com.infosys.dummy.entity.MenuItem;
import com.infosys.dummy.entity.MenuItemOption;
import com.infosys.dummy.exception.BadRequest400Exception;
import com.infosys.dummy.exception.NotFound404Exception;
import com.infosys.dummy.exception.ResponseException;
import com.infosys.dummy.model.MenuItemDto;
import com.infosys.dummy.model.MenuItemOptionDto;
import com.infosys.dummy.repository.MenuItemOptionRepository;
import com.infosys.dummy.repository.MenuItemRepository;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.infosys.dummy.engine.PojoUtility.findOrThrow404;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/9/21
 * <p>Time: 1:33 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"CallToNumericToString", "HardCodedStringLiteral", "MagicNumber", "RedundantSuppression"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerMaster.class)
@Component
public class AdminApiControllerTest {

  @Autowired
  private AdminApiController adminApiController;

  @Autowired
  private MenuItemRepository menuItemRepository;

  @Autowired
  private MenuItemOptionRepository menuItemOptionRepository;

  @Test(expected = BadRequest400Exception.class)
  public void testAddMenuItemBadInputEmptyOptionName() {
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemOption.setName("");
    menuItemOption.setDeltaPrice(new BigDecimal("5.00"));

    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setAllowedOptions(Collections.singletonList(menuItemOption));
    menuItemDto.setName("BadItem");
    menuItemDto.setItemPrice(new BigDecimal("0.50"));
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    fail(responseEntity.toString());
  }

  @Test(expected = ConstraintViolationException.class)
  public void testAddMenuItemBadInputMissingPrice() {
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemOption.setName("BadOption");
//    menuItemOption.setDeltaPrice(new BigDecimal("5.00"));

    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setAllowedOptions(Collections.singletonList(menuItemOption));
    menuItemDto.setName("BadItem");
    menuItemDto.setItemPrice(new BigDecimal("0.50"));
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    fail(responseEntity.toString());
  }

  @Test(expected = NumberFormatException.class)
  public void testAddMenuItemBadInputEmptyPrice() {
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemOption.setName("BadOption");
    menuItemOption.setDeltaPrice(new BigDecimal(""));

    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setAllowedOptions(Collections.singletonList(menuItemOption));
    menuItemDto.setName("BadItem");
    menuItemDto.setItemPrice(new BigDecimal("0.50"));
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    fail(responseEntity.toString());
  }

  @Test
  public void testAddMenuItemGoodInput() {
    MenuItemDto menuItemDto = makeMenuItem();
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    final int id = Integer.parseInt(Objects.requireNonNull(responseEntity.getBody()));
    MenuItem item = findOrFail(menuItemRepository, id);
    Hibernate.initialize(item);
    assertEquals("0.50", item.getItemPrice().toString());
    assertEquals("GoodItem", item.getName());
    Set<String> foodOptionSet = new HashSet<>();
    Collection<MenuItemOption> optionList = item.getAllowedOptions();
    for (MenuItemOption option : optionList) {
      foodOptionSet.add(option.getName());
    }
    assertThat(foodOptionSet, hasItems("olives", "pepperoni"));
    assertEquals(2, foodOptionSet.size());

  }

  private static MenuItemDto makeMenuItem() {
    MenuItemOptionDto oliveOption = makeMenuItemOptionDto("olives", "0.30");
    MenuItemOptionDto pepOption = makeMenuItemOptionDto("pepperoni", "0.40");

    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setAllowedOptions(Arrays.asList(oliveOption, pepOption));
    menuItemDto.setName("GoodItem");
    menuItemDto.setItemPrice(new BigDecimal("0.50"));
    return menuItemDto;
  }

  private static MenuItemOptionDto makeMenuItemOptionDto(String name, String price) {
    MenuItemOptionDto option = new MenuItemOptionDto();
    option.setName(name);
    option.setDeltaPrice(new BigDecimal(price));
    return option;
  }

  // Tests of addMenuItemOption()

  @Test
  public void testAddOptionBadInput() {
    isNotFound(5, makeMenuItemOptionDto("olives", "1000.00"));
    isNotFound(6, makeMenuItemOptionDto("pepperoni", "100.00"));
    isBadRequest(5, makeMenuItemOptionDto("", "0.40"));
    isBadRequest(6, makeMenuItemOptionDto("", "0.50"));
  }

  private void isBadRequest(int id, MenuItemOptionDto optionDto) {
    try {
      final ResponseEntity<String> stringResponseEntity = adminApiController.addMenuItemOption(id, optionDto);
      fail(stringResponseEntity.toString());
    } catch (BadRequest400Exception ignored) { }
  }

  private void isNotFound(int id, MenuItemOptionDto optionDto) {
    try {
      final ResponseEntity<String> stringResponseEntity = adminApiController.addMenuItemOption(id, optionDto);
      fail(stringResponseEntity.toString());
    } catch (NotFound404Exception ignored) { }
  }

  // Test of deleteOption()

  @Test(expected = NotFound404Exception.class)
  public void testDeleteBadOptionId() {
    ResponseEntity<Void> badResponseTwo = adminApiController.deleteOption(100000);
    fail(badResponseTwo.toString());
  }

  @Test(expected = NotFound404Exception.class)
  public void testDeleteOption() throws ResponseException {
    MenuItemDto menuItemDto = createPizzaMenuItem();
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    final int id = Integer.parseInt(Objects.requireNonNull(responseEntity.getBody()));
    System.out.printf("Body: <%s>%n", id);

    MenuItem item = findOrFail(menuItemRepository, id);
    Hibernate.initialize(item);
    Set<String> nameSet = item.getAllowedOptions()
            .stream()
            .map(MenuItemOption::getName)
            .collect(Collectors.toSet());
    assertThat(nameSet, hasItems("pepperoni", "sausage", "mushrooms", "bell peppers", "olives", "onions"));

    MenuItemOption removedOption = item.getAllowedOptions().iterator().next();
    String removedName = removedOption.getName();
    Integer removedId = removedOption.getId();
    assertNotNull(removedId);
    assertTrue(itemHasOptionName(item, removedName));
    assertNotNull(findOrFail(menuItemOptionRepository, removedId));
    ResponseEntity<Void> goodResponse = adminApiController.deleteOption(removedOption.getId());

    assertEquals(HttpStatus.OK, goodResponse.getStatusCode());

    List<MenuItemOption> allOptions = menuItemOptionRepository.findAll();
    for (MenuItemOption option : allOptions) {
      System.out.println(option);
    }

    item = findOrFail(menuItemRepository, id);
    assertFalse(itemHasOptionName(item, removedName));
    findOrThrow404(menuItemOptionRepository, removedId);
  }

  /*
   * This is for tests that need to repeatedly call findOrThrow404() when the test requires that it succeed,
   * but the test is annotated with @Test(expected = NotFound404Exception.class) because at the end, it needs
   * the findOrThrow404() to fail. This way, if one of the earlier call fails when it's supposed to succeed,
   * the test still fails.
   */
  private static <T> T findOrFail(JpaRepository<T, Integer> repository, int id) {
    T entity = null;
    try {
      return findOrThrow404(repository, id);
    } catch (ResponseException ignored) {
      fail("ID=" + id);
    }
    return entity;
  }

  private static MenuItemDto createPizzaMenuItem() {
    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setName("Pizza");
    menuItemDto.setAllowedOptions(new LinkedList<>());
    menuItemDto.getAllowedOptions().add(makeMenuItemOptionDto("pepperoni", "0.30"));
    menuItemDto.getAllowedOptions().add(makeMenuItemOptionDto("sausage", "0.30"));
    menuItemDto.getAllowedOptions().add(makeMenuItemOptionDto("mushrooms", "0.15"));
    menuItemDto.getAllowedOptions().add(makeMenuItemOptionDto("bell peppers", "0.15"));
    menuItemDto.getAllowedOptions().add(makeMenuItemOptionDto("olives", "0.00"));
    menuItemDto.getAllowedOptions().add(makeMenuItemOptionDto("onions", "0.00"));
    menuItemDto.setItemPrice(new BigDecimal("5.95"));
    return menuItemDto;
  }

  private static boolean itemHasOptionName(MenuItem item, String optionName) {
    return item.getAllowedOptions()
            .stream()
            .anyMatch(option -> Objects.equals(option.getName(), optionName));
  }

  @After
  public void tearDown() {
    List<MenuItem> menuItems = menuItemRepository.findAll();
    for (MenuItem menuItem : menuItems) {
      Collection<MenuItemOption> ops = menuItem.getAllowedOptions();
      menuItem.setAllowedOptions(new LinkedList<>());
      menuItemRepository.save(menuItem);
      menuItemOptionRepository.deleteInBatch(ops);
    }
    menuItemRepository.deleteInBatch(menuItems);

    List<MenuItemOption> optionList = menuItemOptionRepository.findAll();
    menuItemOptionRepository.deleteInBatch(optionList);
  }
}