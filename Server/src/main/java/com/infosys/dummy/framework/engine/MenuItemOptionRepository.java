package com.infosys.dummy.framework.engine;

import com.infosys.dummy.entity.MenuItemOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/20/18
 * <p>Time: 1:57 AM
 *
 * @author Miguel Muñoz
 */
@Repository
public interface MenuItemOptionRepository extends JpaRepository<MenuItemOption, Integer> {
//    String MENU_ITEM_CACHE = MenuItemRepository.MENU_ITEM_CACHE;

  @Override
  <MIO extends MenuItemOption> @NotNull MIO save(@NotNull MIO option);

  @Override
  void delete(@NotNull MenuItemOption optionToDelete);
}
