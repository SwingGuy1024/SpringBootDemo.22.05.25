package com.infosys.dummy.repository;

import com.infosys.dummy.entity.MenuItemOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/20/18
 * <p>Time: 1:57 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@Repository
public interface MenuItemOptionRepository extends JpaRepository<MenuItemOption, Integer> {
    String MENU_ITEM_CACHE = MenuItemRepository.MENU_ITEM_CACHE;

    @Override
    @CacheEvict(cacheNames = MENU_ITEM_CACHE, allEntries = true)
    <MIO extends MenuItemOption> @NotNull MIO save(@NotNull MIO option);

    @Override
    @CacheEvict(cacheNames = MENU_ITEM_CACHE, allEntries = true)
    void delete(@NotNull MenuItemOption optionToDelete);
}