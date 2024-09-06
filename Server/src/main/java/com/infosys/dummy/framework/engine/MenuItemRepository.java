package com.infosys.dummy.framework.engine;

import com.infosys.dummy.entity.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/20/18
 * <p>Time: 12:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@Repository
interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

  @Override
  @NotNull List<MenuItem> findAll();

  @Override
  <M extends MenuItem> @NotNull M save(@NotNull M menuItem);

  @Override
  <S extends MenuItem> @NotNull S saveAndFlush(@NotNull S s);

  @Override
  @NotNull
  List<MenuItem> findAllById(@NotNull Iterable<Integer> iterable);

  @Override
  @NotNull
  Optional<MenuItem> findById(@NotNull Integer integer);

  @Override
  void deleteAll();
}
