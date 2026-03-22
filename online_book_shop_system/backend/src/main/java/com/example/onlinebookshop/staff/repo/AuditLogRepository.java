package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuditLogRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void log(Long actorUserId,
                    String action,
                    String entityTable,
                    Long entityId,
                    String changesJson,
                    String note) {

        String sql = """
            INSERT INTO dbo.audit_logs(actor_user_id, action, entity_table, entity_id, changes_json, note)
            VALUES (:actor, :action, :table, :eid, :changes, :note)
            """;

        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("actor", actorUserId)
                .addValue("action", action)
                .addValue("table", entityTable)
                .addValue("eid", entityId)
                .addValue("changes", changesJson)
                .addValue("note", note);

        jdbc.update(sql, ps);
    }
}