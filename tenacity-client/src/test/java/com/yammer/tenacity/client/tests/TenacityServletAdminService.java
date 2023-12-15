package com.yammer.tenacity.client.tests;

import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import io.dropwizard.core.Configuration;

public class TenacityServletAdminService extends TenacityServletService {
    @Override
    protected TenacityBundleBuilder<Configuration> tenacityBundleBuilder() {
        return super.tenacityBundleBuilder().usingAdminPort();
    }
}
