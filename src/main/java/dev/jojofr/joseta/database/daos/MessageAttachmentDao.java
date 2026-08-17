package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.entities.MessageAttachmentEntity;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface MessageAttachmentDao {
    @SqlUpdate("""
        INSERT INTO message_attachments (id, message_id, filename, extension, description, content_type, url, proxy_url, size, width, height)
        VALUES (:id, :messageId, :filename, :extension, :description, :contentType, :url, :proxyUrl, :size, :width, :height)
        ON CONFLICT (id) DO UPDATE SET
            message_id = EXCLUDED.message_id,
            filename = EXCLUDED.filename,
            extension = EXCLUDED.extension,
            description = EXCLUDED.description,
            content_type = EXCLUDED.content_type,
            url = EXCLUDED.url,
            proxy_url = EXCLUDED.proxy_url,
            size = EXCLUDED.size,
            width = EXCLUDED.width,
            height = EXCLUDED.height
    """)
    void upsert(@BindFields MessageAttachmentEntity attachment);
    
    @SqlBatch("""
        INSERT INTO message_attachments (id, message_id, filename, extension, description, content_type, url, proxy_url, size, width, height)
        VALUES (:id, :messageId, :filename, :extension, :description, :contentType, :url, :proxyUrl, :size, :width, :height)
        ON CONFLICT (id) DO UPDATE SET
            message_id = EXCLUDED.message_id,
            filename = EXCLUDED.filename,
            extension = EXCLUDED.extension,
            description = EXCLUDED.description,
            content_type = EXCLUDED.content_type,
            url = EXCLUDED.url,
            proxy_url = EXCLUDED.proxy_url,
            size = EXCLUDED.size,
            width = EXCLUDED.width,
            height = EXCLUDED.height
    """)
    void upsertAll(@BindFields Iterable<MessageAttachmentEntity> attachments);
    
    @SqlUpdate("DELETE FROM message_attachments WHERE message_id = :messageId")
    void deleteByMessageId(long messageId);
}