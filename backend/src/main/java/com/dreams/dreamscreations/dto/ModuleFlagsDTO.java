package com.dreams.dreamscreations.dto;

public class ModuleFlagsDTO {

    private ModuleInfo finance;
    private ModuleInfo shop;

    public ModuleFlagsDTO() {
    }

    public ModuleFlagsDTO(ModuleInfo finance, ModuleInfo shop) {
        this.finance = finance;
        this.shop = shop;
    }

    public ModuleInfo getFinance() {
        return finance;
    }

    public void setFinance(ModuleInfo finance) {
        this.finance = finance;
    }

    public ModuleInfo getShop() {
        return shop;
    }

    public void setShop(ModuleInfo shop) {
        this.shop = shop;
    }

    public static class ModuleInfo {
        private boolean enabled;

        public ModuleInfo() {
        }

        public ModuleInfo(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
