package com.example.onlinebookshop.staff.service;

public class StaffDashboardStats {
    private long newOrders;
    private long pendingPayments;
    private long toPack;
    private long shippedToday;
    private long overdue;

    public StaffDashboardStats(long newOrders, long pendingPayments, long toPack, long shippedToday, long overdue) {
        this.newOrders = newOrders;
        this.pendingPayments = pendingPayments;
        this.toPack = toPack;
        this.shippedToday = shippedToday;
        this.overdue = overdue;
    }

    public long getNewOrders() { return newOrders; }
    public long getPendingPayments() { return pendingPayments; }
    public long getToPack() { return toPack; }
    public long getShippedToday() { return shippedToday; }
    public long getOverdue() { return overdue; }
}