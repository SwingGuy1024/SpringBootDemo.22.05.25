package com.infosys.dummy.repository;

import com.infosys.dummy.entity.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/20/18
 * <p>Time: 12:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
  String MENU_ITEM_CACHE = "menuItems";

  @Override
  @Cacheable(cacheNames = MENU_ITEM_CACHE)
  @NotNull List<MenuItem> findAll();

  @Override
  @CacheEvict(cacheNames = MENU_ITEM_CACHE, allEntries = true)
  <M extends MenuItem> @NotNull M save(@NotNull M menuItem);

  @Override
  @CacheEvict(cacheNames = MENU_ITEM_CACHE, allEntries = true)
  <S extends MenuItem> @NotNull S saveAndFlush(@NotNull S s);
}
