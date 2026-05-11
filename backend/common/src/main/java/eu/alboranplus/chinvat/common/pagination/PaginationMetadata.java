package eu.alboranplus.chinvat.common.pagination;

import java.io.Serializable;

/**
 * Pagination metadata included in all paginated API responses.
 */
public record PaginationMetadata(
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean isFirst,
    boolean isLast,
    boolean hasNext) implements Serializable {

  public PaginationMetadata {
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("size must be > 0");
    }
    if (totalElements < 0) {
      throw new IllegalArgumentException("totalElements must be >= 0");
    }
    if (totalPages < 0) {
      throw new IllegalArgumentException("totalPages must be >= 0");
    }
  }

  /**
   * Get the offset for database queries (page * size).
   */
  public int getOffset() {
    return page * size;
  }

  /**
   * Check if there's a previous page.
   */
  public boolean hasPrevious() {
    return page > 0;
  }
}
