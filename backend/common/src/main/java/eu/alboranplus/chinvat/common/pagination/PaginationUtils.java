package eu.alboranplus.chinvat.common.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Helper utility for pagination operations.
 */
public class PaginationUtils {

  private PaginationUtils() {}

  /**
   * Convert Spring Data Page to PageResponse.
   */
  public static <T> PageResponse<T> toPageResponse(Page<T> page) {
    PaginationMetadata metadata =
        new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext());

    return PageResponse.of(page.getContent(), metadata);
  }

  /**
   * Create PaginationMetadata from list and request.
   * Use when you manually handle pagination (e.g., with custom queries).
   */
  public static PaginationMetadata createMetadata(
      List<?> items, PaginationRequest request, long totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / request.size());

    return new PaginationMetadata(
        request.page(),
        request.size(),
        totalElements,
        totalPages,
        request.page() == 0,
        request.page() == totalPages - 1,
        request.page() < totalPages - 1);
  }

  /**
   * Get safe page number (ensure it's within valid range).
   */
  public static int getSafePageNumber(int page, int totalPages) {
    if (page < 0) {
      return 0;
    }
    if (page >= totalPages && totalPages > 0) {
      return totalPages - 1;
    }
    return page;
  }
}
