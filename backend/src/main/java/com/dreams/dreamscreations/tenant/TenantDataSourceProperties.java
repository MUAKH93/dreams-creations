package com.dreams.dreamscreations.tenant;

import lombok.Data;

@Data
public class TenantDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
}
