package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.CartItem;
import com.example.onlinebookshop.Service.CartService;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.CartItemDTO;
import com.example.onlinebookshop.dto.UpdateCartItemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CartController(CartService cartService, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.cartService = cartService;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    // ✅ GET MY CART
    @GetMapping("/me")
    public List<CartItemDTO> getMyCart(Authentication auth) {
        String email = auth.getName();
        List<CartItem> items = cartService.getCartByEmail(email);

        Set<Long> bookIds = items.stream()
                .map(ci -> ci.getVariant() != null && ci.getVariant().getBook() != null ? ci.getVariant().getBook().getId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, String> coverByBookId = bookIds.isEmpty()
                ? Map.of()
                : loadCoverUrlsByBookIds(bookIds);

        return items.stream().map(ci -> {
            var v = ci.getVariant();
            var b = v != null ? v.getBook() : null;
            Long bookId = b != null ? b.getId() : null;
            return CartItemDTO.builder()
                    .id(ci.getId())
                    .variantId(v != null ? v.getId() : null)
                    .bookId(bookId)
                    .copyId(ci.getCopyId())
                    .quantity(ci.getQuantity())
                    .title(b != null ? b.getTitle() : (v != null ? v.getSku() : null))
                    .sku(v != null ? v.getSku() : null)
                    .salePrice(v != null ? v.getSalePrice() : null)
                    .coverImageUrl(bookId != null ? coverByBookId.get(bookId) : null)
                    .build();
        }).collect(Collectors.toList());
    }

    private Map<Long, String> loadCoverUrlsByBookIds(Set<Long> bookIds) {
        String sql = """
                SELECT bi.book_id, bi.url
                FROM book_images bi
                INNER JOIN (
                    SELECT book_id, MIN(sort_order) AS min_sort_order
                    FROM book_images
                    WHERE deleted_at IS NULL
                      AND is_cover = 1
                      AND book_id IN (:bookIds)
                    GROUP BY book_id
                ) x ON x.book_id = bi.book_id AND x.min_sort_order = bi.sort_order
                WHERE bi.deleted_at IS NULL
                  AND bi.is_cover = 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("bookIds", bookIds);
        Map<Long, String> result = new HashMap<>();
        namedParameterJdbcTemplate.query(sql, params, rs -> {
            long bid = rs.getLong("book_id");
            if (!rs.wasNull() && !result.containsKey(bid)) {
                result.put(bid, rs.getString("url"));
            }
        });
        return result;
    }

    // ✅ ADD ITEM
    @PostMapping("/items")
    public CartItem addToCart(@RequestBody AddToCartRequest req, Authentication auth) {
        String email = auth.getName();
        return cartService.addItemByEmail(email, req);
    }

    // ✅ UPDATE ITEM
    @PutMapping("/items/{variantId}")
    public CartItem updateItem(@PathVariable Long variantId,
                               @RequestBody UpdateCartItemRequest request,
                               Authentication auth) {
        String email = auth.getName();
        return cartService.updateItemByEmail(email, variantId, request);
    }

    // ✅ DELETE ITEM
    @DeleteMapping("/items/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long variantId, Authentication auth) {
        String email = auth.getName();
        cartService.removeItemByEmail(email, variantId);
    }
}