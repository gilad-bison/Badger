package com.example.badger.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;
import androidx.room.Query;
import java.util.List;
import com.example.badger.models.User;

@Dao
public interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(User... items);
    @Update
    public void update(User... items);
    @Delete
    public void delete(User user);
    @Query("SELECT * FROM users")
    public List<User> getUsers();
    @Query("SELECT * FROM users WHERE uid = :uid")
    public User getUserById(String uid);
}
