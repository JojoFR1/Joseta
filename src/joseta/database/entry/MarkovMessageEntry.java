package joseta.database.entry;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "markov_messages")
public class MarkovMessageEntry {
    @DatabaseField(id = true, generatedId = false)
    private long messageId;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private MessageEntry messageEntry;
    @DatabaseField
    private String cleanContent;

    // No-arg constructor required by ORMLite
    private MarkovMessageEntry() {}

    public MarkovMessageEntry(long messageId, MessageEntry messageEntry, String cleanContent) {
        this.messageId = messageId;
        this.messageEntry = messageEntry;
        this.cleanContent = cleanContent;
    }

    public long getMessageId() { return messageId; }
    public MarkovMessageEntry setMessageId(long messageId) { this.messageId = messageId; return this; }
    
    public MessageEntry getMessageEntry() { return messageEntry; }
    public MarkovMessageEntry setMessageEntry(MessageEntry messageEntry) { this.messageEntry = messageEntry; return this; }
    
    public String getCleanContent() { return cleanContent; }
    public MarkovMessageEntry setCleanContent(String cleanContent) { this.cleanContent = cleanContent; return this; }
}
