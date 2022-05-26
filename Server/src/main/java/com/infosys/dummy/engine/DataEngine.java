package com.infosys.dummy.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.dummy.entity.MenuItem;
import com.infosys.dummy.entity.MenuItemOption;
import com.infosys.dummy.model.MenuItemDto;
import com.infosys.dummy.model.MenuItemOptionDto;
import com.infosys.dummy.repository.MenuItemOptionRepository;
import com.infosys.dummy.repository.MenuItemRepository;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.infosys.dummy.engine.PojoUtility.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/9/21
 * <p>Time: 12:19 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@Component
public class DataEngine {
  private static final @NonNls Logger log = LoggerFactory.getLogger(DataEngine.class);
  private final MenuItemRepository menuItemRepositoryWrapper;
  private final MenuItemOptionRepository menuItemOptionRepositoryWrapper;
  private final ObjectMapper objectMapper;

  @Autowired
  public DataEngine(
      final MenuItemRepository menuItemRepositoryWrapper,
      final MenuItemOptionRepository menuItemOptionRepositoryWrapper,
      final ObjectMapper objectMapper
  ) {
    this.menuItemRepositoryWrapper = menuItemRepositoryWrapper;
    this.menuItemOptionRepositoryWrapper = menuItemOptionRepositoryWrapper;
    this.objectMapper = objectMapper;
  }

  public MenuItemDto getMenuItemDto(final Integer id) {
    MenuItem menuItem = findOrThrow404(menuItemRepositoryWrapper, id);
    return objectMapper.convertValue(menuItem, MenuItemDto.class);
  }

  public List<MenuItemDto> getAllMenuItems() {
    return menuItemRepositoryWrapper
        .findAll()
        .stream()
        .map(m -> objectMapper.convertValue(m, MenuItemDto.class))
        .collect(Collectors.toList());
  }

  public Integer addOption(final Integer menuItemId, final MenuItemOptionDto optionDto) {
    confirmNotEmpty(optionDto.getName()); // throws ResponseException
    confirmNull(optionDto.getId(), "ID");
    MenuItemOption menuItemOption = objectMapper.convertValue(optionDto, MenuItemOption.class);
    final MenuItem menuItem = findOrThrow404(menuItemRepositoryWrapper, menuItemId);
    menuItemOption.setMenuItem(menuItem);
    MenuItemOption savedOption = menuItemOptionRepositoryWrapper.save(menuItemOption);
    final Integer newId = savedOption.getId();
    assert newId != null;
    return newId;
  }

  public Integer addMenuItemFromDto(final MenuItemDto menuItemDto) {
    for (MenuItemOptionDto option : skipNull(menuItemDto.getAllowedOptions())) {
      log.trace("addMenuItemFromDto testing for empty.");
      confirmNotEmpty(option.getName(), dto(option, "Name"));
      confirmNotNull(option.getDeltaPrice(), dto(option, "deltaPrice"));
      confirmNull(option.getId(), dto(option, "ID"));
    }

    MenuItem menuItem = convertMenuItem(menuItemDto);
    confirmNull(menuItem.getId(), dto(menuItemDto, "ID"));
    log.trace("MenuItem: {}", menuItem);
    MenuItem savedItem = menuItemRepositoryWrapper.save(menuItem);
    final Integer id = savedItem.getId();
    log.trace("added menuItem with id {}", id);
    return id;
  }

  private static String dto(Object dto, String label) {
    return String.format("%s.%s", dto.getClass().getSimpleName(), label);
  }

  public Integer createNewOption(final MenuItemOptionDto menuItemOptionDto) {
    confirmNull(menuItemOptionDto.getId(), "ID");
    MenuItemOption menuItemOption = convertMenuItemOption(menuItemOptionDto);
    log.trace("MenuItemOption: {}", menuItemOption);
    MenuItemOption savedItem = menuItemOptionRepositoryWrapper.save(menuItemOption);
    final Integer newId = savedItem.getId();
    assert newId != null;
    log.trace("MenuItemOption added with ID {}", newId);
    return newId;
  }

  private MenuItem convertMenuItem(final MenuItemDto menuItemDto) {
    final MenuItem menuItem = objectMapper.convertValue(menuItemDto, MenuItem.class);

    // objectMapper doesn't set the menuItems in the options, because it can't handle circular references, so we
    // set them here.
    for (MenuItemOption option : menuItem.getAllowedOptions()) {
      option.setMenuItem(menuItem);
    }

//    log.trace("Converting from DTO: Menu Item {}", menuItem);
    return menuItem;
  }

  public Void deleteById(final Integer optionId) {
    log.trace("Deleting menuItemOption with id {}", optionId);


    MenuItemOption itemToDelete = findOrThrow404(menuItemOptionRepositoryWrapper, optionId);

    // Before I can successfully delete the menuItemOption, I first have to set its menuItem to null. If I don't
    // do that, the delete call will fail. It doesn't help to set Cascade to Remove in the @ManyToOne annotation in 
    // MenuItemOption. Since it's set to ALL in MenuItem's @OneToMany annotation, the Cascade value doesn't seem to 
    // affect this.
    itemToDelete.setMenuItem(null);
    menuItemOptionRepositoryWrapper.save(itemToDelete);

    menuItemOptionRepositoryWrapper.delete(itemToDelete);
    return null;
  }

  private MenuItemOption convertMenuItemOption(final MenuItemOptionDto menuItemOptionDto) {
    return objectMapper.convertValue(menuItemOptionDto, MenuItemOption.class);
  }

  public Void addOptionToItem(final int menuItemOptionId, final int menuItemId) {
    MenuItem menuItem = findOrThrow404(menuItemRepositoryWrapper, menuItemId);
    MenuItemOption option = findOrThrow404(menuItemOptionRepositoryWrapper, menuItemOptionId);
    option.setMenuItem(menuItem);
    menuItem.getAllowedOptions().add(option);
    menuItemRepositoryWrapper.saveAndFlush(menuItem);
    log.trace("MenuItemOption id {} added to menu item id {}", menuItemOptionId, menuItemId);
    return null;
  }
}
