package com.example.onlinebookshop.wallet;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class WalletRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public WalletRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- BALANCE ----------

    public BigDecimal getBalance(long userId) {
        String sql = "SELECT wallet_balance FROM dbo.users WHERE id = :uid AND deleted_at IS NULL";
        List<BigDecimal> rows = jdbc.queryForList(sql,
                new MapSqlParameterSource("uid", userId), BigDecimal.class);
        return rows.isEmpty() ? BigDecimal.ZERO : rows.get(0);
    }

    /**
     * Credit ví (tăng số dư). Trả về số dư mới sau khi cộng.
     * Dùng optimistic UPDATE: chỉ update nếu row tồn tại.
     */
    public BigDecimal creditBalance(long userId, BigDecimal amount) {
        String sql = """
            UPDATE dbo.users
            SET wallet_balance = wallet_balance + :amount,
                updated_at = GETDATE()
            WHERE id = :uid
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("uid", userId)
                .addValue("amount", amount));
        return getBalance(userId);
    }

    /**
     * Debit ví (giảm số dư). Chỉ thực hiện nếu balance >= amount.
     * Trả về số dư mới, hoặc throw nếu không đủ tiền.
     */
    public BigDecimal debitBalance(long userId, BigDecimal amount) {
        String sql = """
            UPDATE dbo.users
            SET wallet_balance = wallet_balance - :amount,
                updated_at = GETDATE()
            WHERE id = :uid
              AND deleted_at IS NULL
              AND wallet_balance >= :amount
            """;
        int updated = jdbc.update(sql, new MapSqlParameterSource()
                .addValue("uid", userId)
                .addValue("amount", amount));
        if (updated == 0) {
            throw new RuntimeException("Số dư ví không đủ để thực hiện giao dịch.");
        }
        return getBalance(userId);
    }

    // ---------- TRANSACTIONS ----------

    public Long insertTransaction(long userId, String type, BigDecimal amount,
                                  BigDecimal balanceAfter, String refType, Long refId, String note) {
        String sql = """
            INSERT INTO dbo.wallet_transactions
                (user_id, type, amount, balance_after, ref_type, ref_id, note)
            OUTPUT INSERTED.id
            VALUES (:uid, :type, :amount, :balanceAfter, :refType, :refId, :note)
            """;
        List<Long> ids = jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("uid", userId)
                        .addValue("type", type)
                        .addValue("amount", amount)
                        .addValue("balanceAfter", balanceAfter)
                        .addValue("refType", refType)
                        .addValue("refId", refId)
                        .addValue("note", note),
                (rs, rn) -> rs.getLong(1));
        return ids.isEmpty() ? null : ids.get(0);
    }

    public List<WalletTxRow> getTransactions(long userId, int limit) {
        String sql = """
            SELECT TOP (:limit)
                id, type, amount, balance_after, ref_type, ref_id, note, created_at
            FROM dbo.wallet_transactions
            WHERE user_id = :uid
            ORDER BY created_at DESC
            """;
        return jdbc.query(sql,
                new MapSqlParameterSource("uid", userId).addValue("limit", limit),
                (rs, rn) -> new WalletTxRow(
                        rs.getLong("id"),
                        rs.getString("type"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("balance_after"),
                        rs.getString("ref_type"),
                        rs.getObject("ref_id") == null ? null : rs.getLong("ref_id"),
                        rs.getString("note"),
                        rs.getTimestamp("created_at") == null ? null
                                : rs.getTimestamp("created_at").toLocalDateTime()
                ));
    }

    // ---------- WITHDRAWAL ----------

    public Long insertWithdrawalRequest(long userId, BigDecimal amount,
                                        String bankName, String bankAccountNumber,
                                        String bankAccountName, Long walletTxId, String requestCode) {
        String sql = """
            INSERT INTO dbo.withdrawal_requests
                (request_code, user_id, amount, bank_name, bank_account_number, bank_account_name,
                 status, wallet_tx_id)
            OUTPUT INSERTED.id
            VALUES (:code, :uid, :amount, :bankName, :bankAccNo, :bankAccName, 'PENDING', :txId)
            """;
        List<Long> ids = jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("code", requestCode)
                        .addValue("uid", userId)
                        .addValue("amount", amount)
                        .addValue("bankName", bankName)
                        .addValue("bankAccNo", bankAccountNumber)
                        .addValue("bankAccName", bankAccountName)
                        .addValue("txId", walletTxId),
                (rs, rn) -> rs.getLong(1));
        return ids.isEmpty() ? null : ids.get(0);
    }

    public boolean hasPendingWithdrawal(long userId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.withdrawal_requests
            WHERE user_id = :uid
              AND status = 'PENDING'
              AND deleted_at IS NULL
            """;
        Integer count = jdbc.queryForObject(sql, new MapSqlParameterSource("uid", userId), Integer.class);
        return count != null && count > 0;
    }

    public List<WithdrawalRow> getWithdrawalsByUser(long userId) {
        String sql = """
            SELECT id, request_code, amount, bank_name, bank_account_number, bank_account_name,
                   status, admin_note, processed_at, created_at
            FROM dbo.withdrawal_requests
            WHERE user_id = :uid
              AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;
        return jdbc.query(sql, new MapSqlParameterSource("uid", userId), this::mapWithdrawalRow);
    }

    public List<WithdrawalRow> getAllPending() {
        String sql = """
            SELECT wr.id, wr.request_code, wr.amount, wr.bank_name, wr.bank_account_number,
                   wr.bank_account_name, wr.status, wr.admin_note, wr.processed_at, wr.created_at,
                   u.full_name AS user_name, u.email AS user_email, wr.user_id
            FROM dbo.withdrawal_requests wr
            JOIN dbo.users u ON u.id = wr.user_id
            WHERE wr.status = 'PENDING'
              AND wr.deleted_at IS NULL
            ORDER BY wr.created_at ASC
            """;
        return jdbc.query(sql, new MapSqlParameterSource(), this::mapWithdrawalRow);
    }

    /**
     * Lấy tất cả withdrawal requests, có thể filter theo status.
     * status = null hoặc "" → lấy tất cả mọi trạng thái.
     */
    public List<WithdrawalRow> getAll(String status) {
        boolean filterByStatus = status != null && !status.isBlank();
        String sql = """
            SELECT wr.id, wr.request_code, wr.amount, wr.bank_name, wr.bank_account_number,
                   wr.bank_account_name, wr.status, wr.admin_note, wr.processed_at, wr.created_at,
                   u.full_name AS user_name, u.email AS user_email, wr.user_id
            FROM dbo.withdrawal_requests wr
            JOIN dbo.users u ON u.id = wr.user_id
            WHERE wr.deleted_at IS NULL
            """ + (filterByStatus ? " AND wr.status = :status " : "") + """
            ORDER BY wr.created_at DESC
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filterByStatus && status != null) {
            params.addValue("status", status.trim().toUpperCase());
        }
        return jdbc.query(sql, params, this::mapWithdrawalRow);
    }

    public Optional<WithdrawalRow> findById(long id) {
        String sql = """
            SELECT wr.id, wr.request_code, wr.amount, wr.bank_name, wr.bank_account_number,
                   wr.bank_account_name, wr.status, wr.admin_note, wr.processed_at, wr.created_at,
                   u.full_name AS user_name, u.email AS user_email, wr.user_id
            FROM dbo.withdrawal_requests wr
            JOIN dbo.users u ON u.id = wr.user_id
            WHERE wr.id = :id
              AND wr.deleted_at IS NULL
            """;
        List<WithdrawalRow> rows = jdbc.query(sql, new MapSqlParameterSource("id", id), this::mapWithdrawalRow);
        return rows.stream().findFirst();
    }

    public int updateWithdrawalStatus(long id, String status, Long processedBy, String adminNote) {
        String sql = """
            UPDATE dbo.withdrawal_requests
            SET status       = :status,
                processed_by = :processedBy,
                processed_at = SYSUTCDATETIME(),
                admin_note   = :adminNote,
                updated_at   = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
              AND status = 'PENDING'
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status)
                .addValue("processedBy", processedBy)
                .addValue("adminNote", adminNote));
    }

    private WithdrawalRow mapWithdrawalRow(java.sql.ResultSet rs, int rn) throws java.sql.SQLException {
        WithdrawalRow row = new WithdrawalRow();
        row.setId(rs.getLong("id"));
        row.setRequestCode(rs.getString("request_code"));
        row.setAmount(rs.getBigDecimal("amount"));
        row.setBankName(rs.getString("bank_name"));
        row.setBankAccountNumber(rs.getString("bank_account_number"));
        row.setBankAccountName(rs.getString("bank_account_name"));
        row.setStatus(rs.getString("status"));
        row.setAdminNote(rs.getString("admin_note"));
        row.setCreatedAt(rs.getTimestamp("created_at") == null ? null
                : rs.getTimestamp("created_at").toLocalDateTime());
        row.setProcessedAt(rs.getTimestamp("processed_at") == null ? null
                : rs.getTimestamp("processed_at").toLocalDateTime());
        // user info (only in admin queries)
        try { row.setUserId(rs.getLong("user_id")); } catch (Exception ignored) {}
        try { row.setUserName(rs.getString("user_name")); } catch (Exception ignored) {}
        try { row.setUserEmail(rs.getString("user_email")); } catch (Exception ignored) {}
        return row;
    }

    // ---------- DTO records ----------

    public record WalletTxRow(
            long id, String type, java.math.BigDecimal amount, java.math.BigDecimal balanceAfter,
            String refType, Long refId, String note, java.time.LocalDateTime createdAt
    ) {}
}
