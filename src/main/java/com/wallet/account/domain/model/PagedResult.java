package com.wallet.account.domain.model;

import java.util.List;

/**
 * Wraps a page of results together with the total number of elements
 * available across all pages, so callers can build pagination metadata
 * without needing a second query.
 */
public record PagedResult<T>(List<T> content, long totalElements) {
}
