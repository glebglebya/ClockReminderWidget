package com.example.clockreminder.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.clockreminder.model.IntervalUnit;
import com.example.clockreminder.model.Reminder;
import com.example.clockreminder.model.RepeatType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderDao_Impl implements ReminderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Reminder> __insertionAdapterOfReminder;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Reminder> __deletionAdapterOfReminder;

  private final EntityDeletionOrUpdateAdapter<Reminder> __updateAdapterOfReminder;

  public ReminderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminder = new EntityInsertionAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reminders` (`id`,`label`,`colorArgb`,`repeatType`,`startEpochMillis`,`intervalValue`,`intervalUnit`,`endEpochMillis`,`enabled`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getLabel());
        statement.bindLong(3, entity.getColorArgb());
        final String _tmp = __converters.fromRepeatType(entity.getRepeatType());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getStartEpochMillis());
        statement.bindLong(6, entity.getIntervalValue());
        final String _tmp_1 = __converters.fromIntervalUnit(entity.getIntervalUnit());
        statement.bindString(7, _tmp_1);
        if (entity.getEndEpochMillis() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getEndEpochMillis());
        }
        final int _tmp_2 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
      }
    };
    this.__deletionAdapterOfReminder = new EntityDeletionOrUpdateAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `reminders` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfReminder = new EntityDeletionOrUpdateAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reminders` SET `id` = ?,`label` = ?,`colorArgb` = ?,`repeatType` = ?,`startEpochMillis` = ?,`intervalValue` = ?,`intervalUnit` = ?,`endEpochMillis` = ?,`enabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getLabel());
        statement.bindLong(3, entity.getColorArgb());
        final String _tmp = __converters.fromRepeatType(entity.getRepeatType());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getStartEpochMillis());
        statement.bindLong(6, entity.getIntervalValue());
        final String _tmp_1 = __converters.fromIntervalUnit(entity.getIntervalUnit());
        statement.bindString(7, _tmp_1);
        if (entity.getEndEpochMillis() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getEndEpochMillis());
        }
        final int _tmp_2 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Reminder reminder, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReminder.insertAndReturnId(reminder);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Reminder reminder, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfReminder.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Reminder reminder, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReminder.handle(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Reminder>> observeAll() {
    final String _sql = "SELECT * FROM reminders ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reminders"}, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfColorArgb = CursorUtil.getColumnIndexOrThrow(_cursor, "colorArgb");
          final int _cursorIndexOfRepeatType = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatType");
          final int _cursorIndexOfStartEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startEpochMillis");
          final int _cursorIndexOfIntervalValue = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalValue");
          final int _cursorIndexOfIntervalUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalUnit");
          final int _cursorIndexOfEndEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "endEpochMillis");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final int _tmpColorArgb;
            _tmpColorArgb = _cursor.getInt(_cursorIndexOfColorArgb);
            final RepeatType _tmpRepeatType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRepeatType);
            _tmpRepeatType = __converters.toRepeatType(_tmp);
            final long _tmpStartEpochMillis;
            _tmpStartEpochMillis = _cursor.getLong(_cursorIndexOfStartEpochMillis);
            final int _tmpIntervalValue;
            _tmpIntervalValue = _cursor.getInt(_cursorIndexOfIntervalValue);
            final IntervalUnit _tmpIntervalUnit;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfIntervalUnit);
            _tmpIntervalUnit = __converters.toIntervalUnit(_tmp_1);
            final Long _tmpEndEpochMillis;
            if (_cursor.isNull(_cursorIndexOfEndEpochMillis)) {
              _tmpEndEpochMillis = null;
            } else {
              _tmpEndEpochMillis = _cursor.getLong(_cursorIndexOfEndEpochMillis);
            }
            final boolean _tmpEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_2 != 0;
            _item = new Reminder(_tmpId,_tmpLabel,_tmpColorArgb,_tmpRepeatType,_tmpStartEpochMillis,_tmpIntervalValue,_tmpIntervalUnit,_tmpEndEpochMillis,_tmpEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllEnabled(final Continuation<? super List<Reminder>> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE enabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfColorArgb = CursorUtil.getColumnIndexOrThrow(_cursor, "colorArgb");
          final int _cursorIndexOfRepeatType = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatType");
          final int _cursorIndexOfStartEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startEpochMillis");
          final int _cursorIndexOfIntervalValue = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalValue");
          final int _cursorIndexOfIntervalUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalUnit");
          final int _cursorIndexOfEndEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "endEpochMillis");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final int _tmpColorArgb;
            _tmpColorArgb = _cursor.getInt(_cursorIndexOfColorArgb);
            final RepeatType _tmpRepeatType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRepeatType);
            _tmpRepeatType = __converters.toRepeatType(_tmp);
            final long _tmpStartEpochMillis;
            _tmpStartEpochMillis = _cursor.getLong(_cursorIndexOfStartEpochMillis);
            final int _tmpIntervalValue;
            _tmpIntervalValue = _cursor.getInt(_cursorIndexOfIntervalValue);
            final IntervalUnit _tmpIntervalUnit;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfIntervalUnit);
            _tmpIntervalUnit = __converters.toIntervalUnit(_tmp_1);
            final Long _tmpEndEpochMillis;
            if (_cursor.isNull(_cursorIndexOfEndEpochMillis)) {
              _tmpEndEpochMillis = null;
            } else {
              _tmpEndEpochMillis = _cursor.getLong(_cursorIndexOfEndEpochMillis);
            }
            final boolean _tmpEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_2 != 0;
            _item = new Reminder(_tmpId,_tmpLabel,_tmpColorArgb,_tmpRepeatType,_tmpStartEpochMillis,_tmpIntervalValue,_tmpIntervalUnit,_tmpEndEpochMillis,_tmpEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
