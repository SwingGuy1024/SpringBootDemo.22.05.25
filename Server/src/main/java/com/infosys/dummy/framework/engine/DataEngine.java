package com.infosys.dummy.framework.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.dummy.entity.MenuItem;
import com.infosys.dummy.entity.MenuItemOption;
import com.infosys.dummy.exception.NotFound404Exception;
import com.infosys.dummy.model.MenuItemDto;
import com.infosys.dummy.model.MenuItemOptionDto;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>The Cache is designed for a production system, which assumes the overall menu doesn't change from
 * day to day, so it caches the entire Menu in its getAllMenuItems() method, so customers will get the
 * whole menu delivered very quickly.</p>
 * <p>In a real system, the Admin duties would be handled by a separate microservice with its own
 * caching priorities.</p>
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/9/21
 * <p>Time: 12:19 AM
 *
 * @author Miguel Muñoz
 */
@Component
public class DataEngine {
  private static final @NonNls Logger log = LoggerFactory.getLogger(DataEngine.class);
  private static final String MENU_ITEM_CACHE = "menuItems";
  private final MenuItemRepository menuItemRepository;
  private final MenuItemOptionRepository menuItemOptionRepository;
  private final ObjectMapper objectMapper;

  @Autowired
  public DataEngine(
      final MenuItemRepository menuItemRepository,
      final MenuItemOptionRepository menuItemOptionRepository,
      final ObjectMapper objectMapper
  ) {
    this.menuItemRepository = menuItemRepository;
    this.menuItemOptionRepository = menuItemOptionRepository;
    this.objectMapper = objectMapper;
  }

  private static String dto(Object dto, String label) {
    return String.format("%s.%s", dto.getClass().getSimpleName(), label);
  }

  public MenuItemDto getMenuItemDto(final Integer id) {
    MenuItem menuItem = PojoUtility.findOrThrow404(menuItemRepository, id);
    return objectMapper.convertValue(menuItem, MenuItemDto.class);
  }

  //  @Cacheable(cacheNames = MENU_ITEM_CACHE)
  public List<MenuItemDto> getAllMenuItems() {
    return menuItemRepository
        .findAll()
        .stream()
        .map(m -> objectMapper.convertValue(m, MenuItemDto.class))
        .collect(Collectors.toList());
  }

  @CacheEvict(cacheNames = MENU_ITEM_CACHE)
  public Integer addOption(final Integer menuItemId, final MenuItemOptionDto optionDto) {
    PojoUtility.confirmNotEmpty(optionDto.getName()); // throws ResponseException
    PojoUtility.confirmNull(optionDto.getId(), "ID");
    MenuItemOption menuItemOption = objectMapper.convertValue(optionDto, MenuItemOption.class);
    final MenuItem menuItem = PojoUtility.findOrThrow404(menuItemRepository, menuItemId);
    menuItemOption.setMenuItem(menuItem);
    MenuItemOption savedOption = menuItemOptionRepository.save(menuItemOption);
    final Integer newId = savedOption.getId();
    assert newId != null;
    return newId;
  }

  @CacheEvict(cacheNames = MENU_ITEM_CACHE)
  public Integer addMenuItemFromDto(final MenuItemDto menuItemDto) {
    for (MenuItemOptionDto option : PojoUtility.skipNull(menuItemDto.getAllowedOptions())) {
      log.trace("addMenuItemFromDto testing for empty.");
      PojoUtility.confirmNotEmpty(option.getName(), dto(option, "Name"));
      PojoUtility.confirmNotNull(option.getDeltaPrice(), dto(option, "deltaPrice"));
      PojoUtility.confirmNull(option.getId(), dto(option, "ID"));
    }

    MenuItem menuItem = convertMenuItem(menuItemDto);
    PojoUtility.confirmNull(menuItem.getId(), dto(menuItemDto, "ID"));
    log.trace("MenuItem: {}", menuItem);
    MenuItem savedItem = menuItemRepository.save(menuItem);
    final Integer id = savedItem.getId();
    log.trace("added menuItem with id {}", id);
    return id;
  }

  @CacheEvict(cacheNames = MENU_ITEM_CACHE)
  public Integer createNewOption(final MenuItemOptionDto menuItemOptionDto) {
    PojoUtility.confirmNull(menuItemOptionDto.getId(), "ID");
    MenuItemOption menuItemOption = convertMenuItemOption(menuItemOptionDto);
    log.trace("MenuItemOption: {}", menuItemOption);
    MenuItemOption savedItem = menuItemOptionRepository.save(menuItemOption);
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

  @CacheEvict(cacheNames = MENU_ITEM_CACHE)
  public void deleteOptionById(final Integer optionId) {
    log.trace("Deleting menuItemOption with id {}", optionId);


    MenuItemOption itemToDelete = PojoUtility.findOrThrow404(menuItemOptionRepository, optionId);

    // Before I can successfully delete the menuItemOption, I first have to set its menuItem to null. If I don't
    // do that, the delete call will fail. It doesn't help to set Cascade to Remove in the @ManyToOne annotation in 
    // MenuItemOption. Since it's set to ALL in MenuItem's @OneToMany annotation, the Cascade value doesn't seem to 
    // affect this.
    itemToDelete.setMenuItem(null);
    menuItemOptionRepository.save(itemToDelete);

    menuItemOptionRepository.delete(itemToDelete);
  }

  @CacheEvict(cacheNames = MENU_ITEM_CACHE)
  public void deleteMenuItemById(final Integer menuItemId) {
    log.trace("Deleting menuItem with id {}", menuItemId);

    if (!menuItemRepository.existsById(menuItemId)) {
      throw new NotFound404Exception(menuItemId.toString());
    }
    menuItemRepository.deleteById(menuItemId);
  }

  private MenuItemOption convertMenuItemOption(final MenuItemOptionDto menuItemOptionDto) {
    return objectMapper.convertValue(menuItemOptionDto, MenuItemOption.class);
  }

  @CacheEvict(cacheNames = MENU_ITEM_CACHE)
  public void addOptionToItem(final int menuItemOptionId, final int menuItemId) {
    MenuItem menuItem = PojoUtility.findOrThrow404(menuItemRepository, menuItemId);
    MenuItemOption option = PojoUtility.findOrThrow404(menuItemOptionRepository, menuItemOptionId);
    option.setMenuItem(menuItem);
    menuItem.getAllowedOptions().add(option);
    menuItemRepository.saveAndFlush(menuItem);
    log.trace("MenuItemOption id {} added to menu item id {}", menuItemOptionId, menuItemId);
  }
}
