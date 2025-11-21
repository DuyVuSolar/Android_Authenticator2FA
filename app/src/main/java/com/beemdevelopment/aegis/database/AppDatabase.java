package com.beemdevelopment.aegis.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.beemdevelopment.aegis.data.db.MyCreationDao;
import com.beemdevelopment.aegis.model.MyGalleryModel;

@Database(entities = {AuditLogEntry.class, MyGalleryModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AuditLogDao auditLogDao();
    public abstract MyCreationDao myCreationDao();
}