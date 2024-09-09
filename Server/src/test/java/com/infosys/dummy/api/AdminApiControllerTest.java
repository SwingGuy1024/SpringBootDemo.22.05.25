package com.infosys.dummy.api;

import com.infosys.dummy.SpringBootTester;
import com.infosys.dummy.entity.MenuItem;
import com.infosys.dummy.entity.MenuItemOption;
import com.infosys.dummy.exception.BadRequest400Exception;
import com.infosys.dummy.exception.NotFound404Exception;
import com.infosys.dummy.exception.ResponseException;
import com.infosys.dummy.framework.engine.DataEngine;
import com.infosys.dummy.framework.engine.MenuItemOptionTestRepository;
import com.infosys.dummy.framework.engine.MenuItemTestRepository;
import com.infosys.dummy.framework.engine.PojoUtility;
import com.infosys.dummy.model.MenuItemDto;
import com.infosys.dummy.model.MenuItemOptionDto;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.infosys.dummy.framework.engine.PojoUtility.findOrThrow404;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/9/21
 * <p>Time: 1:33 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings({"CallToNumericToString", "HardCodedStringLiteral", "MagicNumber", "RedundantSuppression"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringBootTester.class)
@Component
public class AdminApiControllerTest {

  @Autowired
  private AdminApiController adminApiController;

  @Autowired
  private MenuItemTestRepository menuItemRepository;

  @Autowired
  private MenuItemOptionTestRepository menuItemOptionRepository;

  @Autowired
  private DataEngine dataEngine;

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

  @Test
  public void testAddMenuItemBadInputEmptyOptionName() {
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemOption.setName("");
    menuItemOption.setDeltaPrice(new BigDecimal("5.00"));

    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setAllowedOptions(Collections.singletonList(menuItemOption));
    menuItemDto.setName("BadItem");
    menuItemDto.setItemPrice(new BigDecimal("0.50"));
    assertThrows(BadRequest400Exception.class, () -> adminApiController.addMenuItem(menuItemDto));
  }

  @Test
  public void testAddMenuItemBadInputMissingPrice() {
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemOption.setName("BadOption");

    MenuItemDto menuItemDto = new MenuItemDto();
    menuItemDto.setAllowedOptions(Collections.singletonList(menuItemOption));
    menuItemDto.setName("BadItem");
    menuItemDto.setItemPrice(new BigDecimal("0.50"));
    assertThrows(ConstraintViolationException.class, () -> adminApiController.addMenuItem(menuItemDto));
  }

  @Test
  public void testAddMenuItemBadInputEmptyPrice() {
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemOption.setName("BadOption");
    assertThrows(NumberFormatException.class, () -> menuItemOption.setDeltaPrice(new BigDecimal("")));
  }

  @Test
  public void testAddMenuItemMissingPrice() {
    MenuItemDto menuItemDto = new MenuItemDto();
    MenuItemOptionDto menuItemOption = new MenuItemOptionDto();
    menuItemDto.setAllowedOptions(Collections.singletonList(menuItemOption));
    menuItemDto.setName("BadItem");
    System.out.printf("Bad Price?:%s%n", menuItemDto); // NON-NLS
    assertThrows(BadRequest400Exception.class, () -> dataEngine.addMenuItemFromDto(menuItemDto));
  }

  @Test
  public void testAddMenuItemOptionGoodInput() {
    MenuItemDto menuItemDto = makeMenuItem();
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    final int id = PojoUtility.extractInt(responseEntity);
    MenuItem item = findOrThrow404(menuItemRepository, id);
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
    ResponseEntity<String> response = adminApiController.addMenuItem(menuItemDto);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
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
    } catch (BadRequest400Exception ignored) {}
  }


  private void isNotFound(int id, MenuItemOptionDto optionDto) {
    try {
      final ResponseEntity<String> stringResponseEntity = adminApiController.addMenuItemOption(id, optionDto);
      fail(stringResponseEntity.toString());
    } catch (NotFound404Exception ignored) {}
  }

  // Test of deleteOption()

  @Test
  public void testDeleteBadOptionId() {
    assertThrows(NotFound404Exception.class, () -> adminApiController.deleteOption(100000));
  }

  @Test
  public void testDeleteOption() throws ResponseException {
    MenuItemDto menuItemDto = createPizzaMenuItem();
    ResponseEntity<String> responseEntity = adminApiController.addMenuItem(menuItemDto);
    final int id = PojoUtility.extractInt(responseEntity); //.parseInt(Objects.requireNonNull(responseEntity.getBody
    // ()));
    System.out.printf("Body: <%s>%n", id);

    MenuItem item = findOrThrow404(menuItemRepository, id);
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
    assertNotNull(findOrThrow404(menuItemOptionRepository, removedId));
    ResponseEntity<String> goodResponse = adminApiController.deleteOption(removedOption.getId());

    assertEquals(HttpStatus.OK, goodResponse.getStatusCode());

    List<MenuItemOption> allOptions = menuItemOptionRepository.findAll();
    for (MenuItemOption option : allOptions) {
      System.out.println(option);
    }

    item = findOrThrow404(menuItemRepository, id);
    assertFalse(itemHasOptionName(item, removedName));

    assertThrows(NotFound404Exception.class, () -> findOrThrow404(menuItemOptionRepository, removedId));
  }

  @AfterEach
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
