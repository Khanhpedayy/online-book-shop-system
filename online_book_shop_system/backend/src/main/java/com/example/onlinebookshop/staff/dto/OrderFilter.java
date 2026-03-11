package com.example.onlinebookshop.staff.dto;

/**
 * Bộ lọc cho danh sách đơn hàng (STAFF).
 *
 * Lưu ý: dùng String để đơn giản (không cần enum), vì DB cũng đang lưu dạng VARCHAR.
 */
public class OrderFilter {

    private String status;         // NEW/CONFIRMED/PACKED/... (optional)
    private String paymentStatus;  // PENDING/PAID/... (optional)
    private String delivery;       // NOT_SHIPPED/IN_TRANSIT/DELIVERED/RETURNED (optional)
    private String q;              // keyword: order_code / ship_name / ship_phone
    private String sort;           // placedAtDesc / placedAtAsc / totalDesc / totalAsc

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
}