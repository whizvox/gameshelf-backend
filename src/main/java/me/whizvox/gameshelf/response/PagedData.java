package me.whizvox.gameshelf.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public class PagedData {

  public int page;
  public int totalPages;
  public int itemsInPage;
  public long totalItems;
  public List<?> items;

  public PagedData(int page, int totalPages, int itemsInPage, long totalItems, List<?> items) {
    this.page = page;
    this.totalPages = totalPages;
    this.itemsInPage = itemsInPage;
    this.totalItems = totalItems;
    this.items = items;
  }

  public <T> PagedData(Page<T> page, Function<T, ?> mapper) {
    this(page.getNumber(), page.getTotalPages(), page.getNumberOfElements(), page.getTotalElements(), page.stream().map(mapper).toList());
  }

  public PagedData(Page<?> page) {
    this(page, item -> item);
  }

}
