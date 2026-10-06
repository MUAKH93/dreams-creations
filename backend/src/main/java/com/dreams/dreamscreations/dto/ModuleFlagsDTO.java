package com.dreams.dreamscreations.dto;

public class ModuleFlagsDTO {

    private ModuleInfo finance;

    public ModuleFlagsDTO() {
    }

    public ModuleFlagsDTO(ModuleInfo finance) {
        this.finance = finance;
    }

    public ModuleInfo getFinance() {
        return finance;
    }

    public void setFinance(ModuleInfo finance) {
        this.finance = finance;
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
