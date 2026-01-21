package info.zhihui.ems.iot.infrastructure.registry;

import java.util.Objects;

final class VendorProductKey {

    private final String vendor;
    private final String product;

    VendorProductKey(String vendor, String product) {
        this.vendor = vendor;
        this.product = product;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        VendorProductKey that = (VendorProductKey) other;
        return Objects.equals(vendor, that.vendor) && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor, product);
    }
}
