package com.example.badger.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import java.util.List;
import com.example.badger.models.User;

@Dao
public interface UserDAO {
    @Insert
    public void insert(User... items);
    @Update
    public void update(User... items);
    @Delete
    public void delete(User item);
    @Query("SELECT * FROM users")
    public List<User> getPosts();
    @Query("SELECT * FROM users WHERE uid = :uid")
    public User getUserById(String uid);
}
