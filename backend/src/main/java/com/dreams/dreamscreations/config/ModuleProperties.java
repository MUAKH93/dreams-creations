package com.dreams.dreamscreations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "modules")
public class ModuleProperties {

    private Finance finance = new Finance();
    private Shop shop = new Shop();

    public Finance getFinance() {
        return finance;
    }

    public void setFinance(Finance finance) {
        this.finance = finance;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public static class Finance {
        private boolean enabled = false;
        private boolean autoPostAr = false;
        private boolean autoPostInventory = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAutoPostAr() {
            return autoPostAr;
        }

        public void setAutoPostAr(boolean autoPostAr) {
            this.autoPostAr = autoPostAr;
        }

        public boolean isAutoPostInventory() {
            return autoPostInventory;
        }

        public void setAutoPostInventory(boolean autoPostInventory) {
            this.autoPostInventory = autoPostInventory;
        }
    }

    public static class Shop {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
