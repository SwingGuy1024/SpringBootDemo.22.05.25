package com.infosys.dummy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/19/18
 * <p>Time: 9:42 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@Entity
public class MenuItemOption implements Serializable {
  @Nullable
  private Integer id;
  @Nullable
  private MenuItem menuItem;
  @NotNull
  private BigDecimal deltaPrice;
  @NotEmpty
  private String name;

  @Id
  @GeneratedValue
  @Nullable
  public Integer getId() {
    return id;
  }

  public void setId(@Nullable final Integer id) {
    this.id = id;
  }

  @SuppressWarnings("JpaDataSourceORMInspection")
  @JsonIgnore
  @ManyToOne(cascade = CascadeType.REMOVE)
  @JoinColumn(name = "menu_item_id")
  @Nullable
  public MenuItem getMenuItem() {
    return menuItem;
  }

  public void setMenuItem(@Nullable final MenuItem menuItem) {
    this.menuItem = menuItem;
  }

  public BigDecimal getDeltaPrice() {
    return deltaPrice;
  }

  public void setDeltaPrice(final BigDecimal deltaPrice) {
    this.deltaPrice = deltaPrice;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) { return true; }
    if (!(o instanceof MenuItemOption)) { return false; } // implicitly checks for null

    final MenuItemOption that = (MenuItemOption) o;

    return (getId() != null) ? getId().equals(that.getId()) : (that.getId() == null);
  }

  @Override
  public int hashCode() {
    return (getId() != null) ? getId().hashCode() : 0;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public String toString() {
    //noinspection StringConcatenation,MagicCharacter
    return "MenuItemOption{" +
        "id=" + id +
        ", menuItemId="+(menuItem ==null? "<none>" : menuItem.getId())+ // We don't print the menuItem, to avoid an infinite loop.
        ", deltaPrice=" + deltaPrice +
        ", name='" + name + '\'' +
        '}';
  }
}
