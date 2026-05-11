package eu.alboranplus.chinvat.common.pagination;

import java.io.Serializable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pagination request parameters from API client.
 *
 * Supports:
 * - page: zero-indexed page number (default: 0)
 * - size: items per page (default: 20, max: 100)
 * - sort: comma-separated list of sort expressions (e.g., "name,asc" or "createdAt,desc")
 */
public record PaginationRequest(
    int page,
    int size,
    String sort) implements Serializable {

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final int MAX_SIZE = 100;

  public PaginationRequest {
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("size must be > 0");
    }
    if (size > MAX_SIZE) {
      throw new IllegalArgumentException("size cannot exceed " + MAX_SIZE);
    }
  }

  /**
   * Create a PaginationRequest with default values.
   */
  public static PaginationRequest of(int page, int size) {
    return new PaginationRequest(page, size, null);
  }

  /**
   * Create a PaginationRequest with all defaults.
   */
  public static PaginationRequest defaultRequest() {
    return new PaginationRequest(DEFAULT_PAGE, DEFAULT_SIZE, null);
  }

  /**
   * Convert to Spring Data Pageable for repository queries.
   */
  public Pageable toPageable() {
    Sort sorting = parseSortExpression();
    return PageRequest.of(page, size, sorting);
  }

  /**
   * Parse sort expression (e.g., "name,asc" or "createdAt,desc").
   * If no sort provided, defaults to descending by creation time.
   */
  private Sort parseSortExpression() {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    String[] parts = sort.split(",");
    if (parts.length != 2) {
      throw new IllegalArgumentException("sort must be in format 'field,direction' (e.g., 'name,asc')");
    }

    String field = parts[0].trim();
    String directionStr = parts[1].trim().toUpperCase();

    Sort.Direction direction;
    try {
      direction = Sort.Direction.valueOf(directionStr);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("direction must be 'asc' or 'desc'");
    }

    // Prevent SQL injection via sort field - only allow alphanumeric + underscore
    if (!field.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
      throw new IllegalArgumentException("Invalid sort field: " + field);
    }

    return Sort.by(direction, field);
  }
}
