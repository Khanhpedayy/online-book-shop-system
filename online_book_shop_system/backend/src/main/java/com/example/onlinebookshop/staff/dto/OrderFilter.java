package com.example.onlinebookshop.staff.dto;

public class OrderFilter {

    private String status;
    private String paymentStatus;
    private String delivery;
    private String q;
    private String sort;
    private String stage;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDelivery() { return delivery; }
    public void setDelivery(String delivery) { this.delivery = delivery; }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
}