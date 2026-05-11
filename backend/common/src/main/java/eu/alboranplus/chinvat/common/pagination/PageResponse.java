package eu.alboranplus.chinvat.common.pagination;

import java.io.Serializable;
import java.util.List;

/**
 * Generic paginated response wrapper for API responses.
 *
 * @param <T> Type of paginated items
 */
public record PageResponse<T>(
    List<T> data,
    PaginationMetadata pagination) implements Serializable {

  public PageResponse {
    if (data == null) {
      throw new IllegalArgumentException("data cannot be null");
    }
    if (pagination == null) {
      throw new IllegalArgumentException("pagination cannot be null");
    }
  }

  /**
   * Create a PageResponse from pagination metadata.
   */
  public static <T> PageResponse<T> of(List<T> data, PaginationMetadata pagination) {
    return new PageResponse<>(data, pagination);
  }

  /**
   * Create a single-page response (when pagination is not applicable).
   */
  public static <T> PageResponse<T> single(List<T> data) {
    PaginationMetadata pagination =
        new PaginationMetadata(
            0, // page
            data.size(), // size
            (long) data.size(), // totalElements
            1, // totalPages
            true, // isFirst
            true, // isLast
            false); // hasNext

    return new PageResponse<>(data, pagination);
  }
}
