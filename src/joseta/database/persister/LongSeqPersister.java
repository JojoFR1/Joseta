package joseta.database.persister;

import arc.struct.*;

import java.sql.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.field.types.*;
import com.j256.ormlite.support.*;

public class LongSeqPersister extends StringType {
    private static final LongSeqPersister singleton = new LongSeqPersister();

    public static LongSeqPersister getSingleton() {
        return singleton;
    }

    private LongSeqPersister() {
        super(SqlType.STRING, new Class<?>[] { Seq.class });
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) {
        return stringToSeq(defaultStr);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        String string = results.getString(columnPos);
        return stringToSeq(string);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String string = sqlArg.toString();
        return stringToSeq(string);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        Seq<Long> seq = (Seq<Long>) javaObject;
        return seqToString(seq);
    }

    private String seqToString(Seq<Long> seq) {
        if (seq == null || seq.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (long item : seq) {
            if (sb.length() > 0) sb.append(',');
            sb.append(Long.toString(item));
        }

        return sb.toString();
    }

    private Seq<Long> stringToSeq(String str) {
        Seq<Long> seq = new Seq<>();
        str = str.replace('[', '\0').replace(']', '\0');
        if (str == null || str.isEmpty()) return seq;

        String[] items = str.split(",");
        for (String item : items) {
            item = item.trim();
            if (!item.isEmpty()) seq.add(Long.parseLong(item));
        }

        return seq;
    }
}
