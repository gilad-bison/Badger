package com.example.badger;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.badger.dao.UserDAO;
import com.example.badger.models.User;


@Database(entities = {User.class}, version = 1)
public abstract class BadgerDatabase extends RoomDatabase {
    public abstract UserDAO getUserDAO();
}