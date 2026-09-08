package com.petstore.order.dto;

import com.petstore.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "Delivery address ID is required")
    private Long addressId;

    private PaymentMethod paymentMethod = PaymentMethod.COD;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long addressId, PaymentMethod paymentMethod) {
        this.addressId = addressId;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.COD;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
