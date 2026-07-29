package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopCartDTO;
import com.dreams.dreamscreations.dto.shop.ShopCartItemDTO;
import com.dreams.dreamscreations.dto.shop.ShopCartItemRequest;
import com.dreams.dreamscreations.entity.Customer;
import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.entity.shop.ShopCart;
import com.dreams.dreamscreations.entity.shop.ShopCartItem;
import com.dreams.dreamscreations.repository.CustomerRepository;
import com.dreams.dreamscreations.repository.DesignImageRepository;
import com.dreams.dreamscreations.repository.ProductRepository;
import com.dreams.dreamscreations.repository.shop.ShopCartRepository;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopCartServiceImpl implements ShopCartService {

    private final ShopCartRepository cartRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final DesignImageRepository designImageRepo;
    private final InventoryService inventoryService;

    public ShopCartServiceImpl(ShopCartRepository cartRepo,
                                 CustomerRepository customerRepo,
                                 ProductRepository productRepo,
                                 DesignImageRepository designImageRepo,
                                 InventoryService inventoryService) {
        this.cartRepo = cartRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.designImageRepo = designImageRepo;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public ShopCartDTO getCart(Long customerId) {
        return cartRepo.findByCustomerIdWithItems(customerId)
                .map(cart -> toDto(cart, loadCustomer(customerId)))
                .orElse(emptyDto(customerId));
    }

    @Override
    @Transactional
    public ShopCartDTO addItem(Long customerId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        Product product = loadActiveProduct(productId);
        int stock = inventoryService.getQuantity(product.getSuit());

        ShopCart cart = getOrCreateCart(customerId);
        ShopCartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        int newQty = existing != null ? existing.getQuantity() + quantity : quantity;
        if (newQty > stock) {
            throw new RuntimeException("Only " + stock + " available in stock");
        }

        BigDecimal unitPrice = resolveUnitPrice(product);
        if (existing != null) {
            existing.setQuantity(newQty);
            existing.setUnitPrice(unitPrice);
        } else {
            ShopCartItem item = ShopCartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .build();
            cart.getItems().add(item);
        }

        cartRepo.save(cart);
        return getCart(customerId);
    }

    @Override
    @Transactional
    public ShopCartDTO updateItemQuantity(Long customerId, Long itemId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        ShopCart cart = loadCart(customerId);
        ShopCartItem item = findItem(cart, itemId);
        int stock = inventoryService.getQuantity(item.getProduct().getSuit());
        if (quantity > stock) {
            throw new RuntimeException("Only " + stock + " available in stock");
        }
        item.setQuantity(quantity);
        cartRepo.save(cart);
        return getCart(customerId);
    }

    @Override
    @Transactional
    public ShopCartDTO removeItem(Long customerId, Long itemId) {
        ShopCart cart = loadCart(customerId);
        cart.getItems().removeIf(i -> i.getItemId().equals(itemId));
        cartRepo.save(cart);
        return getCart(customerId);
    }

    @Override
    @Transactional
    public ShopCartDTO clearCart(Long customerId) {
        ShopCart cart = loadCart(customerId);
        cart.getItems().clear();
        cartRepo.save(cart);
        return getCart(customerId);
    }

    @Override
    @Transactional
    public ShopCartDTO mergeGuestItems(Long customerId, List<ShopCartItemRequest> guestItems) {
        if (guestItems == null) {
            return getCart(customerId);
        }
        for (ShopCartItemRequest guest : guestItems) {
            if (guest.getProductId() == null || guest.getQuantity() == null || guest.getQuantity() <= 0) {
                continue;
            }
            try {
                addItem(customerId, guest.getProductId(), guest.getQuantity());
            } catch (RuntimeException ignored) {
                // Skip invalid or out-of-stock guest lines during merge
            }
        }
        return getCart(customerId);
    }

    private ShopCart getOrCreateCart(Long customerId) {
        return cartRepo.findByCustomerIdWithItems(customerId)
                .orElseGet(() -> {
                    Customer customer = loadCustomer(customerId);
                    ShopCart cart = ShopCart.builder().customer(customer).build();
                    return cartRepo.save(cart);
                });
    }

    private ShopCart loadCart(Long customerId) {
        return cartRepo.findByCustomerIdWithItems(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    private ShopCartItem findItem(ShopCart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
    }

    private Customer loadCustomer(Long customerId) {
        return customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
    }

    private Product loadActiveProduct(Long productId) {
        Product product = productRepo.findByIdWithSuit(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        if (!"active".equalsIgnoreCase(product.getStatus())) {
            throw new RuntimeException("Product is not available");
        }
        Suit suit = product.getSuit();
        if (suit == null || suit.getDesign() == null
                || !"active".equalsIgnoreCase(suit.getDesign().getStatus())) {
            throw new RuntimeException("Product is not available in the shop");
        }
        return product;
    }

    private BigDecimal resolveUnitPrice(Product product) {
        if (product.getSellingPrice() != null) {
            return product.getSellingPrice();
        }
        Design design = product.getSuit().getDesign();
        if (design.getBasePrice() != null) {
            return design.getBasePrice();
        }
        return BigDecimal.ZERO;
    }

    private ShopCartDTO emptyDto(Long customerId) {
        Customer customer = loadCustomer(customerId);
        ShopCartDTO dto = new ShopCartDTO();
        dto.setDiscountPercent(nz(customer.getDiscountPercent()));
        dto.setSubtotal(BigDecimal.ZERO);
        dto.setDiscountAmount(BigDecimal.ZERO);
        dto.setTotal(BigDecimal.ZERO);
        return dto;
    }

    private ShopCartDTO toDto(ShopCart cart, Customer customer) {
        ShopCartDTO dto = new ShopCartDTO();
        dto.setCartId(cart.getCartId());
        BigDecimal discountPct = nz(customer.getDiscountPercent());
        dto.setDiscountPercent(discountPct);

        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        List<ShopCartItem> sorted = cart.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getProductId()))
                .toList();

        Set<Long> designIds = sorted.stream()
                .map(i -> i.getProduct().getSuit())
                .filter(Objects::nonNull)
                .map(Suit::getDesign)
                .filter(Objects::nonNull)
                .map(Design::getDesignId)
                .collect(Collectors.toSet());
        Map<Long, String> primaryImageUrls = loadPrimaryImageUrls(designIds);

        for (ShopCartItem item : sorted) {
            Product product = item.getProduct();
            Suit suit = product.getSuit();
            Design design = suit != null ? suit.getDesign() : null;
            int stock = suit != null ? inventoryService.getQuantity(suit) : 0;
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            ShopCartItemDTO line = new ShopCartItemDTO();
            line.setItemId(item.getItemId());
            line.setProductId(product.getProductId());
            if (design != null) {
                line.setDesignId(design.getDesignId());
                line.setDesignCode(design.getDesignCode());
                line.setDesignName(design.getName());
                line.setPrimaryImageUrl(primaryImageUrls.get(design.getDesignId()));
            }
            if (suit != null) {
                line.setSizeValue(suit.getSize() != null ? suit.getSize().getSizeValue() : null);
                line.setColor(suit.getColor());
            }
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getUnitPrice());
            line.setLineTotal(lineTotal);
            line.setStockAvailable(stock);
            line.setInStock(stock >= item.getQuantity());

            dto.getItems().add(line);
            subtotal = subtotal.add(lineTotal);
            count += item.getQuantity();
        }

        BigDecimal discountAmount = subtotal.multiply(discountPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        dto.setItemCount(count);
        dto.setSubtotal(subtotal);
        dto.setDiscountAmount(discountAmount);
        dto.setTotal(subtotal.subtract(discountAmount).max(BigDecimal.ZERO));
        return dto;
    }

    private Map<Long, String> loadPrimaryImageUrls(Set<Long> designIds) {
        if (designIds == null || designIds.isEmpty()) {
            return Map.of();
        }
        List<DesignImage> images = designImageRepo.findByDesign_DesignIdIn(designIds);
        Map<Long, String> urls = new HashMap<>();
        images.stream()
                .sorted(Comparator
                        .comparing((DesignImage img) -> !Boolean.TRUE.equals(img.getIsPrimary()))
                        .thenComparing(img -> img.getDisplayOrder() != null ? img.getDisplayOrder() : 0))
                .forEach(img -> {
                    Long designId = img.getDesign().getDesignId();
                    urls.putIfAbsent(designId, buildImageUrl(img.getImageName()));
                });
        return urls;
    }

    private String buildImageUrl(String imageName) {
        if (imageName == null) {
            return null;
        }
        String encoded = java.net.URLEncoder.encode(imageName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "/api/design-images/view/" + encoded;
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
