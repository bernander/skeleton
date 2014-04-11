package com.bernander.mvc1.dao;

import com.bernander.mvc1.entity.User;

public interface UserDAO {

    public User getUser(String login);
    
}
