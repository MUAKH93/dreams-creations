package com.dreams.dreamscreations.entity.shop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopSettings {

    @Id
    @Column(name = "settings_id")
    private Long settingsId = 1L;

    @Column(name = "store_name", nullable = false, length = 120)
    private String storeName = "Dreams Creations Shop";

    @Column(name = "tagline", length = 255)
    private String tagline;

    @Column(name = "storefront_enabled", nullable = false)
    private Boolean storefrontEnabled = true;

    @Column(name = "allow_guest_browse", nullable = false)
    private Boolean allowGuestBrowse = true;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
